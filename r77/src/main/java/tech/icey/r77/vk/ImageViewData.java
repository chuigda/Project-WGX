package tech.icey.r77.vk;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;

public final class ImageViewData {
    public ImageViewData() {
        this.baseArrayLayer = 0;
        this.layerCount = 1;
        this.mipLevels = 1;
        this.viewType = VK_IMAGE_VIEW_TYPE_2D;
    }

    public ImageViewData aspectMask(int aspectMask) {
        this.aspectMask = aspectMask;
        return this;
    }

    public ImageViewData baseArrayLayer(int baseArrayLayer) {
        this.baseArrayLayer = baseArrayLayer;
        return this;
    }

    public ImageViewData format(int format) {
        this.format = format;
        return this;
    }

    public ImageViewData layerCount(int layerCount) {
        this.layerCount = layerCount;
        return this;
    }

    public ImageViewData mipLevels(int mipLevels) {
        this.mipLevels = mipLevels;
        return this;
    }

    public ImageViewData viewType(int viewType) {
        this.viewType = viewType;
        return this;
    }

    public int aspectMask;
    public int baseArrayLayer;
    public int format;
    public int layerCount;
    public int mipLevels;
    public int viewType;
}
