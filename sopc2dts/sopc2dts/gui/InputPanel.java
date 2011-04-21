package sopc2dts.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import sopc2dts.Logger;
import sopc2dts.Logger.LogLevel;
import sopc2dts.lib.AvalonSystem;
import sopc2dts.parsers.BasicSystemLoader;

public class InputPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -586032545427893647L;
	JTextArea txtSource = new JTextArea();
	JList componentList = new JList();
	Sopc2DTSGui mainGui;
	JButton btnChoose = new JButton("Choose file");
	JTextField txtFileName = new JTextField();
	JButton btnLoad = new JButton("Load file");
	public InputPanel(String fName, Sopc2DTSGui parent)
	{
		super();
		mainGui = parent;
		txtFileName.setText(fName);
		btnChoose.addActionListener(this);
		btnLoad.addActionListener(this);
		JPanel pnlTop = new JPanel(new BorderLayout());
		pnlTop.add(btnChoose, BorderLayout.WEST);
		pnlTop.add(btnLoad, BorderLayout.EAST);
		pnlTop.add(txtFileName, BorderLayout.CENTER);
		this.setLayout(new BorderLayout());
		componentList.setCellRenderer(new BasicComponentRenderer());
		JScrollPane jsp = new JScrollPane(componentList);
		jsp.setPreferredSize(new Dimension(300, 400));
		jsp.setBorder(BorderFactory.createTitledBorder("Component list"));
		this.add(jsp, BorderLayout.EAST);
		jsp = new JScrollPane(txtSource);
		jsp.setBorder(BorderFactory.createTitledBorder("Input source"));
		this.add(jsp, BorderLayout.CENTER);
		this.add(pnlTop, BorderLayout.NORTH);
		if(fName!=null)
		{
			loadFile(fName,500);
		}
	}	
	void loadFile(String fName, int delay)
	{
		File f = new File(fName);
		if(f.exists())
		{
			Thread t = new Thread(new ThreadedLoader(f,delay));
			t.start();
		} else {
			Logger.logln("Refusing to load " + fName + ". It does not exist", 
					LogLevel.WARNING);
		}
	}
	public void setSys(AvalonSystem sys) {
		mainGui.setSys(sys);
		if(sys!=null)
		{
			componentList.setListData(sys.getSystemComponents());
		}
	}
	protected class ThreadedLoader implements Runnable {
		File f;
		int delayMillis;
		protected ThreadedLoader(File inp)
		{
			this(inp, 0);
		}
		protected ThreadedLoader(File inp, int delay)
		{
			f = inp;
			delayMillis = delay;
		}
		public void run() {
			if(delayMillis>0)
			{
				try {
					Thread.sleep(delayMillis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			componentList.setListData(new Object[]{});
			mainGui.clearLog();
		    StringBuffer contents = new StringBuffer();
		    try {
		      BufferedReader input =  new BufferedReader(new FileReader(f));
		      try {
		        String line = null;
		        while (( line = input.readLine()) != null){
		          contents.append(line).append(System.getProperty("line.separator"));
		        }
		      }
		      finally {
		        input.close();
		      }
		    }
		    catch (IOException ex){
		      ex.printStackTrace();
		    }
		    txtSource.setText(contents.toString());
			setSys(BasicSystemLoader.loadSystem(f));
		}
		
	}
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(btnLoad))
		{
			mainGui.clearLog();
			loadFile(txtFileName.getText(), 0);
		} else if(e.getSource().equals(btnChoose))
		{
			JFileChooser jfc = new JFileChooser();
			if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				txtFileName.setText(jfc.getSelectedFile().getAbsolutePath());
				loadFile(jfc.getSelectedFile().getAbsolutePath(), 0);
			}
		}
	}
}
