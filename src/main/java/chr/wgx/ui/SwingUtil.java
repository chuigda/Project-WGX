package chr.wgx.ui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;

public final class SwingUtil {
    public static void createTextAreaMenu(JTextComponent textArea) {
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

    public static JPanel createGroupBox(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        return panel;
    }

    public static void removeAllActionListeners(JButton component) {
        for (ActionListener listener : component.getActionListeners()) {
            component.removeActionListener(listener);
        }
    }

    public static void addConfigItemFast(
            JPanel panel,
            int y,
            String label,
            JComponent field,
            String tooltip
    ) {
        GridBagConstraints cLeft = new GridBagConstraints();
        cLeft.fill = GridBagConstraints.HORIZONTAL;
        cLeft.weightx = 1;
        cLeft.gridx = 0;
        cLeft.anchor = GridBagConstraints.LINE_START;
        GridBagConstraints cRight = new GridBagConstraints();
        cRight.fill = GridBagConstraints.HORIZONTAL;
        cRight.weightx = 1;
        cRight.gridx = 2;
        cRight.anchor = GridBagConstraints.LINE_END;

        JLabel jLabel = new JLabel(label);
        cLeft.gridy = y;
        cRight.gridy = y;
        panel.add(jLabel, cLeft);
        panel.add(field, cRight);

        if (!tooltip.isEmpty()) {
            jLabel.setToolTipText(tooltip);
            field.setToolTipText(tooltip);
        }
    }

    public static void addConfigItemFast(
            JPanel panel,
            int y,
            String label,
            JComponent field
    ) {
        addConfigItemFast(panel, y, label, field, "");
    }
}
