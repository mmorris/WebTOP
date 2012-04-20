/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//WTMath.java
//Defines math routines of general interest to WebTOP
//Compiled by Davis Herring
//Created March 2 2002
//Updated July 8 2004
//Version 0.48

package org.webtop.util;

import java.util.Random;

public final class WTMath
{
	public static final Random random=new Random();

//=====================
// PRINCIPAL FUNCTIONS
//=====================

	//These are different from a%b in that -5%2.4 = -0.2 (remainder), but
	//fmod(-5,2.4) = 2.2.
	public static float mod(final float a,final float b)
	{return a-b*(float)Math.floor(a/b);}
	public static double mod(final double a,final double b)
	{return a-b*Math.floor(a/b);}

	public static float toRads(final float degs)
	{return degs*(float)Math.PI/180;}
	public static double toRads(final double degs)
	{return degs*Math.PI/180;}

	public static float toDegs(final float rads)
	{return rads*180/(float)Math.PI;}
	public static double toDegs(final double rads)
	{return rads*180/Math.PI;}

	public static float sinh(final float x)
	{return (float)(Math.exp(x) - Math.exp(-x)) / 2;}
	public static double sinh(final double x)
	{return (Math.exp(x) - Math.exp(-x)) / 2;}

	public static float cosh(final float x)
	{return (float)(Math.exp(x) + Math.exp(-x)) / 2;}
	public static double cosh(final double x)
	{return (Math.exp(x) + Math.exp(-x)) / 2;}

	public static float tanh(final float x)
	{return sinh(x) / cosh(x);}
	public static double tanh(final double x)
	{return sinh(x) / cosh(x);}

	public static int bound(final int x,final int min,final int max) {
		if(max<min)
			throw new IllegalArgumentException("invalid interval ["+max+','+min+']');
		return x>max?max:x<min?min:x;
	}
	public static float bound(final float x,final float min,final float max) {
		if(max<min)
			throw new IllegalArgumentException("invalid interval ["+max+','+min+']');
		return x>max?max:x<min?min:x;
	}
	public static double bound(final double x,
														 final double min,final double max) {
		if(max<min)
			throw new IllegalArgumentException("invalid interval ["+max+','+min+']');
		return x>max?max:x<min?min:x;
	}

//==================
// BESSEL FUNCTIONS
//==================

	/////////////////////////////////////////////////////////////////////////
	//Bessel functions copied from Numerical Recipes.  They probably need to
	//have a bit of code-cleanup.

	//Returns the Bessel function J0(x) for any real x.
	public static double j0(final double x) {
		final double ax,z;
		final double xx,y,ans,ans1,ans2;
		if((ax=Math.abs(x))<8) {
			y=x*x;
			ans1 = 57568490574d + y*(-13362590354d+y*(651619640.7
							 + y*(-11214424.18 + y*(77392.33017 + y*(-184.9052456)))));
			ans2 = 57568490411d + y*(1029532985d + y*(9494680.718
							 + y*(59272.64853 + y*(267.8532712 + y*1))));
			ans = ans1/ans2;
		} else {
			z = 8/ax;
			y = z*z;
			xx = ax-0.785398164;
			ans1 = 1 + y*(-0.1098628627e-2 + y*(0.2734510407e-4
							 + y*(-0.2073370639e-5 + y*0.2093887211e-6)));
			ans2 = -0.1562499995e-1 + y*(0.1430488765e-3
							 + y*(-0.6911147651e-5 + y*(0.7621095161e-6
							 - y*0.934935152e-7)));
			ans = Math.sqrt(0.636619772/ax)*(Math.cos(xx)*ans1-z*Math.sin(xx)*ans2);
		}
		return ans;
	}

