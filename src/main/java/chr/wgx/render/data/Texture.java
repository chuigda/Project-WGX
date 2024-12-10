package chr.wgx.render.data;

public abstract class Texture {
    public final boolean isAttachment;

    public Texture(boolean isAttachment) {
        this.isAttachment = isAttachment;
    }
}
