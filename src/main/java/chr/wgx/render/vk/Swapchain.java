package chr.wgx.render.vk;

import chr.wgx.config.Config;
import chr.wgx.render.RenderException;
import club.doki7.ffm.NativeLayout;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.ptr.IntPtr;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.*;
import club.doki7.vulkan.datatype.*;
import club.doki7.vulkan.enumtype.*;
import club.doki7.vulkan.handle.VkImage;
import club.doki7.vulkan.handle.VkSwapchainKHR;

import java.lang.foreign.Arena;
import java.util.Objects;

public final class Swapchain {
    public final @EnumType(VkFormat.class) int swapChainImageFormat;
    public final @EnumType(VkFormat.class) int depthFormat;
    public final VkExtent2D swapExtent;

    public final VkSwapchainKHR vkSwapchain;
    public final Resource.SwapchainImage[] swapchainImages;
    public final Resource.Image depthImage;

    public static Swapchain create(VulkanRenderEngineContext cx, int width, int height) throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            SwapchainSupportDetails swapchainSupportDetails = querySwapchainSupportDetails(cx, arena);

            VkSurfaceFormatKHR surfaceFormat = chooseSwapchainSurfaceFormat(swapchainSupportDetails.formats());
            @EnumType(VkPresentModeKHR.class) int presentMode = chooseSwapchainPresentMode(
                    swapchainSupportDetails.presentModes()
            );
            VkExtent2D extent = chooseSwapExtent(cx, swapchainSupportDetails.capabilities(), width, height);
            @EnumType(VkSurfaceTransformFlagsKHR.class) int currentTransform =
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
            VkImage.Ptr vkSwapchainImages = getSwapchainImages(cx, swapchain, arena);
            Resource.SwapchainImage[] swapchainImages = new Resource.SwapchainImage[(int) vkSwapchainImages.size()];
            for (int i = 0; i < vkSwapchainImages.size(); i++) {
                swapchainImages[i] = Resource.SwapchainImage.create(
                        cx,
                        Objects.requireNonNull(vkSwapchainImages.read(i)),
                        surfaceFormat.format()
                );
            }

            @EnumType(VkFormat.class) int depthFormat = findSupportedDepthFormat(cx);
            Resource.Image depthImage = Resource.Image.create(
                    cx,
                    extent.width(),
                    extent.height(),
                    1,
                    VkSampleCountFlags._1,
                    depthFormat,
                    VkImageTiling.OPTIMAL,
                    VkImageUsageFlags.DEPTH_STENCIL_ATTACHMENT,
                    VkImageAspectFlags.DEPTH
            );

