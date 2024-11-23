package chr.wgx.render.vk;

import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action2;

public final class VulkanRenderEngine extends AbstractRenderEngine {
    public VulkanRenderEngine(
            Action0 onInit,
            Action2<Integer, Integer> onResize,
            Action0 onBeforeRenderFrame,
            Action0 onAfterRenderFrame,
            Action0 onClose
    ) {
        super(onInit, onResize, onBeforeRenderFrame, onAfterRenderFrame, onClose);
    }

    private Option<VulkanRenderEngineContext> engineStateOption = Option.none();

    @Override
    protected void init(GLFW glfw, GLFWwindow window) throws RenderException {
        engineStateOption = Option.some(VulkanRenderEngineContext.init(glfw, window));
    }

    @Override
    protected void resize(int width, int height) throws RenderException {
        if (!(engineStateOption instanceof Option.Some<VulkanRenderEngineContext> engineState)) {
            return;
        }
    }

    @Override
    protected void renderFrame() throws RenderException {
        if (!(engineStateOption instanceof Option.Some<VulkanRenderEngineContext> engineState)) {
            return;
        }
    }

    @Override
    protected void close() {
        if (!(engineStateOption instanceof Option.Some<VulkanRenderEngineContext> engineState)) {
            return;
        }
    }

    @Override
    public ObjectHandle createObject(ObjectCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public AttachmentHandle.Color createColorAttachment(AttachmentCreateInfo.Color info) throws RenderException {
        return null;
    }

    @Override
    public AttachmentHandle.Depth createDepthAttachment(AttachmentCreateInfo.Depth info) throws RenderException {
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

    @Override
    public RenderPipelineHandle createPipeline(RenderPipelineCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public RenderTaskHandle createTask(RenderTaskCreateInfo info) throws RenderException {
        return null;
    }
}
