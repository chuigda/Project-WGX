package chr.wgx.render;

import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import tech.icey.xjbutil.container.Pair;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractRenderEngine {
    private final AtomicLong handleCounter = new AtomicLong(0);

    public final long nextHandle() {
        return handleCounter.getAndIncrement();
    }

    public abstract void init() throws RenderException;
    public abstract void resize(int width, int height) throws RenderException;
    public abstract void renderFrame() throws RenderException;
    public abstract void close();

    public abstract ObjectHandle createObject(ObjectCreateInfo info) throws RenderException;
    public abstract Pair<RenderTargetHandle, TextureHandle>
    createRenderTarget(RenderTargetCreateInfo info) throws RenderException;
    public abstract TextureHandle createTexture(TextureCreateInfo info) throws RenderException;
    public abstract UniformHandle createUniform(UniformCreateInfo info) throws RenderException;
    public abstract RenderPipelineHandle createPipeline(RenderPipelineCreateInfo info) throws RenderException;
    public abstract RenderTaskHandle createTask(RenderTaskCreateInfo info) throws RenderException;
}