	//Returns the Bessel function J1(x) for any real x.
	public static double j1(final double x) {
		double ax,z;
		double xx,y,ans,ans1,ans2;
		if((ax=Math.abs(x))<8) {
			y = x*x;
			ans1 = x*(72362614232d + y*(-7895059235d + y*(242396853.1
							 +y*(-2972611.439 + y*(15704.48260 + y*(-30.16036606))))));
			ans2 = 144725228442d+y*(2300535178d+y*(18583304.74
							 + y*(99447.43394 + y*(376.9991397 + y*1))));
			ans = ans1/ans2;
		} else {
			z = 8/ax;
			y = z*z;
			xx = ax-2.356194491;
			ans1 = 1 + y*(0.183105e-2 + y*(-0.3516396496e-4
							 + y*(0.2457520174e-5 + y*(-0.240337019e-6))));
			ans2 = 0.04687499995 + y*(-0.2002690873e-3
							 + y*(0.8449199096e-5 + y*(-0.88228987e-6
							 + y*0.105787412e-6)));
			ans = Math.sqrt(0.636619772/ax)*(Math.cos(xx)*ans1 - z*Math.sin(xx)*ans2);
			if(x<0) ans = -ans;
		}
		return ans;
	}

	//adapted by way of Leigh Brookshaw:
	static public double jn(final int n,final double x) {
		int j,m;
		double ax,bj,bjm,bjp,sum,tox,ans;
		boolean jsum;

		final double ACC	 = 40;
		final double BIGNO = 1e+10;
		final double BIGNI = 1e-10;

		if(n == 0) return j0(x);
		if(n == 1) return j1(x);
		if(n<0) return Math.pow(-1,-n)*jn(-n,x); //Changed to allow for funcions of negative order [Matt]
		//if(n<0) throw new IllegalArgumentException("Bessel functions of negative order not implemented.");

		if((ax=Math.abs(x)) == 0)	 return 0;
		else
			if(ax>n) {
				tox=2/ax;
				bjm=j0(ax);
				bj=j1(ax);
				for(j=1;j<n;j++) {
					bjp=j*tox*bj-bjm;
					bjm=bj;
					bj=bjp;
				}
				ans=bj;
			} else {
				tox=2/ax;
				m=2*((n+(int)Math.sqrt(ACC*n))/2);
				jsum=false;
				bjp=ans=sum=0;
				bj=1;
				for(j=m;j>0;j--) {
					bjm=j*tox*bj-bjp;
					bjp=bj;
					bj=bjm;
					if(Math.abs(bj)>BIGNO) {
						bj *= BIGNI;
						bjp *= BIGNI;
						ans *= BIGNI;
						sum *= BIGNI;
					}
					if(jsum) sum += bj;
					jsum=!jsum;
					if(j == n) ans=bjp;
				}
				sum=2*sum-bj;
				ans /= sum;
			}
		return	x<0 && n%2 == 1 ? -ans : ans;
	}

	//Returns the Bessel function Y0(x) for positive x.
	public static double y0(final double x) {
		double z;
		double xx,y,ans,ans1,ans2;
		if(x<8) {
			y = x*x;
			ans1 = -2957821389d + y*(7062834065d + y*(-512359803.6
							 + y*(10879881.29 + y*(-86327.92757 + y*228.4622733))));
			ans2 = 40076544269d + y*(745249964.8 + y*(7189466.438
							 + y*(47447.26470 + y*(226.1030244 + y*1))));
			ans = (ans1/ans2) + 0.636619772*j0(x)*Math.log(x);
		} else {
			z = 8/x;
			y = z*z;
			xx = x-0.785398164;
			ans1 = 1 + y*(-0.1098628627e-2 + y*(0.2734510407e-4
							 + y*(-0.2073370639e-5 + y*0.2093887211e-6)));
			ans2 = -0.1562499995e-1 + y*(0.1430488765e-3
							 + y*(-0.6911147651e-5 + y*(0.7621095161e-6
							 + y*(-0.934945152e-7))));
			ans = Math.sqrt(0.636619772/x)*(Math.sin(xx)*ans1 + z*Math.cos(xx)*ans2);
		}
		return ans;
	}

