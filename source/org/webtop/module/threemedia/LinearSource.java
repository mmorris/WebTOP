/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.threemedia;

import org.sdl.math.Complex;
import org.sdl.math.FPRound;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFRotation;
import org.web3d.x3d.sai.SFVec3f;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.webtop.component.StatusBar;
import org.webtop.util.WTMath;

public class LinearSource extends WaveSource implements X3DFieldEventListener {
	
	//Lots and Lots of math variables here
	public static final float TWOPI = (float)(2 * Math.PI);
	private int situation;

	//Parametric nonsense.  Clean.  [Davis]
	private float amplitudeIncident;
	private float n1, n2, n3;
	private float lambda1, lambda2, lambda3;
	private float theta1, theta2, theta3;
	private float critical1, critical2, actualCritical2;
	private float d;
	private float f, h, delta, f3, h3, h4;
	private float magGPlus, magG2Plus, magG3Plus, magG4Plus, magGMinus, magG2Minus, magG3Minus, magG4Minus,
					gammaPlus, gammaMinus, gamma2Plus, gamma2Minus, gamma3Plus, gamma3Minus, gamma4Plus, gamma4Minus;
	private float magT, magT2, magT3, magT4, tau, tau2, tau3, tau4;
	private float magR, magR2, magR3, magR4, rho, rho2, rho3, rho4;
	private float k1, k2, k3, v1, v2, v3;
	private float s = 15.0f;
	private float Lprime, Lnotprime;
	private float alpha2, beta2;
	private float psi4, nu4;

	// for situation 4
	private float a, b, c, magG5, gamma5, magT5, tau5, magR5, rho5;

	// for situation 5
	private Complex b6, n6, p6;
	private float magG6, gamma6, magR6, rho6, magT6, tau6;

	// for situation 6
	private float magT7, tau7, magR7, rho7, magG7Plus, gamma7Plus, magG7Minus, gamma7Minus;

	// for situation 7
	private float f8, h8, magT8, tau8, magR8, rho8, magG8Plus, gamma8Plus, magG8Minus, gamma8Minus;

	//Complicated -- er, complex -- nonsense follows.  Clean.  [Davis]
	private Complex D;
	private Complex GPlus, GMinus, G2Plus, G2Minus, G3Plus, G3Minus, G4Plus, G4Minus, G7Plus, G7Minus, G8Plus, G8Minus;
	private Complex T, T2, T3, T4, T6, T7, T8;
	private Complex R, R2, R3, R4, R5, R6, R7, R8;
	private Complex dummy, anotherDummy;
	private Complex h2;
	private Complex psi2;
	private Complex alpha3;
	private Complex beta3;
	private Complex psi3;
	private Complex nu3;
	private Complex m, m6;

	private SFFloat set_angle;
	private boolean wasPlaying,dragging;
	private boolean internalUpdate = false;
	private SFFloat draggerAmp;
	private SFRotation IncidentVectorRotation;
	private SFRotation ReflectedVectorRotation1;
	private SFRotation RefractedVectorRotation1;
	private SFRotation IncidentVectorRotation2;
	private SFRotation ReflectedVectorRotation2;
	private SFRotation RefractedVectorRotation2;
	private SFVec3f IncidentTranslation;
	private SFVec3f ReflectedTranslation;
	private SFVec3f RefractedTranslation;
	//private EventOutSFBool draggerOver;

	private float[] rotation;
	//private float kIncident;
	
	private SFFloat get_amplitude_changed;
	private SFFloat get_wavelength_changed;
	private SFFloat get_angle_changed;
	private SFBool get_mouse_clicked;
	private SFBool get_mouse_over;
	private SFBool get_mouseOverAmplitude;
	private SFBool get_mouseOverWavelength;
	private SFBool get_mouseOverAngle;
	
	public LinearSource(Engine e, SourcePanel sourcePanel, StatusBar bar) {
		super(e, sourcePanel, bar);
		theta1 = 0.0f;
		createX3DNode();
	}
	
	//Even more complicated Constructor
	public LinearSource(Engine e, SourcePanel sourcePanel, StatusBar bar, float A, float L, float E, float x, float y, float Th, float na, float nb, float nc, float dist) {
		super(e, sourcePanel, bar, A, L, E, x, y);
		amplitudeIncident = A;
		theta1 = Th;
		lambda1 = L;
		d = dist;
		n1 = na;
		n2 = nb;
		n3 = nc;

		situation = 0;
		draggerAmp = (SFFloat) sai.getInputField("BoundaryDragger","set_amplitude");
		IncidentVectorRotation = (SFRotation) sai.getInputField("Incident","set_rotation");
		ReflectedVectorRotation1 = (SFRotation) sai.getInputField("Reflected1","set_rotation");
		RefractedVectorRotation1 = (SFRotation) sai.getInputField("Refracted1","set_rotation");
		IncidentVectorRotation2 = (SFRotation) sai.getInputField("Incident2","set_rotation");
		ReflectedVectorRotation2 = (SFRotation) sai.getInputField("Reflected2","set_rotation");
		RefractedVectorRotation2 = (SFRotation) sai.getInputField("Refracted2","set_rotation");
		IncidentTranslation = (SFVec3f) sai.getInputField("Incident2","set_center");
		ReflectedTranslation = (SFVec3f) sai.getInputField("Reflected2","set_center");
		RefractedTranslation = (SFVec3f) sai.getInputField("Refracted2","set_center");
		sai.getOutputField("BoundaryDragger","amplitude_changed",this, "AMP_CHANGED");
		sai.getOutputField("BoundaryDragger","isOver",this, "OVER_DRAGGER");

		draggerAmp.setValue((dist-15.0f)/20.0f);

		createX3DNode();

		//System.out.println(theta2);

		rotation = new float[] {0,1,0,theta1};

		IncidentVectorRotation.setValue(rotation);

		rotation[3] = -theta1;
		ReflectedVectorRotation1.setValue(rotation);


		rotation[3] = (float) (Math.PI + theta2);
		RefractedVectorRotation1.setValue(rotation);

		rotation[3] = theta2;
		IncidentVectorRotation2.setValue(rotation);

		rotation[3] = -theta2;
		ReflectedVectorRotation2.setValue(rotation);

		rotation[3] = (float) (Math.PI + theta3);
		RefractedVectorRotation2.setValue(rotation);

		float center[] = new float[3];
		center[0] = dist - 15;
		center[1] = 10;
		center[2] = 0;
		IncidentTranslation.setValue(center);
		ReflectedTranslation.setValue(center);
		RefractedTranslation.setValue(center);

		updateParameters();
	}
	
	public int getSituation() {
		//Figure out which of the 4 cases we have to draw, either (h,h), (h,e), (e,h), (e,e), (h,h)->(e,h), (h,e)->(e,e), (h,h)->(h,e), (e,h)->(e,e)
		if(n1 <= n2) {
			if(n2 <= n3)
				situation = 0;
			else if(n1 <= n3)
				situation = 0;
			else if(theta1 < critical2)
			//else if(theta1 < critical2)
				situation = 0;
			else if(theta1 == critical2)
				situation = 6;
			else
				situation = 1;
		} else {
			if(n2 <= n3) {
				if(n1 >= n3) {
					if(theta1 < critical1) situation = 0;
					//if(theta1 < critical1) situation = 0;
					else if(theta1 == critical1) situation = 4;
					else if(theta1 == critical2) situation = 7;
					else if(critical1 < theta1 && theta1 < critical2)
					//else if(critical1 <= theta1 && theta1 < critical2)
						situation = 2;
					else
						situation = 3;
				} else {
					if(theta1 < critical1)
					//if(theta1 < critical1)
						situation = 0;
					else if(theta1 == critical1) situation = 4;
					else
						situation = 2;
				}
			} else {
				if(theta1 < critical2)
				//if(theta1 < critical2)
					situation = 0;
				else if(theta1 == critical2)
					situation = 6;
				else if(critical2 < theta1 && theta1 < critical1)
				//else if(critical2 <= theta1 && theta1 < critical1)
					situation = 1;
				else if(theta1 == critical1)
					situation = 5;
				else {
					situation = 3;
				}
			}

			//System.out.println("Situation is " + situation);
		}

		return situation;
	}
	
//	Aiee!	 Broken inheritance!	[Davis]
	public float getValue(float x, float y, float t) {
		return 0;
	}

	//No need to do bounds-checking here; the FloatBoxen do that.
	public void setN1(float n) {
		lambda1=n1*lambda1/n;
		n1 = n;
		updateParameters();
	}

	public void setN2(float n) {
		n2 = n;
		updateParameters();
	}

	public void setN3(float n) {
		n3 = n;
		updateParameters();
	}

	public void setDistance(float distance) {
		float center[] = new float[3];
		center[0] = distance - 15;
		center[1] = 10;
		center[2] = 0;
		IncidentTranslation.setValue(center);
		ReflectedTranslation.setValue(center);
		RefractedTranslation.setValue(center);
		d = distance;
		updateParameters();
		if(!internalUpdate)  //BJT test
			draggerAmp.setValue((d-15)/20);
	}

	public void setWavelength(float lambda) {
		lambda1 = lambda/n1;
		updateParameters();
		if(!internalUpdate)
			set_wavelength.setValue(lambda/n1);
	}

