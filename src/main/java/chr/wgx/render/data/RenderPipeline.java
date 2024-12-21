package chr.wgx.render.data;

import chr.wgx.render.info.RenderPipelineCreateInfo;

public abstract class RenderPipeline {
    public final RenderPipelineCreateInfo createInfo;

    public RenderPipeline(RenderPipelineCreateInfo createInfo) {
        this.createInfo = createInfo;
    }
}
