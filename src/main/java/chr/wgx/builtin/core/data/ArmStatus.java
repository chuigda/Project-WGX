package chr.wgx.builtin.core.data;

public final class ArmStatus {
    // 胸锁关节
    public float sternoclavicularUpDown = 0.0f;
    public float sternoclavicularForwardBack = 0.0f;
    public float sternoclavicularInOut = 0.0f;

    // 肩关节
    public float shoulderRotate = 0.0f;
    public float upperArmRaise = 0.0f;

    // 肘关节
    public float elbowBend = 0.0f;
    public float elbowRotate = 0.0f;

    // 腕关节
    public float wristLeftRight = 0.0f;
    public float wristBend = 0.0f;

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public ArmStatus clone() {
        ArmStatus ret = new ArmStatus();

        ret.sternoclavicularUpDown = sternoclavicularUpDown;
        ret.sternoclavicularForwardBack = sternoclavicularForwardBack;
        ret.sternoclavicularInOut = sternoclavicularInOut;

        ret.shoulderRotate = shoulderRotate;
        ret.upperArmRaise = upperArmRaise;

        ret.elbowBend = elbowBend;
        ret.elbowRotate = elbowRotate;

        ret.wristLeftRight = wristLeftRight;
        ret.wristBend = wristBend;

        return ret;
    }
}
