package chr.wgx.render.vk.data;

import chr.wgx.render.info.AttachmentCreateInfo;
import chr.wgx.render.vk.IVkDisposable;
import chr.wgx.render.vk.Resource;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import tech.icey.xjbutil.container.Ref;

public final class VulkanImageAttachment extends VulkanAttachment implements IVkDisposable {
    public final Ref<Resource.Image> image;

    public VulkanImageAttachment(AttachmentCreateInfo createInfo, Ref<Resource.Image> image) {
        super(createInfo);
        this.image = image;
    }

    @Override
    public void dispose(VulkanRenderEngineContext cx) {
        this.image.value.dispose(cx);
    }
}
