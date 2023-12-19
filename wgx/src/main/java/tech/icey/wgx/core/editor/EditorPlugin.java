package tech.icey.wgx.core.editor;

import tech.icey.util.Pair;
import tech.icey.wgx.babel.BabelPlugin;
import tech.icey.wgx.babel.UIComponent;
import tech.icey.wgx.babel.UIProvider;

import java.util.List;

final class EditorComponent implements UIProvider {
    @Override
    public List<Pair<String, UIComponent>> provide() {
        return List.of(new Pair<>(
        		"TextEditor",
                 new UIComponent.MenuItem(editor, "工具", "文本编辑器")
        ));
    }

    private final SimpleEditor editor = new SimpleEditor();
}

public final class EditorPlugin implements BabelPlugin {
    @Override
    public String getName() {
        return EditorPlugin.class.getName();
    }

    @Override
    public String getDescription() {
        return "一个包含基本功能的编辑器，用来演示插件系统的功能";
    }

    @Override
    public List<Object> getComponents() {
        return List.of(
                new EditorComponent()
        );
    }
}
