package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.info.*;
import chr.wgx.render.vk.data.VulkanDescriptorSetLayout;
import chr.wgx.render.vk.data.VulkanRenderPipeline;
import org.jetbrains.annotations.Nullable;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.ptr.BytePtr;
import club.doki7.ffm.ptr.IntPtr;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.VkColorComponentFlags;
import club.doki7.vulkan.bitmask.VkCullModeFlags;
import club.doki7.vulkan.bitmask.VkSampleCountFlags;
import club.doki7.vulkan.bitmask.VkShaderStageFlags;
import club.doki7.vulkan.datatype.*;
import club.doki7.vulkan.enumtype.*;
import club.doki7.vulkan.handle.VkDescriptorSetLayout;
import club.doki7.vulkan.handle.VkPipeline;
import club.doki7.vulkan.handle.VkPipelineLayout;
import club.doki7.vulkan.handle.VkShaderModule;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.Objects;

public final class ASPECT_PipelineCreate {
    public ASPECT_PipelineCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public VulkanRenderPipeline createPipelineImpl(RenderPipelineCreateInfo info) throws RenderException {
        if (!(info.vulkanShaderProgram instanceof Option.Some<ShaderProgram.Vulkan> someProgram)) {
            throw new RenderException("未提供 Vulkan 渲染器所需的着色器程序");
        }
        ShaderProgram.Vulkan program = someProgram.value;

        VulkanRenderEngineContext cx = engine.cx;

        @Nullable VkShaderModule vertexShaderModule = null;
        @Nullable VkShaderModule fragmentShaderModule = null;
        try (Arena arena = Arena.ofConfined()) {
            vertexShaderModule = cx.createShaderModule(program.vertexShader);
            fragmentShaderModule = cx.createShaderModule(program.fragmentShader);

            VkPipelineShaderStageCreateInfo.Ptr shaderStages = VkPipelineShaderStageCreateInfo.allocate(arena, 2);
            VkPipelineShaderStageCreateInfo vertexStage = shaderStages.at(0);
            vertexStage.stage(VkShaderStageFlags.VERTEX);
            vertexStage.module(vertexShaderModule);
            vertexStage.pName(MAIN_NAME_BUF);
            VkPipelineShaderStageCreateInfo fragmentStage = shaderStages.at(1);
            fragmentStage.stage(VkShaderStageFlags.FRAGMENT);
            fragmentStage.module(fragmentShaderModule);
            fragmentStage.pName(MAIN_NAME_BUF);

            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.allocate(arena);
            VkVertexInputBindingDescription bindingDescription = VkVertexInputBindingDescription.allocate(arena);
            bindingDescription.binding(0);
            bindingDescription.stride(info.vertexInputInfo.stride);
            bindingDescription.inputRate(VkVertexInputRate.VERTEX);
            VkVertexInputAttributeDescription.Ptr attributeDescriptions =
                    VkVertexInputAttributeDescription.allocate(arena, info.vertexInputInfo.attributes.size());
            for (int i = 0; i < attributeDescriptions.size(); i++) {
                FieldInfo attribute = info.vertexInputInfo.attributes.get(i);
                VkVertexInputAttributeDescription attributeDescription = attributeDescriptions.at(i);

                attributeDescription.binding(0);
                attributeDescription.location(attribute.location);
                attributeDescription.format(attribute.type.vkFormat);
                attributeDescription.offset(attribute.byteOffset);
            }
            vertexInputInfo.vertexBindingDescriptionCount(1);
            vertexInputInfo.pVertexBindingDescriptions(bindingDescription);
            vertexInputInfo.vertexAttributeDescriptionCount((int) attributeDescriptions.size());
            vertexInputInfo.pVertexAttributeDescriptions(attributeDescriptions);

            VkPipelineInputAssemblyStateCreateInfo inputAssembly =
                    VkPipelineInputAssemblyStateCreateInfo.allocate(arena);
            inputAssembly.topology(VkPrimitiveTopology.TRIANGLE_LIST);

            IntPtr dynamicStates = IntPtr.allocate(arena, 2);
            dynamicStates.write(0, VkDynamicState.VIEWPORT);
            dynamicStates.write(1, VkDynamicState.SCISSOR);
            VkPipelineDynamicStateCreateInfo dynamicStateInfo = VkPipelineDynamicStateCreateInfo.allocate(arena);
            dynamicStateInfo.dynamicStateCount(2);
            dynamicStateInfo.pDynamicStates(dynamicStates);

            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.allocate(arena);
            viewportState.viewportCount(1);
            viewportState.scissorCount(1);

            VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.allocate(arena);
            rasterizer.depthClampEnable(VkConstants.FALSE);
            rasterizer.rasterizerDiscardEnable(VkConstants.FALSE);
            rasterizer.polygonMode(VkPolygonMode.FILL);
            rasterizer.lineWidth(1.0f);
            rasterizer.cullMode(VkCullModeFlags.NONE);
            rasterizer.frontFace(VkFrontFace.COUNTER_CLOCKWISE);
            rasterizer.depthBiasEnable(VkConstants.FALSE);
            rasterizer.depthBiasConstantFactor(0.0f);
            rasterizer.depthBiasClamp(0.0f);
            rasterizer.depthBiasSlopeFactor(0.0f);

            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.allocate(arena);
            multisampling.sampleShadingEnable(VkConstants.FALSE);
            multisampling.rasterizationSamples(VkSampleCountFlags._1);

            VkPipelineColorBlendAttachmentState colorBlendAttachment =
                    VkPipelineColorBlendAttachmentState.allocate(arena);
            colorBlendAttachment.colorWriteMask(
                    VkColorComponentFlags.R |
                            VkColorComponentFlags.G |
                            VkColorComponentFlags.B |
                            VkColorComponentFlags.A
            );
            // TODO make these parameters of PipelineCreateInfo
            colorBlendAttachment.blendEnable(VkConstants.FALSE);
            colorBlendAttachment.srcColorBlendFactor(VkBlendFactor.ONE);
            colorBlendAttachment.dstColorBlendFactor(VkBlendFactor.ZERO);
            colorBlendAttachment.colorBlendOp(VkBlendOp.ADD);
            colorBlendAttachment.srcAlphaBlendFactor(VkBlendFactor.ONE);
            colorBlendAttachment.dstAlphaBlendFactor(VkBlendFactor.ZERO);
            colorBlendAttachment.alphaBlendOp(VkBlendOp.ADD);

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.allocate(arena);
            colorBlending.logicOpEnable(VkConstants.FALSE);
            colorBlending.logicOp(VkLogicOp.COPY);
            colorBlending.attachmentCount(1);
            colorBlending.pAttachments(colorBlendAttachment);

            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.allocate(arena);
            if (info.depthTest) {
                depthStencil.depthTestEnable(VkConstants.TRUE);
                depthStencil.depthWriteEnable(VkConstants.TRUE);
                depthStencil.depthCompareOp(VkCompareOp.LESS);
                depthStencil.minDepthBounds(0.0f);
                depthStencil.maxDepthBounds(1.0f);
            }

            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.allocate(arena);
            pipelineLayoutInfo.setLayoutCount(info.descriptorSetLayouts.size());
            if (!info.descriptorSetLayouts.isEmpty()) {
                VkDescriptorSetLayout.Ptr pSetLayouts = VkDescriptorSetLayout.Ptr.allocate(
                        arena,
                        info.descriptorSetLayouts.size()
                );
                for (int i = 0; i < info.descriptorSetLayouts.size(); i++) {
                    pSetLayouts.write(i, ((VulkanDescriptorSetLayout) info.descriptorSetLayouts.get(i)).layout);
                }
                pipelineLayoutInfo.pSetLayouts(pSetLayouts);
            }

            if (info.pushConstantInfo instanceof Option.Some<PushConstantInfo> some) {
                PushConstantInfo pushConstantInfo = some.value;

                VkPushConstantRange.Ptr pushConstantRanges = VkPushConstantRange.allocate(
                        arena,
                        pushConstantInfo.pushConstantRanges.size()
                );
                for (int i = 0; i < pushConstantInfo.pushConstantRanges.size(); i++) {
                    PushConstantRange range = pushConstantInfo.pushConstantRanges.get(i);
                    VkPushConstantRange rangeVk = pushConstantRanges.at(i);

                    rangeVk.stageFlags(range.shaderStage.vkShaderStageFlags);
                    rangeVk.offset(range.offset);
                    rangeVk.size(range.type.byteSize);
                }

                pipelineLayoutInfo.pushConstantRangeCount(pushConstantInfo.pushConstantRanges.size());
                pipelineLayoutInfo.pPushConstantRanges(pushConstantRanges);
            }

            VkPipelineLayout.Ptr pPipelineLayout = VkPipelineLayout.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result = cx.dCmd.createPipelineLayout(
                    cx.device,
                    pipelineLayoutInfo,
                    null,
                    pPipelineLayout
            );
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建管线布局, 错误代码: " + VkResult.explain(result));
            }
            VkPipelineLayout pipelineLayout = Objects.requireNonNull(pPipelineLayout.read());

            VkGraphicsPipelineCreateInfo pipelineInfo = VkGraphicsPipelineCreateInfo.allocate(arena);
            pipelineInfo.stageCount(2);
            pipelineInfo.pStages(shaderStages);
            pipelineInfo.pVertexInputState(vertexInputInfo);
            pipelineInfo.pInputAssemblyState(inputAssembly);
            pipelineInfo.pViewportState(viewportState);
            pipelineInfo.pRasterizationState(rasterizer);
            pipelineInfo.pMultisampleState(multisampling);
            pipelineInfo.pDepthStencilState(depthStencil);
            pipelineInfo.pColorBlendState(colorBlending);
            pipelineInfo.pDynamicState(dynamicStateInfo);
            pipelineInfo.layout(pipelineLayout);
            pipelineInfo.basePipelineHandle(null);
            pipelineInfo.basePipelineIndex(-1);

            VkPipelineRenderingCreateInfo pipelineRenderingCreateInfo = VkPipelineRenderingCreateInfo.allocate(arena);
            pipelineRenderingCreateInfo.colorAttachmentCount(info.colorAttachmentCount);
            IntPtr pColorAttachmentFormats = IntPtr.allocate(arena, info.colorAttachmentCount);
            for (int i = 0; i < info.colorAttachmentCount; i++) {
                pColorAttachmentFormats.write(i, engine.swapchain.swapChainImageFormat);
            }
            pipelineRenderingCreateInfo.pColorAttachmentFormats(pColorAttachmentFormats);
            if (info.depthTest) {
                pipelineRenderingCreateInfo.depthAttachmentFormat(engine.swapchain.depthFormat);
            }
            pipelineInfo.pNext(pipelineRenderingCreateInfo);

            VkPipeline.Ptr pPipeline = VkPipeline.Ptr.allocate(arena);
            result = cx.dCmd.createGraphicsPipelines(cx.device, null, 1, pipelineInfo, null, pPipeline);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建图形管线, 错误代码: " + VkResult.explain(result));
            }
            VkPipeline pipeline = Objects.requireNonNull(pPipeline.read());

            VulkanRenderPipeline ret = new VulkanRenderPipeline(info, pipelineLayout, pipeline);
            engine.pipelines.add(ret);
            return ret;
        } finally {
            if (vertexShaderModule != null) {
                cx.dCmd.destroyShaderModule(cx.device, vertexShaderModule, null);
            }
            if (fragmentShaderModule != null) {
                cx.dCmd.destroyShaderModule(cx.device, fragmentShaderModule, null);
            }
        }
    }

    private final VulkanRenderEngine engine;

    private static final BytePtr MAIN_NAME_BUF = BytePtr.allocateString(Arena.global(), "main");
}
