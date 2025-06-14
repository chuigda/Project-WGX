package chr.wgx.main;

import chr.wgx.config.Config;
import chr.wgx.reactor.Reactor;
import chr.wgx.render.IRenderEngineFactory;
import chr.wgx.render.RenderException;
import chr.wgx.render.RenderWindow;
import chr.wgx.render.gles2.GLES2RenderEngine;
import chr.wgx.render.vk.VulkanRenderEngine;
import chr.wgx.ui.ControlWindow;
import chr.wgx.ui.ProgressDialog;
import chr.wgx.util.SharedObjectLoader;
import club.doki7.glfw.GLFW;
import club.doki7.glfw.GLFWConstants;
import club.doki7.glfw.GLFWLoader;
import club.doki7.vulkan.command.VulkanLoader;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

public final class RenderApplication {
    public static void applicationStart(ControlWindow controlWindow, ProgressDialog progressDlg) {
        logger.info("应用程序已启动");
        progressDlg.setProgress(10, "加载本地库");
        loadNativeLibraries();
        logger.info("本地库已加载完成");

        progressDlg.setProgress(20, "创建渲染引擎");
        GLFW glfw = GLFWLoader.loadGLFW();
        glfw.initHint(GLFW.PLATFORM, GLFW.PLATFORM_X11);
        if (glfw.init() != GLFW.TRUE) {
            throw new RuntimeException("GLFW 初始化失败");
        }

        Config config = Config.config();
        IRenderEngineFactory factory = Objects.equals(config.renderMode, "vulkan")
                ? VulkanRenderEngine.FACTORY
                : GLES2RenderEngine.FACTORY;

        try (RenderWindow w = factory.createRenderWindow(
                glfw,
                config.windowTitle,
                config.windowWidth,
                config.windowHeight
        )) {
            progressDlg.setProgress(30, "初始化插件系统");
            new Thread(() -> Reactor.reactorMain(w.renderEngine, controlWindow, progressDlg)).start();

            w.mainLoop();
        } catch (RenderException e) {
            logger.severe("渲染引擎异常: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    public static void loadNativeLibraries() {
        Config config = Config.config();
        boolean isVulkan = config.renderMode.equals("vulkan");

        if (isVulkan) {
            logger.fine("正使用 club.doki7.vulkan.VulkanLoader 加载 vulkan 本地库");
            VulkanLoader.loadVulkanLibrary();
        }

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            logger.fine("从资源 /lib/glfw3.dll 中加载 glfw");
            SharedObjectLoader.loadFromResources("/resources/lib/glfw3.dll", ".dll");

            if (isVulkan) {
                logger.fine("从资源 /lib/vma.dll 中加载 vma");
                SharedObjectLoader.loadFromResources("/resources/lib/vma.dll", ".dll");
            }
        }
        else {
            logger.fine("从系统环境中加载 glfw");
            System.loadLibrary("glfw");

            if (isVulkan) {
                if (!System.getProperty("os.arch").equals("amd64")) {
                    String absolutePath = Paths.get("libvma.so").toAbsolutePath().toString();
                    logger.fine("从本地文件系统加载 vma: " + absolutePath);
                    System.load(absolutePath);
                } else {
                    logger.fine("从资源 /lib/libvma.so 中加载 vma");
                    SharedObjectLoader.loadFromResources("/resources/lib/libvma.so", ".so");
                }
            }
        }
    }

    private static final Logger logger = Logger.getLogger(RenderApplication.class.getName());
}
