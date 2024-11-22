package chr.wgx.render.vk;

import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import tech.icey.xjbutil.container.Pair;

public final class VulkanRenderEngine extends AbstractRenderEngine {
    @Override
    public void init() throws RenderException {
    }

    @Override
    public void resize(int width, int height) throws RenderException {
    }

    @Override
    public void renderFrame() throws RenderException {
    }

    @Override
    public void close() {
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
