/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Manager.java
//Defines a class to clean up helper instances.
//Davis Herring
//Created November 8 2002
//Updated February 11 2004
//Version 1.01

package org.webtop.util;

import java.util.*;

/**
 * An object of type <code>Manager</code> maintains a list of
 * <code>Helper</code>s which it can release en masse.	In this manner
 * <code>Helper</code>s may conveniently be removed from listener
 * lists/etc. and then garbage-collected.
 *
 * @see Helper
 */
public class Manager
{
	/**
	 * The <code>Helper</code>s this manager will release.
	 */
	private final Vector helpers=new Vector();

	/**
	 * Adds the given object to the list of <code>Helper</code>s to
	 * be released.	 Does nothing if <code>c</code> is already in the
	 * list.
	 *
	 * @exception NullPointerException if <code>c</code> is null.
	 */
	public void addHelper(Helper c) {
		if(c==null) throw new NullPointerException("Can't release a null Helper.");
		if(!helpers.contains(c)) helpers.addElement(c);
	}
	/**
	 * Removes the given object from the list of <code>Helper</code>s to
	 * be released.	 Does nothing if <code>c</code> is null or not in
	 * the list.
	 */
	public void removeHelper(Helper c) {helpers.removeElement(c);}

	/**
	 * Releases all the <code>Helper</code>s in, and removes them from,
	 * this object's list.	No references are kept to the released objects.
	 */
	public void release() {
		Enumeration e=helpers.elements();
		while(e.hasMoreElements())
			try {
				((Helper)e.nextElement()).release();
			} catch(RuntimeException re) {
				System.err.println("Exception occurred during Helper release:");
				re.printStackTrace();
			}

		helpers.removeAllElements();
		//We don't trimToSize(); the point of releasing is often to recreate
	}
}
