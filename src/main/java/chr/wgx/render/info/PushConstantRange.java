package chr.wgx.render.info;

import chr.wgx.render.common.CGType;
import chr.wgx.render.common.ShaderStage;

public final class PushConstantRange {
    public final String name;
    public final ShaderStage shaderStage;
    public final CGType type;
    public final int offset;

    public PushConstantRange(String name, ShaderStage shaderStage, CGType type, int offset) {
        this.name = name;
        this.shaderStage = shaderStage;
        this.type = type;
        this.offset = offset;
    }
}
