package chr.wgx.render.info;

public abstract sealed class DescriptorInfo {
    public static final class Uniform extends DescriptorInfo {}

    public static final class UBO extends DescriptorInfo {}

    public static final class Sampler2D extends DescriptorInfo {}
}
