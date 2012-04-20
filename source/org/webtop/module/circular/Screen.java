/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Screen.java
//Calculation engine for Fresnel Circular Module (paints the screen).
//Kiril Vidimce (reworked by Davis Herring)
//Updated October 26 2002
//Version 2.0

package org.webtop.module.circular;

import org.web3d.x3d.sai.*;
import org.webtop.x3d.SAI;

import org.webtop.util.*;

class Screen
{
	public static final float DEF_XSTART = 0f,DEF_XEND = 500f,
														DEF_YSTART = 0f,DEF_YEND = 500f;

	private static final int SERIES_TERMS_L1 = 5,
													 SERIES_TERMS_L2 = 7,
													 SERIES_TERMS_L3 = 30;
	private static final int X_RES_HIGH = 121,Y_RES_HIGH = 121,
													 X_RES_LOW = 25,Y_RES_LOW = 25;


	private static final float minIntensity=-0.1f;
	private float maxIntensity=4;

	//The principal parameters; will be initialized by setDefaults()
	public float xStart,xEnd,yStart,yEnd,screenZ,wavelength,radius;

	public boolean isColor=true,
								 isActive=false;	//use low res if so

	Circular applet;

	//Here are things that need to be switched on for high and low resolution
	private int radialResolutionHigh;
	private float maxRadiusHigh;
	private int radialResolutionLow;
	private float maxRadiusLow;
	//We never reallocate these arrays, only reassign
	//The intensities arrays are simultaneously for the intensity plot and for the
	//getIntensity() function -- they have radialResolution* elements because
	//getIntensity() will need them, but only xMeshPoints* of them are used for
	//the plot, and so only they get x coordinates assigned.
	private float[][] radialColorsHigh,colorsHigh;
	private float[][] intensitiesHigh;
	private float[][] radialColorsLow,colorsLow;
	private float[][] intensitiesLow;

	private MFColor set_color,set_colorLow;

	private MFVec3f set_ilsCoord;
	private MFVec3f set_ilsCoordLow;

	public Screen(Circular main) {
		applet=main;

                SAI sai = main.getSAI();
		setDefaults();

		MFVec3f set_point = (MFVec3f)sai.getInputField("coordinateNode", "set_point");
		set_color = (MFColor)sai.getInputField("colorNode", "set_color");
		MFInt32 set_coordIndex = (MFInt32)sai.getInputField("ifsNode", "set_coordIndex");
		MFVec3f set_pointLow = (MFVec3f)sai.getInputField("coordinateNodeLowRes", "set_point");
		set_colorLow = (MFColor)sai.getInputField("colorNodeLowRes", "set_color");
		MFInt32 set_coordIndexLow = (MFInt32)sai.getInputField("ifsNodeLowRes", "set_coordIndex");

		set_ilsCoord = (MFVec3f) sai.getInputField("ilsCoord", "set_point");
		MFInt32 set_ilsIndex = (MFInt32) sai.getInputField("ilsNode", "set_coordIndex");

		set_ilsCoordLow = (MFVec3f) sai.getInputField("ilsCoordLowRes", "set_point");
		MFInt32 set_ilsIndexLow = (MFInt32) sai.getInputField("ilsNodeLowRes", "set_coordIndex");

		//We can already set the grids and arrays up for the screen:
		for(int res=0;res<2;++res) {
			boolean loRes=res==1;

			int xMeshPoints,yMeshPoints,radialResolution;

			// if active, use lower res
			if(loRes) {
				xMeshPoints = X_RES_LOW;
				yMeshPoints = Y_RES_LOW;
			} else {
				xMeshPoints = X_RES_HIGH;
				yMeshPoints = Y_RES_HIGH;
			}
			float xStep=(xEnd-xStart)/xMeshPoints,
						yStep=(yEnd-yStart)/yMeshPoints;

			float[][] intensities;		//pointer to current array
			//We allocate our data arrays here, but we mostly use them in evaluate().

			// Since the screen is rectangular, we need to compute enough points to
			// reach the corners of the screen. We choose 1.5 instead of sqrt(2) for
			// simplicity.
			//Note: this code assumes a square screen! [Davis]
			if(loRes) {
				radialResolution=radialResolutionLow=(int) (1.5f * xMeshPoints);
				maxRadiusLow=radialResolutionLow*xStep;
				intensities=intensitiesLow=new float[radialResolutionLow][3];

				radialColorsLow=new float[radialResolutionLow][3];
				colorsLow=new float[xMeshPoints*yMeshPoints][3];
			} else {
				radialResolution=radialResolutionHigh=(int) (1.5f * xMeshPoints);
				maxRadiusHigh=radialResolutionHigh*xStep;
				intensities=intensitiesHigh=new float[radialResolutionHigh][3];

				radialColorsHigh=new float[radialResolutionHigh][3];
				colorsHigh=new float[xMeshPoints*yMeshPoints][3];
			}

			float[][] points = new float[xMeshPoints*yMeshPoints][];
			int[] cindex = new int[(xMeshPoints - 1) * (yMeshPoints - 1) * 5];
			int[] ils_cindex = new int[xMeshPoints];
			int index = 0;
			for(int iy = 0; iy < yMeshPoints; iy++) {
				for(int ix = 0; ix < xMeshPoints; ix++) {
					points[ix*yMeshPoints+iy]=new float[] {ix*xStep,iy*yStep,0};

					//For the intensity plot (only one ix loop needed)
					if(iy==0) {
						ils_cindex[ix]=ix;
						intensities[ix][0]=ix*xStep;		//z is left at 0; y is calculated in evaluate()
					}

					if(ix==xMeshPoints-1) break;		//last iteration of this loop not used for cindex
					if(iy==yMeshPoints-1) continue;	//nor that of this loop
					cindex[index++] = iy * xMeshPoints + ix;
					cindex[index++] = (iy + 1) * xMeshPoints + ix;
					cindex[index++] = (iy + 1) * xMeshPoints + ix + 1 ;
					cindex[index++] = (iy * xMeshPoints) + ix + 1 ;
					cindex[index++] = -1;
				}
			}

			(loRes?set_coordIndexLow:set_coordIndex).setValue(0,cindex);
			(loRes?set_pointLow:set_point).setValue(0,points);
			(loRes?set_ilsIndexLow:set_ilsIndex).setValue(0,ils_cindex);
		}
	}

