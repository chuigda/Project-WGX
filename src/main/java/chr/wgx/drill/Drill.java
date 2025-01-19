package chr.wgx.drill;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class Drill {
    public static void main(String[] args) {
        byte[] arr = new byte[16];
        MemorySegment ms = MemorySegment.ofArray(arr);

        double d = ms.get(ValueLayout.JAVA_DOUBLE_UNALIGNED, 0);
        int i = ms.get(ValueLayout.JAVA_INT_UNALIGNED, 8);
        float f = ms.get(ValueLayout.JAVA_FLOAT_UNALIGNED, 12);

        assert d == 0.0;
        assert i == 0;
        assert f == 0.0f;
    }
}
