package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.data.RenderObject;
import chr.wgx.render.info.ObjectCreateInfo;
import chr.wgx.render.vk.data.VulkanRenderObject;
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

public final class ASPECT_ObjectCreate {
    public ASPECT_ObjectCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public RenderObject createObjectImpl(ObjectCreateInfo info) throws RenderException {
        return createObjectImpl(List.of(info)).getFirst();
    }

    public List<RenderObject> createObjectImpl(List<ObjectCreateInfo> infoList) throws RenderException {
        VulkanRenderEngineContext cx = engine.cx;

        List<Integer> vertexCounts = infoList.stream()
                .map(i -> {
                    long bufferSize = i.pVertices.byteSize();
                    assert bufferSize % i.vertexInputInfo.stride == 0;
                    return (int) (bufferSize / i.vertexInputInfo.stride);
                })
                .toList();
        List<Integer> indexCounts = infoList.stream()
                .map(i -> {
                    long bufferSize = i.pIndices.byteSize();
                    assert bufferSize % Integer.BYTES == 0;
                    return (int) (bufferSize / Integer.BYTES);
                })
                .toList();
        List<Resource.Buffer> vertexStagingBuffers = new ArrayList<>();
        List<Resource.Buffer> indexStagingBuffers = new ArrayList<>();
        List<Resource.Buffer> vertexBuffers = new ArrayList<>();
        List<Resource.Buffer> indexBuffers = new ArrayList<>();
        try (Arena arena = Arena.ofConfined()) {
            PointerBuffer ppData = PointerBuffer.allocate(arena);

            for (ObjectCreateInfo oci : infoList) {
                Resource.Buffer vertexStagingBuffer = Resource.Buffer.create(
                        cx,
                        oci.pVertices.byteSize(),
                        VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                        VmaAllocationCreateFlags.VMA_ALLOCATION_CREATE_HOST_ACCESS_RANDOM_BIT,
                        null
                );
                vertexStagingBuffers.add(vertexStagingBuffer);

                @enumtype(VkResult.class) int result = cx.vma.vmaMapMemory(
                        cx.vmaAllocator,
                        vertexStagingBuffer.allocation,
                        ppData
                );
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法映射顶点缓冲区内存, 错误代码: " + VkResult.explain(result));
                }
                MemorySegment pData = ppData.read().reinterpret(oci.pVertices.byteSize());
                pData.copyFrom(oci.pVertices);
                cx.vma.vmaUnmapMemory(cx.vmaAllocator, vertexStagingBuffer.allocation);

                Resource.Buffer indexStagingBuffer = Resource.Buffer.create(
                        cx,
                        oci.pIndices.byteSize(),
                        VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                        VmaAllocationCreateFlags.VMA_ALLOCATION_CREATE_HOST_ACCESS_RANDOM_BIT,
                        null
                );
                indexStagingBuffers.add(indexStagingBuffer);

                result = cx.vma.vmaMapMemory(
                        cx.vmaAllocator,
                        indexStagingBuffer.allocation,
                        ppData
                );
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法映射索引缓冲区内存, 错误代码: " + VkResult.explain(result));
                }
                pData = ppData.read().reinterpret(oci.pIndices.byteSize());
                pData.copyFrom(oci.pIndices);
                cx.vma.vmaUnmapMemory(cx.vmaAllocator, indexStagingBuffer.allocation);

                Resource.Buffer vertexBuffer = Resource.Buffer.create(
                        cx,
                        oci.pVertices.byteSize(),
                        VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_DST_BIT
                        | VkBufferUsageFlags.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                        0,
                        null
                );
                vertexBuffers.add(vertexBuffer);

                Resource.Buffer indexBuffer = Resource.Buffer.create(
                        cx,
                        oci.pIndices.byteSize(),
                        VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_DST_BIT
                        | VkBufferUsageFlags.VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                        0,
                        null
                );
                indexBuffers.add(indexBuffer);
            }

