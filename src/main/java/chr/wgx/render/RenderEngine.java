package chr.wgx.render;

import chr.wgx.render.common.Color;
import chr.wgx.render.data.*;
import chr.wgx.render.info.*;
import chr.wgx.render.task.RenderPass;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action2;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class RenderEngine {
    public final ConcurrentLinkedQueue<Action2<Integer, Integer>> onResizeActions = new ConcurrentLinkedQueue<>();
    public final ConcurrentLinkedQueue<Action0> onBeforeRenderFrameActions = new ConcurrentLinkedQueue<>();
    public final ConcurrentLinkedQueue<Action0> onAfterRenderFrameActions = new ConcurrentLinkedQueue<>();
    public final ConcurrentLinkedQueue<Action0> onCloseActions = new ConcurrentLinkedQueue<>();

    public final void resizeEngine(int width, int height) throws RenderException {
        resize(width, height);
        for (Action2<Integer, Integer> action : onResizeActions) {
            action.apply(width, height);
        }
    }

    public final void renderFrameEngine() throws RenderException {
        for (Action0 action : onBeforeRenderFrameActions) {
            action.apply();
        }
        renderFrame();
        for (Action0 action : onAfterRenderFrameActions) {
            action.apply();
        }
    }

    public final void closeEngine() {
        close();
        for (Action0 action : onCloseActions) {
            action.apply();
        }
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
    public abstract List<PushConstant> createPushConstant(PushConstantInfo info, int count) throws RenderException;

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
            List<Color> clearColors,
            Option<Attachment> depthAttachment
    ) throws RenderException;
}
