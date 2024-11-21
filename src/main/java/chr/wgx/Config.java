package chr.wgx;

import chr.wgx.render.gles2.GLES2Config;
import chr.wgx.render.vk.VulkanConfig;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;

public final class Config {
    public String windowTitle = "Project-WGX 绘图输出窗口";
    public int windowWidth = 640;
    public int windowHeight = 640;

    public String renderMode = "vulkan";
    public VulkanConfig vulkanConfig = new VulkanConfig();
    public GLES2Config gles2Config = new GLES2Config();

    public HashMap<String, ObjectNode> pluginConfigs = new HashMap<>();
}
