package test.gui;

import javax.swing.*;

public abstract class WGXBaseForm extends JFrame {
    protected WGXBaseForm(String title) {
        super(title);
    }

    public abstract JPanel getBasePanel();
}
