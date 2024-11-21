import chr.wgx.Config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class Drill {
    public static void main(String[] args) {
        var config = new Config();

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(config);
            System.out.println(json);

            var neueVkConfig = mapper.readValue(json, Config.class);
            System.out.println(neueVkConfig);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
