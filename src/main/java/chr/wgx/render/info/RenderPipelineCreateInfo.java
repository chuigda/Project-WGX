package chr.wgx.render.info;

import chr.wgx.render.common.Color;
import tech.icey.xjbutil.container.Option;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public final class RenderPipelineCreateInfo {
    public final VertexInputInfo vertexInputInfo;
    public final List<DescriptorInfo> descriptorSetLayout;
    public final List<DescriptorInfo.UBO> pushConstantLayout;
    public final Option<ShaderProgram.Vulkan> vulkanShaderProgram;
    public final Option<ShaderProgram.GLES2> gles2ShaderProgram;
    public final int colorAttachmentCount;
    public final boolean depthTest;

    public RenderPipelineCreateInfo(
            VertexInputInfo vertexInputInfo,
            List<DescriptorInfo> descriptorSetLayout,
            List<DescriptorInfo.UBO> pushConstantLayout,
            Option<ShaderProgram.Vulkan> vulkanShaderProgram,
            Option<ShaderProgram.GLES2> gles2ShaderProgram,
            int colorAttachmentCount,
            boolean depthTest
    ) {
        this.vertexInputInfo = vertexInputInfo;
        this.descriptorSetLayout = descriptorSetLayout;
        this.pushConstantLayout = pushConstantLayout;
        this.vulkanShaderProgram = vulkanShaderProgram;
        this.gles2ShaderProgram = gles2ShaderProgram;
        this.colorAttachmentCount = colorAttachmentCount;
        this.depthTest = depthTest;
    }
}
