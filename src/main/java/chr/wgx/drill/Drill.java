package chr.wgx.drill;

import chr.wgx.builtin.osf.OSFPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public final class Drill {
    public static void main(String[] args) {
        try(var socket = new DatagramSocket(11573)) {
            DatagramPacket packet = new DatagramPacket(new byte[1785], 1785);
            while (true) {
                socket.receive(packet);
                System.out.println(new String(packet.getData(), 0, packet.getLength()));

                OSFPacket decoded = new OSFPacket(packet.getData());
                System.out.println(decoded);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
