package chr.wgx.render.handle;

public final class TextureHandle extends AbstractHandle {
    private final boolean isRenderTarget;

    public TextureHandle(long handle, boolean isRenderTarget) {
        super(handle);
        this.isRenderTarget = isRenderTarget;
    }

    public boolean isRenderTarget() {
        return isRenderTarget;
    }
}
