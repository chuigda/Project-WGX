package chr.wgx.main;

import chr.wgx.render.RenderException;
import chr.wgx.render.vk.VulkanRenderEngine;
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

        try (VulkanWindow w = new VulkanWindow(glfw, "Project-WGX 绘图输出窗口", 640, 640)) {
            w.mainLoop(new VulkanRenderEngine(
                    () -> logger.info("Vulkan 渲染引擎已初始化"),
                    (width, height) -> logger.info("帧缓冲尺寸已调整至 " + width + "x" + height),
                    () -> {},
                    () -> {},
                    () -> logger.info("Vulkan 渲染引擎已关闭")
            ));
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
