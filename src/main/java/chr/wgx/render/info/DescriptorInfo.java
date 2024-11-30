package chr.wgx.render.info;

import chr.wgx.render.common.ShaderStage;

public abstract sealed class DescriptorInfo {
    private final ShaderStage stage;

    public DescriptorInfo(ShaderStage stage) {
        this.stage = stage;
    }

    public ShaderStage getStage() {
        return stage;
    }

    public static final class Uniform extends DescriptorInfo {
        public Uniform(ShaderStage stage) {
            super(stage);
        }
    }

    public static final class UBO extends DescriptorInfo {
        public UBO(ShaderStage stage) {
            super(stage);
        }
    }
}
