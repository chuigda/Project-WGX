package test.vts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tech.icey.rfc6455.Client;
import tech.icey.rfc6455.Connection;
import tech.icey.util.Optional;
import tech.icey.util.RuntimeError;

import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class VTSReceiver implements AutoCloseable {
    private static final int VTS_BROADCAST_PORT = 47779;

    private Connection wsConn;
    private Timer timerThread;

    private boolean isStarted = false;

    public void init(InetSocketAddress addr) throws Exception {
        init(addr.getHostString(), addr.getPort());
    }

    public void init(int port) throws Exception {
        init(InetAddress.getLocalHost(), port);
    }

    public void init(InetAddress host, int port) throws Exception {
        init(host.getHostAddress(), port);
    }

    public void init(String host, int port) throws Exception {
        if (isStarted)
            return;
        isStarted = true;
        wsConn = Client.connect(host, port, "/", Optional.none());
        checkState();
    }

    public String obtainAuthToken() throws IOException {
        return generateAuthToken();
    }

    public void startCommunication(String authToken, VTSCallback callback) throws IOException {
        startCommunication(authToken, callback, 16L);
    }

    public void startCommunication(String authToken, VTSCallback callback, long interval) throws IOException {
        authenticateSession(authToken);
        registerTimer(callback, interval);
    }

    public void stopCommunication() throws Exception {
        timerThread.cancel();
        if (wsConn != null)
            wsConn.close();
        wsConn = null;
        isStarted = false;
    }

    @Override
    public void close() throws Exception {
        stopCommunication();
    }

    private void checkState() throws IOException {
        wsConn.write("""
                {
                    "apiName": "VTubeStudioPublicAPI",
                    "apiVersion": "1.0",
                    "requestID": "StateRequest",
                    "messageType": "APIStateRequest"
                }""");
        VTSRespData.parse(wsConn.read()); // check response ok
    }

    private String generateAuthToken() throws IOException {
        wsConn.write("""
                {
                  "apiName": "VTubeStudioPublicAPI",
                  "apiVersion": "1.0",
                  "requestID": "StartCommunicationAuthRequest",
                  "messageType": "AuthenticationTokenRequest",
                  "data": {
                    "pluginName": "Project-WG VTS Data Collector",
                    "pluginDeveloper": "Chuigda WhiteGive"
                  }
                }""");
        VTSRespData data = VTSRespData.parse(wsConn.read());
        return data.data().get("authenticationToken").getAsString();
    }

    private void authenticateSession(String authToken) throws IOException {
        wsConn.write("""
                {
                	"apiName": "VTubeStudioPublicAPI",
                	"apiVersion": "1.0",
                	"requestID": "SomeID",
                	"messageType": "AuthenticationRequest",
                	"data": {
                		"pluginName": "Project-WG VTS Data Collector",
                		"pluginDeveloper": "Chuigda WhiteGive",
                		"authenticationToken": "%s"
                	}
                }""".formatted(authToken));
        VTSRespData data = VTSRespData.parse(wsConn.read());
        if (!data.data().get("authenticated").getAsBoolean()) {
            throw new VTSException("cannot authenticate with given token");
        }
    }

    private VTSRespData getVTSInput() throws IOException {
        wsConn.write("""
                {
                  "apiName": "VTubeStudioPublicAPI",
                  "apiVersion": "1.0",
                  "requestID": "InputParameterListRequest",
                  "messageType": "InputParameterListRequest"
                }""");
        var data = VTSRespData.parse(wsConn.read());
        if (!VTSRespData.MessageType.INPUT_PARAM_LIST.equals(data.type())) {
            throw new InvalidVTSResponseException(
                    "msg type should be " + VTSRespData.MessageType.INPUT_PARAM_LIST,
                    data);
        }
        return data;
    }

    private void registerTimer(VTSCallback callback, long interval) {
        timerThread = new Timer();
        timerThread.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    VTSRespData data = getVTSInput();
                    callback.onData(data);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        }, interval, interval);
    }

    public static InetSocketAddress discoverVTS() {
        try (DatagramSocket sock = new DatagramSocket(VTS_BROADCAST_PORT)) {
            sock.setSoTimeout(5000);
            byte[] buffer = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            sock.receive(packet);
            String msg = new String(buffer, 0, packet.getLength());
            JsonObject data = JsonParser.parseString(msg).getAsJsonObject().get("data").getAsJsonObject();
            if (!data.get("active").getAsBoolean()) {
                return null;
            }
            return new InetSocketAddress(packet.getAddress(), data.get("port").getAsInt());
        } catch (SocketException e) {
            RuntimeError.runtimeError("cannot open udp socket", e);
        } catch (Exception e) {
            RuntimeError.runtimeError("failed to read udp packet", e);
        }
        return RuntimeError.unreachable();
    }
}
