package tech.icey.wgx.ui;

import java.awt.BorderLayout;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class SimpleEditor extends JFrame {
	public SimpleEditor() {
		super("文本编辑器");
		
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		JMenu fileMenu = new JMenu("文件");
		menuBar.add(fileMenu);
		fileMenu.add(new JMenuItem("新建"));
		fileMenu.add(new JMenuItem("打开"));
		fileMenu.add(new JMenuItem("保存"));
		fileMenu.add(new JMenuItem("另存为"));
		fileMenu.add(new JMenuItem("退出"));
		
		JMenu editMenu = new JMenu("编辑");
		menuBar.add(editMenu);
		JCheckBoxMenuItem lineWrap = new JCheckBoxMenuItem("自动换行");
		editMenu.add(lineWrap);
		
		this.setLayout(new BorderLayout());
		
		JTextArea textArea = new JTextArea();
		textArea.setFont(FontDatabase.defaultMonospaceFont);
		JScrollPane scrollPane = new JScrollPane(textArea);
		this.add(scrollPane);
		
		this.setSize(600, 600);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
	}
}
