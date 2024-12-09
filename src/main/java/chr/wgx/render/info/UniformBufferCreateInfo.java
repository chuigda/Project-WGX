package chr.wgx.render.info;

import chr.wgx.render.common.UniformUpdateFrequency;

@SuppressWarnings("ClassCanBeRecord")
public final class UniformBufferCreateInfo {
    public final UniformUpdateFrequency updateFrequency;
    public final int size;

    public UniformBufferCreateInfo(UniformUpdateFrequency updateFrequency, int size) {
        this.updateFrequency = updateFrequency;
        this.size = size;
    }
}
