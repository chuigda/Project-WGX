package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.vk4j.command.DeviceCommands;
import tech.icey.vk4j.command.EntryCommands;
import tech.icey.vk4j.command.InstanceCommands;
import tech.icey.vk4j.command.StaticCommands;
import tech.icey.vk4j.handle.*;
import tech.icey.vma.VMA;
import tech.icey.vma.handle.VmaAllocator;
import tech.icey.xjbutil.container.Option;

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
    public final VkSemaphore[] imageAvailableSemaphores;
    public final VkSemaphore[] renderFinishedSemaphores;
    public final VkFence[] inFlightFences;
    public final VkCommandPool commandPool;
    public final VkCommandBuffer[] commandBuffers;
    public final Option<VkCommandPool> transferCommandPool;

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
            VmaAllocator vmaAllocator,
            VkSemaphore[] imageAvailableSemaphores,
            VkSemaphore[] renderFinishedSemaphores,
            VkFence[] inFlightFences,
            VkCommandPool commandPool,
            VkCommandBuffer[] commandBuffers,
            Option<VkCommandPool> transferCommandPool
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
        this.imageAvailableSemaphores = imageAvailableSemaphores;
        this.renderFinishedSemaphores = renderFinishedSemaphores;
        this.inFlightFences = inFlightFences;
        this.commandPool = commandPool;
        this.commandBuffers = commandBuffers;
        this.transferCommandPool = transferCommandPool;
    }

    public static VulkanRenderEngineState init(GLFW glfw, GLFWwindow window) throws RenderException {
        return new VREStateInitialiser().init(glfw, window);
    }
}
