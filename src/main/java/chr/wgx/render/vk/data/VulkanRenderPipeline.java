package chr.wgx.render.vk.data;

import chr.wgx.render.data.RenderPipeline;
import chr.wgx.render.info.RenderPipelineCreateInfo;
import chr.wgx.render.vk.IVkDisposable;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import club.doki7.vulkan.handle.VkPipeline;
import club.doki7.vulkan.handle.VkPipelineLayout;

public class VulkanRenderPipeline extends RenderPipeline implements IVkDisposable {
    public final VkPipelineLayout pipelineLayout;
    public final VkPipeline pipeline;

    public VulkanRenderPipeline(RenderPipelineCreateInfo createInfo, VkPipelineLayout pipelineLayout, VkPipeline pipeline) {
        super(createInfo);
        this.pipelineLayout = pipelineLayout;
        this.pipeline = pipeline;
    }

    @Override
    public void dispose(VulkanRenderEngineContext cx) {
        cx.dCmd.destroyPipeline(cx.device, pipeline, null);
        cx.dCmd.destroyPipelineLayout(cx.device, pipelineLayout, null);
    }
}
