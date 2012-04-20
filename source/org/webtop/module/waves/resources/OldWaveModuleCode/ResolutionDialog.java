package webtop.wave;

import java.awt.*;
import java.awt.event.*;

import sdl.gui.numberbox.*;

public class ResolutionDialog extends Frame implements ActionListener {
	private Panel panel;

	private Label			label;
	private IntBox		resolution;
	private Checkbox	gouraud;

	private Button ok;
	private Button cancel;

	private PoolController engine;

	public ResolutionDialog() {
		setLayout(new BorderLayout());
		setResizable(false);
		setBounds(200, 200, 200, 140);
		setTitle("Preferences");

		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {setVisible(false);}
			});

		panel = new Panel();
		panel.setBackground(Color.darkGray.darker());
		panel.setForeground(Color.white);
		panel.setLayout(null);
		add(panel, "Center");

		label = new Label("Resolution:", Label.RIGHT);
		label.setBounds(10, 10, 60, 20);
		panel.add(label);

		resolution = new IntBox(0,32767,0,4);
		resolution.setBounds(80, 10, 60, 20);
		panel.add(resolution);

		gouraud = new Checkbox("Gouraud Shading", true);
		gouraud.setBounds(10, 40, 120, 20);
		panel.add(gouraud);

		ok = new Button("    Ok    ");
		ok.setBounds(25, 70, 60, 20);
		panel.add(ok);
		ok.addActionListener(this);

		cancel = new Button("Cancel");
		cancel.setBounds(95, 70, 60, 20);
		panel.add(cancel);
		cancel.addActionListener(this);
	}

	public void setEngine(PoolController e) {
		engine = e;
	}

	public void setup(int res,boolean npv) {
		resolution.setValue(res);
		gouraud.setState(npv);
	}

	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		if(e.getSource()==ok)
			engine.setPoolOptions(resolution.getValue(), gouraud.getState());
	}
}
