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
 * A <code>WSLProgressListener</code> listens to <code>WSLProgressEvent</code>s
 * posted by a <code>WSLPlayer</code>.	During playback, a
 * <code>WSLPlayer</code> calls the <a
 * href="#progressChanged()"><code>progressChanged()</code></a> method in all
 * registered <code>WSLProgressListener</code>s to report progress of the
 * playback.	In order for an object to receive this information, it has to
 * implement the <code>WSLProgressListener</code>, and register itself with
 * <code>WSLPlayer</code> through its <code>addListener()</code> method.
 *
 * @see			WSLProgressEvent
 * @see			webtop.wsl.client.WSLPlayer#addListener
 *
 * @author	Yong Tze Chi
 */
public interface WSLProgressListener {
	/**
	 * This method is called by <code>WSLPlayer</code> to report progress of
	 * script playback.
	 *
	 * @param	 event	information about the progress supplied by <code>WSLPlayer</code>.
	 */
	public void progressChanged(WSLProgressEvent event);
}
