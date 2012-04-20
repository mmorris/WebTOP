/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.wavefront;

import org.webtop.util.AnimationEngine;
import org.webtop.util.Animation;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;

public class Engine implements AnimationEngine {
	private float elapsed;

	private final SAI sai;

	private Plane plane[] = new Plane[200];

	private final Sphere sphere[] = new Sphere[MAXSPHERES];

	// private final PlaneTransform planeTransform;
	private float tStep;

	private float wavelength;

	private float sFreq, freq;

	private float sWavelength; // scaled frequency, wavelength

	private int numSpheres = 0; // current number of spheres

	private int numPlanes = 0;

	private static final int MAXSPHERES = 9; // make valid

	private static final int MAXPLANES = 100;

	private static final int MAXRADIUS = 140;

	private static final float WAVESPEED = 9f;// units/second of radius
												// expansion

	private static final float SCALE = 0.04f; // arbitrary scaling for size of
												// spheres

	private static final int MAXPLANEDISTANCE = 300;

	// private static final float PI=(float)Math.PI;

	boolean bonk = false;

	private SFInt32 showMode;

	private SFVec3f planeOffset;

	private SFRotation planeRotation;

	private WaveFront.Data curData;

	public Engine(SAI _sai) {
		System.out.println("Engine constructor");
		sai = _sai;
		// planeTransform = new PlaneTransform(sai);
	}

	public void init(Animation anim) {
		tStep = anim.getPeriod() / 1000f;

		planeOffset = (SFVec3f) sai.getInputField("PlaneTranslation",
				"set_translation");
		// planeRotation=(SFRotation)sai.getInputField("PlaneRotationPh","set_rotation");

		// TODO: take this out
		for (int i = 0; i < MAXPLANES; i++) {
			plane[i] = new Plane(sai);
		}
		for (int i = 0; i < MAXSPHERES; i++) {
			sphere[i] = new Sphere(sai);
		}
	}

	public boolean timeElapsed(float periods) {

		elapsed += periods;
		if (elapsed > numSpheres * sWavelength / (WAVESPEED * tStep)) {
			// System.out.println("resetting time");
			elapsed -= numSpheres * sWavelength / (WAVESPEED * tStep);
		}
		return true;
	}

	public synchronized void execute(Animation.Data d) {
		WaveFront.Data data;
		if (curData != null) {
			data = (WaveFront.Data) d;
			if (data.mode != curData.mode) {
				System.out.println("Attempting to clean	up");
				curData = data;
				if (data.mode == WaveFront.SPHEREMODE) {
					planeCleanup();
					sphereInit();
					return;
				} else {

					sphereCleanup();
					planeInit();
					return;
				}
			}
			if (data.mode == WaveFront.SPHEREMODE) {
				sphereExecute(d);
			} else {
				planeExecute(d);
			}
		} else {
			sphereInit();
			sphereExecute(d);
		}
	}

	void sphereInit() {
		System.out.println("sphereInit");
		// for(int i=0; i<MAXSPHERES; i++) {
		// sphere[i]=new Sphere(sai);
		// }

		// for(int i=0; i<MAXSPHERES; i++) {
		// sphere[i].setTransparency(0f);
		// }
		numPlanes = 0;
	}

	void sphereCleanup() {
		System.out.println("sphereCleanup");
		for (int i = 0; i < MAXSPHERES; i++) {
			sphere[i].setTransparency(1);
			// sphere[i].destroy();
			// sphere[i]= null;
		}
		numSpheres = 0;
	}

	void sphereExecute(Animation.Data d) {
		if (curData != d || numSpheres == 0) {
			// Reset time so that widget moves with waves
			elapsed = 0;
			// elapsed=0.1f;

			if (curData == null)
				curData = (WaveFront.Data) d;

			curData = (WaveFront.Data) d;
			System.out.println("curData updated; rebuilding spheres");
			wavelength = curData.sphereWavelength;
			freq = 1 / wavelength;
			sWavelength = SCALE * wavelength;
			sFreq = SCALE * freq;
			//moved this call above the calculations.  Prevents caching of numberbox data [JD]
			//curData = (WaveFront.Data) d;
			final int oldNumSpheres = numSpheres;
			numSpheres = 0;
			for (float y = 0; y < MAXRADIUS - sWavelength; y += sWavelength) {
				// Create whichever spheres are needed
				sphere[numSpheres].setTransparency(0);
				numSpheres++;
			}
			for (int i = numSpheres; i < oldNumSpheres; i++) {
				sphere[i].setTransparency(1f);
				sphere[i].setRadius(1);
			}

		}

		for (int i = 0; i < numSpheres; i++) {
			// Movement
			float radius = (float) (i * sWavelength + elapsed
					* (WAVESPEED * tStep));
			if (radius >= numSpheres * sWavelength - 1)
				radius -= numSpheres * sWavelength;

			final Sphere s = sphere[i];

			s.setRadius(radius);
			s.setTransparency(1);

			// Transparency calculation
			if (radius > (numSpheres * sWavelength) - 8) {
				final double fadeDist = (radius - ((numSpheres * sWavelength) - 8)) / 8;
				if (fadeDist < 0.5)
					s.setTransparency((float) (1 - Math.pow(1 - 2 * fadeDist,
							1 / 3d)));
				else
					s.setTransparency((float) (Math.pow(2 * fadeDist, 1 / 3d)));
			} else
				s.setTransparency(0);
		}
	}

