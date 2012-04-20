/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Vector3.java
//Defines the class Vector3, for representing three-dimensional Cartesian vectors.
//Davis Herring
//Created August 3 2002
//Updated March 11 2004
//Version 1.11

package org.sdl.math;

public class Vector3 extends MVector
{
	public double z;

	public Vector3(final double X,final double Y,final double Z) {x=X;y=Y;z=Z;}
	public Vector3(final Vector2 v) {x=v.x;y=v.y;}
	public Vector3(final Vector3 v) {x=v.x;y=v.y;z=v.z;}
	public double mag() {return Math.pow(x*x+y*y+z*z,.5);}
	public double cylR() {return Math.pow(x*x+y*y,.5);}
	public double phi() {return Math.atan2(z,Math.pow(x*x+y*y,.5));}
	public double lat() {return Math.PI/2-phi();}
	public void add(final Vector3 v) {x+=v.x;y+=v.y;z+=v.z;}
	public void subtract(final Vector3 v) {x-=v.x;y-=v.x;y-=v.z;}
	public void scale(final double s) {x*=s;y*=s;z*=s;}
	public void setCylR(final double r) {final double s=r/cylR();x*=s;y*=s;}
	public void setTheta(final double t) {final double r=cylR();x=r*Math.cos(t);y=r*Math.sin(t);}
	public void setPhi(final double p) {final double phi=phi(),s=Math.sin(p)/Math.sin(phi);x*=s;y*=s;z*=Math.cos(p)/Math.cos(phi);}
	public void setLat(final double l) {setPhi(Math.PI/2-l);}
	public void rotate(final Vector3 a,final double t) {
		if(a.x==0 && a.y==0 && a.z==0) throw new IllegalArgumentException("Cannot rotate about the 0 vector.");
		//We resolve ourselves into components parallel and perpendicular to the axis.
		//The parallel component will be unaffected.
		Vector3 par=proj(this,a),per=subtract(this,par);
		//If we have no such perpendicular component, we are along the axis: rotation does nothing.
		if(per.x==0 && per.y==0 && per.z==0) return;
		//Then take the vector perpendicular to both the
		//axis and our component perpendicular to the axis:
		Vector3 c=crossP(a,per);
		c.setMag(per.mag());
		//By the right-hand rule, this vector (if we place tail-to-tip a, then per, then c)
		//points counter-clockwise as seen from beyond the end of a.  We can then use it and
		//per as x-y components of a new "two-dimensional" vector in the plane perpendicular
		//to the axis.  We set their magnitudes equal and then apply the rotation.
		per.scale(Math.cos(t));
		c.scale(Math.sin(t));
		//With the vectors scaled, their sum is the new perpendicular component.
		//We then re-add the parallel component, and the vector is rotated.
		x=per.x+c.x+par.x;
		y=per.y+c.y+par.y;
		z=per.z+c.z+par.z;
	}

	public double[] toArray(final double[] a) {if(a==null) return new double[] {x,y,z}; a[0]=x;a[1]=y;a[2]=z; return a;}
	public float[] toFArray(final float[] a) {if(a==null) return new float[] {(float)x,(float)y,(float)z}; a[0]=(float)x;a[1]=(float)y;a[2]=(float)z; return a;}
	public String asString(boolean ijk) {
		if(ijk) {
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
			if(z!=0) {
				if(z>0 && sb.length()>0) sb.append('+');
				sb.append(z);
				sb.append('k');
			}
			return sb.toString();
		}
		return "<"+x+','+y+','+z+'>';
	}

	public boolean equals(Object o) {
		if(o instanceof Vector3) {
			Vector3 v=(Vector3)o;
			return x==v.x&&y==v.y&&z==v.z;
		} else return false;
	}
	public String toString() {return getClass().getName()+'['+x+','+y+','+z+']';}

	//Static methods do not modify arguments
	public static Vector3 add(final Vector3 v1,final Vector3 v2) {return new Vector3(v1.x+v2.x,v1.y+v2.y,v1.z+v2.z);}
	public static Vector3 subtract(final Vector3 v1,final Vector3 v2) {return new Vector3(v1.x-v2.x,v1.y-v2.y,v1.z-v2.z);}
	public static Vector3 multiply(final Vector3 v,final double s) {return new Vector3(v.x*s,v.y*s,v.z*s);}
	public static Vector3 proj(final Vector3 v,final Vector3 along) {final double s=comp(v,along)/along.mag();return new Vector3(along.x*s,along.y*s,along.z*s);}
	public static Vector3 crossP(final Vector3 v1,final Vector3 v2) {return new Vector3(v1.y*v2.z-v1.z*v2.y,v1.z*v2.x-v1.x*v2.z,v1.x*v2.y-v1.y*v2.x);}
	public static Vector3 unitV(final Vector3 v) {final double m=v.mag();return new Vector3(v.x/m,v.y/m,v.z/m);}
	public static Vector3 fromCylindrical(final double r,final double t,final double z) {return new Vector3(r*Math.cos(t),r*Math.sin(t),z);}
	public static Vector3 fromSpherical(final double r,final double t,final double p) {final double s=Math.sin(p);return new Vector3(r*s*Math.cos(t),r*s*Math.sin(t),r*Math.cos(p));}

	public static double comp(final Vector3 v,final Vector3 along) {final double xa=along.x,ya=along.y,za=along.z;return (v.x*xa+v.y*ya+v.z*za)/Math.pow(xa*xa+ya*ya+za*za,.5);}
	public static double dotP(final Vector3 v1,final Vector3 v2) {return v1.x*v2.x+v1.y*v2.y+v1.z*v2.z;}
	public static double doubleBetween(final Vector3 v1,final Vector3 v2) {final double x1=v1.x,x2=v2.x,y1=v1.y,y2=v2.y,z1=v1.z,z2=v2.z;return Math.acos((x1*x2+y1*y2+z1*z2)/Math.pow((x1*x1+y1*y1+z1*z1)*(x2*x2+y2*y2+z2*z2),.5));}

	public static Vector3 i() {return new Vector3(1,0,0);}
	public static Vector3 j() {return new Vector3(0,1,0);}
	public static Vector3 k() {return new Vector3(0,0,1);}
	public static Vector3 zero() {return new Vector3(0,0,0);}
}
