/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//ChoiceScripter.java
//Automates scripting with Choices.
//Davis Herring
//Created August 27 2003
//Updated March 31 2004
//Version 0.11

package org.webtop.util.script;

import javax.swing.JComboBox;
import java.awt.event.*;

public class ChoiceScripter extends Scripter implements ItemListener
{
	private final JComboBox choice;
	private final ItemListener listener;
	private final String[] values;
	private final int defaultValue;

	public ChoiceScripter(JComboBox c,org.webtop.wsl.client.WSLPlayer player,String id,String param,String[] vals,int defVal,ItemListener il) {
		super(player,id,param);
		if(c==null) {
			super.destroy();	//broken object should be discarded
			throw new NullPointerException("Choice cannot be null.");
		}
		if(vals==null) {
			super.destroy();
			throw new NullPointerException("Values cannot be null.");
		}
		//defensive copying, yay
		values=(String[])vals.clone();
		if(values.length==0) {
			super.destroy();
			throw new IllegalArgumentException("Must have at least one value.");
		}
		if(defVal<0 || defVal>=values.length) {
			super.destroy();
			throw new IndexOutOfBoundsException("Default value "+defVal+" outside of range [0,"+values.length+").");
		}
		choice=c;
		choice.addItemListener(this);
		listener=il;
		defaultValue=defVal;
	}

	protected void setValue(String value) {
		if(listener!=null) {
			int i;
			for(i=0;i<values.length && !org.webtop.util.WTString.equal(value,values[i]);++i);
			if(i==values.length) i=defaultValue;
			
			listener.itemStateChanged(new ItemEvent(choice,ItemEvent.SELECTED,
					choice.getSelectedItem(),1));
			choice.setSelectedIndex(i);
			//org.webtop.component.WApplication.pickChoice(choice,i,listener);
		}
	}

	protected String getValue() {return values[choice.getSelectedIndex()];}

	protected void setEnabled(boolean on) {choice.setEnabled(on);}

	//This is a bit overkill, but to be complete:
	protected void destroy() {
		choice.removeItemListener(this);
		super.destroy();
	}

	public void itemStateChanged(ItemEvent e) {
		if(e.getSource()==choice) recordActionPerformed(getValue());
		else System.err.println("ChoiceScripter: unexpected itemStateChanged from "+e.getSource());
	}
}
