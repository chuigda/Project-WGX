package chr.wgx.render.vk;

import chr.wgx.Config;
import chr.wgx.render.RenderException;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.Loader;
import tech.icey.panama.NativeLayout;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.panama.buffer.FloatBuffer;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.panama.buffer.PointerBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.Version;
import tech.icey.vk4j.VulkanLoader;
import tech.icey.vk4j.bitmask.*;
import tech.icey.vk4j.command.DeviceCommands;
import tech.icey.vk4j.command.EntryCommands;
import tech.icey.vk4j.command.InstanceCommands;
import tech.icey.vk4j.command.StaticCommands;
import tech.icey.vk4j.datatype.*;
import tech.icey.vk4j.enumtype.*;
import tech.icey.vk4j.handle.*;
import tech.icey.vma.VMA;
import tech.icey.vma.VMAJavaTraceUtil;
import tech.icey.vma.VMAUtil;
import tech.icey.vma.datatype.VmaAllocatorCreateInfo;
import tech.icey.vma.datatype.VmaVulkanFunctions;
import tech.icey.vma.handle.VmaAllocator;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.logging.Logger;

public final class VulkanRenderEngineState {
    public final StaticCommands sCmd;
    public final EntryCommands eCmd;
    public final InstanceCommands iCmd;
    public final DeviceCommands dCmd;
    public final VMA vma;

    public final VkPhysicalDevice physicalDevice;
    public final int graphicsQueueFamilyIndex;
    public final int presentQueueFamilyIndex;
    public final Option<Integer> dedicatedTransferQueueFamilyIndex;

    public final VkInstance instance;
    public final Option<VkDebugUtilsMessengerEXT> debugMessenger;
    public final VkSurfaceKHR surface;
    public final VkDevice device;
    public final VkQueue graphicsQueue;
    public final VkQueue presentQueue;
    public final Option<VkQueue> dedicatedTransferQueue;
    public final VmaAllocator vmaAllocator;

    public Option<VkSwapchainKHR> swapchain = Option.none();

    public VulkanRenderEngineState(
            StaticCommands sCmd,
            EntryCommands eCmd,
            InstanceCommands iCmd,
            DeviceCommands dCmd,
            VMA vma,

            VkPhysicalDevice physicalDevice,
            int graphicsQueueFamilyIndex,
            int presentQueueFamilyIndex,
            Option<Integer> dedicatedTransferQueueFamilyIndex,

            VkInstance instance,
            Option<VkDebugUtilsMessengerEXT> debugMessenger,
            VkSurfaceKHR surface,
            VkDevice device,
            VkQueue graphicsQueue,
            VkQueue presentQueue,
            Option<VkQueue> dedicatedTransferQueue,
            VmaAllocator vmaAllocator
    ) {
        this.sCmd = sCmd;
        this.eCmd = eCmd;
        this.iCmd = iCmd;
        this.dCmd = dCmd;
        this.vma = vma;

        this.physicalDevice = physicalDevice;
        this.graphicsQueueFamilyIndex = graphicsQueueFamilyIndex;
        this.presentQueueFamilyIndex = presentQueueFamilyIndex;
        this.dedicatedTransferQueueFamilyIndex = dedicatedTransferQueueFamilyIndex;

        this.instance = instance;
        this.debugMessenger = debugMessenger;
        this.surface = surface;
        this.device = device;
        this.graphicsQueue = graphicsQueue;
        this.presentQueue = presentQueue;
        this.dedicatedTransferQueue = dedicatedTransferQueue;
        this.vmaAllocator = vmaAllocator;
    }

