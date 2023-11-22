package tech.icey.r77.asset;

import tech.icey.util.NotNull;

public record LayoutField(@NotNull String attrName, @NotNull Type type, int offset) {
    public enum Type {
        INT32,
        FLOAT32,
        VEC2I,
        VEC3I,
        VEC4I,
        VEC2F,
        VEC3F,
        VEC4F
    }
}
