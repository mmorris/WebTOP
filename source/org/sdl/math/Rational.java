/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Rational.java
//Defines the class Rational, for use in representing simplified rational (real) numbers
//Davis Herring
//Created August 5 2002
//Updated February 15 2003
//Version 0.31 (CSDL v1.2.3)

package org.sdl.math;

public class Rational extends Number
{
	private int num,denom;

	// CREATION ROUTINES
	public Rational() {denom=1;}
	public Rational(int n) {num=n;denom=1;}
	public Rational(int n,int d) {setValue(n,d);}
	public Rational(Rational r) {num=r.num;denom=r.denom;}

	//Makes the closest Rational to the double given with a denominator <= maxD
	public static Rational fromDouble(double val,int maxD) {
		if(maxD<1) throw new IllegalArgumentException("Maximum denominator must be at least 1.");
		if(maxD==1) return new Rational((int)Math.rint(val));
		boolean neg;
		if(neg=val<0) val=-val;		//We'll pretend value is positive, then re-apply negativeness
		double minErr=Double.POSITIVE_INFINITY;
		int bestN=0,bestD=0;
		//The numbers below maxD/2 theoretically need not be checked; any 'hit' there
		//would be at least matched by the denominator twice as big.  However, the time
		//savings in using a small denominator outweigh this inefficiency.
		for(int curD=1;curD<=maxD;++curD) {
			double errL=Math.abs((val*curD)%1)/curD,errH=1d/curD-errL;
			if(errL==0) return new Rational((int)(val*curD),curD);
			if(errL<errH) {
				if(errL<minErr) {
					minErr=errL;
					bestD=curD;
					bestN=(int)(val*curD);
				}
			}
			else if(errH<minErr) {
				minErr=errH;
				bestD=curD;
				bestN=(int)(val*curD)+1;
			}
		}
		return new Rational(neg?-bestN:bestN,bestD);
	}

	// INFORMATION ROUTINES
	public int getNum() {return num;}
	public int getDenom() {return denom;}

	public boolean isInt() {return denom==1;}

	public double doubleValue() {return (double)num/denom;}
	public float floatValue() {return (float)num/denom;}
	public int intValue() {return denom==1?num:(int)Math.rint((double)num/denom);}
	public byte byteValue() {return (byte)intValue();}
	public short shortValue() {return (short)intValue();}
	public long longValue() {return denom==1?num:(long)Math.rint((double)num/denom);}

	// MODIFICATION ROUTINES
	//The static versions of these methods return a newly allocated
	//Rational without modifying their argument(s).

	public void setValue(int n,int d) {
		if(d==0) throw new IllegalArgumentException("Cannot divide by 0.");
		num=n;denom=d;
		simp();
	}
	public void setValue(Rational r) {num=r.num;denom=r.denom;}
	//Be warned that using these functions in sequence is unreliable: e.g.,
	//r.setDenom(5); r.setNum(0); r.setNum(3); results in r=3/1, not 3/5
	public void setNum(int n) {num=n;simp();}
	public void setDenom(int d) {
		if(d==0) throw new IllegalArgumentException("Cannot divide by 0.");
		denom=d;simp();
	}

	public void invert() {
		if(num==0) throw new IllegalStateException("Cannot invert 0.");
		int n0=num;
		num=n0<0?-denom:denom;denom=n0<0?-n0:n0;
	}
	public static Rational inverse(Rational r) {r=new Rational(r);r.invert();return r;}

	public void negate() {num=-num;}
	public static Rational negation(Rational r) {r=new Rational(r);r.negate();return r;}

	//This makes this Rational have a smaller denominator (at least as small as maxD).
	//Is there a better/more reliable way to implement this?
	public void roundDenom(int maxD) {
		if(maxD<denom) setValue(fromDouble(doubleValue(),maxD));
	}

