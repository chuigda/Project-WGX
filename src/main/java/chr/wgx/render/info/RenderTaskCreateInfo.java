package chr.wgx.render.info;

import chr.wgx.render.handle.AttachmentHandle;
import chr.wgx.render.handle.RenderPipelineHandle;
import tech.icey.xjbutil.container.Option;

import java.util.List;

public record RenderTaskCreateInfo(
        RenderPipelineHandle pipelineHandle,
        List<AttachmentHandle.Color> colorAttachments,
        Option<AttachmentHandle.Depth> depthAttachment
) {}
