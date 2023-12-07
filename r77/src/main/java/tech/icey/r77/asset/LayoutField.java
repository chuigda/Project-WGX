package tech.icey.r77.asset;

public record LayoutField(String attrName, Type type, int offset) {
    public enum Type {
        Float,
        Vector2,
        Vector3,
        Vector4,
        Matrix4x4
    }
}
