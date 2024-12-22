package chr.wgx.render.info;

import chr.wgx.render.common.UniformUpdateFrequency;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.MemorySegment;

public final class UniformBufferCreateInfo {
    public final UniformUpdateFrequency updateFrequency;
    public final UniformBufferBindingInfo bindingInfo;
    public final Option<MemorySegment> init;

    public UniformBufferCreateInfo(
            UniformUpdateFrequency updateFrequency,
            UniformBufferBindingInfo bindingInfo
    ) {
        this.updateFrequency = updateFrequency;
        this.bindingInfo = bindingInfo;
        this.init = Option.none();
    }

    public UniformBufferCreateInfo(
            UniformUpdateFrequency updateFrequency,
            UniformBufferBindingInfo bindingInfo,
            MemorySegment init
    ) {
        this.updateFrequency = updateFrequency;
        this.bindingInfo = bindingInfo;
        this.init = Option.some(init);
    }
}
