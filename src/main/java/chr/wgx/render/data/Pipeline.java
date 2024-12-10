package chr.wgx.render.data;

import chr.wgx.render.info.RenderPipelineCreateInfo;

public abstract class Pipeline {
    public final RenderPipelineCreateInfo createInfo;

    public Pipeline(RenderPipelineCreateInfo createInfo) {
        this.createInfo = createInfo;
    }
}