	public void setDefaults() {
		xStart=DEF_XSTART;
		xEnd=DEF_XEND;
		yStart=DEF_YSTART;
		yEnd=DEF_YEND;
		screenZ=Circular.Z_DISTANCE_DEFAULT;
		wavelength=Circular.WAVELENGTH_DEFAULT;
		radius=Circular.DIAMETER_DEFAULT/2;
	}

	//This is used to display screen intensities
	public float getIntensity(float r) {
		//Pick arrays and parameters based on resolution:
		float[][] radialColors,colors;
		float[][] intensities;
		int radialResolution;
		float maxRadius;

		if(isActive) {
			intensities=intensitiesLow;
			radialResolution=radialResolutionLow;
			maxRadius=maxRadiusLow;
		} else {
			intensities=intensitiesHigh;
			radialResolution=radialResolutionHigh;
			maxRadius=maxRadiusHigh;
		}

		// compute the real index in our lookup array
		float findex = (r / maxRadius) * radialResolution;
		int iindex = (int) findex;

		if(findex==iindex) {
			return intensities[iindex][1];
		} else {
			//Linearly interpolate between intensities in array
			int ciel=iindex+1;
			return intensities[iindex][1]*(ciel-findex)+intensities[ciel][1]*(findex-iindex);
		}
	}

