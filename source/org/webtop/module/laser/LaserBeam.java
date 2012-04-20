/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.laser;

import org.webtop.util.*;
import org.web3d.x3d.sai.*;
import org.webtop.x3d.SAI;
import org.webtop.x3d.output.*;
import org.webtop.module.laser.Engine;

public class LaserBeam {
	//==================
	// PUBLIC CONSTANTS
	//==================
	public static final float X_IFS_LENGTH=3.75f,Y_IFS_LENGTH=3.75f;

	//To get the beam-mode whichChoice value, multiply the
	//aperture number by 2 and add one iff high res
	public static final int AP_CIRCULAR=0;
	public static final int AP_VERTICAL=1;
	public static final int AP_HORIZONTAL=2;
	public static final int AP_BOTH=3;

	//====================
	// INTERNAL CONSTANTS
	//====================
	//The sample points over each dimension of the ElevationGrid:
	private static final int X_IFS_DIM_HIGH=51,Y_IFS_DIM_HIGH=51;
	private static final int X_IFS_DIM_LOW=11,Y_IFS_DIM_LOW=11;

	private static final int POINTS_HIGH=32,POINTS_LOW=4;
	private static final int CIRCLES_HIGH=100,CIRCLES_LOW=10;
	private static final int FAR_CIRCLES=5;

	//================
	// SAI REFERENCES
	//================
	private MFVec3f beamPointsH,beamPointsL,beamPointsF,beamPointsHG,beamPointsLG/*,linePoints*/;
	private MFInt32 beamIndicesH,beamIndicesL,beamIndicesF,beamIndicesHG,beamIndicesLG/*,lineIndices*/;
	private LinePlot line;

	//private MFColor screenColorsH,screenColorsL; //TODO -- ?!?!
	private IFSScreen screenColorsH,screenColorsL;
	
	private MFColor beamColorsH,beamColorsL,beamColorsHG,beamColorsLG;
	private SFColor beamColorF;

	private SFInt32 egChoice,laserbeamChoice,laserbeamResChoice,apertureChoice;

	//==============
	// DATA BUFFERS
	//==============
	private float[] w,wLow,farw,intensityValues;

	private float[] xCoords,xCoordsLow;

	private float[][] coords,coordsLow,coordsTEM10High,coordsTEM10Low,coordsTEM11High,coordsTEM11Low;

	private float farXCoords[];
	private float farCoords[][];

	private int[] indices,indicesLow,indicesTEMXYLow,indicesTEMXYHigh,farIndices;

	private float beamAtScreenCoords[][];		//What is this, actually? [Davis]

	private float[] beamCavityIntensity=new float [CIRCLES_HIGH+1],
									beamCavityIntensityLow=new float [CIRCLES_LOW+1];

	private float[][] beamCavityIntensityRGBtoColorLow=new float[(POINTS_LOW)*(CIRCLES_LOW+1)][3],
										beamCavityIntensityRGBtoColorHigh=new float[(POINTS_HIGH)*(CIRCLES_HIGH+1)][3],
										beamCavityIntensityRGBtoColorTEMXYLow=new float[(POINTS_LOW*2)*(CIRCLES_LOW+1)][3],
										beamCavityIntensityRGBtoColorTEMXYHigh=new float[(POINTS_HIGH*2)*(CIRCLES_HIGH+1)][3];

	//===============
	// BEAM SETTINGS
	//===============
	private Engine engine;

	private int yIFSDim=Y_IFS_DIM_HIGH,xIFSDim=X_IFS_DIM_HIGH;
	private float xIFSStep=X_IFS_LENGTH/xIFSDim,yIFSStep=Y_IFS_LENGTH/yIFSDim;
	private int nPoints,nCircles;
	private int aperture_mode=AP_CIRCULAR;
	private boolean low_res,lasing=true;
	private float percentageBeam=1;
	private float xp1,xp2,dxp;

	private float wScreen;		//Output

