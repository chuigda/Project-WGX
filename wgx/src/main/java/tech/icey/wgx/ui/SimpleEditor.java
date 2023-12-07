package tech.icey.wgx.ui;

import tech.icey.util.Logger;
import tech.icey.util.Ref;
import tech.icey.util.Optional;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.*;

public final class SimpleEditor extends JFrame {
	public SimpleEditor() {
		super("文本编辑器");
		
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		JMenu fileMenu = new JMenu("文件");
		menuBar.add(fileMenu);
		JMenuItem createMenuItem = new JMenuItem("新建");
		JMenuItem openMenuItem = new JMenuItem("打开");
		JMenuItem saveMenuItem = new JMenu("保存");
		JMenuItem saveAsMenuItem = new JMenuItem("另存为");
		JMenuItem exitMenuItem = new JMenuItem("退出");

		fileMenu.add(createMenuItem);
		fileMenu.add(openMenuItem);
		fileMenu.add(saveMenuItem);
		fileMenu.add(saveAsMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		
		JMenu editMenu = new JMenu("编辑");
		menuBar.add(editMenu);
		JCheckBoxMenuItem lineWrap = new JCheckBoxMenuItem("自动换行");
		editMenu.add(lineWrap);
		
		this.setLayout(new BorderLayout());
		
		JTextArea textArea = new JTextArea();
		textArea.setFont(FontDatabase.defaultMonospaceFont);
		MenuFactory.createTextAreaMenu(textArea);
		JScrollPane scrollPane = new JScrollPane(textArea);
		this.add(scrollPane);

		SimpleEditor self = this;
		Ref<Optional<String>> currentFile = new Ref<>(Optional.none());
		createMenuItem.addActionListener(e -> {
			if (currentFile.value instanceof Optional.Some<String> some) {
				int input = JOptionPane.showConfirmDialog(self, String.format("保存正在编辑的文件 %s？", some.value));
				if (input == JOptionPane.YES_OPTION) {
					try {
						Files.writeString(Paths.get(some.value), textArea.getText());
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(self, String.format("保存文件时发生了错误: %s", ex.getMessage()));
						logger.log(Logger.Level.ERROR, "保存文件时发生了错误: %s", ex.getMessage());
					}
				}
			}

			if (textArea.getText().isEmpty()) {
				return;
			}

			int input = JOptionPane.showConfirmDialog(self, "这会清除正在编辑的内容，你确定吗?");
			if (input == JOptionPane.YES_OPTION) {
				textArea.setText("");
			}
		});

		this.setSize(600, 600);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
	}

	private static final Logger logger = new Logger(SimpleEditor.class.getName());
}
