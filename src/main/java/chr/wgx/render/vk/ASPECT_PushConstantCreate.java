package chr.wgx.render.vk;

import chr.wgx.render.data.PushConstant;
import chr.wgx.render.info.PushConstantInfo;
import chr.wgx.render.vk.data.VulkanPushConstant;

import java.util.ArrayList;
import java.util.List;

public final class ASPECT_PushConstantCreate {
    ASPECT_PushConstantCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    List<PushConstant> createPushConstantImpl(PushConstantInfo info, int count) {
        List<PushConstant> ret = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            VulkanPushConstant pushConstant = new VulkanPushConstant(
                    info,
                    engine.cx.prefabArena.allocate(info.bufferSize)
            );
            ret.add(pushConstant);
        }
        return ret;
    }

    private final VulkanRenderEngine engine;
}
