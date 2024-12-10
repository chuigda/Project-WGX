package chr.wgx.render.vk.data;

import chr.wgx.render.data.Texture;
import chr.wgx.render.vk.IVkDisposable;
import chr.wgx.render.vk.Resource;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import tech.icey.xjbutil.container.Ref;

public final class CombinedImageSampler extends Texture implements IVkDisposable {
    public final Ref<Resource.Image> image;
    public final Resource.Sampler sampler;

    public CombinedImageSampler(Ref<Resource.Image> image, Resource.Sampler sampler) {
        super(true);
        this.image = image;
        this.sampler = sampler;
    }

    public CombinedImageSampler(Resource.Image image, Resource.Sampler sampler) {
        super(false);
        this.image = new Ref<>(image);
        this.sampler = sampler;
    }

    @Override
    public void dispose(VulkanRenderEngineContext cx) {
        if (!isAttachment) {
            image.value.dispose(cx);
        }
        sampler.dispose(cx);
    }
}
