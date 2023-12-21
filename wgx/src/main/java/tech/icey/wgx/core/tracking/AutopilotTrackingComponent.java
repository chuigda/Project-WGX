package tech.icey.wgx.core.tracking;

import java.awt.*;
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

	public Optional<String> readLine() {
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

	private final File file;
	private InputStream inputStream;
	private Optional<List<String>> cachedJSONLines;
	private int cachedJSONLineIndex;
}

final class AutopilotTrackingPanel extends JPanel implements Dockable {
	AutopilotTrackingPanel(
			Function<Boolean, Void> setEnabled,
			Function<TCKFileReader, Void> setCurrentFile,
			Function0<Void> stopAutopilot
	) {
		this.setEnabled = setEnabled;
		this.stopAutopilot = stopAutopilot;

		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);

		JPanel inputBoxPanel = new JPanel();
		BoxLayout inputBoxLayout = new BoxLayout(inputBoxPanel, BoxLayout.X_AXIS);
		inputBoxPanel.setLayout(inputBoxLayout);

		inputBoxPanel.add(new JLabel("TCK 文件: "));
		JTextField tckFilePathTextField = new JTextField();
		inputBoxPanel.add(tckFilePathTextField);
		inputBoxPanel.add(Box.createRigidArea(new Dimension(2, 0)));
		JButton tckFileOpenButton = new JButton("打开");
		inputBoxPanel.add(tckFileOpenButton);

		tckFileOpenButton.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().endsWith(".tck");
				}

				@Override
				public String getDescription() {
					return "DSYS 数据记录文件 (*.tck)";
				}
			});
			int result = fileChooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				tckFilePathTextField.setText(selectedFile.getAbsolutePath());
			}
		});

		this.add(inputBoxPanel);
		this.add(Box.createRigidArea(new Dimension(0, 2)));

		startButton = new JButton("开始");
		pauseResumeButton = new JButton("暂停");
		stopButton = new JButton("停止");

		startButton.addActionListener(e -> {
			String tckFilePath = tckFilePathTextField.getText();
			if (tckFilePath.isEmpty()) {
				JOptionPane.showMessageDialog(
						this,
						"TCK 文件路径不能为空",
						"错误",
						JOptionPane.ERROR_MESSAGE
				);
				return;
			}

			try {
				var tckFileReader = new TCKFileReader(tckFilePath);

				setCurrentFile.apply(tckFileReader);
				this.startButton.setEnabled(false);
				this.pauseResumeButton.setEnabled(true);
				this.stopButton.setEnabled(true);
				this.isEnabled = true;
			} catch (IOException ioException) {
				JOptionPane.showMessageDialog(
						this,
						"无法打开 TCK 文件",
						"错误",
						JOptionPane.ERROR_MESSAGE
				);
			}
		});

		pauseResumeButton.addActionListener(e -> {
			if (this.isEnabled) {
				this.pauseResumeButton.setText("继续");
				this.isEnabled = false;
			} else {
				this.pauseResumeButton.setText("暂停");
				this.isEnabled = true;
			}

			this.setEnabled.apply(this.isEnabled);
		});

		stopButton.addActionListener(e -> {
			this.isEnabled = false;
			this.setEnabled.apply(false);
			stopAutopilot.apply();
			this.stopButton.setEnabled(false);
			this.pauseResumeButton.setEnabled(false);
			this.pauseResumeButton.setText("暂停");
			this.startButton.setEnabled(true);
		});

		pauseResumeButton.setEnabled(false);
		stopButton.setEnabled(false);

		JPanel buttonPanel = new JPanel();
		BoxLayout buttonBoxLayout = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
		buttonPanel.setLayout(buttonBoxLayout);
		buttonPanel.add(startButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(2, 0)));
		buttonPanel.add(pauseResumeButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(2, 0)));
		buttonPanel.add(stopButton);

		this.add(buttonPanel);
	}

	public void onAutopilotStopped() {
		SwingUtilities.invokeLater(() -> {
			this.startButton.setEnabled(true);
			this.pauseResumeButton.setEnabled(false);
			this.stopButton.setEnabled(false);
		});
	}

	@Override
	public void dock() {
	}

	@Override
	public void undock() {
		this.setEnabled.apply(false);
		this.stopAutopilot.apply();
		this.startButton.setEnabled(true);
		this.pauseResumeButton.setEnabled(false);
		this.stopButton.setEnabled(false);
		this.pauseResumeButton.setText("暂停");
	}

	private final JButton startButton;
	private final JButton pauseResumeButton;
	private final JButton stopButton;
	private final Function<Boolean, Void> setEnabled;
	private final Function0<Void> stopAutopilot;
	private volatile boolean isEnabled = false;
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

		if (tckFileReader instanceof Optional.Some<TCKFileReader> someTCKFileReader) {
			Optional<String> line = someTCKFileReader.value.readLine();
			if (line instanceof Optional.Some<String> someLine) {
				System.out.println(someLine);
			} else {
				this.isEnabled = false;
				this.tckFileReader = Optional.none();
				autopilotTrackingPanel.onAutopilotStopped();

				JOptionPane.showMessageDialog(
						autopilotTrackingPanel,
						"自动驾驶已停止运行",
						"提示",
						JOptionPane.INFORMATION_MESSAGE
				);
			}
		}
	}

	private volatile boolean isEnabled;
	private volatile Optional<TCKFileReader> tckFileReader;
	private Masterpiece masterpiece;
	private final AutopilotTrackingPanel autopilotTrackingPanel = new AutopilotTrackingPanel(
			isEnabled -> {
				this.isEnabled = isEnabled;
				return null;
			},
			f -> {
				this.tckFileReader = Optional.some(f);
				return null;
			},
			() -> {
				this.isEnabled = false;
				this.tckFileReader = Optional.none();
				return null;
			}
	);
}
