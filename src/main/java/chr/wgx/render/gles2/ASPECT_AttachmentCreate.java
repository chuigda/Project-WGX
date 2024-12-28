package chr.wgx.render.gles2;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.Texture;
import chr.wgx.render.gles2.data.GLES2Attachment;
import chr.wgx.render.gles2.data.GLES2Texture;
import chr.wgx.render.info.AttachmentCreateInfo;
import tech.icey.gles2.GLES2;
import tech.icey.gles2.GLES2Constants;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.xjbutil.container.Pair;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class ASPECT_AttachmentCreate {
    ASPECT_AttachmentCreate(GLES2RenderEngine engine) {
        this.engine = engine;
    }

    Pair<Attachment, Texture> createColorAttachmentImpl(AttachmentCreateInfo info) {
        GLES2 gles2 = engine.gles2;

        try (Arena arena = Arena.ofConfined()) {
            int actualWidth = info.width == -1 ? engine.framebufferWidth : info.width;
            int actualHeight = info.height == -1 ? engine.framebufferHeight : info.height;

            IntBuffer pFBO = IntBuffer.allocate(arena);
            gles2.glGenFramebuffers(1, pFBO);
            int fbo = pFBO.read();

            IntBuffer pTexture = IntBuffer.allocate(arena);
            gles2.glGenTextures(1, pTexture);
            int textureObject = pTexture.read();

            gles2.glBindTexture(GLES2Constants.GL_TEXTURE_2D, textureObject);
            gles2.glTexImage2D(
                    GLES2Constants.GL_TEXTURE_2D,
                    0,
                    info.pixelFormat.glInternalFormat,
                    actualWidth,
                    actualHeight,
                    0,
                    info.pixelFormat.glFormat,
                    info.pixelFormat.glType,
                    MemorySegment.NULL
            );
            gles2.glTexParameteri(GLES2Constants.GL_TEXTURE_2D, GLES2Constants.GL_TEXTURE_MIN_FILTER, GLES2Constants.GL_LINEAR);
            gles2.glTexParameteri(GLES2Constants.GL_TEXTURE_2D, GLES2Constants.GL_TEXTURE_MAG_FILTER, GLES2Constants.GL_LINEAR);
            gles2.glTexParameteri(GLES2Constants.GL_TEXTURE_2D, GLES2Constants.GL_TEXTURE_WRAP_S, GLES2Constants.GL_CLAMP_TO_EDGE);
            gles2.glTexParameteri(GLES2Constants.GL_TEXTURE_2D, GLES2Constants.GL_TEXTURE_WRAP_T, GLES2Constants.GL_CLAMP_TO_EDGE);

            gles2.glBindFramebuffer(GLES2Constants.GL_FRAMEBUFFER, fbo);
            gles2.glFramebufferTexture2D(
                    GLES2Constants.GL_FRAMEBUFFER,
                    GLES2Constants.GL_COLOR_ATTACHMENT0,
                    GLES2Constants.GL_TEXTURE_2D,
                    textureObject,
                    0
            );
            gles2.glBindFramebuffer(GLES2Constants.GL_FRAMEBUFFER, 0);

            GLES2Attachment attachment = new GLES2Attachment(info, fbo);
            GLES2Texture texture = new GLES2Texture(true, info.pixelFormat, textureObject);

            engine.attachments.add(attachment);
            engine.textures.add(texture);
            if (info.width == -1) {
                engine.dynamicallySizedTextures.add(texture);
            }

            return new Pair<>(attachment, texture);
        }
    }

    public Attachment createDepthAttachmentImpl(AttachmentCreateInfo info) {
        GLES2 gles2 = engine.gles2;

        try (Arena arena = Arena.ofConfined()) {
            int actualWidth = info.width == -1 ? engine.framebufferWidth : info.width;
            int actualHeight = info.height == -1 ? engine.framebufferHeight : info.height;

            IntBuffer pFBO = IntBuffer.allocate(arena);
            gles2.glGenFramebuffers(1, pFBO);
            int fbo = pFBO.read();

            IntBuffer pTexture = IntBuffer.allocate(arena);
            gles2.glGenTextures(1, pTexture);
            int textureObject = pTexture.read();

            gles2.glBindTexture(GLES2Constants.GL_TEXTURE_2D, textureObject);
            gles2.glTexImage2D(
                    GLES2Constants.GL_TEXTURE_2D,
                    0,
                    info.pixelFormat.glInternalFormat,
                    actualWidth,
                    actualHeight,
                    0,
                    info.pixelFormat.glFormat,
                    info.pixelFormat.glType,
                    MemorySegment.NULL
            );

            gles2.glTexParameteri(GLES2Constants.GL_TEXTURE_2D, GLES2Constants.GL_TEXTURE_MIN_FILTER, GLES2Constants.GL_LINEAR);
            gles2.glTexParameteri(GLES2Constants.GL_TEXTURE_2D, GLES2Constants.GL_TEXTURE_MAG_FILTER, GLES2Constants.GL_LINEAR);
            gles2.glTexParameteri(GLES2Constants.GL_TEXTURE_2D, GLES2Constants.GL_TEXTURE_WRAP_S, GLES2Constants.GL_CLAMP_TO_EDGE);
            gles2.glTexParameteri(GLES2Constants.GL_TEXTURE_2D, GLES2Constants.GL_TEXTURE_WRAP_T, GLES2Constants.GL_CLAMP_TO_EDGE);

            gles2.glBindFramebuffer(GLES2Constants.GL_FRAMEBUFFER, fbo);
            gles2.glFramebufferTexture2D(
                    GLES2Constants.GL_FRAMEBUFFER,
                    GLES2Constants.GL_DEPTH_ATTACHMENT,
                    GLES2Constants.GL_TEXTURE_2D,
                    textureObject,
                    0
            );
            gles2.glBindFramebuffer(GLES2Constants.GL_FRAMEBUFFER, 0);

            GLES2Attachment attachment = new GLES2Attachment(info, fbo);
            GLES2Texture texture = new GLES2Texture(true, info.pixelFormat, textureObject);

            engine.attachments.add(attachment);
            engine.textures.add(texture);
            if (info.width == -1) {
                engine.dynamicallySizedTextures.add(texture);
            }

            return attachment;
        }
    }

    private final GLES2RenderEngine engine;
}
