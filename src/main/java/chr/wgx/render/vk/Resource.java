package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.bitmask.VkImageAspectFlags;
import tech.icey.vk4j.bitmask.VkImageUsageFlags;
import tech.icey.vk4j.bitmask.VkSampleCountFlags;
import tech.icey.vk4j.datatype.VkImageCreateInfo;
import tech.icey.vk4j.datatype.VkImageSubresourceRange;
import tech.icey.vk4j.datatype.VkImageViewCreateInfo;
import tech.icey.vk4j.enumtype.*;
import tech.icey.vk4j.handle.VkImage;
import tech.icey.vk4j.handle.VkImageView;
import tech.icey.vma.datatype.VmaAllocationCreateInfo;
import tech.icey.vma.enumtype.VmaMemoryUsage;
import tech.icey.vma.handle.VmaAllocation;
import tech.icey.xjbutil.container.Pair;

import java.lang.foreign.Arena;

public final class Resource {
    public static final class Image {
        public final VkImage image;
        public final VkImageView imageView;
        public final VmaAllocation allocation;

        private Image(VkImage image, VkImageView imageView, VmaAllocation allocation) {
            this.image = image;
            this.imageView = imageView;
            this.allocation = allocation;
        }

        public static Image create(
                VulkanRenderEngineContext cx,
                int width,
                int height,
                int mipLevels,
                @enumtype(VkSampleCountFlags.class) int sampleCountFlags,
                @enumtype(VkFormat.class) int format,
                @enumtype(VkImageTiling.class) int tiling,
                @enumtype(VkImageUsageFlags.class) int usage,
                @enumtype(VkImageAspectFlags.class) int aspect
        ) throws RenderException {
            Pair<VkImage, VmaAllocation> pair = createImage(
                    cx,
                    width,
                    height,
                    mipLevels,
                    sampleCountFlags,
                    format,
                    tiling,
                    usage
            );
            VkImage image = pair.first();
            VmaAllocation allocation = pair.second();
            VkImageView imageView = createImageView(cx, image, format, aspect, mipLevels);

            return new Image(image, imageView, allocation);
        }

        public void dispose(VulkanRenderEngineContext cx) {
            cx.dCmd.vkDestroyImageView(cx.device, imageView, null);
            cx.vma.vmaDestroyImage(cx.vmaAllocator, image, allocation);
        }
    }

    public static final class SwapchainImage {
        public final VkImage image;
        public final VkImageView imageView;

        private SwapchainImage(VkImage image, VkImageView imageView) {
            this.image = image;
            this.imageView = imageView;
        }

        public static SwapchainImage create(
                VulkanRenderEngineContext cx,
                VkImage image,
                @enumtype(VkFormat.class) int format
        ) throws RenderException {
            VkImageView imageView = createImageView(cx, image, format, VkImageAspectFlags.VK_IMAGE_ASPECT_COLOR_BIT, 1);
            return new SwapchainImage(image, imageView);
        }

        public void dispose(VulkanRenderEngineContext cx) {
            cx.dCmd.vkDestroyImageView(cx.device, imageView, null);
        }
    }

    private static Pair<VkImage, VmaAllocation> createImage(
            VulkanRenderEngineContext cx,
            int width,
            int height,
            int mipLevels,
            @enumtype(VkSampleCountFlags.class) int sampleCountFlags,
            @enumtype(VkFormat.class) int format,
            @enumtype(VkImageTiling.class) int tiling,
            @enumtype(VkImageUsageFlags.class) int usage
    ) throws RenderException {
        try (var arena = Arena.ofConfined()) {
            VkImageCreateInfo createInfo = VkImageCreateInfo.allocate(arena);
            createInfo.imageType(VkImageType.VK_IMAGE_TYPE_2D);
            createInfo.extent().width(width);
            createInfo.extent().height(height);
            createInfo.extent().depth(1);
            createInfo.mipLevels(mipLevels);
            createInfo.arrayLayers(1);
            createInfo.format(format);
            createInfo.tiling(tiling);
            createInfo.initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED);
            createInfo.usage(usage);
            createInfo.samples(sampleCountFlags);
            createInfo.sharingMode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE);

            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.allocate(arena);
            allocationCreateInfo.usage(VmaMemoryUsage.VMA_MEMORY_USAGE_GPU_ONLY);

            VkImage.Buffer pImage = VkImage.Buffer.allocate(arena);
            VmaAllocation.Buffer pAllocation = VmaAllocation.Buffer.allocate(arena);
            @enumtype(VkResult.class) int result = cx.vma.vmaCreateImage(
                    cx.vmaAllocator,
                    createInfo,
                    allocationCreateInfo,
                    pImage,
                    pAllocation,
                    null
            );
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法创建 Vulkan 图像, 错误代码: " + result);
            }

            return new Pair<>(pImage.read(), pAllocation.read());
        }
    }

    private static VkImageView createImageView(
            VulkanRenderEngineContext cx,
            VkImage image,
            @enumtype(VkFormat.class) int format,
            @enumtype(VkImageAspectFlags.class) int aspect,
            int mipLevels
    ) throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.allocate(arena);
            createInfo.image(image);
            createInfo.viewType(VkImageViewType.VK_IMAGE_VIEW_TYPE_2D);
            createInfo.format(format);

            VkImageSubresourceRange subresourceRange = createInfo.subresourceRange();
            subresourceRange.aspectMask(aspect);
            subresourceRange.baseMipLevel(0);
            subresourceRange.levelCount(mipLevels);
            subresourceRange.baseArrayLayer(0);
            subresourceRange.layerCount(1);

            VkImageView.Buffer pImageView = VkImageView.Buffer.allocate(arena);
            @enumtype(VkResult.class) int result = cx.dCmd.vkCreateImageView(cx.device, createInfo, null, pImageView);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法创建 Vulkan 图像视图, 错误代码: " + result);
            }
            return pImageView.read();
        }
    }
}
