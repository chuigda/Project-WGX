package chr.wgx.render;

import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action1;
import tech.icey.xjbutil.functional.Action2;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractRenderEngine {
    private final AtomicLong handleCounter = new AtomicLong(1024); // 保留一些低位用于内置对象
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

    public final long nextHandle() {
        return handleCounter.getAndIncrement();
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

    public abstract ObjectHandle createObject(ObjectCreateInfo info) throws RenderException;
    public abstract List<ObjectHandle> createObject(List<ObjectCreateInfo> info) throws RenderException;
    public abstract Pair<ColorAttachmentHandle, SamplerHandle> createColorAttachment(AttachmentCreateInfo i) throws RenderException;
    public abstract DepthAttachmentHandle createDepthAttachment(AttachmentCreateInfo i) throws RenderException;
    public abstract Pair<ColorAttachmentHandle, DepthAttachmentHandle> getDefaultAttachments();

    public abstract SamplerHandle createTexture(BufferedImage image) throws RenderException;
    public abstract UniformHandle createUniform(UniformBufferCreateInfo info) throws RenderException;
    public abstract RenderPipelineHandle createPipeline(RenderPipelineCreateInfo info) throws RenderException;
    public abstract RenderTaskHandle createTask(RenderTaskInfo info) throws RenderException;
}
