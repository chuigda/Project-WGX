package chr.wgx.render.gles2.data;

public final class UniformLocation {
    public final String uniformName;
    public final int location;

    public UniformLocation(String uniformName, int location) {
        this.uniformName = uniformName;
        this.location = location;
    }
}
