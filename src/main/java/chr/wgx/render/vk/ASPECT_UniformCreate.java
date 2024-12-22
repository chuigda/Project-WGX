package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.common.UniformUpdateFrequency;
import chr.wgx.render.info.UniformBufferCreateInfo;
import chr.wgx.render.vk.data.VulkanUniformBuffer;
import tech.icey.vk4j.bitmask.VkBufferUsageFlags;
import tech.icey.vma.bitmask.VmaAllocationCreateFlags;
import tech.icey.vma.datatype.VmaAllocationInfo;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

public final class ASPECT_UniformCreate {
    ASPECT_UniformCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public VulkanUniformBuffer createUniformImpl(UniformBufferCreateInfo info) throws RenderException {
        VulkanRenderEngineContext cx = engine.cx;
        Swapchain swapchain = engine.swapchain;

        int bufferCount = info.updateFrequency == UniformUpdateFrequency.MANUAL
                ? 1
                : swapchain.swapchainImages.length;

        try (Arena arena = Arena.ofConfined()) {
            VmaAllocationInfo allocationInfo = VmaAllocationInfo.allocate(arena);

            List<Resource.Buffer> buffers = new ArrayList<>();
            List<MemorySegment> mappedMemory = new ArrayList<>();
            for (int i = 0; i < bufferCount; i++) {
                buffers.add(Resource.Buffer.create(
                        cx,
                        info.bindingInfo.bufferSize,
                        VkBufferUsageFlags.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                        VmaAllocationCreateFlags.VMA_ALLOCATION_CREATE_MAPPED_BIT
                        | VmaAllocationCreateFlags.VMA_ALLOCATION_CREATE_HOST_ACCESS_RANDOM_BIT,
                        allocationInfo
                ));
                mappedMemory.add(allocationInfo.pMappedData().reinterpret(info.bindingInfo.bufferSize));
            }

            if (info.init instanceof Option.Some<MemorySegment> someInit) {
                MemorySegment initSegment = someInit.value;
                for (MemorySegment memorySegment : mappedMemory) {
                    memorySegment.copyFrom(initSegment);
                }
            }

            VulkanUniformBuffer ret = new VulkanUniformBuffer(
                    info,
                    buffers,
                    mappedMemory,
                    info.updateFrequency == UniformUpdateFrequency.PER_FRAME
                            ? Option.none()
                            : Option.some(engine.uniformManuallyUpdated)
            );
            if (info.updateFrequency == UniformUpdateFrequency.PER_FRAME) {
                engine.framelyUpdatedUniforms.add(ret);
            }
            else {
                engine.manuallyUpdatedUniforms.add(ret);
            }
            return ret;
        }
    }

    private final VulkanRenderEngine engine;
}
