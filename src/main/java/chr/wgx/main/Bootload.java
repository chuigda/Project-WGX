package chr.wgx.main;

import chr.wgx.util.SharedObjectLoader;
import tech.icey.vk4j.VulkanLoader;

public final class Bootload {
    public static void loadNativeLibraries() {
        VulkanLoader.loadVulkanLibrary();

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            SharedObjectLoader.loadFromResources("/lib/glfw3.dll", ".dll");
            SharedObjectLoader.loadFromResources("/lib/vma.dll", ".dll");
        }
        else {
            System.loadLibrary("glfw");
            SharedObjectLoader.loadFromResources("/lib/libvma.so", ".so");
        }
    }
}
