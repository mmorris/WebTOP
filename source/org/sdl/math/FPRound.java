/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//FPRound.java
//Davis Herring
//Defines the FPRound class, which provides static methods for rounding floating-point numbers.
//Created September 5 2001
//Updated February 15 2003
//Version 1.3a (cosmetically different from 1.3)

//Methods:  [value may be float or double]

//logfloor10(value) returns what exponent value would have in scientific
//	notation.
//toSigVal(value,digits) will round value to <digits> significant digits.
//toFixVal(value,places) will round value to <places> decimal places after the
//	integer part displayed by Number.toString(), except for numbers in the
//	interval +/-[0.001,1) which are rounded to <places> significant digits.
//toDelExpVal(value,deltaExp) will round value to the closest number which is
//	equal to some integer divided by 10^deltaExp.  It's called by the other two
//	methods and is rarely particularly useful by itself.

package org.sdl.math;

public abstract class FPRound
{
	public static final double log10=Math.log(10);

	public static final int logfloor10(double value) {
		double absLog=Math.log(Math.abs(value));
		return (int)Math.floor((Double.isInfinite(absLog)?0:absLog)/log10);
	}

	public static float toSigVal(float value, int sigDigits)
	{return toDelExpVal(value,sigDigits-1-logfloor10(value));}

	public static String showZeros(float value, int sigDigits)
	{
		//sigDigits refers to digits after decimal point
		String output;
		float rounded;
		String decimal; //portion of number after decimal point

		int logfloor = logfloor10(value);
		if (logfloor>=0 && logfloor<=6)
			rounded = toDelExpVal(value,sigDigits);
		else
			rounded = toDelExpVal(value,sigDigits);

		System.out.println("value: "+value+" exp: "+logfloor);

		output = String.valueOf(rounded);

		int periodIndex;

		periodIndex = output.indexOf(".");
		decimal = output.substring(periodIndex+1);

		for(int i = 0; i < (sigDigits - decimal.length()); i++){
			output = output.concat("0");
		}

		return output;
	}

	public static double toSigVal(double value, int sigDigits)
	{return toDelExpVal(value,sigDigits-1-logfloor10(value));}

	public static float toFixVal(float value, int fixPlaces)
	{
		int logfloor=logfloor10(value);
		if(logfloor>=-3 && logfloor<=-1)
			return toDelExpVal(value,fixPlaces-1-logfloor);		//To treat decimals like .0032f properly
		else if(logfloor>=0 && logfloor<=6)
			return toDelExpVal(value,fixPlaces);
		else return toDelExpVal(value,fixPlaces-logfloor);
	}

	public static double toFixVal(double value, int fixPlaces)
	{
		int logfloor=logfloor10(value);
		if(logfloor>=-3 && logfloor<=-1)
			return toDelExpVal(value,fixPlaces-1-logfloor);		//To treat decimals like .0032 properly
		else if(logfloor>=0 && logfloor<=6)
			return toDelExpVal(value,fixPlaces);
		else return toDelExpVal(value,fixPlaces-logfloor);
	}

	public static float toDelExpVal(float value, int deltaExp)
	{return (float)(Math.rint(value*Math.pow(10,deltaExp))/Math.pow(10,deltaExp));}

	public static double toDelExpVal(double value, int deltaExp)
	{return Math.rint(value*Math.pow(10,deltaExp))/Math.pow(10,deltaExp);}
}
