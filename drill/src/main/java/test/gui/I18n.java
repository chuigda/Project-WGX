package test.gui;

import java.io.PrintStream;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class I18n {
    private static final String RB_BASE_NAME = "testgui";

    private static ResourceBundle rb = ResourceBundle.getBundle(RB_BASE_NAME);

    public static String tr(String id) {
        return rb.getString(id);
    }

    public static void printMessage(PrintStream s, String id) {
        s.println(tr(id));
    }

    public static void printMessage(String id) {
        printMessage(System.out, id);
    }

    public static void errPrintMessage(String id) {
        printMessage(System.err, id);
    }

    public static void setLocale(Locale locale) {
        rb = ResourceBundle.getBundle(RB_BASE_NAME, locale);
    }
}
