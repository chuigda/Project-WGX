package chr.wgx.builtin.core.data;

public final class TriAxis {
    public float rotate = 0.0f;
    public float pitch = 0.0f;
    public float yaw = 0.0f;

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public TriAxis clone() {
        TriAxis ret = new TriAxis();

        ret.rotate = rotate;
        ret.pitch = pitch;
        ret.yaw = yaw;

        return ret;
    }
}
