package chr.wgx.render.info;

import chr.wgx.render.common.CGType;
import chr.wgx.render.common.ShaderStage;

public final class PushConstantRange {
    public final int offset;
    public final int size;

    public PushConstantRange(int offset, int size) {
        this.offset = offset;
        this.size = size;
    }

    public PushConstantRange(int offset, CGType type) {
        this(offset, type.byteSize);
    }
}
