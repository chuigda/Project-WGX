package tech.icey.wgx.core;

import tech.icey.util.Pair;
import tech.icey.wgx.babel.BabelPlugin;
import tech.icey.wgx.babel.UIComponent;
import tech.icey.wgx.babel.UIProvider;

import java.util.List;

final class ExampleComponent implements UIProvider {
    @Override
    public List<Pair<String, UIComponent>> provide() {
        return List.of(
                new Pair<>(
                        "ExampleEditor",
                        new UIComponent.MenuItem(
                                new SimpleEditor(),
                                "示例菜单",
                                "文本编辑器"

                        )
                )
        );
    }
}

public final class ExamplePlugin implements BabelPlugin {
    @Override
    public String getName() {
        return "Example plugin";
    }

    @Override
    public String getDescription() {
        return "用于演示插件系统功能的 plugin";
    }

    @Override
    public List<Object> getComponents() {
        return List.of(
                new ExampleComponent()
        );
    }
}
