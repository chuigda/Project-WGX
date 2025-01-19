package chr.wgx.builtin;

public final class BehaviorPriorities {
    /// @see chr.wgx.builtin.core.behave.StateInitializer
    public static final int CORE_STATE_INITIALIZER = Integer.MIN_VALUE;

    /// @see chr.wgx.builtin.osf.DataUpdater
    public static final int OSF_DATA_UPDATER = 0;

    /// @see chr.wgx.builtin.core.behave.Averager
    public static final int CORE_AVERAGER = 5000;

    /// @see chr.wgx.builtin.core.behave.CameraConfigUpdater
    public static final int CORE_DATA_CAMERA_CONFIG_UPDATER = 7000;
}
