package chr.wgx.render;

import chr.wgx.render.data.*;
import chr.wgx.render.info.*;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action1;
import tech.icey.xjbutil.functional.Action2;

import java.awt.image.BufferedImage;
import java.util.List;

public abstract class AbstractRenderEngine {
    private final Action1<AbstractRenderEngine> onInit;
    private final Action2<Integer, Integer> onResize;
    private final Action0 onBeforeRenderFrame;
    private final Action0 onAfterRenderFrame;
    private final Action0 onClose;

    public AbstractRenderEngine(
            Action1<AbstractRenderEngine> onInit,
            Action2<Integer, Integer> onResize,
            Action0 onBeforeRenderFrame,
            Action0 onAfterRenderFrame,
            Action0 onClose
    ) {
        this.onInit = onInit;
        this.onResize = onResize;
        this.onBeforeRenderFrame = onBeforeRenderFrame;
        this.onAfterRenderFrame = onAfterRenderFrame;
        this.onClose = onClose;
    }

    public final void initEngine(GLFW glfw, GLFWwindow window) throws RenderException {
        init(glfw, window);
        onInit.apply(this);
    }

    public final void resizeEngine(int width, int height) throws RenderException {
        resize(width, height);
        onResize.apply(width, height);
    }

    public final void renderFrameEngine() throws RenderException {
        onBeforeRenderFrame.apply();
        renderFrame();
        onAfterRenderFrame.apply();
    }

    public final void closeEngine() {
        close();
        onClose.apply();
    }

    protected abstract void init(GLFW glfw, GLFWwindow window) throws RenderException;
    protected abstract void resize(int width, int height) throws RenderException;
    protected abstract void renderFrame() throws RenderException;
    protected abstract void close();

    public abstract RenderObject createObject(ObjectCreateInfo info) throws RenderException;
    public abstract List<RenderObject> createObject(List<ObjectCreateInfo> info) throws RenderException;
    public abstract Pair<Attachment, Texture> createColorAttachment(AttachmentCreateInfo i) throws RenderException;
    public abstract Attachment createDepthAttachment(AttachmentCreateInfo i) throws RenderException;
    public abstract Pair<Attachment, Attachment> getDefaultAttachments();

    public abstract Texture createTexture(BufferedImage image) throws RenderException;
    public abstract UniformBuffer createUniform(UniformBufferCreateInfo info) throws RenderException;
}
