package tech.icey.r77.vk;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import tech.icey.util.ManualDispose;
import tech.icey.util.Optional;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK11.*;
import static tech.icey.util.RuntimeError.runtimeError;


public final class Swapchain implements ManualDispose {
    public Swapchain(
            Device device,
            Surface surface,
            VkWindow vkWindow,
            int requestedImages,
            boolean vsync,
            PresentQueue presentQueue,
            Optional<Queue[]> concurrentQueues
    ) {
        this.device = device;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PhysicalDevice physicalDevice = device.physicalDevice();

            VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
            int ret = KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
                    device.physicalDevice().vkPhysicalDevice,
                    surface.vkSurface(),
                    surfaceCapabilities
            );
            if (ret != VK_SUCCESS) {
                runtimeError("获取表面能力失败: %d", ret);
            }

            this.numImages = calcNumImages(surfaceCapabilities, requestedImages);
            this.surfaceFormat = calcSurfaceFormat(physicalDevice, surface);
            this.swapchainExtent = calcSwapchainExtent(vkWindow, surfaceCapabilities);
            this.vsync = vsync;

            VkSwapchainCreateInfoKHR vkSwapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                    .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(surface.vkSurface())
                    .minImageCount(this.numImages)
                    .imageFormat(this.surfaceFormat.imageFormat)
                    .imageColorSpace(this.surfaceFormat.colorSpace)
                    .imageExtent(swapchainExtent)
                    .imageArrayLayers(1)
                    .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                    .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
                    .preTransform(surfaceCapabilities.currentTransform())
                    .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .clipped(true);