	//Returns the Bessel function Y1(x) for positive x.
	public static double y1(final double x) {
		double z;
		double xx,y,ans,ans1,ans2;
		if(x<8) {
			y = x*x;
			ans1 = x*(-0.4900604943e13 + y*(0.1275274390e13
							 + y*(-0.5153438139e11 + y*(0.7349264551e9
							 + y*(-0.4237922726e7 + y*0.8511937935e4)))));
			ans2 = 0.2499580570e14 + y*(0.4244419664e12
							 + y*(0.3733650367e10 + y*(0.2245904002e8
							 + y*(0.1020426050e6 + y*(0.3549632885e3 + y)))));
			ans = (ans1/ans2) + 0.636619772*(j1(x)*Math.log(x) - 1/x);
		} else {
			z = 8/x;
			y = z*z;
			xx = x-2.356194491;
			ans1 = 1 + y*(0.183105e-2 + y*(-0.3516396496e-4
							 + y*(0.2457520174e-5 + y*(-0.240337019e-6))));
			ans2 = 0.04687499995 + y*(-0.2002690873e-3
							 + y*(0.8449199096e-5 + y*(-0.88228987e-6
							 + y*0.105787412e-6)));
			ans = Math.sqrt(0.636619772/x)*(Math.sin(xx)*ans1 + z*Math.cos(xx)*ans2);
		}
		return ans;
	}

	//Returns the Bessel function I0(x) for any real x
	public static double i0(final double x) {
		double ax,ans,y;
		if((ax=Math.abs(x))<3.75) {
			y=x/3.75;
			y*=y;
			ans=1.0+y*(3.5156229+y*(3.0899424+y*(1.2067492+y*(0.2659732+y*(0.360768e-1+y*0.45813e-2)))));
		} else {
			y=3.75/ax;
			ans=(Math.exp(ax)/Math.sqrt(ax))*(0.39894228+y*(0.1328592e-1+y*(0.225319e-2+y*(-0.157565e-2+y*(0.916281e-2+y*(-0.2057706e-1+y*(0.2635537e-1+y*(-0.1647633e-1+y*0.392377e-2))))))));
		}
		return ans;
	}

	//Returns the Bessel function I1(x) for any real x
	public static double i1(final double x) {
		double ax,ans,y;
		if((ax=Math.abs(x))< 3.75) {
			y=x/3.75;
			y*=y;
			ans=ax*(0.5+y*(0.87890594+y*(0.51498869+y*(0.15084934+y*(0.2658733e-1+y*(0.301532e-2+y*0.32411e-3))))));
		} else {
			y=3.75/ax;
			ans=0.2282967e-1+y*(-0.2895312e-1+y*(0.1787654e-1-y*0.420059e-2));
			ans=0.39894228+y*(-.3988024e-2+y*(-0.362018e-2+y*(0.163801e-2+y*(-0.1031555e-1+y*ans))));
		}
		return x < 0.0 ? -ans : ans;
	}

	//Returns the Bessel function K0(x) for positive x
	public static double k0(final double x) {
		double y,ans;
		if(x <= 2.0) {
			y=x*x/4.0;
			ans=(-1.0*Math.log(x/2.0)*i0(x))+(-0.57721566+y*(0.42278420+y*(0.23069765+y*(0.3488590e-1+y*(0.262698e-2+y*(0.10750e-3+y*0.74e-5))))));
		} else {
			y=2.0/x;
			ans=(Math.exp(-x)/Math.sqrt(x))*(1.25331414+y*(-0.7832358e-1+y*(0.2189568e-1+y*(-0.1062446e-1+y*(0.587872e-2+y*(-0.251540e-2+y*0.53208e-3))))));
		}
		return ans;
	}

	//Returns the Bessel function K1(x) for positive x
	public static double k1(final double x) {
		double y,ans;
		if(x <= 2.0) {
			y=x*x/4.0;
			ans=(Math.log(x/2.0)*i1(x))+(1.0/x)*(1.0+y*(0.15443144+y*(-.67278579+y*(-0.18156897+y*(-0.1919402e-1+y*(-0.110404e-2+y*(-0.4686e-4)))))));
		} else {
			y=2.0/x;
			ans=(Math.exp(-x)/Math.sqrt(x))*(1.25331414+y*(0.23498619+y*(-0.3655620e-1+y*(0.1504268e-1+y*(-0.780353e-2+y*(0.325614e-2+y*(-0.68245e-3)))))));
		}
		return ans;
	}

