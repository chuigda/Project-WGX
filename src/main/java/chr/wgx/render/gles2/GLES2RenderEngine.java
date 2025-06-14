package chr.wgx.render.gles2;

import chr.wgx.config.Config;
import chr.wgx.render.RenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.data.*;
import chr.wgx.render.gles2.data.*;
import chr.wgx.render.gles2.glext.EXT_draw_buffers;
import chr.wgx.render.gles2.glext.EXT_texture_storage;
import chr.wgx.render.gles2.glext.KHR_debug;
import chr.wgx.render.gles2.task.GLES2RenderPass;
import chr.wgx.render.info.*;
import chr.wgx.render.task.RenderPass;
import org.jetbrains.annotations.Nullable;
import club.doki7.gles2.GLES2;
import club.doki7.gles2.GLES2;
import club.doki7.glfw.GLFW;
import club.doki7.glfw.handle.GLFWwindow;
import club.doki7.ffm.RawFunctionLoader;
import club.doki7.ffm.ptr.BytePtr;
import club.doki7.ffm.ptr.IntPtr;
import tech.icey.xjbutil.container.Either;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.sync.Oneshot;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public final class GLES2RenderEngine extends RenderEngine {
    GLES2RenderEngine(GLFW glfw, GLFWwindow window) throws RenderException {
        this.glfw = glfw;
        this.window = window;

        glfw.makeContextCurrent(window);
        RawFunctionLoader loadWithGLFW = name -> {
            try (Arena arena = Arena.ofConfined()) {
                return glfw.getProcAddress(BytePtr.allocateString(arena, name));
            }
        };

        this.gles2 = new GLES2(loadWithGLFW);

        @Nullable BytePtr extensions = gles2.getString(GLES2.EXTENSIONS);
        if (extensions != null) {
            String extensionsString = extensions.readString();
            logger.info("支持的 OpenGL ES2 扩展: " + extensionsString);

            if (Config.config().gles2Config.debug && extensionsString.contains(KHR_debug.EXTENSION_NAME)) {
                KHR_debug debugFunctions;
                try {
                    debugFunctions = new KHR_debug(loadWithGLFW);
                    debugFunctions.glDebugMessageControl(
                            GLES2.DONT_CARE,
                            GLES2.DONT_CARE,
                            GLES2.DONT_CARE,
                            0,
                            null,
                            true
                    );
                    debugFunctions.glDebugMessageCallback(GLES2DebugCallback.DEBUG_CALLBACK, null);
                    logger.info("已启用 OpenGL ES2 调试扩展");
                } catch (Throwable e) {
                    logger.warning("找到了 OpenGL ES2 调试扩展, 但是无法初始化调试函数: " + e.getMessage());
                }
            }

            hasEXTDrawBuffers = extensionsString.contains(EXT_draw_buffers.EXTENSION_NAME);
            if (hasEXTDrawBuffers) {
                logger.info("已找到 GL_EXT_draw_buffers 扩展, 将会支持多个颜色附件");
            } else {
                logger.warning("未找到 GL_EXT_draw_buffers 扩展, GLES2 渲染器将只能支持单个颜色附件");
            }

            hasEXTTextureStorage = extensionsString.contains(EXT_texture_storage.EXTENSION_NAME);
            if (hasEXTTextureStorage) {
                logger.info("已找到 GL_EXT_texture_storage 扩展, 将会支持 R32 纹理格式");
            } else {
                logger.warning("未找到 GL_EXT_texture_storage 扩展, GLES2 渲染器将不支持 R32 纹理格式");
            }
        } else {
            hasEXTDrawBuffers = false;
            hasEXTTextureStorage = false;
            logger.warning("无法获取 OpenGL ES2 扩展列表, 所有扩展均不会启用");
        }

        try (Arena arena = Arena.ofConfined()) {
            IntPtr pWidthHeight = IntPtr.allocate(arena, 2);
            glfw.getFramebufferSize(window, pWidthHeight, pWidthHeight.offset(1));

            framebufferWidth = pWidthHeight.read();
            framebufferHeight = pWidthHeight.read(1);
        }

        objectCreateAspect = new ASPECT_ObjectCreate(this);
        attachmentCreateAspect = new ASPECT_AttachmentCreate(this);
        pipelineCreateAspect = new ASPECT_PipelineCreate(this);
        renderPassCreateAspect = new ASPECT_RenderPassCreate(this);

        Pair<Attachment, Texture> colorAttachment = attachmentCreateAspect.createColorAttachmentImpl(
                new AttachmentCreateInfo(
                        PixelFormat.RGBA_OPTIMAL,
                        -1,
                        -1
                )
        );
        defaultColorAttachment = (GLES2TextureAttachment) colorAttachment.first();
        defaultColorAttachmentTexture = (GLES2Texture) colorAttachment.second();
        defaultDepthAttachment = (GLES2TextureAttachment) attachmentCreateAspect.createDepthAttachmentImpl(
                new AttachmentCreateInfo(
                        PixelFormat.DEPTH_BUFFER_OPTIMAL,
                        -1,
                        -1
                )
        );

        renderFrameAspect = new ASPECT_RenderFrame(this);
    }

    public final Arena prefabArena = Arena.ofAuto();

    @Override
    protected void resize(int width, int height) {
        glfw.makeContextCurrent(window);
        this.framebufferWidth = width;
        this.framebufferHeight = height;

        if (width == 0 || height == 0) {
            return;
        }

        for (GLES2Texture texture : dynamicallySizedTextures) {
            gles2.bindTexture(GLES2.TEXTURE_2D, texture.textureObject);
            gles2.texImage2D(
                    GLES2.TEXTURE_2D,
                    0,
                    texture.pixelFormat.glInternalFormat,
                    width,
                    height,
                    0,
                    texture.pixelFormat.glFormat,
                    texture.pixelFormat.glType,
                    MemorySegment.NULL
            );
        }
    }

    @Override
    protected void renderFrame() throws RenderException {
        if (framebufferWidth == 0 || framebufferHeight == 0) {
            return;
        }

        glfw.makeContextCurrent(window);
        for (DeferredTask<?> task : taskQueue.getAndSet(new ArrayList<>())) {
            task.runTask();
        }

        renderFrameAspect.renderFrameImpl();

        glfw.swapBuffers(window);
    }

    @Override
    protected void close() {
        // TODO
    }

    @Override
    public Pair<Integer, Integer> framebufferSize() {
        return new Pair<>(framebufferWidth, framebufferHeight);
    }

    @Override
    public RenderObject createObject(ObjectCreateInfo info) throws RenderException {
        return createObject(List.of(info)).getFirst();
    }

    @Override
    public List<RenderObject> createObject(List<ObjectCreateInfo> info) throws RenderException {
        return invokeWithGLContext(() -> objectCreateAspect.createObjectImpl(info));
    }

    @Override
    public Pair<Attachment, Texture> createColorAttachment(AttachmentCreateInfo i) throws RenderException {
        return invokeWithGLContext(() -> attachmentCreateAspect.createColorAttachmentImpl(i));
    }

    @Override
    public Attachment createDepthAttachment(AttachmentCreateInfo i) throws RenderException {
        return invokeWithGLContext(() -> attachmentCreateAspect.createDepthAttachmentImpl(i));
    }

    @Override
    public Pair<Attachment, Attachment> getDefaultAttachments() {
        return new Pair<>(defaultColorAttachment, defaultDepthAttachment);
    }

    @Override
    public Texture createTexture(TextureCreateInfo image) throws RenderException {
        return null;
    }

    @Override
    public List<Texture> createTexture(List<TextureCreateInfo> images) throws RenderException {
        return List.of();
    }

    @Override
    public UniformBuffer createUniform(UniformBufferCreateInfo info) {
        MemorySegment cpuBuffer = prefabArena.allocate(info.bindingInfo.bufferSize, 16);
        return new GLES2UniformBuffer(info, cpuBuffer);
    }

    @Override
    public List<PushConstant> createPushConstant(PushConstantInfo info, int count) {
        List<PushConstant> ret = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MemorySegment cpuBuffer = prefabArena.allocate(info.bufferSize, 16);
            ret.add(new GLES2PushConstant(info, cpuBuffer));
        }
        return ret;
    }

    @Override
    public DescriptorSetLayout createDescriptorSetLayout(DescriptorSetLayoutCreateInfo info, int maxSets) {
        return new GLES2DescriptorSetLayout(info);
    }

    @Override
    public DescriptorSet createDescriptorSet(DescriptorSetCreateInfo info) {
        return new GLES2DescriptorSet(info);
    }

    @Override
    public RenderPipeline createPipeline(RenderPipelineCreateInfo info) throws RenderException {
        return invokeWithGLContext(() -> pipelineCreateAspect.createPipelineImpl(info));
    }

    @Override
    public RenderPass createRenderPass(RenderPassCreateInfo info) throws RenderException {
        return invokeWithGLContext(() -> renderPassCreateAspect.createRenderPassImpl(info));
    }

    public static final GLES2RenderEngineFactory FACTORY = new GLES2RenderEngineFactory();

    @FunctionalInterface
    interface GLWorker<T> {
        T apply() throws RenderException;
    }

    /// 在渲染线程上运行指定任务，并阻塞调用直到运行完成。
    <T> T invokeWithGLContext(GLWorker<T> worker) throws RenderException {
        Pair<
                Oneshot.Sender<Either<T, RenderException>>,
                Oneshot.Receiver<Either<T, RenderException>>
                > channel = Oneshot.create();
        Oneshot.Sender<Either<T, RenderException>> tx = channel.first();
        Oneshot.Receiver<Either<T, RenderException>> rx = channel.second();
        DeferredTask<T> task = new DeferredTask<>(worker, tx);

        taskQueue.getAndUpdate(list -> {
            list.add(task);
            return list;
        });

        return switch (rx.recv()) {
            case Either.Left<T, RenderException> left -> left.value;
            case Either.Right<T, RenderException> right -> throw right.value;
        };
    }

    final GLES2 gles2;
    final GLFW glfw;
    final GLFWwindow window;
    final boolean hasEXTDrawBuffers;
    final boolean hasEXTTextureStorage;

    int framebufferWidth;
    int framebufferHeight;
    final GLES2TextureAttachment defaultColorAttachment;
    final GLES2TextureAttachment defaultDepthAttachment;
    final GLES2Texture defaultColorAttachmentTexture;

    final List<GLES2RenderObject> objects = new ArrayList<>();
    final List<GLES2TextureAttachment> attachments = new ArrayList<>();
    final List<GLES2Texture> textures = new ArrayList<>();
    final List<GLES2Texture> dynamicallySizedTextures = new ArrayList<>();
    final List<GLES2RenderPipeline> pipelines = new ArrayList<>();
    final ConcurrentSkipListSet<GLES2RenderPass> renderPasses = new ConcurrentSkipListSet<>();

    private final ASPECT_ObjectCreate objectCreateAspect;
    private final ASPECT_AttachmentCreate attachmentCreateAspect;
    private final ASPECT_PipelineCreate pipelineCreateAspect;
    private final ASPECT_RenderPassCreate renderPassCreateAspect;

    private final ASPECT_RenderFrame renderFrameAspect;

    private static final class DeferredTask<T> {
        public final GLWorker<T> action;
        public final Oneshot.Sender<Either<T, RenderException>> sender;

        DeferredTask(GLWorker<T> action, Oneshot.Sender<Either<T, RenderException>> sender) {
            this.action = action;
            this.sender = sender;
        }

        public void runTask() {
            try {
                sender.send(Either.left(action.apply()));
            } catch (RenderException e) {
                sender.send(Either.right(e));
            }
        }
    }

    private final AtomicReference<List<DeferredTask<?>>> taskQueue = new AtomicReference<>(new ArrayList<>());
    private static final Logger logger = Logger.getLogger(GLES2RenderEngine.class.getName());
}
