package chr.wgx.main;

import chr.wgx.render.RenderException;
import chr.wgx.render.vk.VulkanWindow;
import chr.wgx.util.SharedObjectLoader;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.GLFWConstants;
import tech.icey.glfw.GLFWLoader;
import tech.icey.vk4j.VulkanLoader;

import java.util.logging.Logger;

public final class RenderApplication {
    public static void applicationStart() {
        logger.info("应用程序已启动");
        loadNativeLibraries();
        logger.info("本地库已加载完成");

        GLFW glfw = GLFWLoader.loadGLFW();
        if (glfw.glfwInit() != GLFWConstants.GLFW_TRUE) {
            throw new RuntimeException("GLFW 初始化失败");
        }

        try (VulkanWindow w = new VulkanWindow(glfw, "Zdravstvuyte, mir!", 640, 640)) {
            w.mainLoop(null);
        } catch (RenderException e) {
            throw new RuntimeException(e);
        }
    }

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

    private static final Logger logger = Logger.getLogger(RenderApplication.class.getName());
}
