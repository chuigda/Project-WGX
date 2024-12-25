package chr.wgx.render.info;

import chr.wgx.render.common.ShaderStage;

public abstract sealed class DescriptorLayoutBindingInfo permits
        UniformBufferBindingInfo,
        TextureBindingInfo
{
    public final DescriptorType descriptorType;
    public final ShaderStage stage;

    protected DescriptorLayoutBindingInfo(DescriptorType descriptorType, ShaderStage stage) {
        this.descriptorType = descriptorType;
        this.stage = stage;
    }
}
