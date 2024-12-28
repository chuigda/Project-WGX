package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import chr.wgx.render.gles2.task.GLES2RenderPass;
import tech.icey.gles2.GLES2;

public final class ASPECT_RenderFrame {
    ASPECT_RenderFrame(GLES2RenderEngine engine) {
        this.engine = engine;
    }

    void renderFrameImpl() throws RenderException {
        GLES2 gles2 = engine.gles2;

        for (GLES2RenderPass renderPass : engine.renderPasses) {
            if (renderPass.colorAttachments.size() != 1) {
                throw new RenderException("GLES2 渲染器暂不支持多个颜色附件");
            }
        }
    }

    private final GLES2RenderEngine engine;
}
