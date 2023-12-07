package tech.icey.rfc6455;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

import tech.icey.util.IOUtil;
import static tech.icey.rfc6455.Connection.RFC6455_GUID;

public class Client {
    public static final String CLIENT_KEY = "dGhlIHNhbXBsZSBub25jZQ==";
    public static final String EXPECTED_RESP_KEY;

    static {
        try {
            EXPECTED_RESP_KEY = Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("sha1").digest(
                            (CLIENT_KEY + RFC6455_GUID).getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection connect(String host, int port, String uri) throws IOException {
        InetSocketAddress addr = new InetSocketAddress(host, port);
        Socket socket = new Socket();
        try {
            socket.connect(addr);

            InputStream rx = socket.getInputStream();
            OutputStream tx = socket.getOutputStream();

            String handshakeRequest =
                "GET " + uri + " HTTP/1.1\r\n" +
                "Host: " + host + ":" + port + "\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Key: " + CLIENT_KEY + "\r\n" +
                "Sec-WebSocket-Version: 13\r\n" +
                "\r\n";

            tx.write(handshakeRequest.getBytes(StandardCharsets.UTF_8));

            String headerLine = new String(IOUtil.readUntil(rx, (byte)'\n'), StandardCharsets.UTF_8).trim();
            String[] headerLineParts = headerLine.split(" ", 3);
            if (headerLineParts.length < 3 || !headerLineParts[1].equals("101")) {
                throw new IOException("Invalid HTTP response status line: " + headerLine);
            }

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

            if (!headers.containsKey("UPGRADE") ||
                    !headers.get("UPGRADE").equalsIgnoreCase("WEBSOCKET") ||
                    !headers.containsKey("CONNECTION") ||
                    !headers.get("CONNECTION").equalsIgnoreCase("UPGRADE") ||
                    !headers.containsKey("SEC-WEBSOCKET-ACCEPT") ||
                    !headers.get("SEC-WEBSOCKET-ACCEPT").equalsIgnoreCase(EXPECTED_RESP_KEY)) {
                throw new IOException("Invalid HTTP response: missing WebSocket header");
            }

            return new Connection(uri, socket, rx, tx, true);
        } catch (Exception e) {
            socket.close();
            throw e;
        }
    }
}
