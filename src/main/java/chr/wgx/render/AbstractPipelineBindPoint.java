package chr.wgx.render;

import chr.wgx.render.data.RenderPipeline;

public abstract class AbstractPipelineBindPoint {
    public final RenderPipeline pipeline;

    protected AbstractPipelineBindPoint(RenderPipeline pipeline) {
        this.pipeline = pipeline;
    }
}
