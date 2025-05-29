package chr.wgx.render.vk;

import chr.wgx.config.Config;
import chr.wgx.config.VulkanConfig;
import chr.wgx.render.RenderException;
import org.jetbrains.annotations.Nullable;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.annotation.Pointer;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.VkBufferUsageFlags;
import club.doki7.vulkan.bitmask.VkImageAspectFlags;
import club.doki7.vulkan.bitmask.VkImageUsageFlags;
import club.doki7.vulkan.bitmask.VkSampleCountFlags;
import club.doki7.vulkan.datatype.*;
import club.doki7.vulkan.enumtype.*;
import club.doki7.vulkan.handle.VkBuffer;
import club.doki7.vulkan.handle.VkImage;
import club.doki7.vulkan.handle.VkImageView;
import club.doki7.vulkan.handle.VkSampler;
import club.doki7.vma.bitmask.VmaAllocationCreateFlags;
import club.doki7.vma.datatype.VmaAllocationCreateInfo;
import club.doki7.vma.datatype.VmaAllocationInfo;
import club.doki7.vma.enumtype.VmaMemoryUsage;
import club.doki7.vma.handle.VmaAllocation;
import tech.icey.xjbutil.container.Pair;

import java.lang.foreign.Arena;
import java.util.Objects;

public final class Resource {
    public static final class Image implements IVkDisposable {
        public final VkImage image;
        public final VkImageView imageView;
        public final VmaAllocation allocation;

        private Image(
                VkImage image,
                VkImageView imageView,
                VmaAllocation allocation
        ) {
            this.image = image;
            this.imageView = imageView;
            this.allocation = allocation;
        }

        public static Image create(
                VulkanRenderEngineContext cx,
                int width,
                int height,
                int mipLevels,
                @EnumType(VkSampleCountFlags.class) int sampleCountFlags,
                @EnumType(VkFormat.class) int format,
                @EnumType(VkImageTiling.class) int tiling,
                @EnumType(VkImageUsageFlags.class) int usage,
                @EnumType(VkImageAspectFlags.class) int aspect
        ) throws RenderException {
            Pair<VkImage, VmaAllocation> pair = createImage(
                    cx,
                    width,
                    height,
                    mipLevels,
                    sampleCountFlags,
                    format,
                    tiling,
                    usage
            );
            VkImage image = pair.first();
            VmaAllocation allocation = pair.second();
            VkImageView imageView = createImageView(cx, image, format, aspect, mipLevels);

            return new Image(image, imageView, allocation);
        }

        @Override
        public void dispose(VulkanRenderEngineContext cx) {
            cx.dCmd.destroyImageView(cx.device, imageView, null);
            cx.vma.destroyImage(cx.vmaAllocator, image, allocation);
        }
    }

    public static final class Sampler implements IVkDisposable {
        public final VkSampler sampler;

        private Sampler(VkSampler sampler) {
            this.sampler = sampler;
        }

        public static Sampler create(VulkanRenderEngineContext cx, int mipLevels) throws RenderException {
            VulkanConfig config = Config.config().vulkanConfig;

            try (Arena arena = Arena.ofConfined()) {
                VkSamplerCreateInfo createInfo = VkSamplerCreateInfo.allocate(arena);
                createInfo.magFilter(VkFilter.LINEAR);
                createInfo.minFilter(VkFilter.LINEAR);
                createInfo.addressModeU(VkSamplerAddressMode.REPEAT);
                createInfo.addressModeV(VkSamplerAddressMode.REPEAT);
                createInfo.addressModeW(VkSamplerAddressMode.REPEAT);
                createInfo.anisotropyEnable(config.enableAnisotropy ? VkConstants.TRUE : VkConstants.FALSE);
                createInfo.maxAnisotropy(config.anisotropyLevel);
                createInfo.borderColor(VkBorderColor.INT_OPAQUE_BLACK);
                createInfo.unnormalizedCoordinates(VkConstants.FALSE);
                createInfo.compareEnable(VkConstants.FALSE);
                createInfo.compareOp(VkCompareOp.ALWAYS);
                createInfo.mipmapMode(VkSamplerMipmapMode.LINEAR);
                createInfo.mipLodBias(0);
                createInfo.minLod(0);
                createInfo.maxLod(mipLevels);

                VkSampler.Ptr pSampler = VkSampler.Ptr.allocate(arena);
                @EnumType(VkResult.class) int result = cx.dCmd.createSampler(cx.device, createInfo, null, pSampler);
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("无法创建 Vulkan 采样器, 错误代码: " + result);
                }

                return new Sampler(Objects.requireNonNull(pSampler.read()));
            }
        }

