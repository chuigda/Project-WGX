package chr.wgx.builtin.core.data;

public final class HandStatus {
    public static final class Thumb {
    }

    public static final class Finger {
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public HandStatus clone() {
        return new HandStatus();
    }
}
