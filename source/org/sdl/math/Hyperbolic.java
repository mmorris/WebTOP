/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Hyperbolic.java
//Defines the class Hyperbolic with static hyperbolic trigonometric functions.
//Davis Herring
//Created August 16 2002
//Updated March 12 2004
//Version 1.01

//Some portions taken from my adaptation of the WebTOP math libraries into webtop.util.WTMath.

package org.sdl.math;

public final class Hyperbolic
{
	private Hyperbolic() {}

	public static double sin(final double x)
	{return (Math.exp(x)-Math.exp(-x))/2;}
	public static double cos(final double x)
	{return(Math.exp(x)+Math.exp(-x))/2;}
	public static double tan(final double x)
	{final double e=Math.exp(x),i=1/e; return (e-i)/(e+i);}

	public static double sec(final double x)
	{return 2/(Math.exp(x)+Math.exp(-x));}
	public static double csc(final double x)
	{return 2/(Math.exp(x)-Math.exp(-x));}
	public static double cot(final double x)
	{final double e=Math.exp(x),i=1/e; return (e+i)/(e-i);}

	public static double asin(final double x)
	{return Math.log(x+Math.pow(x*x+1,.5));}
	public static double acos(final double x)
	{return Math.log(x+Math.pow(x*x-1,.5));}
	public static double atan(final double x)
	{return .5*Math.log((1+x)/(1-x));}

	public static double asec(final double x)
	{final double i=1/x; return Math.log(i+Math.pow(i*i-1,.5));}
	public static double acsc(final double x)
	{final double i=1/x; return Math.log(i+Math.pow(i*i+1,.5));}
	public static double acot(final double x)
	{return .5*Math.log((1-x)/(1+x));}
}
