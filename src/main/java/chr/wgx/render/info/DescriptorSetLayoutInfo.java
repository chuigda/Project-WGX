package chr.wgx.render.info;

import java.util.List;

public final class DescriptorSetLayoutInfo {
    public final List<DescriptorLayoutBindingInfo> bindings;

    public DescriptorSetLayoutInfo(List<DescriptorLayoutBindingInfo> bindings) {
        this.bindings = bindings;
    }
}
