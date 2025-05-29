package chr.wgx.render.vk;

import chr.wgx.config.Config;
import chr.wgx.config.VulkanConfig;
import chr.wgx.render.RenderException;
import club.doki7.glfw.GLFW;
import club.doki7.glfw.handle.GLFWwindow;
import club.doki7.ffm.Loader;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.ptr.BytePtr;
import club.doki7.ffm.ptr.FloatPtr;
import club.doki7.ffm.ptr.IntPtr;
import club.doki7.ffm.ptr.PointerPtr;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.Version;
import club.doki7.vulkan.bitmask.*;
import club.doki7.vulkan.command.VkDeviceCommands;
import club.doki7.vulkan.command.VkEntryCommands;
import club.doki7.vulkan.command.VkInstanceCommands;
import club.doki7.vulkan.command.VkStaticCommands;
import club.doki7.vulkan.command.VulkanLoader;
import club.doki7.vulkan.datatype.*;
import club.doki7.vulkan.enumtype.VkCommandBufferLevel;
import club.doki7.vulkan.enumtype.VkPhysicalDeviceType;
import club.doki7.vulkan.enumtype.VkResult;
import club.doki7.vulkan.handle.*;
import club.doki7.vma.VMA;
import club.doki7.vma.VMAJavaTraceUtil;
import club.doki7.vma.VMAUtil;
import club.doki7.vma.datatype.VmaAllocatorCreateInfo;
import club.doki7.vma.datatype.VmaVulkanFunctions;
import club.doki7.vma.handle.VmaAllocator;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.Objects;
import java.util.logging.Logger;

@SuppressWarnings("NotNullFieldNotInitialized")
final class VREContextInitialiser {
    private final Arena prefabArena = Arena.ofAuto();

    private GLFW glfw;
    private GLFWwindow window;
    private VkStaticCommands sCmd;
    private VkEntryCommands eCmd;
    private boolean enableValidationLayers;
    private VkInstance instance;
    private VkInstanceCommands iCmd;
    private Option<VkDebugUtilsMessengerEXT> debugMessenger;
    private VkSurfaceKHR surface;
    private VkPhysicalDevice physicalDevice;
    private int graphicsQueueFamilyIndex;
    private int presentQueueFamilyIndex;
    private Option<Integer> dedicatedTransferQueueFamilyIndex;
    private VkDevice device;
    private VkDeviceCommands dCmd;
    private VkQueue graphicsQueue;
    private VkQueue presentQueue;
    private Option<VkQueue> dedicatedTransferQueue;
    private VMA vma;
    private VmaAllocator vmaAllocator;
    private VkSemaphore[] imageAvailableSemaphores;
    private VkFence[] inFlightFences;
    private VkCommandPool commandPool;
    private VkCommandBuffer.Ptr commandBuffers;
    private VkCommandPool graphicsOnceCommandPool;
    private Option<VkCommandPool> transferCommandPool;

