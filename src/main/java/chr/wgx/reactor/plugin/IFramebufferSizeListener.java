package chr.wgx.reactor.plugin;

public interface IFramebufferSizeListener extends IPlugin {
    void onFramebufferResize(int width, int height);
}
