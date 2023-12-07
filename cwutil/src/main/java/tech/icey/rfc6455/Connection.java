package tech.icey.rfc6455;

import tech.icey.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static tech.icey.util.RuntimeError.unreachable;

public final class Connection implements AutoCloseable {
    // RFC6455 5.2 - Opcode
    //   %x0 denotes a continuation frame
    //   %x1 denotes a text frame
    //   %x2 denotes a binary frame
    //   %x3-7 are reserved for further non-control frames
    //   %x8 denotes a connection close
    //   %x9 denotes a ping
    //   %xA denotes a pong
    //   %xB-F are reserved for further control frames

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

        public static OpCode from(int code) {
            return switch (code) {
                case 0x0 -> CONTINUATION;
                case 0x1 -> TEXT;
                case 0x2 -> BINARY;
                case 0x8 -> CLOSE;
                case 0x9 -> PING;
                case 0xA -> PONG;
                default -> unreachable();
            };
        }
    }

    public static final String RFC6455_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

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
                    // RFC6455 7.1.2
                    //   Once an endpoint has both sent and received a Close control frame, that endpoint SHOULD _Close the
                    //   WebSocket Connection_ as defined in Section 7.1.1.
                    // RFC6455 7.1.4
                    //   If the TCP connection was closed after the WebSocket closing handshake was completed, the WebSocket
                    //   connection is said to have been closed _cleanly_.

                    try {
                        impWrite(OpCode.CLOSE, payload);
                    } catch (IOException ignored) {
                        // ignore any exception when we're already going to close
                    }
                    return null;
                }
                case TEXT, BINARY -> {
                    while (!fin) {
                        // RFC6455 5.4
                        //   - An unfragmented message consists of a single frame with the FIN bit set (Section 5.2) and an
                        //     opcode other than 0.
                        //   - A fragmented message consists of a single frame with the FIN bit clear and an opcode other
                        //     than 0, followed by zero or more frames with the FIN bit clear and the opcode set to 0,
                        //     and terminated by a single frame with the FIN bit set and an opcode of 0.

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
                    if (opCode == OpCode.TEXT) {
                        if (buffer == null) {
                            return Either.right("");
                        } else {
                            return Either.right(StandardCharsets.UTF_8.decode(buffer).toString());
                        }
                    } else {
                        if (buffer == null) {
                            return Either.left(new byte[0]);
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
    }

    @Override
    public void close() {
        try {
            // RFC6455 5.5.1
            //   The Close frame contains an opcode of 0x8.
            impWrite(OpCode.CLOSE, new byte[] {0x03, (byte)0xe8});
            socket.close();
        } catch (IOException ignored) {
            // ignore any exception when we're already going to close
        }
        this.listenerThread.interrupt();
        this.listenerThread = null;
    }

    private final String uri;
    private final Socket socket;
    private final InputStream rx;
    private final OutputStream tx;
    private final boolean isClient;
    private Thread listenerThread;

    Connection(@NotNull String uri,
               @NotNull Socket socket,
               @NotNull InputStream rx,
               @NotNull OutputStream tx,
               boolean isClient,
               @Nullable RFC6455Callback callback) {
        this.uri = uri;
        this.socket = socket;
        this.rx = rx;
        this.tx = tx;
        this.isClient = isClient;
        this.listenerThread = new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    Either<byte[], String> data = read();
                    if (data == null) {
                        // closed
                        socket.close();
                        break;
                    }
                    try {
                        if (callback != null)
                            callback.onData(data);
                    } catch (Exception e) {
                        // do not interrupt
                        callback.onError(e);
                    }
                } catch (IOException e) {
                    if (callback != null)
                        callback.onError(e);
                    else
                        RuntimeError.runtimeError(e);
                }
            }
        });
        this.listenerThread.start();
    }

    private void impWrite(OpCode opCode, @Nullable byte[] bytes) throws IOException {
        boolean hasMask = isClient;
        int maskBit = hasMask ? 0x80 : 0x00;
        byte controlByte = (byte)(0x80 | opCode.getCode());

        byte[] maskingKeyBytes = null;
        // frame-masking-key present only if frame-masked is 1
        if (hasMask) {
            int maskingKey = (int)(Math.random() * 0x7FFFFFFF);
            maskingKeyBytes = new byte[] {
                    (byte)((maskingKey >> 24) & 0xFF),
                    (byte)((maskingKey >> 16) & 0xFF),
                    (byte)((maskingKey >> 8) & 0xFF),
                    (byte)(maskingKey & 0xFF)
            };

            if (bytes != null && bytes.length > 0) {
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] ^= maskingKeyBytes[i % 4];
                }
            }
        }

        int payloadLength = bytes == null ? 0 : bytes.length;

        synchronized (tx) {
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
                        0, 0, 0, 0,
                        (byte)((payloadLength >> 24) & 0xFF),
                        (byte)((payloadLength >> 16) & 0xFF),
                        (byte)((payloadLength >> 8) & 0xFF),
                        (byte)(payloadLength & 0xFF)
                });
            }

            if (maskingKeyBytes != null) {
                tx.write(maskingKeyBytes);
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

        // RFC6455 5.2 - Opcode
        //   RSV1, RSV2, RSV3: 1 bit each
        //     MUST be 0 unless an extension is negotiated that defines meanings for non-zero values.
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

        // RFC6455 5.1
        //   In the WebSocket Protocol, data is transmitted using a sequence of frames. To avoid confusing network
        //   intermediaries (such as intercepting proxies) and for security reasons that are further discussed in
        //   Section 10.3, a client MUST mask all frames that it sends to the server (see Section 5.3 for further details).
        //   (Note that masking is done whether or not the WebSocket Protocol is running over TLS.) The server MUST close
        //   the connection upon receiving a frame that is not masked. In this case, a server MAY send a Close frame with
        //   a status code of 1002 (protocol error) as defined in Section 7.4.1. A server MUST NOT mask any frames that
        //   it sends to the client.  A client MUST close a connection if it detects a masked frame. In this case, it MAY
        //   use the status code 1002 (protocol error) as defined in Section 7.4.1.
        boolean hasMask = (header[1] & 0x80) != 0;
        if ((/*this*/ isClient && hasMask) || (!/*this*/ isClient && !hasMask)) {
            throw new IOException("Invalid RFC6455 frame: invalid mask bit");
        }

        // RFC6455 5.2
        //   Payload length: 7 bits, 7+16 bits, or 7+64 bits
        //     The length of the "Payload data", in bytes: if 0-125, that is the payload length.
        //     If 126, the following 2 bytes interpreted as a 16-bit unsigned integer are the
        //     payload length.  If 127, the following 8 bytes interpreted as a 64-bit unsigned integer
        //     (the most significant bit MUST be 0) are the payload length. Multibyte length quantities
        //     are expressed in network byte order.
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

        // RFC6455 5.2
        //   Masking-key: 0 or 4 bytes
        //     All frames sent from the client to the server are masked by a 32-bit value that is
        //     contained within the frame. This field is present if the mask bit is set to 1 and is
        //     absent if the mask bit is set to 0.
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
            // RFC6455 5.3
            //   Octet i of the transformed data ("transformed-octet-i") is the XOR of
            //   octet i of the original data ("original-octet-i") with octet at index
            //   i modulo 4 of the masking key ("masking-key-octet-j"):
            //
            //     j                   = i MOD 4
            //     transformed-octet-i = original-octet-i XOR masking-key-octet-j
            //
            for (int i = 0; i < payloadLength; i++) {
                payload[i] ^= maskingKeyBytes[i % 4];
            }
        }

        return new Tuple3<>(OpCode.from(opCodeByte), payload, fin);
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
}
