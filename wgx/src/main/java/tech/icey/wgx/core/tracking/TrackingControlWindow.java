package tech.icey.wgx.core.tracking;

import tech.icey.r77.math.Vector3;
import tech.icey.util.Logger;
import tech.icey.wgx.babel.Dockable;
import tech.icey.wgx.babel.DockingPort;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class TripleEdit extends JPanel {
	public TripleEdit(Vector3 initState, Function<Vector3, Void> updateState) {
		BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
		this.setLayout(layout);

		float initX = initState.x();
        float initY = initState.y();
        float initZ = initState.z();

        JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(initX, -Float.MAX_VALUE, Float.MAX_VALUE, 0.1f));
        JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(initY, -Float.MAX_VALUE, Float.MAX_VALUE, 0.1f));
        JSpinner zSpinner = new JSpinner(new SpinnerNumberModel(initZ, -Float.MAX_VALUE, Float.MAX_VALUE, 0.1f));

        var spinnerEventListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                float x = (float)(double) xSpinner.getValue();
                float y = (float)(double) ySpinner.getValue();
                float z = (float)(double) zSpinner.getValue();
                updateState.apply(new Vector3(x, y, z));
            }
        };

        xSpinner.addChangeListener(spinnerEventListener);
        ySpinner.addChangeListener(spinnerEventListener);
        zSpinner.addChangeListener(spinnerEventListener);

        var preferredSize = new Dimension(32, xSpinner.getEditor().getPreferredSize().height);
        xSpinner.getEditor().setPreferredSize(preferredSize);
        ySpinner.getEditor().setPreferredSize(preferredSize);
        zSpinner.getEditor().setPreferredSize(preferredSize);

        var gapSize = new Dimension(4, 0);

        this.add(new JPanel());
        this.add(new JLabel("X"));
        this.add(Box.createRigidArea(gapSize));
        this.add(xSpinner);
        this.add(Box.createRigidArea(gapSize));
        this.add(new JLabel("Y"));
        this.add(Box.createRigidArea(gapSize));
        this.add(ySpinner);
        this.add(Box.createRigidArea(gapSize));
        this.add(new JLabel("Z"));
        this.add(Box.createRigidArea(gapSize));
        this.add(zSpinner);
	}
}

public final class TrackingControlWindow extends JFrame implements DockingPort {
    public TrackingControlWindow(
            Function<Vector3, Void> setTranslationAdjust,
            Function<Vector3, Void> setFaceAngleAdjust,
            Function<Vector3, Void> setTranslationLimit,
            Function<Vector3, Void> setFaceAngleLimit
    ) {
        super("姿态控制");

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        BoxLayout mainLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(mainLayout);
        this.setContentPane(mainPanel);

        JPanel trackingModePanel = new JPanel();
        trackingModePanel.setBorder(BorderFactory.createTitledBorder("控制模式"));
        BoxLayout trackingModePanelLayout = new BoxLayout(trackingModePanel, BoxLayout.Y_AXIS);
        trackingModePanel.setLayout(trackingModePanelLayout);

        JLabel trackingModelContentDefault = new JLabel("选择一个控制模式，相应的组件会在这里显示");

        trackingModeComboBox.addItem("关闭");
        trackingModeComboBox.addActionListener(e -> {
            int selectedIndex = trackingModeComboBox.getSelectedIndex();
            if (trackingModelContentPanel.getComponent(0) instanceof Dockable d) {
                d.undock();
            }
            trackingModelContentPanel.removeAll();
            if (selectedIndex == 0) {
                trackingModelContentPanel.add(trackingModelContentDefault);
            } else {
                JPanel p = trackingModePanels.get(selectedIndex - 1);
                if (p instanceof Dockable d) {
                    d.dock();
                }
                trackingModelContentPanel.add(p);
            }
            this.pack();
            this.revalidate();
        });
        trackingModelContentPanel.add(trackingModelContentDefault);

        trackingModePanel.add(trackingModeComboBox);
        trackingModePanel.add(trackingModelContentPanel);
        mainPanel.add(trackingModePanel);

        JPanel postProcessPanel = new JPanel();
        postProcessPanel.setBorder(BorderFactory.createTitledBorder("后处理选项"));
        postProcessPanel.setLayout(new GridBagLayout());
        mainPanel.add(postProcessPanel);

        JLabel translationAdjustLabel = new JLabel("位移修正");
        JLabel faceAngleAdjustLabel = new JLabel("旋转修正");
        JLabel translationLimitLabel = new JLabel("位移限制");
        JLabel faceAngleLimitLabel = new JLabel("旋转限制");
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 4);
        c.gridx = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        {
        	c.gridy = 0;
        	postProcessPanel.add(translationAdjustLabel, c);
        	c.gridy = 1;
        	postProcessPanel.add(faceAngleAdjustLabel, c);
        	c.gridy = 2;
        	postProcessPanel.add(translationLimitLabel, c);
        	c.gridy = 3;
        	postProcessPanel.add(faceAngleLimitLabel, c);
        }
        
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        {
        	c.gridy = 0;
        	postProcessPanel.add(new TripleEdit(Vector3.ZERO, setTranslationAdjust), c);
        	c.gridy = 1;
        	postProcessPanel.add(new TripleEdit(Vector3.ZERO, setFaceAngleAdjust), c);
        	c.gridy = 2;
        	postProcessPanel.add(new TripleEdit(Vector3.mul(120.0f, Vector3.UNIT), setTranslationLimit), c);
        	c.gridy = 3;
        	postProcessPanel.add(new TripleEdit(Vector3.mul(90.0f, Vector3.UNIT), setFaceAngleLimit), c);
        }
        
        this.setMinimumSize(new Dimension(480, 0));
        this.pack();
        this.setResizable(false);
    }

    @Override
    public void addElement(String name, long location, JPanel panel) {
        SwingUtilities.invokeLater(() -> {
            trackingModes.add(name);
            trackingModeComboBox.addItem(name);
            trackingModePanels.add(panel);
        });
    }
    
    private final JComboBox<String> trackingModeComboBox = new JComboBox<>();
    private final JPanel trackingModelContentPanel = new JPanel();
    private final List<String> trackingModes = new ArrayList<>();
    private final List<JPanel> trackingModePanels = new ArrayList<>();

    private static final Logger logger = new Logger(TrackingControlWindow.class.getName());
}
