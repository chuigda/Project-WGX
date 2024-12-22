package chr.wgx.render.info;

import chr.wgx.render.common.CGType;
import chr.wgx.render.common.ShaderStage;

public final class PushConstantRange {
    public final ShaderStage shaderStage;
    public final int offset;
    public final int size;

    public PushConstantRange(ShaderStage shaderStage, int offset, int size) {
        this.shaderStage = shaderStage;
        this.offset = offset;
        this.size = size;
    }

    public PushConstantRange(ShaderStage shaderStage, int offset, CGType type) {
        this(shaderStage, offset, type.byteSize);
    }
}
