package tech.icey.wgx;

import javax.swing.WindowConstants;

import tech.icey.wgx.ui.SimpleEditor;

public class Drill {
    public static void main(String[] args) {
        SimpleEditor editor = new SimpleEditor();
        editor.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        editor.setVisible(true);
    }
}
