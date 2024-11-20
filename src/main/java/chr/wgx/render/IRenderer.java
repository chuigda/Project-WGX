package chr.wgx.render;

public interface IRenderer {
    void init() throws RenderException;
    void resize(int width, int height) throws RenderException;
    void renderFrame() throws RenderException;
    void close();
}
