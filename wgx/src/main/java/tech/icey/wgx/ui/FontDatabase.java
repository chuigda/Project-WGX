package tech.icey.wgx.ui;

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
                defaultMonospaceFont = new StyleContext().getFont("Fira Code", Font.PLAIN, 12);
                break init;
            }

            boolean hasNotoMono = fontFamilies.stream()
                    .anyMatch(s -> s.contains("Noto Mono"));
            if (hasNotoMono) {
                defaultMonospaceFont = new StyleContext().getFont("Noto Mono", Font.PLAIN, 14);
                break init;
            }

            boolean hasDejavuSansMono = fontFamilies.stream()
                    .anyMatch(s -> s.contains("DejaVu Sans Mono"));
            if (hasDejavuSansMono) {
                defaultMonospaceFont = new Font("DejaVu Sans Mono", Font.PLAIN, 14);
                break init;
            }

            boolean hasLucidaConsole = fontFamilies.stream()
                    .anyMatch(s -> s.contains("Lucida Console"));
            if (hasLucidaConsole) {
                defaultMonospaceFont = new Font("Lucida Console", Font.PLAIN, 14);
                break init;
            }

            boolean hasConsolas = fontFamilies.stream()
                    .anyMatch(s -> s.contains("Consolas"));
            if (hasConsolas) {
                defaultMonospaceFont = new Font("Consolas", Font.PLAIN, 14);
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
