package tech.icey.util;

public class RuntimeError {
    public static class Unreachable extends RuntimeException {
        public Unreachable(String message) {
            super(message);
        }
    }

    public static <T> T unreachable() {
        throw new Unreachable("Unreachable code executed");
    }

    public static <T> T unreachable(String reason) {
        throw new Unreachable(reason);
    }

    public static <T> T runtimeError(String message) {
        throw new RuntimeException(message);
    }

    public static <T> T runtimeError(String fmt, Object... args) {
        throw new RuntimeException(String.format(fmt, args));
    }

    public static <T> T runtimeError(String message, Throwable cause) {
        throw new RuntimeException(message, cause);
    }

    public static <T> T runtimeError(Throwable cause) {
        throw new RuntimeException(cause);
    }
}
