package tech.icey.bekhave;

import tech.icey.util.Pair;

import javax.swing.*;
import java.util.List;

public interface UIProvider {
    enum DisplayPosition {
        PluginMenu,
        Puppetering,
        ScreenAnimation,
        BodyAnimation
    }

    List<Pair<JPanel, DisplayPosition>> provide();
}
