/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Complex.java
//Defines the class Complex, for representing complex numbers.
//Davis Herring
//Created August 6 2002
//Updated August 17 2002
//Version 1.21 (CSDL v3.32)

package org.sdl.math;

public class Complex
{
	public double R,I;

	public Complex() {}
	public Complex(double a,double b) {R=a;I=b;}
	public Complex(Complex c) {R=c.R;I=c.I;}
	public static Complex fromPolar(double r,double t) {return new Complex(r*Math.cos(t),r*Math.sin(t));}

	public double mag() {return Math.pow(R*R+I*I,.5);}
	public double arg() {return Math.atan2(I,R);}
	public Complex log() {return new Complex(Math.log(R*R+I*I)/2,arg());}
	public Complex exp() {return fromPolar(Math.exp(R),I);}

	public Complex conjugate() {return new Complex(R,-I);}

	public void setRect(double a,double b) {R=a;I=b;}
	public void setPolar(double m,double t) {R=m*Math.cos(t);I=m*Math.sin(t);}

	public void add(Complex c) {R+=c.R;I+=c.I;}
	public void add(double d) {R+=d;}
	public void subtract(Complex c) {R-=c.R;I-=c.I;}
	public void subtract(double d) {R-=d;}
	public void multiply(Complex c) {setRect(R*c.R-I*c.I,R*c.I+I*c.R);}
	public void multiply(double d) {R*=d;I*=d;}
	public void divide(Complex c) {double m=c.R*c.R+c.I*c.I;setRect((R*c.R+I*c.I)/m,(c.R*I-c.I*R)/m);}
	public void divide(double d) {R/=d;I/=d;}

	public static Complex add(Complex c1,Complex c2) {return new Complex(c1.R+c2.R,c1.I+c2.I);}
	public static Complex add(Complex c,double d) {return new Complex(c.R+d,c.I);}
	public static Complex subtract(Complex c1,Complex c2) {return new Complex(c1.R-c2.R,c1.I-c2.I);}
	public static Complex subtract(Complex c,double d) {return new Complex(c.R-d,c.I);}
	public static Complex multiply(Complex c1,Complex c2) {return new Complex(c1.R*c2.R-c1.I*c2.I,c1.R*c2.I+c1.I*c2.R);}
	public static Complex multiply(Complex c,double d) {return new Complex(c.R*d,c.I*d);}
	public static Complex divide(Complex c1,Complex c2) {double m=c2.R*c2.R+c2.I*c2.I;return new Complex((c1.R*c2.R+c1.I*c2.I)/m,(c2.R*c1.I-c2.I*c1.R)/m);}
	public static Complex divide(Complex c,double d) {return new Complex(c.R/d,c.I/d);}
	public static Complex divide(double d,Complex c) {double m=c.R*c.R+c.I*c.I;return new Complex(c.R*d/m,-c.I*d/m);}

	public static Complex pow(Complex base,Complex power) {Complex l=base.log(); l.multiply(power); return l.exp();}
	public static Complex pow(Complex base,double power) {Complex l=base.log(); l.multiply(power); return l.exp();}

	public Complex sin() {return new Complex(Hyperbolic.cos(I)*Math.sin(R),Math.cos(R)*Hyperbolic.sin(I));}
	public Complex cos() {return new Complex(Hyperbolic.cos(I)*Math.cos(R),-Math.sin(R)*Hyperbolic.sin(I));}
	public Complex tan() {return new Complex(Math.sin(2*R)/(Math.cos(2*R) + Hyperbolic.cos(2*I)),Hyperbolic.sin(2*I)/(Math.cos(2*R) + Hyperbolic.cos(2*I)));}

	public Complex sinh() {Complex e=exp(); e.subtract(Complex.multiply(this,-1).exp()); e.divide(2); return e;}
	public Complex cosh() {Complex e=exp(); e.add(Complex.multiply(this,-1).exp()); e.divide(2); return e;}
	public Complex tanh() {Complex e=exp(),e2=new Complex(e),i=Complex.multiply(this,-1).exp(); e.subtract(i); e2.add(i); e.divide(e2); return e;}

	public Complex asinh() {Complex s=multiply(this,this); s.add(1); s=pow(s,.5); s.add(this); return s.log();}
	public Complex acosh() {Complex s=multiply(this,this); s.subtract(1); s=pow(s,.5); s.add(this); return s.log();}
	//The next three functions may not be reliable.  Check them at some point!
	public Complex atanh() {Complex p1=add(this,1); p1.divide(subtract(this,1)); p1=p1.log(); p1.divide(2); return p1;}

	public Complex asech() {Complex i=divide(1,this),i2=multiply(i,i); i2.subtract(1); i.add(i2.log()); return i.log();}
	public Complex acsch() {Complex i=divide(1,this),i2=multiply(i,i); i2.add(1); i.add(i2.log()); return i.log();}
	public Complex acoth() {Complex p=add(this,1),m=subtract(this,1); p.divide(m); p=p.log(); p.divide(2); return p;}

	public static Complex i() {return new Complex(0,1);}

	public String rectString() {
		StringBuffer sb=new StringBuffer(35);
		if(R!=0) {
			sb.append(R);
			if(I>0) sb.append('+');
		}
		if(I!=0) {
			sb.append(I);
			sb.append('i');
		}
		return sb.toString();
	}
	public String polarString() {
		StringBuffer sb=new StringBuffer(40);
		double t=mag();
		if(t!=1) sb.append(t);
		t=arg();
		if(t!=0) {
			sb.append("e^(");
			if(t!=1) sb.append(t);
			sb.append("i)");
		}
		return sb.toString();
	}
	public String toString() {return getClass().getName()+'['+R+','+I+']';}
}
