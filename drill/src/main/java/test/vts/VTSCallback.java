package test.vts;

public interface VTSCallback {
    void onData(VTSRespData data);

    default void onError(Exception e) {
        e.printStackTrace();
    }
}
