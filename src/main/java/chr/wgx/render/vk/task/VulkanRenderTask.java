package chr.wgx.render.vk.task;

import chr.wgx.render.task.RenderTask;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanRenderObject;

import java.util.List;

public final class VulkanRenderTask extends RenderTask {
    public final VulkanRenderObject renderObject;
    public final List<VulkanDescriptorSet> descriptorSets;

    VulkanRenderTask(VulkanRenderObject renderObject, List<VulkanDescriptorSet> descriptorSets) {
        this.renderObject = renderObject;
        this.descriptorSets = descriptorSets;
    }
}
