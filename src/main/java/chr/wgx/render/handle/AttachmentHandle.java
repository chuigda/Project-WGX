package chr.wgx.render.handle;

public abstract sealed class AttachmentHandle extends AbstractHandle {
    protected AttachmentHandle(long handle) {
        super(handle);
    }

    public static final class Color extends AttachmentHandle {
        public Color(long handle) {
            super(handle);
        }
    }

    public static final class Depth extends AttachmentHandle {
        public Depth(long handle) {
            super(handle);
        }
    }
}
