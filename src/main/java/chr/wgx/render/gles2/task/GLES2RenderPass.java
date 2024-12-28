package chr.wgx.render.gles2.task;

import chr.wgx.render.common.Color;
import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderPipeline;
import chr.wgx.render.gles2.data.GLES2Attachment;
import chr.wgx.render.gles2.data.GLES2RenderPipeline;
import chr.wgx.render.task.RenderPass;
import chr.wgx.render.task.RenderPipelineBind;
import tech.icey.xjbutil.container.Option;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public final class GLES2RenderPass extends RenderPass {
    public final List<GLES2Attachment> colorAttachments;
    public final Option<GLES2Attachment> depthAttachment;

    public final Set<GLES2Attachment> inputAttachments = ConcurrentHashMap.newKeySet();
    public final ConcurrentSkipListSet<GLES2RenderPipelineBind> bindList = new ConcurrentSkipListSet<>();

    public GLES2RenderPass(
            String renderPassName,
            int priority,
            List<Attachment> colorAttachments,
            List<Color> clearColors,
            Option<Attachment> depthAttachment
    ) {
        super(renderPassName, priority, clearColors);
        this.colorAttachments = colorAttachments.stream().map(attachment -> (GLES2Attachment) attachment).toList();
        this.depthAttachment = depthAttachment.map(attachment -> (GLES2Attachment) attachment);
    }

    @Override
    public void addInputAttachments(List<Attachment> attachments) {
        for (Attachment attachment : attachments) {
            inputAttachments.add((GLES2Attachment) attachment);
        }
    }

    @Override
    public RenderPipelineBind createPipelineBind(int priority, RenderPipeline pipeline) {
        GLES2RenderPipelineBind ret = new GLES2RenderPipelineBind(priority, (GLES2RenderPipeline) pipeline);
        bindList.add(ret);
        return ret;
    }
}
