package test.gui.utils;

import test.gui.I18n;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.concurrent.ExecutionException;

public class LinkRunner extends SwingWorker<Void, Void> {

    private final URI uri;

    public LinkRunner(URI u) {
        if (u == null) {
            throw new NullPointerException();
        }
        uri = u;
    }

    @Override
    protected Void doInBackground() throws Exception {
        Desktop desktop = java.awt.Desktop.getDesktop();
        desktop.browse(uri);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (ExecutionException | InterruptedException ee) {
            ee.printStackTrace();
            I18n.errPrintMessage("error.unableToOpenBrowser");
        }
    }
}
