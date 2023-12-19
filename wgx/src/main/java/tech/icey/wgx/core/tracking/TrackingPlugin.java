package tech.icey.wgx.core.tracking;

import tech.icey.wgx.babel.BabelPlugin;

import java.util.List;

public class TrackingPlugin implements BabelPlugin {
    @Override
    public String getName() {
        return "姿态控制模块";
    }

    @Override
    public String getDescription() {
        return "通过输入参数来控制基本的运动";
    }

    @Override
    public List<Object> getComponents() {
        return List.of();
    }
}
