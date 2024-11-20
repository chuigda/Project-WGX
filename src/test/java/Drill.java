import chr.wgx.render.vk.VulkanConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class Drill {
    public static void main(String[] args) {
        var vkConfig = new VulkanConfig();

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(vkConfig);
            System.out.println(json);

            var neueVkConfig = mapper.readValue(json, VulkanConfig.class);
            System.out.println(neueVkConfig);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