	public void setAmplitude(float amp) {
		amplitudeIncident = amp;
		updateParameters();
		if(!internalUpdate)
			set_amplitude.setValue(amp);
	}

	public void setAngle(float ang) {
		theta1 = ang;
		updateParameters();
		if(!internalUpdate)
			set_angle.setValue(ang);
	}

	public float getN1() {
		return n1;
	}

	public float getN2() {
		return n2;
	}

	public float getN3() {
		return n3;
	}

	public float getDistance() {
		return d;
	}

	public float getWavelength() {
		return lambda1;
	}

	public float getAmplitude() {
		return amplitudeIncident;
	}

	public float getAngle() {
		return theta1;
	}

	public void updateParameters() {
		lambda2 = n1 * lambda1 / n2;
		lambda3 = n1 * lambda1 / n3;
		theta2 = (float) Math.asin(n1 * Math.sin(theta1) / n2);
		theta3 = (float) Math.asin(n1 * Math.sin(theta1) / n3);
		critical1 = (float) Math.asin(n2 / n1);
		critical2 = (float) Math.asin(n3 / n1);
		actualCritical2 = (float) Math.asin(n3 / n2);
		f = (float) ((n2 * Math.cos(theta2)) / (n1 * Math.cos(theta1)));
		h = (float) ((n2 * Math.cos(theta2)) / (n3 * Math.cos(theta3)));
		delta = (float) ((TWOPI / lambda2) * d * Math.cos(theta2));
		alpha2 = 1 + f;
		beta2 = 1 - f;
		dummy = new Complex(0, -delta);
		anotherDummy = new Complex(0, delta);
		dummy = dummy.exp();
		anotherDummy = anotherDummy.exp();
		h2 = Complex.multiply(Complex.i(), dummy);
		h2.multiply(n2 * Math.cos(theta2));
		h2.divide(n1 * Math.sqrt(Math.pow(Math.sin(theta1), 2) - Math.pow(Math.sin(critical2), 2)));
		psi2 = Complex.subtract(dummy, h2);
		G2Plus = Complex.multiply(psi2, 2);
		G2Plus.divide(Complex.subtract(Complex.multiply(psi2, alpha2), Complex.multiply(psi2.conjugate(), beta2)));
		G2Minus = Complex.multiply(Complex.divide(psi2.conjugate(), psi2), -1);
		G2Minus.multiply(G2Plus);
		T2 = Complex.add(Complex.multiply(anotherDummy, G2Plus), Complex.multiply(dummy, G2Minus));
		R2 = Complex.add(G2Plus, G2Minus);
		R2.subtract(1);
		magG2Plus = (float) G2Plus.mag();
		gamma2Plus = (float) G2Plus.arg();
		magG2Minus = (float) G2Minus.mag();
		gamma2Minus = (float) G2Minus.arg();
		magT2 = (float) T2.mag();
		tau2 = (float) T2.arg();
		magR2 = (float) R2.mag();
		rho2 = (float) R2.arg();
		D = Complex.subtract(Complex.multiply(dummy, (1+f) * (1+h)), Complex.multiply(anotherDummy, (1 - f) * (1 - h)));
		GPlus = Complex.multiply(dummy, Complex.divide(2 * (1 + h), D));
		GMinus = Complex.multiply(anotherDummy, Complex.divide(-2 * (1 - h), D));
		T = Complex.divide(4 * h, D);
		R = Complex.add(Complex.multiply(GPlus, 0.5 * (1 - f)), Complex.multiply(GMinus, 0.5 * (1 + f)));
		magGPlus = (float) GPlus.mag();
		gammaPlus = (float) GPlus.arg();
		magGMinus = (float) GMinus.mag();
		gammaMinus = (float) GMinus.arg();
		magT = (float) T.mag();
		tau = (float) T.arg();
		magR = (float) R.mag();
		rho = (float) R.arg();
		k1 = TWOPI / lambda1;
		k2 = TWOPI / lambda2;
		k3 = TWOPI / lambda3;
		v1 = speed / n1;
		v2 = n1 * v1 / n2;
		v3 = n1 * v1 / n3;
		Lprime = (float) (lambda1 / (TWOPI * Math.sqrt(Math.pow(Math.sin(theta1), 2) - Math.pow(Math.sin(critical2), 2))));
		Lnotprime = (float) (lambda1 / (TWOPI * Math.sqrt(Math.pow(Math.sin(theta1), 2) - Math.pow(Math.sin(critical1), 2))));
		f3 = (float) (Math.sqrt(Math.pow(Math.sin(theta1), 2) - Math.pow(Math.sin(critical1), 2)) / Math.cos(theta1));
		h3 = (float) ((n1 * Math.cos(theta1)) / (n3 * Math.cos(theta3)) * f3);
		alpha3 = Complex.add(new Complex(0, f3), 1);
		beta3 = Complex.multiply(alpha3.conjugate(), Math.exp(-d / Lnotprime));
		psi3 = Complex.add(new Complex(0, h3), 1);
		nu3 = Complex.multiply(psi3.conjugate(), Math.exp(-d / Lnotprime));
		G3Plus = Complex.multiply(psi3, 2);
		G3Plus.divide(Complex.subtract(Complex.multiply(alpha3, psi3), Complex.multiply(beta3, nu3)));
		G3Minus = Complex.divide(Complex.multiply(nu3, -1), psi3);
		G3Minus.multiply(G3Plus);
		T3 = Complex.add(Complex.multiply(G3Plus, Math.exp(-d / Lnotprime)), G3Minus);
		R3 = Complex.add(G3Plus, Complex.multiply(G3Minus, Math.exp(-d / Lnotprime)));
		R3.subtract(1);
		magG3Plus = (float) G3Plus.mag();
		gamma3Plus = (float) G3Plus.arg();
		magG3Minus = (float) G3Minus.mag();
		gamma3Minus = (float) G3Minus.arg();
		magT3 = (float) T3.mag();
		tau3 = (float) T3.arg();
		magR3 = (float) R3.mag();
		rho3 = (float) R3.arg();
		h4 = Lprime / Lnotprime;  // Was Lnotprime / Lprime. Fixed by Dr. F and mjm on 8/2/2003.
		psi4 = 1 + h4;
		nu4 = (float) ((1 - h4) * Math.exp(-d / Lnotprime));
		G4Plus = Complex.divide(2 * psi4, Complex.subtract(Complex.multiply(alpha3, psi4), Complex.multiply(beta3, nu4)));
		G4Minus = Complex.multiply(G4Plus, -1 * nu4 / psi4);
		T4 = Complex.add(Complex.multiply(G4Plus, Math.exp(-d / Lnotprime)), G4Minus);
		R4 = Complex.add(G4Plus, Complex.multiply(G4Minus, Math.exp(-d / Lnotprime)));
		R4.subtract(1);
		magG4Plus = (float) G4Plus.mag();
		gamma4Plus = (float) G4Plus.arg();
		magG4Minus = (float) G4Minus.mag();
		gamma4Minus = (float) G4Minus.arg();
		magT4 = (float) T4.mag();
		tau4 = (float) T4.arg();
		magR4 = (float) R4.mag();
		rho4 = (float) R4.arg();

		Complex i = Complex.i();

		// for situation 4
		a = (float) (n2 / ( n1 * Math.cos(theta1)));
		b = (float) (n2 /(n3*Math.cos(theta3)));
		c = (float) (2*Math.PI*d) / (lambda2);

		Complex T5;
		m = Complex.divide(1,Complex.add(new Complex(0,-c),(a+b)));
		R5 = Complex.divide(Complex.add(new Complex(0,-c),(a-b)),Complex.add(new Complex(0,-c),(b+a)));
		T5 = Complex.divide(2*b,(Complex.add(new Complex(0,-c),(b+a))));
		magT5 = (float) T5.mag();
		tau5 = (float) T5.arg();
		magR5 = (float) R5.mag();
		rho5 = (float) R5.arg();


		// for situation 5
		b6 = Complex.divide(Complex.multiply(Complex.multiply(i,n2),Complex.multiply(i,-1*c).exp()),n1*Math.sqrt(Math.sin(critical1)*Math.sin(critical1)-Math.sin(critical2)*Math.sin(critical2)));
		R6 = Complex.divide(Complex.add(Complex.multiply(i,b6.I+c),a),Complex.subtract(Complex.multiply(i,b6.I+c),a));
		T6 = Complex.divide(Complex.multiply(Complex.multiply(i,b6.I),2),Complex.subtract(Complex.multiply(i,b6.I+c),a));
		m6 = Complex.divide(1,Complex.multiply(Complex.subtract(Complex.multiply(i,b6.I+c),a),-1));
		n6 = Complex.add(Complex.multiply(Complex.multiply(Complex.add(b6,Complex.multiply(i,c)),-1),m6),Complex.divide(Complex.add(Complex.add(b6,a*b6.R),Complex.multiply(i,c)),Complex.multiply(m6,m6)));
		p6 = Complex.subtract(Complex.multiply(Complex.subtract(b6.conjugate(),Complex.multiply(i,c)),m6),Complex.divide(Complex.add(Complex.add(b6,a*b6.R),Complex.multiply(i,c)),Complex.multiply(m6,m6)));
		magT6 = (float) T6.mag();
		tau6 = (float) T6.arg();
		magR6 = (float) R6.mag();
		rho6 = (float) R6.arg();

		// for situation 6
		T7 = Complex.divide(2,Complex.add(Complex.multiply(i,-1*f*Math.sin(delta)),Math.cos(delta)));
		G7Plus = Complex.divide(2,Complex.add(Complex.multiply(Complex.multiply(i,2*delta).exp(),1-f),1+f));
		G7Minus = Complex.divide(2,Complex.add(Complex.multiply(Complex.multiply(i,-2*delta).exp(),1+f),1-f));
		R7 = Complex.multiply(Complex.add(Complex.multiply(G7Plus,1-f),Complex.multiply(G7Minus,1+f)),.5);
		magT7 = (float) T7.mag();
		tau7 = (float) T7.arg();
		magR7 = (float) R7.mag();
		rho7 = (float) R7.arg();
		magG7Plus = (float) G7Plus.mag();
		gamma7Plus = (float) G7Plus.arg();
		magG7Minus = (float) G7Minus.mag();
		gamma7Minus = (float) G7Minus.arg();

		// for situation 7
		f8 = (float) (Math.sqrt(Math.pow(Math.sin(theta1),2)-Math.pow(Math.sin(critical1),2))/Math.cos(theta1));
		h8 = (float) ((n1*Math.cos(theta1)*f8)/(n3*Math.cos(theta3)));
		G8Plus = Complex.divide(1,Complex.subtract(Complex.add(Complex.multiply(i,f8),1),Complex.multiply(Complex.add(Complex.multiply(i,-f8),1),Math.exp(-2*d/Lnotprime))));
		G8Minus = Complex.multiply(G8Plus,Math.exp(-2*d/Lnotprime));
		T8 = Complex.add(Complex.multiply(G8Plus,Math.exp(-1*d/Lnotprime)),G8Minus);
		R8 = Complex.add(Complex.add(G8Plus,Complex.multiply(G8Minus,Math.exp(-1*d/Lnotprime))),-1);
		magT8 = (float) T8.mag();
		tau8 = (float) T8.arg();
		magR8 = (float) R8.mag();
		rho8 = (float) R8.arg();
		magG8Plus = (float) G8Plus.mag();
		gamma8Plus = (float) G8Plus.arg();
		magG8Minus = (float) G8Minus.mag();
		gamma8Minus = (float) G8Minus.arg();


		/*System.out.println("lambda2: " + lambda2);
		System.out.println("f: " + f);
		System.out.println("theta2: " + theta2);
		System.out.println("delta: " + delta);
		System.out.println("magR7: " + magR7);
		System.out.println("rho7: " + rho7);
		System.out.println("magG7Plus: " + magG7Plus + " phase: " + gamma7Plus);
		System.out.println("magG7Minus: " + magG7Minus + " phase: " + gamma7Minus);
		System.out.println("magT7: " + magT7 + " tau: " + tau7);
		System.out.println("ReT7: " + T7.R + " ImT7: " + T7.I);
		*/


		/*
		System.out.println("lambda1: " + lambda1 + "\n" +
											 "lambda2: " + lambda2 + "\n" +
											 "lambda3: " + lambda3 + "\n" +
											 "theta2:  " + theta2 + "\n" +
											 "theta3:  " + theta3 + "\n" +
											 "critical1: " + critical1 + "\n" +
											 "critical2: " + critical2 + "\n" +
											 "actualCritical2: " + actualCritical2 + "\n" +
											 "f: " + f + "\n" +
											 "h: " + h + "\n" +
											 "delta: " + delta + "\n" +
											 "alpha2: " + alpha2 + "\n" +
											 "beta2: " + beta2 + "\n" +
											 "h2: " + h2 + "\n" +
											 "psi2: " +psi2 + "\n" +
											 "T2: " + T2 + "\n" +
											 "R2: "  +R2  + "\n" +
											 "magG2Plus: " + magG2Plus + "\n" +
											 "gamma2Plus: " + gamma2Plus + "\n" +
											 "magG2Minus: "  + magG2Minus + "\n" +
											 "gamma2Minus: " + gamma2Minus  + "\n" +
											 "magT2: " + magT2 + "\n" +
											 "tau2: " + tau2 + "\n" +
											 "magR2: " + magR2 + "\n" +
											 "rho2: " + rho2  + "\n" +
											 "D: " + D  + "\n" +
											 "GPlus: " + GPlus + "\n" +
											 "GMinus: " + GMinus + "\n" +
											 "T: " + T  + "\n" +
											 "R: " + R  + "\n" +
											 "magGPlus: " +magGPlus + "\n" +
											 "gammaPlus: " + gammaPlus + "\n" +
											 "magGMinus: " + magGMinus + "\n" +
											 "gammaMinus: " + gammaMinus + "\n" +
											 "magT: " + magT + "\n" +
											 "tau: " + tau + "\n" +
											 "magR: " + magR + "\n" +
											 "rho: "  +rho  + "\n" +
											 "k1: "  +k1  + "\n" +
											 "k2: "  +k2  + "\n" +
											 "k3: "  +k3  + "\n" +
											 "v1: "  +v1  + "\n" +
											 "v2: "  +v2  + "\n" +
											 "v3: "  +v3  + "\n" +
											 "Lprime: "  +Lprime  + "\n" +
											 "Lnotprime: "  +Lnotprime  + "\n" +
											 "f3: "  +f3  + "\n" +
											 "h3: "  +h3  + "\n" +
											 "alpha3: " + alpha3  + "\n" +
											 "beta3: " + beta3  + "\n" +
											 "psi3: " + psi3  + "\n" +
											 "nu3: " + nu3  + "\n" +
											 "G3Plus: " + G3Plus  + "\n" +
											 "G3Minus: " + G3Minus  + "\n" +
											 "T3: " + T3  + "\n" +
											 "R3: " + R3  + "\n" +
											 "magG3Plus: " + magG3Plus  + "\n" +
											 "gamma3Plus: " + gamma3Plus  + "\n" +
											 "magG3Minus: " + magG3Minus  + "\n" +
											 "gamma3Minus: " + gamma3Minus  + "\n" +
											 "magT3: " + magT3  + "\n" +
											 "tau3: " + tau3  + "\n" +
											 "magR3: " + magR3  + "\n" +
											 "rho3: " + rho3  + "\n" +
											 "T: " + T  + "\n" +
											 "T: " + T  + "\n" +
											 "h4: " + h4  + "\n" +
											 "psi4: " + psi4  + "\n" +
											 "nu4: " + nu4  + "\n" +
											 "G4Plus: " + G4Plus  + "\n" +
											 "G4Minus: " + G4Minus  + "\n" +
											 "T4: " + T4  + "\n" +
											 "R4: " + R4  + "\n" +
											 "magG4Plus: " + magG4Plus  + "\n" +
											 "gamma4Plus: " + gamma4Plus  + "\n" +
											 "magG4Minus: " + magG4Minus  + "\n" +
											 "gamma4Minus: " + gamma4Minus  + "\n" +
											 "magT4: " + magT4  + "\n" +
											 "tau4: " + tau4  + "\n" +
											 "magR4: " + magR4  + "\n" +
											 "rho4: " + rho4  + "\n");
		*/

		if(isTIRAtFirstBoundary())
			sourcePanel.setFirstAngleOfRefraction("None");
		else
			sourcePanel.setFirstAngleOfRefraction(String.valueOf(FPRound.toFixVal(WTMath.toDegs((double) theta2), 2)));

		if(n1 > n2)
				sourcePanel.setFirstCriticalAngle(String.valueOf(FPRound.toFixVal(WTMath.toDegs((double) critical1), 2)));
		else
				sourcePanel.setFirstCriticalAngle("None");

		if(isTIRAtSecondBoundary())
			sourcePanel.setSecondAngleOfRefraction("None");
		else {
			sourcePanel.setSecondAngleOfRefraction(String.valueOf(FPRound.toFixVal(WTMath.toDegs((double) theta3), 2)));
		}

		if(n2 > n3)
			sourcePanel.setSecondCriticalAngle(String.valueOf(FPRound.toFixVal(WTMath.toDegs((double) actualCritical2), 2)));
		else
			sourcePanel.setSecondCriticalAngle("None");


		rotation[3] = theta1;

		IncidentVectorRotation.setValue(rotation);

		rotation[3] = -theta1;
		ReflectedVectorRotation1.setValue(rotation);


		rotation[3] = (float) (Math.PI + theta2);
		RefractedVectorRotation1.setValue(rotation);

		rotation[3] = theta2;
		IncidentVectorRotation2.setValue(rotation);

		rotation[3] = -theta2;
		ReflectedVectorRotation2.setValue(rotation);

		rotation[3] = (float) (Math.PI + theta3);
		RefractedVectorRotation2.setValue(rotation);
	}
	
