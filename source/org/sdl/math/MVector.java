/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//MVector.java
//Defines the class MVector, for representing mathematical Cartesian vectors.
//Davis Herring
//Created August 3 2002
//Updated March 11 2004
//Version 1.11

package org.sdl.math;

public abstract class MVector
{
	public double x,y;

	public double theta() {return Math.atan2(y,x);}

	public abstract double mag();
	public abstract void scale(double s);
	public void setMag(final double m) {scale(m/mag());}
	public void normalize() {scale(1/mag());}

	public abstract double[] toArray(double[] a);
	public abstract float[] toFArray(float[] a);
}
