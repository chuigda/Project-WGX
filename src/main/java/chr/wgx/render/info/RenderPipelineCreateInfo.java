package chr.wgx.render.info;

import tech.icey.xjbutil.container.Option;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public final class RenderPipelineCreateInfo {
    public final VertexInputInfo vertexInputInfo;
    public final List<DescriptorSetLayoutInfo> descriptorSetLayouts;
    public final List<UniformBufferBindingInfo> pushConstantLayouts;
    public final Option<ShaderProgram.Vulkan> vulkanShaderProgram;
    public final Option<ShaderProgram.GLES2> gles2ShaderProgram;
    public final int colorAttachmentCount;
    public final boolean depthTest;

    public RenderPipelineCreateInfo(
            VertexInputInfo vertexInputInfo,
            List<DescriptorSetLayoutInfo> descriptorSetLayouts,
            List<UniformBufferBindingInfo> pushConstantLayouts,
            Option<ShaderProgram.Vulkan> vulkanShaderProgram,
            Option<ShaderProgram.GLES2> gles2ShaderProgram,
            int colorAttachmentCount,
            boolean depthTest
    ) {
        this.vertexInputInfo = vertexInputInfo;
        this.descriptorSetLayouts = descriptorSetLayouts;
        this.pushConstantLayouts = pushConstantLayouts;
        this.vulkanShaderProgram = vulkanShaderProgram;
        this.gles2ShaderProgram = gles2ShaderProgram;
        this.colorAttachmentCount = colorAttachmentCount;
        this.depthTest = depthTest;
    }
}
