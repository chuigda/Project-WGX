package test.vts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tech.icey.util.Either;
import tech.icey.util.Optional;
import tech.icey.util.RuntimeError;

public record VTSRespData(long timestamp, String requestId, String type, JsonObject data) {
    public static final class MessageType {
        private MessageType() {
        }

        public static final String AUTH_TOKEN = "AuthenticationTokenResponse";
        public static final String API_STATE = "APIStateResponse";
        public static final String INPUT_PARAM_LIST = "InputParameterListResponse";
    }

    public static VTSRespData parse(Optional<Either<byte[], String>> raw) {
        JsonObject resp = getResp(raw);
        JsonObject data = resp.getAsJsonObject("data");
        checkPacketData(data);
        return new VTSRespData(
                resp.get("timestamp").getAsLong(),
                resp.get("requestID").getAsString(),
                resp.get("messageType").getAsString(),
                data);
    }

    private static void checkPacketData(JsonObject data) {
        if (data.has("errorID")) {
            RuntimeError.runtimeError("VTS error: (%d) %s", data.get("errorID").getAsInt(), data.get("message").getAsString());
        }
    }

    private static JsonObject getResp(Optional<Either<byte[], String>> resp) {
        if (resp.isNone() || resp.get().isLeft() || resp.get().right() == null) {
            RuntimeError.runtimeError("unable to receive VTS message");
        }
        return JsonParser.parseString(resp.get().right()).getAsJsonObject();
    }
}
