package chr.wgx.render;

import tech.icey.glfw.GLFW;

public interface IRenderEngineFactory {
    RenderWindow createRenderWindow(GLFW glfw, String title, int width, int height) throws RenderException;
}
