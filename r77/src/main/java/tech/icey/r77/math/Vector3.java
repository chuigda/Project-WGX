package tech.icey.r77.math;

import tech.icey.r77.asset.IntoBytes;

import java.nio.ByteBuffer;

public record Vector3(float x, float y, float z) implements IntoBytes {
    public Vector2 xy() { return new Vector2(x, y); }
    public Vector2 xz() { return new Vector2(x, z); }
    public Vector2 yz() { return new Vector2(y, z); }

    @Override
    public int bytesSize() {
        return Float.BYTES * 3;
    }

    @Override
    public void writeToByteBuffer(ByteBuffer buffer) {
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
    }

    public Vector3 add(Vector3 b) {
        return Vector3.add(this, b);
    }

    public boolean isPositive() {
        return Vector3.isPositive(this);
    }

    public Vector3 threshold(Vector3 thres) {
        assert thres.isPositive();
        return new Vector3(
                Math.abs(x) < thres.x ? x : Math.copySign(thres.x, x),
                Math.abs(y) < thres.y ? y : Math.copySign(thres.y, y),
                Math.abs(z) < thres.z ? z : Math.copySign(thres.z, z)
        );
    }

    public Vector3 threshold(Vector3 lower, Vector3 upper) {
        return new Vector3(
                x < lower.x ? lower.x : (Math.min(x, upper.x)),
                y < lower.y ? lower.y : (Math.min(y, upper.y)),
                z < lower.z ? lower.z : (Math.min(z, upper.z))
        );
    }

    public static final Vector3 ZERO = new Vector3(0.0f, 0.0f, 0.0f);
    public static final Vector3 UNIT = new Vector3(1.0f, 1.0f, 1.0f);

    public static Vector3 add(Vector3 a, Vector3 b) {
        return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vector3 sub(Vector3 a, Vector3 b) {
        return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static Vector3 mul(float coeff, Vector3 a) {
        return new Vector3(coeff * a.x, coeff * a.y, coeff * a.z);
    }

    public static Vector3 mul(Vector3 a, Vector3 b) {
        return new Vector3(a.x * b.x, a.y * b.y, a.z * b.z);
    }

    public static boolean isPositive(Vector3 v) {
        return v.x > 0 && v.y > 0 && v.z > 0;
    }
}
