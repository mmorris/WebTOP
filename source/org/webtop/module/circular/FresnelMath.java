/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//FresnelMath.java
//Mathematical functions for Fresnel diffraction.
//Kiril Vidimce (edited by Davis Herring)
//Updated October 26 2002
//Version 2.0

//This file may be redundant/need refactoring. [Davis]

package org.webtop.module.circular;

import org.webtop.util.*;

class FresnelMath
{
	public static float computeCircleIntensity(float _l, float u, int nMax, int mode) {
		if(_l == 0) {
			if(mode==Circular.APERTURE_MODE)
				return(4 * (float) Math.pow(Math.sin(u / 4), 2));
			else if(mode==Circular.OBSTACLE_MODE)
				return(1);
		}

		float v=u*_l;
		if(_l >= 0 && _l < 1) {
			float sub = (float) (Math.pow(u, 2) + Math.pow(v, 2)) / (2 * u);
			if(mode==Circular.APERTURE_MODE)
				return (float) (Math.pow(functionV(0, u, v, nMax) - Math.cos(sub), 2) +
							Math.pow(functionV(1, u, v, nMax) - Math.sin(sub), 2));
			else if(mode==Circular.OBSTACLE_MODE)
				return (float) (Math.pow(functionV(0, u, v, nMax), 2) +
							Math.pow(functionV(1, u, v, nMax), 2));
		} else if(_l == 1) {
			if(mode==Circular.APERTURE_MODE)
				return (float) ((1 - 2 * WTMath.j0(v) * Math.cos(v) + Math.pow(WTMath.j0(v), 2)) / 4);
			else if(mode==Circular.OBSTACLE_MODE)
				return (float) ((1 + 2 * WTMath.j0(v) * Math.cos(v) + Math.pow(WTMath.j0(v), 2)) / 4);
		} else {
			if(mode==Circular.APERTURE_MODE)
				return (float) (Math.pow(functionU(1, u, v, nMax), 2) +
								Math.pow(functionU(2, u, v, nMax), 2));
			else if(mode==Circular.OBSTACLE_MODE) {
					 float sub = (float) (Math.pow(u, 2) + Math.pow(v, 2)) / (2 * u);
					 return (float) (Math.pow( (functionU(1, u, v, nMax) - Math.sin(sub) ), 2) +
									 Math.pow( (functionU(2, u, v, nMax) + Math.cos(sub) ), 2));
				 }
		}
		//peter
		return(1);
	}

	public static float functionV(float m, float u,
									float v, int nMax) {
		int series;
		float sum = 0;

		for(int n = 0; n < nMax; n++) {
			series = (int) (m + 2 * n);
			sum += Math.pow(-1, n) * Math.pow(v / u, series) * WTMath.jn(series, v);
		}

		return sum;
	}

	public static float functionU(float m, float u,
									float v, int nMax) {
		int series;
		float sum = 0;

		for(int n = 0; n < nMax; n++) {
			series = (int) (m + 2 * n);
			sum += Math.pow(-1, n) * Math.pow(u / v, series) * WTMath.jn(series, v);
		}

		return sum;
	}
}
