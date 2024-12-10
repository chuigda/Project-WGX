package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.info.RenderPipelineCreateInfo;
import chr.wgx.render.info.ShaderProgram;
import chr.wgx.render.info.VertexInputInfo;
import chr.wgx.render.vk.data.VulkanPipeline;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.VkColorComponentFlags;
import tech.icey.vk4j.bitmask.VkCullModeFlags;
import tech.icey.vk4j.bitmask.VkSampleCountFlags;
import tech.icey.vk4j.bitmask.VkShaderStageFlags;
import tech.icey.vk4j.datatype.*;
import tech.icey.vk4j.enumtype.*;
import tech.icey.vk4j.handle.VkPipeline;
import tech.icey.vk4j.handle.VkPipelineLayout;
import tech.icey.vk4j.handle.VkShaderModule;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.logging.Logger;

public final class PipelineCreateAspect {
    public PipelineCreateAspect(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public VulkanPipeline createPipelineImpl(RenderPipelineCreateInfo info) throws RenderException {
        if (!(info.vulkanShaderProgram instanceof Option.Some<ShaderProgram.Vulkan> someProgram)) {
            throw new RenderException("未提供 Vulkan 渲染器所需的着色器程序");
        }
        ShaderProgram.Vulkan program = someProgram.value;

        VulkanRenderEngineContext cx = engine.cx;

        VkShaderModule vertexShaderModule = null;
        VkShaderModule fragmentShaderModule = null;
        try (Arena arena = Arena.ofConfined()) {
            vertexShaderModule = cx.createShaderModule(program.vertexShader);
            fragmentShaderModule = cx.createShaderModule(program.fragmentShader);

            VkPipelineShaderStageCreateInfo[] shaderStages = VkPipelineShaderStageCreateInfo.allocate(arena, 2);
            shaderStages[0].stage(VkShaderStageFlags.VK_SHADER_STAGE_VERTEX_BIT);
            shaderStages[0].module(vertexShaderModule);
            shaderStages[0].pName(MAIN_NAME_BUF);
            shaderStages[1].stage(VkShaderStageFlags.VK_SHADER_STAGE_FRAGMENT_BIT);
            shaderStages[1].module(fragmentShaderModule);
            shaderStages[1].pName(MAIN_NAME_BUF);

            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.allocate(arena);
            VkVertexInputBindingDescription bindingDescription = VkVertexInputBindingDescription.allocate(arena);
            bindingDescription.binding(0);
            bindingDescription.stride(info.vertexInputInfo.stride);
            bindingDescription.inputRate(VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX);
            VkVertexInputAttributeDescription[] attributeDescriptions =
                    VkVertexInputAttributeDescription.allocate(arena, info.vertexInputInfo.attributes.size());
            for (int i = 0; i < attributeDescriptions.length; i++) {
                VertexInputInfo.Attribute attribute = info.vertexInputInfo.attributes.get(i);

                attributeDescriptions[i].binding(0);
                attributeDescriptions[i].location(attribute.location);
                attributeDescriptions[i].format(attribute.type.vkFormat);
                attributeDescriptions[i].offset(attribute.byteOffset);
            }
            vertexInputInfo.vertexBindingDescriptionCount(1);
            vertexInputInfo.pVertexBindingDescriptions(bindingDescription);
            vertexInputInfo.vertexAttributeDescriptionCount(attributeDescriptions.length);
            vertexInputInfo.pVertexAttributeDescriptions(attributeDescriptions[0]);

            VkPipelineInputAssemblyStateCreateInfo inputAssembly =
                    VkPipelineInputAssemblyStateCreateInfo.allocate(arena);
            inputAssembly.topology(VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);

            IntBuffer dynamicStates = IntBuffer.allocate(arena, 2);
            dynamicStates.write(0, VkDynamicState.VK_DYNAMIC_STATE_VIEWPORT);
            dynamicStates.write(1, VkDynamicState.VK_DYNAMIC_STATE_SCISSOR);
            VkPipelineDynamicStateCreateInfo dynamicStateInfo = VkPipelineDynamicStateCreateInfo.allocate(arena);
            dynamicStateInfo.dynamicStateCount(2);
            dynamicStateInfo.pDynamicStates(dynamicStates);

            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.allocate(arena);
            viewportState.viewportCount(1);
            viewportState.scissorCount(1);

            VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.allocate(arena);
            rasterizer.depthClampEnable(Constants.VK_FALSE);
            rasterizer.rasterizerDiscardEnable(Constants.VK_FALSE);
            rasterizer.polygonMode(VkPolygonMode.VK_POLYGON_MODE_FILL);
            rasterizer.lineWidth(1.0f);
            rasterizer.cullMode(VkCullModeFlags.VK_CULL_MODE_NONE);
            rasterizer.frontFace(VkFrontFace.VK_FRONT_FACE_COUNTER_CLOCKWISE);
            rasterizer.depthBiasEnable(Constants.VK_FALSE);
            rasterizer.depthBiasConstantFactor(0.0f);
            rasterizer.depthBiasClamp(0.0f);
            rasterizer.depthBiasSlopeFactor(0.0f);

            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.allocate(arena);
            multisampling.sampleShadingEnable(cx.msaaSampleCountFlags != VkSampleCountFlags.VK_SAMPLE_COUNT_1_BIT ?
                    Constants.VK_TRUE :
                    Constants.VK_FALSE);
            multisampling.rasterizationSamples(cx.msaaSampleCountFlags);
            multisampling.minSampleShading(cx.msaaSampleCountFlags != VkSampleCountFlags.VK_SAMPLE_COUNT_1_BIT ?
                    0.2f :
                    1.0f);
            multisampling.pSampleMask(null);

            VkPipelineColorBlendAttachmentState colorBlendAttachment =
                    VkPipelineColorBlendAttachmentState.allocate(arena);
            colorBlendAttachment.colorWriteMask(
                    VkColorComponentFlags.VK_COLOR_COMPONENT_R_BIT |
                            VkColorComponentFlags.VK_COLOR_COMPONENT_G_BIT |
                            VkColorComponentFlags.VK_COLOR_COMPONENT_B_BIT |
                            VkColorComponentFlags.VK_COLOR_COMPONENT_A_BIT
            );
            // TODO make these parameters of PipelineCreateInfo
            colorBlendAttachment.blendEnable(Constants.VK_FALSE);
            colorBlendAttachment.srcColorBlendFactor(VkBlendFactor.VK_BLEND_FACTOR_ONE);
            colorBlendAttachment.dstColorBlendFactor(VkBlendFactor.VK_BLEND_FACTOR_ZERO);
            colorBlendAttachment.colorBlendOp(VkBlendOp.VK_BLEND_OP_ADD);
            colorBlendAttachment.srcAlphaBlendFactor(VkBlendFactor.VK_BLEND_FACTOR_ONE);
            colorBlendAttachment.dstAlphaBlendFactor(VkBlendFactor.VK_BLEND_FACTOR_ZERO);
            colorBlendAttachment.alphaBlendOp(VkBlendOp.VK_BLEND_OP_ADD);

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.allocate(arena);
            colorBlending.logicOpEnable(Constants.VK_FALSE);
            colorBlending.logicOp(VkLogicOp.VK_LOGIC_OP_COPY);
            colorBlending.attachmentCount(1);
            colorBlending.pAttachments(colorBlendAttachment);

            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.allocate(arena);
            if (info.depthTest) {
                depthStencil.depthTestEnable(Constants.VK_TRUE);
                depthStencil.depthWriteEnable(Constants.VK_TRUE);
                depthStencil.depthCompareOp(VkCompareOp.VK_COMPARE_OP_LESS);
                depthStencil.minDepthBounds(0.0f);
                depthStencil.maxDepthBounds(1.0f);
            }

            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.allocate(arena);
            // TODO add push constant and descriptor set layout then
            VkPipelineLayout.Buffer pPipelineLayout = VkPipelineLayout.Buffer.allocate(arena);
            @enumtype(VkResult.class) int result = cx.dCmd.vkCreatePipelineLayout(
                    cx.device,
                    pipelineLayoutInfo,
                    null,
                    pPipelineLayout
            );
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法创建管线布局, 错误代码: " + VkResult.explain(result));
            }
            VkPipelineLayout pipelineLayout = pPipelineLayout.read();

            VkGraphicsPipelineCreateInfo pipelineInfo = VkGraphicsPipelineCreateInfo.allocate(arena);
            pipelineInfo.stageCount(2);
            pipelineInfo.pStages(shaderStages[0]);
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
            IntBuffer pColorAttachmentFormats = IntBuffer.allocate(arena, info.colorAttachmentCount);
            for (int i = 0; i < info.colorAttachmentCount; i++) {
                pColorAttachmentFormats.write(i, engine.swapchain.swapChainImageFormat);
            }
            pipelineRenderingCreateInfo.pColorAttachmentFormats(pColorAttachmentFormats);
            if (info.depthTest) {
                pipelineRenderingCreateInfo.depthAttachmentFormat(engine.swapchain.depthFormat);
            }
            pipelineInfo.pNext(pipelineRenderingCreateInfo);

            VkPipeline.Buffer pPipeline = VkPipeline.Buffer.allocate(arena);
            result = cx.dCmd.vkCreateGraphicsPipelines(cx.device, null, 1, pipelineInfo, null, pPipeline);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法创建图形管线, 错误代码: " + VkResult.explain(result));
            }
            VkPipeline pipeline = pPipeline.read();

            VulkanPipeline ret = new VulkanPipeline(info, pipelineLayout, pipeline);
            engine.pipelines.add(ret);
            return ret;
        } finally {
            if (vertexShaderModule != null) {
                cx.dCmd.vkDestroyShaderModule(cx.device, vertexShaderModule, null);
            }
            if (fragmentShaderModule != null) {
                cx.dCmd.vkDestroyShaderModule(cx.device, fragmentShaderModule, null);
            }
        }
    }

    private final VulkanRenderEngine engine;

    private static final ByteBuffer MAIN_NAME_BUF = ByteBuffer.allocateString(Arena.global(), "main");
    private static final Logger logger = Logger.getLogger(PipelineCreateAspect.class.getName());
}
