package chr.wgx.render.gles2.task;

import chr.wgx.render.gles2.data.GLES2DescriptorSet;
import chr.wgx.render.gles2.data.GLES2PushConstant;
import chr.wgx.render.gles2.data.GLES2RenderObject;
import chr.wgx.render.task.RenderTask;
import tech.icey.xjbutil.container.Option;

import java.util.List;

public class GLES2RenderTask extends RenderTask {
    public final GLES2RenderObject renderObject;
    public final List<GLES2DescriptorSet> descriptorSets;
    public final Option<GLES2PushConstant> pushConstant;

    public GLES2RenderTask(
            GLES2RenderObject renderObject,
            List<GLES2DescriptorSet> descriptorSets,
            Option<GLES2PushConstant> pushConstant
    ) {
        this.renderObject = renderObject;
        this.descriptorSets = descriptorSets;
        this.pushConstant = pushConstant;
    }
}
