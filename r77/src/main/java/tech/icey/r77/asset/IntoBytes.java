package tech.icey.r77.asset;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public interface IntoBytes {
    default byte[] intoBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(bytesSize()).order(ByteOrder.nativeOrder());
        writeToByteBuffer(buffer);

        buffer.rewind();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    int bytesSize();

    void writeToByteBuffer(ByteBuffer buffer);
}
