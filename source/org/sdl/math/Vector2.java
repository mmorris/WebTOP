/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Vector2.java
//Defines the class Vector2, for representing two-dimensional Cartesian vectors.
//Davis Herring
//Created August 3 2002
//Updated March 11 2004
//Version 1.11

package org.sdl.math;

public class Vector2 extends MVector
{
	public Vector2(final double X,final double Y) {x=X;y=Y;}
	public Vector2(MVector v) {x=v.x;y=v.y;}
	public double mag() {return Math.pow(x*x+y*y,.5);}
	public void add(final Vector2 v) {x+=v.x;y+=v.y;}
	public void subtract(final Vector2 v) {x-=v.x;y-=v.x;}
	public void scale(final double s) {x*=s;y*=s;}
	public void setTheta(final double t) {final double m=mag();x=m*Math.cos(t);y=m*Math.sin(t);}
	public void rotate(final double t) {final double c=Math.cos(t),s=Math.sin(t);x=c*x-s*y;y=s*x+c*y;}

	public double[] toArray(final double[] a) {if(a==null) return new double[] {x,y}; a[0]=x;a[1]=y; return a;}
	public float[] toFArray(final float[] a) {if(a==null) return new float[] {(float)x,(float)y}; a[0]=(float)x;a[1]=(float)y; return a;}
	public String asString(boolean ij) {
		if(ij) {
			StringBuffer sb=new StringBuffer(24);
			if(x!=0) {
				sb.append(x);
				sb.append('i');
			}
			if(y!=0) {
				if(y>0 && sb.length()>0) sb.append('+');
				sb.append(y);
				sb.append('j');
			}
			return sb.toString();
		}
		return "<"+x+','+y+'>';
	}

	public boolean equals(Object o) {
		if(o instanceof Vector2) {
			Vector2 v=(Vector2)o;
			return x==v.x&&y==v.y;
		} else return false;
	}
	public String toString() {return getClass().getName()+'['+x+','+y+']';}

	//Static methods do not modify arguments
	public static Vector2 add(final Vector2 v1,final Vector2 v2) {return new Vector2(v1.x+v2.x,v1.y+v2.y);}
	public static Vector2 subtract(final Vector2 v1,final Vector2 v2) {return new Vector2(v1.x-v2.x,v1.y-v2.y);}
	public static Vector2 multiply(final Vector2 v,final double s) {return new Vector2(v.x*s,v.y*s);}
	public static Vector2 proj(final Vector2 v,final Vector2 along) {final double s=comp(v,along)/along.mag();return new Vector2(along.x*s,along.y*s);}
	public static Vector2 unitV(final Vector2 v) {final double m=v.mag();return new Vector2(v.x/m,v.y/m);}
	public static Vector2 fromPolar(final double r,final double t) {return new Vector2(r*Math.cos(t),r*Math.sin(t));}

	public static double comp(final Vector2 v,final Vector2 along) {final double xa=along.x,ya=along.y;return (v.x*xa+v.y*ya)/Math.pow(xa*xa+ya*ya,.5);}
	public static double dotP(final Vector2 v1,final Vector2 v2) {return v1.x*v2.x+v1.y*v2.y;}

	public static Vector2 i() {return new Vector2(1,0);}
	public static Vector2 j() {return new Vector2(0,1);}
	public static Vector2 zero() {return new Vector2(0,0);}
}
