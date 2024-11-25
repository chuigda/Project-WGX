package chr.wgx.render.info;

import chr.wgx.render.handle.ObjectHandle;
import chr.wgx.render.handle.RenderPipelineHandle;

import java.util.List;

public final class RenderTaskInfo {
    public final RenderPipelineHandle pipelineHandle;
    public final List<ObjectHandle> objectHandles;

    // TODO: attachments, uniforms and push constants

    public RenderTaskInfo(RenderPipelineHandle pipelineHandle, List<ObjectHandle> objectHandles) {
        this.pipelineHandle = pipelineHandle;
        this.objectHandles = objectHandles;
    }
}
