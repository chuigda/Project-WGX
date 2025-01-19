package chr.wgx.builtin.core.behave;

import chr.wgx.builtin.BehaviorPriorities;
import chr.wgx.builtin.core.data.ArmStatus;
import chr.wgx.builtin.core.data.CoreData;
import chr.wgx.builtin.core.data.LegStatus;
import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPluginBehavior;
import tech.icey.xjbutil.container.Ref;

import java.util.ArrayDeque;
import java.util.Deque;

public class Averager implements IPluginBehavior {
    public Averager(
            CoreData coreDataRef,
            Ref<Boolean> coreDataProgramUpdatedRef,
            Ref<Boolean> enabled,
            Ref<Integer> frameCount
    ) {
        this.coreDataRef = coreDataRef;
        this.coreDataProgramUpdatedRef = coreDataProgramUpdatedRef;
        this.enabled = enabled;
        this.frameCount = frameCount;
    }

    @Override
    public String name() {
        return "WGC_Averager";
    }

    @Override
    public String description() {
        return "对 " + CoreData.class.getCanonicalName() + " 中的数据进行均值化处理，从而平滑数据\n"
                + "注意：若要在其他行为中使用均值化后的数据，请确保该行为的优先级高于 Averager (" + priority() + ")";
    }

    @Override
    public int priority() {
        return BehaviorPriorities.CORE_AVERAGER;
    }

    @Override
    public void tick(Reactor reactor) {
        if (!enabled.value) {
            return;
        }

        if (!coreDataProgramUpdatedRef.value) {
            return;
        }

        popIfExceeding();

        frameDataList.add(coreDataRef.clone());

        coreDataRef.waist.rotate = 0.0f;
        coreDataRef.waist.pitch = 0.0f;
        coreDataRef.waist.yaw = 0.0f;

        coreDataRef.head.rotate = 0.0f;
        coreDataRef.head.pitch = 0.0f;
        coreDataRef.head.yaw = 0.0f;

        for (int i = 0; i <= 1; i++) {
            LegStatus legStatus = coreDataRef.leg[i];
            ArmStatus armStatus = coreDataRef.arm[i];

            legStatus.hipLeftRight = 0.0f;
            legStatus.hipRotate = 0.0f;
            legStatus.kneeBend = 0.0f;
            legStatus.ankleLeftRight = 0.0f;
            legStatus.ankleBend = 0.0f;

            armStatus.sternoclavicularUpDown = 0.0f;
            armStatus.sternoclavicularForwardBack = 0.0f;
            armStatus.sternoclavicularInOut = 0.0f;
            armStatus.shoulderRotate = 0.0f;
            armStatus.upperArmRaise = 0.0f;
            armStatus.elbowBend = 0.0f;
            armStatus.elbowRotate = 0.0f;
            armStatus.wristLeftRight = 0.0f;
            armStatus.wristBend = 0.0f;
        }

        for (CoreData frameData : frameDataList) {
            coreDataRef.waist.rotate += frameData.waist.rotate;
            coreDataRef.waist.pitch += frameData.waist.pitch;
            coreDataRef.waist.yaw += frameData.waist.yaw;

            coreDataRef.head.rotate += frameData.head.rotate;
            coreDataRef.head.pitch += frameData.head.pitch;
            coreDataRef.head.yaw += frameData.head.yaw;

            for (int i = 0; i <= 1; i++) {
                LegStatus legStatus = coreDataRef.leg[i];
                LegStatus frameLegStatus = frameData.leg[i];
                ArmStatus armStatus = coreDataRef.arm[i];
                ArmStatus frameArmStatus = frameData.arm[i];

                legStatus.hipLeftRight += frameLegStatus.hipLeftRight;
                legStatus.hipRotate += frameLegStatus.hipRotate;
                legStatus.kneeBend += frameLegStatus.kneeBend;
                legStatus.ankleLeftRight += frameLegStatus.ankleLeftRight;
                legStatus.ankleBend += frameLegStatus.ankleBend;

                armStatus.sternoclavicularUpDown += frameArmStatus.sternoclavicularUpDown;
                armStatus.sternoclavicularForwardBack += frameArmStatus.sternoclavicularForwardBack;
                armStatus.sternoclavicularInOut += frameArmStatus.sternoclavicularInOut;
                armStatus.shoulderRotate += frameArmStatus.shoulderRotate;
                armStatus.upperArmRaise += frameArmStatus.upperArmRaise;
                armStatus.elbowBend += frameArmStatus.elbowBend;
                armStatus.elbowRotate += frameArmStatus.elbowRotate;
                armStatus.wristLeftRight += frameArmStatus.wristLeftRight;
                armStatus.wristBend += frameArmStatus.wristBend;
            }
        }

        float frameCountFloat = (float) frameCount.value;
        coreDataRef.waist.rotate /= frameCountFloat;
        coreDataRef.waist.pitch /= frameCountFloat;
        coreDataRef.waist.yaw /= frameCountFloat;

        coreDataRef.head.rotate /= frameCountFloat;
        coreDataRef.head.pitch /= frameCountFloat;
        coreDataRef.head.yaw /= frameCountFloat;

        for (int i = 0; i <= 1; i++) {
            LegStatus legStatus = coreDataRef.leg[i];
            ArmStatus armStatus = coreDataRef.arm[i];

            legStatus.hipLeftRight /= frameCountFloat;
            legStatus.hipRotate /= frameCountFloat;
            legStatus.kneeBend /= frameCountFloat;
            legStatus.ankleLeftRight /= frameCountFloat;
            legStatus.ankleBend /= frameCountFloat;

            armStatus.sternoclavicularUpDown /= frameCountFloat;
            armStatus.sternoclavicularForwardBack /= frameCountFloat;
            armStatus.sternoclavicularInOut /= frameCountFloat;
            armStatus.shoulderRotate /= frameCountFloat;
            armStatus.upperArmRaise /= frameCountFloat;
            armStatus.elbowBend /= frameCountFloat;
            armStatus.elbowRotate /= frameCountFloat;
            armStatus.wristLeftRight /= frameCountFloat;
            armStatus.wristBend /= frameCountFloat;
        }
    }

    private void popIfExceeding() {
        if (frameDataList.size() >= frameCount.value) {
            frameDataList.pop();
        }
    }

    private final CoreData coreDataRef;
    private final Ref<Boolean> coreDataProgramUpdatedRef;

    private final Ref<Boolean> enabled;
    private final Ref<Integer> frameCount;
    private final Deque<CoreData> frameDataList = new ArrayDeque<>();
}
