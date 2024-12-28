package chr.wgx.render.gles2.data;

import chr.wgx.render.data.RenderPipeline;
import chr.wgx.render.info.RenderPipelineCreateInfo;

public final class GLES2RenderPipeline extends RenderPipeline {
    public final int shaderProgram;

    public GLES2RenderPipeline(RenderPipelineCreateInfo createInfo, int shaderProgram) {
        super(createInfo);
        this.shaderProgram = shaderProgram;
    }
}
