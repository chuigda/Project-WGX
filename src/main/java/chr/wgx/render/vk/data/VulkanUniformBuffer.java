package chr.wgx.render.vk.data;

import chr.wgx.render.common.UniformUpdateFrequency;
import chr.wgx.render.data.UniformBuffer;
import chr.wgx.render.info.UniformBufferCreateInfo;
import chr.wgx.render.vk.IVkDisposable;
import chr.wgx.render.vk.Resource;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VulkanUniformBuffer extends UniformBuffer implements IVkDisposable {
    public final List<Resource.Buffer> underlyingBuffer;
    public final List<MemorySegment> mappedMemory;
    public final byte[] cpuBufferBack;
    public final MemorySegment cpuBuffer;
    public final AtomicBoolean updated = new AtomicBoolean(false);

    public VulkanUniformBuffer(
            UniformBufferCreateInfo createInfo,
            List<Resource.Buffer> underlyingBuffer,
            List<MemorySegment> mappedMemory
    ) {
        super(createInfo);
        this.underlyingBuffer = underlyingBuffer;
        this.mappedMemory = mappedMemory;
        this.cpuBufferBack = new byte[createInfo.bindingInfo.bufferSize];
        this.cpuBuffer = MemorySegment.ofArray(cpuBufferBack);
    }

    @Override
    public void updateBufferContent(MemorySegment segment) {
        synchronized (this) {
            cpuBuffer.copyFrom(segment);
        }

        if (createInfo.updateFrequency != UniformUpdateFrequency.PER_FRAME) {
            updated.set(true);
        }
    }

    @Override
    public void updateBufferContent(Action1<MemorySegment> updateAction) {
        synchronized (this) {
            updateAction.apply(cpuBuffer);
        }

        if (createInfo.updateFrequency != UniformUpdateFrequency.PER_FRAME) {
            updated.set(true);
        }
    }

    @Override
    public void dispose(VulkanRenderEngineContext cx) {
        for (Resource.Buffer buffer : underlyingBuffer) {
            buffer.dispose(cx);
        }
    }
}
