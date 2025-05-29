package chr.wgx.render.gles2.data;

import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.data.Texture;
import chr.wgx.render.gles2.IGLES2Disposable;
import club.doki7.gles2.GLES2;
import club.doki7.ffm.buffer.IntBuffer;

import java.lang.foreign.Arena;

public final class GLES2Texture extends Texture implements IGLES2Disposable {
    public final PixelFormat pixelFormat;
    public final int textureObject;

    public GLES2Texture(boolean isAttachment, PixelFormat pixelFormat, int textureObject) {
        super(isAttachment);
        this.pixelFormat = pixelFormat;
        this.textureObject = textureObject;
    }

    @Override
    public void dispose(GLES2 gles2) {
        try (Arena arena = Arena.ofConfined()) {
            IntBuffer pTexture = IntBuffer.allocate(arena);
            pTexture.write(textureObject);

            gles2.glDeleteTextures(1, pTexture);
        }
    }
}
