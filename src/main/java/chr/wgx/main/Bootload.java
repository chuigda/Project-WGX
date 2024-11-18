package chr.wgx.main;

import chr.wgx.util.SharedObjectLoader;
import tech.icey.vk4j.VulkanLoader;

import java.util.logging.Logger;

public final class Bootload {
    public static void loadNativeLibraries() {
        logger.fine("正使用 tech.icey.vk4j.VulkanLoader 加载 vulkan 本地库");
        VulkanLoader.loadVulkanLibrary();

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            logger.fine("从资源 /lib/glfw3.dll 中加载 glfw");
            SharedObjectLoader.loadFromResources("/resources/lib/glfw3.dll", ".dll");
            logger.fine("从资源 /lib/vma.dll 中加载 vma");
            SharedObjectLoader.loadFromResources("/resources/lib/vma.dll", ".dll");
        }
        else {
            logger.fine("从系统环境中加载 glfw");
            System.loadLibrary("glfw");
            logger.fine("从资源 /lib/libvma.so 中加载 vma");
            SharedObjectLoader.loadFromResources("/resources/lib/libvma.so", ".so");
        }
    }

    private static final Logger logger = Logger.getLogger(Bootload.class.getName());
}
