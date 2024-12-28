package chr.wgx.render.gles2.data;

import chr.wgx.render.data.RenderPipeline;
import chr.wgx.render.info.RenderPipelineCreateInfo;

import java.util.List;

public final class GLES2RenderPipeline extends RenderPipeline {
    public final int shaderProgram;
    public final List<UniformLocation> uniformLocations;

    public GLES2RenderPipeline(RenderPipelineCreateInfo createInfo, int shaderProgram, List<UniformLocation> uniformLocations) {
        super(createInfo);
        this.shaderProgram = shaderProgram;
        this.uniformLocations = uniformLocations;
    }
}
