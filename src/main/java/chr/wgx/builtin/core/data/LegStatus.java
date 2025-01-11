package chr.wgx.builtin.core.data;

public final class LegStatus {
    // 髋关节
    public float hipLeftRight = 0.0f;
    public float hipRotate = 0.0f;

    // 膝关节
    public float kneeBend = 0.0f;

    // 踝关节
    public float ankleLeftRight = 0.0f;
    public float ankleBend = 0.0f;

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public LegStatus clone() {
        LegStatus ret = new LegStatus();

        ret.hipLeftRight = hipLeftRight;
        ret.hipRotate = hipRotate;

        ret.kneeBend = kneeBend;

        ret.ankleLeftRight = ankleLeftRight;
        ret.ankleBend = ankleBend;

        return ret;
    }
}
