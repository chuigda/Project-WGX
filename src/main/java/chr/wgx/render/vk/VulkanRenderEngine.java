package chr.wgx.render.vk;

import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action2;

import java.lang.foreign.Arena;
import java.util.logging.Logger;

public final class VulkanRenderEngine extends AbstractRenderEngine {
    public VulkanRenderEngine(
            Action0 onInit,
            Action2<Integer, Integer> onResize,
            Action0 onBeforeRenderFrame,
            Action0 onAfterRenderFrame,
            Action0 onClose
    ) {
        super(onInit, onResize, onBeforeRenderFrame, onAfterRenderFrame, onClose);
    }

    private Option<VulkanRenderEngineContext> engineContextOption = Option.none();
    private Option<Swapchain> swapchainOption = Option.none();

    @Override
    protected void init(GLFW glfw, GLFWwindow window) throws RenderException {
        VulkanRenderEngineContext cx = VulkanRenderEngineContext.create(glfw, window);
        engineContextOption = Option.some(cx);

        try (Arena arena = Arena.ofConfined()) {
            IntBuffer pWidthHeight = IntBuffer.allocate(arena, 2);
            glfw.glfwGetFramebufferSize(window, pWidthHeight, pWidthHeight.offset(1));

            int width = pWidthHeight.read(0);
            int height = pWidthHeight.read(1);
            swapchainOption = Option.some(Swapchain.create(cx, width, height));
            logger.info("交换链已创建");
        } catch (RenderException e) {
            logger.severe("无法创建交换链: " + e.getMessage());
            logger.warning("程序会继续运行, 但渲染结果将不会被输出");
        }
    }

    @Override
    protected void resize(int width, int height) throws RenderException {
        if (!(engineContextOption instanceof Option.Some<VulkanRenderEngineContext> cx)) {
            return;
        }
    }

    @Override
    protected void renderFrame() throws RenderException {
        if (!(engineContextOption instanceof Option.Some<VulkanRenderEngineContext> cx)) {
            return;
        }
    }

    @Override
    protected void close() {
        if (!(engineContextOption instanceof Option.Some<VulkanRenderEngineContext> cx)) {
            return;
        }
    }

    @Override
    public ObjectHandle createObject(ObjectCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public AttachmentHandle.Color createColorAttachment(AttachmentCreateInfo.Color info) throws RenderException {
        return null;
    }

    @Override
    public AttachmentHandle.Depth createDepthAttachment(AttachmentCreateInfo.Depth info) throws RenderException {
        return null;
    }

    @Override
    public UniformHandle.Sampler2D createTexture(TextureCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public UniformHandle createUniform(UniformCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public RenderPipelineHandle createPipeline(RenderPipelineCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public RenderTaskHandle createTask(RenderTaskCreateInfo info) throws RenderException {
        return null;
    }

    private static final Logger logger = Logger.getLogger(VulkanRenderEngine.class.getName());
}
