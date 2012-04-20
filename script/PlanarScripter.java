/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//PlanarScripter.java
//Declares a class for automating WSL scripting with ScalarWidgets.
//Davis Herring
//Created November 10 2002
//Updated March 31 2004
//Version 0.11

package webtop.util.script;

import webtop.util.WTString;
import webtop.vrml.widget.PlanarWidget;
import webtop.wsl.client.WSLPlayer;

public class PlanarScripter extends WidgetScripter implements PlanarWidget.Listener
{
	private final float defaultXValue,defaultYValue;

	public PlanarScripter(PlanarWidget widget,WSLPlayer player,String id,String param,float defX,float defY) {
		super(widget,player,id,param);
		defaultXValue=defX;
		defaultYValue=defY;
		widget.addListener((PlanarWidget.Listener)this);
	}

	protected void setValue(String value) {
		int split = value.indexOf(',');
		if(split==-1) ((PlanarWidget)widget).setValue(defaultXValue,defaultYValue);
		else ((PlanarWidget)widget).setValue(WTString.toFloat(value.substring(0,split),defaultXValue),
																				 WTString.toFloat(value.substring(split+1),defaultYValue));
	}

	protected void destroy() {
		((PlanarWidget)widget).removeListener((PlanarWidget.Listener)this);
		super.destroy();
	}

	public void valueChanged(PlanarWidget src, float x,float y) {
		if(src==widget) {
			if(src.isActive()) recordMouseDragged(String.valueOf(x)+','+y);
		} else System.err.println("PlanarScripter: unexpected valueChanged from "+src);
	}
}
