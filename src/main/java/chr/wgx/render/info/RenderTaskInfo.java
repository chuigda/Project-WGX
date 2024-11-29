package chr.wgx.render.info;

import chr.wgx.render.handle.ColorAttachmentHandle;
import chr.wgx.render.handle.DepthAttachmentHandle;
import chr.wgx.render.handle.ObjectHandle;
import chr.wgx.render.handle.RenderPipelineHandle;
import tech.icey.xjbutil.container.Option;

import java.util.List;

public final class RenderTaskInfo {
    public final RenderPipelineHandle pipelineHandle;
    public final List<ObjectHandle> objectHandles;

    public final List<ColorAttachmentHandle> colorAttachments;
    public final Option<DepthAttachmentHandle> depthAttachment;

    // TODO: attachments, uniforms and push constants

    public RenderTaskInfo(
            RenderPipelineHandle pipelineHandle,
            List<ObjectHandle> objectHandles,
            List<ColorAttachmentHandle> colorAttachments,
            Option<DepthAttachmentHandle> depthAttachment
    ) {
        this.pipelineHandle = pipelineHandle;
        this.objectHandles = objectHandles;
        this.colorAttachments = colorAttachments;
        this.depthAttachment = depthAttachment;
    }
}
