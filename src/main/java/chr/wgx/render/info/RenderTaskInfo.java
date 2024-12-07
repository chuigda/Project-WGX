package chr.wgx.render.info;

import chr.wgx.render.common.ClearBehavior;
import chr.wgx.render.common.Color;
import chr.wgx.render.handle.ColorAttachmentHandle;
import chr.wgx.render.handle.DepthAttachmentHandle;
import chr.wgx.render.handle.ObjectHandle;
import chr.wgx.render.handle.RenderPipelineHandle;
import tech.icey.xjbutil.container.Option;

import java.util.List;

public final class RenderTaskInfo {
    public static final int PRIORITY_NOT_IMPORTANT = -1;

    public final int priority;
    public final RenderPipelineHandle pipelineHandle;
    public final List<ObjectHandle> objectHandles;

    public final List<ColorAttachmentHandle> colorAttachments;
    public final Option<DepthAttachmentHandle> depthAttachment;

    public final ClearBehavior clearBehavior;
    public final Option<Color> clearColor;

    // TODO: attachments, uniforms and push constants

    public RenderTaskInfo(
            int priority,
            RenderPipelineHandle pipelineHandle,
            List<ObjectHandle> objectHandles,
            List<ColorAttachmentHandle> colorAttachments,
            Option<DepthAttachmentHandle> depthAttachment,
            ClearBehavior clearBehavior,
            Option<Color> clearColor
    ) {
        this.priority = priority;
        this.pipelineHandle = pipelineHandle;
        this.objectHandles = objectHandles;
        this.colorAttachments = colorAttachments;
        this.depthAttachment = depthAttachment;
        this.clearBehavior = clearBehavior;
        this.clearColor = clearColor;
    }

    public RenderTaskInfo(
            int priority,
            RenderPipelineHandle pipelineHandle,
            List<ObjectHandle> objectHandles,
            List<ColorAttachmentHandle> colorAttachments,
            Option<DepthAttachmentHandle> depthAttachment,
            ClearBehavior clearBehavior
    ) {
        this(
                priority,
                pipelineHandle,
                objectHandles,
                colorAttachments,
                depthAttachment,
                clearBehavior,
                clearBehavior == ClearBehavior.DONT_CLEAR ?
                        Option.none()
                        : Option.some(new Color(0.0f, 0.0f, 0.0f, 0.0f))
        );
    }

    public RenderTaskInfo(
            int priority,
            RenderPipelineHandle pipelineHandle,
            List<ObjectHandle> objectHandles,
            List<ColorAttachmentHandle> colorAttachments,
            Option<DepthAttachmentHandle> depthAttachment
    ) {
        this(
                priority,
                pipelineHandle,
                objectHandles,
                colorAttachments,
                depthAttachment,
                ClearBehavior.DONT_CLEAR,
                Option.none()
        );
    }
}
