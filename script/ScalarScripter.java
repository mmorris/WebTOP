/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//ScalarScripter.java
//Declares a class for automating WSL scripting with ScalarWidgets.
//Davis Herring
//Created November 10 2002
//Updated March 31 2004
//Version 0.11

package webtop.util.script;

import webtop.util.WTString;
import webtop.vrml.widget.ScalarWidget;
import webtop.wsl.client.WSLPlayer;

public class ScalarScripter extends WidgetScripter implements ScalarWidget.Listener
{
	private final float defaultValue;

	public ScalarScripter(ScalarWidget widget,WSLPlayer player,String id,String param,float defVal) {
		super(widget,player,id,param);
		defaultValue=defVal;
		widget.addListener((ScalarWidget.Listener)this);
	}

	protected void setValue(String value) {
		((ScalarWidget)widget).setValue(WTString.toFloat(value,defaultValue));
	}

	protected void destroy() {
		((ScalarWidget)widget).removeListener((ScalarWidget.Listener)this);
		super.destroy();
	}

	public void valueChanged(ScalarWidget src, float value) {
		if(src==widget) {
			if(src.isActive()) recordMouseDragged(String.valueOf(value));
		} else System.err.println("ScalarScripter: unexpected valueChanged from "+src);
	}
}
