package chr.wgx.render.data;

import chr.wgx.render.info.DescriptorSetCreateInfo;

public abstract class DescriptorSet {
    public final DescriptorSetCreateInfo createInfo;

    protected DescriptorSet(DescriptorSetCreateInfo createInfo) {
        this.createInfo = createInfo;
    }
}
