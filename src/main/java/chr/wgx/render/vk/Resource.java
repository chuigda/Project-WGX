package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.bitmask.VkImageAspectFlags;
import tech.icey.vk4j.datatype.VkImageSubresourceRange;
import tech.icey.vk4j.datatype.VkImageViewCreateInfo;
import tech.icey.vk4j.enumtype.VkFormat;
import tech.icey.vk4j.enumtype.VkImageViewType;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vk4j.handle.VkImage;
import tech.icey.vk4j.handle.VkImageView;
import tech.icey.vma.handle.VmaAllocation;

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