	public LaserBeam(Engine e,SAI sai) {
		engine=e;
		
		//Set up the beam shape
		beamPointsH=(MFVec3f) sai.getInputField("BeamCoordsHigh","set_point");
		beamIndicesH=(MFInt32) sai.getInputField("BeamFacesHigh","set_coordIndex");
		beamColorsH=(MFColor) sai.getInputField("BeamColorsHigh","set_color");

		beamPointsL=(MFVec3f) sai.getInputField("BeamCoordsLow","set_point");
		beamIndicesL=(MFInt32) sai.getInputField("BeamFacesLow","set_coordIndex");
		beamColorsL=(MFColor) sai.getInputField("BeamColorsLow","set_color");
		
		//Separate Beams for The Tem00 Gausian Beam
		beamPointsHG=(MFVec3f) sai.getInputField("BeamCoordsHighG","set_point");
		beamIndicesHG=(MFInt32) sai.getInputField("BeamFacesHighG","set_coordIndex");
		beamColorsHG=(MFColor) sai.getInputField("BeamColorsHighG","set_color");

		beamPointsLG=(MFVec3f) sai.getInputField("BeamCoordsLowG","set_point");
		beamIndicesLG=(MFInt32) sai.getInputField("BeamFacesLowG","set_coordIndex");
		beamColorsLG=(MFColor) sai.getInputField("BeamColorsLowG","set_color");
		//
		
		beamPointsF=(MFVec3f) sai.getInputField("FarCoords","set_point");
		beamIndicesF=(MFInt32) sai.getInputField("FarFaces","set_coordIndex");
		beamColorF=(SFColor) sai.getInputField("FarBeamMaterial","set_diffuseColor");
		
		
		//Set up the Screen
		screenColorsH = new IFSScreen(new IndexedSet(sai, sai.getNode("IFSHigh")), new int[][]{{X_IFS_DIM_HIGH, Y_IFS_DIM_HIGH}}, X_IFS_LENGTH, Y_IFS_LENGTH);
		screenColorsL = new IFSScreen(new IndexedSet(sai, sai.getNode("IFSLow")), new int[][]{{X_IFS_DIM_LOW, Y_IFS_DIM_LOW}}, X_IFS_LENGTH, Y_IFS_LENGTH);
		
		screenColorsH.setup();
		screenColorsL.setup();

		//set up the intensity line
		line=new LinePlot(new IndexedSet(sai,sai.getNode("ilsNode")),
											2*X_IFS_LENGTH,2*X_IFS_DIM_HIGH-3,2*X_IFS_DIM_LOW-3);

		egChoice=(SFInt32) sai.getInputField("ResSwitch","set_whichChoice");

		laserbeamChoice=(SFInt32) sai.getInputField("BeamSwitch","set_whichChoice");
		laserbeamResChoice=(SFInt32) sai.getInputField("BeamModeSwitch","set_whichChoice");
		apertureChoice=(SFInt32) sai.getInputField("ApertureSwitch","set_whichChoice");

		low_res=true;		//To force setLowRes to do stuff
		setLowResolution(false);

		xCoords=new float[nCircles+1];
		xCoordsLow=new float[nCircles+1];

		farXCoords=new float[FAR_CIRCLES+1];

		beamAtScreenCoords=new float[nPoints][3];
		
		coords=new float[nPoints*(nCircles+1)][3];
		coordsLow=new float[(POINTS_LOW)*(CIRCLES_LOW+1)][3];

		coordsTEM10Low=new float[(POINTS_LOW*2)*(CIRCLES_LOW+1)*2][3];
		coordsTEM10High=new float[(POINTS_HIGH*2)*(CIRCLES_HIGH+1)*2][3];
		coordsTEM11Low=new float[(POINTS_LOW*2)*(CIRCLES_LOW+1)*2][3];
		coordsTEM11High=new float[(POINTS_HIGH*2)*(CIRCLES_HIGH+1)*2][3];

		farCoords=new float[nPoints*(FAR_CIRCLES+1)][3];

		w=new float[CIRCLES_HIGH+1];
		wLow=new float[CIRCLES_LOW+1];

		farw=new float[FAR_CIRCLES+1];
		
		indices=new int[((nCircles)*6*nPoints)];
		indicesLow=new int[((CIRCLES_LOW)*6*POINTS_LOW)];

		indicesTEMXYLow=new int[((CIRCLES_LOW)*6*(POINTS_LOW*2))];
		indicesTEMXYHigh=new int[((CIRCLES_HIGH)*6*(POINTS_HIGH*2))];

		farIndices=new int[((nCircles)*6*nPoints)];

		initializeIndices();
		initializeFarIndices();
	} // end laserbeam constructor

	public int getXIFSDim() {return xIFSDim;}
	public int getYIFSDim() {return yIFSDim;}

	public void setLasing(boolean on) {
		if(lasing!=on) {
			lasing=on;
			updateChoices();
		}
	}
	public boolean isLasing() {return lasing;}

	//This should probably call a more-generic beam-redraw
	public void setIntensity(float valueFromScrollbar) {
		float hue=WTMath.hue(engine.getLambda());

		percentageBeam=1 + (valueFromScrollbar / 10);
		DebugPrinter.println("LaserBeam::setIntensity: percentageBeam=" + percentageBeam);

		float scaledvalues[]=new float[nCircles+1];
		float scaledvaluesRGB[][]=new float[nCircles+1][3];

		if(low_res) {
			for(int i=0; i<(nCircles+1); i++) {
				scaledvalues[i]=beamCavityIntensityLow[i] * percentageBeam;
				WTMath.hls2rgb(scaledvaluesRGB[i], hue, scaledvalues[i], WTMath.SATURATION);
			}

			//float beamCavityIntensityRGBtoColorLow[][]=new float[(nPoints)*(nCircles+1)][3];

			if(aperture_mode==AP_CIRCULAR) {
				for(int i=0, counter=0; i<nCircles+1; i++) {
					for(int j=0; j<nPoints; j++) {
						beamCavityIntensityRGBtoColorLow[counter]=scaledvaluesRGB[i];
						counter++;
					}
				}
				beamColorsLG.setValue(beamCavityIntensityRGBtoColorLow.length,beamCavityIntensityRGBtoColorLow);
			} else {
				for(int i=0, counter=0; i<nCircles+1; i++) {
					for(int j=0; j<nPoints*2; j++) {
						beamCavityIntensityRGBtoColorTEMXYLow[counter]=scaledvaluesRGB[i];
						counter++;
					}
				}
				beamColorsL.setValue(beamCavityIntensityRGBtoColorLow.length,beamCavityIntensityRGBtoColorLow);
			}
		} else {
			for(int i=0; i<(nCircles+1); i++) {
				scaledvalues[i]=beamCavityIntensity[i] * percentageBeam;
				WTMath.hls2rgb(scaledvaluesRGB[i], hue, scaledvalues[i], WTMath.SATURATION);
			}

			//float beamCavityIntensityRGB[][]=new float[(nPoints)*(nCircles+1)][3];

			if(aperture_mode==AP_CIRCULAR) {
				for(int i=0, counter=0; i<nCircles+1; i++) {
					for(int j=0; j<nPoints; j++) {
						beamCavityIntensityRGBtoColorHigh[counter]=scaledvaluesRGB[i];
						counter++;
					}
				}
				beamColorsHG.setValue(beamCavityIntensityRGBtoColorHigh.length,beamCavityIntensityRGBtoColorHigh);
			} else {
				for(int i=0, counter=0; i<nCircles+1; i++) {
					for(int j=0; j<nPoints*2; j++) {
						beamCavityIntensityRGBtoColorTEMXYHigh[counter]=scaledvaluesRGB[i];
						counter++;
					}
				}
				beamColorsH.setValue(beamCavityIntensityRGBtoColorTEMXYHigh.length,beamCavityIntensityRGBtoColorTEMXYHigh);
			}
		}
	}

