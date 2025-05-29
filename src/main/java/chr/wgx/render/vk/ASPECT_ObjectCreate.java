package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.data.RenderObject;
import chr.wgx.render.info.ObjectCreateInfo;
import chr.wgx.render.vk.data.VulkanRenderObject;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.ptr.PointerPtr;
import club.doki7.vulkan.bitmask.VkAccessFlags;
import club.doki7.vulkan.bitmask.VkBufferUsageFlags;
import club.doki7.vulkan.bitmask.VkPipelineStageFlags;
import club.doki7.vulkan.datatype.VkBufferCopy;
import club.doki7.vulkan.datatype.VkBufferMemoryBarrier;
import club.doki7.vulkan.enumtype.VkResult;
import club.doki7.vma.bitmask.VmaAllocationCreateFlags;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

public final class ASPECT_ObjectCreate {
    public ASPECT_ObjectCreate(VulkanRenderEngine engine) {
        this.engine = engine;
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
            PointerPtr ppData = PointerPtr.allocate(arena);

            for (ObjectCreateInfo oci : infoList) {
                Resource.Buffer vertexStagingBuffer = Resource.Buffer.create(
                        cx,
                        oci.pVertices.byteSize(),
                        VkBufferUsageFlags.TRANSFER_SRC,
                        VmaAllocationCreateFlags.HOST_ACCESS_RANDOM,
                        null
                );
                vertexStagingBuffers.add(vertexStagingBuffer);

                @EnumType(VkResult.class) int result = cx.vma.mapMemory(
                        cx.vmaAllocator,
                        vertexStagingBuffer.allocation,
                        ppData
                );
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("无法映射顶点缓冲区内存, 错误代码: " + VkResult.explain(result));
                }
                MemorySegment pData = ppData.read().reinterpret(oci.pVertices.byteSize());
                pData.copyFrom(oci.pVertices);
                cx.vma.unmapMemory(cx.vmaAllocator, vertexStagingBuffer.allocation);

                Resource.Buffer indexStagingBuffer = Resource.Buffer.create(
                        cx,
                        oci.pIndices.byteSize(),
                        VkBufferUsageFlags.TRANSFER_SRC,
                        VmaAllocationCreateFlags.HOST_ACCESS_RANDOM,
                        null
                );
                indexStagingBuffers.add(indexStagingBuffer);

                result = cx.vma.mapMemory(
                        cx.vmaAllocator,
                        indexStagingBuffer.allocation,
                        ppData
                );
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("无法映射索引缓冲区内存, 错误代码: " + VkResult.explain(result));
                }
                pData = ppData.read().reinterpret(oci.pIndices.byteSize());
                pData.copyFrom(oci.pIndices);
                cx.vma.unmapMemory(cx.vmaAllocator, indexStagingBuffer.allocation);

                Resource.Buffer vertexBuffer = Resource.Buffer.create(
                        cx,
                        oci.pVertices.byteSize(),
                        VkBufferUsageFlags.TRANSFER_DST | VkBufferUsageFlags.VERTEX_BUFFER,
                        0,
                        null
                );
                vertexBuffers.add(vertexBuffer);

                Resource.Buffer indexBuffer = Resource.Buffer.create(
                        cx,
                        oci.pIndices.byteSize(),
                        VkBufferUsageFlags.TRANSFER_DST | VkBufferUsageFlags.INDEX_BUFFER,
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
                        cx.dCmd.cmdCopyBuffer(cmd, stagingBuffer.buffer, targetBuffer.buffer, 1, copyRegion);

                        barrier.srcAccessMask(VkAccessFlags.TRANSFER_WRITE);
                        barrier.dstAccessMask(VkAccessFlags.VERTEX_ATTRIBUTE_READ);
                        barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                        barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                        barrier.buffer(targetBuffer.buffer);
                        barrier.offset(0);
                        barrier.size(oci.pVertices.byteSize());
                        cx.dCmd.cmdPipelineBarrier(
                                cmd,
                                VkPipelineStageFlags.ALL_COMMANDS,
                                VkPipelineStageFlags.ALL_COMMANDS,
                                0,
                                0, null,
                                1, barrier,
                                0, null
                        );

                        stagingBuffer = indexStagingBuffers.get(i);
                        targetBuffer = indexBuffers.get(i);
                        copyRegion.size(oci.pIndices.byteSize());
                        cx.dCmd.cmdCopyBuffer(cmd, stagingBuffer.buffer, targetBuffer.buffer, 1, copyRegion);

                        barrier.srcAccessMask(VkAccessFlags.TRANSFER_WRITE);
                        barrier.dstAccessMask(VkAccessFlags.VERTEX_ATTRIBUTE_READ);
                        barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                        barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                        barrier.buffer(targetBuffer.buffer);
                        barrier.offset(0);
                        barrier.size(oci.pIndices.byteSize());
                        cx.dCmd.cmdPipelineBarrier(
                                cmd,
                                VkPipelineStageFlags.ALL_COMMANDS,
                                VkPipelineStageFlags.ALL_COMMANDS,
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
                        barrier.srcAccessMask(VkAccessFlags.TRANSFER_WRITE);
                        barrier.dstAccessMask(VkAccessFlags.VERTEX_ATTRIBUTE_READ);
                        barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                        barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                        barrier.buffer(vertexBuffer.buffer);
                        barrier.offset(0);
                        barrier.size(oci.pVertices.byteSize());
                        cx.dCmd.cmdPipelineBarrier(
                                cmd,
                                VkPipelineStageFlags.ALL_COMMANDS,
                                VkPipelineStageFlags.ALL_COMMANDS,
                                0,
                                0, null,
                                1, barrier,
                                0, null
                        );

                        Resource.Buffer indexBuffer = indexBuffers.get(i);
                        barrier.srcAccessMask(VkAccessFlags.TRANSFER_WRITE);
                        barrier.dstAccessMask(VkAccessFlags.INDEX_READ);
                        barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                        barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                        barrier.buffer(indexBuffer.buffer);
                        barrier.offset(0);
                        barrier.size(oci.pIndices.byteSize());
                        cx.dCmd.cmdPipelineBarrier(
                                cmd,
                                VkPipelineStageFlags.ALL_COMMANDS,
                                VkPipelineStageFlags.ALL_COMMANDS,
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
                        cx.dCmd.cmdCopyBuffer(cmd, stagingBuffer.buffer, targetBuffer.buffer, 1, copyRegion);

                        stagingBuffer = indexStagingBuffers.get(i);
                        targetBuffer = indexBuffers.get(i);
                        copyRegion.size(oci.pIndices.byteSize());
                        cx.dCmd.cmdCopyBuffer(cmd, stagingBuffer.buffer, targetBuffer.buffer, 1, copyRegion);
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
}
