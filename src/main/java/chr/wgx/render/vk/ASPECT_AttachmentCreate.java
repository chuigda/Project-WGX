package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.Texture;
import chr.wgx.render.info.AttachmentCreateInfo;
import chr.wgx.render.vk.data.CombinedImageSampler;
import chr.wgx.render.vk.data.VulkanImageAttachment;
import club.doki7.vulkan.bitmask.VkImageAspectFlags;
import club.doki7.vulkan.bitmask.VkImageUsageFlags;
import club.doki7.vulkan.bitmask.VkSampleCountFlags;
import club.doki7.vulkan.enumtype.VkFormat;
import club.doki7.vulkan.enumtype.VkImageTiling;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.container.Ref;

public final class ASPECT_AttachmentCreate {
    public ASPECT_AttachmentCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public Pair<Attachment, Texture> createColorAttachmentImpl(AttachmentCreateInfo info) throws RenderException {
        Resource.Image image = createColorAttachmentImage(info);
        Resource.Sampler sampler = Resource.Sampler.create(engine.cx, 0);

        Ref<Resource.Image> imageRef = new Ref<>(image);
        VulkanImageAttachment attachment = new VulkanImageAttachment(info, imageRef);
        CombinedImageSampler texture = new CombinedImageSampler(imageRef, sampler);

        engine.colorAttachments.add(attachment);
        engine.textures.add(texture);

        return new Pair<>(attachment, texture);
    }

    public Attachment createDepthAttachmentImpl(AttachmentCreateInfo info) throws RenderException {
        Resource.Image image = createDepthAttachmentImage(info);
        Ref<Resource.Image> imageRef = new Ref<>(image);

        VulkanImageAttachment attachment = new VulkanImageAttachment(info, imageRef);
        engine.depthAttachments.add(attachment);
        return attachment;
    }

    public Resource.Image createColorAttachmentImage(AttachmentCreateInfo info) throws RenderException {
        int actualWidth = info.width;
        int actualHeight = info.height;
        if (actualWidth == -1) {
            actualWidth = engine.swapchain.swapExtent.width();
        }
        if (actualHeight == -1) {
            actualHeight = engine.swapchain.swapExtent.height();
        }

        return Resource.Image.create(
                engine.cx,
                actualWidth,
                actualHeight,
                1,
                VkSampleCountFlags._1,
                VkFormat.B8G8R8A8_SRGB,
                VkImageTiling.OPTIMAL,
                VkImageUsageFlags.COLOR_ATTACHMENT | VkImageUsageFlags.SAMPLED,
                VkImageAspectFlags.COLOR
        );
    }

    public Resource.Image createDepthAttachmentImage(AttachmentCreateInfo info) throws RenderException {
        int actualWidth = info.width;
        int actualHeight = info.height;
        if (actualWidth == -1) {
            actualWidth = engine.swapchain.swapExtent.width();
        }
        if (actualHeight == -1) {
            actualHeight = engine.swapchain.swapExtent.height();
        }

        return Resource.Image.create(
                engine.cx,
                actualWidth,
                actualHeight,
                1,
                VkSampleCountFlags._1,
                VkFormat.D32_SFLOAT,
                VkImageTiling.OPTIMAL,
                VkImageUsageFlags.DEPTH_STENCIL_ATTACHMENT,
                VkImageAspectFlags.DEPTH
        );
    }

    private final VulkanRenderEngine engine;
}
