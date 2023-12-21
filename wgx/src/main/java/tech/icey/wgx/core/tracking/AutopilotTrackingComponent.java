package tech.icey.wgx.core.tracking;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import tech.icey.util.Function0;
import tech.icey.util.Optional;
import tech.icey.util.Pair;
import tech.icey.wgx.babel.*;

import javax.swing.*;

final class TCKFileReader {
	public TCKFileReader(String filePath) throws IOException {
		this.file = new File(filePath);
		this.inputStream = new FileInputStream(file);
	}

	public synchronized Optional<String> readLine() {
		try {
			if (cachedJSONLines instanceof Optional.Some<List<String>> someCachedJSONLines) {
				if (cachedJSONLineIndex < someCachedJSONLines.value.size()) {
					return Optional.some(someCachedJSONLines.value.get(cachedJSONLineIndex++));
				}
			}

			ByteBuffer sectionSizeBuf = ByteBuffer.allocate(Integer.BYTES);
			sectionSizeBuf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
			int readSize = inputStream.read(sectionSizeBuf.array());
			if (readSize != Integer.BYTES) {
				return Optional.none();
			}

			int sectionSize = sectionSizeBuf.getInt();
			byte[] sectionBuf = new byte[sectionSize];
			readSize = inputStream.read(sectionBuf);
			if (readSize != sectionSize) {
				return Optional.none();
			}

			Inflater inflater = new Inflater();
			inflater.setInput(sectionBuf);

			ByteArrayOutputStream inflateOutput = new ByteArrayOutputStream();
			byte[] inflateBuf = new byte[1024];
			while (!inflater.finished()) {
				int inflateSize = inflater.inflate(inflateBuf);
				inflateOutput.write(inflateBuf, 0, inflateSize);
			}

			String inflatedString = inflateOutput.toString(StandardCharsets.UTF_8);
			cachedJSONLines = Optional.some(List.of(inflatedString.split("\n")));
			cachedJSONLineIndex = 0;

			return readLine();
		} catch (IOException | DataFormatException e) {
			return Optional.none();
		}
	}

	public synchronized void rewind() throws IOException {
		inputStream = new FileInputStream(file);
		cachedJSONLineIndex = 0;
		cachedJSONLines = Optional.none();
	}

	private final File file;
	private InputStream inputStream;
	private Optional<List<String>> cachedJSONLines;
	private int cachedJSONLineIndex;
}

final class AutopilotTrackingPanel extends JPanel implements Dockable {
	AutopilotTrackingPanel(
			Function<Boolean, Void> setEnabled,
			Function<TCKFileReader, Void> setCurrentFile,
			Function0<Void> rewindCurrentFile
	) {
		this.setEnabled = setEnabled;
		this.setCurrentFile = setCurrentFile;
		this.rewindCurrentFile = rewindCurrentFile;

		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);

		JPanel inputBoxPanel = new JPanel();
		BoxLayout inputBoxLayout = new BoxLayout(inputBoxPanel, BoxLayout.X_AXIS);
		inputBoxPanel.setLayout(inputBoxLayout);

		inputBoxPanel.add(new JLabel("TCK 文件: "));
		JTextField tckFilePathTextField = new JTextField();
		inputBoxPanel.add(tckFilePathTextField);
		JButton tckFileOpenButton = new JButton("打开");
		inputBoxPanel.add(tckFileOpenButton);

		this.add(inputBoxPanel);
	}

	@Override
	public void dock() {

	}

	@Override
	public void undock() {
		this.setEnabled.apply(false);
	}

	private final Function<Boolean, Void> setEnabled;
	private final Function<TCKFileReader, Void> setCurrentFile;
	private final Function0<Void> rewindCurrentFile;
}

public final class AutopilotTrackingComponent implements DataPublisher, UIProvider {
	@Override
	public List<Pair<String, UIComponent>> provide() {
		return List.of(
				new Pair<>(
						"AutopilotTracking",
						new UIComponent.SubElement(
								autopilotTrackingPanel,
								"AUTOPILOT",
								"TrackingControl",
								0
						)
				)
		);
	}

	@Override
	public int publisherInitPriority() {
		return 0;
	}

	@Override
	public void initialise(Masterpiece masterpiece) {
		this.masterpiece = masterpiece;
	}

	@Override
	public void publish() {
		if (!isEnabled) {
			return;
		}
	}

	private volatile boolean isEnabled;
	private volatile TCKFileReader tckFileReader;
	private Masterpiece masterpiece;
	private final AutopilotTrackingPanel autopilotTrackingPanel = new AutopilotTrackingPanel(
			isEnabled -> {
				this.isEnabled = isEnabled;
				return null;
			},
			f -> { return null; },
			() -> { return null; }
	);
}
