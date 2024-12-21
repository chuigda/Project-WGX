package chr.wgx.render.info;

import chr.wgx.render.common.CGType;

@SuppressWarnings("ClassCanBeRecord")
public final class FieldInfoInput {
    public final String name;
    public final CGType type;

    public FieldInfoInput(String name, CGType type) {
        this.name = name;
        this.type = type;
    }
}
