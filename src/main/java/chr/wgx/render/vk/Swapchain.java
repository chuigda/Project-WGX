package chr.wgx.render.vk;

import chr.wgx.Config;
import chr.wgx.render.RenderException;
import tech.icey.panama.NativeLayout;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.VkCompositeAlphaFlagsKHR;
import tech.icey.vk4j.bitmask.VkImageUsageFlags;
import tech.icey.vk4j.bitmask.VkSurfaceTransformFlagsKHR;
import tech.icey.vk4j.datatype.VkExtent2D;
import tech.icey.vk4j.datatype.VkSurfaceCapabilitiesKHR;
import tech.icey.vk4j.datatype.VkSurfaceFormatKHR;
import tech.icey.vk4j.datatype.VkSwapchainCreateInfoKHR;
import tech.icey.vk4j.enumtype.*;
import tech.icey.vk4j.handle.VkImage;
import tech.icey.vk4j.handle.VkImageView;
import tech.icey.vk4j.handle.VkSwapchainKHR;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;

public final class Swapchain {
    public final @enumtype(VkFormat.class) int swapChainImageFormat;
    public final VkExtent2D swapExtent;

    public final VkSwapchainKHR swapchain;
    public final Resource.SwapchainImage[] swapchainImages;
    public final Resource.Image depthImage;
    public final Option<Resource.Image> msaaColorImage;

    public static Swapchain create(
            VulkanRenderEngineContext cx,
            int width,
            int height
    ) throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            SwapchainSupportDetails swapchainSupportDetails = querySwapchainSupportDetails(cx, arena);

            VkSurfaceFormatKHR surfaceFormat = chooseSwapchainSurfaceFormat(swapchainSupportDetails.formats());
            @enumtype(VkPresentModeKHR.class) int presentMode = chooseSwapchainPresentMode(
                    swapchainSupportDetails.presentModes()
            );
            VkExtent2D extent = chooseSwapExtent(cx, swapchainSupportDetails.capabilities(), width, height);
            @enumtype(VkSurfaceTransformFlagsKHR.class) int currentTransform =
                    swapchainSupportDetails.capabilities.currentTransform();

            int imageCount = swapchainSupportDetails.capabilities().minImageCount() + 1;
            if (swapchainSupportDetails.capabilities().maxImageCount() > 0 &&
                    imageCount > swapchainSupportDetails.capabilities().maxImageCount()) {
                imageCount = swapchainSupportDetails.capabilities().maxImageCount();
            }