	// ARITHMETIC ROUTINES
	public void add(Rational r) {
		if(denom!=r.denom) {
			if(r.num*denom%r.denom==0) num+=r.num*denom/r.denom;
			else if(num*r.denom%denom==0) {
				num*=r.denom/denom;
				num+=r.num;
				denom=r.denom;
			} else {
				num*=r.denom;
				num+=r.num*denom;
				denom*=r.denom;
			}
		} else num+=r.num;
		simp();
	}
	public void add(int i) {num+=i*denom;}
	public void subtract(Rational r) {
		if(denom!=r.denom) {
			if(r.num*denom%r.denom==0) num-=r.num*denom/r.denom;
			else if(num*r.denom%denom==0) {
				num*=r.denom/denom;
				num-=r.num;
				denom=r.denom;
			} else {
				num*=r.denom;
				num-=r.num*denom;
				denom*=r.denom;
			}
		} else num-=r.num;
		simp();
	}
	public void subtract(int i) {num-=i*denom;}
	public void multiply(Rational r) {num*=r.num; denom*=r.denom; simp();}
	public void multiply(int s) {
		if(denom%s==0) denom/=s;
		else {
			num*=s;
			simp();
		}
	}
	public void divide(Rational r) {
		if(r.num==0) throw new IllegalArgumentException("Cannot divide by 0.");
		if(r==this) {	//Special simple case (also, code below wouldn't work)
			num=denom=1;
		} else {
			num*=r.denom;
			denom*=r.num;
			simp();
		}
	}
	public void divide(int s) {
		if(num%s==0) num/=s;
		else {
			denom*=s;
			simp();
		}
	}

	public static Rational add(Rational r1,Rational r2) {r1=new Rational(r1);r1.add(r2);return r1;}
	public static Rational add(Rational r1,int i) {r1=new Rational(r1);r1.num+=i*r1.denom;return r1;}
	public static Rational subtract(Rational r1,Rational r2) {r1=new Rational(r1);r1.subtract(r2);return r1;}
	public static Rational subtract(Rational r1,int i) {r1=new Rational(r1);r1.num-=i*r1.denom;return r1;}
	public static Rational multiply(Rational r1,Rational r2) {return new Rational(r1.num*r2.num,r1.denom*r2.denom);}
	public static Rational multiply(Rational r1,int s) {r1=new Rational(r1);r1.multiply(s);return r1;}
	public static Rational divide(Rational r1,Rational r2) {return new Rational(r1.num*r2.denom,r1.num*r2.denom);}
	public static Rational divide(Rational r1,int s) {r1=new Rational(r1);r1.divide(s);return r1;}

	public static Rational pow(Rational r,int e) {
		Rational R=new Rational(1);
		while(e-->0) {
			R.num*=r.num;
			R.denom*=r.denom;
		}
		//e is always one less than 'real' value here; pre-increment to offset.
		while(++e<0) {
			R.num*=r.denom;
			R.denom*=r.num;
		}
		return R;
	}

	// COMPARISON ROUTINES
	public boolean equals(Object o) {
		if(o instanceof Rational) {
			Rational r=(Rational)o;
			return num==r.num&&denom==r.denom;
		}
		if(o instanceof Number) {
			if(denom==1) return num==((Number)o).intValue();
			return doubleValue()==((Number)o).doubleValue();
		}
		return false;
	}
	public boolean greaterThan(Rational r) {return num*r.denom>r.num*denom;}
	public boolean greaterThan(Number n) {return doubleValue()>n.doubleValue();}
	public boolean lessThan(Rational r) {return num*r.denom<r.num*denom;}
	public boolean lessThan(Number n) {return doubleValue()<n.doubleValue();}

	// OUTPUT ROUTINES
	public String asString() {
		if(denom==1) return String.valueOf(num);
		else return String.valueOf(num)+'/'+denom;
	}
	public String toString() {return getClass().getName()+'['+num+'/'+denom+']';}

	private void simp() {
		if(num==0) {
			denom=1;
			return;
		}
		if(denom<0) {
			num=-num;
			denom=-denom;
		}
		int n=num<0?-num:num,d=denom;
		while(n!=d) {
			while(n>d) n-=d;
			while(d>n) d-=n;
		}
		num/=n;
		denom/=n;
	}

	/* Fun and testing: rational approximations to pi, e
	public static void main(String[] args) {
		Rational pi0=null,e0=null;
		for(int i=1;i<Integer.MAX_VALUE;i++) {
			Rational pi=fromDouble(Math.PI,i),e=fromDouble(Math.E,i);
			if(!pi.equals(pi0)) {
				System.out.println(pi.asString()+": "+pi.doubleValue());
				pi0=pi;
			}
			if(!e.equals(e0)) {
				System.out.println(e.asString()+": "+e.doubleValue());
				e0=e;
			}
		}
	}*/
}
