package sopc2dts.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import sopc2dts.LogListener;
import sopc2dts.Logger;
import sopc2dts.lib.AvalonSystem;

public class Sopc2DTSGui extends JFrame implements LogListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1060629328921686729L;
	JTabbedPane jtp = new JTabbedPane();
	JTextArea txtLog = new JTextArea();
	InputPanel pnlInput;
	OutputPanel pnlOutput = new OutputPanel();
	private AvalonSystem sys;

	public Sopc2DTSGui(String inpFile) {
		super("Sopc2DTS");
		Logger.addLogListener(this);
		Logger.setUseStdOutErr(false);
		pnlInput = new InputPanel(inpFile, this);
		jtp.addTab("Input", pnlInput);
//		jtp.addTab("Settings", new JPanel());
//		jtp.addTab("Boardinfo", new JPanel());
		jtp.addTab("Output", pnlOutput);
		txtLog.setEditable(false);
		JScrollPane jsp = new JScrollPane(txtLog);
		jsp.setPreferredSize(new Dimension(800,200));
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(jsp, BorderLayout.SOUTH);
		this.getContentPane().add(jtp, BorderLayout.CENTER);
		this.pack();
	}
	public void clearLog()
	{
		txtLog.setText("");
	}
	public void messageLogged(String log) {
		txtLog.setText(txtLog.getText() + log);
	}
	public void setSys(AvalonSystem sys) {
		this.sys = sys;
		pnlOutput.setSys(sys);
	}
	public AvalonSystem getSys() {
		return sys;
	}
	
}
