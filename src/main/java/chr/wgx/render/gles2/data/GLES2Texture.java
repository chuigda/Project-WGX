package chr.wgx.render.gles2.data;

import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.data.Texture;

public final class GLES2Texture extends Texture {
    public final PixelFormat pixelFormat;
    public final int textureObject;

    public GLES2Texture(boolean isAttachment, PixelFormat pixelFormat, int textureObject) {
        super(isAttachment);
        this.pixelFormat = pixelFormat;
        this.textureObject = textureObject;
    }
}
