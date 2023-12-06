package tech.icey.babel;

import tech.icey.util.Radioactive;

import java.util.HashMap;

import static tech.icey.util.RuntimeError.runtimeError;

public final class Bible {
    public void publishData(String ident, Radioactive data) {
        if (sharedData.containsKey(ident)) {
            runtimeError("发布数据 %s 时遇到错误: 数据已经存在。这可能表明某个插件存在问题，或者某些插件之间不兼容。", ident);
        }

        sharedData.put(ident, data);
    }

    public Radioactive getPublishedData(String ident) {
        if (!sharedData.containsKey(ident)) {
            runtimeError("获取数据 %s 时遇到错误: 数据不存在。这可能表明某个插件存在问题，或者你没有安装某些插件的依赖项目。", ident);
        }

        return sharedData.get(ident);
    }

    private final HashMap<String, Radioactive> sharedData = new HashMap<>();
}
