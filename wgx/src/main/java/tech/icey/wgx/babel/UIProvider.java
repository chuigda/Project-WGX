package tech.icey.wgx.babel;

import tech.icey.util.Pair;
import java.util.List;

public interface UIProvider {
    List<Pair<String, UIComponent>> provide();
}
