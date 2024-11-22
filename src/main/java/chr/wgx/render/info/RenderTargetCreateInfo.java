package chr.wgx.render.info;

/**
 * @param width     use {@code -1} to use the size of the window framebuffer
 * @param height    use {@code -1} to use the size of the window framebuffer
 * @param depthTest create a depth buffer for depth test purpose
 */
public record RenderTargetCreateInfo(int width, int height, boolean depthTest) {
}
