/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Helper.java
//Declares an interface for helper classes
//that should be automatically removed/released.
//Davis Herring
//Created November 10 2002
//Updated November 22 2002
//Version 0.0

package org.webtop.util;

/**
 * An object implementing <code>Helper</code> is a background object
 * that should be released along with other background objects.
 * Such destruction often must occur whenever the applet is
 * <code>stop()<code>ped and re-<code>start()ed</code>, as well as
 * when objects are removed from the module (as per
 * polarizers/wave sources/etc.).
 *
 * @see Manager
 */
public interface Helper
{
	/**
	 * Disables this object and removes it from whatever associations
	 * it had made with other module objects.	 Must be callable
	 * multiple times without detrimental effect.
	 */
	public void release();
}
