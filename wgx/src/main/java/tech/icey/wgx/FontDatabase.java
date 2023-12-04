package tech.icey.wgx;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.StyleContext;

public class FontDatabase {
    public static final Font defaultMonospaceFont;

    static {
        init:
        {
            List<String> fontFamilies = Arrays.asList(GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames());

            boolean hasFiraCode = fontFamilies.stream()
                    .anyMatch(s -> s.contains("Fira Code"));
            if (hasFiraCode) {
                defaultMonospaceFont = new StyleContext().getFont("Fira Code", Font.PLAIN, 14);
                break init;
            }

            boolean hasNotoMono = fontFamilies.stream()
                    .anyMatch(s -> s.contains("Noto Mono"));
            if (hasNotoMono) {
                defaultMonospaceFont = new StyleContext().getFont("Noto Mono", Font.PLAIN, 14);
                break init;
            }

            boolean hasSimSun = fontFamilies.stream()
                    .anyMatch(s -> s.contains("宋体"));
            if (hasSimSun) {
                defaultMonospaceFont = new Font("宋体", Font.PLAIN, 14);
                break init;
            }

            defaultMonospaceFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        }
    }
}
