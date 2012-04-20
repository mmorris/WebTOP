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
 * This is the base class for WSLAPI event classes.  It encapsulates
 * information common to all WSL events: the event's type identifier, a
 * reference to the object generating the event, and the system time when the
 * event occured.
 *
 * @author	Yong Tze Chi
 */
public class WSLEvent {
	private final Object source;
	private final int id;
	private final long timeStamp;

	/**
	 * Constructs a <code>WSLEvent</code> with the given source and type number.
	 */
	public WSLEvent(Object src, int id) {
		source = src;
		this.id = id;
		timeStamp = System.currentTimeMillis();
	}

	/**
	 * Returns the object generating this event.
	 *
	 * @return	reference to the source object.
	 */
	public Object getSource() {return source;}

	/**
	 * Returns the event identifier.	The significance of this number is defined
	 * by subclasses of <code>WSLEvent</code>.
	 *
	 * @return	an integer identifying the type of event.
	 */
	public int getID() {return id;}

	/**
	 * Returns the timestamp associated with this event.
	 *
	 * @return system time when this event happened.
	 * @see System#currentTimeMillis()
	 */
	public long getTimeStamp() {return timeStamp;}

	/**
	 * Returns a string representing this event.
	 *
	 * @return a String (of unspecified format) describing this event.
	 */
	public String toString()
	{return getClass().getName()+"[source="+source+",id="+id+']';}
}
