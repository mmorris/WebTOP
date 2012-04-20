/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//AnimationEngine.java
//Defines the interface for an Animation's calculation engine
//Davis Herring
//Created July 20 2002
//Updated March 26 2004
//Version 1.0 (cosmetically different from 0.3)

package org.webtop.util;

/**
 * An object that implements <code>AnimationEngine</code> can be given to an
 * <code>Animation</code>, which will then use it for calculations.
 */
public interface AnimationEngine
{
	/**
	 * Called when the <code>Animation</code> object is constructed.	The
	 * <code>Animation<code> is completely established, but the thread has not
	 * been started.	This will only be called once by <code>Animation</code>
	 * (although other code may of course call it).
	 */
	public void init(Animation a);

	/**
	 * Called just before a calculation.	The argument indicates how many
	 * animation periods have elapsed since last call.	Ideally is always close
	 * to 1; may be greater if system is unable to keep up with animation.	A
	 * value of 0 indicates that an update has been explicitly requested.	 This
	 * function should return false if the engine does not wish to execute this
	 * iteration (as might be appropriate if system load is too high).
	 *
	 * @param periods the number of periods that have elapsed since the last
	 *								call to this function.
	 * @return true if execute() should be called for this iteration; false
	 *				 otherwise.
	 */
	public boolean timeElapsed(float periods);

	/**
	 * Performs the calculation for the animation.
	 *
	 * <p>The data object supplied is guaranteed to remain unchanged throughout
	 * the call to this function; furthermore, successive calls to execute will
	 * use the same <code>Data</code> object if and only if no new
	 * <code>Data</code> object has been given to the <code>Animation</code>.
	 *
	 * @param d the client-specified data object containing parameters for the
	 *					animation.
	 */
	public void execute(Animation.Data d);
}
