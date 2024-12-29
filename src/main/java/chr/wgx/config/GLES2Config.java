package chr.wgx.config;

/// GLES2 渲染器配置
public final class GLES2Config {
    /// 是否启用多重采样抗锯齿
    public boolean enableMSAA = true;
    /// 是否启用线条平滑
    public boolean enableLineSmooth = true;
    /// 是否启用调试（如果 GL_KHR_debug 扩展可用）
    public boolean debug = false;

    public void detectJVMArgumentsOverride() {
        String debug = System.getProperty("wgx.gles2.debug");
        if (debug != null) {
            this.debug = !debug.isBlank();
        }
    }

    public static final GLES2Config DEFAULT = new GLES2Config();
}
