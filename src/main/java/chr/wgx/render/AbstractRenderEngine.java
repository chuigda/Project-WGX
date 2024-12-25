package chr.wgx.render;

import chr.wgx.render.data.*;
import chr.wgx.render.info.*;
import chr.wgx.render.task.RenderPass;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action1;
import tech.icey.xjbutil.functional.Action2;

import java.awt.image.BufferedImage;
import java.util.List;

public abstract class AbstractRenderEngine {
    private final Action1<AbstractRenderEngine> onInit;
    private final Action2<Integer, Integer> onResize;
    private final Action0 onBeforeRenderFrame;
    private final Action0 onAfterRenderFrame;
    private final Action0 onClose;

    public AbstractRenderEngine(
            Action1<AbstractRenderEngine> onInit,
            Action2<Integer, Integer> onResize,
            Action0 onBeforeRenderFrame,
            Action0 onAfterRenderFrame,
            Action0 onClose
    ) {
        this.onInit = onInit;
        this.onResize = onResize;
        this.onBeforeRenderFrame = onBeforeRenderFrame;
        this.onAfterRenderFrame = onAfterRenderFrame;
        this.onClose = onClose;
    }

    public final void resizeEngine(int width, int height) throws RenderException {
        resize(width, height);
        onResize.apply(width, height);
    }

    public final void renderFrameEngine() throws RenderException {
        onBeforeRenderFrame.apply();
        renderFrame();
        onAfterRenderFrame.apply();
    }

    public final void closeEngine() {
        close();
        onClose.apply();
    }

    protected abstract void resize(int width, int height) throws RenderException;
    protected abstract void renderFrame() throws RenderException;
    protected abstract void close();

    public abstract RenderObject createObject(ObjectCreateInfo info) throws RenderException;
    public abstract List<RenderObject> createObject(List<ObjectCreateInfo> info) throws RenderException;
    public abstract Pair<Attachment, Texture> createColorAttachment(AttachmentCreateInfo i) throws RenderException;
    public abstract Attachment createDepthAttachment(AttachmentCreateInfo i) throws RenderException;
    public abstract Pair<Attachment, Attachment> getDefaultAttachments();
    public abstract Texture createTexture(TextureCreateInfo image) throws RenderException;
    public abstract List<Texture> createTexture(List<TextureCreateInfo> images) throws RenderException;
    public abstract UniformBuffer createUniform(UniformBufferCreateInfo info) throws RenderException;

    public abstract DescriptorSetLayout createDescriptorSetLayout(
            DescriptorSetLayoutCreateInfo info,
            int maxSets
    ) throws RenderException;
    public abstract DescriptorSet createDescriptorSet(DescriptorSetCreateInfo info) throws RenderException;
    public abstract RenderPipeline createPipeline(RenderPipelineCreateInfo info) throws RenderException;

    public abstract RenderPass createRenderPass(
            String renderPassName,
            int priority,
            List<Attachment> colorAttachments,
            Option<Attachment> depthAttachment
    ) throws RenderException;
}
