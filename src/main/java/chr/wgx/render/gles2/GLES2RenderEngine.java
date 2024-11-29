package chr.wgx.render.gles2;

import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import tech.icey.gles2.GLES2;
import tech.icey.gles2.GLES2Constants;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.panama.buffer.PointerBuffer;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action1;
import tech.icey.xjbutil.functional.Action2;
import tech.icey.xjbutil.sync.Oneshot;

import java.lang.foreign.Arena;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public final class GLES2RenderEngine extends AbstractRenderEngine {
    // TODO: move this to elsewhere
    @FunctionalInterface
    public interface CheckedAction {
        void invoke(GLES2 gl, Arena arena) throws RenderException;
    }

    private final ConcurrentLinkedQueue<CheckedAction> taskQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentMap<Long, Object> handlerMapping = new ConcurrentHashMap<>();

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
        this.gles2Option = Option.some(new GLES2(name -> {
            try (var localArena = Arena.ofConfined()) {
                return glfw.glfwGetProcAddress(ByteBuffer.allocateString(localArena, name));
            }
        }));
    }

    @Override
    protected void resize(int width, int height) {
        if (!(gles2Option instanceof Option.Some<GLES2> someGLES2)) {
            return;
        }
        GLES2 gles2 = someGLES2.value;

        gles2.glViewport(0, 0, width, height);
    }

    @Override
    protected void renderFrame() throws RenderException {
        if (!(gles2Option instanceof Option.Some<GLES2> someGLES2)) {
            return;
        }
        GLES2 gles2 = someGLES2.value;

        gles2.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gles2.glClear(GLES2Constants.GL_COLOR_BUFFER_BIT | GLES2Constants.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    protected void close() {
        // there's actually nothing to do here
    }

    private void invokeLater(CheckedAction run) {
        taskQueue.add(run);
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
    public AttachmentHandle.Color createColorAttachment(AttachmentCreateInfo.Color i) throws RenderException {
        return null;
    }

    @Override
    public AttachmentHandle.Depth createDepthAttachment(AttachmentCreateInfo.Depth i) throws RenderException {
        return null;
    }

    @Override
    public Pair<AttachmentHandle.Color, AttachmentHandle.Depth> getDefaultAttachments() {
        return null;
    }

    @Override
    public UniformHandle.Sampler2D createTexture(TextureCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public UniformHandle createUniform(UniformCreateInfo info) throws RenderException {
        return null;
    }

    private static int loadShader(Arena arena, GLES2 gl, @enumtype(GLES2Constants.class) int shaderType, String shaderSource) {
        var shaderHandle = gl.glCreateShader(shaderType);
        gl.glShaderSource(shaderHandle, 1, new PointerBuffer(arena.allocateFrom(shaderSource)), null);
        gl.glCompileShader(shaderHandle);

        // TODO: check shader compilation status

        return shaderHandle;
    }

    private static void bindAttributes(GLES2 gl, Arena arena, int programHandle, VertexInputInfo info) {
        int index = 0;
        for (var attr : info.attributes) {
            var ty = attr.type;
            var name = attr.name;
            gl.glBindAttribLocation(programHandle, index, ByteBuffer.allocateString(arena, name));
            index = index + ty.glIndexSize;
        }
    }

    @Override
    public RenderPipelineHandle createPipeline(RenderPipelineCreateInfo info) throws RenderException {
        var handle = nextHandle();
        invokeLater((gles, arena) -> {
            if (! (info.gles2ShaderProgram instanceof Option.Some<ShaderProgram.GLES2> someProgramSource)) {
                // TODO: do something
                throw new RuntimeException();
            }

            var programSource = someProgramSource.value;
            var vertexShader = programSource.vertexShader;
            var fragmentShader = programSource.fragmentShader;

            var programHandle = gles.glCreateProgram();

            // attributes
            bindAttributes(gles, arena, programHandle, info.vertexInputInfo);

            // shaders
            gles.glAttachShader(programHandle, loadShader(arena, gles, GLES2Constants.GL_VERTEX_SHADER, vertexShader));
            gles.glAttachShader(programHandle, loadShader(arena, gles, GLES2Constants.GL_FRAGMENT_SHADER, fragmentShader));
            gles.glLinkProgram(programHandle);

            // TODO: check program compilation status
        });

        return new RenderPipelineHandle(handle);
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

    private Option<GLES2> gles2Option = Option.none();
}
