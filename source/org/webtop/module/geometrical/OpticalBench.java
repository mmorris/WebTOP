/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Maintains and orders the list of optical elements

package org.webtop.module.geometrical;

import java.util.*;

public class OpticalBench
{
	public class Enumeration {
		private java.util.Enumeration e;
		public Enumeration() {e=opticals.elements();}
		public OpticalElement nextElement() {return (OpticalElement)e.nextElement();}
		public boolean hasMoreElements() {return e.hasMoreElements();}
	}

	private Vector opticals=new Vector(5,3);
	public void add(OpticalElement oe) {
		if(opticals.contains(oe)) throw new IllegalArgumentException(oe+" is already on the bench.");
		int i=0;
		while(i<opticals.size() && elementAt(i).getPosition()<oe.getPosition()) i++;
		opticals.insertElementAt(oe,i);
	}
	public void remove(OpticalElement oe) {
		opticals.removeElement(oe);
	}
	public void update(OpticalElement oe) {
		int i=0,ioe=opticals.indexOf(oe);
		if(ioe==-1) throw new IllegalArgumentException(oe+" is not on the bench.");
		while(i<opticals.size() && (i==ioe || elementAt(i).getPosition()<oe.getPosition())) i++;
		if(i!=ioe) {
			opticals.insertElementAt(oe,i);
			//Adjust for insertion as necessary
			opticals.removeElementAt(ioe+(i<ioe?1:0));
		}
	}
	public int size() {return opticals.size();}
	public int indexOf(OpticalElement oe) {return opticals.indexOf(oe);}
	public OpticalElement elementAt(int i) {return (OpticalElement)opticals.elementAt(i);}
	public OpticalElement lastElement() {return (OpticalElement)opticals.lastElement();}
	public OpticalElement firstElement() {return (OpticalElement)opticals.firstElement();}
	public Enumeration elements() {return new Enumeration();}
}