            if (vsync) {
                vkSwapchainCreateInfo.presentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR);
            } else {
                vkSwapchainCreateInfo.presentMode(KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR);
            }

            int numQueues;
            List<Integer> queueIndices = new ArrayList<>();
            if (concurrentQueues instanceof Optional.Some<Queue[]> someConcurrentQueues) {
                numQueues = someConcurrentQueues.value.length;
                for (int i = 0; i < numQueues; i++) {
                    Queue queue = someConcurrentQueues.value[i];
                    if (queue.queueFamilyIndex != presentQueue.queueFamilyIndex) {
                        queueIndices.add(queue.queueFamilyIndex);
                    }
                }
            } else {
                numQueues = 1;
            }

            if (!queueIndices.isEmpty()) {
                IntBuffer queueFamilyIndices = stack.mallocInt(queueIndices.size() + 1);
                for (int i = 0; i < numQueues; i++) {
                    queueFamilyIndices.put(queueIndices.get(i));
                }
                queueFamilyIndices.put(presentQueue.queueFamilyIndex);
                queueFamilyIndices.rewind();

                vkSwapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT)
                        .queueFamilyIndexCount(queueFamilyIndices.capacity())
                        .pQueueFamilyIndices(queueFamilyIndices);
            } else {
                vkSwapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            LongBuffer swapchainBuf = stack.mallocLong(1);
            ret = KHRSwapchain.vkCreateSwapchainKHR(device.vkDevice(), vkSwapchainCreateInfo, null, swapchainBuf);
            if (ret != VK_SUCCESS) {
                runtimeError("创建交换链失败: %d", ret);
            }
            this.vkSwapchain = swapchainBuf.get(0);
            this.imageViews = createImageViews(stack, device, vkSwapchain, surfaceFormat.imageFormat);

            this.syncSemaphores = new SyncSemaphores[numImages];
            for (int i = 0; i < numImages; i++) {
                syncSemaphores[i] = new SyncSemaphores(device);
            }
        }
    }

    public boolean acquireNextImage() {
        assert !isDisposed;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer imageIndexBuf = stack.mallocInt(1);
            int ret = KHRSwapchain.vkAcquireNextImageKHR(
                    device.vkDevice(),
                    vkSwapchain,
                    Long.MAX_VALUE,
                    syncSemaphores[currentFrame].imgAcquisitionSemaphore.vkSemaphore(),
                    VK_NULL_HANDLE,
                    imageIndexBuf
            );
            if (ret == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                return true;
            } else if (ret != VK_SUCCESS && ret != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                runtimeError("无法获取交换链图像: %d", ret);
            }

            currentFrame = imageIndexBuf.get(0);
            return false;
        }
    }

    public boolean presentImage(Queue queue) {
        assert !isDisposed;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack)
                    .sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pWaitSemaphores(stack.longs(syncSemaphores[currentFrame].renderCompleteSemaphore.vkSemaphore()))
                    .swapchainCount(1)
                    .pSwapchains(stack.longs(vkSwapchain))
                    .pImageIndices(stack.ints(currentFrame));

            int ret = KHRSwapchain.vkQueuePresentKHR(queue.vkQueue, presentInfo);
            if (ret == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || ret == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                return true;
            } else if (ret != VK_SUCCESS) {
                runtimeError("无法呈现图像: %d", ret);
            }

            currentFrame = (currentFrame + 1) % numImages;
            return false;
        }
    }

    public Device device() {
        assert !isDisposed;
        return device;
    }

    public int numImages() {
        assert !isDisposed;
        return numImages;
    }

    public SurfaceFormat surfaceFormat() {
        assert !isDisposed;
        return surfaceFormat;
    }

    public VkExtent2D swapchainExtent() {
        assert !isDisposed;
        return swapchainExtent;
    }

    public boolean vsync() {
        assert !isDisposed;
        return vsync;
    }

    public long vkSwapchain() {
        assert !isDisposed;
        return vkSwapchain;
    }

    public ImageView[] imageViews() {
        assert !isDisposed;
        return imageViews;
    }

    @Override
    public boolean isManuallyDisposed() {
        return isDisposed;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            for (ImageView imageView : imageViews) {
                imageView.dispose();
            }
            KHRSwapchain.vkDestroySwapchainKHR(device.vkDevice(), vkSwapchain, null);
            isDisposed = true;
        }
    }

    private int calcNumImages(VkSurfaceCapabilitiesKHR surfaceCapabilities, int requestedImages) {
        int maxImages = surfaceCapabilities.maxImageCount();
        int minImages = surfaceCapabilities.minImageCount();
        int result = minImages;
        if (maxImages != 0) {
            result = Math.min(requestedImages, maxImages);
        }
        result = Math.max(result, minImages);
        return result;
    }

    private SurfaceFormat calcSurfaceFormat(PhysicalDevice physicalDevice, Surface surface) {
        int imageFormat;
        int colorSpace;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buf = stack.mallocInt(1);
            int ret = KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(
                    physicalDevice.vkPhysicalDevice,
                    surface.vkSurface(),
                    buf,
                    null
            );
            if (ret != VK_SUCCESS) {
                runtimeError("无法获取物理设备支持的表面格式");
            }

            int numFormats = buf.get(0);
            if (numFormats < 0) {
                runtimeError("物理设备似乎不支持任何表面格式");
            }

            VkSurfaceFormatKHR.Buffer surfaceFormats = VkSurfaceFormatKHR.calloc(numFormats, stack);
            ret = KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(
                    physicalDevice.vkPhysicalDevice,
                    surface.vkSurface(),
                    buf,
                    surfaceFormats
            );
            if (ret != VK_SUCCESS) {
                runtimeError("无法获取物理设备支持的表面格式");
            }

            imageFormat = VK_FORMAT_R8G8B8A8_SRGB;
            colorSpace = surfaceFormats.get(0).colorSpace();
            for (int i = 0; i < numFormats; i++) {
                VkSurfaceFormatKHR surfaceFormat = surfaceFormats.get(i);
                if (surfaceFormat.format() == VK_FORMAT_R8G8B8A8_SRGB &&
                        surfaceFormat.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                    imageFormat = surfaceFormat.format();
                    colorSpace = surfaceFormat.colorSpace();
                    break;
                }
            }

            return new SurfaceFormat(imageFormat, colorSpace);
        }
    }

    private VkExtent2D calcSwapchainExtent(VkWindow window, VkSurfaceCapabilitiesKHR surfaceCapabilities) {
        VkExtent2D result = VkExtent2D.calloc();

        if (surfaceCapabilities.currentExtent().width() == 0xFFFFFFFF) {
            int width = Math.min(window.getWidth(), surfaceCapabilities.maxImageExtent().width());
            width = Math.max(width, surfaceCapabilities.minImageExtent().width());

            int height = Math.min(window.getHeight(), surfaceCapabilities.maxImageExtent().height());
            height = Math.max(height, surfaceCapabilities.minImageExtent().height());

            result.width(width);
            result.height(height);
        } else {
            result.set(surfaceCapabilities.currentExtent());
        }

        return result;
    }

    private ImageView[] createImageViews(MemoryStack stack, Device device, long swapchain, int format) {
        IntBuffer numImagesBuf = stack.mallocInt(1);
        int ret = KHRSwapchain.vkGetSwapchainImagesKHR(device.vkDevice(), swapchain, numImagesBuf, null);
        if (ret != VK_SUCCESS) {
            runtimeError("无法获取交换链图像: %d", ret);
        }
        int numImages = numImagesBuf.get(0);

        LongBuffer imagesBuf = stack.mallocLong(numImages);
        ret = KHRSwapchain.vkGetSwapchainImagesKHR(device.vkDevice(), swapchain, numImagesBuf, imagesBuf);
        if (ret != VK_SUCCESS) {
            runtimeError("无法获取交换链图像: %d", ret);
        }

        ImageView[] result = new ImageView[numImages];
        ImageViewData imageViewData = new ImageViewData()
                .format(format)
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        for (int i = 0; i < numImages; i++) {
            result[i] = new ImageView(device, imagesBuf.get(i), imageViewData);
        }
        return result;
    }

    private final Device device;
    private final int numImages;
    private final SurfaceFormat surfaceFormat;
    private final VkExtent2D swapchainExtent;
    private final boolean vsync;
    private final long vkSwapchain;
    private final ImageView[] imageViews;

    private final class SyncSemaphores implements ManualDispose {
        SyncSemaphores(Semaphore imgAcquisitionSemaphore, Semaphore renderCompleteSemaphore) {
            this.imgAcquisitionSemaphore = imgAcquisitionSemaphore;
            this.renderCompleteSemaphore = renderCompleteSemaphore;
        }

        SyncSemaphores(Device device) {
            this(new Semaphore(device), new Semaphore(device));
        }

        @Override
        public boolean isManuallyDisposed() {
            return imgAcquisitionSemaphore.isManuallyDisposed() && renderCompleteSemaphore.isManuallyDisposed();
        }

        @Override
        public void dispose() {
            if (isManuallyDisposed()) {
                return;
            }

            imgAcquisitionSemaphore.dispose();
            renderCompleteSemaphore.dispose();
        }

        private final Semaphore imgAcquisitionSemaphore, renderCompleteSemaphore;
    }

    private final SyncSemaphores[] syncSemaphores;
    private int currentFrame = 0;

    private volatile boolean isDisposed = false;
}
