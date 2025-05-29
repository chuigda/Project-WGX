package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.data.Texture;
import chr.wgx.render.info.TextureCreateInfo;
import chr.wgx.render.vk.data.CombinedImageSampler;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.ptr.IntPtr;
import club.doki7.ffm.ptr.PointerPtr;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.*;
import club.doki7.vulkan.datatype.VkBufferImageCopy;
import club.doki7.vulkan.datatype.VkExtent3D;
import club.doki7.vulkan.datatype.VkImageMemoryBarrier;
import club.doki7.vulkan.datatype.VkImageSubresourceLayers;
import club.doki7.vulkan.enumtype.VkFormat;
import club.doki7.vulkan.enumtype.VkImageLayout;
import club.doki7.vulkan.enumtype.VkImageTiling;
import club.doki7.vulkan.enumtype.VkResult;
import club.doki7.vma.bitmask.VmaAllocationCreateFlags;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

public final class ASPECT_TextureCreate {
    ASPECT_TextureCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public List<Texture> createTextureImpl(List<TextureCreateInfo> infoList) throws RenderException {
        VulkanRenderEngineContext cx = engine.cx;

        List<Resource.Buffer> stagingBufferList = new ArrayList<>();
        List<Resource.Image> imageList = new ArrayList<>();
        List<Resource.Sampler> samplerList = new ArrayList<>();

        try (Arena arena = Arena.ofConfined()) {
            for (TextureCreateInfo info : infoList) {
                long imageSize = (long) info.image.getWidth() * info.image.getHeight() * 4;

                Resource.Buffer stagingBuffer = Resource.Buffer.create(
                        cx,
                        imageSize,
                        VkBufferUsageFlags.TRANSFER_SRC,
                        VmaAllocationCreateFlags.HOST_ACCESS_RANDOM,
                        null
                );
                stagingBufferList.add(stagingBuffer);

                PointerPtr ppData = PointerPtr.allocate(arena);
                @EnumType(VkResult.class) int result = cx.vma.mapMemory(
                        cx.vmaAllocator,
                        stagingBuffer.allocation,
                        ppData
                );
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("vmaMapMemory 失败: " + result);
                }

                MemorySegment pData = ppData.read().reinterpret(imageSize);
                IntPtr colorBuf = new IntPtr(pData);
                for (int y = 0; y < info.image.getHeight(); y++) {
                    for (int x = 0; x < info.image.getWidth(); x++) {
                        int data = info.image.getRGB(x, y);
                        int linearIndex = y * info.image.getWidth() + x;
                        colorBuf.write(linearIndex, data);
                    }
                }
                cx.vma.unmapMemory(cx.vmaAllocator, stagingBuffer.allocation);

                Resource.Image image = Resource.Image.create(
                        cx,
                        info.image.getWidth(),
                        info.image.getHeight(),
                        1,
                        VkSampleCountFlags._1,
                        VkFormat.R8G8B8A8_SRGB,
                        VkImageTiling.OPTIMAL,
                        VkImageUsageFlags.TRANSFER_DST | VkImageUsageFlags.SAMPLED,
                        VkImageAspectFlags.COLOR
                );
                imageList.add(image);

                samplerList.add(Resource.Sampler.create(cx, 0));
            }

            if (cx.dedicatedTransferQueue.isSome()) {
                cx.executeTransferCommand(cmdBuf -> {
                    VkImageMemoryBarrier.Ptr barriers = VkImageMemoryBarrier.allocate(arena, infoList.size());
                    for (int i = 0; i < infoList.size(); i++) {
                        Resource.Image image = imageList.get(i);
                        VkImageMemoryBarrier barrier = barriers.at(i);

                        imageLayout_undefinedToTransferDest(
                                image,
                                barrier
                        );
                    }
                    cx.dCmd.cmdPipelineBarrier(
                            cmdBuf,
                            VkPipelineStageFlags.TOP_OF_PIPE,
                            VkPipelineStageFlags.TRANSFER,
                            VkDependencyFlags.BY_REGION,
                            0, null,
                            0, null,
                            infoList.size(), barriers
                    );

                    VkBufferImageCopy copyRegion = VkBufferImageCopy.allocate(arena);
                    VkImageSubresourceLayers imageSubresource = copyRegion.imageSubresource();
                    imageSubresource.aspectMask(VkImageAspectFlags.COLOR);
                    imageSubresource.mipLevel(0);
                    imageSubresource.baseArrayLayer(0);
                    imageSubresource.layerCount(1);
                    VkExtent3D extent = copyRegion.imageExtent();
                    extent.depth(1);

                    for (int i = 0; i < infoList.size(); i++) {
                        TextureCreateInfo info = infoList.get(i);
                        Resource.Buffer stagingBuffer = stagingBufferList.get(i);
                        Resource.Image image = imageList.get(i);

                        extent.width(info.image.getWidth());
                        extent.height(info.image.getHeight());

                        cx.dCmd.cmdCopyBufferToImage(
                                cmdBuf,
                                stagingBuffer.buffer,
                                image.image,
                                VkImageLayout.TRANSFER_DST_OPTIMAL,
                                1,
                                copyRegion
                        );
                    }
                });

                cx.executeGraphicsCommand(cmdBuf -> {
                    VkImageMemoryBarrier.Ptr barriers = VkImageMemoryBarrier.allocate(arena, infoList.size());
                    for (int i = 0; i < infoList.size(); i++) {
                        Resource.Image image = imageList.get(i);
                        VkImageMemoryBarrier barrier = barriers.at(i);

                        imageLayout_transferDestToSharedReadOnly(
                                image,
                                barrier,
                                cx.dedicatedTransferQueueFamilyIndex.get(),
                                cx.graphicsQueueFamilyIndex
                        );
                    }
                    cx.dCmd.cmdPipelineBarrier(
                            cmdBuf,
                            VkPipelineStageFlags.TRANSFER,
                            VkPipelineStageFlags.FRAGMENT_SHADER,
                            VkDependencyFlags.BY_REGION,
                            0, null,
                            0, null,
                            infoList.size(), barriers
                    );
                });
            } else {
                cx.executeGraphicsCommand(cmdBuf -> {
                    VkImageMemoryBarrier.Ptr barriers = VkImageMemoryBarrier.allocate(arena, infoList.size());
                    for (int i = 0; i < infoList.size(); i++) {
                        Resource.Image image = imageList.get(i);
                        VkImageMemoryBarrier barrier = barriers.at(i);

                        imageLayout_undefinedToTransferDest(
                                image,
                                barrier
                        );
                    }
                    cx.dCmd.cmdPipelineBarrier(
                            cmdBuf,
                            VkPipelineStageFlags.TOP_OF_PIPE,
                            VkPipelineStageFlags.TRANSFER,
                            VkDependencyFlags.BY_REGION,
                            0, null,
                            0, null,
                            infoList.size(), barriers
                    );

                    VkBufferImageCopy copyRegion = VkBufferImageCopy.allocate(arena);
                    VkImageSubresourceLayers imageSubresource = copyRegion.imageSubresource();
                    imageSubresource.aspectMask(VkImageAspectFlags.COLOR);
                    imageSubresource.mipLevel(0);
                    imageSubresource.baseArrayLayer(0);
                    imageSubresource.layerCount(1);
                    VkExtent3D extent = copyRegion.imageExtent();
                    extent.depth(1);

                    for (int i = 0; i < infoList.size(); i++) {
                        TextureCreateInfo info = infoList.get(i);
                        Resource.Buffer stagingBuffer = stagingBufferList.get(i);
                        Resource.Image image = imageList.get(i);

                        extent.width(info.image.getWidth());
                        extent.height(info.image.getHeight());

                        cx.dCmd.cmdCopyBufferToImage(
                                cmdBuf,
                                stagingBuffer.buffer,
                                image.image,
                                VkImageLayout.TRANSFER_DST_OPTIMAL,
                                1,
                                copyRegion
                        );
                    }

                    for (int i = 0; i < infoList.size(); i++) {
                        Resource.Image image = imageList.get(i);
                        VkImageMemoryBarrier barrier = barriers.at(i);

                        imageLayout_transferDestToSharedReadOnly(
                                image,
                                barrier,
                                VkConstants.QUEUE_FAMILY_IGNORED,
                                VkConstants.QUEUE_FAMILY_IGNORED
                        );
                    }
                    cx.dCmd.cmdPipelineBarrier(
                            cmdBuf,
                            VkPipelineStageFlags.TRANSFER,
                            VkPipelineStageFlags.FRAGMENT_SHADER,
                            VkDependencyFlags.BY_REGION,
                            0, null,
                            0, null,
                            infoList.size(), barriers
                    );
                });
            }

            List<Texture> textures = new ArrayList<>();
            for (int i = 0; i < infoList.size(); i++) {
                Resource.Image image = imageList.get(i);
                Resource.Sampler sampler = samplerList.get(i);

                CombinedImageSampler cis = new CombinedImageSampler(image, sampler);
                engine.textures.add(cis);
                textures.add(cis);
            }
            return textures;
        } catch (RenderException e) {
            for (Resource.Image image : imageList) {
                image.dispose(cx);
            }
            for (Resource.Sampler sampler : samplerList) {
                sampler.dispose(cx);
            }
            throw e;
        } finally {
            for (Resource.Buffer stagingBuffer : stagingBufferList) {
                stagingBuffer.dispose(cx);
            }
        }
    }

    private void imageLayout_undefinedToTransferDest(Resource.Image image, VkImageMemoryBarrier barrier) {
        barrier.srcAccessMask(0);
        barrier.dstAccessMask(VkAccessFlags.TRANSFER_WRITE);
        barrier.oldLayout(VkImageLayout.UNDEFINED);
        barrier.newLayout(VkImageLayout.TRANSFER_DST_OPTIMAL);
        barrier.srcQueueFamilyIndex(VkConstants.QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex(VkConstants.QUEUE_FAMILY_IGNORED);
        barrier.image(image.image);
        barrier.subresourceRange().aspectMask(VkImageAspectFlags.COLOR);
        barrier.subresourceRange().baseMipLevel(0);
        barrier.subresourceRange().levelCount(1);
        barrier.subresourceRange().baseArrayLayer(0);
        barrier.subresourceRange().layerCount(1);
    }

    private void imageLayout_transferDestToSharedReadOnly(
            Resource.Image image,
            VkImageMemoryBarrier barrier,
            int srcQueueFamilyIndex,
            int dstQueueFamilyIndex
    ) {
        barrier.srcAccessMask(0);
        barrier.dstAccessMask(VkAccessFlags.SHADER_READ);
        barrier.oldLayout(VkImageLayout.TRANSFER_DST_OPTIMAL);
        barrier.newLayout(VkImageLayout.SHADER_READ_ONLY_OPTIMAL);
        barrier.srcQueueFamilyIndex(srcQueueFamilyIndex);
        barrier.dstQueueFamilyIndex(dstQueueFamilyIndex);
        barrier.image(image.image);
        barrier.subresourceRange().aspectMask(VkImageAspectFlags.COLOR);
        barrier.subresourceRange().baseMipLevel(0);
        barrier.subresourceRange().levelCount(1);
        barrier.subresourceRange().baseArrayLayer(0);
        barrier.subresourceRange().layerCount(1);
    }

    private final VulkanRenderEngine engine;
}
