package chr.wgx.render.info;

public abstract sealed class ShaderProgram {
    public static final class GLES2 extends ShaderProgram {
        public final String vertexShader;
        public final String fragmentShader;

        public GLES2(String vertexShader, String fragmentShader) {
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
        }
    }

    public static final class Vulkan extends ShaderProgram {
        public final byte[] vertexShader;
        public final byte[] fragmentShader;

        public Vulkan(byte[] vertexShader, byte[] fragmentShader) {
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
        }
    }
}
