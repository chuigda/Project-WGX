package tech.icey.r77.math;

import tech.icey.r77.asset.IntoBytes;

import java.nio.ByteBuffer;

public record Vector2(float x, float y) implements IntoBytes {
    @Override
    public void writeToByteBuffer(ByteBuffer buffer) {
        buffer.putFloat(x);
        buffer.putFloat(y);
    }
}