	//Returns Fresnel Number
	public float evaluate(int mode) {
		if(mode==Circular.APERTURE_MODE)
			maxIntensity=4;
		else if(mode==Circular.OBSTACLE_MODE)
			maxIntensity=6;

		DebugPrinter.println("Screen.evaluate().start");

		// constants
		float
			nanoToMicro	 = 0.001f,
			milliToMicro = 1000;

		int xMeshPoints;
		int yMeshPoints;

		//Pointers to data arrays
		float[][] radialColors,colors;
		float[][] intensities;
		int radialResolution;
		float maxRadius;

		// if active, use lower res
		if(isActive) {
			xMeshPoints = X_RES_LOW;
			yMeshPoints = Y_RES_LOW;
			radialColors=radialColorsLow;
			colors=colorsLow;
			intensities=intensitiesLow;
			radialResolution=radialResolutionLow;
			maxRadius=maxRadiusLow;
		} else {
			xMeshPoints = X_RES_HIGH;
			yMeshPoints = Y_RES_HIGH;
			radialColors=radialColorsHigh;
			colors=colorsHigh;
			intensities=intensitiesHigh;
			radialResolution=radialResolutionHigh;
			maxRadius=maxRadiusHigh;
		}

		float l=wavelength*nanoToMicro,	//wavelength, um
					a=radius*milliToMicro,		//radius, um
					xStep=(float)(xEnd-xStart)/xMeshPoints,
					yStep=(float)(yEnd-yStart)/yMeshPoints,
					hue=WTMath.hue(wavelength);

		// various goodies to speed up the calculations
		float x, y, intensity, _l;
		int ix, iy;

		final float z = screenZ * milliToMicro;
		final float NF = (float) Math.pow(a, 2) / (l * z);
		final float u = 2 * (float) Math.PI * NF;

		// In order to exploit the radial symmetry, we will precompute the
		// intensity values along the x axis, and then traverse a quadrant of
		// the observation screen and lookup the intensity values by computing
		// the distance from the given point to the center (i.e., radius).

		DebugPrinter.println("radialResolution = " + radialResolution);
		DebugPrinter.println("maxRadius = " +maxRadius);
		DebugPrinter.println("Wavelength = " + l + " micrometers");
		DebugPrinter.println("Current z  = " + z + " micrometers");
		DebugPrinter.println("Radius     = " + a + " micrometers");
		DebugPrinter.println("NF         = " + NF);

		for(ix = 0; ix < radialResolution; ix++) {
			x=ix*xStep;
			_l = x / a;

			// determine the number of terms needed based on location on screen
			int expansion = SERIES_TERMS_L1;
			if((0 < _l && _l <= 0.8f) || (_l > 1.2f))
				expansion = SERIES_TERMS_L2;
			else if((0.8f < _l && _l < 1) || (1 < _l && _l <= 1.2f))
				expansion = SERIES_TERMS_L3;

			// compute the intensity
			intensities[ix][1] = intensity = (applet.getMode()==Circular.OBSTACLE_MODE?Circular.OBSTACLE_SCALE:1)*
																			 FresnelMath.computeCircleIntensity(_l, u, expansion, applet.getMode());

			// normalize/bound the intensity
			intensity = WTMath.bound((intensity - minIntensity) / (maxIntensity - minIntensity),0,1);

			float[] target=radialColors[ix];
			//Compute in color or in grayscale
			WTMath.hls2rgb(target,hue,intensity,isColor?WTMath.SATURATION:0);
		}

		for(ix = 0; ix < xMeshPoints; ix++) {
			x=ix*xStep;
			for(iy = 0; iy < yMeshPoints; iy++) {
				y=iy*yStep;

				// compute the radius
				float ro = (float) Math.sqrt(x*x+y*y);

				// compute the real index in our lookup array
				float findex = (ro / maxRadius) * radialResolution;
				int iindex = (int) findex;

				//DebugPrinter.println("At (x="+x+",y="+y+"): findex="+findex);
				float[] curcolor=colors[ix*yMeshPoints+iy];
				if(findex==iindex) {
					float[] lookup=radialColors[iindex];
					curcolor[0]=lookup[0];
					curcolor[1]=lookup[1];
					curcolor[2]=lookup[2];
				} else {
					//Linearly interpolate between colors in array
					int ciel=iindex+1;
					float[] c1=radialColors[iindex],c2=radialColors[ciel];
					curcolor[0]=c1[0]*(ciel-findex)+c2[0]*(findex-iindex);
					curcolor[1]=c1[1]*(ciel-findex)+c2[1]*(findex-iindex);
					curcolor[2]=c1[2]*(ciel-findex)+c2[2]*(findex-iindex);
				}
			}
		}

		(isActive?set_colorLow:set_color).setValue(0,colors);
		(isActive?set_ilsCoordLow:set_ilsCoord).setValue(0,intensities);

		DebugPrinter.println("Screen.evaluate().end");
		return NF;
	}
}
