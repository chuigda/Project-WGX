package chr.wgx.render.gles2;

import chr.wgx.config.Config;
import chr.wgx.render.RenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.common.Color;
import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.data.*;
import chr.wgx.render.gles2.data.*;
import chr.wgx.render.gles2.task.GLES2RenderPass;
import chr.wgx.render.info.*;
import chr.wgx.render.task.RenderPass;
import org.jetbrains.annotations.Nullable;
import tech.icey.gles2.GLES2;
import tech.icey.gles2.GLES2Constants;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.RawFunctionLoader;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.xjbutil.container.Either;
import tech.icey.xjbutil.container.Option;
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

        glfw.glfwMakeContextCurrent(window);
        RawFunctionLoader loadWithGLFW = name -> {
            try (Arena arena = Arena.ofConfined()) {
                return glfw.glfwGetProcAddress(ByteBuffer.allocateString(arena, name));
            }
        };

        this.gles2 = new GLES2(loadWithGLFW);

        @Nullable GLES2EXTDrawBuffers extDrawBuffers = null;
        @Nullable ByteBuffer extensions = gles2.glGetString(GLES2Constants.GL_EXTENSIONS);
        if (extensions != null) {
            String extensionsString = extensions.readString();
            logger.info("支持的 OpenGL ES2 扩展: " + extensionsString);

            if (Config.config().gles2Config.debug && extensionsString.contains("GL_KHR_debug")) {
                GLES2KHRDebug debugFunctions;
                try {
                    debugFunctions = new GLES2KHRDebug(loadWithGLFW);
                    debugFunctions.glDebugMessageControl(
                            GLES2Constants.GL_DONT_CARE,
                            GLES2Constants.GL_DONT_CARE,
                            GLES2Constants.GL_DONT_CARE,
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

            if (extensionsString.contains("GL_EXT_draw_buffers")) {
                try {
                    extDrawBuffers = new GLES2EXTDrawBuffers(loadWithGLFW);
                    logger.info("已启用 OpenGL ES2 多渲染目标扩展");
                } catch (Throwable e) {
                    logger.warning("找到了 OpenGL ES2 多渲染目标扩展, 但是无法初始化多渲染目标函数: " + e.getMessage());
                }
            }
        }

        if (extDrawBuffers != null) {
            this.extDrawBuffers = Option.some(extDrawBuffers);
        } else {
            this.extDrawBuffers = Option.none();
            logger.warning("OpenGL ES2 多渲染目标扩展不可用");
        }

        try (Arena arena = Arena.ofConfined()) {
            IntBuffer pWidthHeight = IntBuffer.allocate(arena, 2);
            glfw.glfwGetFramebufferSize(window, pWidthHeight, pWidthHeight.offset(1));

            framebufferWidth = pWidthHeight.read();
            framebufferHeight = pWidthHeight.read(1);
        }

        objectCreateAspect = new ASPECT_ObjectCreate(this);
        attachmentCreateAspect = new ASPECT_AttachmentCreate(this);
        pipelineCreateAspect = new ASPECT_PipelineCreate(this);

        defaultColorAttachment = (GLES2TextureAttachment) attachmentCreateAspect.createColorAttachmentImpl(
                new AttachmentCreateInfo(
                        PixelFormat.RGBA8888_FLOAT,
                        -1,
                        -1
                )
        ).first();
        defaultDepthAttachment = (GLES2TextureAttachment) attachmentCreateAspect.createDepthAttachmentImpl(
                new AttachmentCreateInfo(
                        PixelFormat.DEPTH_BUFFER_OPTIMAL,
                        -1,
                        -1
                )
        );
    }

    public final Arena prefabArena = Arena.ofAuto();

    @Override
    protected void resize(int width, int height) {
        glfw.glfwMakeContextCurrent(window);
        this.framebufferWidth = width;
        this.framebufferHeight = height;

        if (width == 0 || height == 0) {
            return;
        }

        for (GLES2Texture texture : dynamicallySizedTextures) {
            gles2.glBindTexture(GLES2Constants.GL_TEXTURE_2D, texture.textureObject);
            gles2.glTexImage2D(
                    GLES2Constants.GL_TEXTURE_2D,
                    0,
                    GLES2Constants.GL_RGBA,
                    width,
                    height,
                    0,
                    GLES2Constants.GL_RGBA,
                    GLES2Constants.GL_UNSIGNED_BYTE,
                    MemorySegment.NULL
            );
        }
    }

    @Override
    protected void renderFrame() throws RenderException {
        if (framebufferWidth == 0 || framebufferHeight == 0) {
            return;
        }

        glfw.glfwMakeContextCurrent(window);
        for (DeferredTask<?> task : taskQueue.getAndSet(new ArrayList<>())) {
            task.runTask();
        }

        glfw.glfwSwapBuffers(window);
    }

    @Override
    protected void close() {
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
    public RenderPass createRenderPass(
            String renderPassName,
            int priority,
            List<Attachment> colorAttachments,
            List<Color> clearColors,
            Option<Attachment> depthAttachment
    ) {
        GLES2RenderPass ret = new GLES2RenderPass(
                renderPassName,
                priority,
                colorAttachments,
                clearColors,
                depthAttachment
        );
        renderPasses.add(ret);
        return ret;
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
    final Option<GLES2EXTDrawBuffers> extDrawBuffers;

    int framebufferWidth;
    int framebufferHeight;
    final GLES2TextureAttachment defaultColorAttachment;
    final GLES2TextureAttachment defaultDepthAttachment;

    final List<GLES2RenderObject> objects = new ArrayList<>();
    final List<GLES2TextureAttachment> attachments = new ArrayList<>();
    final List<GLES2Texture> textures = new ArrayList<>();
    final List<GLES2Texture> dynamicallySizedTextures = new ArrayList<>();
    final List<GLES2RenderPipeline> pipelines = new ArrayList<>();
    final ConcurrentSkipListSet<GLES2RenderPass> renderPasses = new ConcurrentSkipListSet<>();

    private final ASPECT_ObjectCreate objectCreateAspect;
    private final ASPECT_AttachmentCreate attachmentCreateAspect;
    private final ASPECT_PipelineCreate pipelineCreateAspect;

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
