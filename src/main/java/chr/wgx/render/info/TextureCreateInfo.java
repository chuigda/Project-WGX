package chr.wgx.render.info;

import java.awt.image.BufferedImage;

public final class TextureCreateInfo {
    public final BufferedImage image;
    public final boolean mipmap;

    public TextureCreateInfo(BufferedImage image, boolean mipmap) {
        this.image = image;
        this.mipmap = mipmap;
    }

    public TextureCreateInfo image(BufferedImage image) {
        return new TextureCreateInfo(image, mipmap);
    }
}
