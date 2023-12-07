package tech.icey.rfc6455;

import tech.icey.util.IOUtil;
import tech.icey.util.Optional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static tech.icey.rfc6455.Connection.RFC6455_GUID;

public final class Server implements AutoCloseable {
    public Server(int port, RFC6455Callback callback) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.callback = callback;
    }

    public Connection accept() throws IOException {
        Socket socket = serverSocket.accept();
        InputStream rx = socket.getInputStream();
        OutputStream tx = socket.getOutputStream();

        // RFC7230 3
        //   All HTTP/1.1 messages consist of a start-line followed by a sequence of octets in a format similar to the
        //   Internet Message Format [RFC5322]: zero or more header fields (collectively referred to as the "headers"
        //   or the "header section"), an empty line indicating the end of the header section, and an optional message
        //   body.
        //
        //     HTTP-message   = start-line
        //                      *( header-field CRLF )
        //                      CRLF
        //                      [ message-body ]
        //

        // RFC7230 3.1.1
        //   A request-line begins with a method token, followed by a single space (SP), the request-target, another
        //   single space (SP), the protocol version, and ends with CRLF.
        //
        //     request-line   = method SP request-target SP HTTP-version CRLF
        //
        // RFC6455 4.2.1
        //   The client's opening handshake consists of the following parts. ...
        //     1. An HTTP/1.1 or higher GET request
        String headerLine = new String(IOUtil.readUntil(rx, (byte)'\n'), StandardCharsets.UTF_8).trim();
        String[] headerLineParts = headerLine.split(" ", 3);
        // Common clients would use HTTP/1.1 for compatibility reason, but some clients may use HTTP/2.0 or higher.
        // To be simple we don't check the HTTP version.
        if (headerLineParts.length < 3 || !headerLineParts[0].equals("GET")) {
            throw new IOException("Invalid HTTP request status line: " + headerLine);
        }
        String uri = headerLineParts[1];

        // RFC7230 3.2
        //   Each header field consists of a case-insensitive field name followed by a colon (":"), optional leading
        //   whitespace, the field value, and optional trailing whitespace.
        //
        //     header-field   = field-name ":" OWS field-value OWS
        //
        HashMap<String, String> headers = new HashMap<>();
        while (true) {
            byte[] line = IOUtil.readUntil(rx, (byte)'\n');
            String lineText = new String(line, StandardCharsets.UTF_8).trim();
            if (lineText.isEmpty()) {
                break;
            }

            String[] keyValue = lineText.split(":", 2);
            if (keyValue.length != 2) {
                throw new IOException("Invalid HTTP header line: " + lineText);
            }

            headers.put(keyValue[0].trim().toUpperCase(), keyValue[1].trim());
        }

        // RFC7231 4.3.1
        //   A payload within a GET request message has no defined semantics;
        //   sending a payload body on a GET request might cause some existing
        //   implementations to reject the request.
        //
        // RFC6455 4.2.1
        //   The client's opening handshake consists of the following parts. ...
        //     3. An |Upgrade| header field containing the value "websocket", treated as an ASCII case-insensitive
        //        value.
        //     4. A |Connection| header field that includes the token "Upgrade", treated as an ASCII case-insensitive
        //        value.
        //     5. A |Sec-WebSocket-Key| header field with a base64-encoded value that, when decoded, is 16 bytes in
        //        length.
        //     6. A |Sec-WebSocket-Version| header field, with a value of 13.
        if (!headers.containsKey("UPGRADE") ||
                !headers.get("UPGRADE").equalsIgnoreCase("WEBSOCKET") ||
                !headers.containsKey("CONNECTION") ||
                !headers.get("CONNECTION").equalsIgnoreCase("UPGRADE") ||
                !headers.containsKey("SEC-WEBSOCKET-KEY") ||
                headers.get("SEC-WEBSOCKET-KEY").length() != 24 ||
                !headers.containsKey("SEC-WEBSOCKET-VERSION") ||
                !headers.get("SEC-WEBSOCKET-VERSION").equalsIgnoreCase("13")) {
            throw new IOException("Invalid HTTP request: missing WebSocket header");
        }

        // ensure the decoded key is 16 bytes in length
        byte[] key = java.util.Base64.getDecoder().decode(headers.get("SEC-WEBSOCKET-KEY"));
        if (key.length != 16) {
            throw new IOException("Invalid HTTP request: invalid WebSocket key");
        }

        // RFC6455 4.2.2
        //   When a client establishes a WebSocket connection to a server, the server MUST complete the following steps
        //   to accept the connection and send the server's opening handshake.
        //     4. Establish the following information:
        //        - /key/
        //          The |Sec-WebSocket-Key| header field in the client's handshake includes a base64-encoded value that,
        //          if decoded, is 16 bytes in length. This (encoded) value is used in the creation of the server's
        //          handshake to indicate an acceptance of the connection. It is not necessary for the server to
        //          base64-decode the |Sec-WebSocket-Key| value.
        String responseKeyText;
        try {
            responseKeyText = java.util.Base64.getEncoder().encodeToString(
                    java.security.MessageDigest.getInstance("SHA-1").digest(
                            (headers.get("SEC-WEBSOCKET-KEY") + RFC6455_GUID).getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (Exception e) {
            // principally should be unreachable
            throw new RuntimeException(e);
        }

        String response =
            "HTTP/1.1 101 Switching Protocols\r\n" +
            "Upgrade: websocket\r\n" +
            "Connection: Upgrade\r\n" +
            "Sec-WebSocket-Accept: " + responseKeyText + "\r\n" +
            "\r\n";

        tx.write(response.getBytes(StandardCharsets.UTF_8));
        return new Connection(uri, socket, rx, tx, false, Optional.some(callback));
    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
    }

    private final ServerSocket serverSocket;

    private final RFC6455Callback callback;
}
