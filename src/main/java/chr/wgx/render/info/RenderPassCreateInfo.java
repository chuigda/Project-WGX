package chr.wgx.render.info;

import tech.icey.xjbutil.container.Option;

import java.util.List;

public final class RenderPassCreateInfo {
    public final String name;
    public final int priority;

    public final List<RenderPassAttachmentInfo> colorAttachmentInfos;
    public final Option<RenderPassAttachmentInfo> depthAttachmentInfo;

    public RenderPassCreateInfo(
            String name,
            int priority,
            List<RenderPassAttachmentInfo> colorAttachmentInfos,
            Option<RenderPassAttachmentInfo> depthAttachmentInfo
    ) {
        this.name = name;
        this.priority = priority;
        this.colorAttachmentInfos = colorAttachmentInfos;
        this.depthAttachmentInfo = depthAttachmentInfo;
    }

    public RenderPassCreateInfo(
            String name,
            int priority,
            List<RenderPassAttachmentInfo> colorAttachmentInfos,
            RenderPassAttachmentInfo depthAttachmentInfo
    ) {
        this(name, priority, colorAttachmentInfos, Option.some(depthAttachmentInfo));
    }

    public RenderPassCreateInfo(
            String name,
            int priority,
            List<RenderPassAttachmentInfo> colorAttachmentInfos
    ) {
        this(name, priority, colorAttachmentInfos, Option.none());
    }

    public RenderPassCreateInfo(
            String name,
            int priority,
            RenderPassAttachmentInfo colorAttachment,
            Option<RenderPassAttachmentInfo> depthAttachmentInfo
    ) {
        this(name, priority, List.of(colorAttachment), depthAttachmentInfo);
    }

    public RenderPassCreateInfo(
            String name,
            int priority,
            RenderPassAttachmentInfo colorAttachment,
            RenderPassAttachmentInfo depthAttachmentInfo
    ) {
        this(name, priority, List.of(colorAttachment), Option.some(depthAttachmentInfo));
    }

    public RenderPassCreateInfo(
            String name,
            int priority,
            RenderPassAttachmentInfo colorAttachment
    ) {
        this(name, priority, List.of(colorAttachment), Option.none());
    }
}
