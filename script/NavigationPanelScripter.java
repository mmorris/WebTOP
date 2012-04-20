/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//NavigationPanelScripter.java
//Declares a class for automating WSL scripting with the NavigationPanel.
//Davis Herring
//Created March 3 2004
//Updated March 31 2004
//Version 0.12

package webtop.util.script;

import webtop.wsl.client.WSLPlayer;
import webtop.wsl.script.*;
import webtop.wsl.event.*;
import webtop.vrml.widget.*;

//For now, this class will only record view point changes.

public class NavigationPanelScripter extends WidgetScripter implements NavigationPanel.Listener
{
	public static final String VIEW_NODE="view";

	public NavigationPanelScripter(NavigationPanel navPanel,WSLPlayer player) {
		super(navPanel,player,null,"");
		if(navPanel==null) {
			super.destroy();		//broken object should be discarded
			throw new NullPointerException("NavigationPanel cannot be null.");
		}
		navPanel.addListener((NavigationPanel.Listener)this);
		//This may mean that NavigationPanelScripter should not really be a
		//WidgetScripter; I'm not sure.  The point is, we don't have a parameter
		//under which to log mouse events.  [Davis]
		navPanel.removeListener((Widget.Listener)this);
	}

	protected void setValue(String value) {
		if(value==null) ((NavigationPanel)widget).setActiveView(0);
		else ((NavigationPanel)widget).setView(value);
	}

	protected String getValue()
	{return NavigationPanel.viewString(((NavigationPanel)widget).getView());}

	public void addTo(WSLAttributeList atts) {throw new RuntimeException("NavigationPanels must be added to a WSLNode.");}

	public void addTo(WSLNode node) {
		final WSLAttributeList atts=new WSLAttributeList();
		atts.add(WSLNode.VALUE,getValue());
		node.addChild(new WSLNode(VIEW_NODE,atts));
	}

	//This is a bit overkill, but to be complete:
	protected void destroy() {
		((NavigationPanel)widget).removeListener((NavigationPanel.Listener)this);
		super.destroy();
	}

	public void initialize(WSLScriptEvent e) {
		if(wslPlayer.isViewpointEventEnabled()) {
			final WSLNode view=e.getNode().getNode(VIEW_NODE);
			setValue(view==null?null:view.getAttributes().getValue(WSLNode.VALUE));
		}
	}

	protected void process(WSLScriptEvent event) {
		switch(event.getID()) {
		case WSLScriptEvent.VIEWPOINT_CHANGED:
			((NavigationPanel)widget).setView(event.getValue());
			break;
		case WSLScriptEvent.VIEWPOINT_SELECTED:
			//0 indicates the default viewpoint
			((NavigationPanel)widget).setActiveView(webtop.util.WTString.toInt(event.getValue(),0));
			break;
		}
		super.process(event);
	}

	public void playerStateChanged(WSLPlayerEvent event) {
		//This is something of a hack, but it gets the desired optional disabling
		//to happen.
		switch(event.getID()) {
		case WSLPlayerEvent.PLAYER_STARTED:
			playbackDisable=wslPlayer.isViewpointEventEnabled();
			break;
		case WSLPlayerEvent.PLAYER_STOPPED:
			playbackDisable=true;
			break;
		}
		super.playerStateChanged(event);
	}

	//Not doing anything with this at the moment.
	public void visibilityChanged(NavigationPanel src,boolean visible) {
		if(src!=widget) System.err.println("NavigationPanelScripter: unexpected visibilityChanged from "+src);
	}

	public void viewChanged(NavigationPanel src,float[] view) {
		if(src==widget) {
			if(src.getActiveView()==-1)
				recordViewpointChanged(NavigationPanel.viewString(view));
		} else System.err.println("NavigationPanelScripter: unexpected viewChanged from "+src);
	}

	public void activeViewChanged(NavigationPanel src,int view) {
		if(src==widget) {
			if(view!=-1) recordViewpointSelected(String.valueOf(view));
		} else System.err.println("NavigationPanelScripter: unexpected activeViewChanged from "+src);
	}
}
