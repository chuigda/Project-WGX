package chr.wgx.render.data;

import chr.wgx.render.info.DescriptorSetLayoutCreateInfo;

public abstract class DescriptorSetLayout {
    public final DescriptorSetLayoutCreateInfo createInfo;

    protected DescriptorSetLayout(DescriptorSetLayoutCreateInfo createInfo) {
        this.createInfo = createInfo;
    }
}
