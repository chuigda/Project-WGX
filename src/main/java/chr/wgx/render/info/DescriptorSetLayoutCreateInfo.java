package chr.wgx.render.info;

import java.util.List;

public final class DescriptorSetLayoutCreateInfo {
    public final List<DescriptorLayoutBindingInfo> bindings;

    public DescriptorSetLayoutCreateInfo(List<DescriptorLayoutBindingInfo> bindings) {
        this.bindings = bindings;
    }
}
