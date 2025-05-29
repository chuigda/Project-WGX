package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.Texture;
import chr.wgx.render.gles2.data.GLES2Texture;
import chr.wgx.render.gles2.data.GLES2TextureAttachment;
import chr.wgx.render.info.AttachmentCreateInfo;
import club.doki7.gles2.GLES2;
import club.doki7.gles2.GLES2Constants;
import club.doki7.ffm.ptr.IntPtr;
import tech.icey.xjbutil.container.Pair;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class ASPECT_AttachmentCreate {
    ASPECT_AttachmentCreate(GLES2RenderEngine engine) {
        this.engine = engine;
    }

    Pair<Attachment, Texture> createColorAttachmentImpl(AttachmentCreateInfo info) throws RenderException {
        GLES2 gles2 = engine.gles2;

        try (Arena arena = Arena.ofConfined()) {
            int actualWidth = info.width == -1 ? engine.framebufferWidth : info.width;
            int actualHeight = info.height == -1 ? engine.framebufferHeight : info.height;

            IntPtr pTexture = IntPtr.allocate(arena);
            gles2.genTextures(1, pTexture);
            int textureObject = pTexture.read();

            gles2.bindTexture(GLES2Constants.TEXTURE_2D, textureObject);
            gles2.texImage2D(
                    GLES2Constants.TEXTURE_2D,
                    0,
                    info.pixelFormat.glInternalFormat,
                    actualWidth,
                    actualHeight,
                    0,
                    info.pixelFormat.glFormat,
                    info.pixelFormat.glType,
                    MemorySegment.NULL
            );
            gles2.texParameteri(GLES2Constants.TEXTURE_2D, GLES2Constants.TEXTURE_MIN_FILTER, GLES2Constants.LINEAR);
            gles2.texParameteri(GLES2Constants.TEXTURE_2D, GLES2Constants.TEXTURE_MAG_FILTER, GLES2Constants.LINEAR);
            gles2.texParameteri(GLES2Constants.TEXTURE_2D, GLES2Constants.TEXTURE_WRAP_S, GLES2Constants.CLAMP_TO_EDGE);
            gles2.texParameteri(GLES2Constants.TEXTURE_2D, GLES2Constants.TEXTURE_WRAP_T, GLES2Constants.CLAMP_TO_EDGE);

            int status = gles2.getError();
            if (status != GLES2Constants.NO_ERROR) {
                throw new RenderException("创建纹理失败: " + status);
            }

            GLES2TextureAttachment attachment = new GLES2TextureAttachment(info, textureObject);
            GLES2Texture texture = new GLES2Texture(true, info.pixelFormat, textureObject);

            engine.attachments.add(attachment);
            engine.textures.add(texture);
            if (info.width == -1) {
                engine.dynamicallySizedTextures.add(texture);
            }

            return new Pair<>(attachment, texture);
        }
    }

    public Attachment createDepthAttachmentImpl(AttachmentCreateInfo info) throws RenderException {
        GLES2 gles2 = engine.gles2;

        try (Arena arena = Arena.ofConfined()) {
            int actualWidth = info.width == -1 ? engine.framebufferWidth : info.width;
            int actualHeight = info.height == -1 ? engine.framebufferHeight : info.height;

            IntPtr pTexture = IntPtr.allocate(arena);
            gles2.genTextures(1, pTexture);
            int textureObject = pTexture.read();

            gles2.bindTexture(GLES2Constants.TEXTURE_2D, textureObject);
            gles2.texImage2D(
                    GLES2Constants.TEXTURE_2D,
                    0,
                    info.pixelFormat.glInternalFormat,
                    actualWidth,
                    actualHeight,
                    0,
                    info.pixelFormat.glFormat,
                    info.pixelFormat.glType,
                    MemorySegment.NULL
            );

            gles2.texParameteri(GLES2Constants.TEXTURE_2D, GLES2Constants.TEXTURE_MIN_FILTER, GLES2Constants.LINEAR);
            gles2.texParameteri(GLES2Constants.TEXTURE_2D, GLES2Constants.TEXTURE_MAG_FILTER, GLES2Constants.LINEAR);
            gles2.texParameteri(GLES2Constants.TEXTURE_2D, GLES2Constants.TEXTURE_WRAP_S, GLES2Constants.CLAMP_TO_EDGE);
            gles2.texParameteri(GLES2Constants.TEXTURE_2D, GLES2Constants.TEXTURE_WRAP_T, GLES2Constants.CLAMP_TO_EDGE);

            GLES2TextureAttachment attachment = new GLES2TextureAttachment(info, textureObject);
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
