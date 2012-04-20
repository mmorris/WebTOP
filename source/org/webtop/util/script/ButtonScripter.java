/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//ButtonScripter.java
//Automates scripting with Buttons.
//Davis Herring
//Created August 27 2003
//Updated March 31 2004
//Version 0.11

package org.webtop.util.script;

import javax.swing.JButton;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Event.*;

public class ButtonScripter extends Scripter implements ActionListener
{
	private final JButton button;

	public ButtonScripter(JButton b,org.webtop.wsl.client.WSLPlayer player,String id,String param) {
		super(player,id,param);
		if(b==null) {
			super.destroy();	//broken object should be discarded
			throw new NullPointerException("Button cannot be null.");
		}
		button=b;
		button.addActionListener(this);
	}

	//Buttons don't have values; eventually Scripter will distinguish between
	//actionPerformed and parameterSet events (once WSL does) and this will make
	//more sense.
	protected void setValue(String value) {
		//if(listener!=null) org.webtop.util.script.Event.clickButton(button,listener);
		//if(listener!=null) listener.actionPerformed(new ActionEvent(button,ActionEvent.ACTION_PERFORMED,
		//		 button.getActionCommand()));
		ActionListener[] listeners = button.getActionListeners();
		for(ActionListener listener : listeners) {
			listener.actionPerformed(new ActionEvent(button,ActionEvent.ACTION_PERFORMED,
					null));
		}
		
	}

	protected String getValue() {throw new RuntimeException("Buttons cannot be queried.");}

	protected void setEnabled(boolean on) {button.setEnabled(on);}

	//This is a bit overkill, but to be complete:
	protected void destroy() {
		button.removeActionListener(this);
		super.destroy();
	}

	//Buttons cannot be initialized
	public void initialize(org.webtop.wsl.event.WSLScriptEvent event) {}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==button) recordActionPerformed(null);
		else System.err.println("ButtonScripter: unexpected actionPerformed from "+e.getSource());
	}
}