	public Complex getG5(float x) {
		return Complex.add(Complex.add(R5,1),Complex.multiply(new Complex(0,2*k2*(x+s)),m));
	}

	public Complex getG6(float x) {
		return Complex.add(Complex.add(R6,1),Complex.multiply(new Complex(0,2*k2*(x+s)),m6));
	}

	public void putG5(Complex G5) {
		magG5=(float)G5.mag();
		gamma5=(float)G5.arg();
	}

	public void putG6(Complex G6) {
		magG6=(float)G6.mag();
		gamma6=(float)G6.arg();
	}

	public float getIncidentWaveInIncidentMedium(float x, float y, float t, int scenario) {
		return (float) (amplitudeIncident * Math.cos(k1 * ((x + s) * Math.cos(theta1) + y * Math.sin(theta1) - v1 * t)));
	}
	
	public float getReflectedWaveInIncidentMedium(float x, float y, float t, int scenario) {
		switch(scenario) {
		case 0:
			return (float) (amplitudeIncident * magR * Math.cos(k1 * (-1 * (x+s) * Math.cos(theta1) + y * Math.sin(theta1) - v1 * t) + rho));
		case 1:
			return (float) (amplitudeIncident * magR2 * Math.cos(k1 * (-1 * (x+s) * Math.cos(theta1) + y * Math.sin(theta1) - v1 * t) + rho2));
		case 2:
			return (float) (amplitudeIncident * magR3 * Math.cos(k1 * (-1 * (x+s) * Math.cos(theta1) + y * Math.sin(theta1) - v1 * t) + rho3));
		case 3:
			return (float) (amplitudeIncident * magR4 * Math.cos(k1 * (-1 * (x+s) * Math.cos(theta1) + y * Math.sin(theta1) - v1 * t) + rho4));
		case 4:
			return (float) (amplitudeIncident * magR5 * Math.cos(k1 * (-1 * (x+s) * Math.cos(theta1) + y * Math.sin(theta1) - v1 * t) + rho5));
		case 5:
			return (float) (amplitudeIncident * magR6 * Math.cos(k1 * (-1 * (x+s) * Math.cos(theta1) + y * Math.sin(theta1) - v1 * t) + rho6));
		case 6:
			return (float) (amplitudeIncident * magR7 * Math.cos(k1 * (-1 * (x+s) * Math.cos(theta1) + y * Math.sin(theta1) - v1 * t) + rho7));
		case 7:
			return (float) (amplitudeIncident * magR8 * Math.cos(k1 * (-1 * (x+s) * Math.cos(theta1) + y * Math.sin(theta1) - v1 * t) + rho8));
		default:
			throw new IllegalArgumentException("Bad scenario number "+scenario);
		}
	}
	
