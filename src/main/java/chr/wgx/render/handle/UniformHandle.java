package chr.wgx.render.handle;

public abstract sealed class UniformHandle extends AbstractHandle {
    public UniformHandle(long handle) {
        super(handle);
    }

    public static final class Uniform extends UniformHandle {
        public Uniform(long handle) {
            super(handle);
        }
    }

    public static final class UBO extends UniformHandle {
        public UBO(long handle) {
            super(handle);
        }
    }

    public static final class Sampler2D extends UniformHandle {
        public Sampler2D(long handle) {
            super(handle);
        }
    }
}
