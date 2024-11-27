package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.handle.ObjectHandle;
import chr.wgx.render.info.ObjectCreateInfo;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.PointerBuffer;
import tech.icey.vk4j.bitmask.VkAccessFlags;
import tech.icey.vk4j.bitmask.VkBufferUsageFlags;
import tech.icey.vk4j.bitmask.VkPipelineStageFlags;
import tech.icey.vk4j.datatype.VkBufferCopy;
import tech.icey.vk4j.datatype.VkBufferMemoryBarrier;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vma.bitmask.VmaAllocationCreateFlags;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.logging.Logger;

public final class ObjectCreate {
    public ObjectCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public ObjectHandle createObjectImpl(ObjectCreateInfo info) throws RenderException {
        VulkanRenderEngineContext cx = engine.cx;

        long bufferSize = info.pData.segment().byteSize();
        assert bufferSize % info.vertexInputInfo.stride == 0;
        long vertexCount = bufferSize / info.vertexInputInfo.stride;

        try (Arena arena = Arena.ofConfined()) {
            Resource.Buffer stagingBuffer = Resource.Buffer.create(
                    cx,
                    bufferSize,
                    VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VmaAllocationCreateFlags.VMA_ALLOCATION_CREATE_HOST_ACCESS_RANDOM_BIT,
                    null
            );

            PointerBuffer ppData = PointerBuffer.allocate(arena);
            @enumtype(VkResult.class) int result = cx.vma.vmaMapMemory(
                    cx.vmaAllocator,
                    stagingBuffer.allocation,
                    ppData
            );
            if (result != VkResult.VK_SUCCESS) {
                stagingBuffer.dispose(cx);
                throw new RenderException("无法映射缓冲区内存, 错误代码: " + VkResult.explain(result));
            }
            MemorySegment pData = ppData.read().reinterpret(bufferSize);
            pData.copyFrom(info.pData.segment());
            cx.vma.vmaUnmapMemory(cx.vmaAllocator, stagingBuffer.allocation);

            Resource.Buffer vertexBuffer = Resource.Buffer.create(
                    cx,
                    bufferSize,
                    VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_DST_BIT
                            | VkBufferUsageFlags.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                    0,
                    null
            );

            if (cx.dedicatedTransferQueue.isSome()) {
                cx.executeTransferCommand(cmd -> {
                    VkBufferCopy copyRegion = VkBufferCopy.allocate(arena);
                    copyRegion.size(bufferSize);
                    cx.dCmd.vkCmdCopyBuffer(cmd, stagingBuffer.buffer, vertexBuffer.buffer, 1, copyRegion);

                    VkBufferMemoryBarrier barrier = VkBufferMemoryBarrier.allocate(arena);
                    barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                    barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
                    barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                    barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                    barrier.buffer(vertexBuffer.buffer);
                    barrier.offset(0);
                    barrier.size(bufferSize);
                    cx.dCmd.vkCmdPipelineBarrier(
                            cmd,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                            0,
                            0, null,
                            1, barrier,
                            0, null
                    );
                });
                stagingBuffer.dispose(cx);

                cx.executeGraphicsCommand(cmd -> {
                    VkBufferMemoryBarrier barrier = VkBufferMemoryBarrier.allocate(arena);
                    barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                    barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
                    barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                    barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                    barrier.buffer(vertexBuffer.buffer);
                    barrier.offset(0);
                    barrier.size(bufferSize);
                    cx.dCmd.vkCmdPipelineBarrier(
                            cmd,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                            0,
                            0, null,
                            1, barrier,
                            0, null
                    );
                });

                long handle = engine.nextHandle();
                synchronized (engine.objects) {
                    engine.objects.put(handle, new Resource.Object(vertexBuffer, info.vertexInputInfo, vertexCount));
                }
                return new ObjectHandle(handle);
            }
            else {
                cx.executeGraphicsCommand(cmd -> {
                    VkBufferCopy copyRegion = VkBufferCopy.allocate(arena);
                    copyRegion.size(bufferSize);
                    cx.dCmd.vkCmdCopyBuffer(cmd, stagingBuffer.buffer, vertexBuffer.buffer, 1, copyRegion);
                });
                stagingBuffer.dispose(cx);

                long handle = engine.nextHandle();
                synchronized (engine.objects) {
                    engine.objects.put(handle, new Resource.Object(vertexBuffer, info.vertexInputInfo, vertexCount));
                }
                return new ObjectHandle(handle);
            }
        }
    }

    private final VulkanRenderEngine engine;
    private static final Logger logger = Logger.getLogger(ObjectCreate.class.getName());
}
