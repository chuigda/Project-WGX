package tech.icey.rfc6455;

import tech.icey.cwutil.Either;
import tech.icey.cwutil.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class Connection implements AutoCloseable {
    private final Socket socket;
    private final InputStream rx;
    private final OutputStream tx;
    private final boolean isClient;

    Connection(@NotNull Socket socket,
               @NotNull InputStream rx,
               @NotNull OutputStream tx,
               boolean isClient) {
        this.socket = socket;
        this.rx = rx;
        this.tx = tx;
        this.isClient = isClient;
    }

    private void impWrite(OpCode opCode, @NotNull byte[] bytes) throws IOException {
        boolean hasMask = isClient;
        int maskBit = hasMask ? 0x80 : 0x00;
        byte controlByte = (byte)(0x80 | opCode.getCode());
        int payloadLength = bytes.length;

        if (payloadLength <= 125) {
            tx.write(new byte[] { controlByte, (byte)(payloadLength | maskBit) });
        } else if (payloadLength <= 0xFFFF) {
            tx.write(new byte[] {
                    controlByte,
                    (byte)(126 | maskBit),
                    (byte)((payloadLength >> 8) & 0xFF),
                    (byte)(payloadLength & 0xFF)
            });
        } else {
            tx.write(new byte[] {
                    controlByte,
                    (byte)(127 | maskBit),
                    // length is only 32 bits, so fill the rest with 0
                    0, 0, 0, 0,
                    (byte)((payloadLength >> 24) & 0xFF),
                    (byte)((payloadLength >> 16) & 0xFF),
                    (byte)((payloadLength >> 8) & 0xFF),
                    (byte)(payloadLength & 0xFF)
            });
        }

        if (bytes.length == 0) {
            return;
        }

        if (hasMask) {
            int maskingKey = (int)(Math.random() * 0xFFFFFFFF);
            byte[] maskingKeyBytes = new byte[] {
                    (byte)((maskingKey >> 24) & 0xFF),
                    (byte)((maskingKey >> 16) & 0xFF),
                    (byte)((maskingKey >> 8) & 0xFF),
                    (byte)(maskingKey & 0xFF)
            };
            tx.write(maskingKeyBytes);

            for (int i = 0; i < bytes.length; i++) {
                bytes[i] ^= maskingKeyBytes[i % 4];
            }
        }

        tx.write(bytes);
    }

    public enum OpCode {
        CONTINUATION(0x0),
        TEXT(0x1),
        BINARY(0x2),
        CLOSE(0x8),
        PING(0x9),
        PONG(0xA);

        private final int code;

        OpCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public void write(@NotNull byte[] bytes) throws IOException {
        byte[] copied = new byte[bytes.length];
        System.arraycopy(bytes, 0, copied, 0, bytes.length);
        impWrite(OpCode.BINARY, copied);
    }

    public void write(@NotNull String string) throws IOException {
        byte[] utf8Bytes = string.getBytes(StandardCharsets.UTF_8);
        impWrite(OpCode.TEXT, utf8Bytes);
    }

    public @NotNull Either<byte[], String> read() throws IOException {
        // TODO: implement reading logics
        return null;
    }

    @Override
    public void close() throws Exception {
        impWrite(OpCode.CLOSE, new byte[0]);
        socket.close();
    }
}
