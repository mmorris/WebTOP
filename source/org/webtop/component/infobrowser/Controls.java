/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.component.infobrowser;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.ActionListener;

public class Controls extends JPanel {
	//JButton backButton = new JButton("Back");
	//JButton forwardButton = new JButton("Forward");
	//JButton homeButton = new JButton("Home");
	JButton printButton = new JButton("Print");
	
	public Controls() {
		/*this.setLayout(new GridBagLayout());
		this.add(backButton, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
	               , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0,0));
		this.add(forwardButton, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
	               , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0,0));
		this.add(homeButton, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
	               , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0,0));
				   */
		this.add(printButton, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0
	               , GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0,0));
	}
	
	public void addActionListener(ActionListener listener) {
		printButton.addActionListener(listener);
	}
	
}
