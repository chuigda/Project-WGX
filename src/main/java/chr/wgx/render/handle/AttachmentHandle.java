package chr.wgx.render.handle;

public abstract sealed class AttachmentHandle extends AbstractHandle {
    protected AttachmentHandle(long handle) {
        super(handle);
    }

    public static final class Color extends AttachmentHandle {
        private final UniformHandle.Sampler2D textureHandle;

        public Color(long handle, UniformHandle.Sampler2D textureHandle) {
            super(handle);
            this.textureHandle = textureHandle;
        }

        public UniformHandle.Sampler2D textureHandle() {
            return textureHandle;
        }
    }

    public static final class Depth extends AttachmentHandle {
        public Depth(long handle) {
            super(handle);
        }
    }
}
