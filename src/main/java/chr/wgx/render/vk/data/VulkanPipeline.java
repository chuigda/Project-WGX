package chr.wgx.render.vk.data;

import chr.wgx.render.data.RenderPipeline;
import chr.wgx.render.info.RenderPipelineCreateInfo;
import chr.wgx.render.vk.IVkDisposable;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import tech.icey.vk4j.handle.VkPipeline;
import tech.icey.vk4j.handle.VkPipelineLayout;

public class VulkanPipeline extends RenderPipeline implements IVkDisposable {
    public final VkPipelineLayout pipelineLayout;
    public final VkPipeline pipeline;

    public VulkanPipeline(RenderPipelineCreateInfo createInfo, VkPipelineLayout pipelineLayout, VkPipeline pipeline) {
        super(createInfo);
        this.pipelineLayout = pipelineLayout;
        this.pipeline = pipeline;
    }

    @Override
    public void dispose(VulkanRenderEngineContext cx) {
        cx.dCmd.vkDestroyPipeline(cx.device, pipeline, null);
        cx.dCmd.vkDestroyPipelineLayout(cx.device, pipelineLayout, null);
    }
}
