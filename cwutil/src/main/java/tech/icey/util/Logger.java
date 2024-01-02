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

        public static Optional<Level> fromString(String str) {
            return switch (str.toLowerCase()) {
                case "debug" -> Optional.some(DEBUG);
                case "info" -> Optional.some(INFO);
                case "warn" -> Optional.some(WARN);
                case "error" -> Optional.some(ERROR);
                case "fatal" -> Optional.some(FATAL);
                default -> Optional.none();
            };
        }

        private final int value;
    }

    private static volatile Level level = Level.WARN;

    private static boolean logStderrAlways = false;

    private static final List<Function3<Date, Level, String, Void>> hooks = new ArrayList<>();

    public static synchronized Level getLevel() {
        return level;
    }

    public static synchronized void setLevel(Level level) {
        Logger.level = level;
    }

    public static synchronized void setLogStderrAlways(boolean logStderrAlways) {
        Logger.logStderrAlways = logStderrAlways;
    }

    public static synchronized void installHook(Function3<Date, Level, String, Void> hook) {
        hooks.add(hook);
    }

    public static synchronized void log_s(Level level, String message) {
        Date now = new Date();
        for (Function3<Date, Level, String, Void> hook : hooks) {
            hook.apply(now, level, message);
        }

        if (hooks.isEmpty() || logStderrAlways) {
            String time = String.format("%tFT%<tT.%<tL%<tz", now);
            System.err.printf("%s %s %s\n", time, level.name(), message);
        }
    }

    public void log(Level level, String message, Object... args) {
        if (level.value < Logger.level.value) {
            return;
        }

        log_s(level, className + ": " + String.format(message, args));
    }

    public static void log_static(Level level, String className, String message, Object... args) {
        if (level.value < Logger.level.value) {
            return;
        }

        log_s(level, className + ": " + String.format(message, args));
    }
    
    public void debug(String message, Object... args) {
    	log(Level.DEBUG, message, args);
    }
    
    public void info(String message, Object... args) {
    	log(Level.INFO, message, args);
    }
}
