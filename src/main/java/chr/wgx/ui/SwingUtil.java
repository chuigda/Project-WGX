package chr.wgx.ui;

import javax.swing.*;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public final class SwingUtil {
    public static final Font defaultMonospaceFont;

    static {
        init: {
            List<String> fontFamilies = Arrays.asList(
                    GraphicsEnvironment
                            .getLocalGraphicsEnvironment()
                            .getAvailableFontFamilyNames()
            );

            boolean hasFiraCode = fontFamilies.stream()
                    .anyMatch(s -> s.contains("Fira Code"));
            if (hasFiraCode) {
                defaultMonospaceFont = new StyleContext().getFont("Fira Code", Font.PLAIN, 12);
                break init;
            }

            boolean hasNotoMono = fontFamilies.stream()
                    .anyMatch(s -> s.contains("Noto Mono"));
            if (hasNotoMono) {
                defaultMonospaceFont = new StyleContext().getFont("Noto Mono", Font.PLAIN, 12);
                break init;
            }

            boolean hasDejavuSansMono = fontFamilies.stream()
                    .anyMatch(s -> s.contains("DejaVu Sans Mono"));
            if (hasDejavuSansMono) {
                defaultMonospaceFont = new StyleContext().getFont("DejaVu Sans Mono", Font.PLAIN, 12);
                break init;
            }

            boolean hasLucidaConsole = fontFamilies.stream()
                    .anyMatch(s -> s.contains("Lucida Console"));
            if (hasLucidaConsole) {
                defaultMonospaceFont = new StyleContext().getFont("Lucida Console", Font.PLAIN, 12);
                break init;
            }

            boolean hasConsolas = fontFamilies.stream()
                    .anyMatch(s -> s.contains("Consolas"));
            if (hasConsolas) {
                defaultMonospaceFont = new StyleContext().getFont("Consolas", Font.PLAIN, 12);
                break init;
            }

            boolean hasSimSun = fontFamilies.stream()
                    .anyMatch(s -> s.contains("宋体"));
            if (hasSimSun) {
                defaultMonospaceFont = new StyleContext().getFont("宋体", Font.PLAIN, 12);
                break init;
            }

            defaultMonospaceFont = new StyleContext().getFont(Font.MONOSPACED, Font.PLAIN, 12);
        }
    }

    public static void createTextAreaMenu(JTextArea textArea) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem cut = new JMenuItem("剪切");
        JMenuItem copy = new JMenuItem("复制");
        JMenuItem paste = new JMenuItem("粘贴");
        JMenuItem selectAll = new JMenuItem("全选");
        menu.add(cut);
        menu.add(copy);
        menu.add(paste);
        menu.add(selectAll);
        cut.addActionListener(_ -> textArea.cut());
        copy.addActionListener(_ -> textArea.copy());
        paste.addActionListener(_ -> textArea.paste());
        selectAll.addActionListener(_ -> textArea.selectAll());

        if (!textArea.isEditable()) {
            cut.setEnabled(false);
            paste.setEnabled(false);
        }

        textArea.setComponentPopupMenu(menu);
    }
}
