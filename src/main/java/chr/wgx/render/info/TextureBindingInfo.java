package chr.wgx.render.info;

import chr.wgx.render.common.ShaderStage;

public final class TextureBindingInfo extends DescriptorLayoutBindingInfo {
    public TextureBindingInfo(ShaderStage stage) {
        super(DescriptorType.COMBINED_IMAGE_SAMPLER, 1, stage);
    }
}
