package chr.wgx;

import chr.wgx.render.gles2.GLES2Config;
import chr.wgx.render.vk.VulkanConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import tech.icey.xjbutil.container.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;

public final class Config {
    public String logLevel = "info";
    public int controlWindowWidth = 1024;
    public int controlWindowHeight = 768;

    public String windowTitle = "Project-WGX 绘图输出窗口";
    public int windowWidth = 640;
    public int windowHeight = 640;

    public String renderMode = "vulkan";
    public VulkanConfig vulkanConfig = new VulkanConfig();
    public GLES2Config gles2Config = new GLES2Config();

    public HashMap<String, JsonObject> pluginConfigs = new HashMap<>();

    public String toPrettyJSON() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static final Config DEFAULT = new Config();

    public static Option<Config> GLOBAL_CONFIG = Option.none();

    public static Config config() {
        if (!(GLOBAL_CONFIG instanceof Option.Some<Config> someConfig)) {
            Config newConfig;
            try {
                String configText = Files.readString(Paths.get("config.json"));
                Gson gson = new Gson();
                newConfig = gson.fromJson(configText, Config.class);
            } catch (IOException | JsonSyntaxException e) {
                logger.info(String.format("无法读取配置文件: %s, 使用默认配置", e.getMessage()));
                newConfig = new Config();
            }
            GLOBAL_CONFIG = Option.some(newConfig);
            return newConfig;
        }

        return someConfig.value;
    }

    private static final Logger logger = Logger.getLogger(Config.class.getName());
}
