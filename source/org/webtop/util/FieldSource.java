/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//FieldSource.java
//Defines an abstract class for calculating electric fields for plane waves.
//Davis Herring
//Created September 12 2004
//Updated April 18 2005
//Version 0.0

package org.webtop.util;

import org.sdl.math.Vector2;

//A plane wave travelling along the z-axis is assumed.

public abstract class FieldSource {
	public static class Polarized extends FieldSource {
		private float lambda,wavenumber,ampX,ampY,phaseDiff;
		//The fields are cosine waves with val==amp at z==t==0; the phase
		//difference advances the y component along the z axis.
		public Polarized(float vel,float wLength,float eX,float eY,float pDiff) {
			super(vel);
			setWavelength(wLength);
			ampX=eX;
			ampY=eY;
			phaseDiff=pDiff;
		}

		public float getWavelength() {return lambda;}
		public float getWavenumber() {return wavenumber;}
		public float getXAmplitude() {return ampX;}
		public float getYAmplitude() {return ampY;}
		public float getPhaseDifference() {return phaseDiff;}

		public void setWavelength(float l) {
			if(l<=0) throw new IllegalArgumentException("Non-positive wavelength");
			lambda=l;
			wavenumber=2*(float)Math.PI/l;
		}
		public void setWavenumber(float k) {
			if(k<=0) throw new IllegalArgumentException("Non-positive wavenumber");
			wavenumber=k;
			lambda=2*(float)Math.PI/k;
		}
		public void setXAmplitude(float a) {ampX=a;}
		public void setYAmplitude(float a) {ampX=a;}
		public void setPhaseDifference(float e) {phaseDiff=e;}

		protected float getX(float Z) {return ampX*(float)Math.cos(Z*wavenumber);}
		protected float getY(float Z) {return ampY*(float)Math.cos(Z*wavenumber-phaseDiff);}
	}

	//We approximate unpolarized light by superimposing a set of polarized waves
	//of different frequencies and phases.
	public static class Unpolarized extends FieldSource {
		//Here we assume ampX==ampY; phase differences are entirely random
		private float omega0,sigmaOmega,amp;
		private final FieldSource.Polarized[] waves;
		private final int n;

		public Unpolarized(float vel,float af0,float saf,float e,int nWaves) {
			super(vel);
			//Using the internal functions avoids setting up the waves
			setCF0(af0);
			setBW0(saf);
			amp=e;
			n=nWaves;
			waves=new FieldSource.Polarized[n];
			setupWaves();
		}

		//Note that the frequencies here are angular
		public float getCentralFrequency() {return omega0;}
		public float getBandwidth() {return sigmaOmega;}
		public float getAmplitude() {return amp;}

		public void setCentralFrequency(float o0) {setCF0(o0); setupWaves();}
		private void setCF0(float o0) {
			if(o0<=0)
				throw new IllegalArgumentException("Non-positive central frequency");
			omega0=o0;
		}
		public void setBandwidth(float bw) {setBW0(bw); setupWaves();}
		private void setBW0(float bw) {
			if(bw<0) throw new IllegalArgumentException("Negative bandwidth");
			sigmaOmega=bw;
		}
		public void setAmplitude(float a) {
			amp=a;
			for(int i=0;i<n;++i) {
				final FieldSource.Polarized p=waves[i];
				p.setXAmplitude(a);
				p.setYAmplitude(a);
			}
		}

		//It is of course very unlikely that in normal use we would have a
		//negative Gaussian frequency produced, but just in case...
		private float positiveRandomFrequency() {
			float f;
			do {
				f=omega0+sigmaOmega*(float)WTMath.random.nextGaussian();
			} while(f<=0);
			return f;
		}

		//Public in case a client wants to re-randomize waves
		public void setupWaves() {
			for(int i=0;i<n;++i)
				waves[i]=new FieldSource.Polarized(this.velocity,
																					 this.velocity*positiveRandomFrequency()/(2*(float)Math.PI),
																					 amp,amp,(float)(2*Math.PI*Math.random()));
		}

		protected float getX(float Z) {
			float s=0;
			for(int i=0;i<n;++i) s+=waves[i].getX(Z);
			return s;
		}

		protected float getY(float Z) {
			float s=0;
			for(int i=0;i<n;++i) s+=waves[i].getY(Z);
			return s;
		}
	}

	//If this wasn't light, we might have a reason to have a FieldSource without
	//a constant velocity.  Then abstract again and make ConstantVelocitySource
	//with this declaration.
	public final float velocity;
	public FieldSource(float vel) {velocity=vel;}

	public final Vector2 getField(float z,float t) {
		final float Z=z-velocity*t;
		return new Vector2(getX(Z),getY(Z));
	}
	public final void storeField(float z,float t,float[] buf) {
		final float Z=z-velocity*t;
		buf[0]=getX(Z);
		buf[1]=getY(Z);
	}

	//Subclasses need only consider the wave at t==0:
	protected abstract float getX(float Z);
	protected abstract float getY(float Z);
}