    private static final class Initialiser {
        private GLFW glfw;
        private GLFWwindow window;
        private StaticCommands sCmd;
        private EntryCommands eCmd;
        private boolean enableValidationLayers;
        private VkInstance instance;
        private InstanceCommands iCmd;
        private Option<VkDebugUtilsMessengerEXT> debugMessenger;
        private VkSurfaceKHR surface;
        private VkPhysicalDevice physicalDevice;
        private int graphicsQueueFamilyIndex;
        private int presentQueueFamilyIndex;
        private Option<Integer> dedicatedTransferQueueFamilyIndex;
        private VkDevice device;
        private DeviceCommands dCmd;
        private VkQueue graphicsQueue;
        private VkQueue presentQueue;
        private Option<VkQueue> dedicatedTransferQueue;
        private VMA vma;
        private VmaAllocator vmaAllocator;

        public VulkanRenderEngineState init(GLFW glfw, GLFWwindow window) throws RenderException {
            this.glfw = glfw;
            this.window = window;

            sCmd = VulkanLoader.loadStaticCommands();
            eCmd = VulkanLoader.loadEntryCommands();

            createInstance();
            setupDebugMessenger();
            createSurface();
            pickPhysicalDevice();
            findQueueFamilyIndices();
            createLogicalDevice();
            createVMA();
            createSwapchain();

            return new VulkanRenderEngineState(
                    sCmd,
                    eCmd,
                    iCmd,
                    dCmd,
                    vma,

                    physicalDevice,
                    graphicsQueueFamilyIndex,
                    presentQueueFamilyIndex,
                    dedicatedTransferQueueFamilyIndex,

                    instance,
                    debugMessenger,
                    surface,
                    device,
                    graphicsQueue,
                    presentQueue,
                    dedicatedTransferQueue,
                    vmaAllocator
            );
        }

        private void createInstance() throws RenderException {
            enableValidationLayers = Config.config().vulkanConfig.validationLayers;
            boolean validationLayersSupported = checkValidationLayerSupport();
            if (enableValidationLayers && !validationLayersSupported) {
                logger.warning("配置文件中要求启用 Vulkan 校验层, 但当前环境中没有受支持的校验层可用");
                enableValidationLayers = false;
            }

            try (Arena arena = Arena.ofConfined()) {
                VkApplicationInfo appInfo = VkApplicationInfo.allocate(arena);
                appInfo.pApplicationName(APP_NAME_BUF);
                appInfo.applicationVersion(Version.vkMakeAPIVersion(0, 1, 0, 0));
                appInfo.pEngineName(ENGINE_NAME_BUF);
                appInfo.engineVersion(Version.vkMakeAPIVersion(0, 1, 0, 0));
                appInfo.apiVersion(Version.VK_API_VERSION_1_3);

                IntBuffer pGLFWExtensionCount = IntBuffer.allocate(arena);
                PointerBuffer glfwExtensions = glfw.glfwGetRequiredInstanceExtensions(pGLFWExtensionCount);
                if (glfwExtensions == null) {
                    throw new RenderException("无法获取 GLFW 所需的 Vulkan 实例扩展");
                }
                int glfwExtensionCount = pGLFWExtensionCount.read();
                glfwExtensions = glfwExtensions.reinterpret(glfwExtensionCount);

                PointerBuffer extensions;
                if (!enableValidationLayers) {
                    extensions = glfwExtensions;
                } else {
                    extensions = PointerBuffer.allocate(arena, glfwExtensionCount + 1);
                    for (int i = 0; i < glfwExtensionCount; i++) {
                        extensions.write(i, glfwExtensions.read(i));
                    }
                    extensions.write(glfwExtensionCount, VALIDATION_LAYER_EXTENSION_BUF);
                }

                VkInstanceCreateInfo instanceCreateInfo = VkInstanceCreateInfo.allocate(arena);
                instanceCreateInfo.pApplicationInfo(appInfo);
                instanceCreateInfo.enabledExtensionCount((int) extensions.size());
                instanceCreateInfo.ppEnabledExtensionNames(extensions);

                if (enableValidationLayers) {
                    PointerBuffer ppEnabledLayerNames = PointerBuffer.allocate(arena);
                    ppEnabledLayerNames.write(VALIDATION_LAYER_NAME_BUF);
                    instanceCreateInfo.enabledLayerCount(1);
                    instanceCreateInfo.ppEnabledLayerNames(ppEnabledLayerNames);

                    VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo =
                            VkDebugUtilsMessengerCreateInfoEXT.allocate(arena);
                    populateDebugMessengerCreateInfo(debugCreateInfo);
                    instanceCreateInfo.pNext(debugCreateInfo);
                }

                VkInstance.Buffer pInstance = VkInstance.Buffer.allocate(arena);
                @enumtype(VkResult.class) int result =
                        eCmd.vkCreateInstance(instanceCreateInfo, null, pInstance);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法创建 Vulkan 实例, 错误代码: " + VkResult.explain(result));
                }

                instance = pInstance.read();
                iCmd = VulkanLoader.loadInstanceCommands(instance, sCmd);
            }
        }

