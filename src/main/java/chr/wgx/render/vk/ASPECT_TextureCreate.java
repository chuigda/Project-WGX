package chr.wgx.render.vk;

import chr.wgx.config.Config;
import chr.wgx.render.RenderException;
import chr.wgx.render.data.Texture;
import chr.wgx.render.info.TextureCreateInfo;
import chr.wgx.render.vk.data.CombinedImageSampler;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.panama.buffer.PointerBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.*;
import tech.icey.vk4j.datatype.VkBufferImageCopy;
import tech.icey.vk4j.datatype.VkExtent3D;
import tech.icey.vk4j.datatype.VkImageMemoryBarrier;
import tech.icey.vk4j.datatype.VkImageSubresourceLayers;
import tech.icey.vk4j.enumtype.VkFormat;
import tech.icey.vk4j.enumtype.VkImageLayout;
import tech.icey.vk4j.enumtype.VkImageTiling;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vma.bitmask.VmaAllocationCreateFlags;

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

        @enumtype(VkFormat.class) int format = Config.config().vulkanConfig.forceUNORM ?
                VkFormat.VK_FORMAT_R8G8B8A8_UNORM :
                VkFormat.VK_FORMAT_R8G8B8A8_SRGB;

        List<Resource.Buffer> stagingBufferList = new ArrayList<>();
        List<Resource.Image> imageList = new ArrayList<>();
        List<Resource.Sampler> samplerList = new ArrayList<>();

        try (Arena arena = Arena.ofConfined()) {
            for (TextureCreateInfo info : infoList) {
                long imageSize = (long) info.image.getWidth() * info.image.getHeight() * 4;

                Resource.Buffer stagingBuffer = Resource.Buffer.create(
                        cx,
                        imageSize,
                        VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                        VmaAllocationCreateFlags.VMA_ALLOCATION_CREATE_HOST_ACCESS_RANDOM_BIT,
                        null
                );
                stagingBufferList.add(stagingBuffer);

                PointerBuffer ppData = PointerBuffer.allocate(arena);
                @enumtype(VkResult.class) int result = cx.vma.vmaMapMemory(
                        cx.vmaAllocator,
                        stagingBuffer.allocation,
                        ppData
                );
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("vmaMapMemory 失败: " + result);
                }

                MemorySegment pData = ppData.read().reinterpret(imageSize);
                IntBuffer colorBuf = new IntBuffer(pData);
                for (int y = 0; y < info.image.getHeight(); y++) {
                    for (int x = 0; x < info.image.getWidth(); x++) {
                        int data = info.image.getRGB(x, y);
                        int linearIndex = y * info.image.getWidth() + x;
                        colorBuf.write(linearIndex, data);
                    }
                }
                cx.vma.vmaUnmapMemory(cx.vmaAllocator, stagingBuffer.allocation);

                Resource.Image image = Resource.Image.create(
                        cx,
                        info.image.getWidth(),
                        info.image.getHeight(),
                        1,
                        VkSampleCountFlags.VK_SAMPLE_COUNT_1_BIT,
                        format,
                        VkImageTiling.VK_IMAGE_TILING_OPTIMAL,
                        VkImageUsageFlags.VK_IMAGE_USAGE_TRANSFER_DST_BIT
                        | VkImageUsageFlags.VK_IMAGE_USAGE_SAMPLED_BIT,
                        VkImageAspectFlags.VK_IMAGE_ASPECT_COLOR_BIT
                );
                imageList.add(image);

                samplerList.add(Resource.Sampler.create(cx, 0));
            }

            if (cx.dedicatedTransferQueue.isSome()) {
                throw new UnsupportedOperationException("暂不支持专用传输队列");
            } else {
                cx.executeGraphicsCommand(cmdBuf -> {
                    VkImageMemoryBarrier[] barriers = VkImageMemoryBarrier.allocate(arena, infoList.size());
                    for (int i = 0; i < infoList.size(); i++) {
                        Resource.Image image = imageList.get(i);
                        VkImageMemoryBarrier barrier = barriers[i];
                        barrier.srcAccessMask(0);
                        barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                        barrier.oldLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED);
                        barrier.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
                        barrier.srcQueueFamilyIndex(Constants.VK_QUEUE_FAMILY_IGNORED);
                        barrier.dstQueueFamilyIndex(Constants.VK_QUEUE_FAMILY_IGNORED);
                        barrier.image(image.image);
                        barrier.subresourceRange().aspectMask(VkImageAspectFlags.VK_IMAGE_ASPECT_COLOR_BIT);
                        barrier.subresourceRange().baseMipLevel(0);
                        barrier.subresourceRange().levelCount(1);
                        barrier.subresourceRange().baseArrayLayer(0);
                        barrier.subresourceRange().layerCount(1);
                    }
                    cx.dCmd.vkCmdPipelineBarrier(
                            cmdBuf,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_TRANSFER_BIT,
                            VkDependencyFlags.VK_DEPENDENCY_BY_REGION_BIT,
                            0, null,
                            0, null,
                            infoList.size(), barriers[0]
                    );

                    VkBufferImageCopy copyRegion = VkBufferImageCopy.allocate(arena);
                    VkImageSubresourceLayers imageSubresource = copyRegion.imageSubresource();
                    imageSubresource.aspectMask(VkImageAspectFlags.VK_IMAGE_ASPECT_COLOR_BIT);
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

                        cx.dCmd.vkCmdCopyBufferToImage(
                                cmdBuf,
                                stagingBuffer.buffer,
                                image.image,
                                VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                                1,
                                copyRegion
                        );
                    }

                    for (int i = 0; i < infoList.size(); i++) {
                        Resource.Image image = imageList.get(i);
                        VkImageMemoryBarrier barrier = barriers[i];
                        barrier.srcAccessMask(0);
                        barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_SHADER_READ_BIT);
                        barrier.oldLayout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
                        barrier.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                        barrier.srcQueueFamilyIndex(Constants.VK_QUEUE_FAMILY_IGNORED);
                        barrier.dstQueueFamilyIndex(Constants.VK_QUEUE_FAMILY_IGNORED);
                        barrier.image(image.image);
                        barrier.subresourceRange().aspectMask(VkImageAspectFlags.VK_IMAGE_ASPECT_COLOR_BIT);
                        barrier.subresourceRange().baseMipLevel(0);
                        barrier.subresourceRange().levelCount(1);
                        barrier.subresourceRange().baseArrayLayer(0);
                        barrier.subresourceRange().layerCount(1);
                    }
                    cx.dCmd.vkCmdPipelineBarrier(
                            cmdBuf,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_TRANSFER_BIT,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                            VkDependencyFlags.VK_DEPENDENCY_BY_REGION_BIT,
                            0, null,
                            0, null,
                            infoList.size(), barriers[0]
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

    private final VulkanRenderEngine engine;
}
