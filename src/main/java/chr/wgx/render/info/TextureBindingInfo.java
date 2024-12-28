package chr.wgx.render.info;

import chr.wgx.render.common.ShaderStage;

public final class TextureBindingInfo extends DescriptorLayoutBindingInfo {
    public TextureBindingInfo(String bindingName, ShaderStage stage) {
        super(DescriptorType.COMBINED_IMAGE_SAMPLER, bindingName, stage);
    }
}
