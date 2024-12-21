package chr.wgx.render.info;

import chr.wgx.render.data.Descriptor;
import chr.wgx.render.data.DescriptorSetLayout;

import java.util.List;

public final class DescriptorSetCreateInfo {
    public final DescriptorSetLayout layout;
    public final List<Descriptor> descriptors;

    public DescriptorSetCreateInfo(DescriptorSetLayout layout, List<Descriptor> descriptors) {
        this.layout = layout;
        this.descriptors = descriptors;
    }
}
