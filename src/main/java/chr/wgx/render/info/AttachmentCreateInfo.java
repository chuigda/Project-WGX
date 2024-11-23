package chr.wgx.render.info;

public abstract sealed class AttachmentCreateInfo {
    private final int width;
    private final int height;

    /// @param width use {@code -1} to use the same width as the swapchain image
    /// @param height use {@code -1} to use the same height as the swapchain image
    protected AttachmentCreateInfo(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public final int width() {
        return width;
    }

    public final int height() {
        return height;
    }

    public static final class Color extends AttachmentCreateInfo {
        public Color(int width, int height) {
            super(width, height);
        }
    }

    public static final class Depth extends AttachmentCreateInfo {
        public Depth(int width, int height) {
            super(width, height);
        }
    }
}
