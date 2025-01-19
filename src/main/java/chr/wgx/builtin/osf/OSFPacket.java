package chr.wgx.builtin.osf;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;

/// OSF UDP protocol packet, documented here:
/// - [OSF facetracker.py](https://github.com/emilianavt/OpenSeeFace/blob/40119c17971c019b892b047b457c8182190acb8c/facetracker.py)
/// - [OSF demo OpenSee.cs](https://github.com/emilianavt/OpenSeeFace/blob/40119c17971c019b892b047b457c8182190acb8c/Unity/OpenSee.cs#L19)
/// - [Project-WG](https://github.com/chuigda/Project-WG/blob/b6b2901e4a0e1bb2e9db1fcd838fdea00f36f3eb/src/ui_next/track/OSFTrackControl.cc#L79)
public final class OSFPacket {
    public final double now;

    public final int id;

    public final float width;
    public final float height;

    public final float eyeBlinkRight;
    public final float eyeBlinkLeft;

    public byte success;

    public final float pnpError;
    public final float[] quaternion;
    public final float[] euler;
    public final float[] translation;
    public final float[] lms_confidence;
    public final Vector2f[] lms;
    public final Vector3f[] pnpPoints;

    public final float eyeLeft;
    public final float eyeRight;

    public final float eyeSteepnessLeft;
    public final float eyeUpDownLeft;
    public final float eyeQuirkLeft;

    public final float eyeSteepnessRight;
    public final float eyeUpDownRight;
    public final float eyeQuirkRight;

    public final float mouthCornerUpdownLeft;
    public final float mouthCornerInOutLeft;
    public final float mouthCornerUpdownRight;
    public final float mouthCornerInOutRight;

    public final float mouthOpen;
    public final float mouthWide;

    public OSFPacket(byte[] rawPacket) {
        assert rawPacket.length >= 1785;

        MemorySegment s = MemorySegment.ofArray(rawPacket);
        long offset = 0;

        this.now = s.get(JAVA_DOUBLE_UNALIGNED, offset);
        offset += Double.BYTES;

        this.id = s.get(JAVA_INT_UNALIGNED, offset);
        offset += Integer.BYTES;

        this.width = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;
        this.height = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;

        this.eyeBlinkRight = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;
        this.eyeBlinkLeft = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;

        this.success = s.get(JAVA_BYTE, offset);
        offset += Byte.BYTES;

        this.pnpError = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;

        this.quaternion = new float[4];
        for (int i = 0; i < 4; i++) {
            this.quaternion[i] = s.get(JAVA_FLOAT_UNALIGNED, offset);
            offset += Float.BYTES;
        }

        this.euler = new float[3];
        for (int i = 0; i < 3; i++) {
            this.euler[i] = s.get(JAVA_FLOAT_UNALIGNED, offset);
            offset += Float.BYTES;
        }

        this.translation = new float[3];
        for (int i = 0; i < 3; i++) {
            this.translation[i] = s.get(JAVA_FLOAT_UNALIGNED, offset);
            offset += Float.BYTES;
        }

        this.lms_confidence = new float[68];
        for (int i = 0; i < 68; i++) {
            this.lms_confidence[i] = s.get(JAVA_FLOAT_UNALIGNED, offset);
            offset += Float.BYTES;
        }

        this.lms = new Vector2f[68];
        for (int i = 0; i < 68; i++) {
            this.lms[i] = new Vector2f(
                    s.get(JAVA_FLOAT_UNALIGNED, offset),
                    s.get(JAVA_FLOAT_UNALIGNED, offset + Float.BYTES)
            );
            offset += 2 * Float.BYTES;
        }

        this.pnpPoints = new Vector3f[68];
        for (int i = 0; i < 68; i++) {
            this.pnpPoints[i] = new Vector3f(
                    s.get(JAVA_FLOAT_UNALIGNED, offset),
                    s.get(JAVA_FLOAT_UNALIGNED, offset + Float.BYTES),
                    s.get(JAVA_FLOAT_UNALIGNED, offset + 2 * Float.BYTES)
            );
            offset += 3 * Float.BYTES;
        }

        this.eyeLeft = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;
        this.eyeRight = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;

        this.eyeSteepnessLeft = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;
        this.eyeUpDownLeft = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;
        this.eyeQuirkLeft = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;

        this.eyeSteepnessRight = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;
        this.eyeUpDownRight = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;
        this.eyeQuirkRight = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;

        this.mouthCornerUpdownLeft = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;
        this.mouthCornerInOutLeft = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;
        this.mouthCornerUpdownRight = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;
        this.mouthCornerInOutRight = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;

        this.mouthOpen = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;
        this.mouthWide = s.get(JAVA_FLOAT_UNALIGNED, offset);
        offset += Float.BYTES;

        assert offset == 1785L;
    }
}
