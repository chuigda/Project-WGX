package chr.wgx.render;

import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import tech.icey.xjbutil.container.Pair;

public interface IRenderEngine {
    void init() throws RenderException;
    void resize(int width, int height) throws RenderException;
    void renderFrame() throws RenderException;
    void close();

    ObjectHandle createObject(ObjectCreateInfo info) throws RenderException;
    Pair<RenderTargetHandle, TextureHandle> createRenderTarget(RenderTargetCreateInfo info) throws RenderException;
    TextureHandle createTexture(TextureCreateInfo info) throws RenderException;
    UniformHandle createUniform(UniformCreateInfo info) throws RenderException;
    RenderPipelineHandle createPipeline(RenderPipelineCreateInfo info) throws RenderException;
    RenderTaskHandle createTask(RenderTaskCreateInfo info) throws RenderException;
}
