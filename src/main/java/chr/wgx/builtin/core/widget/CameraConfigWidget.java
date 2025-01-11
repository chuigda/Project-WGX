package chr.wgx.builtin.core.widget;

import chr.wgx.reactor.IWidget;
import chr.wgx.widget.XYZEditor;
import org.joml.Vector3f;

import javax.swing.*;
import java.awt.*;

public final class CameraConfigWidget extends JPanel implements IWidget {
    public CameraConfigWidget() {
        this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        this.setLayout(new GridBagLayout());

        GridBagConstraints cLeft = new GridBagConstraints();
        cLeft.fill = GridBagConstraints.HORIZONTAL;
        cLeft.weightx = 0.5;
        cLeft.gridx = 0;
        cLeft.gridy = 0;
        cLeft.anchor = GridBagConstraints.WEST;

        GridBagConstraints cRight = new GridBagConstraints();
        cRight.fill = GridBagConstraints.HORIZONTAL;
        cRight.weightx = 0.5;
        cRight.gridx = 1;
        cRight.gridy = 0;
        cRight.anchor = GridBagConstraints.EAST;

        isProgramUpdated = new JCheckBox("程序更新");
        isProgramUpdated.setToolTipText("指示相机设置目前是否由程序控制");
        isProgramUpdated.setSelected(false);
        isProgramUpdated.setEnabled(false);
        this.add(isProgramUpdated, cLeft);

        JLabel fovLabel = new JLabel("视场角: ");
        fovField = new JSpinner(new SpinnerNumberModel(45.0f, 0.0f, 180.0f, 0.1f));
        cLeft.gridy += 1;
        cRight.gridy += 1;
        this.add(fovLabel, cLeft);
        this.add(fovField, cRight);

        JLabel cameraPosLabel = new JLabel("相机位置: ");
        cameraPosEditor = new XYZEditor(new Vector3f(-10.0f, -10.0f, -10.0f), new Vector3f(10.0f, 10.0f, 10.0f));
        cameraPosEditor.setValue(new Vector3f(0.0f, 1.0f, 1.0f));
        cLeft.gridy += 1;
        cRight.gridy += 1;
        this.add(cameraPosLabel, cLeft);
        this.add(cameraPosEditor, cRight);

        JLabel lookAtPosLabel = new JLabel("观察位置: ");
        lookAtPosEditor = new XYZEditor(new Vector3f(-10.0f, -10.0f, -10.0f), new Vector3f(10.0f, 10.0f, 10.0f));
        lookAtPosEditor.setValue(new Vector3f(0.0f, 0.0f, 0.0f));
        cLeft.gridy += 1;
        cRight.gridy += 1;
        this.add(lookAtPosLabel, cLeft);
        this.add(lookAtPosEditor, cRight);

        fovField.addChangeListener(_ -> onFOVUpdated());
        cameraPosEditor.onValueChanged(this::onCameraPosUpdated);
        lookAtPosEditor.onValueChanged(this::onLookAtPosUpdated);
    }

    @Override
    public String displayName() {
        return "相机设置";
    }

    @Override
    public JPanel getContentPanel() {
        return this;
    }

    public synchronized void onFOVUpdated() {
        fov = (float) (double) fovField.getValue();
        updated = true;
    }

    public synchronized void onCameraPosUpdated(Vector3f value) {
        cameraPos.set(value);
        updated = true;
    }

    public synchronized void onLookAtPosUpdated(Vector3f value) {
        lookAtPos.set(value);
        updated = true;
    }

    @Override
    public void onDock() {}

    @Override
    public void onUndock() {}

    public final JCheckBox isProgramUpdated;
    public final JSpinner fovField;
    public final XYZEditor cameraPosEditor;
    public final XYZEditor lookAtPosEditor;

    public float fov = 45.0f;
    public Vector3f cameraPos = new Vector3f();
    public Vector3f lookAtPos = new Vector3f();
    public boolean updated = false;
}
