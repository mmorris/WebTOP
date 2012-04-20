/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//DoubleBox.java
//Davis Herring
//Defines the DoubleBox, the NumberBox subclass that handles 'double's.
//Created August 12 2001
//Updated August 26 2003
//Vesion 1.5.1

package org.sdl.gui.numberbox;

import java.awt.event.*;
import org.sdl.math.FPRound;

public class DoubleBox extends NumberBox
{
	private double lastVal, curVal, minVal, maxVal;

	public DoubleBox() {this(0,1,0,10);}
	public DoubleBox(double min, double max) {this(min,max,0,10);}
	public DoubleBox(double min, double max, double init) {this(min,max,init,10);}
	public DoubleBox(double min, double max, double init, int columns) {
		super(columns);
		if(min==Double.NEGATIVE_INFINITY) min=-Double.MAX_VALUE;
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

	public void setMin(double min) { //throws BoundsException
		if(min==Double.NEGATIVE_INFINITY) min=-Double.MAX_VALUE;
		if(min>maxVal) throw new BoundsException(this,"Min cannot be greater than max.");
		if(curVal<(minVal=min)) {
			processNumberEvent(EVENT_boundsForcedChange,getNumberValue());
			setValue(minVal);
		}
	}
	public double getMin() {return minVal;}

	public void setMax(double max) {setMax0(max);} //throws BoundsException
	private void setMax0(double max) { //throws BoundsException
		if(max==Double.POSITIVE_INFINITY) max=Double.MAX_VALUE;
		if(minVal>max) throw new BoundsException(this,"Max cannot be less than min.");
		if(curVal>(maxVal=max)) {
			processNumberEvent(EVENT_boundsForcedChange,getNumberValue());
			setValue(maxVal);
		}
	}
	public double getMax() {return maxVal;}

	public double getValue() {return curVal;}

	public void setValue(double val) {
		setText(val);
		validateEntry();
	}

	public void setSigValue(double val, int sigDigits)
	{setValue(FPRound.toSigVal(val,sigDigits));}
	public void setFixValue(double val, int places)
	{setValue(FPRound.toFixVal(val,places));}

	public void setValue(Number val) {setValue(val.doubleValue());}
	public Number getNumberValue() {return new Double(curVal);}

	public void revert() {setValue(lastVal);}

	public boolean validateEntry() {
		double entry;

		try {entry=new Double(getText()).doubleValue();}
		catch(NumberFormatException e) {
			setTheText(""+curVal);
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
			setText(curVal);
			processNumberEvent(EVENT_invalidEntry,new Double(entry));
			return false;
		}
	}

	private void setText(double d) {setTheText(String.valueOf(d));}

	protected boolean check(String s) {
		try {
			new Double(s.toLowerCase().endsWith("e")||
								 s.endsWith("-")||s.endsWith("+")||
								 s.endsWith(".")||s.length()==0?s+'0':s);
			return true;
		} catch(NumberFormatException e) {return false;}
	}

	public String toString() {return getClass().getName()+"[interval=["+minVal+','+maxVal+"],value="+curVal+super.toString()+']';}
}
