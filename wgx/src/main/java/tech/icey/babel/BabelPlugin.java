package tech.icey.babel;

import java.util.List;

public interface BabelPlugin {
    String getName();

    String getDescription();

    List<Object> getComponents();
}
