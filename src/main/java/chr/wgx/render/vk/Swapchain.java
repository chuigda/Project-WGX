package chr.wgx.render.vk;

import chr.wgx.config.Config;
import chr.wgx.render.RenderException;
import tech.icey.panama.NativeLayout;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.*;
import tech.icey.vk4j.datatype.*;
import tech.icey.vk4j.enumtype.*;
import tech.icey.vk4j.handle.VkImage;
import tech.icey.vk4j.handle.VkSwapchainKHR;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;

public final class Swapchain {
    public final @enumtype(VkFormat.class) int swapChainImageFormat;
    public final @enumtype(VkFormat.class) int depthFormat;
    public final VkExtent2D swapExtent;

    public final VkSwapchainKHR vkSwapchain;
    public final Resource.SwapchainImage[] swapchainImages;
    public final Resource.Image depthImage;
    public final Option<Resource.Image> msaaColorImage;

    public static Swapchain create(VulkanRenderEngineContext cx, int width, int height) throws RenderException {
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

            @enumtype(VkFormat.class) int depthFormat = findSupportedDepthFormat(cx);
            Resource.Image depthImage = Resource.Image.create(
                    cx,
                    extent.width(),
                    extent.height(),
                    1,
                    cx.msaaSampleCountFlags,
                    depthFormat,
                    VkImageTiling.VK_IMAGE_TILING_OPTIMAL,
                    VkImageUsageFlags.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                    VkImageAspectFlags.VK_IMAGE_ASPECT_DEPTH_BIT
            );

            Option<Resource.Image> msaaColorImage;
            if (cx.msaaSampleCountFlags != VkSampleCountFlags.VK_SAMPLE_COUNT_1_BIT) {
                msaaColorImage = Option.some(Resource.Image.create(
                        cx,
                        extent.width(),
                        extent.height(),
                        1,
                        cx.msaaSampleCountFlags,
                        surfaceFormat.format(),
                        VkImageTiling.VK_IMAGE_TILING_OPTIMAL,
                        VkImageUsageFlags.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
                        VkImageAspectFlags.VK_IMAGE_ASPECT_COLOR_BIT
                ));
            } else {
                msaaColorImage = Option.none();
            }

            return new Swapchain(
                    surfaceFormat.format(),
                    depthFormat,
                    extent,
                    swapchain,
                    swapchainImages,
                    depthImage,
                    msaaColorImage
            );
        }
    }

    public void dispose(VulkanRenderEngineContext cx) {
        for (Resource.SwapchainImage swapchainImage : swapchainImages) {
            swapchainImage.dispose(cx);
        }
        depthImage.dispose(cx);
        if (msaaColorImage instanceof Option.Some<Resource.Image> someImage) {
            someImage.value.dispose(cx);
        }
        cx.dCmd.vkDestroySwapchainKHR(cx.device, vkSwapchain, null);
    }

    private Swapchain(
            @enumtype(VkFormat.class) int swapChainImageFormat,
            @enumtype(VkFormat.class) int depthFormat,
            VkExtent2D swapExtent,
            VkSwapchainKHR swapchain,
            Resource.SwapchainImage[] swapchainImages,
            Resource.Image depthImage,
            Option<Resource.Image> msaaColorImage
    ) {
        this.swapChainImageFormat = swapChainImageFormat;
        this.depthFormat = depthFormat;
        this.swapExtent = swapExtent;
        this.vkSwapchain = swapchain;
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
        VkExtent2D actualExtent = VkExtent2D.allocate(cx.prefabArena);
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

    @enumtype(VkFormat.class) static final int[] CANDIDATE_DEPTH_FORMATS = new int[] {
            VkFormat.VK_FORMAT_D32_SFLOAT,
            VkFormat.VK_FORMAT_D32_SFLOAT_S8_UINT,
            VkFormat.VK_FORMAT_D24_UNORM_S8_UINT
    };
    @enumtype(VkFormatFeatureFlags.class) static final int DEPTH_IMAGE_FEATURES =
            VkFormatFeatureFlags.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT;

    private static @enumtype(VkFormat.class) int findSupportedDepthFormat(
            VulkanRenderEngineContext cx
    ) throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            VkFormatProperties formatProperties = VkFormatProperties.allocate(arena);
            for (@enumtype(VkFormat.class) int format : CANDIDATE_DEPTH_FORMATS) {
                cx.iCmd.vkGetPhysicalDeviceFormatProperties(cx.physicalDevice, format, formatProperties);
                if ((formatProperties.optimalTilingFeatures() & DEPTH_IMAGE_FEATURES) != 0) {
                    return format;
                }
            }
        }

        throw new RenderException("没有可用的深度缓冲格式");
    }
}