            VkSwapchainKHR swapchain = createSwapchain(
                    cx,
                    arena,
                    surfaceFormat,
                    extent,
                    currentTransform, imageCount, presentMode);
            VkImage[] vkSwapchainImages = getSwapchainImages(cx, swapchain);
            Resource.SwapchainImage[] swapchainImages = new Resource.SwapchainImage[vkSwapchainImages.length];
            for (int i = 0; i < vkSwapchainImages.length; i++) {
                swapchainImages[i] = Resource.SwapchainImage.create(cx, vkSwapchainImages[i], surfaceFormat.format());
            }
        }
    }

    private Swapchain(
            @enumtype(VkFormat.class) int swapChainImageFormat,
            VkExtent2D swapExtent,
            VkSwapchainKHR swapchain,
            Resource.SwapchainImage[] swapchainImages,
            Resource.Image depthImage,
            Option<Resource.Image> msaaColorImage
    ) {
        this.swapChainImageFormat = swapChainImageFormat;
        this.swapExtent = swapExtent;
        this.swapchain = swapchain;
        this.swapchainImages = swapchainImages;
        this.depthImage = depthImage;
        this.msaaColorImage = msaaColorImage;
    }

    private static VkSwapchainKHR createSwapchain(
            VulkanRenderEngineContext cx,
            Arena arena,
            VkSurfaceFormatKHR surfaceFormat,
            VkExtent2D extent,
            @enumtype(VkSurfaceTransformFlagsKHR.class) int currentTransform,
            int imageCount,
            @enumtype(VkPresentModeKHR.class) int presentMode
    ) throws RenderException {
        VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.allocate(arena);
        swapchainCreateInfo.surface(cx.surface);
        swapchainCreateInfo.minImageCount(imageCount);
        swapchainCreateInfo.imageFormat(surfaceFormat.format());
        swapchainCreateInfo.imageColorSpace(surfaceFormat.colorSpace());
        swapchainCreateInfo.imageExtent(extent);
        swapchainCreateInfo.imageArrayLayers(1);
        swapchainCreateInfo.imageUsage(VkImageUsageFlags.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
        if (cx.graphicsQueueFamilyIndex != cx.presentQueueFamilyIndex) {
            swapchainCreateInfo.imageSharingMode(VkSharingMode.VK_SHARING_MODE_CONCURRENT);
            IntBuffer pQueueFamilyIndices = IntBuffer.allocate(arena, 2);
            pQueueFamilyIndices.write(0, cx.graphicsQueueFamilyIndex);
            pQueueFamilyIndices.write(1, cx.presentQueueFamilyIndex);
            swapchainCreateInfo.pQueueFamilyIndices(pQueueFamilyIndices);
        } else {
            swapchainCreateInfo.imageSharingMode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE);
        }
        swapchainCreateInfo.preTransform(currentTransform);
        swapchainCreateInfo.compositeAlpha(VkCompositeAlphaFlagsKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
        swapchainCreateInfo.presentMode(presentMode);
        swapchainCreateInfo.clipped(Constants.VK_TRUE);

        VkSwapchainKHR.Buffer pSwapchain = VkSwapchainKHR.Buffer.allocate(arena);
        @enumtype(VkResult.class) int result = cx.dCmd.vkCreateSwapchainKHR(
                cx.device,
                swapchainCreateInfo,
                null,
                pSwapchain
        );
        if (result != VkResult.VK_SUCCESS) {
            throw new RenderException("无法创建 Vulkan 交换链, 错误代码: " + VkResult.explain(result));
        }
        return pSwapchain.read();
    }

    private static VkImage[] getSwapchainImages(
            VulkanRenderEngineContext cx,
            VkSwapchainKHR swapchain
    ) throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            IntBuffer pImageCount = IntBuffer.allocate(arena);
            @enumtype(VkResult.class) int result =
                    cx.dCmd.vkGetSwapchainImagesKHR(cx.device, swapchain, pImageCount, null);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法获取 Vulkan 交换链图像, 错误代码: " + VkResult.explain(result));
            }

            int imageCount = pImageCount.read();
            VkImage.Buffer pImages = VkImage.Buffer.allocate(arena, imageCount);
            result = cx.dCmd.vkGetSwapchainImagesKHR(cx.device, swapchain, pImageCount, pImages);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法获取 Vulkan 交换链图像, 错误代码: " + VkResult.explain(result));
            }
            return pImages.readAll();
        }
    }

    private record SwapchainSupportDetails(
            VkSurfaceCapabilitiesKHR capabilities,
            VkSurfaceFormatKHR[] formats,
            @enumtype(VkPresentModeKHR.class) IntBuffer presentModes
    ) {}

    private static SwapchainSupportDetails querySwapchainSupportDetails(
            VulkanRenderEngineContext cx,
            Arena arena
    ) throws RenderException {
        VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.allocate(arena);
        @enumtype(VkResult.class) int result = cx.iCmd.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
                cx.physicalDevice,
                cx.surface,
                surfaceCapabilities
        );
        if (result != VkResult.VK_SUCCESS) {
            throw new RenderException("无法获取 Vulkan 表面能力, 错误代码: " + VkResult.explain(result));
        }

        try (Arena localArena = Arena.ofConfined()) {
            IntBuffer pCount = IntBuffer.allocate(localArena);
            result = cx.iCmd.vkGetPhysicalDeviceSurfaceFormatsKHR(cx.physicalDevice, cx.surface, pCount, null);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法获取 Vulkan 表面格式, 错误代码: " + VkResult.explain(result));
            }

            int formatCount = pCount.read();
            VkSurfaceFormatKHR[] surfaceFormats = VkSurfaceFormatKHR.allocate(arena, formatCount);
            result = cx.iCmd.vkGetPhysicalDeviceSurfaceFormatsKHR(
                    cx.physicalDevice,
                    cx.surface,
                    pCount,
                    surfaceFormats[0]
            );
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法获取 Vulkan 表面格式, 错误代码: " + VkResult.explain(result));
            }

            result = cx.iCmd.vkGetPhysicalDeviceSurfacePresentModesKHR(
                    cx.physicalDevice,
                    cx.surface,
                    pCount,
                    null
            );
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法获取 Vulkan 表面呈现模式, 错误代码: " + VkResult.explain(result));
            }

            int presentModeCount = pCount.read();
            IntBuffer presentModes = IntBuffer.allocate(arena, presentModeCount);
            result = cx.iCmd.vkGetPhysicalDeviceSurfacePresentModesKHR(
                    cx.physicalDevice,
                    cx.surface,
                    pCount,
                    presentModes
            );
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法获取 Vulkan 表面呈现模式, 错误代码: " + VkResult.explain(result));
            }

            return new SwapchainSupportDetails(surfaceCapabilities, surfaceFormats, presentModes);
        }
    }

    private static VkSurfaceFormatKHR chooseSwapchainSurfaceFormat(VkSurfaceFormatKHR[] formats) {
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

    private static @enumtype(VkPresentModeKHR.class) int chooseSwapchainPresentMode(
            @enumtype(VkPresentModeKHR.class) IntBuffer presentModes
    ) {
        int vsyncPreference = Config.config().vulkanConfig.vsync;

        if (vsyncPreference == 2) {
            return VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR;
        }

        for (int i = 0; i < presentModes.size(); i++) {
            @enumtype(VkPresentModeKHR.class) int presentMode = presentModes.read(i);
            if (presentMode == VkPresentModeKHR.VK_PRESENT_MODE_MAILBOX_KHR) {
                return presentMode;
            }
        }

        if (vsyncPreference == 0) {
            for (int i = 0; i < presentModes.size(); i++) {
                @enumtype(VkPresentModeKHR.class) int presentMode = presentModes.read(i);
                if (presentMode == VkPresentModeKHR.VK_PRESENT_MODE_IMMEDIATE_KHR) {
                    return presentMode;
                }
            }
        }

        return VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR;
    }

    private static VkExtent2D chooseSwapExtent(
            VulkanRenderEngineContext cx,
            VkSurfaceCapabilitiesKHR capabilities,
            int width,
            int height
    ) {
        VkExtent2D actualExtent = VkExtent2D.allocate(cx.autoArena);
        if (capabilities.currentExtent().width() != NativeLayout.UINT32_MAX) {
            actualExtent.segment().copyFrom(capabilities.currentExtent().segment());
        } else {
            actualExtent.width(Math.max(
                    capabilities.minImageExtent().width(),
                    Math.min(capabilities.maxImageExtent().width(), width)
            ));
            actualExtent.height(Math.max(
                    capabilities.minImageExtent().height(),
                    Math.min(capabilities.maxImageExtent().height(), height)
            ));
        }
        return actualExtent;
    }
}
