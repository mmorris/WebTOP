/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//WidgetScripter.java
//Declares a class for automating WSL scripting with Widgets.
//Davis Herring
//Created November 10 2002
//Updated February 1 2004
//Version 0.2

package org.webtop.util.script;

import org.webtop.x3d.widget.Widget;
import org.webtop.wsl.client.WSLPlayer;
import org.webtop.wsl.event.*;

public class WidgetScripter extends Scripter implements Widget.Listener
{
	protected final Widget widget;

	public WidgetScripter(Widget widget,WSLPlayer player,String id,String param) {
		super(player,id,param);
		if(widget==null) {
			super.destroy();		//broken object should be discarded
			throw new NullPointerException("Widget cannot be null.");
		}
		this.widget=widget;
		widget.addListener(this);
	}

	//We implement this only to allow scripting of simple Widget instances;
	//there is no value to set at this level of abstraction.
	protected void setValue(String value) {}

	protected String getValue() {throw new RuntimeException("Widgets cannot be queried.");}

	protected void setEnabled(boolean on) {widget.setEnabled(on);}

	//This is a bit overkill, but to be complete:
	protected void destroy() {widget.removeListener(this); super.destroy();}

	//We do nothing on initialize -- we're just a widget, not the 'real' value-holder.
	//But is this how it should be done?  Should Scripter just not do as much?
	public void initialize(WSLScriptEvent event) {}

	//ACTION_PERFORMED events should not (directly) affect widgets
	protected boolean filter(WSLScriptEvent event) {
		return event.getID()!=event.ACTION_PERFORMED && super.filter(event);
	}

	//When the script clicks the widget, reflect this fact
	protected void process(WSLScriptEvent event) {
		switch(event.getID()) {
		case WSLScriptEvent.MOUSE_PRESSED: widget.setActive(true); break;
		case WSLScriptEvent.MOUSE_RELEASED: widget.setActive(false); break;
		}
		super.process(event);
	}

	//Make sure widget appears inactive when script stops
	public void playerStateChanged(WSLPlayerEvent event) {
		switch(event.getID()) {
		case WSLPlayerEvent.PLAYER_STOPPED: widget.setActive(false); break;
		}
		super.playerStateChanged(event);
	}

	public void mouseEntered(Widget src) {
		if(src==widget) recordMouseEntered();
		else System.err.println("WidgetScripter: unexpected mouseEntered from "+src);
	}
	public void mouseExited(Widget src) {
		if(src==widget) recordMouseExited();
		else System.err.println("WidgetScripter: unexpected mouseEntered from "+src);
	}
	public void mousePressed(Widget src) {
		if(src==widget) recordMousePressed();
		else System.err.println("WidgetScripter: unexpected mousePressed from "+src);
	}
	public void mouseReleased(Widget src) {
		if(src==widget) recordMouseReleased();
		else System.err.println("WidgetScripter: unexpected mouseReleased from "+src);
	}
}
