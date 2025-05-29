package chr.wgx.render;

import club.doki7.glfw.GLFW;

public interface IRenderEngineFactory {
    RenderWindow createRenderWindow(GLFW glfw, String title, int width, int height) throws RenderException;
}
