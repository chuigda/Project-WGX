package test.gui;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
            I18n.errPrintMessage("error.lookAndFeelUnavailable");
        }
        SwingUtilities.invokeLater(CtrlPane::show);
    }
}
