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

/**
 * This interface is implemented by classes that wish to listen to
 * <code>WSLScriptEvent</code> during playback of a script.	 Two methods are to
 * be implemented in order for <code>WSLPlayer</code> to interface correctly
 * with the listener class.	 When the playback is started, the
 * <code>initialize()</code> method is called, and during playback the <a
 * href="#scriptActionFired"> <code>scriptActionFired()</code></a> method is
 * called regularly to post <code>WSLScriptEvent</code>s.
 *
 * @see			WSLScriptEvent
 * @see			webtop.wsl.client.WSLPlayer#addListener
 *
 * @author	Yong Tze Chi
 */
public interface WSLScriptListener {
	/**
	 * This method is called to initialize the WebTOP module when the playback
	 * script is started.	 The WebTOP module should initialize itself to the
	 * starting state provided by the <a href="WSLNode"><code>WSLNode</code></a>
	 * instance associated with the <code>WSLScriptEvent</code>.
	 *
	 * @param	 event	the <code>WSLScriptEvent</code> instance that contains
	 *								information about this event.
	 */
	public void initialize(WSLScriptEvent event);

	/**
	 * This method is called regularly during playback.	 The object implenting
	 * <code>WSLScriptListener</code> should process all events it understands and
	 * ignore those it does not.
	 *
	 * @param	 event	the <code>WSLScriptEvent</code> instance that contains
	 *								information about the parameter value change.
	 */
	public void scriptActionFired(WSLScriptEvent event);
}
