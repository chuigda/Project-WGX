package tech.icey.wgx.babel;

import tech.icey.r77.math.Matrix4x4;
import tech.icey.util.Radioactive;
import tech.icey.util.RadioactiveRef;

import java.util.HashMap;

import static tech.icey.util.RuntimeError.runtimeError;

public final class Masterpiece {
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

    public void clearDirty() {
        modelPosition.clearDirty();
        jointStatus.clearDirty();
        trackingParam.clearDirty();

        viewMatrix.clearDirty();

        for (Radioactive item : sharedData.values()) {
            item.clearDirty();
        }
    }
    
    public final ModelPosition modelPosition = new ModelPosition();
    public final JointStatus jointStatus = new JointStatus();
    public final TrackingParam trackingParam = new TrackingParam();
    public final RadioactiveRef<Matrix4x4> viewMatrix = new RadioactiveRef<>(Matrix4x4.IDENTITY);
    private final HashMap<String, Radioactive> sharedData = new HashMap<>();
}
