package tech.icey.wgx;

public class Drill {
    public static void main(String[] args) {
        DeviceInfoDialog w = new DeviceInfoDialog(null, null);
        w.setVisible(true);

        System.err.println("This will be displayed only after the dialog is closed");
    }
}
