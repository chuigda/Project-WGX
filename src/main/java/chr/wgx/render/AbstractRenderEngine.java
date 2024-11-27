package chr.wgx.render;

import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action1;
import tech.icey.xjbutil.functional.Action2;

import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractRenderEngine {
    private final AtomicLong handleCounter = new AtomicLong(1024); // 保留一些低位用于内置对象
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

    public final long nextHandle() {
        return handleCounter.getAndIncrement();
    }

    public final void initEngine(GLFW glfw, GLFWwindow window) throws RenderException {
        init(glfw, window);
        onInit.apply(this);
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

    /// 初始化渲染器
    ///
    /// @param glfw GLFW 函数
    /// @param window GLFW 窗口
    /// @throws RenderException 如果初始化失败，实现应抛出异常
    protected abstract void init(GLFW glfw, GLFWwindow window) throws RenderException;
    /// 当窗口大小改变时，这个函数会被调用。渲染器实现应重写这个函数以处理窗口大小改变的情况，
    /// 例如调用 {@code glViewport} 或者重建 Vulkan 交换链
    ///
    /// @param width 新的帧缓冲宽度
    /// @param height 新的帧缓冲高度
    /// @throws RenderException 如果处理失败，实现应抛出异常
    protected abstract void resize(int width, int height) throws RenderException;
    /// 当需要渲染一帧时，这个函数会被调用。渲染器实现应重写这个函数以渲染一帧
    protected abstract void renderFrame() throws RenderException;
    /// 当窗口将被关闭，渲染器将要被销毁时，这个函数会被调用。渲染器实现应重写这个函数以释放资源
    protected abstract void close();

    /// 上传一个对象（顶点缓冲）到渲染器，这个函数会阻塞直到对象上传完成
    ///
    /// @param info 对象创建信息
    /// @return 对象句柄
    /// @throws RenderException 如果创建失败，实现应抛出异常
    public abstract ObjectHandle createObject(ObjectCreateInfo info) throws RenderException;
    /// 创建一个颜色附件，这个函数会阻塞直到附件创建完成
    ///
    /// @param i 附件创建信息
    /// @return 附件句柄
    /// @throws RenderException 如果创建失败，实现应抛出异常
    public abstract AttachmentHandle.Color createColorAttachment(AttachmentCreateInfo.Color i) throws RenderException;
    /// 创建一个深度附件，这个函数会阻塞直到附件创建完成
    ///
    /// @param i 附件创建信息
    /// @return 附件句柄
    /// @throws RenderException 如果创建失败，实现应抛出异常
    public abstract AttachmentHandle.Depth createDepthAttachment(AttachmentCreateInfo.Depth i) throws RenderException;
    /// 获取默认的颜色和深度附件，可以理解为 OpenGL 中“默认帧缓冲”的概念，这个函数不应该阻塞
    ///
    /// @return 默认的颜色和深度附件
    public abstract Pair<AttachmentHandle.Color, AttachmentHandle.Depth> getDefaultAttachments();

    /// 上传一张纹理，这个函数会阻塞直到纹理上传完成
    ///
    /// @param info 纹理创建信息
    /// @return 纹理句柄
    /// @throws RenderException 如果创建失败，实现应抛出异常
    public abstract UniformHandle.Sampler2D createTexture(TextureCreateInfo info) throws RenderException;
    /// 创建一个 Uniform 缓冲，这个函数会阻塞直到缓冲创建完成
    ///
    /// @param info Uniform 创建信息
    /// @return Uniform 句柄
    /// @throws RenderException 如果创建失败，实现应抛出异常
    public abstract UniformHandle createUniform(UniformCreateInfo info) throws RenderException;
    /// 创建一个渲染管线，这个函数会阻塞直到管线创建完成
    ///
    /// @param info 渲染管线创建信息
    /// @return 渲染管线句柄
    /// @throws RenderException 如果创建失败，实现应抛出异常
    public abstract RenderPipelineHandle createPipeline(RenderPipelineCreateInfo info) throws RenderException;
    /// 创建一个渲染任务，这个函数会阻塞直到任务创建完成
    ///
    /// @param info 渲染任务创建信息
    /// @return 渲染任务句柄
    /// @throws RenderException 如果创建失败，实现应抛出异常
    public abstract RenderTaskHandle createTask(RenderTaskInfo info) throws RenderException;
}
