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
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action1;
import tech.icey.xjbutil.functional.Action2;
import tech.icey.xjbutil.sync.Oneshot;

import java.awt.image.BufferedImage;
import java.lang.foreign.Arena;
import java.util.List;

public final class GLES2RenderEngine extends AbstractRenderEngine {
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

    @Override
    public ObjectHandle createObject(ObjectCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public List<ObjectHandle> createObject(List<ObjectCreateInfo> infos) throws RenderException {
        return null;
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
        return null;
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
