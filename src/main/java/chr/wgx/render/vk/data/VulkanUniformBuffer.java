package chr.wgx.render.vk.data;

import chr.wgx.render.common.UniformUpdateFrequency;
import chr.wgx.render.data.UniformBuffer;
import chr.wgx.render.info.UniformBufferCreateInfo;
import chr.wgx.render.vk.IVkDisposable;
import chr.wgx.render.vk.Resource;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import club.doki7.vulkan.handle.VkDeviceMemory;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VulkanUniformBuffer extends UniformBuffer implements IVkDisposable {
    public final List<Resource.Buffer> underlyingBuffer;
    public final List<MemorySegment> mappedMemory;
    public final List<VkDeviceMemory> deviceMemoryHandles;
    public final byte[] cpuBufferBack;
    public final MemorySegment cpuBuffer;

    public final Option<AtomicBoolean> updated;

    public VulkanUniformBuffer(
            UniformBufferCreateInfo createInfo,
            List<Resource.Buffer> underlyingBuffer,
            List<MemorySegment> mappedMemory,
            List<VkDeviceMemory> deviceMemoryHandles,
            Option<AtomicBoolean> updated
    ) {
        super(createInfo);
        this.underlyingBuffer = underlyingBuffer;
        this.mappedMemory = mappedMemory;
        this.deviceMemoryHandles = deviceMemoryHandles;
        this.cpuBufferBack = new byte[createInfo.bindingInfo.bufferSize];
        this.cpuBuffer = MemorySegment.ofArray(cpuBufferBack);

        this.updated = updated;
    }

    public synchronized void updateGPU(int frameIndex) {
        assert createInfo.updateFrequency == UniformUpdateFrequency.PER_FRAME;

        mappedMemory.get(frameIndex).copyFrom(cpuBuffer);
    }

    public synchronized void updateGPU() {
        assert createInfo.updateFrequency == UniformUpdateFrequency.MANUAL;
        assert mappedMemory.size() == 1;

        mappedMemory.getFirst().copyFrom(cpuBuffer);
    }

    @Override
    public void updateBufferContent(MemorySegment segment) {
        synchronized (this) {
            cpuBuffer.copyFrom(segment);
        }

        if (updated instanceof Option.Some<AtomicBoolean> some) {
            some.value.set(true);
        }
    }

    @Override
    public void updateBufferContent(Action1<MemorySegment> updateAction) {
        synchronized (this) {
            updateAction.apply(cpuBuffer);
        }

        if (updated instanceof Option.Some<AtomicBoolean> some) {
            some.value.set(true);
        }
    }

    @Override
    public void dispose(VulkanRenderEngineContext cx) {
        for (Resource.Buffer buffer : underlyingBuffer) {
            buffer.dispose(cx);
        }
    }
}
