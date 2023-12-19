package tech.icey.wgx.core.tracking;

import tech.icey.r77.math.Vector3;
import tech.icey.util.Pair;
import tech.icey.wgx.babel.DataManipulator;
import tech.icey.wgx.babel.Masterpiece;
import tech.icey.wgx.babel.UIComponent;
import tech.icey.wgx.babel.UIProvider;

import java.util.List;

public final class TrackingControlComponent implements UIProvider, DataManipulator {
    @Override
    public int priority() {
        return 1000;
    }

    @Override
    public void initialise(Masterpiece masterpiece) {}

    @Override
    public void manipulate(Masterpiece masterpiece) {
        Vector3 translationLimit = this.translationLimit;
        Vector3 rotationLimit = this.faceAngleLimit;

        if (masterpiece.trackingParam.dirty()) {
            masterpiece.trackingParam.setTranslation(
                    masterpiece.trackingParam.getTranslation().threshold(translationLimit)
            );
            masterpiece.trackingParam.setFaceAngle(
                    masterpiece.trackingParam.getFaceAngle().threshold(rotationLimit)
            );
        }
    }

    @Override
    public List<Pair<String, UIComponent>> provide() {
        return List.of(
                new Pair<>(
                        "TrackingControl",
                        new UIComponent.MenuItem(
                                trackingControlWindow,
                                "动作",
                                "姿态控制"
                        )
                )
        );
    }

    private final TrackingControlWindow trackingControlWindow = new TrackingControlWindow(
            translationLimit -> { this.translationLimit = translationLimit; return null; },
            faceAngleLimit -> { this.faceAngleLimit = faceAngleLimit; return null; }
    );

    private volatile Vector3 translationLimit = new Vector3(100, 100, 100);
    private volatile Vector3 faceAngleLimit = new Vector3(90, 90, 90);
}
