package tech.icey.r77.asset;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public interface IntoBytes {
    default byte[] intoBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(0).order(ByteOrder.nativeOrder());
        writeToByteBuffer(buffer);
        return buffer.array();
    }

    void writeToByteBuffer(ByteBuffer buffer);
}