	void planeInit() {
		System.out.println("planeInit");
		// for(int i=0; i<MAXPLANES; i++) {
		// plane[i]=new Plane(sai);
		// }

		for (int i = 0; i < MAXPLANES; i++) {
			plane[i].setTransparency(1f);
		}
	}

	void planeCleanup() {
		for (int i = 0; i < MAXPLANES; i++) {
			// plane[i].destroy();
			plane[i].setTransparency(1);
		}
		numPlanes = 0;
	}

	void planeExecute(Animation.Data d) {
		if (curData != d || numPlanes == 0) {
			System.out.println("entering update block");

			curData = (WaveFront.Data) d;
			wavelength = curData.planeWavelength * 2f;
			freq = 1 / wavelength;
			sWavelength = SCALE * wavelength;
			sFreq = SCALE * freq;
			//moved this call above the calculations.  Prevents caching of numberbox data [JD]
			//curData = (WaveFront.Data) d;

			int oldNumPlanes;
			oldNumPlanes = numPlanes;
			numPlanes = 0;
			for (float y = 0; y < MAXPLANEDISTANCE; y += sWavelength) {
				numPlanes++;
				// System.out.println("Visibilizing : ) Plane");
				plane[numPlanes].setTransparency(0);
				plane[numPlanes].setColor(0.64f, 0.64f, 0.64f);
				plane[numPlanes].setSize(50);

				plane[numPlanes].setPosition(sWavelength * numPlanes
						- sWavelength);
				// System.out.println(numPlanes);
			}
			// System.out.println("done creating planes");

			for (int i = numPlanes + 1; i <= oldNumPlanes; i++) {
				plane[i].setTransparency(1f);
				// plane[i].setSize(100);
			}
			curData = (WaveFront.Data) d;

		}

		if (elapsed >= sWavelength / (WAVESPEED * tStep))
			elapsed -= sWavelength / (WAVESPEED * tStep);

		float offset[] = new float[3];
		offset[2] = elapsed * (WAVESPEED * tStep)
				- (sWavelength * numPlanes / 2);

		planeOffset.setValue(offset);

		// TODO: fixme (this sets angle of waves)
		float rotation[] = new float[4];
		rotation[1] = 1;

		// Fading
		float transparency = (sWavelength - (elapsed * WAVESPEED * tStep))
				/ sWavelength;
		if (transparency < 0)
			transparency = 0;
		plane[1].setTransparency(transparency);
		plane[numPlanes].setTransparency(1 - transparency);

	}

	/*
	 * private class PlaneTransform { private final SFRotation phiRotation;
	 * private final SFRotation thetaRotation;
	 * 
	 * public PlaneTransform(SAI sai) {
	 * phiRotation=(SFRotation)sai.getInputField("PlaneRotationPhi",
	 * "set_rotation");
	 * thetaRotation=(SFRotation)sai.getInputField("PlaneRotationTheta",
	 * "set_rotation"); }
	 * 
	 * public void setPhi(float angle) { float[] rot = {1, 0, 0, angle};
	 * phiRotation.setValue(rot); }
	 * 
	 * public void setTheta(float angle) { float[] rot = {0, 1, 0, angle};
	 * thetaRotation.setValue(rot); } }
	 */

	private class Sphere extends X3DObject {
		private final SFFloat radius;

		private final SFColor diffuseColor;

		private final SFFloat transparency;

		// private final SFFloat initTest; //TODO: remove after testing

		protected String getNodeName() {
			return "Sphere";
		}

		public Sphere(SAI sai) {
			super(sai, sai.getNode("World"));

			createProto("AllBut");

			place();
			radius = (SFFloat) sai.getInputField(this.getNode(), "set_radius");
			diffuseColor = (SFColor) sai.getInputField(this.getNode(),
					"set_diffuseColor");
			transparency = (SFFloat) sai.getInputField(this.getNode(),
					"set_transparency");
			setTransparency(1);

		}

		public void setRadius(float r) {
			radius.setValue((float) r);
		}

		public void setColor(float r, float g, float b) {
			SAI.set3(diffuseColor, r, g, b);
		}

		public void setTransparency(float t) {
			if (t > 1.0f) {
				// System.out.print("Transparency out of range");
				// TODO: Figure out why we need this here.
				t = 1.0f;
			}
			transparency.setValue(t);
		}
	}

	private class Plane extends X3DObject {
		private final SFFloat size;

		private final SFColor diffuseColor;

		private final SFFloat transparency;

		private final SFFloat position;

		protected String getNodeName() {
			return "Plane";
		}

		public Plane(SAI sai) {
			super(sai, sai.getNode("PlaneTranslation"));

			createProto("PlaneWave");
			place();
			size = (SFFloat) sai.getInputField(this.getNode(), "set_size");
			diffuseColor = (SFColor) sai.getInputField(this.getNode(),
					"set_diffuseColor");
			transparency = (SFFloat) sai.getInputField(this.getNode(),
					"set_transparency");
			position = (SFFloat) sai.getInputField(this.getNode(),
					"set_position");
			setTransparency(1);
		}

		public void setPosition(float x) {
			// setSize(30);
			// setTransparency(0);
			position.setValue((float) x);
		}

		public void setSize(float s) {
			size.setValue(s);
		}

		public void setColor(float r, float g, float b) {
			SAI.set3(diffuseColor, r, g, b);
		}

		public void setTransparency(float t) {
			transparency.setValue(t);
		}
	}
}
