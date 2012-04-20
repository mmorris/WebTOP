/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//NumberBoxScripter.java
//Declares a class for automating WSL scripting with NumberBoxen.
//Davis Herring
//Created November 9 2002
//Updated March 31 2004
//Version 0.22

package org.webtop.util.script;

import org.sdl.gui.numberbox.NumberBox;
import org.webtop.wsl.client.WSLPlayer;
import org.webtop.wsl.event.WSLScriptEvent;

public class NumberBoxScripter extends Scripter implements NumberBox.Listener
{
	private final NumberBox field;
	public final Number defaultValue;

	public NumberBoxScripter(NumberBox box,WSLPlayer player,String id,String param,Number defVal) {
		super(player,id,param);
		if(box==null) {
			super.destroy();	//broken object should be discarded
			throw new NullPointerException("NumberBox cannot be null.");
		}
		field=box;
		defaultValue=defVal;
		field.addNumberListener(this);
	}

	protected void setValue(String value) {
		if(value==null) field.setValue(defaultValue);
		else {
			Number val;
			try {
				//We use the largest types; they'll be cast properly if appropriate
				if(value.indexOf('.')==-1) val=new Long(value);
				else val=new Double(value);
			} catch(NumberFormatException e) {val=defaultValue;}
			field.setValue(val);
		}
	}

	protected String getValue() {return field.getNumberValue().toString();}

	protected void setEnabled(boolean on) {field.setEditable(on);}

	//This is a bit overkill, but to be complete:
	protected void destroy() {field.removeNumberListener(this); super.destroy();}

	//We screen MOUSE_DRAGGED events; they should not (directly) affect
	//NumberBoxen.
	protected boolean filter(WSLScriptEvent event) {
		// no need to filter out MOUSE_DRAGGED events
		// because we use NumberBoxScripters instead of WidgetScripters
		// in our new code.  Old script files still use the MOUSE_DRAGGED
		// events so we need to support them in the new versions, and we do
		// so by routing the MOUSE_DRAGGED events that would have operated
		// on Widgets to the NumberBox.
		// - [Shane Fry]
		
		//return event.getID()!=event.MOUSE_DRAGGED && super.filter(event);    																				
		return super.filter(event); 
	}

	public void numChanged(NumberBox src, Number newVal) {
		if(src==field) recordActionPerformed(newVal.toString());
		else System.err.println("NumberBoxScripter: unexpected numChanged from "+src);
	}

	//This class cares not about other NumberBox events; we just check sources
	public void boundsForcedChange(NumberBox src, Number oldVal) {
		if(src!=field) System.err.println("NumberBoxScripter: unexpected boundsForcedChange from "+src);
	}
	public void invalidEntry(NumberBox src, Number badVal) {
		if(src!=field) System.err.println("NumberBoxScripter: unexpected invalidEntry from "+src);
	}
}