        @Override
        public void dispose(VulkanRenderEngineContext cx) {
            cx.dCmd.destroySampler(cx.device, sampler, null);
        }
    }

    public static final class SwapchainImage implements IVkDisposable {
        public final VkImage image;
        public final VkImageView imageView;

        private SwapchainImage(VkImage image, VkImageView imageView) {
            this.image = image;
            this.imageView = imageView;
        }

        @Override
        public void dispose(VulkanRenderEngineContext cx) {
            cx.dCmd.destroyImageView(cx.device, imageView, null);
        }

        public static SwapchainImage create(
                VulkanRenderEngineContext cx,
                VkImage image,
                @EnumType(VkFormat.class) int format
        ) throws RenderException {
            VkImageView imageView = createImageView(cx, image, format, VkImageAspectFlags.COLOR, 1);
            return new SwapchainImage(image, imageView);
        }
    }

    public static final class Buffer implements IVkDisposable {
        public final VkBuffer buffer;
        public final VmaAllocation allocation;

        private Buffer(VkBuffer buffer, VmaAllocation allocation) {
            this.buffer = buffer;
            this.allocation = allocation;
        }

        @Override
        public void dispose(VulkanRenderEngineContext cx) {
            cx.vma.destroyBuffer(cx.vmaAllocator, buffer, allocation);
        }

        public static Buffer create(
                VulkanRenderEngineContext cx,
                long size,
                @EnumType(VkBufferUsageFlags.class) int usage,
                @EnumType(VmaAllocationCreateFlags.class) int allocationFlags,
                @Nullable @Pointer VmaAllocationInfo allocationInfo
        ) throws RenderException {
            try (Arena arena = Arena.ofConfined()) {
                VkBufferCreateInfo createInfo = VkBufferCreateInfo.allocate(arena);
                createInfo.size(size);
                createInfo.usage(usage);
                createInfo.sharingMode(VkSharingMode.EXCLUSIVE);

                VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.allocate(arena);
                allocationCreateInfo.usage(VmaMemoryUsage.AUTO);
                allocationCreateInfo.flags(allocationFlags);

                VkBuffer.Ptr pBuffer = VkBuffer.Ptr.allocate(arena);
                VmaAllocation.Ptr pAllocation = VmaAllocation.Ptr.allocate(arena);
                @EnumType(VkResult.class) int result = cx.vma.createBuffer(
                        cx.vmaAllocator,
                        createInfo,
                        allocationCreateInfo,
                        pBuffer,
                        pAllocation,
                        allocationInfo
                );
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("无法分配 Vulkan 缓冲区, 错误代码: " + result);
                }

                return new Buffer(
                        Objects.requireNonNull(pBuffer.read()),
                        Objects.requireNonNull(pAllocation.read())
                );
            }
        }
    }

    private static Pair<VkImage, VmaAllocation> createImage(
            VulkanRenderEngineContext cx,
            int width,
            int height,
            int mipLevels,
            @EnumType(VkSampleCountFlags.class) int sampleCountFlags,
            @EnumType(VkFormat.class) int format,
            @EnumType(VkImageTiling.class) int tiling,
            @EnumType(VkImageUsageFlags.class) int usage
    ) throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            VkImageCreateInfo createInfo = VkImageCreateInfo.allocate(arena);
            createInfo.imageType(VkImageType._2D);
            createInfo.extent().width(width);
            createInfo.extent().height(height);
            createInfo.extent().depth(1);
            createInfo.mipLevels(mipLevels);
            createInfo.arrayLayers(1);
            createInfo.format(format);
            createInfo.tiling(tiling);
            createInfo.usage(usage);
            createInfo.samples(sampleCountFlags);
            createInfo.sharingMode(VkSharingMode.EXCLUSIVE);

            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.allocate(arena);
            allocationCreateInfo.usage(VmaMemoryUsage.GPU_ONLY);

            VkImage.Ptr pImage = VkImage.Ptr.allocate(arena);
            VmaAllocation.Ptr pAllocation = VmaAllocation.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result = cx.vma.createImage(
                    cx.vmaAllocator,
                    createInfo,
                    allocationCreateInfo,
                    pImage,
                    pAllocation,
                    null
            );
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建 Vulkan 图像, 错误代码: " + VkResult.explain(result));
            }

            return new Pair<>(pImage.read(), pAllocation.read());
        }
    }

    private static VkImageView createImageView(
            VulkanRenderEngineContext cx,
            VkImage image,
            @EnumType(VkFormat.class) int format,
            @EnumType(VkImageAspectFlags.class) int aspect,
            int mipLevels
    ) throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.allocate(arena);
            createInfo.image(image);
            createInfo.viewType(VkImageViewType._2D);
            createInfo.format(format);

            VkImageSubresourceRange subresourceRange = createInfo.subresourceRange();
            subresourceRange.aspectMask(aspect);
            subresourceRange.baseMipLevel(0);
            subresourceRange.levelCount(mipLevels);
            subresourceRange.baseArrayLayer(0);
            subresourceRange.layerCount(1);

            VkImageView.Ptr pImageView = VkImageView.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result = cx.dCmd.createImageView(cx.device, createInfo, null, pImageView);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建 Vulkan 图像视图, 错误代码: " + result);
            }
            return Objects.requireNonNull(pImageView.read());
        }
    }
}
