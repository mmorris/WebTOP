/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//IntBox.java
//Davis Herring
//Defines the IntBox, the NumberBox subclass that handles (32-bit) integers.
//Created August 13 2001
//Updated August 26 2003
//Vesion 1.5.1

package org.sdl.gui.numberbox;

import java.awt.event.*;
import org.sdl.math.FPRound;

public class IntBox extends NumberBox
{
	private int lastVal, curVal, minVal, maxVal;

	public IntBox() {this(0,1,0,10);}
	public IntBox(int min, int max) {this(min,max,0,10);}
	public IntBox(int min, int max, int init) {this(min,max,init,10);}
	public IntBox(int min, int max, int init, int columns)
	{
		super(columns);
		minVal=min;
		try {
			setMax0(max);
		}
		catch(BoundsException e) {
			//The only reference to this object is in the exception (since its
			//being thrown will prevent assignment of the result of 'new').  We
			//discard that reference, and this broken object can't be used.
			throw new BoundsException(null,e.getMessage());
		}
		lastVal=curVal=min;	//temp/backup assignments
		setValue(init);			//Now set up initial state
		curStr=String.valueOf(curVal);
	}

	public void setMin(int min) { //throws BoundsException
		if(min>maxVal) throw new BoundsException(this,"Min cannot be greater than max.");
		if(curVal<(minVal=min)) {
			processNumberEvent(EVENT_boundsForcedChange,getNumberValue());
			setValue(minVal);
		}
	}
	public int getMin() {return minVal;}

	public void setMax(int max) {setMax0(max);} //throws BoundsException
	private void setMax0(int max) { //throws BoundsException
		if(minVal>max) throw new BoundsException(this,"Max cannot be less than min.");
		if(curVal>(maxVal=max)) {
			processNumberEvent(EVENT_boundsForcedChange,getNumberValue());
			setValue(maxVal);
		}
	}
	public int getMax() {return maxVal;}

	public int getValue() {return curVal;}

	public void setValue(int val) {
		setText(val);
		validateEntry();
	}

	public void setValue(Number val) {setValue(val.intValue());}
	public Number getNumberValue() {return new Integer(curVal);}

	public void revert() {setValue(lastVal);}

	public boolean validateEntry() {
		int entry;

		try {entry=Integer.parseInt(getText());}
		catch(NumberFormatException e) {
			reset();
			processNumberEvent(EVENT_invalidEntry,null);
			return false;
		}

		if(entry >= minVal && entry <= maxVal) {		//In range; use it
			boolean changed = (curVal != entry);
			setText(entry);
			if(changed) {
				lastVal = curVal;
				curVal = entry;
				ping();
			}
			else processNumberEvent(EVENT_redundantEntry,getNumberValue());
			return true;
		} else {
			reset();
			processNumberEvent(EVENT_invalidEntry,new Integer(entry));
			return false;
		}
	}

	private void setText(int i) {setTheText(String.valueOf(i));}

	protected boolean check(String s) {
		try {
			Integer.parseInt(s.length()<2?s+'0':s);
			return true;
		} catch(NumberFormatException e) {return false;}
	}

	public String toString() {return getClass().getName()+"[interval=["+minVal+','+maxVal+"],value="+curVal+super.toString()+']';}
}
