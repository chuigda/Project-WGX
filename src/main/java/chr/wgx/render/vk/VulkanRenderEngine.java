package chr.wgx.render.vk;

import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import tech.icey.xjbutil.container.Pair;
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

    @Override
    protected void init() throws RenderException {
    }

    @Override
    protected void resize(int width, int height) throws RenderException {
    }

    @Override
    protected void renderFrame() throws RenderException {
    }

    @Override
    protected void close() {
    }

    @Override
    public ObjectHandle createObject(ObjectCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public Pair<RenderTargetHandle, TextureHandle> createRenderTarget(RenderTargetCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public TextureHandle createTexture(TextureCreateInfo info) throws RenderException {
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
