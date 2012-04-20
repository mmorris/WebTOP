/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.wsl.event;

import org.webtop.wsl.script.*;
import org.webtop.util.WTString;

/**
 * This is used by <code>WSLPlayer</code> to playback user interactions saved
 * in a script. An instance of <code>WSLScriptEvent</code> is generated during
 * playback when it is time for <code>WSLPlayer</code> to fire that event. The
 * event identifier in <code>WSLScriptEvent</code> specifies which type of
 * action is to be played back.  Additional information, such as the target of
 * this event, the parameter affected, and the value can be obtained through
 * the methods provided. An object wishing to receive
 * <code>WSLScriptEvent</code> implements the <code>WSLScriptListener</code>
 * interface and registers itself with a <code>WSLPlayer</code> through its
 * <code>addListener()</code> method.
 *
 * @see			WSLScriptListener
 * @see			webtop.wsl.client.WSLPlayer#addListener
 *
 * @author	Yong Tze Chi
 */
public class WSLScriptEvent extends WSLEvent {
	private final String target;
	private final String parameter;
	private final String value;
	private final WSLNode node;

	public static final int OBJECT_ADDED = 1001,
													OBJECT_REMOVED = 1002,
													MOUSE_ENTERED = 1003,
													MOUSE_EXITED = 1004,
													MOUSE_PRESSED = 1005,
													MOUSE_RELEASED = 1006,
													MOUSE_DRAGGED = 1007,
													ACTION_PERFORMED = 1008,
													VIEWPOINT_CHANGED = 1009,
													VIEWPOINT_SELECTED = 1010,
													INITIALIZE_MODULE = -1;

	/**
	 * Constructs this <code>WSLScriptEvent</code> with a source object and a
	 * script data node.	The <code>WSLNode</code> contains information about the
	 * script event.
	 *
	 * @param	 source	 reference to the source object generating this event.
	 * @param	 n			 <code>WSLNode</code> instance containing information
	 *								 about the recorded user interaction.
	 * @param  name		 name of the <code>WSLModule</code> for which the event is
	 *								 being created.
	 */
	public WSLScriptEvent(Object source, WSLNode n, String name) {
		super(source,getEventID(n,name));

		node = n;
		target = n.getAttributes().getValue("target");
		parameter = n.getAttributes().getValue("param");
		value = n.getAttributes().getValue("value");
	}

	private static int getEventID(WSLNode node,String name) {
		//We have to throw these here, as we're used in the super() call.
		if(node==null) throw new NullPointerException("no WSLNode specified");
		if(name==null) throw new NullPointerException("no module name given");
		final String tag = node.getName();
		if(tag.equals("objectAdded")) return OBJECT_ADDED;
		else if(tag.equals("objectRemoved")) return OBJECT_REMOVED;
		else if(tag.equals("mouseEntered")) return MOUSE_ENTERED;
		else if(tag.equals("mouseExited")) return MOUSE_EXITED;
		else if(tag.equals("mousePressed")) return MOUSE_PRESSED;
		else if(tag.equals("mouseReleased")) return MOUSE_RELEASED;
		else if(tag.equals("mouseDragged")) return MOUSE_DRAGGED;
		else if(tag.equals("actionPerformed")) return ACTION_PERFORMED;
		else if(tag.equals("viewpointChanged")) return VIEWPOINT_CHANGED;
		else if(tag.equals("viewpointSelected")) return VIEWPOINT_SELECTED;
		else if(tag.equals(name)) return INITIALIZE_MODULE;
		else throw new IllegalArgumentException("Unknown script event "+tag);
	}

	/**
	 * Gets the <code>target</code> attribute of this script event.  The
	 * <code>target</code> attribute tells which object in the WebTOP module is
	 * to be affected by this script event.
	 *
	 * @return value of the <code>target</code> attribute.
	 */
	public String getTarget() {return target;}

	/**
	 * Gets the <code>param</code> attribute of this script event.	The
	 * <code>param</code> attribute specifies what parameter this script event
	 * would affect.
	 *
	 * @return value of the <code>param</code> attribute.
	 */
	public String getParameter() {return parameter;}

	/**
	 * Gets the <code>value</code> attribute of this script event.	The
	 * <code>value</code> specifies the new value of the parameter affected by
	 * this script event.
	 *
	 * @return new value of the parameter affected by this script event.
	 */
	public String getValue() {return value;}

	/**
	 * Returns the <code>WSLNode</code> associated with this script event.
	 * Information that cannot be accessed through other
	 * <code>WSLScriptEvent</code> methods can be accessed through the
	 * <code>WSLNode</code> instance. For instance, a <code>OBJECT_ADDED</code>
	 * event has a <code>WSLNode</code> instance describing the new object to be
	 * added.
	 *
	 * @return reference to the <code>WSLNode</code> associated with this script
	 *         event.  Will never return null.
	 */
	public WSLNode getNode() {return node;}
}
