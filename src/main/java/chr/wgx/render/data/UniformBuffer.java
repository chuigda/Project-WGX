package chr.wgx.render.data;

import chr.wgx.render.info.UniformBufferCreateInfo;

public abstract non-sealed class UniformBuffer extends Descriptor {
    public final UniformBufferCreateInfo createInfo;

    public UniformBuffer(UniformBufferCreateInfo createInfo) {
        this.createInfo = createInfo;
    }
}