        private void setupDebugMessenger() {
            if (!enableValidationLayers) {
                debugMessenger = Option.none();
                return;
            }

            try (Arena arena = Arena.ofConfined()) {
                VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.allocate(arena);
                populateDebugMessengerCreateInfo(debugCreateInfo);

                VkDebugUtilsMessengerEXT.Buffer pDebugMessenger = VkDebugUtilsMessengerEXT.Buffer.allocate(arena);
                @enumtype(VkResult.class) int result = iCmd.vkCreateDebugUtilsMessengerEXT(
                        instance,
                        debugCreateInfo,
                        null,
                        pDebugMessenger
                );
                if (result != VkResult.VK_SUCCESS) {
                    logger.severe("无法创建 Vulkan 调试信使, 错误代码: " + VkResult.explain(result));
                    logger.warning("程序将会继续运行, 但校验层调试信息可能无法输出");
                    debugMessenger = Option.none();
                } else {
                    debugMessenger = Option.some(pDebugMessenger.read());
                }
            }
        }

        private void createSurface() throws RenderException {
            try (Arena arena = Arena.ofConfined()) {
                VkSurfaceKHR.Buffer pSurface = VkSurfaceKHR.Buffer.allocate(arena);
                @enumtype(VkResult.class) int result = glfw.glfwCreateWindowSurface(instance, window, null, pSurface);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法创建 Vulkan 窗口表面, 错误代码: " + VkResult.explain(result));
                }
                surface = pSurface.read();
            }
        }

        private void pickPhysicalDevice() throws RenderException {
            int physicalDeviceID = Config.config().vulkanConfig.physicalDeviceID;
            try (Arena arena = Arena.ofConfined()) {
                IntBuffer pDeviceCount = IntBuffer.allocate(arena);
                @enumtype(VkResult.class) int result = iCmd.vkEnumeratePhysicalDevices(instance, pDeviceCount, null);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法获取 Vulkan 物理设备列表, 错误代码: " + VkResult.explain(result));
                }

                int deviceCount = pDeviceCount.read();
                if (deviceCount == 0) {
                    throw new RenderException("未找到任何 Vulkan 物理设备");
                }

                VkPhysicalDevice.Buffer pDevices = VkPhysicalDevice.Buffer.allocate(arena, deviceCount);
                result = iCmd.vkEnumeratePhysicalDevices(instance, pDeviceCount, pDevices);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法获取 Vulkan 物理设备列表, 错误代码: " + VkResult.explain(result));
                }

                VkPhysicalDeviceProperties pDeviceProperties = VkPhysicalDeviceProperties.allocate(arena);
                for (int i = 0; i < deviceCount; i++) {
                    VkPhysicalDevice device = pDevices.read(i);
                    iCmd.vkGetPhysicalDeviceProperties(device, pDeviceProperties);
                    if (physicalDeviceID == 0 || i == physicalDeviceID) {
                        physicalDevice = device;
                        break;
                    }
                }
                if (physicalDevice == null) {
                    throw new RenderException("未找到指定的 Vulkan 物理设备");
                }
            }
        }

        private void findQueueFamilyIndices() throws RenderException {
            Option<Integer> graphicsFamilyIndexOpt = Option.none();
            Option<Integer> presentFamilyIndexOpt = Option.none();
            dedicatedTransferQueueFamilyIndex = Option.none();

            try (Arena arena = Arena.ofConfined()) {
                IntBuffer pQueueFamilyPropertyCount = IntBuffer.allocate(arena);
                iCmd.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null);
                int queueFamilyPropertyCount = pQueueFamilyPropertyCount.read();
                VkQueueFamilyProperties[] queueFamilyProperties =
                        VkQueueFamilyProperties.allocate(arena, queueFamilyPropertyCount);
                iCmd.vkGetPhysicalDeviceQueueFamilyProperties(
                        physicalDevice,
                        pQueueFamilyPropertyCount,
                        queueFamilyProperties[0]
                );

                for (int i = 0; i < queueFamilyPropertyCount; i++) {
                    VkQueueFamilyProperties queueFamilyProperty = queueFamilyProperties[i];
                    @enumtype(VkQueueFlags.class) int queueFlags = queueFamilyProperty.queueFlags();
                    logger.fine("正在检查队列 " + i + ", 支持操作: " + VkQueueFlags.explain(queueFlags));

                    if ((queueFlags & VkQueueFlags.VK_QUEUE_GRAPHICS_BIT) != 0 && graphicsFamilyIndexOpt.isNone()) {
                        logger.info(
                                "找到支持图形渲染的队列族: " + i +
                                        ", 队列数量: " + queueFamilyProperty.queueCount() +
                                        ", 支持的操作: " + VkQueueFlags.explain(queueFlags)
                        );
                        graphicsFamilyIndexOpt = Option.some(i);
                    }

                    IntBuffer pSupportsPresent = IntBuffer.allocate(arena);
                    iCmd.vkGetPhysicalDeviceSurfaceSupportKHR(
                            physicalDevice,
                            i,
                            surface,
                            pSupportsPresent
                    );
                    int supportsPresent = pSupportsPresent.read();
                    if (supportsPresent == Constants.VK_TRUE && presentFamilyIndexOpt.isNone()) {
                        logger.info("找到支持窗口呈现的队列族: " + i);
                        presentFamilyIndexOpt = Option.some(i);
                    }

                    @enumtype(VkQueueFlags.class) int prohibitedFlags =
                            VkQueueFlags.VK_QUEUE_GRAPHICS_BIT |
                                    VkQueueFlags.VK_QUEUE_COMPUTE_BIT;
                    if ((queueFlags & VkQueueFlags.VK_QUEUE_TRANSFER_BIT) != 0 &&
                            supportsPresent != Constants.VK_TRUE &&
                            (queueFlags & prohibitedFlags) == 0 &&
                            dedicatedTransferQueueFamilyIndex.isNone()) {
                        logger.info(
                                "找到专用传输队列族: " + i +
                                        ", 队列数量: " + queueFamilyProperty.queueCount() +
                                        ", 支持的操作: " + VkQueueFlags.explain(queueFlags)
                        );
                        dedicatedTransferQueueFamilyIndex = Option.some(i);
                    }
                }

                if (!(graphicsFamilyIndexOpt instanceof Option.Some<Integer> someGraphicsFamilyIndex)) {
                    throw new RenderException("未找到支持图形渲染的队列族");
                }
                if (!(presentFamilyIndexOpt instanceof Option.Some<Integer> somePresentFamilyIndex)) {
                    throw new RenderException("未找到支持窗口呈现的队列族");
                }
                graphicsQueueFamilyIndex = someGraphicsFamilyIndex.value;
                presentQueueFamilyIndex = somePresentFamilyIndex.value;

                if (dedicatedTransferQueueFamilyIndex.isNone()) {
                    logger.info("未找到专用传输队列族, 渲染器将不会使用多线程数据传输");
                }
            }
        }

        private void createLogicalDevice() throws RenderException {
            VulkanConfig config = Config.config().vulkanConfig;

            try (Arena arena = Arena.ofConfined()) {
                VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.allocate(arena);
                if (config.enableAnisotropy) {
                    deviceFeatures.samplerAnisotropy(Constants.VK_TRUE);
                }
                if (config.enableMSAA) {
                    deviceFeatures.sampleRateShading(Constants.VK_TRUE);
                }

                FloatBuffer pQueuePriorities = FloatBuffer.allocate(arena);
                pQueuePriorities.write(1.0f);

                int queueCreateInfoCount = graphicsQueueFamilyIndex != presentQueueFamilyIndex ? 2 : 1;
                if (dedicatedTransferQueueFamilyIndex.isSome()) {
                    queueCreateInfoCount++;
                }
                VkDeviceQueueCreateInfo[] queueCreateInfos = VkDeviceQueueCreateInfo.allocate(arena, queueCreateInfoCount);
                queueCreateInfos[0].queueCount(1);
                queueCreateInfos[0].queueFamilyIndex(graphicsQueueFamilyIndex);
                queueCreateInfos[0].pQueuePriorities(pQueuePriorities);
                if (graphicsQueueFamilyIndex != presentQueueFamilyIndex) {
                    queueCreateInfos[1].queueCount(1);
                    queueCreateInfos[1].queueFamilyIndex(presentQueueFamilyIndex);
                    queueCreateInfos[1].pQueuePriorities(pQueuePriorities);
                }
                if (dedicatedTransferQueueFamilyIndex instanceof Option.Some<Integer> index) {
                    queueCreateInfos[queueCreateInfoCount - 1].queueCount(1);
                    queueCreateInfos[queueCreateInfoCount - 1].queueFamilyIndex(index.value);
                    queueCreateInfos[queueCreateInfoCount - 1].pQueuePriorities(pQueuePriorities);
                }

                PointerBuffer ppDeviceExtensions = PointerBuffer.allocate(arena);
                ppDeviceExtensions.write(VK_SWAPCHAIN_EXTENSION_BUF);

                VkPhysicalDeviceDynamicRenderingFeatures dynamicRenderingFeatures =
                        VkPhysicalDeviceDynamicRenderingFeatures.allocate(arena);
                dynamicRenderingFeatures.dynamicRendering(Constants.VK_TRUE);

                VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.allocate(arena);
                deviceCreateInfo.pEnabledFeatures(deviceFeatures);
                deviceCreateInfo.queueCreateInfoCount(queueCreateInfoCount);
                deviceCreateInfo.pQueueCreateInfos(queueCreateInfos[0]);
                deviceCreateInfo.enabledExtensionCount(1);
                deviceCreateInfo.ppEnabledExtensionNames(ppDeviceExtensions);
                if (enableValidationLayers) {
                    PointerBuffer ppEnabledLayerNames = PointerBuffer.allocate(arena);
                    ppEnabledLayerNames.write(VALIDATION_LAYER_NAME_BUF);
                    deviceCreateInfo.enabledLayerCount(1);
                    deviceCreateInfo.ppEnabledLayerNames(ppEnabledLayerNames);
                }
                deviceCreateInfo.pNext(dynamicRenderingFeatures);

                VkDevice.Buffer pDevice = VkDevice.Buffer.allocate(arena);
                @enumtype(VkResult.class) int result =
                        iCmd.vkCreateDevice(physicalDevice, deviceCreateInfo, null, pDevice);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法创建 Vulkan 逻辑设备, 错误代码: " + VkResult.explain(result));
                }
                device = pDevice.read();
                dCmd = VulkanLoader.loadDeviceCommands(instance, device, sCmd);

                VkQueue.Buffer pQueue = VkQueue.Buffer.allocate(arena);
                dCmd.vkGetDeviceQueue(device, graphicsQueueFamilyIndex, 0, pQueue);
                graphicsQueue = pQueue.read();
                dCmd.vkGetDeviceQueue(device, presentQueueFamilyIndex, 0, pQueue);
                presentQueue = pQueue.read();
                if (dedicatedTransferQueueFamilyIndex instanceof Option.Some<Integer> index) {
                    dCmd.vkGetDeviceQueue(device, index.value, 0, pQueue);
                    dedicatedTransferQueue = Option.some(pQueue.read());
                } else {
                    dedicatedTransferQueue = Option.none();
                }
            }
        }

        private void createVMA() throws RenderException {
            vma = new VMA(Loader::loadFunction);
            VMAJavaTraceUtil.enableJavaTraceForVMA();

            try (Arena arena = Arena.ofConfined()) {
                VmaVulkanFunctions vmaVulkanFunctions = VmaVulkanFunctions.allocate(arena);
                VMAUtil.fillVulkanFunctions(vmaVulkanFunctions, sCmd, eCmd, iCmd, dCmd);

                VmaAllocatorCreateInfo vmaCreateInfo = VmaAllocatorCreateInfo.allocate(arena);
                vmaCreateInfo.instance(instance);
                vmaCreateInfo.physicalDevice(physicalDevice);
                vmaCreateInfo.device(device);
                vmaCreateInfo.pVulkanFunctions(vmaVulkanFunctions);
                vmaCreateInfo.vulkanApiVersion(Version.VK_API_VERSION_1_3);

                VmaAllocator.Buffer pVmaAllocator = VmaAllocator.Buffer.allocate(arena);
                @enumtype(VkResult.class) int result = vma.vmaCreateAllocator(vmaCreateInfo, pVmaAllocator);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法创建 Vulkan 内存分配器, 错误代码: " + VkResult.explain(result));
                }
                vmaAllocator = pVmaAllocator.read();
            }
        }

        private void createSwapchain() throws RenderException {
            try (Arena arena = Arena.ofConfined()) {
                SwapchainSupportDetails swapchainSupportDetails = querySwapchainSupportDetails(arena);

                VkSurfaceFormatKHR surfaceFormat = chooseSwapchainSurfaceFormat(swapchainSupportDetails.formats());
                @enumtype(VkPresentModeKHR.class) int presentMode =
                        chooseSwapchainPresentMode(swapchainSupportDetails.presentModes());
                VkExtent2D extent = chooseSwapExtent(swapchainSupportDetails.capabilities(), arena);

                int imageCount = swapchainSupportDetails.capabilities().minImageCount() + 1;
                if (swapchainSupportDetails.capabilities().maxImageCount() > 0 &&
                    imageCount > swapchainSupportDetails.capabilities().maxImageCount()) {
                    imageCount = swapchainSupportDetails.capabilities().maxImageCount();
                }

                VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.allocate(arena);
                swapchainCreateInfo.surface(surface);
                swapchainCreateInfo.minImageCount(imageCount);
                swapchainCreateInfo.imageFormat(surfaceFormat.format());
                swapchainCreateInfo.imageColorSpace(surfaceFormat.colorSpace());
                swapchainCreateInfo.imageExtent(extent);
                swapchainCreateInfo.imageArrayLayers(1);
                swapchainCreateInfo.imageUsage(VkImageUsageFlags.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
                if (graphicsQueueFamilyIndex != presentQueueFamilyIndex) {
                    swapchainCreateInfo.imageSharingMode(VkSharingMode.VK_SHARING_MODE_CONCURRENT);
                    IntBuffer pQueueFamilyIndices = IntBuffer.allocate(arena, 2);
                    pQueueFamilyIndices.write(0, graphicsQueueFamilyIndex);
                    pQueueFamilyIndices.write(1, presentQueueFamilyIndex);
                    swapchainCreateInfo.pQueueFamilyIndices(pQueueFamilyIndices);
                } else {
                    swapchainCreateInfo.imageSharingMode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE);
                }
                swapchainCreateInfo.preTransform(swapchainSupportDetails.capabilities().currentTransform());
                swapchainCreateInfo.compositeAlpha(VkCompositeAlphaFlagsKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
                swapchainCreateInfo.presentMode(presentMode);
                swapchainCreateInfo.clipped(Constants.VK_TRUE);

                VkSwapchainKHR.Buffer pSwapchain = VkSwapchainKHR.Buffer.allocate(arena);
                @enumtype(VkResult.class) int result = dCmd.vkCreateSwapchainKHR(device, swapchainCreateInfo, null, pSwapchain);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法创建 Vulkan 交换链, 错误代码: " + VkResult.explain(result));
                }
                VkSwapchainKHR swapchain = pSwapchain.read();
            }
        }

        private boolean checkValidationLayerSupport() {
            try (Arena arena = Arena.ofConfined()) {
                IntBuffer pLayerCount = IntBuffer.allocate(arena);
                @enumtype(VkResult.class) int result = eCmd.vkEnumerateInstanceLayerProperties(pLayerCount, null);
                if (result != VkResult.VK_SUCCESS) {
                    logger.warning("无法获取 Vulkan 实例层属性, 错误代码: " + VkResult.explain(result));
                    return false;
                }

                int layerCount = pLayerCount.read();
                if (layerCount == 0) {
                    return false;
                }

                VkLayerProperties[] availableLayerProperties = VkLayerProperties.allocate(arena, layerCount);
                result = eCmd.vkEnumerateInstanceLayerProperties(pLayerCount, availableLayerProperties[0]);
                if (result != VkResult.VK_SUCCESS) {
                    logger.warning("无法获取 Vulkan 实例层属性, 错误代码: " + VkResult.explain(result));
                    return false;
                }

                for (VkLayerProperties layerProperties : availableLayerProperties) {
                    if (VALIDATION_LAYER_NAME.equals(layerProperties.layerName().readString())) {
                        logger.info("找到 Vulkan 校验层: " + VALIDATION_LAYER_NAME);
                        return true;
                    }
                }
                return false;
            }
        }

        private static void populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo) {
            debugCreateInfo.messageSeverity(
                    VkDebugUtilsMessageSeverityFlagsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT |
                    VkDebugUtilsMessageSeverityFlagsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT |
                    VkDebugUtilsMessageSeverityFlagsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT |
                    VkDebugUtilsMessageSeverityFlagsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT
            );
            debugCreateInfo.messageType(
                    VkDebugUtilsMessageTypeFlagsEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
                    VkDebugUtilsMessageTypeFlagsEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
                    VkDebugUtilsMessageTypeFlagsEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT
            );
            debugCreateInfo.pfnUserCallback(DebugMessengerUtil.DEBUG_CALLBACK_PTR);
        }

        private record SwapchainSupportDetails(
                VkSurfaceCapabilitiesKHR capabilities,
                VkSurfaceFormatKHR[] formats,
                @enumtype(VkPresentModeKHR.class) IntBuffer presentModes
        ) {}

        private SwapchainSupportDetails querySwapchainSupportDetails(Arena arena) throws RenderException {
            VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.allocate(arena);
            @enumtype(VkResult.class) int result = iCmd.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
                    physicalDevice,
                    surface,
                    surfaceCapabilities
            );
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法获取 Vulkan 表面能力, 错误代码: " + VkResult.explain(result));
            }

            try (Arena localArena = Arena.ofConfined()) {
                IntBuffer pCount = IntBuffer.allocate(localArena);
                result = iCmd.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pCount, null);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法获取 Vulkan 表面格式, 错误代码: " + VkResult.explain(result));
                }

                int formatCount = pCount.read();
                VkSurfaceFormatKHR[] surfaceFormats = VkSurfaceFormatKHR.allocate(arena, formatCount);
                result = iCmd.vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pCount, surfaceFormats[0]);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法获取 Vulkan 表面格式, 错误代码: " + VkResult.explain(result));
                }

                result = iCmd.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pCount, null);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法获取 Vulkan 表面呈现模式, 错误代码: " + VkResult.explain(result));
                }

                int presentModeCount = pCount.read();
                IntBuffer presentModes = IntBuffer.allocate(arena, presentModeCount);
                result = iCmd.vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pCount, presentModes);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法获取 Vulkan 表面呈现模式, 错误代码: " + VkResult.explain(result));
                }

                return new SwapchainSupportDetails(surfaceCapabilities, surfaceFormats, presentModes);
            }
        }

        private VkSurfaceFormatKHR chooseSwapchainSurfaceFormat(VkSurfaceFormatKHR[] formats) {
            @enumtype(VkFormat.class) int preferredFormat = Config.config().vulkanConfig.forceUNORM ?
                    VkFormat.VK_FORMAT_B8G8R8A8_UNORM :
                    VkFormat.VK_FORMAT_B8G8R8A8_SRGB;
            for (VkSurfaceFormatKHR format : formats) {
                if (format.format() == preferredFormat &&
                    format.colorSpace() == VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                    return format;
                }
            }
            return formats[0];
        }

        private @enumtype(VkPresentModeKHR.class) int chooseSwapchainPresentMode(
                @enumtype(VkPresentModeKHR.class) IntBuffer presentModes
        ) {
            if (Config.config().vulkanConfig.vsync == 2) {
                return VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR;
            }

            for (int i = 0; i < presentModes.size(); i++) {
                @enumtype(VkPresentModeKHR.class) int presentMode = presentModes.read(i);
                if (presentMode == VkPresentModeKHR.VK_PRESENT_MODE_MAILBOX_KHR) {
                    return presentMode;
                }
            }

            if (Config.config().vulkanConfig.vsync == 0) {
                for (int i = 0; i < presentModes.size(); i++) {
                    @enumtype(VkPresentModeKHR.class) int presentMode = presentModes.read(i);
                    if (presentMode == VkPresentModeKHR.VK_PRESENT_MODE_IMMEDIATE_KHR) {
                        return presentMode;
                    }
                }
            }

            return VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR;
        }

        private VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities, Arena arena) {
            if (capabilities.currentExtent().width() != NativeLayout.UINT32_MAX) {
                return capabilities.currentExtent();
            }

            try (Arena localArena = Arena.ofConfined()) {
                IntBuffer pWidth = IntBuffer.allocate(localArena);
                IntBuffer pHeight = IntBuffer.allocate(localArena);
                glfw.glfwGetFramebufferSize(window, pWidth, pHeight);

                int width = pWidth.read();
                int height = pHeight.read();

                VkExtent2D actualExtent = VkExtent2D.allocate(arena);
                actualExtent.width(Math.max(
                        capabilities.minImageExtent().width(),
                        Math.min(capabilities.maxImageExtent().width(), width)
                ));
                actualExtent.height(Math.max(
                        capabilities.minImageExtent().height(),
                        Math.min(capabilities.maxImageExtent().height(), height)
                ));
                return actualExtent;
            }
        }

        private static final ByteBuffer APP_NAME_BUF = ByteBuffer.allocateString(Arena.global(), "Project-WGX");
        private static final ByteBuffer ENGINE_NAME_BUF = ByteBuffer.allocateString(
                Arena.global(),
                "NG-ARACI : Neue Genesis Advanced Rendering And Computing Infrastructure"
        );
        private static final ByteBuffer VALIDATION_LAYER_EXTENSION_BUF =
                ByteBuffer.allocateString(Arena.global(), Constants.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
        private static final String VALIDATION_LAYER_NAME = "VK_LAYER_KHRONOS_validation";
        private static final ByteBuffer VALIDATION_LAYER_NAME_BUF =
                ByteBuffer.allocateString(Arena.global(), VALIDATION_LAYER_NAME);
        private static final ByteBuffer VK_SWAPCHAIN_EXTENSION_BUF =
                ByteBuffer.allocateString(Arena.global(), Constants.VK_KHR_SWAPCHAIN_EXTENSION_NAME);

        private static final Logger logger = Logger.getLogger(Initialiser.class.getName());
    }

    public static VulkanRenderEngineState init(GLFW glfw, GLFWwindow window) throws RenderException {
        return new Initialiser().init(glfw, window);
    }
}
