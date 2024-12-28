package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import chr.wgx.render.common.Color;
import chr.wgx.render.data.Attachment;
import chr.wgx.render.gles2.data.GLES2TextureAttachment;
import chr.wgx.render.gles2.task.GLES2RenderPass;
import tech.icey.gles2.GLES2;
import tech.icey.gles2.GLES2Constants;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.List;

public final class ASPECT_RenderPassCreate {
    ASPECT_RenderPassCreate(GLES2RenderEngine engine) {
        this.engine = engine;
    }

    GLES2RenderPass createRenderPassImpl(
            String renderPassName,
            int priority,
            List<Attachment> colorAttachments,
            List<Color> clearColors,
            Option<Attachment> depthAttachment
    ) throws RenderException {
        GLES2 gles2 = engine.gles2;

        List<GLES2TextureAttachment> colorTextureAttachments = colorAttachments.stream()
                .map(attachment -> (GLES2TextureAttachment) attachment)
                .toList();
        Option<GLES2TextureAttachment> depthTextureAttachment = depthAttachment
                .map(attachment -> (GLES2TextureAttachment) attachment);

        try (Arena arena = Arena.ofConfined()) {
            IntBuffer pFramebufferObject = IntBuffer.allocate(arena);
            gles2.glGenFramebuffers(1, pFramebufferObject);
            int framebufferObject = pFramebufferObject.read();

            gles2.glBindFramebuffer(GLES2Constants.GL_FRAMEBUFFER, framebufferObject);
            @enumtype(GLES2Constants.class) int currentAttachment = GLES2Constants.GL_COLOR_ATTACHMENT0;
            for (GLES2TextureAttachment attachment : colorTextureAttachments) {
                gles2.glFramebufferTexture2D(
                        GLES2Constants.GL_FRAMEBUFFER,
                        currentAttachment,
                        GLES2Constants.GL_TEXTURE_2D,
                        attachment.textureObject,
                        0
                );
                currentAttachment++;
            }

            if (depthTextureAttachment instanceof Option.Some<GLES2TextureAttachment> some) {
                gles2.glFramebufferTexture2D(
                        GLES2Constants.GL_FRAMEBUFFER,
                        GLES2Constants.GL_DEPTH_ATTACHMENT,
                        GLES2Constants.GL_TEXTURE_2D,
                        some.value.textureObject,
                        0
                );
            }

            int status = gles2.glCheckFramebufferStatus(GLES2Constants.GL_FRAMEBUFFER);
            if (status != GLES2Constants.GL_FRAMEBUFFER_COMPLETE) {
                throw new RenderException("未能创建完整的帧缓冲对象");
            }
            gles2.glBindFramebuffer(GLES2Constants.GL_FRAMEBUFFER, 0);

            GLES2RenderPass ret =  new GLES2RenderPass(
                    renderPassName,
                    priority,
                    colorTextureAttachments,
                    clearColors,
                    depthTextureAttachment,
                    framebufferObject
            );
            engine.renderPasses.add(ret);
            return ret;
        }
    }

    private final GLES2RenderEngine engine;
}
