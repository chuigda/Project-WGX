package chr.wgx.builtin.osf;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class OSF {
    // 8
    double now;

    // 4
    int id;

    // 2 * 4
    float width;
    float height;

    // 2 * 4
    float eyeBlinkRight;
    float eyeBlinkLeft;

    // 1
    byte success;

    // 4
    float pnpError;

    // 4 * 4
    float[] quaternion;

    // 3 * 4
    float[] euler;

    // 3 * 4
    float[] translation;

    // 68 * 4
    float[] lms_confidence;

    // 68 * 2 * 4
    float[][] lms;

    // 70 * 3 * 4
    float[][] pnpPoints;

    // 14 * 4
    float eyeLeft;
    float eyeRight;

    float eyeSteepnessLeft;
    float eyeUpDownLeft;
    float eyeQuirkLeft;

    float eyeSteepnessRight;
    float eyeUpDownRight;
    float eyeQuirkRight;

    float mouthCornerUpdownLeft;
    float mouthCornerInOutLeft;
    float mouthCornerUpdownRight;
    float mouthCornerInOutRight;

    float mouthOpen;
    float mouthWide;

    public static int byteArrayToInt(byte[] bytes, int start) {
        int x = 0;
        for (int i = 0; i < 4; i++) {
            int b = (bytes[i] & 0xFF) << ((start + i) * 8);
            x |= b;
        }
        return x;
    }

    public static long byteArrayToLong(byte[] bytes, int start) {
        int x = 0;
        for (int i = 0; i < 8; i++) {
            int b = (bytes[i] & 0xFF) << ((start + i) * 8);
            x |= b;
        }
        return x;
    }

    public static float intBitCastToFloat(int input) {
        int[] buffer = new int[1];
        buffer[0] = input;
        var buf = MemorySegment.ofArray(buffer);
        float[] ret = buf.toArray(ValueLayout.JAVA_FLOAT);
        return ret[0];
    }

    public static double longBitCastToDouble(long input) {
        long[] buffer = new long[1];
        buffer[0] = input;
        var buf = MemorySegment.ofArray(buffer);
        double[] ret = buf.toArray(ValueLayout.JAVA_DOUBLE);
        return ret[0];
    }

    public OSF(byte[] input_stream, int start) {
        this.now = longBitCastToDouble(byteArrayToLong(input_stream, start));
        start = start + 8;
        this.id = byteArrayToInt(input_stream, start);
        start = start + 4;
        this.width = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.height = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.eyeBlinkRight = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.eyeBlinkLeft = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.success = input_stream[start];
        start = start + 1;
        this.pnpError = intBitCastToFloat(byteArrayToInt(input_stream, start));
        this.quaternion = new float[4];
        for (int i = 0; i < 4; i++) {
            this.quaternion[i] = intBitCastToFloat(byteArrayToInt(input_stream, start));
            start = start + 4;
        }
        this.euler = new float[3];
        for (int i = 0; i < 3; i++) {
            this.quaternion[i] = intBitCastToFloat(byteArrayToInt(input_stream, start));
            start = start + 4;
        }
        this.translation = new float[3];
        for (int i = 0; i < 3; i++) {
            this.translation[i] = intBitCastToFloat(byteArrayToInt(input_stream, start));
            start = start + 4;
        }
        this.lms_confidence = new float[68];
        for (int i = 0; i < 68; i++) {
            this.lms_confidence[i] = intBitCastToFloat(byteArrayToInt(input_stream, start));
            start = start + 4;
        }
        this.lms = new float[68][2];
        for (int i = 0; i < 68; i++) {
            for (int j = 0; j < 2; j++) {
                this.lms[i][j] = intBitCastToFloat(byteArrayToInt(input_stream, start));
                start = start + 4;
            }
        }
        this.pnpPoints = new float[70][3];
        for (int i = 0; i < 70; i++) {
            for (int j = 0; j < 3; j++) {
                this.pnpPoints[i][j] = intBitCastToFloat(byteArrayToInt(input_stream, start));
                start = start + 4;
            }
        }

        // 14 * 4
        this.eyeLeft = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.eyeRight = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.eyeSteepnessLeft = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.eyeUpDownLeft = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.eyeQuirkLeft = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.eyeSteepnessRight = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.eyeUpDownRight = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.eyeQuirkRight = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.mouthCornerUpdownLeft = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.mouthCornerInOutLeft = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.mouthCornerUpdownRight = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.mouthCornerInOutRight = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.mouthOpen = intBitCastToFloat(byteArrayToInt(input_stream, start));
        start = start + 4;
        this.mouthWide = intBitCastToFloat(byteArrayToInt(input_stream, start));
    }
    public static void main(String[] args) throws IOException {
        var socket = new DatagramSocket(8888);
        while(true) {
            byte[] buf = new byte[1781];
            var packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            byte[] ret = packet.getData();
            var OSF = new OSF(ret, 0);
        }
    }
}