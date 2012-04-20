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
 * <code>WSLProgressEvent</code> is used to report the progress of
 * <code>WSLPlayer</code> while playing a script.  During playback,
 * <code>WSLPlayer</code> posts <code>WSLProgressEvent</code> regularly to all
 * registered <code>WSLProgressListener</code>.  Progress is reported in terms
 * of current playback time. Methods are provided to access these information.
 *
 * @see			WSLProgressListener
 * @see			webtop.wsl.client.WSLPlayer
 *
 * @author	Yong Tze Chi
 */
public class WSLProgressEvent extends WSLEvent {
	private final float progress;
	private final float total;

	public static final int PROGRESS_EVENT = 101;

	/**
	 * Constructs a <code>WSLProgressEvent</code> with the given source, current
	 * progress, and total playback time.
	 *
	 * @param	 source		 reference to the source object.
	 * @param	 progress	 current progress value.
	 * @param	 total		 total progress value.
	 */
	public WSLProgressEvent(Object source, float progress, float total) {
		super(source,PROGRESS_EVENT);
		if(progress>total) throw new IllegalArgumentException("progress "+progress+" > total "+total+'.');
		this.progress = progress;
		this.total = total;
	}

	/**
	 * Gets current progress value.  This will be no greater than the total
	 * progress value.
	 *
	 * @return	a <code>float</code> specifying the current progress value.
	 */
	public float getProgress() {return progress;}

	/**
	 * Gets total progress value.
	 *
	 * @return	the total progress value.
	 */
	public float getTotal() {return total;}
}