	public float getWaveInMiddleMedium(float x, float y, float t, int scenario) {
		if(scenario<4 || scenario==6 || scenario ==7) {
			return getPlusWaveInMiddleMedium(x,y,t,scenario)+getMinusWaveInMiddleMedium(x,y,t,scenario);
		} else if(scenario==4) {
		   return (float) (amplitudeIncident * magG5 * Math.cos(k2 * (y - v2 * t) + gamma5));
		} else if(scenario==5) {
		   return (float) (amplitudeIncident * magG6 * Math.cos(k2 * (y - v2 * t) + gamma6));
		} else
			return 0;
	}

	public float getPlusWaveInMiddleMedium(float x, float y, float t, int scenario) {
		if(scenario == 0) {
			return (float) (amplitudeIncident * magGPlus * Math.cos(k2 * ((x+s) * Math.cos(theta2) + y * Math.sin(theta2) - v2 * t) + gammaPlus));
		} else if(scenario == 1) {
			return (float) (amplitudeIncident * magG2Plus * Math.cos(k2 * ((x+s) * Math.cos(theta2) + y * Math.sin(theta2) - v2 * t) + gamma2Plus));
		} else if(scenario == 2) {
			return (float) (amplitudeIncident * magG3Plus * Math.exp(-1 * (x+s) / Lnotprime) * Math.cos(k1 * (y * Math.sin(theta1) - v1 * t) + gamma3Plus));
		} else if(scenario == 3) {
			return (float) (amplitudeIncident * magG4Plus * Math.exp(-1 * (x+s) / Lnotprime) * Math.cos(k1 * (y * Math.sin(theta1) - v1 * t) + gamma4Plus));
		} else if(scenario == 6) {
			return (float) (amplitudeIncident * magG7Plus * Math.cos(k2 * ((x+s) * Math.cos(theta2) + y * Math.sin(theta2) - v2 * t) + gamma7Plus));
		} else if(scenario == 7) {
			return (float) (amplitudeIncident * magG8Plus * Math.exp(-1 * (x+s) / Lnotprime) * Math.cos(k1 * (y * Math.sin(theta1) - v1 * t) + gamma8Plus));
		} else
			return 0;
	}

	public float getMinusWaveInMiddleMedium(float x, float y, float t, int scenario) {
		if(scenario == 0) {
			return (float) (amplitudeIncident * magGMinus * Math.cos(k2 * (-1 * (x+s) * Math.cos(theta2) + y * Math.sin(theta2) - v2 * t) + gammaMinus));
		} else if(scenario == 1) {
			return (float) (amplitudeIncident * magG2Minus * Math.cos(k2 * (-1 * (x+s) * Math.cos(theta2) + y * Math.sin(theta2) - v2 * t) + gamma2Minus));
		} else if(scenario == 2) {
			return (float) (amplitudeIncident * magG3Minus * Math.exp((x+s-d) / Lnotprime) * Math.cos(k1 * (y * Math.sin(theta1) - v1 * t) + gamma3Minus));
		} else if(scenario == 3) {
			return (float) (amplitudeIncident * magG4Minus * Math.exp((x+s-d) / Lnotprime) * Math.cos(k1 * (y * Math.sin(theta1) - v1 * t) + gamma4Minus));
		} else if(scenario == 6) {
			return (float) (amplitudeIncident * magG7Minus * Math.cos(k2 * (-1 * (x+s) * Math.cos(theta2) + y * Math.sin(theta2) - v2 * t) + gamma7Minus));
		} else if(scenario == 7) {
			return (float) (amplitudeIncident * magG8Minus * Math.exp((x+s-d) / Lnotprime) * Math.cos(k1 * (y * Math.sin(theta1) - v1 * t) + gamma8Minus));
		} else
			return 0;
	}

	public float getTransmittedWaveInThirdMedium(float x, float y, float t, int scenario) {
		if(scenario == 0) {
			return (float) (amplitudeIncident * magT * Math.cos(k3 * ((x+s-d) * Math.cos(theta3) + y * Math.sin(theta3) - v3 * t) + tau));
		} else if(scenario == 1) {
			return (float) (amplitudeIncident * magT2 * Math.exp(-1 * (x+s-d) / Lprime) * Math.cos(k1 * (y * Math.sin(theta1) - v1 * t) + tau2));
		} else if(scenario == 2) {
			return (float) (amplitudeIncident * magT3 * Math.cos(k3 * ((x+s-d) * Math.cos(theta3) + y * Math.sin(theta3) - v3 * t) + tau3));
		} else if(scenario == 3) {
			return (float) (amplitudeIncident * magT4 * Math.exp(-1 * (x+s-d) / Lprime) * Math.cos(k1 * (y * Math.sin(theta1) - v1 * t) + tau4));
		}

		if(scenario == 4) {
			return (float) (amplitudeIncident * magT5 * Math.cos(k3 * ((x+s-d) * Math.cos(theta3) + y * Math.sin(theta3) - v3 * t) + tau5));
		}

		if(scenario == 5) {
			return (float) (amplitudeIncident * magT6 * Math.cos(k1 * (y * Math.sin(theta1) - v1 * t) + tau6)*Math.exp(-1*(x+s-d)/Lprime));
		}

		if(scenario == 6) {
			return (float) (amplitudeIncident * magT7 * Math.cos(k3 * (y - v3 * t) + tau7));
		} else if(scenario == 7) {
			return (float) (amplitudeIncident * magT8 * Math.cos(k3 * (y - v3 * t) + tau8));
		} else
			return 0;
	}
	
	public boolean isTIRAtFirstBoundary() {situation = getSituation(); return situation == 2 || situation == 3;}

	public boolean isTIRAtSecondBoundary() {situation = getSituation(); return situation == 1 || situation == 3;}

	protected void createX3DNode() {
		//This was commented out.  Is being created in the X3D file, may not need to be created here [JD]
		/*StringBuffer sb = new StringBuffer(VRMLString);
		sb.append("LinearWidget { amplitude ").append(amplitude);
		sb.append(" wavelength ").append(wavelength);
		//sb.append(" phase ").append(phase);
		sb.append(" angle ").append(theta1);
		sb.append(" x ").append(X);
		sb.append(" y ").append(Y);
		sb.append(" }\n");
		createNode(sb.toString());
		place();*/

		set_enabled = (SFBool) sai.getInputField(/*getNode()*/"Widget","enabled");
		set_amplitude = (SFFloat) sai.getInputField(/*getNode()*/"Widget","set_amplitude");
		set_wavelength = (SFFloat) sai.getInputField(/*getNode()*/"Widget","set_wavelength");
		set_position = (SFVec3f) sai.getInputField(/*getNode()*/"Widget","set_position");
		set_widgetVisible = (SFBool) sai.getInputField(/*getNode()*/"Widget","set_widgetVisible");
		set_angle = (SFFloat) sai.getInputField(/*getNode()*/"Widget","set_angle");

		/*sai.getOutputField(getNode(),"amplitude_changed",this, "amplitude_changed");
		sai.getOutputField(getNode(),"wavelength_changed",this, "wavelength_changed");
		sai.getOutputField(getNode(),"angle_changed",this, "angle_changed");
		sai.getOutputField(getNode(),"mouseClicked",this, "mouse_clicked");
		sai.getOutputField(getNode(),"mouseOver",this, "mouse_over");
		sai.getOutputField(getNode(),"mouseOverAmplitude",this, "mouseOverAmplitude");
		sai.getOutputField(getNode(),"mouseOverWavelength",this, "mouseOverWavelength");
		sai.getOutputField(getNode(),"mouseOverAngle",this, "mouseOverAngle");*/
		
		get_amplitude_changed = (SFFloat)sai.getOutputField("Widget","amplitude_changed",this, "amplitude_changed");
		get_wavelength_changed = (SFFloat)sai.getOutputField("Widget","wavelength_changed",this, "wavelength_changed");
		get_angle_changed = (SFFloat)sai.getOutputField("Widget","angle_changed",this, "angle_changed");
		get_mouse_clicked = (SFBool)sai.getOutputField("Widget","mouseClicked",this, "mouse_clicked");
		get_mouse_over = (SFBool)sai.getOutputField("Widget","mouseOver",this, "mouse_over");
		get_mouseOverAmplitude = (SFBool)sai.getOutputField("Widget","mouseOverAmplitude",this, "mouseOverAmplitude");
		get_mouseOverWavelength = (SFBool)sai.getOutputField("Widget","mouseOverWavelength",this, "mouseOverWavelength");
		get_mouseOverAngle = (SFBool)sai.getOutputField("Widget","mouseOverAngle",this, "mouseOverAngle");
	}
	
protected String getNodeName() {return "<LinearWidget>";}
	
