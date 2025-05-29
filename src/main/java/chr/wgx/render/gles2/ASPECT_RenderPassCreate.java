package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import chr.wgx.render.gles2.data.GLES2TextureAttachment;
import chr.wgx.render.gles2.task.GLES2RenderPass;
import chr.wgx.render.info.RenderPassCreateInfo;
import club.doki7.gles2.GLES2;
import club.doki7.gles2.GLES2Constants;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.ptr.IntPtr;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.List;

public final class ASPECT_RenderPassCreate {
    ASPECT_RenderPassCreate(GLES2RenderEngine engine) {
        this.engine = engine;
    }

    GLES2RenderPass createRenderPassImpl(RenderPassCreateInfo info) throws RenderException {
        GLES2 gles2 = engine.gles2;

        List<GLES2TextureAttachment> colorTextureAttachments = info.colorAttachmentInfos.stream()
                .map(attachmentInfo -> (GLES2TextureAttachment) attachmentInfo.attachment)
                .toList();
        Option<GLES2TextureAttachment> depthTextureAttachment = info.depthAttachmentInfo
                .map(attachment -> (GLES2TextureAttachment) attachment.attachment);

        try (Arena arena = Arena.ofConfined()) {
            IntPtr pFramebufferObject = IntPtr.allocate(arena);
            gles2.genFramebuffers(1, pFramebufferObject);
            int framebufferObject = pFramebufferObject.read();

            gles2.bindFramebuffer(GLES2Constants.FRAMEBUFFER, framebufferObject);
            @EnumType(GLES2Constants.class) int currentAttachment = GLES2Constants.COLOR_ATTACHMENT0;
            for (GLES2TextureAttachment attachment : colorTextureAttachments) {
                gles2.framebufferTexture2D(
                        GLES2Constants.FRAMEBUFFER,
                        currentAttachment,
                        GLES2Constants.TEXTURE_2D,
                        attachment.textureObject,
                        0
                );
                currentAttachment++;
            }

            if (depthTextureAttachment instanceof Option.Some<GLES2TextureAttachment> some) {
                gles2.framebufferTexture2D(
                        GLES2Constants.FRAMEBUFFER,
                        GLES2Constants.DEPTH_ATTACHMENT,
                        GLES2Constants.TEXTURE_2D,
                        some.value.textureObject,
                        0
                );
            }

            int status = gles2.checkFramebufferStatus(GLES2Constants.FRAMEBUFFER);
            if (status != GLES2Constants.FRAMEBUFFER_COMPLETE) {
                throw new RenderException("未能创建完整的帧缓冲对象");
            }
            gles2.bindFramebuffer(GLES2Constants.FRAMEBUFFER, 0);

            GLES2RenderPass ret =  new GLES2RenderPass(
                    info,
                    colorTextureAttachments,
                    depthTextureAttachment,
                    framebufferObject
            );
            engine.renderPasses.add(ret);
            return ret;
        }
    }

    private final GLES2RenderEngine engine;
}