	//Returns the Bessel function Kn(x) for positive x
	public static double kn(final int n,final double x) {
		int j;
		double bk,bkm,bkp,tox;
		tox=2.0/x;
		bkm=k0(x);
		bk=k1(x);
		if(n == 0) return k0(x);
		if(n == 1) return k1(x);
		if(n<0) throw new IllegalArgumentException("Bessel functions of negative order not implemented.");
		for(j=1; j<n; j++) {
			bkp=bkm+j*tox*bk;
			bkm=bk;
			bk=bkp;
		}
		return bk;
	}

//=================
// COLOR FUNCTIONS
//=================

	//This is the color function from the Circular Module. Eventually it should
	//get a home in some other class (possibly its own); also, any modules using
	//this color model should refer to this copy, and any using another color
	//model should either change to use this one or have their model moved to a
	//similar global location. [Davis]

	//empirical constants for hue()
	private static final float BLUE_WAVE	=410; //some use 400
	private static final float RED_WAVE		=625; //some use 630
	private static final float HUE_SCALE	=240;
	public static final float SATURATION =  1;	//we always use saturated colors

	private static final int RED=0,GREEN=1,BLUE=2;

	public static float hue(final float wavelength) {
		final float hue=(wavelength - BLUE_WAVE) / (RED_WAVE - BLUE_WAVE);
		return HUE_SCALE*(1-bound(hue,0,1));
	}

	//Fills the first three elements of the array rgb (which must be non-null
	//and have length >= 3) with the red, green, and blue components of the
	//color whose hue is h, whose luminosity is l, and whose saturation is s.
	//All values are on [0,1] except for h, which is on [0,360].
	public static void hls2rgb(final float[] rgb,
														 final float h,final float l,final float s) {
		final float m2=(l<=0.5f) ? l*(1+s) : l+s-l*s,m1=2*l-m2;

		if(s==0)										// special case for efficiency
			rgb[RED]=rgb[GREEN]=rgb[BLUE]=l;
		else {
			rgb[RED]=component(m1,m2,h+120);
			rgb[GREEN]=component(m1,m2,h);
			rgb[BLUE]=component(m1,m2,h-120);
		}

		//Clamp color values
		if(rgb[RED]>1) rgb[RED]=1;
		if(rgb[GREEN]>1) rgb[GREEN]=1;
		if(rgb[BLUE]>1) rgb[BLUE]=1;
	}

	//Helper function for HLS
	private static float component(final float n1,final float n2,float h) {
		while(h<0) h+=360;
		while(h>360) h-=360;

		if(h<60) return n1+(n2-n1)*h/60;
		else if(h<180) return n2;
		else if(h<240) return n1+(n2-n1)*(240-h)/60;
		else return n1;
	}

	//Stores into an array RGB values (on [0,1]) for a given hue, saturation,
	//and value; VERY similar to java.awt.Color's HSB -> RGB color
	//functions... thus, is it needed?
	//Apparently, only NSlit uses this at the moment.
	public static void hsv2rgb(final float[] rgb,
														 float hue,final float sat,final float val) {
		if(sat==0) {
			rgb[RED]=rgb[GREEN]=rgb[BLUE]=val;
		} else {
			hue=mod(hue,360)/60;
			final int hue_range=(int)Math.floor(hue);	//integer part of hue/60
			final float hue_offset=hue-hue_range;	    //fractional part
			final float f4=val*(1-sat),
									f5=val*(1-sat*hue_offset),
									f6=val*(1-sat*(1-hue_offset));
			switch (hue_range) {
			case 0:
				rgb[RED] = val;
				rgb[GREEN] = f6;
				rgb[BLUE] = f4;
				break;
			case 1:
				rgb[RED] = f5;
				rgb[GREEN] = val;
				rgb[BLUE] = f4;
				break;
			case 2:
				rgb[RED] = f4;
				rgb[GREEN] = val;
				rgb[BLUE] = f6;
				break;
			case 3:
				rgb[RED] = f4;
				rgb[GREEN] = f5;
				rgb[BLUE] = val;
				break;
			case 4:
				rgb[RED] = f6;
				rgb[GREEN] = f4;
				rgb[BLUE] = val;
				break;
			case 5:
				rgb[RED] = val;
				rgb[GREEN] = f4;
				rgb[BLUE] = f5;
				break;
			}
		}
	}

	private WTMath() {}
}