	public void setApertureMode(int mode) {
		aperture_mode=mode;
		updateChoices();
	}
	public int getApertureMode() {return aperture_mode;}

	public void setLowResolution(boolean isLowRes) {
		DebugPrinter.println("LaserBeam::setLowResolution("+isLowRes+") (was "+low_res+")");
		if(low_res==isLowRes) return;
		if(low_res=isLowRes) {	//sneaky sneaky
			yIFSDim=Y_IFS_DIM_LOW;
			xIFSDim=X_IFS_DIM_LOW;
			nCircles=CIRCLES_LOW;
			nPoints=POINTS_LOW;
		} else {
			yIFSDim=Y_IFS_DIM_HIGH;
			xIFSDim=X_IFS_DIM_HIGH;
			nCircles=CIRCLES_HIGH;
			nPoints=POINTS_HIGH;
		}

		updateChoices();
		xIFSStep=(float) (X_IFS_LENGTH) / (float) (xIFSDim);
		yIFSStep=(float) (Y_IFS_LENGTH) / (float) (yIFSDim);

		dxp=(xp2-xp1)/nCircles;

		intensityValues=new float[(xIFSDim) * (yIFSDim)];
		//makeIndices();
	}

	public float getWScreen() {return wScreen;}

	//Returns the intensity at the center of screen, as for the applet's output label
	public float getI() {
		if(aperture_mode==0) {		//TEM00 case
			float row=(float) (Math.sqrt(Math.pow(0,2) + Math.pow(0,2)));
			return (float) (Math.pow((engine.getW0()/wScreen), 2) * Math.exp(-2*Math.pow(row,2)/Math.pow(wScreen,2)));
		} else return 0;		//any other case: center-of-screen intensity is 0
	}

	public void setXPs(float one,float two) {
		xp1=one;
		xp2=two;
		dxp=(xp2-xp1)/nCircles;
	}

	private void dieIfNotLasing() {if(!lasing) throw new IllegalStateException("Nothing to draw; not lasing");}

	//Must be called (immediately) before drawing functions!
	public void calculate() {
		wScreen=(float) (engine.getW0() * (Math.sqrt(1+Math.pow(engine.getZ()/engine.getzR(),2))));
	}

	public void drawBeam() {
		dieIfNotLasing();
		DebugPrinter.println("LaserBeam::drawBeam()");
		if(low_res) {		// Low Resolution Beam
			switch(aperture_mode) {
			case AP_CIRCULAR:
				makeBeam00Low();
				beamIndicesLG.setValue(indicesLow.length,indicesLow);
				break;
			case AP_HORIZONTAL:
				makeBeamXYLow();
				beamIndicesL.setValue(indicesTEMXYLow.length,indicesTEMXYLow);
				break;
			case AP_VERTICAL:
				makeBeamXYLow();
				beamIndicesL.setValue(indicesTEMXYLow.length,indicesTEMXYLow);
				break;
			case AP_BOTH:
				makeBeam11Low();
				beamIndicesL.setValue(indicesTEMXYLow.length,indicesTEMXYLow);
				break;
			default:
				new IllegalStateException("Error: aperture_mode is=" + aperture_mode + " low_res=" + low_res).printStackTrace();
				break;
			}
			laserbeamResChoice.setValue(aperture_mode*2);
		} // end low res
		// High Resolution Beam
		else {
			switch(aperture_mode) {
			case AP_CIRCULAR:
				makeBeam00High();
				beamIndicesHG.setValue(indices.length,indices);
				break;
			case AP_HORIZONTAL:
				makeBeamXYHigh();
				beamIndicesH.setValue(indicesTEMXYHigh.length,indicesTEMXYHigh);
				break;
			case AP_VERTICAL:
				makeBeamXYHigh();
				beamIndicesH.setValue(indicesTEMXYHigh.length,indicesTEMXYHigh);
				break;
			case AP_BOTH:
				makeBeam11High();
				beamIndicesH.setValue(indicesTEMXYHigh.length,indicesTEMXYHigh);
				break;
			default:
				new IllegalStateException("Error: aperture_mode is=" + aperture_mode + " low_res=" + low_res).printStackTrace();
				break;
			}
			laserbeamResChoice.setValue(aperture_mode*2+1);
		} // end high res
	} // end laserbeam.drawBeam()

	public void drawFarBeam() {
		dieIfNotLasing();
		DebugPrinter.println("LaserBeam::drawFarBeam()");
		int index=0;
		dxp=(engine.getScreenDist() - xp2) / (FAR_CIRCLES);
		for(float xp=xp2; index<FAR_CIRCLES+1; index++) {
			farXCoords[index]=xp / 100;
			xp+=dxp;
		}

		index=0;
		for(float xp=xp2; index<FAR_CIRCLES+1; index++) {
			float z=xp - engine.getL1();
			farw[index]=(float) (engine.getW0() * (Math.sqrt(1+(Math.pow(z/engine.getzR(),2)))));
			xp+=dxp;
		}
		
		CircleFactory.genCircle(wScreen, engine.getScreenDist()/Laser.SCREENDIST_SCALE, 0, beamAtScreenCoords, nPoints);

		for(int c=0; c<(nCircles+1); c++)
			CircleFactory.genCircle(farw[c], farXCoords[c], nPoints*c, farCoords, nPoints);

//		beamPointsF.setValue(farCoords);
	}

