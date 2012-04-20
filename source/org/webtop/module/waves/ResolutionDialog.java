package org.webtop.module.waves;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import org.sdl.gui.numberbox.*;

public class ResolutionDialog extends JFrame implements ActionListener {
	
	private JPanel panel;
	private JLabel label;
	private IntBox resolution; 
	private JCheckBox gouraud;
	private JButton ok; 
	private JButton cancel; 
	
	private PoolController engine; 
	
	public ResolutionDialog(){
		setLayout(new BorderLayout());
		setResizable(false);
		setBounds(200,200,200,140);
		setTitle("Preferences");
		
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){ setVisible(false);}
		});
		
		panel = new JPanel();
		panel.setLayout(null);
		add(panel, "Center");
		
		label = new JLabel("Resolution: ", JLabel.RIGHT);
		label.setBounds(10,10,60,20);
		panel.add(label);
		
		resolution = new IntBox(0,32767,0,4);
		resolution.setBounds(80,10,60,20);
		panel.add(resolution);
		
		gouraud = new JCheckBox("Gouraud Shading", true);
		gouraud.setBounds(10,40,120,20);
		panel.add(gouraud);
		
		ok = new JButton("   Ok   ");
		ok.setBounds(25, 70, 60, 20);
		panel.add(ok);
		ok.addActionListener(this);
		
		cancel = new JButton("Cancel");
		cancel.setBounds(95, 70, 60, 20);
		panel.add(cancel);
		cancel.addActionListener(this);
	}
	
	public void setEngine(PoolController e){
		engine = e;
	}
	
	public void setup(int res, boolean npv){
		resolution.setValue(res); 
		gouraud.setEnabled(npv);
		
	}
	
	//Event handling 
	public void actionPerformed(ActionEvent e){
		setVisible(false);
		if(e.getSource() == ok){
			engine.setPoolOptions(resolution.getValue(), gouraud.isEnabled());
		}
	}
}
