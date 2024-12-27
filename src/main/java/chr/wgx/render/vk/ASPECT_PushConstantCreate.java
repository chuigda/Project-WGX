package chr.wgx.render.vk;

import chr.wgx.render.data.PushConstant;
import chr.wgx.render.info.PushConstantRange;
import chr.wgx.render.vk.data.VulkanPushConstant;

import java.util.ArrayList;
import java.util.List;

public final class ASPECT_PushConstantCreate {
    ASPECT_PushConstantCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    List<PushConstant> createPushConstantImpl(List<PushConstantRange> pushConstantRanges, int count) {
        PushConstantRange lastRange = pushConstantRanges.getLast();
        int pushConstantBufferSize = lastRange.offset + lastRange.size;

        List<PushConstant> ret = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            VulkanPushConstant pushConstant = new VulkanPushConstant(
                    engine.cx.prefabArena.allocate(pushConstantBufferSize)
            );
            ret.add(pushConstant);
        }
        return ret;
    }

    private final VulkanRenderEngine engine;
}
