package test.vts;

public class InvalidVTSResponseException extends VTSException {
    private final VTSRespData data;

    public InvalidVTSResponseException(String msg, VTSRespData data) {
        super(msg);
        this.data = data;
    }

    public VTSRespData getData() {
        return data;
    }
}
