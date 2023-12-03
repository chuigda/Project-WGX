package tech.icey.util;

public class RuntimeError {
    public static class Unreachable extends RuntimeException {
        public Unreachable(String message) {
            super(message);
        }
    }

    public static void unreachable() {
        throw new Unreachable("Unreachable code executed");
    }

    public static void unreachable(String reason) {
        throw new Unreachable(reason);
    }

    public static void runtimeError(String message) {
        throw new RuntimeException(message);
    }

    public static void runtimeError(String fmt, Object... args) {
        throw new RuntimeException(String.format(fmt, args));
    }
}
