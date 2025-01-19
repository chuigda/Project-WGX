package chr.wgx.builtin.core.behave;

import chr.wgx.builtin.BehaviorPriorities;
import chr.wgx.builtin.core.data.CameraConfig;
import chr.wgx.builtin.core.widget.CameraConfigWidget;
import chr.wgx.reactor.Radioactive;
import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPluginBehavior;
import org.joml.Vector3f;
import tech.icey.xjbutil.container.Ref;

import javax.swing.*;

public final class CameraConfigUpdater implements IPluginBehavior {
    public CameraConfigUpdater(
            Radioactive<CameraConfig> cameraConfigRef,
            Ref<Boolean> cameraConfigProgramUpdated,
            CameraConfigWidget cameraConfigWidget
    ) {
        this.cameraConfigRef = cameraConfigRef;
        this.cameraConfigProgramUpdated = cameraConfigProgramUpdated;
        this.cameraConfigWidget = cameraConfigWidget;
    }

    @Override
    public String name() {
        return "WGC_WidgetDataUpdater";
    }

    @Override
    public String description() {
        return "用于同步 Reactor 中的数据和 UI 上实际显示的数据";
    }

    @Override
    public int priority() {
        return BehaviorPriorities.CORE_DATA_CAMERA_CONFIG_UPDATER;
    }

    @Override
    public void tick(Reactor reactor) {
        if (cameraConfigProgramUpdated.value) {
            float fov = cameraConfigRef.value.fov;
            Vector3f cameraPosition = new Vector3f(cameraConfigRef.value.cameraPosition);
            Vector3f lookAtPosition = new Vector3f(cameraConfigRef.value.lookAtPosition);

            SwingUtilities.invokeLater(() -> {
                cameraConfigWidget.isProgramUpdated.setSelected(true);

                cameraConfigWidget.fovField.setEnabled(false);
                cameraConfigWidget.cameraPosEditor.setEnabled(false);
                cameraConfigWidget.lookAtPosEditor.setEnabled(false);

                cameraConfigWidget.fovField.setValue(fov);
                cameraConfigWidget.cameraPosEditor.setValue(cameraPosition);
                cameraConfigWidget.lookAtPosEditor.setValue(lookAtPosition);
            });
        } else {
            synchronized (cameraConfigWidget) {
                if (cameraConfigWidget.updated) {
                    cameraConfigRef.value.fov = cameraConfigWidget.fov;
                    cameraConfigRef.value.cameraPosition = cameraConfigWidget.cameraPosition;
                    cameraConfigRef.value.lookAtPosition = cameraConfigWidget.lookAtPosition;
                    cameraConfigRef.changed = true;

                    cameraConfigWidget.updated = false;
                }
            }
        }
    }

    private final Radioactive<CameraConfig> cameraConfigRef;
    private final Ref<Boolean> cameraConfigProgramUpdated;
    private final CameraConfigWidget cameraConfigWidget;
}
