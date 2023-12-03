package tech.icey.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public record Logger(String className) {
    public enum Level {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3),
        FATAL(4);

        Level(int value) { this.value = value; }

        public int getValue() {
            return value;
        }

        private final int value;
    }

    private static Level level = Level.WARN;

    private static final List<Function3<Date, Level, String, Void>> hooks = new ArrayList<>();

    public static synchronized Level getLevel() {
        return level;
    }

    public static synchronized void setLevel(Level level) {
        Logger.level = level;
    }

    public static synchronized void installHook(Function3<Date, Level, String, Void> hook) {
        hooks.add(hook);
    }

    public static synchronized void log_s(Level level, String message) {
        if (level.value < Logger.level.value) {
            return;
        }

        Date now = new Date();
        for (Function3<Date, Level, String, Void> hook : hooks) {
            hook.apply(now, level, message);
        }

        if (hooks.isEmpty()) {
            String time = String.format("%tFT%<tT.%<tL%<tz", now);
            System.err.printf("%s %s %s\n", time, level.name(), message);
        }
    }

    public void log(Level level, String message, Object... args) {
        log_s(level, className + ": " + String.format(message, args));
    }
}
