package chr.wgx.builtin.core.data;

public final class CoreData {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    public TriAxis waist;
    public TriAxis head;

    public final LegStatus[] leg;
    public final ArmStatus[] arm;
    public final HandStatus[] hand;

    public int timerBlinkInterval;
    public int timerCurrentValue;

    public CoreData() {
        waist = new TriAxis();
        head = new TriAxis();

        leg = new LegStatus[2];
        leg[LEFT] = new LegStatus();
        leg[RIGHT] = new LegStatus();

        arm = new ArmStatus[2];
        arm[LEFT] = new ArmStatus();
        arm[RIGHT] = new ArmStatus();

        hand = new HandStatus[2];
        hand[LEFT] = new HandStatus();
        hand[RIGHT] = new HandStatus();

        timerBlinkInterval = -1;
        timerCurrentValue = 0;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public CoreData clone() {
        CoreData ret = new CoreData();

        ret.waist = waist.clone();
        ret.head = head.clone();

        ret.leg[LEFT] = leg[LEFT].clone();
        ret.leg[RIGHT] = leg[RIGHT].clone();

        ret.arm[LEFT] = arm[LEFT].clone();
        ret.arm[RIGHT] = arm[RIGHT].clone();

        ret.hand[LEFT] = hand[LEFT].clone();
        ret.hand[RIGHT] = hand[RIGHT].clone();

        ret.timerBlinkInterval = timerBlinkInterval;
        ret.timerCurrentValue = timerCurrentValue;

        return ret;
    }
}
