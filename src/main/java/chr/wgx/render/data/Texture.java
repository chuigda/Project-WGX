package chr.wgx.render.data;

public abstract non-sealed class Texture extends Descriptor {
    public final boolean isAttachment;

    public Texture(boolean isAttachment) {
        this.isAttachment = isAttachment;
    }
}
