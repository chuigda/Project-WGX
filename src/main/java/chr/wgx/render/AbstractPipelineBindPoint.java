package chr.wgx.render;

import chr.wgx.render.data.Pipeline;

public abstract class AbstractPipelineBindPoint {
    public final Pipeline pipeline;

    protected AbstractPipelineBindPoint(Pipeline pipeline) {
        this.pipeline = pipeline;
    }
}
