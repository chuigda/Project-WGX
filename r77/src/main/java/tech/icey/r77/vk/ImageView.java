package tech.icey.r77.vk;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import tech.icey.util.ManualDispose;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static tech.icey.util.RuntimeError.runtimeError;

public final class ImageView implements ManualDispose {
    public ImageView(Device device, long vkImage, ImageViewData imageViewData) {
        this.device = device;
        this.imageViewData = imageViewData;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer imageViewBuf = stack.mallocLong(1);
            VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(vkImage)
                    .viewType(imageViewData.viewType)
                    .format(imageViewData.format)
                    .subresourceRange(r -> r
                            .aspectMask(imageViewData.aspectMask)
                            .baseArrayLayer(imageViewData.baseArrayLayer)
                            .layerCount(imageViewData.layerCount)
                            .baseMipLevel(0)
                            .levelCount(imageViewData.mipLevels)
                    );

            if (vkCreateImageView(device.vkDevice, imageViewCreateInfo, null, imageViewBuf) != VK_SUCCESS) {
                runtimeError("创建 ImageView 失败");
            }

            this.vkImageView = imageViewBuf.get(0);
        }
    }

    public final Device device;
    public final ImageViewData imageViewData;
    public final long vkImageView;

    @Override
    public boolean isManuallyDisposed() {
        return isDisposed;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            vkDestroyImageView(device.vkDevice, vkImageView, null);
            isDisposed = true;
        }
    }

    private volatile boolean isDisposed = false;
}
