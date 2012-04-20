/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package webtop.wsl.event;

/**
 * This class is used to signal state transitions in <code>WSLPlayer</code>.
 * This class defines event identifiers associated with state transitions.
 *
 * @see			WSLPlayerListener
 * @see			webtop.wsl.client.WSLPlayer
 *
 * @author	Yong Tze Chi
 */
public class WSLPlayerEvent extends WSLEvent {
	public static final int SCRIPT_LOADED = 2001,
													SCRIPT_UNLOADED = 2002,
													PLAYER_STARTED = 2003,
													PLAYER_PAUSED = 2004,
													PLAYER_STOPPED = 2005,
													RECORDER_STARTED = 2006,
													RECORDER_STOPPED = 2007,
													PLAYER_RESET = 2008;

	/**
	 * Constructs a <code>WSLPlayerEvent</code> with a source and an event
	 * identifier.
	 *
	 * @param	 source	 reference to the object generating this event.
	 * @param	 id			 event identifier.
	 */
	public WSLPlayerEvent(Object source, int id) {super(source, id);}
}