	public void drawSpot() {
		dieIfNotLasing();
		DebugPrinter.println("LaserBeam::drawSpot()");

		switch(aperture_mode) {
			case AP_CIRCULAR:
				makeSpot00();
				break;
			case AP_HORIZONTAL:
				//makeSpot01(); //Davis/Dr. Foley hack #4...xy was in the wrong direction
				makeSpot10();
				break;
			case AP_VERTICAL:
				//makeSpot10();//Davis/Dr. Foley hack#4..xy was in the wrong direction
				makeSpot01();
				break;
			case AP_BOTH:
				makeSpot11();
				break;
			default:
				new IllegalStateException("Error: aperture_mode is=" + aperture_mode).printStackTrace();
				break;
		}

		float hue=WTMath.hue(engine.getLambda());

		float intensityRGB[][]=new float[xIFSDim*yIFSDim][3];

		for(int i=0; i<xIFSDim*yIFSDim; i++)
			WTMath.hls2rgb(intensityRGB[i], hue, intensityValues[i], WTMath.SATURATION);

		if(low_res)
			screenColorsL.setColors(intensityRGB);
		else
			screenColorsH.setColors(intensityRGB);

		// in case you want to draw the beam past mirror2, just
		// uncomment this code to set the appropriate colors
		//		beamColorF.setValue(intensityForBeamRGB);
	}

	// this function draws the intensity line over the observation screen
	public void calculateLine() {
		dieIfNotLasing();
		DebugPrinter.println("LaserBeam::calculateLine()");

		float zR=engine.getzR(),L1=engine.getL1(),w0=engine.getW0(),length=engine.getLength();

		float ILength=(float) (Math.pow(zR,2)/(Math.pow(zR,2)+Math.pow(length+200f-L1,2)));

		int sample_number=xIFSDim-1;

		float dx=X_IFS_LENGTH/sample_number;

		float row;

		float[] I=new float [(2*sample_number)-1];

		switch(aperture_mode) {
			case AP_CIRCULAR:
				for(int i=0, p=sample_number-1; i<sample_number; i++, p++)
					I[p]=(float) (w0*w0/(wScreen*wScreen) * Math.exp(-2*dx*i*dx*i/(wScreen*wScreen))) / ILength;
				break;
			case AP_HORIZONTAL:
				//Values are already 0!
				break;
			case AP_VERTICAL:
				for(int i=0, p=sample_number-1; i<sample_number; i++, p++)
					I[p]=(float) (w0*w0/(wScreen*wScreen) * Math.exp(-2*dx*i*dx*i/(wScreen*wScreen)) * 8*dx*i*dx*i/(wScreen*wScreen)) / ILength;
				break;
			case AP_BOTH:
				//Values are already 0!
				break;
			default:
				new IllegalStateException("Bad aperture mode: "+aperture_mode).printStackTrace();
				break;
		}

		if(aperture_mode==AP_CIRCULAR || aperture_mode==AP_VERTICAL)
			for(int i=(sample_number*2)-2, p=0; i>sample_number-1; i--, p++) I[p]=I[i];

		//linePoints.setValue(I);
		line.setValues(I);
	}

	private void makeBeam00Low() {
		// Find the x-sample points
		int index=0;
		for(float xp=xp1; index<nCircles+1; index++) {
			if(xp >=xp2+dxp) new IllegalStateException("xp >=xp2+dxp").printStackTrace();
			xCoordsLow[index]=xp / 100;
			xp+=dxp;
		}

		// the w value at the observation screen
		// Calculate the w(z), the beam width, at each x-sample point
		// At the same time calculate the Intensity at each x-sample point
		index=0;
		for(float xp=xp1; index<nCircles+1; index++) {
			float z=xp - engine.getL1();
			wLow[index]=(float) (engine.getW0() * (Math.sqrt(1+(Math.pow(z/engine.getzR(),2)))));
			float row=(float) (Math.sqrt(2*Math.pow(wLow[index],2)));
			beamCavityIntensityLow[index]=(float) ((Math.pow(engine.getW0()/wLow[index], 2) * Math.exp(-2*Math.pow(row,2)/Math.pow(wLow[index],2)) ) );
			xp+=dxp;
		}

		// ---- COLOR MODEL ----
		// now we need to set the color of the beam
		// convert to RGB values first
		float beamCavityIntensityRGBLow[][]=new float[nCircles+1][3];
		float hue=WTMath.hue(engine.getLambda());
		float scaledvalues[]=new float[nCircles+1];

		for(int i=0; i<nCircles+1; i++) {
			scaledvalues[i]=beamCavityIntensityLow[i] * percentageBeam;
			WTMath.hls2rgb(beamCavityIntensityRGBLow[i], hue, scaledvalues[i], WTMath.SATURATION);
		}
		// ---- END COLOR MODEL ----

		// each panel needs the same color
		for(int i=0, counter=0; i<nCircles+1; i++) {
			for(int j=0; j<nPoints; j++) {
				beamCavityIntensityRGBtoColorLow[counter]=beamCavityIntensityRGBLow[i];
				counter++;
			}
		}

		// pass the colors to VRML
		beamColorsLG.setValue(beamCavityIntensityRGBtoColorLow.length,beamCavityIntensityRGBtoColorLow);

		for(int c=0; c<(nCircles+1); c++)
			CircleFactory.genCircle(wLow[c], xCoordsLow[c], nPoints*c, coordsLow, nPoints);
		beamPointsLG.setValue(coordsLow.length,coordsLow);
	} // end makeSpot00Low()

