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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class ObjectCreateAspect {
    public ObjectCreateAspect(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public ObjectHandle createObjectImpl(ObjectCreateInfo info) throws RenderException {
        return createObjectImpl(List.of(info)).getFirst();
    }

    public List<ObjectHandle> createObjectImpl(List<ObjectCreateInfo> info) throws RenderException {
        VulkanRenderEngineContext cx = engine.cx;

        List<Long> vertexCounts = info.stream()
                .map(i -> {
                    long bufferSize = i.pData.byteSize();
                    assert bufferSize % i.vertexInputInfo.stride == 0;
                    return bufferSize / i.vertexInputInfo.stride;
                })
                .toList();
        List<Resource.Buffer> stagingBuffers = new ArrayList<>();
        List<Resource.Buffer> vertexBuffers = new ArrayList<>();
        try (Arena arena = Arena.ofConfined()) {
            PointerBuffer ppData = PointerBuffer.allocate(arena);

            for (ObjectCreateInfo oci : info) {
                Resource.Buffer stagingBuffer = Resource.Buffer.create(
                        cx,
                        oci.pData.byteSize(),
                        VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                        VmaAllocationCreateFlags.VMA_ALLOCATION_CREATE_HOST_ACCESS_RANDOM_BIT,
                        null
                );
                stagingBuffers.add(stagingBuffer);

                @enumtype(VkResult.class) int result = cx.vma.vmaMapMemory(
                        cx.vmaAllocator,
                        stagingBuffer.allocation,
                        ppData
                );
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法映射缓冲区内存, 错误代码: " + VkResult.explain(result));
                }
                MemorySegment pData = ppData.read().reinterpret(oci.pData.byteSize());
                pData.copyFrom(oci.pData);
                cx.vma.vmaUnmapMemory(cx.vmaAllocator, stagingBuffer.allocation);

                Resource.Buffer vertexBuffer = Resource.Buffer.create(
                        cx,
                        oci.pData.byteSize(),
                        VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_DST_BIT
                                | VkBufferUsageFlags.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                        0,
                        null
                );
                vertexBuffers.add(vertexBuffer);
            }

            if (cx.dedicatedTransferQueue.isSome()) {
                cx.executeTransferCommand(cmd -> {
                    VkBufferCopy copyRegion = VkBufferCopy.allocate(arena);
                    VkBufferMemoryBarrier barrier = VkBufferMemoryBarrier.allocate(arena);
                    for (int i = 0; i < info.size(); i++) {
                        ObjectCreateInfo oci = info.get(i);
                        Resource.Buffer stagingBuffer = stagingBuffers.get(i);
                        Resource.Buffer vertexBuffer = vertexBuffers.get(i);

                        copyRegion.size(oci.pData.byteSize());
                        cx.dCmd.vkCmdCopyBuffer(cmd, stagingBuffer.buffer, vertexBuffer.buffer, 1, copyRegion);

                        barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                        barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
                        barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                        barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                        barrier.buffer(vertexBuffer.buffer);
                        barrier.offset(0);
                        barrier.size(oci.pData.byteSize());
                        cx.dCmd.vkCmdPipelineBarrier(
                                cmd,
                                VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                                VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                                0,
                                0, null,
                                1, barrier,
                                0, null
                        );
                    }
                });

                cx.executeGraphicsCommand(cmd -> {
                    VkBufferMemoryBarrier barrier = VkBufferMemoryBarrier.allocate(arena);
                    for (int i = 0; i < info.size(); i++) {
                        ObjectCreateInfo oci = info.get(i);
                        Resource.Buffer vertexBuffer = vertexBuffers.get(i);

                        barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                        barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
                        barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                        barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                        barrier.buffer(vertexBuffer.buffer);
                        barrier.offset(0);
                        barrier.size(oci.pData.byteSize());
                        cx.dCmd.vkCmdPipelineBarrier(
                                cmd,
                                VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                                VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                                0,
                                0, null,
                                1, barrier,
                                0, null
                        );
                    }
                });
            }
            else {
                cx.executeGraphicsCommand(cmd -> {
                    VkBufferCopy copyRegion = VkBufferCopy.allocate(arena);
                    for (int i = 0; i < info.size(); i++) {
                        ObjectCreateInfo oci = info.get(i);
                        Resource.Buffer stagingBuffer = stagingBuffers.get(i);
                        Resource.Buffer vertexBuffer = vertexBuffers.get(i);

                        copyRegion.size(oci.pData.byteSize());
                        cx.dCmd.vkCmdCopyBuffer(cmd, stagingBuffer.buffer, vertexBuffer.buffer, 1, copyRegion);
                    }
                });
            }

            List<ObjectHandle> handles = new ArrayList<>();
            for (int i = 0; i < info.size(); i++) {
                ObjectCreateInfo oci = info.get(i);
                Resource.Buffer vertexBuffer = vertexBuffers.get(i);
                long vertexCount = vertexCounts.get(i);

                long handle = engine.nextHandle();
                synchronized (engine.objects) {
                    engine.objects.put(handle, new Resource.Object(vertexBuffer, oci.vertexInputInfo, (int) vertexCount));
                }
                handles.add(new ObjectHandle(handle));
            }
            return handles;
        }
        catch (RenderException e) {
            for (Resource.Buffer buffer : vertexBuffers) {
                buffer.dispose(cx);
            }
            throw e;
        }
        finally {
            for (Resource.Buffer buffer : stagingBuffers) {
                buffer.dispose(cx);
            }
        }
    }

    private final VulkanRenderEngine engine;
    private static final Logger logger = Logger.getLogger(ObjectCreateAspect.class.getName());
}
