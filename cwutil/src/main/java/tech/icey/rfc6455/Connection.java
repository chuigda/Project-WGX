package tech.icey.rfc6455;

import tech.icey.cwutil.Either;
import tech.icey.cwutil.NotNull;
import tech.icey.cwutil.Nullable;
import tech.icey.cwutil.Tuple3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class Connection implements AutoCloseable {
    private final String uri;
    private final Socket socket;
    private final InputStream rx;
    private final OutputStream tx;
    private final boolean isClient;

    Connection(@NotNull String uri,
               @NotNull Socket socket,
               @NotNull InputStream rx,
               @NotNull OutputStream tx,
               boolean isClient) {
        this.uri = uri;
        this.socket = socket;
        this.rx = rx;
        this.tx = tx;
        this.isClient = isClient;
    }

    private void impWrite(OpCode opCode, @Nullable byte[] bytes) throws IOException {
        boolean hasMask = isClient;
        int maskBit = hasMask ? 0x80 : 0x00;
        byte controlByte = (byte)(0x80 | opCode.getCode());

        if (bytes != null && bytes.length != 0 && hasMask) {
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

        int payloadLength = bytes == null ? 0 : bytes.length;

        synchronized (tx) {
            if (payloadLength <= 125) {
                    tx.write(new byte[]{controlByte, (byte) (payloadLength | maskBit)});
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
                        0, 0, 0, 0,
                        (byte)((payloadLength >> 24) & 0xFF),
                        (byte)((payloadLength >> 16) & 0xFF),
                        (byte)((payloadLength >> 8) & 0xFF),
                        (byte)(payloadLength & 0xFF)
                });
            }

            if (bytes == null || bytes.length == 0) {
                return;
            }

            tx.write(bytes);
        }
    }

    private @NotNull Tuple3<OpCode, byte[], Boolean> readFragment() throws IOException {
        byte[] header = new byte[2];
        if (rx.read(header) < 2) {
            throw new IOException("Unexpected EOF");
        }

        byte controlByte = header[0];
        if ((controlByte & (0x60 | 0x40 | 0x20)) != 0) {
            throw new IOException("Invalid RFC6455 frame: reserved bits set");
        }

        boolean fin = (controlByte & 0x80) != 0;
        byte opCodeByte = (byte)(controlByte & 0x0F);
        if (opCodeByte != 0x0 &&
                opCodeByte != 0x1 &&
                opCodeByte != 0x2 &&
                opCodeByte != 0x8 &&
                opCodeByte != 0x9 &&
                opCodeByte != 0xA) {
            throw new IOException("Invalid RFC6455 frame: reserved op code");
        }

        boolean hasMask = (header[1] & 0x80) != 0;
        int payloadLength = header[1] & 0x7F;

        if (payloadLength == 126) {
            byte[] lengthBytes = new byte[2];
            if (rx.read(lengthBytes) < 2) {
                throw new IOException("Unexpected EOF");
            }
            payloadLength = ((lengthBytes[0] & 0xFF) << 8) | (lengthBytes[1] & 0xFF);
        } else if (payloadLength == 127) {
            byte[] lengthBytes = new byte[8];
            if (rx.read(lengthBytes) < 8) {
                throw new IOException("Unexpected EOF");
            }

            payloadLength = payloadLengthFrom8Bytes(lengthBytes);
        }

        byte[] maskingKeyBytes = null;
        if (hasMask) {
            maskingKeyBytes = new byte[4];
            if (rx.read(maskingKeyBytes) < 4) {
                throw new IOException("Unexpected EOF");
            }
        }

        byte[] payload = new byte[payloadLength];
        if (rx.read(payload) < payloadLength) {
            throw new IOException("Unexpected EOF");
        }

        if (hasMask) {
            for (int i = 0; i < payloadLength; i++) {
                payload[i] ^= maskingKeyBytes[i % 4];
            }
        }

        return new Tuple3<>(OpCode.values()[opCodeByte], payload, fin);
    }

    private static int payloadLengthFrom8Bytes(byte[] lengthBytes) throws IOException {
        long payloadLengthLong = ((long) (lengthBytes[0] & 0xFF) << 56) |
                ((long) (lengthBytes[1] & 0xFF) << 48) |
                ((long) (lengthBytes[2] & 0xFF) << 40) |
                ((long) (lengthBytes[3] & 0xFF) << 32) |
                ((long) (lengthBytes[4] & 0xFF) << 24) |
                ((lengthBytes[5] & 0xFF) << 16) |
                ((lengthBytes[6] & 0xFF) << 8) |
                (lengthBytes[7] & 0xFF);

        if (payloadLengthLong > Integer.MAX_VALUE) {
            throw new IOException("Payload length too large");
        }
        return (int)payloadLengthLong;
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

    public @NotNull String uri() {
        return uri;
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

    public @Nullable Either<byte[], String> read() throws IOException {
        while (true) {
            var fragmentResult = readFragment();
            OpCode opCode = fragmentResult.first;
            byte[] payload = fragmentResult.second;
            boolean fin = fragmentResult.third;

            ByteBuffer buffer = payload != null ? ByteBuffer.wrap(payload) : null;
            switch (opCode) {
                case PING -> impWrite(OpCode.PONG, payload);
                case PONG -> {}
                case CLOSE -> {
                    try {
                        impWrite(OpCode.CLOSE, null);
                    } catch (IOException ignored) {
                        // ignore any exception when we're already going to close
                    }
                    return null;
                }
                case TEXT, BINARY -> {
                    while (!fin) {
                        fragmentResult = readFragment();
                        OpCode contOpCode = fragmentResult.first;
                        payload = fragmentResult.second;
                        fin = fragmentResult.third;

                        if (contOpCode != OpCode.CONTINUATION) {
                            throw new IOException("Invalid RFC6455 frame: continuation expected");
                        }

                        if (buffer == null) {
                            buffer = ByteBuffer.wrap(payload);
                        } else {
                            buffer.put(payload);
                        }
                    }

                    if (buffer == null) {
                        return null;
                    }

                    if (opCode == OpCode.TEXT) {
                        return Either.right(StandardCharsets.UTF_8.decode(buffer).toString());
                    } else {
                        byte[] bytes = new byte[buffer.position()];
                        buffer.flip();
                        buffer.get(bytes);
                        return Either.left(bytes);
                    }
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        try {
            impWrite(OpCode.CLOSE, new byte[0]);
        } catch (IOException ignored) {
            // ignore any exception when we're already going to close
        }
        socket.close();
    }
}
