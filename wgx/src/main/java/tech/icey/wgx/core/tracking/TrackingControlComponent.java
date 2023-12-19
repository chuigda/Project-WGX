package tech.icey.wgx.core.tracking;

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
    public void initialise(Masterpiece masterpiece) {
    }

    @Override
    public void manipulate(Masterpiece masterpiece) {
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

    private final TrackingControlWindow trackingControlWindow = new TrackingControlWindow();
}
