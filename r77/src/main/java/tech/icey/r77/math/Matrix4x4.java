package tech.icey.r77.math;

import tech.icey.r77.asset.IntoBytes;

import java.nio.ByteBuffer;

public final class Matrix4x4 implements IntoBytes {
    public Matrix4x4(
            float m11, float m12, float m13, float m14,
            float m21, float m22, float m23, float m24,
            float m31, float m32, float m33, float m34,
            float m41, float m42, float m43, float m44
    ) {
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m14 = m14;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m24 = m24;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        this.m34 = m34;
        this.m41 = m41;
        this.m42 = m42;
        this.m43 = m43;
        this.m44 = m44;
    }

    public final float m11, m12, m13, m14;
    public final float m21, m22, m23, m24;
    public final float m31, m32, m33, m34;
    public final float m41, m42, m43, m44;

    @Override
    public int bytesSize() {
        return Float.BYTES * 16;
    }

    @Override
    public void writeToByteBuffer(ByteBuffer buffer) {
        buffer.putFloat(m11);
        buffer.putFloat(m12);
        buffer.putFloat(m13);
        buffer.putFloat(m14);
        buffer.putFloat(m21);
        buffer.putFloat(m22);
        buffer.putFloat(m23);
        buffer.putFloat(m24);
        buffer.putFloat(m31);
        buffer.putFloat(m32);
        buffer.putFloat(m33);
        buffer.putFloat(m34);
        buffer.putFloat(m41);
        buffer.putFloat(m42);
        buffer.putFloat(m43);
        buffer.putFloat(m44);
    }

    public static final Matrix4x4 IDENTITY = new Matrix4x4(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    );

    public static Matrix4x4 mul(Matrix4x4 lhs, Matrix4x4 rhs) {
        return new Matrix4x4(
                lhs.m11 * rhs.m11 + lhs.m12 * rhs.m21 + lhs.m13 * rhs.m31 + lhs.m14 * rhs.m41,
                lhs.m11 * rhs.m12 + lhs.m12 * rhs.m22 + lhs.m13 * rhs.m32 + lhs.m14 * rhs.m42,
                lhs.m11 * rhs.m13 + lhs.m12 * rhs.m23 + lhs.m13 * rhs.m33 + lhs.m14 * rhs.m43,
                lhs.m11 * rhs.m14 + lhs.m12 * rhs.m24 + lhs.m13 * rhs.m34 + lhs.m14 * rhs.m44,
                lhs.m21 * rhs.m11 + lhs.m22 * rhs.m21 + lhs.m23 * rhs.m31 + lhs.m24 * rhs.m41,
                lhs.m21 * rhs.m12 + lhs.m22 * rhs.m22 + lhs.m23 * rhs.m32 + lhs.m24 * rhs.m42,
                lhs.m21 * rhs.m13 + lhs.m22 * rhs.m23 + lhs.m23 * rhs.m33 + lhs.m24 * rhs.m43,
                lhs.m21 * rhs.m14 + lhs.m22 * rhs.m24 + lhs.m23 * rhs.m34 + lhs.m24 * rhs.m44,
                lhs.m31 * rhs.m11 + lhs.m32 * rhs.m21 + lhs.m33 * rhs.m31 + lhs.m34 * rhs.m41,
                lhs.m31 * rhs.m12 + lhs.m32 * rhs.m22 + lhs.m33 * rhs.m32 + lhs.m34 * rhs.m42,
                lhs.m31 * rhs.m13 + lhs.m32 * rhs.m23 + lhs.m33 * rhs.m33 + lhs.m34 * rhs.m43,
                lhs.m31 * rhs.m14 + lhs.m32 * rhs.m24 + lhs.m33 * rhs.m34 + lhs.m34 * rhs.m44,
                lhs.m41 * rhs.m11 + lhs.m42 * rhs.m21 + lhs.m43 * rhs.m31 + lhs.m44 * rhs.m41,
                lhs.m41 * rhs.m12 + lhs.m42 * rhs.m22 + lhs.m43 * rhs.m32 + lhs.m44 * rhs.m42,
                lhs.m41 * rhs.m13 + lhs.m42 * rhs.m23 + lhs.m43 * rhs.m33 + lhs.m44 * rhs.m43,
                lhs.m41 * rhs.m14 + lhs.m42 * rhs.m24 + lhs.m43 * rhs.m34 + lhs.m44 * rhs.m44
        );
    }

    public static Matrix4x4 rmul(Matrix4x4 lhs, Vector4 rhs) {
        // TODO(+) This seems not correct, double check.
        return new Matrix4x4(
                lhs.m11 * rhs.x + lhs.m12 * rhs.y + lhs.m13 * rhs.z + lhs.m14 * rhs.t,
                lhs.m21 * rhs.x + lhs.m22 * rhs.y + lhs.m23 * rhs.z + lhs.m24 * rhs.t,
                lhs.m31 * rhs.x + lhs.m32 * rhs.y + lhs.m33 * rhs.z + lhs.m34 * rhs.t,
                lhs.m41 * rhs.x + lhs.m42 * rhs.y + lhs.m43 * rhs.z + lhs.m44 * rhs.t,
                lhs.m11 * rhs.x + lhs.m12 * rhs.y + lhs.m13 * rhs.z + lhs.m14 * rhs.t,
                lhs.m21 * rhs.x + lhs.m22 * rhs.y + lhs.m23 * rhs.z + lhs.m24 * rhs.t,
                lhs.m31 * rhs.x + lhs.m32 * rhs.y + lhs.m33 * rhs.z + lhs.m34 * rhs.t,
                lhs.m41 * rhs.x + lhs.m42 * rhs.y + lhs.m43 * rhs.z + lhs.m44 * rhs.t,
                lhs.m11 * rhs.x + lhs.m12 * rhs.y + lhs.m13 * rhs.z + lhs.m14 * rhs.t,
                lhs.m21 * rhs.x + lhs.m22 * rhs.y + lhs.m23 * rhs.z + lhs.m24 * rhs.t,
                lhs.m31 * rhs.x + lhs.m32 * rhs.y + lhs.m33 * rhs.z + lhs.m34 * rhs.t,
                lhs.m41 * rhs.x + lhs.m42 * rhs.y + lhs.m43 * rhs.z + lhs.m44 * rhs.t,
                lhs.m11 * rhs.x + lhs.m12 * rhs.y + lhs.m13 * rhs.z + lhs.m14 * rhs.t,
                lhs.m21 * rhs.x + lhs.m22 * rhs.y + lhs.m23 * rhs.z + lhs.m24 * rhs.t,
                lhs.m31 * rhs.x + lhs.m32 * rhs.y + lhs.m33 * rhs.z + lhs.m34 * rhs.t,
                lhs.m41 * rhs.x + lhs.m42 * rhs.y + lhs.m43 * rhs.z + lhs.m44 * rhs.t
        );
    }
}
