package tech.icey.util;

public class Unreachable extends RuntimeException {
    public Unreachable(String reason) {
        super(reason);
    }

    public static void unreachable() {
        throw new Unreachable("Unreachable code executed");
    }

    public static void unreachable(String reason) {
        throw new Unreachable(reason);
    }
}
