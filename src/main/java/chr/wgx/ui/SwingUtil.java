package chr.wgx.ui;

import javax.swing.*;

public final class SwingUtil {
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
