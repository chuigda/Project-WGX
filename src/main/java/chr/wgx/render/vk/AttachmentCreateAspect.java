package chr.wgx.render.vk;

import chr.wgx.Config;
import chr.wgx.render.RenderException;
import chr.wgx.render.handle.ColorAttachmentHandle;
import chr.wgx.render.handle.DepthAttachmentHandle;
import chr.wgx.render.handle.SamplerHandle;
import chr.wgx.render.info.AttachmentCreateInfo;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.bitmask.VkImageAspectFlags;
import tech.icey.vk4j.bitmask.VkImageUsageFlags;
import tech.icey.vk4j.bitmask.VkSampleCountFlags;
import tech.icey.vk4j.enumtype.VkFormat;
import tech.icey.vk4j.enumtype.VkImageTiling;
import tech.icey.xjbutil.container.Pair;

public final class AttachmentCreateAspect {
    public AttachmentCreateAspect(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public Pair<ColorAttachmentHandle, SamplerHandle>
    createColorAttachmentImpl(AttachmentCreateInfo info) throws RenderException {
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

        try {
            Resource.Attachment attachment = Resource.Attachment.create(engine.cx, info.width, info.height, image);

            long handle = engine.nextHandle();
            synchronized (engine.colorAttachments) {
                engine.colorAttachments.put(handle, attachment);
            }
            synchronized (engine.samplerIsAttachment) {
                engine.samplerIsAttachment.put(handle, true);
            }

            return new Pair<>(new ColorAttachmentHandle(handle), new SamplerHandle(handle));
        } catch (RenderException e) {
            image.dispose(engine.cx);
            throw e;
        }
    }

    public DepthAttachmentHandle createDepthAttachmentImpl(AttachmentCreateInfo info) throws RenderException {
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

        try {
            Resource.Attachment attachment = Resource.Attachment.create(engine.cx, info.width, info.height, image);

            long handle = engine.nextHandle();
            synchronized (engine.depthAttachments) {
                engine.depthAttachments.put(handle, attachment);
            }

            return new DepthAttachmentHandle(handle);
        } catch (RenderException e) {
            image.dispose(engine.cx);
            throw e;
        }
    }

    private final VulkanRenderEngine engine;
}
