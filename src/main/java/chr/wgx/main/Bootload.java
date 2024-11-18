package chr.wgx.main;

import chr.wgx.util.SharedObjectLoader;
import tech.icey.vk4j.VulkanLoader;

import java.util.logging.Logger;

public final class Bootload {
    public static void loadNativeLibraries() {
        logger.fine("loading vulkan library using tech.icey.vk4j.VulkanLoader");
        VulkanLoader.loadVulkanLibrary();

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            logger.fine("loading resources/lib/glfw3.dll");
            SharedObjectLoader.loadFromResources("/lib/glfw3.dll", ".dll");
            logger.fine("loading resources/lib/vma.dll");
            SharedObjectLoader.loadFromResources("/lib/vma.dll", ".dll");
        }
        else {
            logger.fine("loading system library glfw");
            System.loadLibrary("glfw");
            logger.fine("loading resources/lib/libvma.so");
            SharedObjectLoader.loadFromResources("/lib/libvma.so", ".so");
        }
    }

    private static final Logger logger = Logger.getLogger(Bootload.class.getName());
}
