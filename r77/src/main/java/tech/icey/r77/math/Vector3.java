package tech.icey.r77.math;

import tech.icey.r77.asset.IntoBytes;

import java.nio.ByteBuffer;

public record Vector3(float x, float y, float z) implements IntoBytes {
    @Override
    public void writeToByteBuffer(ByteBuffer buffer) {
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
    }
}
