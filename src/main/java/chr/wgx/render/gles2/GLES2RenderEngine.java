package chr.wgx.render.gles2;

import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import tech.icey.gles2.GLES2;
import tech.icey.gles2.GLES2Constants;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.xjbutil.container.Either;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.container.Ref;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action1;
import tech.icey.xjbutil.functional.Action2;
import tech.icey.xjbutil.sync.Oneshot;

import java.awt.image.BufferedImage;
import java.lang.foreign.Arena;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class GLES2RenderEngine extends AbstractRenderEngine {
    @FunctionalInterface
    public interface CheckedFunction<T> {
        T apply(GLES2 gles2) throws RenderException;
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class DeferredTask<T> {
        public final CheckedFunction<T> action;
        @SuppressWarnings("rawtypes")
        public final Oneshot.Sender sender;

        DeferredTask(CheckedFunction<T> action, Oneshot.Sender<Either<T, RenderException>> sender) {
            this.action = action;
            this.sender = sender;
        }
    }

    private final Ref<List<DeferredTask<?>>> taskQueue = new Ref<>(new ArrayList<>());

    // OpenGL ES2 的所有资源更新实际上只在渲染线程上进行，因此不需要加锁或者使用并行数据结构
    private final HashMap<Long, Resource.Object> objects = new HashMap<>();
    private final HashMap<Long, Resource.Pipeline> pipelines = new HashMap<>();

    public GLES2RenderEngine(
            Action1<AbstractRenderEngine> onInit,
            Action2<Integer, Integer> onResize,
            Action0 onBeforeRenderFrame,
            Action0 onAfterRenderFrame,
            Action0 onClose
    ) {
        super(onInit, onResize, onBeforeRenderFrame, onAfterRenderFrame, onClose);
    }

    @Override
    protected void init(GLFW glfw, GLFWwindow window) {
        glfw.glfwMakeContextCurrent(window);
        this.gles2 = new GLES2(name -> {
            try (var localArena = Arena.ofConfined()) {
                return glfw.glfwGetProcAddress(ByteBuffer.allocateString(localArena, name));
            }
        });
    }

    @Override
    protected void resize(int width, int height) {
        gles2.glViewport(0, 0, width, height);
    }

    @Override
    protected void renderFrame() throws RenderException {
        List<DeferredTask<?>> pendingTasks = List.of();
        synchronized (taskQueue) {
            if (!taskQueue.value.isEmpty()) {
                pendingTasks = taskQueue.value;
                taskQueue.value = new ArrayList<>();
            }
        }

        for (DeferredTask<?> task : pendingTasks) {
            try {
                //noinspection unchecked
                task.sender.send(Either.left(task.action.apply(gles2)));
            } catch (RenderException e) {
                //noinspection unchecked
                task.sender.send(Either.right(e));
            }
        }

        gles2.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gles2.glClear(GLES2Constants.GL_COLOR_BUFFER_BIT | GLES2Constants.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    protected void close() {
        // there's actually nothing to do here
    }

    private <T> T invokeLater(CheckedFunction<T> run) throws RenderException {
        Pair<Oneshot.Sender<Either<T, RenderException>>, Oneshot.Receiver<Either<T, RenderException>>> channel = Oneshot.create();
        Oneshot.Sender<Either<T, RenderException>> tx = channel.first();
        Oneshot.Receiver<Either<T, RenderException>> rx = channel.second();
        synchronized (taskQueue) {
            taskQueue.value.add(new DeferredTask<>(run, tx));
        }

        return switch (rx.recv()) {
            case Either.Left<T, RenderException> l -> l.value;
            case Either.Right<T, RenderException> r -> throw r.value;
        };
    }

    @Override
    public ObjectHandle createObject(ObjectCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public List<ObjectHandle> createObject(List<ObjectCreateInfo> infos) throws RenderException {
        var results = new ArrayList<ObjectHandle>();
        for (var info : infos) {
            results.add(createObject(info));        // null implies exception, no need to check
        }

        return results;
    }

    @Override
    public Pair<ColorAttachmentHandle, SamplerHandle> createColorAttachment(AttachmentCreateInfo i) throws RenderException {
        return null;
    }

    @Override
    public DepthAttachmentHandle createDepthAttachment(AttachmentCreateInfo i) throws RenderException {
        return null;
    }

    @Override
    public Pair<ColorAttachmentHandle, DepthAttachmentHandle> getDefaultAttachments() {
        return null;
    }

    @Override
    public SamplerHandle createTexture(BufferedImage image) throws RenderException {
        return null;
    }

    @Override
    public UniformHandle createUniform(UniformCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public RenderPipelineHandle createPipeline(RenderPipelineCreateInfo info) throws RenderException {
        return invokeLater(gles -> {
            boolean hasCompiledVertexShader = false;
            boolean hasCompiledFragmentShader = false;
            int vertexShaderHandle = 0;
            int fragmentShaderHandle = 0;

            try (Arena arena = Arena.ofConfined()) {
                if (! (info.gles2ShaderProgram instanceof Option.Some<ShaderProgram.GLES2> someProgramSource)) {
                    throw new RenderException("无法创建渲染管线: 缺少 GLES2 着色器程序");
                }

                ShaderProgram.GLES2 programSource = someProgramSource.value;
                String vertexShader = programSource.vertexShader;
                String fragmentShader = programSource.fragmentShader;

                int programHandle = gles.glCreateProgram();

                vertexShaderHandle = GLES2Utils.loadShader(gles, arena, GLES2Constants.GL_VERTEX_SHADER, vertexShader);
                hasCompiledVertexShader = true;

                fragmentShaderHandle = GLES2Utils.loadShader(gles, arena, GLES2Constants.GL_FRAGMENT_SHADER, fragmentShader);
                hasCompiledFragmentShader = true;

                gles.glAttachShader(programHandle, vertexShaderHandle);
                gles.glAttachShader(programHandle, fragmentShaderHandle);
                gles.glLinkProgram(programHandle);

                GLES2Utils.checkStatus(
                        GLES2Utils.InformationKind.Program,
                        gles,
                        arena,
                        GLES2Constants.GL_LINK_STATUS,
                        programHandle,
                        msg -> new RenderException("无法链接程序: " + msg)
                );

                long handle = nextHandle();
                pipelines.put(handle, new Resource.Pipeline(info, programHandle));
                return new RenderPipelineHandle(handle);
            } finally {
                if (hasCompiledVertexShader) {
                    gles.glDeleteShader(vertexShaderHandle);
                }
                if (hasCompiledFragmentShader) {
                    gles.glDeleteShader(fragmentShaderHandle);
                }
            }
        });
    }

    @Override
    public RenderTaskHandle createTask(RenderTaskInfo info) throws RenderException {
        return null;
    }

    private static final class UnUploadedObject {
        public final ObjectCreateInfo info;
        public final Oneshot.Sender<Option<Integer>> sender;

        public UnUploadedObject(ObjectCreateInfo info, Oneshot.Sender<Option<Integer>> sender) {
            this.info = info;
            this.sender = sender;
        }
    }

    private GLES2 gles2;
}