	public String toString() {
		return new String("LinearSource(Amplitude=" + amplitude + ", Wavelength=" + wavelength
			+ ", Phase=" + phase + ", Angle=" + theta1 + ", X=" + X + ", Y=" + Y + ")");
	}

	public void readableFieldChanged(X3DFieldEvent e) {
		//System.out.println("REMOVEME: A WIDGET CHANGED!");
		String arg = (String) e.getData();
		//System.out.println("readableFieldChanged arg = " + arg);
		if(arg.equals("amplitude_changed")) {
			internalUpdate = true;
			setAmplitude(((SFFloat)e.getSource()).getValue());
			internalUpdate = false;
			sourcePanel.setAmplitude(FPRound.toFixVal(amplitudeIncident, 2));
			engine.update();
		} else if(arg.equals("angle_changed")) {
			internalUpdate = true;
				setAngle(((SFFloat)e.getSource()).getValue());
				internalUpdate = false;
			sourcePanel.setAngle(theta1);
			engine.update();
		} else if(arg.equals("mouse_clicked")) {
			if(((SFBool)e.getSource()).getValue()) {
				dragging = true;
				engine.setWidgetDragging(true);
			} else {
				dragging = false;
				engine.setWidgetDragging(false);
				statusBar.setText(null);
			}
		} else if(arg.equals("AMP_CHANGED")) {
			//On further inspection, there is already a method for "amplutude_changed" so 
			//commenting this out [JD]
				float temp = ((SFFloat) e.getSource()).getValue();
				temp = 20 * temp + 15;
				internalUpdate = true;
				setDistance(temp);
				internalUpdate = false;
				sourcePanel.setDistance(temp);
				engine.update();				 
		} else if(arg.equals("mouse_over")) {
			if(((SFBool)e.getSource()).getValue()) {
				wasPlaying = engine.isPlaying();
				engine.pause();
				widgetVisible = true;
				sourcePanel.show(this);
				if(wasPlaying) engine.play();
			} 
		} else if(arg.equals("mouseOverAmplitude")) {
			if(((SFBool)e.getSource()).getValue()) {
				statusBar.setText("Use this widget to change the amplitude.  Up - increase.  Down - decrease.");
			} else if(!dragging) {
				statusBar.setText(null);
			}
		} else if(arg.equals("mouseOverWavelength")) {
			if(((SFBool)e.getSource()).getValue()) {
				statusBar.setText("Use this widget to change the wavelength.  Outward - increase.  Inward - decrease.");
			} else if(!dragging) {
				statusBar.setText(null);
			}
		} else if(arg.equals("mouseOverPhase")) {
			if(((SFBool)e.getSource()).getValue())
				statusBar.setText("Use this widget to change the phase.  Outward - increase.  Inward - decrease.");
			else if(!dragging)
				statusBar.setText(null);
		} else if(arg.equals("mouseOverAngle")) {
			if(((SFBool)e.getSource()).getValue())
				statusBar.setText("Use this widget to change the angle.  Rotate 360 degrees.");
			else if(!dragging)
				statusBar.setText(null);
		} else if(arg.equals("OVER_DRAGGER")) {
				if(((SFBool)e.getSource()).getValue())
						statusBar.setText("Drag this widget to change the distance from the first boundary to the second.");
				else if(!dragging)
						statusBar.setText(null);
		} else if(arg.equals("wavelength_changed")) {
			internalUpdate = true;
				setWavelength(((SFFloat) e.getSource()).getValue());
				internalUpdate = false;
			//sourcePanel.setWavelength(wavelength);
			sourcePanel.setWavelength(lambda1);
			//if(wslPlayer!=null)
				//wslPlayer.recordMouseDragged(getID(), "wavelength", String.valueOf(lambda1));
			engine.update();
		}
	}
	
