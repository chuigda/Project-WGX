package tech.icey.wgx.core.tracking;

import tech.icey.util.Pair;
import tech.icey.wgx.babel.*;

import javax.swing.*;
import java.util.List;

public final class VTSTrackingComponent implements DataPublisher, UIProvider {
	@Override
	public List<Pair<String, UIComponent>> provide() {
		return List.of(
			new Pair<>(
				"VTSTracking",
				new UIComponent.SubElement(vtsTrackingPanel, "VTS", "TrackingControl", 0)
			)
		);
	}

	@Override
	public int publisherInitPriority() {
		return 0;
	}

	@Override
	public void initialise(Masterpiece masterpiece) {}

	@Override
	public void publish() {}

	private final VTSTrackingPanel vtsTrackingPanel = new VTSTrackingPanel();
}

final class VTSTrackingPanel extends JPanel implements Dockable {

	public VTSTrackingPanel() {
		JLabel l = new JLabel("hi");
		this.add(l);
	}

	@Override
	public void dock() {}

	@Override
	public void undock() {}
}