            if (cx.dedicatedTransferQueue.isSome()) {
                cx.executeTransferCommand(cmd -> {
                    VkBufferCopy copyRegion = VkBufferCopy.allocate(arena);
                    VkBufferMemoryBarrier barrier = VkBufferMemoryBarrier.allocate(arena);
                    for (int i = 0; i < infoList.size(); i++) {
                        ObjectCreateInfo oci = infoList.get(i);
                        Resource.Buffer stagingBuffer = vertexStagingBuffers.get(i);
                        Resource.Buffer targetBuffer = vertexBuffers.get(i);

                        copyRegion.size(oci.pVertices.byteSize());
                        cx.dCmd.vkCmdCopyBuffer(cmd, stagingBuffer.buffer, targetBuffer.buffer, 1, copyRegion);

                        barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                        barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
                        barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                        barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                        barrier.buffer(targetBuffer.buffer);
                        barrier.offset(0);
                        barrier.size(oci.pVertices.byteSize());
                        cx.dCmd.vkCmdPipelineBarrier(
                                cmd,
                                VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                                VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                                0,
                                0, null,
                                1, barrier,
                                0, null
                        );

                        stagingBuffer = indexStagingBuffers.get(i);
                        targetBuffer = indexBuffers.get(i);
                        copyRegion.size(oci.pIndices.byteSize());
                        cx.dCmd.vkCmdCopyBuffer(cmd, stagingBuffer.buffer, targetBuffer.buffer, 1, copyRegion);

                        barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                        barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
                        barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                        barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                        barrier.buffer(targetBuffer.buffer);
                        barrier.offset(0);
                        barrier.size(oci.pIndices.byteSize());
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
                    for (int i = 0; i < infoList.size(); i++) {
                        ObjectCreateInfo oci = infoList.get(i);

                        Resource.Buffer vertexBuffer = vertexBuffers.get(i);
                        barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                        barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
                        barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                        barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                        barrier.buffer(vertexBuffer.buffer);
                        barrier.offset(0);
                        barrier.size(oci.pVertices.byteSize());
                        cx.dCmd.vkCmdPipelineBarrier(
                                cmd,
                                VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                                VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                                0,
                                0, null,
                                1, barrier,
                                0, null
                        );

                        Resource.Buffer indexBuffer = indexBuffers.get(i);
                        barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                        barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_INDEX_READ_BIT);
                        barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                        barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                        barrier.buffer(indexBuffer.buffer);
                        barrier.offset(0);
                        barrier.size(oci.pIndices.byteSize());
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
                    for (int i = 0; i < infoList.size(); i++) {
                        ObjectCreateInfo oci = infoList.get(i);
                        Resource.Buffer stagingBuffer = vertexStagingBuffers.get(i);
                        Resource.Buffer targetBuffer = vertexBuffers.get(i);

                        copyRegion.size(oci.pVertices.byteSize());
                        cx.dCmd.vkCmdCopyBuffer(cmd, stagingBuffer.buffer, targetBuffer.buffer, 1, copyRegion);

                        stagingBuffer = indexStagingBuffers.get(i);
                        targetBuffer = indexBuffers.get(i);
                        copyRegion.size(oci.pIndices.byteSize());
                        cx.dCmd.vkCmdCopyBuffer(cmd, stagingBuffer.buffer, targetBuffer.buffer, 1, copyRegion);
                    }
                });
            }

            List<RenderObject> ret = new ArrayList<>();
            for (int i = 0; i < infoList.size(); i++) {
                ObjectCreateInfo oci = infoList.get(i);
                Resource.Buffer vertexBuffer = vertexBuffers.get(i);
                Resource.Buffer indexBuffer = indexBuffers.get(i);
                int vertexCount = vertexCounts.get(i);
                int indexCount = indexCounts.get(i);

                VulkanRenderObject renderObject = new VulkanRenderObject(
                        oci.vertexInputInfo,
                        vertexBuffer,
                        indexBuffer,
                        vertexCount,
                        indexCount
                );
                engine.objects.add(renderObject);
                ret.add(renderObject);
            }
            return ret;
        }
        catch (RenderException e) {
            for (Resource.Buffer buffer : vertexBuffers) {
                buffer.dispose(cx);
            }
            for (Resource.Buffer buffer : indexBuffers) {
                buffer.dispose(cx);
            }
            throw e;
        }
        finally {
            for (Resource.Buffer buffer : vertexStagingBuffers) {
                buffer.dispose(cx);
            }
            for (Resource.Buffer buffer : indexStagingBuffers) {
                buffer.dispose(cx);
            }
        }
    }

    private final VulkanRenderEngine engine;
    private static final Logger logger = Logger.getLogger(ASPECT_ObjectCreate.class.getName());
}
