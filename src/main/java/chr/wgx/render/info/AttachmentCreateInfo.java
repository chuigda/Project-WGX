package chr.wgx.render.info;

@SuppressWarnings("ClassCanBeRecord")
public final class AttachmentCreateInfo {
    public final int width;
    public final int height;

    /// @param width use {@code -1} to use the same width as the swapchain image
    /// @param height use {@code -1} to use the same height as the swapchain image
    public AttachmentCreateInfo(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
