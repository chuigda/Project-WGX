package tech.icey.r77.math;

import tech.icey.r77.asset.IntoBytes;

import java.nio.ByteBuffer;

public record Vector4(float x, float y, float z, float t) implements IntoBytes {
    public Vector2 xy() { return new Vector2(x, y); }
    public Vector2 xz() { return new Vector2(x, z); }
    public Vector2 xt() { return new Vector2(x, t); }
    public Vector2 yz() { return new Vector2(y, z); }
    public Vector2 yt() { return new Vector2(y, t); }
    public Vector2 zt() { return new Vector2(z, t); }
    public Vector3 xyz() { return new Vector3(x, y, z); }
    public Vector3 xyt() { return new Vector3(x, y, t); }
    public Vector3 xzt() { return new Vector3(x, z, t); }
    public Vector3 yzt() { return new Vector3(y, z, t); }

    @Override
    public int bytesSize() {
        return Float.BYTES * 4;
    }

    public Vector4 perspectiveDivide() {
        return new Vector4(x / t, y / t, z / t, 1.0f);
    }

    @Override
    public void writeToByteBuffer(ByteBuffer buffer) {
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        buffer.putFloat(t);
    }

    public static final Vector4 ZERO = new Vector4(0.0f, 0.0f, 0.0f, 0.0f);
    public static final Vector4 UNIT = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);

    public static Vector4 add(Vector4 a, Vector4 b) {
        return new Vector4(a.x + b.x, a.y + b.y, a.z + b.z, a.t + b.t);
    }

    public static Vector4 sub(Vector4 a, Vector4 b) {
        return new Vector4(a.x - b.x, a.y - b.y, a.z - b.z, a.t - b.t);
    }

    public static Vector4 mul(float coeff, Vector4 a) {
        return new Vector4(coeff * a.x, coeff * a.y, coeff * a.z, coeff * a.t);
    }

    public static Vector4 mul(Vector4 a, Vector4 b) {
        return new Vector4(a.x * b.x, a.y * b.y, a.z * b.z, a.t * b.t);
    }
}
