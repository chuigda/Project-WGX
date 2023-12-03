package tech.icey.wgx;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class FontDatabase {
    public static final Font defaultMonospaceFont;

    static {
        init:
        {
            List<String> fontFamilies = Arrays.asList(GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames());

            // try SimSun on Windows anyway
            boolean hasSimSun = fontFamilies.stream()
                    .anyMatch(s -> s.contains("宋体"));
            if (hasSimSun) {
                defaultMonospaceFont = new Font("宋体", Font.PLAIN, 14);
                break init;
            }

            // finally, fallback to system default
            defaultMonospaceFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        }
    }
}
