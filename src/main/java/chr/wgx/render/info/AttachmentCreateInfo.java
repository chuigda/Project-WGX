package chr.wgx.render.info;

import chr.wgx.render.common.PixelFormat;

public final class AttachmentCreateInfo {
    public final PixelFormat pixelFormat;
    public final int width;
    public final int height;

    /// @param width use {@code -1} to use the same width as the swapchain image
    /// @param height use {@code -1} to use the same height as the swapchain image
    public AttachmentCreateInfo(PixelFormat pixelFormat, int width, int height) {
        this.pixelFormat = pixelFormat;
        this.width = width;
        this.height = height;
    }

    public AttachmentCreateInfo(PixelFormat pixelFormat) {
        this(pixelFormat, -1, -1);
    }
}
