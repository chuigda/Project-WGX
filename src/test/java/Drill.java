import chr.wgx.render.vk.VkConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class Drill {
    public static void main(String[] args) {
        var vkConfig = new VkConfig();

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(vkConfig);
            System.out.println(json);

            var neueVkConfig = mapper.readValue(json, VkConfig.class);
            System.out.println(neueVkConfig);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
