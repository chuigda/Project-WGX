package chr.wgx.render.info;

import tech.icey.xjbutil.container.Option;

import java.util.List;

public record RenderPipelineCreateInfo(
        List<DescriptorInfo> descriptorSetLayout,
        List<DescriptorInfo.UBO> pushConstantLayout,
        Option<ShaderProgram.Vulkan> vulkanShaderProgram,
        Option<ShaderProgram.GLES2> gles2ShaderProgram
) {
}
