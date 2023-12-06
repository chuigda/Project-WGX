package tech.icey.babel;

import tech.icey.util.Tuple3;

import javax.swing.*;
import java.util.List;

public interface UIProvider {
    List<Tuple3<String, UIEntryPoint, JPanel>> provide();
}