    public VulkanRenderEngineContext init(GLFW glfw, GLFWwindow window) throws RenderException {
        this.glfw = glfw;
        this.window = window;

        sCmd = VulkanLoader.loadStaticCommands();
        eCmd = VulkanLoader.loadEntryCommands(sCmd);

        createInstance();
        setupDebugMessenger();
        createSurface();
        pickPhysicalDevice();
        findQueueFamilyIndices();
        createLogicalDevice();
        createVMA();
        createSyncObjects();
        createCommandPool();
        createCommandBuffers();

        return new VulkanRenderEngineContext(
                prefabArena,

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
                vmaAllocator,
                imageAvailableSemaphores,
                inFlightFences,
                commandPool,
                graphicsOnceCommandPool,
                commandBuffers,
                transferCommandPool
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
            appInfo.applicationVersion(new Version(0, 1, 0, 0).encode());
            appInfo.pEngineName(ENGINE_NAME_BUF);
            appInfo.engineVersion(new Version(0, 1, 0, 0).encode());
            appInfo.apiVersion(Version.VK_API_VERSION_1_3.encode());

            IntPtr pGLFWExtensionCount = IntPtr.allocate(arena);
            PointerPtr glfwExtensions = glfw.getRequiredInstanceExtensions(pGLFWExtensionCount);
            if (glfwExtensions == null) {
                throw new RenderException("无法获取 GLFW 所需的 Vulkan 实例扩展");
            }
            int glfwExtensionCount = pGLFWExtensionCount.read();
            glfwExtensions = glfwExtensions.reinterpret(glfwExtensionCount);

            PointerPtr extensions;
            if (!enableValidationLayers) {
                extensions = glfwExtensions;
            } else {
                extensions = PointerPtr.allocate(arena, glfwExtensionCount + 1);
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
                PointerPtr ppEnabledLayerNames = PointerPtr.allocate(arena);
                ppEnabledLayerNames.write(VALIDATION_LAYER_NAME_BUF);
                instanceCreateInfo.enabledLayerCount(1);
                instanceCreateInfo.ppEnabledLayerNames(ppEnabledLayerNames);

                VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo =
                        VkDebugUtilsMessengerCreateInfoEXT.allocate(arena);
                populateDebugMessengerCreateInfo(debugCreateInfo);
                instanceCreateInfo.pNext(debugCreateInfo);
            }

            VkInstance.Ptr pInstance = VkInstance.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result =
                    eCmd.createInstance(instanceCreateInfo, null, pInstance);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建 Vulkan 实例, 错误代码: " + VkResult.explain(result));
            }

            instance = Objects.requireNonNull(pInstance.read());
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

            VkDebugUtilsMessengerEXT.Ptr pDebugMessenger = VkDebugUtilsMessengerEXT.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result = iCmd.createDebugUtilsMessengerEXT(
                    instance,
                    debugCreateInfo,
                    null,
                    pDebugMessenger
            );
            if (result != VkResult.SUCCESS) {
                logger.severe("无法创建 Vulkan 调试信使, 错误代码: " + VkResult.explain(result));
                logger.warning("程序将会继续运行, 但校验层调试信息可能无法输出");
                debugMessenger = Option.none();
            } else {
                debugMessenger = Option.some(Objects.requireNonNull(pDebugMessenger.read()));
            }
        }
    }

    private void createSurface() throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            VkSurfaceKHR.Ptr pSurface = VkSurfaceKHR.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result = glfw.createWindowSurface(instance, window, null, pSurface);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建 Vulkan 窗口表面, 错误代码: " + VkResult.explain(result));
            }
            surface = Objects.requireNonNull(pSurface.read());
        }
    }

    private void pickPhysicalDevice() throws RenderException {
        int physicalDeviceID = Config.config().vulkanConfig.physicalDeviceID;
        try (Arena arena = Arena.ofConfined()) {
            logger.info("正在选取 Vulkan 物理设备");
            IntPtr pDeviceCount = IntPtr.allocate(arena);
            @EnumType(VkResult.class) int result = iCmd.enumeratePhysicalDevices(instance, pDeviceCount, null);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法获取 Vulkan 物理设备列表, 错误代码: " + VkResult.explain(result));
            }

            int deviceCount = pDeviceCount.read();
            if (deviceCount == 0) {
                throw new RenderException("未找到任何 Vulkan 物理设备");
            }

            VkPhysicalDevice.Ptr pDevices = VkPhysicalDevice.Ptr.allocate(arena, deviceCount);
            result = iCmd.enumeratePhysicalDevices(instance, pDeviceCount, pDevices);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法获取 Vulkan 物理设备列表, 错误代码: " + VkResult.explain(result));
            }

            VkPhysicalDeviceProperties.Ptr deviceProperties = VkPhysicalDeviceProperties.allocate(arena, deviceCount);
            for (int i = 0; i < deviceCount; i++) {
                VkPhysicalDevice device = Objects.requireNonNull(pDevices.read(i));
                VkPhysicalDeviceProperties deviceProperty = deviceProperties.at(i);
                iCmd.getPhysicalDeviceProperties(device, deviceProperty);
                Version decodedVersion = Version.decode(deviceProperty.apiVersion());
                logger.info(String.format(
                        "发现 Vulkan 物理设备, ID: %s, 名称: %s, 类型: %s, API 版本: %d.%d.%d",
                        Integer.toUnsignedString(deviceProperty.deviceID()),
                        deviceProperty.deviceName().readString(),
                        VkPhysicalDeviceType.explain(deviceProperty.deviceType()),
                        decodedVersion.major(),
                        decodedVersion.minor(),
                        decodedVersion.patch()
                ));
            }

            for (int i = 0; i < deviceCount; i++) {
                VkPhysicalDeviceProperties deviceProperty = deviceProperties.at(i);
                if (physicalDeviceID == 0 || deviceProperty.deviceID() == physicalDeviceID) {
                    physicalDevice = Objects.requireNonNull(pDevices.read(i));

                    if (physicalDeviceID == 0) {
                        logger.info("自动选定 Vulkan 物理设备: " + deviceProperty.deviceName().readString());
                    } else {
                        logger.info(String.format(
                                "根据指定的设备 ID=%s 选定了 Vulkan 物理设备: %s",
                                Integer.toUnsignedString(physicalDeviceID),
                                deviceProperty.deviceName().readString()
                        ));
                    }
                    break;
                }
            }

            //noinspection ConstantValue
            if (physicalDevice == null) {
                throw new RenderException("未找到指定的 Vulkan 物理设备");
            }
        }
    }

    private void findQueueFamilyIndices() throws RenderException {
        Config config = Config.config();
        Option<Integer> graphicsFamilyIndexOpt = Option.none();
        Option<Integer> presentFamilyIndexOpt = Option.none();
        dedicatedTransferQueueFamilyIndex = Option.none();

        try (Arena arena = Arena.ofConfined()) {
            IntPtr pQueueFamilyPropertyCount = IntPtr.allocate(arena);
            iCmd.getPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null);
            int queueFamilyPropertyCount = pQueueFamilyPropertyCount.read();
            VkQueueFamilyProperties.Ptr queueFamilyProperties =
                    VkQueueFamilyProperties.allocate(arena, queueFamilyPropertyCount);
            iCmd.getPhysicalDeviceQueueFamilyProperties(
                    physicalDevice,
                    pQueueFamilyPropertyCount,
                    queueFamilyProperties
            );

            for (int i = 0; i < queueFamilyPropertyCount; i++) {
                VkQueueFamilyProperties queueFamilyProperty = queueFamilyProperties.at(i);
                @EnumType(VkQueueFlags.class) int queueFlags = queueFamilyProperty.queueFlags();
                logger.fine("正在检查队列 " + i + ", 支持操作: " + VkQueueFlags.explain(queueFlags));

                if ((queueFlags & VkQueueFlags.GRAPHICS) != 0 && graphicsFamilyIndexOpt.isNone()) {
                    logger.info(
                            "找到支持图形渲染的队列族: " + i +
                                    ", 队列数量: " + queueFamilyProperty.queueCount() +
                                    ", 支持的操作: " + VkQueueFlags.explain(queueFlags)
                    );
                    graphicsFamilyIndexOpt = Option.some(i);
                }

                IntPtr pSupportsPresent = IntPtr.allocate(arena);
                iCmd.getPhysicalDeviceSurfaceSupportKHR(
                        physicalDevice,
                        1,
                        surface,
                        pSupportsPresent
                );
                int supportsPresent = pSupportsPresent.read();
                if (supportsPresent == VkConstants.TRUE && presentFamilyIndexOpt.isNone()) {
                    logger.info("找到支持窗口呈现的队列族: " + i);
                    presentFamilyIndexOpt = Option.some(i);
                }

                if (config.vulkanConfig.alwaysUploadWithGraphicsQueue) {
                    continue;
                }

                @EnumType(VkQueueFlags.class) int prohibitedFlags =
                        VkQueueFlags.GRAPHICS | VkQueueFlags.COMPUTE;
                if ((queueFlags & VkQueueFlags.TRANSFER) != 0
                        && supportsPresent != VkConstants.TRUE
                        && (queueFlags & prohibitedFlags) == 0
                        && dedicatedTransferQueueFamilyIndex.isNone()) {
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
                logger.info("专用传输队列族未找到或被手动禁用, 渲染器将不会使用专用传输队列传输数据");
            }
        }
    }

    private void createLogicalDevice() throws RenderException {
        VulkanConfig config = Config.config().vulkanConfig;

        try (Arena arena = Arena.ofConfined()) {
            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.allocate(arena);
            if (config.enableAnisotropy) {
                deviceFeatures.samplerAnisotropy(VkConstants.TRUE);
            }

            FloatPtr pQueuePriorities = FloatPtr.allocate(arena);
            pQueuePriorities.write(1.0f);

            int queueCreateInfoCount = graphicsQueueFamilyIndex != presentQueueFamilyIndex ? 2 : 1;
            if (dedicatedTransferQueueFamilyIndex.isSome()) {
                queueCreateInfoCount++;
            }
            VkDeviceQueueCreateInfo.Ptr queueCreateInfos = VkDeviceQueueCreateInfo.allocate(arena, queueCreateInfoCount);
            VkDeviceQueueCreateInfo graphicsQueueInfo = queueCreateInfos.at(0);
            graphicsQueueInfo.queueCount(1);
            graphicsQueueInfo.queueFamilyIndex(graphicsQueueFamilyIndex);
            graphicsQueueInfo.pQueuePriorities(pQueuePriorities);
            if (graphicsQueueFamilyIndex != presentQueueFamilyIndex) {
                VkDeviceQueueCreateInfo presentQueueInfo = queueCreateInfos.at(1);
                presentQueueInfo.queueCount(1);
                presentQueueInfo.queueFamilyIndex(presentQueueFamilyIndex);
                presentQueueInfo.pQueuePriorities(pQueuePriorities);
            }
            if (dedicatedTransferQueueFamilyIndex instanceof Option.Some<Integer> someIndex) {
                VkDeviceQueueCreateInfo transferQueueInfo = queueCreateInfos.at(queueCreateInfoCount - 1);
                transferQueueInfo.queueCount(1);
                transferQueueInfo.queueFamilyIndex(someIndex.value);
                transferQueueInfo.pQueuePriorities(pQueuePriorities);
            }

            PointerPtr ppDeviceExtensions = PointerPtr.allocate(arena);
            ppDeviceExtensions.write(VK_SWAPCHAIN_EXTENSION_BUF);

            VkPhysicalDeviceDynamicRenderingFeatures dynamicRenderingFeatures =
                    VkPhysicalDeviceDynamicRenderingFeatures.allocate(arena);
            dynamicRenderingFeatures.dynamicRendering(VkConstants.TRUE);

            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.allocate(arena);
            deviceCreateInfo.pEnabledFeatures(deviceFeatures);
            deviceCreateInfo.queueCreateInfoCount(queueCreateInfoCount);
            deviceCreateInfo.pQueueCreateInfos(queueCreateInfos);
            deviceCreateInfo.enabledExtensionCount(1);
            deviceCreateInfo.ppEnabledExtensionNames(ppDeviceExtensions);
            if (enableValidationLayers) {
                PointerPtr ppEnabledLayerNames = PointerPtr.allocate(arena);
                ppEnabledLayerNames.write(VALIDATION_LAYER_NAME_BUF);
                deviceCreateInfo.enabledLayerCount(1);
                deviceCreateInfo.ppEnabledLayerNames(ppEnabledLayerNames);
            }
            deviceCreateInfo.pNext(dynamicRenderingFeatures);

            VkDevice.Ptr pDevice = VkDevice.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result =
                    iCmd.createDevice(physicalDevice, deviceCreateInfo, null, pDevice);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建 Vulkan 逻辑设备, 错误代码: " + VkResult.explain(result));
            }
            device = Objects.requireNonNull(pDevice.read());
            dCmd = VulkanLoader.loadDeviceCommands(device, sCmd);

            VkQueue.Ptr pQueue = VkQueue.Ptr.allocate(arena);
            dCmd.getDeviceQueue(device, graphicsQueueFamilyIndex, 0, pQueue);
            graphicsQueue = Objects.requireNonNull(pQueue.read());
            dCmd.getDeviceQueue(device, presentQueueFamilyIndex, 0, pQueue);
            presentQueue = Objects.requireNonNull(pQueue.read());
            if (dedicatedTransferQueueFamilyIndex instanceof Option.Some<Integer> someIndex) {
                dCmd.getDeviceQueue(device, someIndex.value, 0, pQueue);
                dedicatedTransferQueue = Option.some(Objects.requireNonNull(pQueue.read()));
            } else {
                dedicatedTransferQueue = Option.none();
            }
        }
    }

    private void createVMA() throws RenderException {
        vma = new VMA(Loader::loadFunctionOrNull);
        VMAJavaTraceUtil.enableJavaTraceForVMA();

        try (Arena arena = Arena.ofConfined()) {
            VmaVulkanFunctions vmaVulkanFunctions = VmaVulkanFunctions.allocate(arena);
            VMAUtil.fillVulkanFunctions(vmaVulkanFunctions, sCmd, eCmd, iCmd, dCmd);

            VmaAllocatorCreateInfo vmaCreateInfo = VmaAllocatorCreateInfo.allocate(arena);
            vmaCreateInfo.instance(instance);
            vmaCreateInfo.physicalDevice(physicalDevice);
            vmaCreateInfo.device(device);
            vmaCreateInfo.pVulkanFunctions(vmaVulkanFunctions);
            vmaCreateInfo.vulkanApiVersion(Version.VK_API_VERSION_1_3.encode());

            VmaAllocator.Ptr pVmaAllocator = VmaAllocator.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result = vma.createAllocator(vmaCreateInfo, pVmaAllocator);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建 Vulkan 内存分配器, 错误代码: " + VkResult.explain(result));
            }
            vmaAllocator = Objects.requireNonNull(pVmaAllocator.read());
        }
    }

    private void createSyncObjects() throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.allocate(arena);
            VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.allocate(arena);
            fenceCreateInfo.flags(VkFenceCreateFlags.SIGNALED);

            imageAvailableSemaphores = new VkSemaphore[Config.config().vulkanConfig.maxFramesInFlight];
            inFlightFences = new VkFence[Config.config().vulkanConfig.maxFramesInFlight];

            VkSemaphore.Ptr pImageAvailableSemaphore = VkSemaphore.Ptr.allocate(arena);
            VkFence.Ptr pInFlightFence = VkFence.Ptr.allocate(arena);

            @EnumType(VkResult.class) int result;
            for (int i = 0; i < Config.config().vulkanConfig.maxFramesInFlight; i++) {
                result = dCmd.createSemaphore(device, semaphoreCreateInfo, null, pImageAvailableSemaphore);
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("无法创建 Vulkan 信号量, 错误代码: " + VkResult.explain(result));
                }

                result = dCmd.createFence(device, fenceCreateInfo, null, pInFlightFence);
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("无法创建 Vulkan 栅栏, 错误代码: " + VkResult.explain(result));
                }

                imageAvailableSemaphores[i] = Objects.requireNonNull(pImageAvailableSemaphore.read());
                inFlightFences[i] = Objects.requireNonNull(pInFlightFence.read());
            }
        }
    }

    private void createCommandPool() throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.allocate(arena);
            commandPoolCreateInfo.queueFamilyIndex(graphicsQueueFamilyIndex);
            commandPoolCreateInfo.flags(
                    VkCommandPoolCreateFlags.TRANSIENT |
                    VkCommandPoolCreateFlags.RESET_COMMAND_BUFFER
            );

            VkCommandPool.Ptr pCommandPool = VkCommandPool.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result =
                    dCmd.createCommandPool(device, commandPoolCreateInfo, null, pCommandPool);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建 Vulkan 命令池, 错误代码: " + VkResult.explain(result));
            }
            commandPool = Objects.requireNonNull(pCommandPool.read());

            commandPoolCreateInfo.flags(VkCommandPoolCreateFlags.TRANSIENT);
            result = dCmd.createCommandPool(device, commandPoolCreateInfo, null, pCommandPool);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建 Vulkan 一次性命令池, 错误代码: " + VkResult.explain(result));
            }
            graphicsOnceCommandPool = Objects.requireNonNull(pCommandPool.read());

            if (dedicatedTransferQueueFamilyIndex instanceof Option.Some<Integer> someIndex) {
                VkCommandPoolCreateInfo transferCommandPoolCreateInfo = VkCommandPoolCreateInfo.allocate(arena);
                transferCommandPoolCreateInfo.queueFamilyIndex(someIndex.value);
                transferCommandPoolCreateInfo.flags(VkCommandPoolCreateFlags.TRANSIENT);

                VkCommandPool.Ptr pTransferCommandPool = VkCommandPool.Ptr.allocate(arena);
                result = dCmd.createCommandPool(device, transferCommandPoolCreateInfo, null, pTransferCommandPool);
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("无法创建 Vulkan 传输命令池, 错误代码: " + VkResult.explain(result));
                }
                transferCommandPool = Option.some(Objects.requireNonNull(pTransferCommandPool.read()));
            } else {
                transferCommandPool = Option.none();
            }
        }
    }

    private void createCommandBuffers() throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            int frameCount = Config.config().vulkanConfig.maxFramesInFlight;

            VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.allocate(arena);
            commandBufferAllocateInfo.commandPool(commandPool);
            commandBufferAllocateInfo.level(VkCommandBufferLevel.PRIMARY);
            commandBufferAllocateInfo.commandBufferCount(frameCount);

            commandBuffers = VkCommandBuffer.Ptr.allocate(prefabArena, frameCount);
            @EnumType(VkResult.class) int result = dCmd.allocateCommandBuffers(
                    device,
                    commandBufferAllocateInfo,
                    commandBuffers
            );
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法分配 Vulkan 命令缓冲区, 错误代码: " + VkResult.explain(result));
            }
        }
    }

    private boolean checkValidationLayerSupport() {
        try (Arena arena = Arena.ofConfined()) {
            IntPtr pLayerCount = IntPtr.allocate(arena);
            @EnumType(VkResult.class) int result = eCmd.enumerateInstanceLayerProperties(pLayerCount, null);
            if (result != VkResult.SUCCESS) {
                logger.warning("无法获取 Vulkan 实例层属性, 错误代码: " + VkResult.explain(result));
                return false;
            }

            int layerCount = pLayerCount.read();
            if (layerCount == 0) {
                return false;
            }

            VkLayerProperties.Ptr availableLayerProperties = VkLayerProperties.allocate(arena, layerCount);
            result = eCmd.enumerateInstanceLayerProperties(pLayerCount, availableLayerProperties);
            if (result != VkResult.SUCCESS) {
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
                VkDebugUtilsMessageSeverityFlagsEXT.VERBOSE |
                        VkDebugUtilsMessageSeverityFlagsEXT.INFO |
                        VkDebugUtilsMessageSeverityFlagsEXT.WARNING |
                        VkDebugUtilsMessageSeverityFlagsEXT.ERROR
        );
        debugCreateInfo.messageType(
                VkDebugUtilsMessageTypeFlagsEXT.GENERAL |
                        VkDebugUtilsMessageTypeFlagsEXT.VALIDATION |
                        VkDebugUtilsMessageTypeFlagsEXT.PERFORMANCE
        );
        debugCreateInfo.pfnUserCallback(DebugMessengerUtil.DEBUG_CALLBACK_PTR);
    }

    private static final BytePtr APP_NAME_BUF = BytePtr.allocateString(Arena.global(), "Project-WGX");
    private static final BytePtr ENGINE_NAME_BUF = BytePtr.allocateString(
            Arena.global(),
            "NG-DRCI-J : Neue Genesis Data-oriented Rendering and Computing Infrastructure for Java"
    );
    private static final BytePtr VALIDATION_LAYER_EXTENSION_BUF =
            BytePtr.allocateString(Arena.global(), VkConstants.EXT_DEBUG_UTILS_EXTENSION_NAME);
    private static final String VALIDATION_LAYER_NAME = "VK_LAYER_KHRONOS_validation";
    private static final BytePtr VALIDATION_LAYER_NAME_BUF =
            BytePtr.allocateString(Arena.global(), VALIDATION_LAYER_NAME);
    private static final BytePtr VK_SWAPCHAIN_EXTENSION_BUF =
            BytePtr.allocateString(Arena.global(), VkConstants.KHR_SWAPCHAIN_EXTENSION_NAME);

    private static final Logger logger = Logger.getLogger(VREContextInitialiser.class.getName());
}
