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
//Automates scripting with JList assuming a DefaultListModel.
//Brian  Thomas
//Created July 1 2008
//Version 0.1

package org.webtop.util.script;

import javax.swing.*;

import org.webtop.wsl.client.WSLPlayer;

public class ListScripter extends Scripter {

	//JButton b,org.webtop.wsl.client.WSLPlayer player,String id,String param
	public ListScripter(JList list, DefaultListModel listModel, WSLPlayer player, String id, String param) {
		super(player,id,param);
		if(list==null) {
			super.destroy();	//broken object should be discarded
			throw new NullPointerException("List cannot be null.");
		}
		if(listModel==null) {
			super.destroy();	//broken object should be discarded
			throw new NullPointerException("List Model cannot be null.");
		}
		//button=b;
		//button.addActionListener(this);
	}

	@Override
	protected String getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setEnabled(boolean on) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setValue(String value) {
		// TODO Auto-generated method stub

	}
	
	private String getCommand(String command) {
		
		return command;
	}

}
