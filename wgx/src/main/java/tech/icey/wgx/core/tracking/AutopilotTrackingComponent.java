package tech.icey.wgx.core.tracking;

import java.util.List;

import tech.icey.util.Pair;
import tech.icey.wgx.babel.DataPublisher;
import tech.icey.wgx.babel.Masterpiece;
import tech.icey.wgx.babel.UIComponent;
import tech.icey.wgx.babel.UIProvider;

public final class AutopilotTrackingComponent implements DataPublisher, UIProvider {
	@Override
	public List<Pair<String, UIComponent>> provide() {
		return List.of();
	}

	@Override
	public int publisherInitPriority() {
		return 0;
	}

	@Override
	public void initialise(Masterpiece masterpiece) {}

	@Override
	public void publish() {}
}
