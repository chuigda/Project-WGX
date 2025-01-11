package chr.wgx.widget;

import org.joml.Vector3f;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.functional.Action1;

import javax.swing.*;

public final class XYZEditor extends JPanel {
    public XYZEditor(Vector3f minValue, Vector3f maxValue) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        SpinnerModel xModel = new SpinnerNumberModel(0.0f, minValue.x, maxValue.x, 0.1f);
        SpinnerModel yModel = new SpinnerNumberModel(0.0f, minValue.y, maxValue.y, 0.1f);
        SpinnerModel zModel = new SpinnerNumberModel(0.0f, minValue.z, maxValue.z, 0.1f);

        JLabel xLabel = new JLabel("X: ");
        JLabel yLabel = new JLabel("Y: ");
        JLabel zLabel = new JLabel("Z: ");
        xField = new JSpinner(xModel);
        yField = new JSpinner(yModel);
        zField = new JSpinner(zModel);

        this.add(xLabel);
        this.add(xField);
        this.add(yLabel);
        this.add(yField);
        this.add(zLabel);
        this.add(zField);

        xField.addChangeListener(_ -> onFieldUpdated());
        yField.addChangeListener(_ -> onFieldUpdated());
        zField.addChangeListener(_ -> onFieldUpdated());
    }

    public void setValue(Vector3f value) {
        SwingUtilities.invokeLater(() -> {
            xField.setValue(value.x);
            yField.setValue(value.y);
            zField.setValue(value.z);
        });
    }

    public synchronized void onValueChanged(Action1<Vector3f> action) {
        onValueChangedAction = Option.some(action);
    }

    private synchronized void onFieldUpdated() {
        float x = getFloatOrDoubleValue(xField);
        float y = getFloatOrDoubleValue(yField);
        float z = getFloatOrDoubleValue(zField);

        if (onValueChangedAction instanceof Option.Some<Action1<Vector3f>> some) {
            some.value.apply(new Vector3f(x, y, z));
        }
    }

    private final JSpinner xField;
    private final JSpinner yField;
    private final JSpinner zField;

    private Option<Action1<Vector3f>> onValueChangedAction = Option.none();

    private static float getFloatOrDoubleValue(JSpinner spinner) {
        Object value = spinner.getValue();
        if (value instanceof Float) {
            return (float) value;
        } else {
            return (float) (double) value;
        }
    }
}
