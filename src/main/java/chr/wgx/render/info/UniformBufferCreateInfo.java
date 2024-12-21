package chr.wgx.render.info;

import chr.wgx.render.common.UniformUpdateFrequency;

@SuppressWarnings("ClassCanBeRecord")
public final class UniformBufferCreateInfo {
    public final UniformUpdateFrequency updateFrequency;
    public final UniformBufferBindingInfo bindingInfo;

    public UniformBufferCreateInfo(
            UniformUpdateFrequency updateFrequency,
            UniformBufferBindingInfo bindingInfo
    ) {
        this.updateFrequency = updateFrequency;
        this.bindingInfo = bindingInfo;
    }
}
