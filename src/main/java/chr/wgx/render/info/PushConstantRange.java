package chr.wgx.render.info;

import chr.wgx.render.common.CGType;
import chr.wgx.render.common.ShaderStage;

public final class PushConstantRange {
    public final ShaderStage shaderStage;
    public final CGType type;
    public final int offset;

    public PushConstantRange(ShaderStage shaderStage, CGType type, int offset) {
        this.shaderStage = shaderStage;
        this.type = type;
        this.offset = offset;
    }
}
