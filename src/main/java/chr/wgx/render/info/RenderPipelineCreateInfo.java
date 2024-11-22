package chr.wgx.render.info;

import chr.wgx.render.common.Color;
import tech.icey.xjbutil.container.Option;

import java.util.List;

public record RenderPipelineCreateInfo(
        VertexInputInfo vertexInputInfo,
        List<DescriptorInfo> descriptorSetLayout,
        List<DescriptorInfo.UBO> pushConstantLayout,
        Option<ShaderProgram.Vulkan> vulkanShaderProgram,
        Option<ShaderProgram.GLES2> gles2ShaderProgram,
        int colorAttachmentCount,
        boolean multisampling,
        boolean depthTest,
        List<Color> clearColors
) {}