	private void makeBeam00High() {
		// Find the x-sample points
		int index=0;
		for(float xp=xp1; index<nCircles+1; index++) {
			if(xp >=xp2+dxp) new IllegalStateException("xp >=xp2+dxp").printStackTrace();
			xCoords[index]=xp / 100;
			xp+=dxp;
		}

		// Calculate the w(z), the beam width, at each x-sample point
		// At the same time calculate the Intensity at each x-sample point
		index=0;
		for(float xp=xp1; index<nCircles+1; index++) {
			float z=xp - engine.getL1();
			w[index]=(float) (engine.getW0() * (Math.sqrt(1+(Math.pow(z/engine.getzR(),2)))));
			float row=(float) (Math.sqrt(2*Math.pow(w[index],2)));
			beamCavityIntensity[index]=(float) (Math.pow(engine.getW0()/w[index], 2) * Math.exp(-2*row*row/(w[index]*w[index])));
			xp+=dxp;
		}

		// ----	COLOR MODEL ----
		// now we need to set the color of the beam
		// convert to RGB values first
		float beamCavityIntensityRGB[][]=new float[nCircles+1][3];
		float hue=WTMath.hue(engine.getLambda());
		float scaledvalues[]=new float[nCircles+1];

		for(int i=0; i<nCircles+1; i++) {
			scaledvalues[i]=beamCavityIntensity[i] * percentageBeam;
			WTMath.hls2rgb(beamCavityIntensityRGB[i], hue, scaledvalues[i], WTMath.SATURATION);
		}
		// ---- END COLOR MODEL ----

		// each panel needs the same color
		for(int i=0, counter=0; i<nCircles+1; i++) {
			for(int j=0; j<nPoints; j++) {
				beamCavityIntensityRGBtoColorHigh[counter]=beamCavityIntensityRGB[i];
				counter++;
			}
		}

		// pass the colors to VRML
		beamColorsHG.setValue(beamCavityIntensityRGBtoColorHigh.length,beamCavityIntensityRGBtoColorHigh);

		for(int c=0; c<(nCircles+1); c++)
			CircleFactory.genCircle(w[c], xCoords[c], nPoints*c, coords, nPoints);
		beamPointsHG.setValue(coords.length,coords);
	} // end makeSpot00High()

	private void makeBeamXYLow() {
		// Find the x-sample points
		int index=0;
		for(float xp=xp1; index<nCircles+1; index++) {
			if(xp >=xp2+dxp) new IllegalStateException("xp >=xp2+dxp").printStackTrace();
			xCoordsLow[index]=xp / 100;
			xp+=dxp;
		}

		// Calculate the w(z), the beam width, at each x-sample point
		// At the same time calculate the Intensity at each x-sample point
		index=0;
		for(float xp=xp1; index<nCircles+1; index++,xp+=dxp) {
			float z=xp-engine.getL1();
			wLow[index]=(float) (engine.getW0() * (Math.sqrt(1+(Math.pow(z/engine.getzR(),2)))));
			float row=(float) (Math.sqrt(2*Math.pow(wLow[index],2)));
			beamCavityIntensityLow[index]=(float) ((Math.pow(engine.getW0()/wLow[index], 2) * Math.exp(-2*row*row/(wLow[index]*wLow[index]))));
		}

		// ----	COLOR MODEL ----
		// now we need to set the color of the beam
		// convert to RGB values first
		float beamCavityIntensityRGBLow[][]=new float[nCircles+1][3];
		float hue=WTMath.hue(engine.getLambda());
		float scaledvalues[]=new float[nCircles+1];

		for(int i=0; i<nCircles+1; i++) {
			scaledvalues[i]=beamCavityIntensityLow[i] * percentageBeam;
			WTMath.hls2rgb(beamCavityIntensityRGBLow[i], hue, scaledvalues[i], WTMath.SATURATION);
		}
		// ---- END COLOR MODEL ----

		// each panel needs the same color
		for(int i=0, counter=0; i<(nCircles+1); i++) {
			for(int j=0; j<nPoints*2; j++) {
				beamCavityIntensityRGBtoColorTEMXYLow[counter]=beamCavityIntensityRGBLow[i];
				counter++;
			}
		}

		// pass the colors to VRML
		beamColorsL.setValue(beamCavityIntensityRGBtoColorTEMXYLow.length,beamCavityIntensityRGBtoColorTEMXYLow);

		for(int c=0; c<(nCircles+1); c++)
			CircleFactory.genBeamTEMXY(wLow[c], xCoordsLow[c], (nPoints*2)*c, coordsTEM10Low, (nPoints), nCircles);
		beamPointsL.setValue(coordsTEM10Low.length,coordsTEM10Low);
	}

	private void makeBeamXYHigh() {
		// Find the x-sample points
		int index=0;
		for(float xp=xp1; index<nCircles+1; index++) {
			if(xp >=xp2+dxp) new IllegalStateException("xp >=xp2+dxp").printStackTrace();
			xCoords[index]=xp / 100;
			xp+=dxp;
		}

		// Calculate the w(z), the beam width, at each x-sample point
		// At the same time calculate the Intensity at each x-sample point
		index=0;
		for(float xp=xp1; index<nCircles+1; index++,xp+=dxp) {
			float z=xp-engine.getL1();
			w[index]=(float) (engine.getW0() * (Math.sqrt(1+(Math.pow(z/engine.getzR(),2)))));
			float row=(float) (Math.sqrt(2*Math.pow(w[index],2)));
			beamCavityIntensity[index]=(float) (Math.pow(engine.getW0()/w[index], 2) * Math.exp(-2*row*row/(w[index]*w[index])));
		}

		// ----	COLOR MODEL ----
		// now we need to set the color of the beam
		// convert to RGB values first
		float beamCavityIntensityRGB[][]=new float[nCircles+1][3];
		float hue=WTMath.hue(engine.getLambda());
		float scaledvalues[]=new float[nCircles+1];

		for(int i=0; i<nCircles+1; i++) {
			scaledvalues[i]=beamCavityIntensity[i] * percentageBeam;
			WTMath.hls2rgb(beamCavityIntensityRGB[i], hue, scaledvalues[i], WTMath.SATURATION);
		}
		// ---- END COLOR MODEL ----

		// each panel needs the same color
		for(int i=0, counter=0; i<nCircles+1; i++) {
			for(int j=0; j<nPoints*2; j++) {
				beamCavityIntensityRGBtoColorTEMXYHigh[counter]=beamCavityIntensityRGB[i];
				counter++;
			}
		}

		// pass the colors to VRML
		beamColorsH.setValue(beamCavityIntensityRGBtoColorTEMXYHigh.length,beamCavityIntensityRGBtoColorTEMXYHigh);

		for(int c=0; c<(nCircles+1); c++)
			CircleFactory.genBeamTEMXY(w[c], xCoords[c], (nPoints*2)*c, coordsTEM10High, nPoints, nCircles);
		beamPointsH.setValue(coordsTEM10High.length,coordsTEM10High);
	}

