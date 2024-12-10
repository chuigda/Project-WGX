package chr.wgx.render.data;

import chr.wgx.render.info.UniformBufferCreateInfo;

public abstract class UniformBuffer {
    public final UniformBufferCreateInfo createInfo;

    public UniformBuffer(UniformBufferCreateInfo createInfo) {
        this.createInfo = createInfo;
    }
}
