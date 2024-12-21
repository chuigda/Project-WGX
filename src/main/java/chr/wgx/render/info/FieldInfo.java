package chr.wgx.render.info;

import chr.wgx.render.common.CGType;

public final class FieldInfo {
    public final String name;
    public final CGType type;

    public final int location;
    public final int byteOffset;

    FieldInfo(String name, CGType type, int location, int byteOffset) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.byteOffset = byteOffset;
    }
}
