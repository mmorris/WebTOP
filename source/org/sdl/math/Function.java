/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Function.java
//Declares interfaces for pure function objects.
//Davis Herring
//Created November 10 2002
//Updated November 13 2002
//Version 1.01

package org.sdl.math;

public interface Function {		//f(x)
	public interface TwoVar {		//f(x,y)
		public double eval(double arg1,double arg2);
		public static final TwoVar distance=new TwoVar() {public double eval(double x,double y) {return Math.sqrt(x*x+y*y);}};
		public static final TwoVar add=new TwoVar() {public double eval(double x,double y) {return x+y;}};
		public static final TwoVar subtract=new TwoVar() {public double eval(double x,double y) {return x-y;}};
		public static final TwoVar multiply=new TwoVar() {public double eval(double x,double y) {return x*y;}};
		public static final TwoVar divide=new TwoVar() {public double eval(double x,double y) {return x/y;}};
		public static final TwoVar pow=new TwoVar() {public double eval(double b,double e) {return Math.pow(b,e);}};
	}
	public interface ThreeVar {	//f(x,y,z)
		public double eval(double arg1,double arg2,double arg3);
		public static final ThreeVar distance=new ThreeVar() {public double eval(double x,double y,double z) {return Math.sqrt(x*x+y*y+z*z);}};
	}
	public interface NVar {			//f(...)
		public double eval(double[] args);
		public static final NVar distance=new NVar() {
				public double eval(double[] v) {
					double ret=0;
					for(int index=0;index<v.length;++index)
						ret+=v[index]*v[index];
					return Math.sqrt(ret);
				}
			};
	}
	public interface TwoD {			//<f,g>(x,y)
		public double[] eval(double arg1,double arg2);
		public static final TwoD swap=new TwoD() {public double[] eval(double arg1,double arg2) {return new double[] {arg2,arg1};}};
		public static final TwoD identity=new TwoD() {public double[] eval(double arg1,double arg2) {return new double[] {arg1,arg2};}};
	}
	public interface ThreeD {		//<f,g,h>(x,y,z)
		public double[] eval(double arg1,double arg2,double arg3);
		public static final ThreeD identity=new ThreeD() {public double[] eval(double arg1,double arg2,double arg3) {return new double[] {arg1,arg2,arg3};}};
	}
	public interface ND {				//<f,...>(...)
		public double[] eval(double[] args);
		public static final ND identity=new ND() {public double[] eval(double[] args) {return (double[])args.clone();}};
	}
	public double eval(double arg);
	public static final Function identity=new Function() {public double eval(double arg) {return arg;}};
}
