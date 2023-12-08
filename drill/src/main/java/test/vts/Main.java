package test.vts;

import tech.icey.util.RuntimeError;

public class Main {
    public static void main(String[] args) {
        var vtsAddr = VTSReceiver.discoverVTS();
        if (vtsAddr == null) {
            RuntimeError.runtimeError("vts not enabled");
        }
        System.out.println(vtsAddr);
        try (var vts = new VTSReceiver()) {
            vts.init(vtsAddr);
            String token = vts.obtainAuthToken();
            vts.startCommunication(token, data -> {
                System.out.println(data);
            }, 16L);
            // vts.startCommunication(8765);
            System.out.println("started");
            Thread.sleep(5000);
            vts.stopCommunication();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