	private static String VRMLString =
		"PROTO RotationWidget [\n" +
		"  InitializeOnly        SFFloat     minAngle            0\n" +
		"  InputOnly      SFFloat     set_minAngle\n" +
		"  OutputOnly     SFFloat     minAngle_changed\n" +
		"  InitializeOnly        SFFloat     maxAngle            -1\n" +
		"  InputOnly      SFFloat     set_maxAngle\n" +
		"  OutputOnly     SFFloat     maxAngle_changed\n" +
		"  InitializeOnly        SFFloat     offset              0\n" +
		"  InputOnly      SFFloat     set_offset\n" +
		"  OutputOnly     SFFloat     offset_changed\n" +
		"  InputOutput SFBool      enabled             TRUE\n" +
		"  OutputOnly     SFBool      isActive\n" +
		"  OutputOnly     SFBool      isOver\n" +
		"  InputOnly      SFBool      set_isOver\n" +
		"  InputOnly      SFBool      set_isActive\n" +
		"  OutputOnly     SFRotation  rotation_changed\n" +
		"  OutputOnly     SFVec3f     trackPoint_changed\n" +
		"  InitializeOnly        MFNode      normalGeometry      []\n" +
		"  InitializeOnly        MFNode      overGeometry        []\n" +
		"  InitializeOnly        MFNode      clickedGeometry     []\n" +
		"]\n" +
		"{\n" +
		"  Group {\n" +
		"    children [\n" +
		"      DEF Touch-SENSOR TouchSensor {\n" +
		"        enabled IS enabled\n" +
		"         isOver IS isOver\n" +
		"      }\n" +
		"      DEF Rotational-SENSOR PlaneSensor {\n" +
		"        isActive IS isActive\n" +
		"        enabled IS enabled\n" +
		"        maxPosition 2000 2000\n" +
		"        minPosition -2000 -2000\n" +
		"        offset 2000 0 0\n" +
		"      }\n" +
		"      DEF Rotational-TRANSFORM Transform {\n" +
		"        children DEF Rotational-SWITCH Switch {\n" +
		"          whichChoice 0\n" +
		"          choice [\n" +
		"            Group { children IS normalGeometry }\n" +
		"            Group { children IS overGeometry }\n" +
		"            Group { children IS clickedGeometry }\n" +
		"          ]\n" +
		"        }\n" +
		"      }\n" +
		"    ]\n" +
		"  }\n" +
		"  DEF Rotational-SCRIPT Script {\n" +
		"    InitializeOnly    SFFloat minAngle IS minAngle\n" +
		"    InputOnly  SFFloat set_minAngle IS set_minAngle\n" +
		"    OutputOnly SFFloat minAngle_changed IS minAngle_changed\n" +
		"    InitializeOnly    SFFloat maxAngle IS maxAngle\n" +
		"    InputOnly  SFFloat set_maxAngle IS set_maxAngle\n" +
		"    OutputOnly SFFloat maxAngle_changed IS maxAngle_changed\n" +
		"    InitializeOnly    SFFloat trackOffset 0\n" +
		"    InitializeOnly    SFFloat offset IS offset\n" +
		"    InputOnly  SFFloat set_offset IS set_offset\n" +
		"    OutputOnly SFFloat offset_changed IS offset_changed\n" +
		"    InputOnly  SFVec3f set_translation\n" +
		"    InputOnly  SFVec3f set_hitPoint\n" +
		"    InputOnly  SFBool  set_touchSensorIsActive\n" +
		"    InputOnly  SFBool  set_touchSensorIsOver\n" +
		"    InputOnly  SFBool  set_planeSensorIsActive\n" +
		"    InputOnly  SFBool  set_isActive IS set_isActive\n" +
		"    InputOnly  SFBool  set_isOver IS set_isOver\n" +
		"    OutputOnly SFRotation rotation_changed IS rotation_changed\n" +
		"    OutputOnly SFVec3f    trackPoint_changed IS trackPoint_changed\n" +
		"    OutputOnly SFVec3f    internalOffset_changed\n" +
		"    OutputOnly SFInt32    whichChoice_changed\n" +
		"    InitializeOnly    SFBool  setOffset FALSE\n" +
		"    InitializeOnly    SFFloat lastOffset 0\n" +
		"    InitializeOnly    SFFloat lastAngle  0\n" +
		"    InitializeOnly    SFBool  isActive FALSE\n" +
		"    InitializeOnly    SFBool  isOver   FALSE\n" +
		"    InitializeOnly    MFNode  normalGeometry IS normalGeometry\n" +
		"    InitializeOnly    MFNode  overGeometry IS overGeometry\n" +
		"    InitializeOnly    MFNode  clickedGeometry IS clickedGeometry\n" +
		"    InitializeOnly    SFNode  Rotational-SWITCH USE Rotational-SWITCH\n" +
		"    #InitializeOnly    SFNode  Touch-SENSOR USE Touch-SENSOR\n" +
		"    url \"ecmascript:\n" +
		"      function initialize() {\n" +
		"        rotation_changed = new SFRotation(0, 0, 1, offset);\n" +
		"        internalOffset_changed = new SFVec3f(10, 0, 0);\n" +
		"        trackOffset = offset;\n" +
		"        lastOffset = offset;\n" +
		"        lastAngle = offset;\n" +
		"        whichChoice_changed = 0;\n" +
		"      }\n" +
		"      function set_translation(value, time) {\n" +
		"        length = Math.sqrt(value[0]*value[0] + value[1]*value[1]);\n" +
		"        angle = Math.atan2(value[1], value[0]);\n" +
		"        if(angle<0) angle = 2*Math.PI + angle;\n" +
		"        if(angle>=0 && angle<Math.PI/2.0 && lastAngle>=Math.PI*3.0/2.0)\n" +
		"          trackOffset += angle + Math.PI*2.0 - lastAngle;\n" +
		"        else if(angle>=Math.PI*3.0/2.0 && lastAngle>=0 && lastAngle<Math.PI/2.0)\n" +
		"          trackOffset += angle - lastAngle - Math.PI*2.0;\n" +
		"        else\n" +
		"          trackOffset += angle - lastAngle;\n" +
		"        offset = trackOffset;\n" +
		"        if(minAngle<maxAngle) {\n" +
		"          if(offset<minAngle) offset = minAngle;\n" +
		"          if(trackOffset<minAngle-Math.PI*2.0) trackOffset += Math.PI*2.0;\n" +
		"        }\n" +
		"        if(maxAngle>minAngle) {\n" +
		"          if(offset>maxAngle) offset = maxAngle;\n" +
		"          if(trackOffset>maxAngle+Math.PI*2.0) trackOffset -= Math.PI*2.0;\n" +
		"        }\n" +
		"        lastOffset = offset;\n" +
		"        lastAngle = angle;\n" +
		"        rotation_changed[3] = offset;\n" +
		"        offset_changed = offset;\n" +
		"      }\n" +
		"      function set_hitPoint(value, time) {\n" +
		"        if(setOffset) {\n" +
		"          setOffset = FALSE;\n" +
		"          internalOffset_changed[0] = value[0];\n" +
		"          internalOffset_changed[1] = value[1];\n" +
		"          length = Math.sqrt(value[0]*value[0] + value[1]*value[1]);\n" +
		"          lastAngle = Math.atan2(value[1], value[0]);\n" +
		"          if(lastAngle<0) lastAngle = Math.PI*2.0 + lastAngle;\n" +
		"        }\n" +
		"      }\n" +
		"      function set_offset(value, time) {\n" +
		"        offset = value;\n" +
		"        trackOffset = offset;\n" +
		"        rotation_changed[3] = offset;\n" +
		"        internalOffset_changed[0] = 2000 * Math.cos(offset);\n" +
		"        internalOffset_changed[1] = 2000 * Math.sin(offset);\n" +
		"      }\n" +
		"      function set_touchSensorIsActive(value, time) {\n" +
		"        isActive = value;\n" +
		"        if(value) setOffset = TRUE;\n" +
		"      }\n" +
		"      function set_touchSensorIsOver(value, time) {\n" +
		"        isOver = value;\n" +
		"        if(value && !isActive && overGeometry.length > 0) {\n" +
		"          whichChoice_changed = 1;\n" +
		"        }\n" +
		"        else if(!value && !isActive) {\n" +
		"          whichChoice_changed = 0;\n" +
		"        }\n" +
		"      }\n" +
		"      function set_planeSensorIsActive(value, time) {\n" +
		"        if(!value) {\n" +
		"          trackOffset = offset;\n" +
		"          lastOffset = offset;\n" +
		"          whichChoice_changed = 0;\n" +
		"       }\n" +
		"        else {\n" +
		"          if(clickedGeometry.length >0) {\n" +
		"            whichChoice_changed = 2;\n" +
		"          }\n" +
		"        }\n" +
		"      }\n" +
		"      function set_isActive(value, time) {\n" +
		"        isActive = value;\n" +
		"        if(value && clickedGeometry.length > 0) whichChoice_changed = 2;\n" +
		"        else if(!value) {\n" +
		"          if(isOver && overGeometry.length > 0) whichChocie_changed = 1;\n" +
		"          else whichChoice_changed = 0;\n" +
		"        }\n" +
		"      }\n" +
		"      function set_isOver(value, time) {\n" +
		"        isOver = value;\n" +
		"        if(value && !isActive && overGeometry.length > 0) whichChoice_changed = 1;\n" +
		"        else if(!value) {\n" +
		"          if(isActive && activeGeometry.length > 0) whichChoice_changed = 2;\n" +
		"          else whichChoice_changed = 0;\n" +
		"        }\n" +
		"      }\n" +
		"      function set_minAngle(value, time) {\n" +
		"        minAngle = value;\n" +
		"      }\n" +
		"      function set_maxAngle(value, time) {\n" +
		"        maxAngle = value;\n" +
		"      }\n" +
		"    \"\n" +
		"  }\n" +
		"  ROUTE Touch-SENSOR.hitPoint_changed TO Rotational-SCRIPT.set_hitPoint\n" +
		"  ROUTE Touch-SENSOR.isActive TO Rotational-SCRIPT.set_touchSensorIsActive\n" +
		"  ROUTE Touch-SENSOR.isOver TO Rotational-SCRIPT.set_touchSensorIsOver\n" +
		"  ROUTE Rotational-SENSOR.translation_changed TO Rotational-SCRIPT.set_translation\n" +
		"  ROUTE Rotational-SENSOR.isActive TO Rotational-SCRIPT.set_planeSensorIsActive\n" +
		"  ROUTE Rotational-SCRIPT.rotation_changed TO Rotational-TRANSFORM.set_rotation\n" +
		"  ROUTE Rotational-SCRIPT.internalOffset_changed TO Rotational-SENSOR.set_offset\n" +
		"  ROUTE Rotational-SCRIPT.whichChoice_changed TO Rotational-SWITCH.set_whichChoice\n" +
		"}\n" +
		"PROTO LinearWidget [ InitializeOnly    SFFloat amplitude  0.0\n" +
		"                     InitializeOnly    SFFloat angle      0.0\n" +
		//"                     InitializeOnly    SFFloat phase      0.0\n" +
		"                     InitializeOnly    SFFloat wavelength 1.0\n" +
		"                     InitializeOnly    SFFloat x          0.0\n" +
		"                     InitializeOnly    SFFloat y          0.0\n" +
		"                     InputOutput SFBool enabled TRUE\n" +
		"                     InputOnly  SFFloat set_amplitude\n" +
		"                     InputOnly  SFFloat set_angle\n" +
		//"                     InputOnly  SFFloat set_phase\n" +
		"                     InputOnly  SFFloat set_wavelength\n" +
		"                     InputOnly  SFVec3f set_position\n" +
		"                     InputOnly  SFBool  set_widgetVisible\n" +
		"                     OutputOnly SFFloat amplitude_changed\n" +
		"                     OutputOnly SFFloat angle_changed\n" +
		//"                     OutputOnly SFFloat phase_changed\n" +
		"                     OutputOnly SFFloat wavelength_changed\n" +
		"                     OutputOnly SFVec3f position_changed\n" +
		"                     OutputOnly SFBool  mouseClicked\n" +
		"                     OutputOnly SFBool  mouseOver\n" +
		"                     OutputOnly SFBool  mouseOverAmplitude\n" +
		"                     OutputOnly SFBool  mouseOverWavelength\n" +
		//"                     OutputOnly SFBool  mouseOverPhase\n" +
		"                     OutputOnly SFBool  mouseOverAngle      ]\n" +
		"{\n" +
		"  DEF Widget-SWITCH Switch {\n" +
		"    whichChoice 0\n" +
		"    choice [\n" +
		"      Transform { children [\n" +
		"        DEF Icon0-SENSOR TouchSensor {\n" +
		"          isOver IS mouseOver\n" +
		//"           isOver IS isOver\n" +
		"          enabled IS enabled\n" +
		"        }\n" +
		"        DEF Icon0-TRANSFORM3 Transform {\n" +
		"          rotation 1 0 0 -1.57\n" +
		"          children DEF Icon0-TRANSFORM2 Transform {\n" +
		"            children Transform {\n" +
		"              rotation 1 0 0 1.57\n" +
		"              children Transform {\n" +
		"                rotation 0 1 0 -1.57\n" +
		"                children DEF Icon0-TRANSFORM Transform {\n" +
		"                  children DEF Icon-SHAPE Group { children [\n" +
		"                    Shape {\n" +
		"                      appearance DEF Widget-APPEARANCE2 Appearance {\n" +
		"                        material Material {\n" +
		"                          diffuseColor 1.0 0.2 0.2\n" +
		"                        }\n" +
		"                      }\n" +
		"                      geometry Sphere {\n" +
		"                        radius 1.0\n" +
		"                      }\n" +
		"                    }\n" +
		"                    DEF Linear-SHAPE Shape {\n" +
		"                      appearance DEF Widget-APPEARANCE3 Appearance {\n" +
		"                        material Material {\n" +
		"                          diffuseColor 0.2 0.2 0.2\n" +
		"                          transparency 0.25\n" +
		"                        }\n" +
		"                      }\n" +
		"                      geometry Box {\n" +
		"                        size 4.0 1.0 0.04\n" +
		"                      }\n" +
		"                    }\n" +
		"                    Transform {\n" +
		"                      translation 0 0 -0.5\n" +
		"                      children USE Linear-SHAPE\n" +
		"                    }\n" +
		"                    Transform {\n" +
		"                      translation 0 0 -1.0\n" +
		"                      children USE Linear-SHAPE\n" +
		"                    }\n" +
		"                    Transform {\n" +
		"                      translation 0 0 0.5\n" +
		"                      children USE Linear-SHAPE\n" +
		"                    }\n" +
		"                    Transform {\n" +
		"                      translation 0 0 1.0\n" +
		"                      children USE Linear-SHAPE\n" +
		"                    }\n" +
		"                  ] }\n" +
		"                }\n" +
		"              }\n" +
		"            }\n" +
		"          }\n" +
		"        }\n" +
		"      ] }\n" +
		"      Group { children [\n" +
		"        USE Icon0-TRANSFORM3\n" +
		"        Transform { rotation 1 0 0 -1.57 children [\n" +
		"          DEF Widget-TRANSFORM Transform { children [\n" +
		"            DEF Angle-TRANSFORM Transform { children [\n" +
		"              DEF Angle-ROTATIONWIDGET RotationWidget {\n" +
		"                minAngle 0\n" +
		"                maxAngle 6.28318530\n" +
		"                set_offset IS set_angle\n" +
		"                offset_changed IS angle_changed\n" +
		"                offset IS angle\n" +
		"                isOver IS mouseOverAngle\n" +
		"        enabled IS enabled\n" +
		"                normalGeometry [\n" +
		"                  Transform { rotation 1 0 0 1.57 children [\n" +
		"                    Transform {\n" +
		"                      rotation 0 1 0 -1.57\n" +
		"                      children [\n" +
		"                        DEF Arrow-TRANSFORM Transform { children [\n" +
		"                          Transform {\n" +
		"                            translation 20 0 0\n" +
		"                            scale 4 4 4\n" +
		"                            children [\n" +
		"                              DEF Arrow Transform {\n" +
		"                                translation -0.141421 -0.125 0.353553\n" +
		"                                rotation 0 1 0 0.785398\n" +
		"                                children [\n" +
		"                                  Shape {\n" +
		"                                    appearance DEF Widget-APPEARANCE Appearance {\n" +
		"                                      material Material {\n" +
		"                                        diffuseColor 0.4 0.4 0.8\n" +
		"                                      }\n" +
		"                                    }\n" +
		"                                    geometry DEF Arrow-FACES IndexedFaceSet {\n" +
		"                                      ccw TRUE\n" +
		"                                      solid TRUE\n" +
		"                                      coord DEF Arrow-COORD Coordinate { point [\n" +
		"                                        0 0.25 0, 0.5 0.25 0, 0.5 0.25 -0.5, 0 0.25 -0.1, 0.4 0.25 -0.1, \n" +
		"                                        0.4 0.25 -0.5, 0 0.25 0, 0.5 0.25 0, 0.5 0.25 -0.5, \n" +
		"                                        0.4 0.25 -0.5, 0.4 0.25 -0.1, 0 0.25 -0.1, 0 0 0, 0.5 0 0, \n" +
		"                                        0.5 0 -0.5, 0.4 0 -0.5, 0.4 0 -0.1, 0 0 -0.1, 0 0 0, \n" +
		"                                        0.5 0 0, 0.5 0 -0.5, 0 0 -0.1, 0.4 0 -0.1, 0.4 0 -0.5]\n" +
		"                                      }\n" +
		"                                      coordIndex [\n" +
		"                                        3, 0, 1, -1, 3, 1, 4, -1, 4, 1, 2, -1, 4, 2, 5, -1, 6, 12, 13, -1, \n" +
		"                                        6, 13, 7, -1, 7, 13, 14, -1, 7, 14, 8, -1, 8, 14, 15, -1, \n" +
		"                                        8, 15, 9, -1, 9, 15, 16, -1, 9, 16, 10, -1, 10, 16, 17, -1, \n" +
		"                                        10, 17, 11, -1, 11, 17, 12, -1, 11, 12, 6, -1, 18, 21, 22, -1, \n" +
		"                                        18, 22, 19, -1, 19, 22, 23, -1, 19, 23, 20, -1]\n" +
		"                                      }\n" +
		"                                  }\n" +
		"                                ]\n" +
		"                              }\n" +
		"                            ]\n" +
		"                          }\n" +
		"                          Transform {\n" +
		"                            rotation 0 1 0 3.142\n" +
		"                            scale 4 4 4\n" +
		"                            translation -20 0 0\n" +
		"                            children USE Arrow\n" +
		"                          }\n" +
		"                          Transform {\n" +
		"                            rotation 0 0 1 1.57\n" +
		"                            children [\n" +
		"                              Shape {\n" +
		"                                appearance USE Widget-APPEARANCE\n" +
		"                                geometry Cylinder {\n" +
		"                                  radius 0.1\n" +
		"                                  height 41\n" +
		"                                }\n" +
		"                              }\n" +
		"                            ]\n" +
		"                          }\n" +
		"                        ] }\n" +
		"                      ]\n" +
		"                    }\n" +
		"                  ] }\n" +
		"                ]\n" +
		"              }\n" +
		"            ] }\n" +
		"            DEF WidgetGroup-TRANSFORM Transform {\n" +
		"              children [\n" +
		"                Transform { rotation 1 0 0 1.57 children [\n" +
		"                  Transform {\n" +
		"                    rotation 0 0 1 -1.57\n" +
		"                    children [\n" +
		"                      DEF Wavelength-TRANSFORM2 Transform { children\n" +
		"                        Transform {\n" +
		"                          translation 0 0.5 0\n" +
		"                          children Shape {\n" +
		"                            appearance USE Widget-APPEARANCE\n" +
		"                            geometry Cylinder {\n" +
		"                              radius 0.1\n" +
		"                              height 1\n" +
		"                            }\n" +
		"                          }\n" +
		"                        }\n" +
		"                      }\n" +
		"                      Transform { children [\n" +
		"                        TouchSensor {\n" +
		"                          isOver IS mouseOverWavelength\n" +
		"                          enabled IS enabled\n" +
		"                        }\n" +
		"                        DEF Wavelength-SENSOR PlaneSensor {\n" +
		"                          minPosition 0 2.01\n" +
		"                          maxPosition 0 42.0\n" +
		"                          offset      0 2.01 0\n" +
		"                          isActive IS mouseClicked\n" +
		"                          enabled IS enabled\n" +
		"                        }\n" +
		"                        DEF Wavelength-TRANSFORM Transform {\n" +
		"                          children Shape {\n" +
		"                            appearance USE Widget-APPEARANCE\n" +
		"                            geometry Cone {\n" +
		"                              height 2.0\n" +
		"                              bottomRadius 0.8\n" +
		"                            }\n" +
		"                          }\n" +
		"                        }\n" +
		"                      ] }\n" +
		"                    ]\n" +
		"                  }\n" +
		"                ] }\n" +
		"              ]\n" +
		"            }\n" +
		"            Transform {\n" +
		"              rotation 1 0 0 1.57\n" +
		"              children [\n" +
		"                DEF Amplitude-TRANSFORM2 Transform { children\n" +
		"                  Transform {\n" +
		"                    translation 0 0.5 0\n" +
		"                    children [\n" +
		"                      Shape {\n" +
		"                        appearance USE Widget-APPEARANCE\n" +
		"                        geometry Cylinder {\n" +
		"                          radius 0.1\n" +
		"                          height 1\n" +
		"                        }\n" +
		"                      }\n" +
		"                    ]\n" +
		"                  }\n" +
		"                }\n" +
		"                Transform { children [\n" +
		"                  TouchSensor {\n" +
		"                    isOver IS mouseOverAmplitude\n" +
		"                    enabled IS enabled\n" +
		"                  }\n" +
		"                  DEF Amplitude-SENSOR PlaneSensor {\n" +
		"                    minPosition 0 2.0\n" +
		"                    maxPosition 0 12.0\n" +
		"                    offset 0 2.0 0\n" +
		"                    isActive IS mouseClicked\n" +
		"                    enabled IS enabled\n" +
		"                  }\n" +
		"                  DEF Amplitude-TRANSFORM Transform {\n" +
		"                    translation 0 3 0\n" +
		"                    children [\n" +
		"                      Shape {\n" +
		"                        appearance USE Widget-APPEARANCE\n" +
		"                        geometry Cone {\n" +
		"                          height 2.0\n" +
		"                          bottomRadius 0.8\n" +
		"                        }\n" +
		"                      }\n" +
		"                    ]\n" +
		"                  }\n" +
		"                ] }\n" +
		"              ]\n" +
		"            }\n" +
		"          ] }\n" +
		"        ] }\n" +
		"      ] }\n" +
		"    ]\n" +
		"  }\n" +
		"  DEF Linear-SCRIPT Script {\n" +
		"    InitializeOnly    SFFloat amplitude  IS amplitude\n" +
		"    InitializeOnly    SFFloat angle      IS angle\n" +
		"    InitializeOnly    SFFloat wavelength IS wavelength\n" +
		//"    InitializeOnly    SFFloat phase      IS phase\n" +
		"    InitializeOnly    SFFloat x          IS x\n" +
		"    InitializeOnly    SFFloat y          IS y\n" +
		"    InputOnly  SFVec3f    set_translation1\n" +
		"    InputOnly  SFVec3f    set_translation2\n" +
		//"    InputOnly  SFVec3f    set_translation3\n" +
		"    InputOnly  SFVec3f    set_translation4\n" +
		"    InputOnly  SFRotation set_rotation\n" +
		"    InputOnly  SFFloat    set_amplitude  IS set_amplitude\n" +
		"    InputOnly  SFFloat    set_angle      IS set_angle\n" +
		//"    InputOnly  SFFloat    set_phase      IS set_phase\n" +
		"    InputOnly  SFFloat    set_wavelength IS set_wavelength\n" +
		"    InputOnly  SFVec3f    set_position   IS set_position\n" +
		"    InputOnly  SFBool     set_widgetVisible IS set_widgetVisible\n" +
		"    InputOnly  SFBool     set_widgetVisibleInternal\n" +
		"    OutputOnly SFVec3f    scale1_changed\n" +
		"    OutputOnly SFVec3f    translation1_changed\n" +
		"    OutputOnly SFRotation rotation_changed\n" +
		"    OutputOnly SFVec3f    scale2_changed\n" +
		"    OutputOnly SFVec3f    translation2_changed\n" +
		//"    OutputOnly SFVec3f    scale3_changed\n" +
		//"    OutputOnly SFVec3f    translation3_changed\n" +
		"    OutputOnly SFVec3f    translation4_changed\n" +
		"    OutputOnly SFVec3f    offset1_changed\n" +
		"    OutputOnly SFVec3f    offset2_changed\n" +
		//"    OutputOnly SFVec3f    offset3_changed\n" +
		"    OutputOnly SFFloat    angle_init\n" +
		"    OutputOnly SFInt32    whichChoice\n" +
		"    OutputOnly SFFloat amplitude_changed  IS amplitude_changed\n" +
		"    OutputOnly SFFloat angle_changed      IS angle_changed\n" +
		//"    OutputOnly SFFloat phase_changed      IS phase_changed\n" +
		"    OutputOnly SFFloat wavelength_changed IS wavelength_changed\n" +
		"    url \"ecmascript:\n" +
		"      function initialize() {\n" +
		"        offset1_changed = new SFVec3f(0, amplitude+2.0, 0);\n" +
		"        scale1_changed = new SFVec3f(1, amplitude+2.0, 1);\n" +
		"        translation1_changed = new SFVec3f(0, amplitude+2.0, 0);\n" +
		"        angle_init = angle;\n" +
		"        rotation_changed = new SFRotation(0, 1, 0, angle_init);\n" +
		"        if(wavelength<=0) wavelength = 0.01;\n" +
		"        offset2_changed = new SFVec3f(0, wavelength+2.0, 0);\n" +
		"        scale2_changed = new SFVec3f(1, wavelength+2.0, 1);\n" +
		"        translation2_changed = new SFVec3f(0, wavelength+2.0, 0);\n" +
		//"        offset3_changed = new SFVec3f(0, -2.0-phase, 0);\n" +
		//"        scale3_changed = new SFVec3f(1, 2.0+phase, 1);\n" +
		//"        translation3_changed = new SFVec3f(0, -2.0-phase, 0);\n" +
		"        translation4_changed = new SFVec3f(x, y, 10);\n" +
		"        //position_changed = new SFVec3f(x, y, 0);\n" +
		"      }\n" +
		"      function set_translation1(value, time) {\n" +
		"        scale1_changed[1] = value[1];\n" +
		"        amplitude = value[1]-2.0;\n" +
		"        amplitude_changed = amplitude;\n" +
		"      }\n" +
		"      function set_amplitude(value, time) {\n" +
		"        amplitude = value;\n" +
		"        translation1_changed[1] = value+2.0;\n" +
		"        scale1_changed[1] = value+2.0;\n" +
		"        offset1_changed[1] = value+2.0;\n" +
		"      }\n" +
		"      function set_rotation(value, time) {\n" +
		"        angle = value[3];\n" +
		"        angle_changed = angle;\n" +
		"      }\n" +
		"      function set_angle(value, time) {\n" +
		"        angle = value;\n" +
		"        rotation_changed[3] = value;\n" +
		"        angle_init = value;\n" +
		"      }\n" +
		"      function set_translation2(value, time) {\n" +
		"        scale2_changed[1] = value[1];\n" +
		"        wavelength = value[1]-2.0;\n" +
		"        wavelength_changed = wavelength;\n" +
		"      }\n" +
		"      function set_wavelength(value, time) {\n" +
		"        scale2_changed[1] = value+2.0;\n" +
		"        translation2_changed[1] = value+2.0;\n" +
		"        offset2_changed[1] = value+2.0;\n" +
		"        wavelength = value;\n" +
		"      }\n" +
		//"      function set_translation3(value, time) {\n" +
		//"        scale3_changed[1] = -value[1];\n" +
		//"        phase = -2.0-value[1];\n" +
		//"        phase_changed = phase;\n" +
		//"      }\n" +
		//"      function set_phase(value, time) {\n" +
		//"        scale3_changed[1] = value+2.0;\n" +
		//"        translation3_changed[1] = -2.0-value;\n" +
		//"        offset3_changed[1] = -2.0-value;\n" +
		//"        phase = value;\n" +
		//"      }\n" +
		"      function set_translation4(value, time) {\n" +
		"        x = value[0];\n" +
		"        y = value[1];\n" +
		"        position_changed[0] = x;\n" +
		"        position_changed[1] = y;\n" +
		"      }\n" +
		"      function set_position(value, time) {\n" +
		"        x = value[0];\n" +
		"        y = value[1];\n" +
		"        translation4_changed[0] = x;\n" +
		"        translation4_changed[1] = y;\n" +
		"      }\n" +
		"      function set_widgetVisible(value, time) {\n" +
		"        if(value) whichChoice = 1;\n" +
		"        else whichChoice = 0;\n" +
		"      }\n" +
		"      function set_widgetVisibleInternal(value, time) {\n" +
		"        if(value) whichChoice = 1;\n" +
		"      }\n" +
		"    \"\n" +
		"  }\n" +
		"  ROUTE Icon0-SENSOR.isOver TO Linear-SCRIPT.set_widgetVisibleInternal\n" +
		"  ROUTE Angle-ROTATIONWIDGET.rotation_changed TO Icon0-TRANSFORM2.rotation\n" +
		"  ROUTE Angle-ROTATIONWIDGET.rotation_changed TO WidgetGroup-TRANSFORM.rotation\n" +
		"  ROUTE Amplitude-SENSOR.translation_changed TO Amplitude-TRANSFORM.translation\n" +
		"  ROUTE Amplitude-SENSOR.translation_changed TO Linear-SCRIPT.set_translation1\n" +
		"  ROUTE Wavelength-SENSOR.translation_changed TO Wavelength-TRANSFORM.set_translation\n" +
		"  ROUTE Wavelength-SENSOR.translation_changed TO Linear-SCRIPT.set_translation2\n" +
		"  ROUTE Linear-SCRIPT.rotation_changed TO Icon0-TRANSFORM2.rotation\n" +
		"  ROUTE Linear-SCRIPT.rotation_changed TO WidgetGroup-TRANSFORM.rotation\n" +
		"  ROUTE Linear-SCRIPT.offset1_changed TO Amplitude-SENSOR.offset\n" +
		"  ROUTE Linear-SCRIPT.scale1_changed TO Amplitude-TRANSFORM2.scale\n" +
		"  ROUTE Linear-SCRIPT.translation1_changed TO Amplitude-TRANSFORM.translation\n" +
		"  ROUTE Linear-SCRIPT.offset2_changed TO Wavelength-SENSOR.offset\n" +
		"  ROUTE Linear-SCRIPT.scale2_changed TO Wavelength-TRANSFORM2.scale\n" +
		"  ROUTE Linear-SCRIPT.translation2_changed TO Wavelength-TRANSFORM.translation\n" +
		"  ROUTE Linear-SCRIPT.translation4_changed TO Icon0-TRANSFORM2.set_translation\n" +
		"  ROUTE Linear-SCRIPT.translation4_changed TO Widget-TRANSFORM.set_translation\n" +
		"  ROUTE Linear-SCRIPT.whichChoice TO Widget-SWITCH.whichChoice\n" +
		"}\n";
}
