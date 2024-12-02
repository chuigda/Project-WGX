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
import java.util.*;

/**
 * See also <a href="https://docs.gl/es2">this page</a>
 */
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

        public void runTask(GLES2 gles2) {
            try {
                //noinspection unchecked
                sender.send(Either.left(action.apply(gles2)));
            } catch (RenderException e) {
                //noinspection unchecked
                sender.send(Either.right(e));
            }
        }
    }

    private final Ref<List<DeferredTask<?>>> taskQueue = new Ref<>(new ArrayList<>());

    // OpenGL ES2 的所有资源更新实际上只在渲染线程上进行，因此不需要加锁或者使用并行数据结构
    private final HashMap<Long, Resource.Object> objects = new HashMap<>();
    private final HashMap<Long, Resource.Pipeline> pipelines = new HashMap<>();
    private final HashMap<Long, Resource.Task> tasks = new HashMap<>();

    public GLES2RenderEngine(
            Action1<AbstractRenderEngine> onInit,
            Action2<Integer, Integer> onResize,
            Action0 onBeforeRenderFrame,
            Action0 onAfterRenderFrame,
            Action0 onClose
    ) {
        super(onInit, onResize, onBeforeRenderFrame, onAfterRenderFrame, onClose);
    }

    // region Object Getter

    // All Object Getters are supposed to be run in the render thread

    private Resource.Pipeline getObject(RenderPipelineHandle handle) {
        return Objects.requireNonNull(pipelines.get(handle.getId()));
    }

    private Resource.Object getObject(ObjectHandle handle) {
        return Objects.requireNonNull(objects.get(handle.getId()));
    }

    private Resource.Task getObject(RenderTaskHandle handle) {
        return Objects.requireNonNull(tasks.get(handle.getId()));
    }

    // endregion Object Getter

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

    private void doRenderObject(GLES2 gl, Arena arena, int programHandle, List<ObjectHandle> objects) {
        if (objects.isEmpty()) return;
        VertexInputInfo vertexInfo = null;
        for (var objHandle : objects) {
            var obj = getObject(objHandle);
            if (vertexInfo == null) {
                vertexInfo = obj.attributeInfo;
                // initialize vertex attributes
                // TODO: do not initialize if init the same VertexInputInfo before, save some performance
                GLES2Utils.initializeAttributes(gl, arena, programHandle, vertexInfo);
            } else {
                // TODO check same vertex info
            }

            gl.glBindBuffer(GLES2Constants.GL_ARRAY_BUFFER, obj.glHandle);
            gl.glDrawArrays(GLES2Constants.GL_TRIANGLES, 0, (int) obj.vertexCount);
        }
    }

    private void doRunTask(GLES2 gl, Resource.Task task) {
        try (var arena = Arena.ofConfined()) {
            var pipeline = getObject(task.taskInfo.pipelineHandle);
            var programHandle = pipeline.programHandle;

            gl.glUseProgram(programHandle);
            doRenderObject(gl, arena, programHandle, task.taskInfo.objectHandles);
        }
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

        pendingTasks.forEach(x -> x.runTask(gles2));

        gles2.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gles2.glClear(GLES2Constants.GL_COLOR_BUFFER_BIT | GLES2Constants.GL_DEPTH_BUFFER_BIT);
        tasks.values().forEach(x -> doRunTask(gles2, x));
    }

    @Override
    protected void close() {
        // there's actually nothing to do here
    }

    /// 将程序在渲染线程上运行，并阻塞调用直到运行完成。
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
        return createObject(List.of(info)).getFirst();
    }

    @Override
    public List<ObjectHandle> createObject(List<ObjectCreateInfo> infos) throws RenderException {
        if (infos.isEmpty()) return List.of();
        return invokeLater(gl -> {
            var handles = new ArrayList<ObjectHandle>(infos.size());
            try (var arena = Arena.ofConfined()) {
                for (var info : infos) {
                    var bufferHandle = GLES2Utils.initBuffer(gl, arena, GLES2Constants.GL_ARRAY_BUFFER, info.pData);

                    var dataSize = info.vertexInputInfo.stride;
                    var vertexCount = info.pData.byteSize() / dataSize;
                    var object = new Resource.Object(bufferHandle, info.vertexInputInfo, vertexCount);

                    var handle = nextHandle();
                    objects.put(handle, object);
                    handles.add(new ObjectHandle(handle));
                }
            }

            return handles;
        });
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
                if (!(info.gles2ShaderProgram instanceof Option.Some<ShaderProgram.GLES2> someProgramSource)) {
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
                        gles, arena,
                        GLES2Constants.GL_LINK_STATUS,
                        programHandle,
                        msg -> {
                            gles.glDeleteProgram(programHandle);
                            return new RenderException("无法链接程序: " + msg);
                        }
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
        return invokeLater(gl -> {
            var handle = nextHandle();
            tasks.put(handle, new Resource.Task(info));
            return new RenderTaskHandle(handle);
        });
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
