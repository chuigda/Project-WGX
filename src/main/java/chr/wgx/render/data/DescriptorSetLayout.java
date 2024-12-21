package chr.wgx.render.data;

import chr.wgx.render.info.DescriptorSetLayoutCreateInfo;

public abstract class DescriptorSetLayout {
    public final DescriptorSetLayoutCreateInfo info;

    protected DescriptorSetLayout(DescriptorSetLayoutCreateInfo info) {
        this.info = info;
    }
}
