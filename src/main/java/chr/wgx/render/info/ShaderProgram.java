package chr.wgx.render.info;

public abstract sealed class ShaderProgram {
    public static final class GLES2 extends ShaderProgram {
        private final String vertexShader;
        private final String fragmentShader;

        public GLES2(String vertexShader, String fragmentShader) {
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
        }

        public String vertexShader() {
            return vertexShader;
        }

        public String fragmentShader() {
            return fragmentShader;
        }
    }

    public static final class Vulkan extends ShaderProgram {
        private final byte[] vertexShader;
        private final byte[] fragmentShader;

        public Vulkan(byte[] vertexShader, byte[] fragmentShader) {
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
        }

        public byte[] vertexShader() {
            return vertexShader;
        }

        public byte[] fragmentShader() {
            return fragmentShader;
        }
    }
}
