package chr.wgx.render;

import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import org.jetbrains.annotations.NotNull;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action2;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractRenderEngine {
    private final AtomicLong handleCounter = new AtomicLong(0);
    private final Action0 onInit;
    private final Action2<Integer, Integer> onResize;
    private final Action0 onBeforeRenderFrame;
    private final Action0 onAfterRenderFrame;
    private final Action0 onClose;

    public AbstractRenderEngine(
            Action0 onInit,
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

    public final long nextHandle() {
        return handleCounter.getAndIncrement();
    }

    public final void initEngine(GLFWwindow window) throws RenderException {
        init(window);
        onInit.apply();
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

    protected abstract void init(GLFWwindow window) throws RenderException;
    protected abstract void resize(int width, int height) throws RenderException;
    protected abstract void renderFrame() throws RenderException;
    protected abstract void close();

    public abstract ObjectHandle createObject(ObjectCreateInfo info) throws RenderException;
    public abstract AttachmentHandle.Color createColorAttachment(AttachmentCreateInfo.Color i) throws RenderException;
    public abstract AttachmentHandle.Depth createDepthAttachment(AttachmentCreateInfo.Depth i) throws RenderException;

    public abstract UniformHandle.Sampler2D createTexture(TextureCreateInfo info) throws RenderException;
    public abstract UniformHandle createUniform(UniformCreateInfo info) throws RenderException;
    public abstract RenderPipelineHandle createPipeline(RenderPipelineCreateInfo info) throws RenderException;
    public abstract RenderTaskHandle createTask(RenderTaskCreateInfo info) throws RenderException;
}
