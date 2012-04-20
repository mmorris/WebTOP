/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LanguageDialogue extends JFrame implements ActionListener {

	private JPanel panel;
	private JLabel			label;
	private JButton ok;
	private JButton cancel;

	//private PoolController engine;

	public LanguageDialogue() {
		setLayout(new BorderLayout());
		setResizable(false);
		setBounds(200, 200, 200, 140);
		setTitle("Choose Language");

		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {setVisible(false);}
			});

		panel = new JPanel();
		panel.setBackground(Color.darkGray.darker());
		panel.setForeground(Color.white);
		panel.setLayout(null);
		add(panel, "Center");

		label = new JLabel("Language:", Label.RIGHT);
		label.setBounds(10, 10, 60, 20);
		panel.add(label);

		//resolution = new IntBox(0,32767,0,4);
		//resolution.setBounds(80, 10, 60, 20);
		//panel.add(resolution);

		ok = new JButton("    Ok    ");
		ok.setBounds(25, 70, 60, 20);
		panel.add(ok);
		ok.addActionListener(this);

		cancel = new JButton("Cancel");
		cancel.setBounds(95, 70, 60, 20);
		panel.add(cancel);
		cancel.addActionListener(this);
	}

	/*
	public void setEngine(PoolController e) {
		engine = e;
	}
	*/
	public void setup(int res,boolean npv) {


	}

	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		if(e.getSource()==ok)
			System.out.println("cheeze");
	}
}
