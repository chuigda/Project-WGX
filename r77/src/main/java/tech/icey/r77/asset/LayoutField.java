package tech.icey.r77.asset;

import tech.icey.util.NotNull;

public record LayoutField(@NotNull String attrName, @NotNull Type type, int offset) {
    public enum Type {
        Float,
        Vector2,
        Vector3,
        Vector4,
        Matrix4x4
    }
}
