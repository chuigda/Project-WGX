package chr.wgx.render.info;

import chr.wgx.render.data.DescriptorSetLayout;
import tech.icey.xjbutil.container.Option;

import java.util.List;

public final class RenderPipelineCreateInfo {
    public final VertexInputInfo vertexInputInfo;
    public final List<DescriptorSetLayout> descriptorSetLayouts;
    public final Option<PushConstantInfo> pushConstantInfo;
    public final Option<ShaderProgram.Vulkan> vulkanShaderProgram;
    public final Option<ShaderProgram.GLES2> gles2ShaderProgram;
    public final int colorAttachmentCount;
    public final boolean depthTest;

    public RenderPipelineCreateInfo(
            VertexInputInfo vertexInputInfo,
            List<DescriptorSetLayout> descriptorSetLayouts,
            Option<PushConstantInfo> pushConstantInfo,
            Option<ShaderProgram.Vulkan> vulkanShaderProgram,
            Option<ShaderProgram.GLES2> gles2ShaderProgram,
            int colorAttachmentCount,
            boolean depthTest
    ) {
        this.vertexInputInfo = vertexInputInfo;
        this.descriptorSetLayouts = descriptorSetLayouts;
        this.pushConstantInfo = pushConstantInfo;
        this.vulkanShaderProgram = vulkanShaderProgram;
        this.gles2ShaderProgram = gles2ShaderProgram;
        this.colorAttachmentCount = colorAttachmentCount;
        this.depthTest = depthTest;
    }
}