	private void makeBeam11Low() {
		// Find the x-sample points
		int index=0;
		for(float xp=xp1; index<nCircles+1; index++) {
			if(xp >=xp2+dxp) new IllegalStateException("xp >=xp2+dxp").printStackTrace();
			xCoordsLow[index]=xp / 100;
			xp+=dxp;
		}

		// Calculate the w(z), the beam width, at each x-sample point
		// At the same time calculate the Intensity at each x-sample point
		index=0;
		for(float xp=xp1; index<nCircles+1; index++,xp+=dxp) {
			float z=xp-engine.getL1();
			wLow[index]=(float) (engine.getW0() * (Math.sqrt(1+(Math.pow(z/engine.getzR(),2)))));
			float row=(float) (Math.sqrt(2*Math.pow(wLow[index],2)));
			beamCavityIntensityLow[index]=(float) (Math.pow(engine.getW0()/wLow[index], 2) * Math.exp(-2*row*row/(wLow[index]*wLow[index])));
		}

		// ----	COLOR MODEL ----
		// now we need to set the color of the beam
		// convert to RGB values first
		float beamCavityIntensityRGBLow[][]=new float[nCircles+1][3];
		float hue=WTMath.hue(engine.getLambda());
		float scaledvalues[]=new float[nCircles+1];

		for(int i=0; i<nCircles+1; i++) {
			scaledvalues[i]=beamCavityIntensityLow[i] * percentageBeam;
			WTMath.hls2rgb(beamCavityIntensityRGBLow[i], hue, scaledvalues[i], WTMath.SATURATION);
		}
		// ---- END COLOR MODEL ----

		// each panel needs the same color
		for(int i=0, counter=0; i<(nCircles+1); i++) {
			for(int j=0; j<nPoints*2; j++) {
				beamCavityIntensityRGBtoColorTEMXYLow[counter]=beamCavityIntensityRGBLow[i];
//				beamCavityIntensityRGBtoColorTEM10Low[(nCircles+1)*(nPoints*2)+counter]=beamCavityIntensityRGBLow[i];
				counter++;
			}
		}

		// pass the colors to VRML
		beamColorsL.setValue(beamCavityIntensityRGBtoColorTEMXYLow.length,beamCavityIntensityRGBtoColorTEMXYLow);

		for(int c=0; c<(nCircles+1); c++)
			CircleFactory.genBeamTEM11(wLow[c], xCoordsLow[c], (nPoints*2)*c, coordsTEM11Low, (nPoints), nCircles);
		beamPointsL.setValue(coordsTEM11Low.length,coordsTEM11Low);
	}

	private void makeBeam11High() {
		// Find the x-sample points
		int index=0;
		for(float xp=xp1; index<nCircles+1; index++) {
			if(xp >=xp2+dxp) new IllegalStateException("xp >=xp2+dxp").printStackTrace();
			xCoords[index]=xp / 100;
			xp+=dxp;
		}

		// Calculate the w(z), the beam width, at each x-sample point
		// At the same time calculate the Intensity at each x-sample point
		index=0;
		for(float xp=xp1; index<nCircles+1; index++) {
			float z=xp-engine.getL1();
			w[index]=(float) (engine.getW0() * (Math.sqrt(1+(Math.pow(z/engine.getzR(),2)))));
			float row=(float) (Math.sqrt(2*Math.pow(w[index],2)));
			beamCavityIntensity[index]=(float) ((Math.pow(engine.getW0()/w[index], 2) * Math.exp(-2*row*row/(w[index]*w[index]))));
			xp+=dxp;
		}

		// ----	COLOR MODEL ----
		// now we need to set the color of the beam
		// convert to RGB values first
		float beamCavityIntensityRGB[][]=new float[nCircles+1][3];
		float hue=WTMath.hue(engine.getLambda());
		float scaledvalues[]=new float[nCircles+1];

		for(int i=0; i<nCircles+1; i++) {
			scaledvalues[i]=beamCavityIntensity[i] * percentageBeam;
			WTMath.hls2rgb(beamCavityIntensityRGB[i], hue, scaledvalues[i], WTMath.SATURATION);
		}
		// ---- END COLOR MODEL ----

		// each panel needs the same color
		for(int i=0, counter=0; i<nCircles+1; i++) {
			for(int j=0; j<nPoints*2; j++) {
				beamCavityIntensityRGBtoColorTEMXYHigh[counter]=beamCavityIntensityRGB[i];
				counter++;
			}
		}

		// pass the colors to VRML
		beamColorsH.setValue(beamCavityIntensityRGBtoColorTEMXYHigh.length,beamCavityIntensityRGBtoColorTEMXYHigh);

		for(int c=0; c<(nCircles+1); c++)
			CircleFactory.genBeamTEM11(w[c], xCoords[c], (nPoints*2)*c, coordsTEM11High, nPoints, nCircles);
		beamPointsH.setValue(coordsTEM11High.length,coordsTEM11High);
	}

