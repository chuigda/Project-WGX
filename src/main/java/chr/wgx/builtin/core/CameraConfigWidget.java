package chr.wgx.builtin.core;

import chr.wgx.reactor.IWidget;

import javax.swing.*;

public final class CameraConfigWidget implements IWidget {
    static class ContentPanel extends JPanel {
    }

    private final ContentPanel contentPanel = new ContentPanel();

    @Override
    public String displayName() {
        return "";
    }

    @Override
    public JPanel getContentPanel() {
        return contentPanel;
    }

    @Override
    public void onDock() {
    }

    @Override
    public void onUndock() {
    }
}
