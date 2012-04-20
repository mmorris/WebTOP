/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//ScrollbarScripter.java
//Automates scripting with Scrollbars.
//Davis Herring
//Created August 27 2003
//Updated March 31 2004
//Version 0.11

package webtop.util.script;

import java.awt.Scrollbar;
import java.awt.event.*;

public class ScrollbarScripter extends Scripter implements AdjustmentListener
{
	private final Scrollbar scrollbar;
	private final AdjustmentListener listener;
	private final int defaultValue;

	public ScrollbarScripter(Scrollbar s,webtop.wsl.client.WSLPlayer player,String id,String param,int defVal,AdjustmentListener al) {
		super(player,id,param);
		if(s==null) {
			super.destroy();	//broken object should be discarded
			throw new NullPointerException("Scrollbar cannot be null.");
		}
		scrollbar=s;
		scrollbar.addAdjustmentListener(this);
		listener=al;
		defaultValue=defVal;
	}

	protected void setValue(String value) {
		if(listener!=null)
			webtop.component.WApplet.setScrollbar(scrollbar,webtop.util.WTString.toInt(value,defaultValue),listener);
	}

	protected String getValue() {return String.valueOf(scrollbar.getValue());}

	protected void setEnabled(boolean on) {scrollbar.setEnabled(on);}

	//This is a bit overkill, but to be complete:
	protected void destroy() {
		scrollbar.removeAdjustmentListener(this);
		super.destroy();
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		if(e.getSource()==scrollbar) recordActionPerformed(getValue());
		else System.err.println("ScrollbarScripter: unexpected adjustmentValueChanged from "+e.getSource());
	}
}
