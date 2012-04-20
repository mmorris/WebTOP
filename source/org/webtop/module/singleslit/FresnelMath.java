/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

// Author(s): Kiril Vidimce
package org.webtop.module.singleslit;

import org.webtop.util.WTMath;

class FresnelMath
{
	private final static int CP = 0,
													 SP = 1;

	public static float computeSlitIntensity(float w1, float w2) {
		return (float) ((Math.pow(func_C(w2) - func_C(w1), 2.0f) +
										 Math.pow(func_S(w2) - func_S(w1), 2.0f)) / 2.0f);
	}

	private static float func_C(float u) {
		return(u < 0.0f ? -func_CP_SP(-u, SP) : func_CP_SP(u, SP));
	}

	private static float func_S(float u) {
		return(u < 0.0f ? -func_CP_SP(-u, CP) : func_CP_SP(u, CP));
	}

	private static float func_CP_SP(float u, int which) {
		float sine = (float) Math.sin(0.5f * Math.PI * u * u);
		float cosine = (float) Math.cos(0.5f * Math.PI * u * u);

		if(which != 0)
			return(0.5f + func_f(u) * sine - func_g(u) * cosine);
		else
			return(0.5f - func_f(u) * cosine - func_g(u) * sine);
	}

	private static float func_g(float u) {
		return(1.0f / (2.0f + 4.142f * u + 3.492f * u * u + 6.670f * u * u * u));
	}

	private static float func_f(float u) {
		return((1.0f + 0.926f * u)/(2.0f + 1.792f * u + 3.104f * u * u));
	}

	//This stuff needs to find a home, or else be rid of [Davis]
	/*public static float computeCircleIntensity(float _l, float u, float v, int nMax) {
		if(_l == 0.0)
			return(4.0f * (float) Math.pow(Math.sin(u / 4.0f), 2.0f));
		else if(_l > 0.0 && _l < 1.0) {
			float sub = (float) (Math.pow(u, 2.0) + Math.pow(v, 2.0f)) / (2.0f * u);
			return (float)(Math.pow(functionV(0, u, v, nMax) - Math.cos(sub), 2.0) +
										 Math.pow(functionV(1, u, v, nMax) - Math.sin(sub), 2.0));
		} else if(_l == 1.0)
			return (float) ((1.0f - 2.0f * WTMath.j0(v) *
											 Math.cos(v) + Math.pow(WTMath.j0(v), 2)) / 4.0);
		else
			return (float) (Math.pow(functionU(1, u, v, nMax), 2.0) +
											Math.pow(functionU(2, u, v, nMax), 2.0));
		// return 0.0f;
	}

	public static float computeTwoSlitIntensity(float w1, float w2,
																							float w3, float w4) {
		return (float) ((Math.pow(func_C(w2) - func_C(w1) + func_C(w4) -
															func_C(w3), 2) + Math.pow(func_S(w2) - func_S(w1)
																												+ func_S(w4) - func_S(w3), 2)) / 2.0f);
	}

	private static float functionV(float m, float u,
																float v, int nMax) {
		//	 const int nMax = _DEFAULT_CIRCLE_SERIES_;
		int series;
		float sum = 0.0f;

		for(int n = 0; n < nMax; n++) {
			series = (int) (m + 2 * n);
			sum += Math.pow(-1.0f, n) * Math.pow(v / u, series) * WTMath.jn(series, v);
		}

		return sum;
		// return 0.0f;
	}

	private static float functionU(float m, float u,
																float v, int nMax) {
		//	 const int nMax = _DEFAULT_CIRCLE_SERIES_;
		int series;
		float sum = 0.0f;

		for(int n = 0; n < nMax; n++) {
			series = (int) (m + 2 * n);
			sum += Math.pow(-1.0f, n) * Math.pow(u / v, series) * WTMath.jn(series, v);
		}

		return sum;
		// return 0.0f;
	}*/
}
