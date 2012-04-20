/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.threemedia;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;
import org.webtop.module.threemedia.PoolController;
//import org.webtop.module.twomedia.TwoMedia;
import org.webtop.util.*;
import org.webtop.util.script.*;


import org.sdl.gui.numberbox.*;

public class ResolutionDialog extends JFrame implements ActionListener{
	public static final long serialVersionUID = 0;//to shut eclipse up
	
	private JPanel panel;
	private JLabel label;
	private IntBox		resolution;
	private JCheckBox gouraud;

	private JButton ok;
	private JButton cancel;
	
	private ButtonScripter okScripter, cancelScripter;
	
	private PoolController engine;
	
	public ResolutionDialog(ThreeMedia wave) {
		setLayout(new BorderLayout());
		setResizable(false);
		setBounds(200, 200, 200, 140);
		setTitle("Preferences");

		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {setVisible(false);}
			});

		panel = new JPanel();
		panel.setBackground(Color.darkGray.darker());
		panel.setForeground(Color.white);
		add(panel);

		label = new JLabel("Resolution:", Label.RIGHT);
		label.setBounds(10, 10, 60, 20);
		panel.add(label);

		resolution = new IntBox(0,32767,0,4);
		panel.add(resolution);

		gouraud = new JCheckBox("Gouraud Shading", true);
		gouraud.setBounds(10, 40, 120, 20);
		panel.add(gouraud);

		ok = new JButton("    Ok    ");
		panel.add(ok);
		ok.addActionListener(this);

		cancel = new JButton("Cancel");
		panel.add(cancel);
		cancel.addActionListener(this);
		
		okScripter = new ButtonScripter(ok, wave.getWSLPlayer(), null, "ok");
		cancelScripter = new ButtonScripter(cancel, wave.getWSLPlayer(), null, "cancel");
		
	}
	
	public void setEngine(PoolController e) {
		engine = e;
	}
	
	public void setup(int res,boolean npv) {
		resolution.setValue(res);
		gouraud.setEnabled(npv);
	}

	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		if(e.getSource()==ok)
			engine.setPoolOptions(resolution.getValue(),gouraud.isEnabled());
	}
	
	protected void toWSLNode(WSLNode node){
		
	}

}
