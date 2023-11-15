package tech.icey.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class IOUtil {
    public static byte[] readUntil(InputStream stream, byte b) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        while (true) {
            int read = stream.read();
            if (read == -1) {
                throw new RuntimeException("EOF");
            }
            buffer.put((byte) read);
            if (read == b) {
                break;
            }
        }

        byte[] bytes = new byte[buffer.position()];
        buffer.flip();
        buffer.get(bytes);
        return bytes;
    }
}
