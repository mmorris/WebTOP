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


public class Mirror
{
	public static final float MIRROR_RADIUS=1.27f;

	private MFVec3f CirclesForMirror;
	private MFInt32 CirclesForIndex;
	private SFVec3f ScaleMirror;
	private SFBool FlipMirror;

	private float xmin=0;
	private float xmax_circle=0;
	private float radius_mirror=0;
	static final private int POINTS_Mirror=20;
	static final public int MAX_CIRCLES_Mirror=5;

	private float scriptR=0;
	private float mirror_angle=0;

	private float scaleFactor=1.01f;

	private float coordMirror[][];
	private	float sampleMirror[][];
	private int indicesMirror[];

	public Mirror(SAI sai) {
		CirclesForMirror=(MFVec3f) sai.getInputField("Mirror1-COORD","set_point");
		CirclesForIndex=(MFInt32) sai.getInputField("Mirror1-IFS","set_coordIndex");
		ScaleMirror=(SFVec3f) sai.getInputField("Mirror1Transform","set_scale");
		FlipMirror=(SFBool) sai.getInputField("Mirror1Flip","flip");

		coordMirror=new float[POINTS_Mirror*MAX_CIRCLES_Mirror+1][];
		for(int i=0; i<(POINTS_Mirror*MAX_CIRCLES_Mirror+1); i++) {
			coordMirror[i]=new float[3];
			for(int j=0; j<3; j++) {
				coordMirror[i][j]=0;
			}
		}

		sampleMirror=new float[MAX_CIRCLES_Mirror][];
		for(int i=0; i<(MAX_CIRCLES_Mirror); i++) {
			sampleMirror[i]=new float[3];
			for(int j=0; j<3; j++) {
				sampleMirror[i][j]=0;
			}
		}
	}

	public void drawMirror(float radius_mirror_, float mirror_angle_) {
		radius_mirror=radius_mirror_;
		mirror_angle=mirror_angle_;

		indicesMirror=new int[((MAX_CIRCLES_Mirror-1)*6*POINTS_Mirror)+
									(5*POINTS_Mirror)];
		for(int ii=0; ii<((MAX_CIRCLES_Mirror-1)*6*POINTS_Mirror)+(5*POINTS_Mirror); ii++) {
			indicesMirror[ii]=0;
		}

		// indices from 0 to 40
		int p=0;
		int two=2;
		int one=1;
		for(int q=0; q<POINTS_Mirror; q++) {
			if(q+1==POINTS_Mirror) {
				indicesMirror[p++]=0;
				indicesMirror[p++]=POINTS_Mirror;
				indicesMirror[p++]=1;
				indicesMirror[p++]=0;
				indicesMirror[p++]=-1;
			} else {
				indicesMirror[p++]=0;
				indicesMirror[p++]=one++;
				indicesMirror[p++]=two++;
				indicesMirror[p++]=0;
				indicesMirror[p++]=-1;
			}
		}

		for(int circle=1; circle< MAX_CIRCLES_Mirror; circle++) {
			if(circle==1) p=POINTS_Mirror*5;

			int nine=1+(POINTS_Mirror*circle);
			int ten=2+(POINTS_Mirror*circle);
			one=1+(POINTS_Mirror*(circle-1));
			two=2+(POINTS_Mirror*(circle-1));
			for(int q=0; q<POINTS_Mirror; q++) {
				if(q+1==POINTS_Mirror) {
					indicesMirror[p++]=POINTS_Mirror*circle;
					indicesMirror[p++]=nine++;
					indicesMirror[p++]=1+(POINTS_Mirror*circle);
					indicesMirror[p++]=1+(POINTS_Mirror*(circle-1));
					indicesMirror[p++]=POINTS_Mirror*circle;
					indicesMirror[p++]=-1;
				} else {
					indicesMirror[p++]=one;
					indicesMirror[p++]=(nine++);
					indicesMirror[p++]=(ten++);
					indicesMirror[p++]=(two++);
					indicesMirror[p++]=(one++);
					indicesMirror[p++]=-1;
				}
			}
		}

		CircleFactory.genCircleSamples1(radius_mirror, sampleMirror, MAX_CIRCLES_Mirror, mirror_angle);

		for(int ii=0; ii<MAX_CIRCLES_Mirror; ii++) {
			CircleFactory.genCircle(sampleMirror[ii][1], sampleMirror[ii][0], 1+POINTS_Mirror*ii, coordMirror, POINTS_Mirror);
		}

		CirclesForMirror.setValue(coordMirror.length,coordMirror);
		CirclesForIndex.setValue(indicesMirror.length,indicesMirror);
	}

	public void scaleMirror(float value) {
		DebugPrinter.println("Mirror::scaleMirror(" + value+")");
		value=scaleFactor - (value/2);
		DebugPrinter.println("...value'=" + value);
		float arra[]=new float [3];
		arra[0]=value;
		arra[1]=1;
		arra[2]=1;
		ScaleMirror.setValue(arra);
	}

	public void setDefaults() {
		float arra[]=new float [3];
		arra[0]=1;
		arra[1]=1;
		arra[2]=1;
		ScaleMirror.setValue(arra);
	}

	public float getMirror_angle() {
		return mirror_angle;
	}

	public float[][] getXCoordsMirror() {
		return sampleMirror;
	}

	public int[] getIndices() {
		return indicesMirror;
	}

	public float getRadius() {
		return radius_mirror;
	}
}