            return new Swapchain(
                    surfaceFormat.format(),
                    depthFormat,
                    extent,
                    swapchain,
                    swapchainImages,
                    depthImage
            );
        }
    }

    public void dispose(VulkanRenderEngineContext cx) {
        for (Resource.SwapchainImage swapchainImage : swapchainImages) {
            swapchainImage.dispose(cx);
        }
        depthImage.dispose(cx);

        cx.dCmd.destroySwapchainKHR(cx.device, vkSwapchain, null);
    }

    private Swapchain(
            @EnumType(VkFormat.class) int swapChainImageFormat,
            @EnumType(VkFormat.class) int depthFormat,
            VkExtent2D swapExtent,
            VkSwapchainKHR swapchain,
            Resource.SwapchainImage[] swapchainImages,
            Resource.Image depthImage
    ) {
        this.swapChainImageFormat = swapChainImageFormat;
        this.depthFormat = depthFormat;
        this.swapExtent = swapExtent;
        this.vkSwapchain = swapchain;
        this.swapchainImages = swapchainImages;
        this.depthImage = depthImage;
    }

    private static VkSwapchainKHR createSwapchain(
            VulkanRenderEngineContext cx,
            Arena arena,
            VkSurfaceFormatKHR surfaceFormat,
            VkExtent2D extent,
            @EnumType(VkSurfaceTransformFlagsKHR.class) int currentTransform,
            int imageCount,
            @EnumType(VkPresentModeKHR.class) int presentMode
    ) throws RenderException {
        VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.allocate(arena);
        swapchainCreateInfo.surface(cx.surface);
        swapchainCreateInfo.minImageCount(imageCount);
        swapchainCreateInfo.imageFormat(surfaceFormat.format());
        swapchainCreateInfo.imageColorSpace(surfaceFormat.colorSpace());
        swapchainCreateInfo.imageExtent(extent);
        swapchainCreateInfo.imageArrayLayers(1);
        swapchainCreateInfo.imageUsage(VkImageUsageFlags.COLOR_ATTACHMENT);
        if (cx.graphicsQueueFamilyIndex != cx.presentQueueFamilyIndex) {
            swapchainCreateInfo.imageSharingMode(VkSharingMode.CONCURRENT);
            IntPtr pQueueFamilyIndices = IntPtr.allocate(arena, 2);
            pQueueFamilyIndices.write(0, cx.graphicsQueueFamilyIndex);
            pQueueFamilyIndices.write(1, cx.presentQueueFamilyIndex);
            swapchainCreateInfo.pQueueFamilyIndices(pQueueFamilyIndices);
        } else {
            swapchainCreateInfo.imageSharingMode(VkSharingMode.EXCLUSIVE);
        }
        swapchainCreateInfo.preTransform(currentTransform);
        swapchainCreateInfo.compositeAlpha(VkCompositeAlphaFlagsKHR.OPAQUE);
        swapchainCreateInfo.presentMode(presentMode);
        swapchainCreateInfo.clipped(VkConstants.TRUE);

        VkSwapchainKHR.Ptr pSwapchain = VkSwapchainKHR.Ptr.allocate(arena);
        @EnumType(VkResult.class) int result = cx.dCmd.createSwapchainKHR(
                cx.device,
                swapchainCreateInfo,
                null,
                pSwapchain
        );
        if (result != VkResult.SUCCESS) {
            throw new RenderException("无法创建 Vulkan 交换链, 错误代码: " + VkResult.explain(result));
        }
        return Objects.requireNonNull(pSwapchain.read());
    }

    private static VkImage.Ptr getSwapchainImages(
            VulkanRenderEngineContext cx,
            VkSwapchainKHR swapchain,
            Arena arena
    ) throws RenderException {
        try (Arena localArena = Arena.ofConfined()) {
            IntPtr pImageCount = IntPtr.allocate(localArena);
            @EnumType(VkResult.class) int result =
                    cx.dCmd.getSwapchainImagesKHR(cx.device, swapchain, pImageCount, null);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法获取 Vulkan 交换链图像, 错误代码: " + VkResult.explain(result));
            }

            int imageCount = pImageCount.read();
            VkImage.Ptr pImages = VkImage.Ptr.allocate(arena, imageCount);
            result = cx.dCmd.getSwapchainImagesKHR(cx.device, swapchain, pImageCount, pImages);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法获取 Vulkan 交换链图像, 错误代码: " + VkResult.explain(result));
            }
            return pImages;
        }
    }

    private record SwapchainSupportDetails(
            VkSurfaceCapabilitiesKHR capabilities,
            VkSurfaceFormatKHR.Ptr formats,
            @EnumType(VkPresentModeKHR.class) IntPtr presentModes
    ) {}

    private static SwapchainSupportDetails querySwapchainSupportDetails(
            VulkanRenderEngineContext cx,
            Arena arena
    ) throws RenderException {
        VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.allocate(arena);
        @EnumType(VkResult.class) int result = cx.iCmd.getPhysicalDeviceSurfaceCapabilitiesKHR(
                cx.physicalDevice,
                cx.surface,
                surfaceCapabilities
        );
        if (result != VkResult.SUCCESS) {
            throw new RenderException("无法获取 Vulkan 表面能力, 错误代码: " + VkResult.explain(result));
        }

        try (Arena localArena = Arena.ofConfined()) {
            IntPtr pCount = IntPtr.allocate(localArena);
            result = cx.iCmd.getPhysicalDeviceSurfaceFormatsKHR(cx.physicalDevice, cx.surface, pCount, null);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法获取 Vulkan 表面格式, 错误代码: " + VkResult.explain(result));
            }

            int formatCount = pCount.read();
            VkSurfaceFormatKHR.Ptr surfaceFormats = VkSurfaceFormatKHR.allocate(arena, formatCount);
            result = cx.iCmd.getPhysicalDeviceSurfaceFormatsKHR(
                    cx.physicalDevice,
                    cx.surface,
                    pCount,
                    surfaceFormats
            );
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法获取 Vulkan 表面格式, 错误代码: " + VkResult.explain(result));
            }

            result = cx.iCmd.getPhysicalDeviceSurfacePresentModesKHR(
                    cx.physicalDevice,
                    cx.surface,
                    pCount,
                    null
            );
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法获取 Vulkan 表面呈现模式, 错误代码: " + VkResult.explain(result));
            }

            int presentModeCount = pCount.read();
            IntPtr presentModes = IntPtr.allocate(arena, presentModeCount);
            result = cx.iCmd.getPhysicalDeviceSurfacePresentModesKHR(
                    cx.physicalDevice,
                    cx.surface,
                    pCount,
                    presentModes
            );
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法获取 Vulkan 表面呈现模式, 错误代码: " + VkResult.explain(result));
            }

            return new SwapchainSupportDetails(surfaceCapabilities, surfaceFormats, presentModes);
        }
    }

    private static VkSurfaceFormatKHR chooseSwapchainSurfaceFormat(VkSurfaceFormatKHR.Ptr formats) {
        @EnumType(VkFormat.class) int preferredFormat = VkFormat.B8G8R8A8_SRGB;
        for (VkSurfaceFormatKHR format : formats) {
            if (format.format() == preferredFormat &&
                format.colorSpace() == VkColorSpaceKHR.SRGB_NONLINEAR) {
                return format;
            }
        }
        return formats.at(0);
    }

    private static @EnumType(VkPresentModeKHR.class) int chooseSwapchainPresentMode(
            @EnumType(VkPresentModeKHR.class) IntPtr presentModes
    ) {
        int vsyncPreference = Config.config().vulkanConfig.vsync;

        if (vsyncPreference == 2) {
            return VkPresentModeKHR.FIFO;
        }

        for (int i = 0; i < presentModes.size(); i++) {
            @EnumType(VkPresentModeKHR.class) int presentMode = presentModes.read(i);
            if (presentMode == VkPresentModeKHR.MAILBOX) {
                return presentMode;
            }
        }

        if (vsyncPreference == 0) {
            for (int i = 0; i < presentModes.size(); i++) {
                @EnumType(VkPresentModeKHR.class) int presentMode = presentModes.read(i);
                if (presentMode == VkPresentModeKHR.IMMEDIATE) {
                    return presentMode;
                }
            }
        }

        return VkPresentModeKHR.FIFO;
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

    @EnumType(VkFormat.class) static final int[] CANDIDATE_DEPTH_FORMATS = new int[] {
            VkFormat.D32_SFLOAT,
            VkFormat.D32_SFLOAT_S8_UINT,
            VkFormat.D24_UNORM_S8_UINT
    };
    @EnumType(VkFormatFeatureFlags.class) static final int DEPTH_IMAGE_FEATURES =
            VkFormatFeatureFlags.DEPTH_STENCIL_ATTACHMENT;

    private static @EnumType(VkFormat.class) int findSupportedDepthFormat(
            VulkanRenderEngineContext cx
    ) throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            VkFormatProperties formatProperties = VkFormatProperties.allocate(arena);
            for (@EnumType(VkFormat.class) int format : CANDIDATE_DEPTH_FORMATS) {
                cx.iCmd.getPhysicalDeviceFormatProperties(cx.physicalDevice, format, formatProperties);
                if ((formatProperties.optimalTilingFeatures() & DEPTH_IMAGE_FEATURES) != 0) {
                    return format;
                }
            }
        }

        throw new RenderException("没有可用的深度缓冲格式");
    }
}
