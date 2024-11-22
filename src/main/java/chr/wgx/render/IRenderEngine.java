package chr.wgx.render;

import chr.wgx.render.handle.RenderTargetHandle;
import chr.wgx.render.handle.TextureHandle;
import chr.wgx.render.info.RenderTargetCreateInfo;
import chr.wgx.render.info.TextureCreateInfo;
import tech.icey.xjbutil.container.Pair;

public interface IRenderEngine {
    void init() throws RenderException;
    void resize(int width, int height) throws RenderException;
    void renderFrame() throws RenderException;
    void close();

    Pair<RenderTargetHandle, TextureHandle> createRenderTarget(RenderTargetCreateInfo info) throws RenderException;
    TextureHandle createTexture(TextureCreateInfo info) throws RenderException;
}
