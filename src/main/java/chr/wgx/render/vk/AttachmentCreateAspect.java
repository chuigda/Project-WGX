package chr.wgx.render.vk;

import chr.wgx.config.Config;
import chr.wgx.render.RenderException;
import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.Texture;
import chr.wgx.render.info.AttachmentCreateInfo;
import chr.wgx.render.vk.data.CombinedImageSampler;
import chr.wgx.render.vk.data.ImageAttachment;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.bitmask.VkImageAspectFlags;
import tech.icey.vk4j.bitmask.VkImageUsageFlags;
import tech.icey.vk4j.bitmask.VkSampleCountFlags;
import tech.icey.vk4j.enumtype.VkFormat;
import tech.icey.vk4j.enumtype.VkImageTiling;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.container.Ref;

public final class AttachmentCreateAspect {
    public AttachmentCreateAspect(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public Pair<Attachment, Texture> createColorAttachmentImpl(AttachmentCreateInfo info) throws RenderException {
        int actualWidth = info.width;
        int actualHeight = info.height;
        if (actualWidth == -1) {
            actualWidth = engine.swapchain.swapExtent.width();
        }
        if (actualHeight == -1) {
            actualHeight = engine.swapchain.swapExtent.height();
        }

        @enumtype(VkFormat.class) int format = Config.config().vulkanConfig.forceUNORM ?
                VkFormat.VK_FORMAT_R8G8B8A8_UNORM :
                VkFormat.VK_FORMAT_R8G8B8A8_SRGB;

        Resource.Image image = Resource.Image.create(
                engine.cx,
                actualWidth,
                actualHeight,
                1,
                format,
                VkSampleCountFlags.VK_SAMPLE_COUNT_1_BIT,
                VkImageTiling.VK_IMAGE_TILING_OPTIMAL,
                VkImageUsageFlags.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VkImageUsageFlags.VK_IMAGE_USAGE_SAMPLED_BIT,
                VkImageAspectFlags.VK_IMAGE_ASPECT_COLOR_BIT
        );
        Resource.Sampler sampler = Resource.Sampler.create(engine.cx, 0);

        Ref<Resource.Image> imageRef = new Ref<>(image);
        ImageAttachment attachment = new ImageAttachment(info, imageRef);
        CombinedImageSampler texture = new CombinedImageSampler(imageRef, sampler);

        engine.colorAttachments.add(attachment);
        engine.textures.add(texture);

        return new Pair<>(attachment, texture);
    }

    public Attachment createDepthAttachmentImpl(AttachmentCreateInfo info) throws RenderException {
        int actualWidth = info.width;
        int actualHeight = info.height;
        if (actualWidth == -1) {
            actualWidth = engine.swapchain.swapExtent.width();
        }
        if (actualHeight == -1) {
            actualHeight = engine.swapchain.swapExtent.height();
        }

        Resource.Image image = Resource.Image.create(
                engine.cx,
                actualWidth,
                actualHeight,
                1,
                VkFormat.VK_FORMAT_D32_SFLOAT,
                VkSampleCountFlags.VK_SAMPLE_COUNT_1_BIT,
                VkImageTiling.VK_IMAGE_TILING_OPTIMAL,
                VkImageUsageFlags.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                VkImageAspectFlags.VK_IMAGE_ASPECT_DEPTH_BIT
        );
        Ref<Resource.Image> imageRef = new Ref<>(image);

        ImageAttachment attachment = new ImageAttachment(info, imageRef);
        engine.depthAttachments.add(attachment);
        return attachment;
    }

    private final VulkanRenderEngine engine;
}
