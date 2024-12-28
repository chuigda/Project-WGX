package chr.wgx.render.gles2.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.gles2.data.GLES2DescriptorSet;
import chr.wgx.render.gles2.data.GLES2RenderObject;
import chr.wgx.render.task.RenderTaskDynamic;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GLES2RenderTaskDynamic extends RenderTaskDynamic {
    public final GLES2RenderObject renderObject;
    public final List<AtomicReference<GLES2DescriptorSet>> descriptorSets;
    public final AtomicReference<GLES2DescriptorSet> pushConstant;

    public GLES2RenderTaskDynamic(
            GLES2RenderObject renderObject,
            List<DescriptorSet> descriptorSets,
            AtomicReference<GLES2DescriptorSet> pushConstant
    ) {
        this.renderObject = renderObject;
        this.descriptorSets = descriptorSets.stream()
                .map(ds -> new AtomicReference<>((GLES2DescriptorSet) ds))
                .toList();
        this.pushConstant = pushConstant;
    }

    @Override
    public void updateDescriptorSet(int location, DescriptorSet descriptorSet) {
        descriptorSets.get(location).set((GLES2DescriptorSet) descriptorSet);
    }
}
