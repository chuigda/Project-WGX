package chr.wgx.render.vk.task;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderPipeline;
import chr.wgx.render.info.RenderPassCreateInfo;
import chr.wgx.render.task.RenderPass;
import chr.wgx.render.task.RenderPipelineBind;
import chr.wgx.render.vk.data.VulkanAttachment;
import chr.wgx.render.vk.data.VulkanImageAttachment;
import chr.wgx.render.vk.data.VulkanRenderPipeline;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VulkanRenderPass extends RenderPass {
    public final List<VulkanAttachment> colorAttachments;
    public final Option<VulkanImageAttachment> depthAttachment;
    public final int renderAreaWidth;
    public final int renderAreaHeight;

    public final Set<VulkanAttachment> inputAttachments = ConcurrentHashMap.newKeySet();
    public final ConcurrentSkipListSet<VulkanRenderPipelineBind> bindList = new ConcurrentSkipListSet<>();

    private final Arena prefabArena;
    private final AtomicBoolean renderPassesNeedRecompilation;

    public VulkanRenderPass(
            RenderPassCreateInfo createInfo,
            Arena prefabArena,
            AtomicBoolean renderPassesNeedRecompilation
    ) {
        super(createInfo);

        this.colorAttachments = createInfo.colorAttachmentInfos.stream()
                .map(attachmentInfo -> (VulkanAttachment) attachmentInfo.attachment)
                .toList();
        this.depthAttachment = createInfo.depthAttachmentInfo
                .map(attachmentInfo -> (VulkanImageAttachment) attachmentInfo.attachment);

        this.renderAreaWidth = colorAttachments.getFirst().createInfo.width;
        this.renderAreaHeight = colorAttachments.getFirst().createInfo.height;

        this.prefabArena = prefabArena;
        this.renderPassesNeedRecompilation = renderPassesNeedRecompilation;

        assert sanitize();
    }

    @Override
    public void addInputAttachments(List<Attachment> attachments) {
        for (Attachment attachment : attachments) {
            inputAttachments.add((VulkanAttachment) attachment);
        }
        renderPassesNeedRecompilation.set(true);
    }

    @Override
    public RenderPipelineBind createPipelineBind(int priority, RenderPipeline pipeline) {
        VulkanRenderPipelineBind bind = new VulkanRenderPipelineBind(
                priority,
                (VulkanRenderPipeline) pipeline,
                prefabArena
        );
        bindList.add(bind);
        return bind;
    }

    private boolean sanitize() {
        int width = colorAttachments.getFirst().createInfo.width;
        int height = colorAttachments.getFirst().createInfo.height;

        for (VulkanAttachment attachment : colorAttachments) {
            assert attachment.createInfo.width == width;
            assert attachment.createInfo.height == height;
        }

        if (depthAttachment instanceof Option.Some<VulkanImageAttachment> some) {
            assert some.value.createInfo.width == width;
            assert some.value.createInfo.height == height;
        }

        return true;
    }
}
