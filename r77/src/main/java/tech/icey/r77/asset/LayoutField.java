package tech.icey.r77.asset;

public final class LayoutField {
    public enum Type {
        Float,
        Vector2,
        Vector3,
        Vector4,
        Matrix4x4
    }

    public LayoutField(String attrName, Type type, int offset) {
        this.attrName = attrName;
        this.type = type;
        this.offset = offset;
    }

    public final String attrName;
    public final Type type;
    public final int offset;
}