	private void initializeIndices() {
		// FOR HIGH RESOLUTION
		// Coordinate Indices
		int p=0;

		// FOR DRAWING THE BEAM
		// Indices for the left circles
		for(int circle=0; circle<CIRCLES_HIGH; circle++) {
			int eight=0+(POINTS_HIGH*(circle+1));
			int nine=1+(POINTS_HIGH*(circle+1));
			int zero=0+(POINTS_HIGH*(circle));
			int one=1+(POINTS_HIGH*(circle));
			for(int q=0; q<POINTS_HIGH; q++) {
				if(q+1==nPoints) {
					indices[p++]=POINTS_HIGH*(circle+1)-1;
					indices[p++]=eight++;
					indices[p++]=(POINTS_HIGH*(circle+1));
					indices[p++]=(POINTS_HIGH*(circle));
					indices[p++]=POINTS_HIGH*(circle+1)-1;
					indices[p++]=-1;
				} else {
					indices[p++]=zero;
					indices[p++]=(eight++);
					indices[p++]=(nine++);
					indices[p++]=(one++);
					indices[p++]=(zero++);
					indices[p++]=-1;
				}
			} // end q<nPoints
		} // end circles < MaxCircles

		// FOR LOW RESOLUTION
		// Coordinate Indices
		p=0;

		// FOR DRAWING THE BEAM
		// Indices for the left circles
		for(int circle=0; circle<CIRCLES_LOW; circle++) {
			int eight=0+(POINTS_LOW*(circle+1));
			int nine=1+(POINTS_LOW*(circle+1));
			int zero=0+(POINTS_LOW*(circle));
			int one=1+(POINTS_LOW*(circle));
			for(int q=0; q<POINTS_LOW; q++) {
				if(q+1==POINTS_LOW) {
					indicesLow[p++]=POINTS_LOW*(circle+1)-1;
					indicesLow[p++]=eight++;
					indicesLow[p++]=(POINTS_LOW*(circle+1));
					indicesLow[p++]=(POINTS_LOW*(circle));
					indicesLow[p++]=POINTS_LOW*(circle+1)-1;
					indicesLow[p++]=-1;
				} else {
					indicesLow[p++]=zero;
					indicesLow[p++]=(eight++);
					indicesLow[p++]=(nine++);
					indicesLow[p++]=(one++);
					indicesLow[p++]=(zero++);
					indicesLow[p++]=-1;
				}
			} // end q<nPoints
		} // end circles < MaxCircles

		// FOR HIGH RESOLUTION -- for tem10 case
		// Coordinate Indices
		p=0;

		// FOR DRAWING THE BEAM
		// Indices for the left circles
		for(int circle=0; circle<CIRCLES_HIGH; circle++) {
			int eight=0+(POINTS_HIGH*2*(circle+1));
			int nine=1+(POINTS_HIGH*2*(circle+1));
			int zero=0+(POINTS_HIGH*2*(circle));
			int one=1+(POINTS_HIGH*2*(circle));
			for(int q=0; q<POINTS_HIGH*2; q++) {
				if(q+1==POINTS_HIGH*2) {
					indicesTEMXYHigh[p++]=POINTS_HIGH*2*(circle+1)-1;
					indicesTEMXYHigh[p++]=eight++;
					indicesTEMXYHigh[p++]=(POINTS_HIGH*2*(circle+1));
					indicesTEMXYHigh[p++]=(POINTS_HIGH*2*(circle));
					indicesTEMXYHigh[p++]=POINTS_HIGH*2*(circle+1)-1;
					indicesTEMXYHigh[p++]=-1;
				} else {
					indicesTEMXYHigh[p++]=zero;
					indicesTEMXYHigh[p++]=(eight++);
					indicesTEMXYHigh[p++]=(nine++);
					indicesTEMXYHigh[p++]=(one++);
					indicesTEMXYHigh[p++]=(zero++);
					indicesTEMXYHigh[p++]=-1;
				}
			} // end q<nPoints
		} // end circles < MaxCircles

		// FOR LOW RESOLUTION -- for tem10 case
		// Coordinate Indices
		p=0;

		//for(int ii=0; ii<((CIRCLES_LOW/2)*6*(POINTS_LOW*2)); ii++)
		//	indices[ii]=0;

		for(int circle=0; circle<CIRCLES_LOW; circle++) {
			int eight=0+((POINTS_LOW*2)*(circle+1));
			int nine=1+((POINTS_LOW*2)*(circle+1));
			int zero=0+((POINTS_LOW*2)*(circle));
			int one=1+((POINTS_LOW*2)*(circle));
			if(circle==CIRCLES_LOW) {
/*				indicesTEMXYLow[p++]=-1;
				indicesTEMXYLow[p++]=-1;
				indicesTEMXYLow[p++]=-1;
				indicesTEMXYLow[p++]=-1;
				indicesTEMXYLow[p++]=-1;
				indicesTEMXYLow[p++]=-1;
*/			} else {
				for(int q=0; q<POINTS_LOW*2; q++) {
					if(q+1==POINTS_LOW*2) {
						indicesTEMXYLow[p++]=(POINTS_LOW*2)*(circle+1)-1;
						indicesTEMXYLow[p++]=eight++;
						indicesTEMXYLow[p++]=((POINTS_LOW*2)*(circle+1));
						indicesTEMXYLow[p++]=((POINTS_LOW*2)*(circle));
						indicesTEMXYLow[p++]=(POINTS_LOW*2)*(circle+1)-1;
						indicesTEMXYLow[p++]=-1;
					} else {
						indicesTEMXYLow[p++]=zero;
						indicesTEMXYLow[p++]=(eight++);
						indicesTEMXYLow[p++]=(nine++);
						indicesTEMXYLow[p++]=(one++);
						indicesTEMXYLow[p++]=(zero++);
						indicesTEMXYLow[p++]=-1;
					}
				} // end q<nPoints
			}
		} // end circles < MaxCircles
	}

