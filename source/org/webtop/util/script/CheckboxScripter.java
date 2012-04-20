/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//CheckboxScripter.java
//Automates scripting with Checkboxes.
//Davis Herring
//Created August 27 2003
//Updated March 31 2004
//Version 0.11

package org.webtop.util.script;

import javax.swing.JCheckBox;
import java.awt.event.*;

public class CheckboxScripter extends Scripter implements ItemListener
{
	private final JCheckBox checkbox;
	private final ItemListener listener;
	private final boolean defaultValue;

	public CheckboxScripter(JCheckBox c,org.webtop.wsl.client.WSLPlayer player,String id,String param,boolean defVal,ItemListener il) {
		super(player,id,param);
		if(c==null) {
			super.destroy();	//broken object should be discarded
			throw new NullPointerException("Checkbox cannot be null.");
		}
		checkbox=c;
		checkbox.addItemListener(this);
		listener=il;
		defaultValue=defVal;
	}

	protected void setValue(String value) {
		boolean newVal = org.webtop.util.WTString.toBoolean(value,defaultValue);
		checkbox.setSelected(newVal);
		if(listener!=null) {
			listener.itemStateChanged(new ItemEvent(checkbox,ItemEvent.ITEM_STATE_CHANGED,
					null,newVal?1:0));
		}
			
	}

	protected String getValue() {return String.valueOf(checkbox.isSelected());}

	protected void setEnabled(boolean on) {checkbox.setEnabled(on);}

	//This is a bit overkill, but to be complete:
	protected void destroy() {
		checkbox.removeItemListener(this);
		super.destroy();
	}

	public void itemStateChanged(ItemEvent e) {
		if(e.getSource()==checkbox) recordActionPerformed(getValue());
		else System.err.println("CheckboxScripter: unexpected itemStateChanged from "+e.getSource());
	}
}
