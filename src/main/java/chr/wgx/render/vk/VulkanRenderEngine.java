package chr.wgx.render.vk;

import chr.wgx.config.Config;
import chr.wgx.render.IRenderEngineFactory;
import chr.wgx.render.RenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.common.Color;
import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.data.*;
import chr.wgx.render.info.*;
import chr.wgx.render.task.RenderPass;
import chr.wgx.render.vk.compiled.CompiledRenderPassOp;
import chr.wgx.render.vk.data.*;
import chr.wgx.render.vk.task.VulkanRenderPass;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.NativeLayout;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.*;
import tech.icey.vk4j.datatype.*;
import tech.icey.vk4j.enumtype.*;
import tech.icey.vk4j.handle.*;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.container.Ref;

import java.lang.foreign.Arena;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class VulkanRenderEngine extends RenderEngine {
    VulkanRenderEngine(GLFW glfw, GLFWwindow window) throws RenderException {
        cx = VulkanRenderEngineContext.create(glfw, window);
        try (Arena arena = Arena.ofConfined()) {
            IntBuffer pWidthHeight = IntBuffer.allocate(arena, 2);
            glfw.glfwGetFramebufferSize(window, pWidthHeight, pWidthHeight.offset(1));

            int width = pWidthHeight.read(0);
            int height = pWidthHeight.read(1);
            swapchain = Swapchain.create(cx, width, height);
            logger.info("交换链已创建, 图像数量=" + swapchain.swapchainImages.length);
        }

        AttachmentCreateInfo pseudoColorAttachmentInfo = new AttachmentCreateInfo(PixelFormat.RGBA8888_FLOAT, -1, -1);
        AttachmentCreateInfo pseudoDepthAttachmentInfo = new AttachmentCreateInfo(PixelFormat.DEPTH_BUFFER_OPTIMAL, -1, -1);

        swapchainColorAttachment = new VulkanSwapchainAttachment(
                pseudoColorAttachmentInfo,
                swapchain.swapchainImages[0],
                swapchain.msaaColorImage
        );
        swapchainDepthAttachment = new VulkanImageAttachment(
                pseudoDepthAttachmentInfo,
                new Ref<>(swapchain.depthImage)
        );

        objectCreateAspect = new ASPECT_ObjectCreate(this);
        attachmentCreateAspect = new ASPECT_AttachmentCreate(this);
        uniformCreateAspect = new ASPECT_UniformCreate(this);
        pushConstantCreateAspect = new ASPECT_PushConstantCreate(this);
        textureCreateAspect = new ASPECT_TextureCreate(this);
        descriptorSetLayoutCreateAspect = new ASPECT_DescriptorSetLayoutCreate(this);
        descriptorSetCreateAspect = new ASPECT_DescriptorSetCreate(this);
        pipelineCreateAspect = new ASPECT_PipelineCreate(this);
        renderPassCompilationAspect = new ASPECT_RenderPassCompilation(this);
        renderFrameAspect = new ASPECT_RenderFrame(this);
    }

    @Override
    protected void resize(int width, int height) throws RenderException {
        if (width == 0 || height == 0) {
            pauseRender = true;
            return;
        }
        pauseRender = false;

        cx.waitDeviceIdle();
        swapchain.dispose(cx);

        try {
            swapchain = Swapchain.create(cx, width, height);
            logger.info("交换链已重新创建");
        } catch (RenderException e) {
            logger.severe("无法重新创建交换链: " + e.getMessage());
            throw e;
        }

        swapchainColorAttachment.msaaColorImage = swapchain.msaaColorImage;
        swapchainDepthAttachment.image.value = swapchain.depthImage;

        List<Ref<Resource.Image>> updatedImages = new ArrayList<>();

        for (VulkanImageAttachment attachment : colorAttachments) {
            if (attachment.createInfo.width != -1) {
                continue;
            }

            attachment.image.value.dispose(cx);
            attachment.image.value = attachmentCreateAspect.createColorAttachmentImage(
                    attachment.createInfo
            );
            updatedImages.add(attachment.image);
        }

        for (VulkanImageAttachment attachment : depthAttachments) {
            if (attachment.createInfo.width != -1) {
                continue;
            }

            attachment.image.value.dispose(cx);
            attachment.image.value = attachmentCreateAspect.createDepthAttachmentImage(
                    attachment.createInfo
            );
            updatedImages.add(attachment.image);
        }

        synchronized (this.imageDescriptorSetUsage) {
            for (Ref<Resource.Image> image : updatedImages) {
                Set<ImageDescriptorUsage> usages = this.imageDescriptorSetUsage.get(image);
                if (usages == null) {
                    continue;
                }

                descriptorSetCreateAspect.updateDescriptorSetItem(usages);
            }
        }
    }

    @Override
    protected void renderFrame() throws RenderException {
        if (pauseRender) {
            return;
        }

        boolean recompile = renderPassNeedCompilation.getAndSet(false);
        boolean updateUniform = uniformManuallyUpdated.getAndSet(false);

        if (recompile || updateUniform) {
            cx.waitDeviceIdle();

            if (recompile) {
                logger.info("正在开始重新编译渲染通道");
                long startTime = System.nanoTime();
                renderPassCompilationAspect.recompileRenderPasses();
                long endTime = System.nanoTime();
                logger.info("已重新编译渲染通道, 共耗时 %d 微秒".formatted((endTime - startTime) / 1_000));
            }

            if (updateUniform) {
                logger.info("正在更新标记为手动更新的 uniform 缓冲区");
                long startTime = System.nanoTime();
                for (VulkanUniformBuffer uniform : manuallyUpdatedUniforms) {
                    uniform.updateGPU();
                }
                long endTime = System.nanoTime();
                logger.info("已更新标记为手动更新的 uniform 缓冲区, 共耗时 %d 微秒".formatted((endTime - startTime) / 1_000));
            }
        }

        renderFrameAspect.renderFrameImpl(currentFrameIndex);

        currentFrameIndex = (currentFrameIndex + 1) % Config.config().vulkanConfig.maxFramesInFlight;
    }

    @Override
    protected void close() {
        cx.waitDeviceIdle();
        swapchain.dispose(cx);

        for (VulkanRenderPipeline pipeline : pipelines) {
            pipeline.dispose(cx);
        }

        for (VulkanRenderObject object : objects) {
            object.dispose(cx);
        }

        for (VulkanImageAttachment attachment : colorAttachments) {
            attachment.dispose(cx);
        }

        for (VulkanImageAttachment attachment : depthAttachments) {
            attachment.dispose(cx);
        }

        for (CombinedImageSampler texture : textures) {
            texture.dispose(cx);
        }

        for (VulkanUniformBuffer uniform : perFrameUpdatedUniforms) {
            uniform.dispose(cx);
        }

        for (VulkanUniformBuffer uniform : manuallyUpdatedUniforms) {
            uniform.dispose(cx);
        }

        for (Map.Entry<VulkanDescriptorSetLayout, VkDescriptorPool> entry : descriptorPools.entrySet()) {
            cx.dCmd.vkDestroyDescriptorPool(cx.device, entry.getValue(), null);
            entry.getKey().dispose(cx);
        }

        cx.dispose();
    }

    @Override
    public RenderObject createObject(ObjectCreateInfo info) throws RenderException {
        return createObject(List.of(info)).getFirst();
    }

    @Override
    public List<RenderObject> createObject(List<ObjectCreateInfo> infos) throws RenderException {
        return objectCreateAspect.createObjectImpl(infos);
    }

    @Override
    public Pair<Attachment, Texture> createColorAttachment(AttachmentCreateInfo info) throws RenderException {
        return attachmentCreateAspect.createColorAttachmentImpl(info);
    }

    @Override
    public Attachment createDepthAttachment(AttachmentCreateInfo info) throws RenderException {
        return attachmentCreateAspect.createDepthAttachmentImpl(info);
    }

    @Override
    public Pair<Attachment, Attachment> getDefaultAttachments() {
        return new Pair<>(swapchainColorAttachment, swapchainDepthAttachment);
    }

    @Override
    public Texture createTexture(TextureCreateInfo image) throws RenderException {
        return createTexture(List.of(image)).getFirst();
    }

    @Override
    public List<Texture> createTexture(List<TextureCreateInfo> images) throws RenderException {
        return textureCreateAspect.createTextureImpl(images);
    }

    @Override
    public UniformBuffer createUniform(UniformBufferCreateInfo info) throws RenderException {
        return uniformCreateAspect.createUniformImpl(info);
    }

    @Override
    public List<PushConstant> createPushConstant(PushConstantInfo info, int count) {
        return pushConstantCreateAspect.createPushConstantImpl(info, count);
    }

    @Override
    public DescriptorSetLayout createDescriptorSetLayout(
            DescriptorSetLayoutCreateInfo info,
            int maxSets
    ) throws RenderException {
        return descriptorSetLayoutCreateAspect.createDescriptorSetLayoutImpl(info, maxSets);
    }

    @Override
    public DescriptorSet createDescriptorSet(DescriptorSetCreateInfo info) throws RenderException {
        return descriptorSetCreateAspect.createDescriptorSetImpl(info);
    }

    @Override
    public RenderPipeline createPipeline(RenderPipelineCreateInfo info) throws RenderException {
        return pipelineCreateAspect.createPipelineImpl(info);
    }

    @Override
    public RenderPass createRenderPass(
            String renderPassName,
            int priority,
            List<Attachment> colorAttachments,
            List<Color> clearColors,
            Option<Attachment> depthAttachment
    ) {
        VulkanRenderPass ret = new VulkanRenderPass(
                renderPassName,
                priority,
                clearColors,
                colorAttachments.stream().map(attachment -> (VulkanAttachment) attachment).toList(),
                depthAttachment.map(attachment -> (VulkanImageAttachment) attachment),
                this.cx.prefabArena,
                this.renderPassNeedCompilation
        );
        this.renderPasses.add(ret);
        this.renderPassNeedCompilation.set(true);
        return ret;
    }

    public static final IRenderEngineFactory FACTORY = new VulkanRenderEngineFactory();

    private void resetAndRecordCommandBuffer(
            VkCommandBuffer commandBuffer,
            Resource.SwapchainImage swapchainImage
    ) throws RenderException {
        swapchainColorAttachment.swapchainImage = swapchainImage;

        cx.dCmd.vkResetCommandBuffer(commandBuffer, 0);

        try (Arena arena = Arena.ofConfined()) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.allocate(arena);
            beginInfo.flags(VkCommandBufferUsageFlags.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            @enumtype(VkResult.class) int result = cx.dCmd.vkBeginCommandBuffer(commandBuffer, beginInfo);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法开始记录指令缓冲, 错误代码: " + VkResult.explain(result));
            }

            for (CompiledRenderPassOp op : compiledRenderPassOps) {
                op.recordToCommandBuffer(cx, swapchain, commandBuffer, currentFrameIndex);
            }

            result = cx.dCmd.vkEndCommandBuffer(commandBuffer);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法结束指令缓冲记录, 错误代码: " + VkResult.explain(result));
            }
        }
    }

    private final ASPECT_ObjectCreate objectCreateAspect;
    private final ASPECT_AttachmentCreate attachmentCreateAspect;
    private final ASPECT_UniformCreate uniformCreateAspect;
    private final ASPECT_PushConstantCreate pushConstantCreateAspect;
    private final ASPECT_TextureCreate textureCreateAspect;
    private final ASPECT_DescriptorSetLayoutCreate descriptorSetLayoutCreateAspect;
    private final ASPECT_DescriptorSetCreate descriptorSetCreateAspect;
    private final ASPECT_PipelineCreate pipelineCreateAspect;
    private final ASPECT_RenderPassCompilation renderPassCompilationAspect;
    private final ASPECT_RenderFrame renderFrameAspect;

    VulkanRenderEngineContext cx;
    Swapchain swapchain;
    final VulkanSwapchainAttachment swapchainColorAttachment;
    final VulkanImageAttachment swapchainDepthAttachment;
    int currentFrameIndex = 0;
    boolean pauseRender = false;

    final Set<VulkanRenderObject> objects = ConcurrentHashMap.newKeySet();
    final Set<VulkanRenderPipeline> pipelines = ConcurrentHashMap.newKeySet();
    final Set<VulkanImageAttachment> colorAttachments = ConcurrentHashMap.newKeySet();
    final Set<VulkanImageAttachment> depthAttachments = ConcurrentHashMap.newKeySet();
    final Set<VulkanUniformBuffer> perFrameUpdatedUniforms = ConcurrentHashMap.newKeySet();
    final Set<VulkanUniformBuffer> manuallyUpdatedUniforms = ConcurrentHashMap.newKeySet();
    final AtomicBoolean uniformManuallyUpdated = new AtomicBoolean(false);
    final Set<CombinedImageSampler> textures = ConcurrentHashMap.newKeySet();
    final ConcurrentHashMap<VulkanDescriptorSetLayout, VkDescriptorPool> descriptorPools = new ConcurrentHashMap<>();
    final Set<VulkanDescriptorSet> descriptorSets = ConcurrentHashMap.newKeySet();

    static final class ImageDescriptorUsage {
        public final VulkanDescriptorSet descriptorSet;
        public final Integer binding;

        public ImageDescriptorUsage(VulkanDescriptorSet descriptorSet, Integer binding) {
            this.descriptorSet = descriptorSet;
            this.binding = binding;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ImageDescriptorUsage that = (ImageDescriptorUsage) o;
            return descriptorSet.equals(that.descriptorSet) && binding.equals(that.binding);
        }

        @Override
        public int hashCode() {
            return Objects.hash(descriptorSet, binding);
        }
    }

    final HashMap<Ref<Resource.Image>, Set<ImageDescriptorUsage>> imageDescriptorSetUsage = new HashMap<>();

    final Set<VulkanRenderPass> renderPasses = new ConcurrentSkipListSet<>();
    final AtomicBoolean renderPassNeedCompilation = new AtomicBoolean(true);
    List<CompiledRenderPassOp> compiledRenderPassOps = Collections.emptyList();

    private static final Logger logger = Logger.getLogger(VulkanRenderEngine.class.getName());
}