	private void initializeFarIndices() {
		int p=0;
		for(int circle=0; circle<nCircles; circle++) {
			int eight=0+(nPoints*(circle+1));
			int nine=1+(nPoints*(circle+1));
			int zero=0+(nPoints*(circle));
			int one=1+(nPoints*(circle));
			for(int q=0; q<nPoints; q++) {
				if(q+1==nPoints) {
					farIndices[p++]=nPoints*(circle+1)-1;
					farIndices[p++]=eight++;
					farIndices[p++]=(nPoints*(circle+1));
					farIndices[p++]=(nPoints*(circle));
					farIndices[p++]=nPoints*(circle+1)-1;
					farIndices[p++]=-1;
				} else {
					farIndices[p++]=zero;
					farIndices[p++]=(eight++);
					farIndices[p++]=(nine++);
					farIndices[p++]=(one++);
					farIndices[p++]=(zero++);
					farIndices[p++]=-1;
				}
			} // end q<nPoints
		} // end circles < MaxCircles

		beamIndicesF.setValue(indices.length,indices);
	}

	private void makeSpot00() {
		//float wlength=(float) (engine.getW0() * (Math.sqrt(1+(Math.pow(((engine.getLength())/engine.getzR()),2)))));
		//float rowlength=(float) (Math.sqrt(Math.pow(0,2) + Math.pow(0,2)));
		//float ILength=(float) (Math.pow((engine.getW0()/wlength), 2) * Math.exp(-2*Math.pow(rowlength,2)/Math.pow(wlength,2)));
		float ILength=(float) (1/(1+Math.pow((engine.getLength()+200f-engine.getL1())/engine.getzR(),2)));

		int counter=0;
		for(float j=0; j<yIFSDim; j++) {
			float y=yIFSStep*j;
			for(float i=0; i<xIFSDim; i++) {
				float x=xIFSStep*i;
				float row=(float) (Math.sqrt(Math.pow(x,2) + Math.pow(y,2)));
				intensityValues[counter++]=(float) (5* (Math.pow(engine.getW0()/wScreen, 2) * Math.exp(-2*Math.pow(row,2)/Math.pow(wScreen,2))) / ILength );
			}
		}
	}

	private void makeSpot01() {
		float ILength=(float) (Math.pow(engine.getzR(),2)/(Math.pow(engine.getzR(),2)+Math.pow(engine.getLength()+200f-engine.getL1(),2)));

		int counter=0;
		for(float j=0; j<yIFSDim; j++) {
			float y=yIFSStep*j;
			for(float i=0; i<xIFSDim; i++) {
				float x=xIFSStep*i;
				float row=(float) (Math.sqrt(Math.pow(x,2) + Math.pow(y,2)));
				intensityValues[counter++]=(float) (5*(Math.pow((engine.getW0()/wScreen), 2) * Math.exp(-2*Math.pow(row,2)/Math.pow(wScreen,2)) * (8*Math.pow(x,2)/Math.pow(wScreen,2))) / ILength);
			}
		}
	} // end makeSpot10()

	private void makeSpot10() {
		float ILength=(float) (Math.pow(engine.getzR(),2)/(Math.pow(engine.getzR(),2)+Math.pow(engine.getLength()+200f-engine.getL1(),2)));

		int counter=0;
		for(float j=0; j<yIFSDim; j++) {
			float y=yIFSStep*j;
			for(float i=0; i<xIFSDim; i++) {
				float x=xIFSStep*i;
				float row=(float) (Math.sqrt(Math.pow(x,2) + Math.pow(y,2)));
				intensityValues[counter++]=(float) (5*(Math.pow((engine.getW0()/wScreen), 2) * Math.exp(-2*Math.pow(row,2)/Math.pow(wScreen,2)) * (8*Math.pow(y,2)/Math.pow(wScreen,2))) / ILength);
			}
		}
	} // end makeSpot01()

	private void makeSpot11() {
		float ILength=(float) (Math.pow(engine.getzR(),2)/(Math.pow(engine.getzR(),2)+Math.pow(engine.getLength()+200f-engine.getL1(),2)));

		int counter=0;
		for(float j=0; j<yIFSDim; j++) {
			float y=yIFSStep*j;
			for(float i=0; i<xIFSDim; i++) {
				float x=xIFSStep*i;
				float row=(float) (Math.sqrt(Math.pow(x,2) + Math.pow(y,2)));
				intensityValues[counter++]=(float) (5*(Math.pow((engine.getW0()/wScreen), 2) * Math.exp(-2*Math.pow(row,2)/Math.pow(wScreen,2)) * (8*Math.pow(x,2)/Math.pow(wScreen,2)) * (8*Math.pow(y,2)/Math.pow(wScreen,2))) / ILength);
			}
		}
	} // end makeSpot11()

	/*private void makeIndices() {
		int numberOfLines=xIFSDim-1;

		int []coordIndices=new int[2*numberOfLines-1];

		for(int i=0; i<2*numberOfLines-1; i++)
			coordIndices[i]=i;

		lineIndices.setValue(coordIndices);
	}*/

	private void updateChoices() {
		SAI.setDraw(laserbeamChoice,lasing);
		//This choice is set by drawBeam()
		//laserbeamResChoice.setValue(aperture_mode*2+(low_res?0:1));
		apertureChoice.setValue(aperture_mode);
		egChoice.setValue(lasing?(low_res?1:0):2);
	}

	public void printAllData() {
		DebugPrinter.println("LaserBeam::printAllData()... xp1=" + xp1+"\txp2=" + xp2);

		DebugPrinter.println("--Values for W--");
		for(int i=0; i<nCircles; i++)
			DebugPrinter.print(w[i] + " ");
		DebugPrinter.print("\n");

		DebugPrinter.println("--Values for coords--");
		for(int i=0; i<nCircles; i++) {
			for(int j=0; j<3; j++) {
				DebugPrinter.print(coords[i][j] + " ");
			}
			DebugPrinter.print("| ");
			if(i%8==0)
				DebugPrinter.print("\n");
		}
	}
}
