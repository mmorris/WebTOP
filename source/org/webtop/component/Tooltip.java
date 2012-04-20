/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Tooltip.java
//Defines a class to automate help messages for GUI components.
//Davis Herring
//Created October 27 2002
//Updated May 25 2004
//Version 1.1.1

//It might be useful to have some sort of static hash table mapping Components
//to Tooltips -- for example, in situations where you use a RecursiveListener
//to set up a Tooltip over an area, and then want to replace the general tip
//for a specific control in that hierarchy.

//Already RecursiveListener-s (with their new forceRelease() method) can be
//used to establish Tooltips (or other GUIListeners, of course) on a component
//tree "but not on this subtree" (that is, forceRelease() with a
//RecursiveListener on that tree).

package org.webtop.component;

import java.awt.*;
import java.awt.event.*;

public class Tooltip extends MouseAdapter implements GUIListener
{
	public interface Listener {
		//This once deliberately had the same signature as VRMLMouseRelay.Listener,
		//but that interface has been superceded by the widgets library.
		//tip will be null iff the mouse has left the component (or src's tip IS null)
		public void toolTip(Tooltip src,String tip);
	}
	public static class SBListener implements Listener {
		public final StatusBar statusBar;
		public SBListener(StatusBar sb) {if((statusBar=sb)==null) throw new NullPointerException("Status bar must not be null.");}
		public void toolTip(Tooltip src,String tip) {statusBar.setText(tip);}
	}

	public String tooltip;		//no reason not to allow tooltip changes
	private final Listener listener;

	public Tooltip(String tip,Listener l) {
		if(l==null) throw new NullPointerException("Listener must not be null.");
		tooltip=tip;
		listener=l;
	}

	public void register(Component c) {c.addMouseListener(this);}
	public void deregister(Component c) {c.removeMouseListener(this);}

	public void mouseEntered(MouseEvent e) {listener.toolTip(this,tooltip);}
	public void mouseExited(MouseEvent e) {listener.toolTip(this,null);}

	public String toString() {return getClass().getName()+'['+tooltip+']';}
}
