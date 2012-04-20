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

/***************************************
 * This code will generate circles		 *
 * that are used throughout the engine *
 ***************************************/

public abstract class CircleFactory
{
	public static void genCircle(float radius, float x, int offset, float[][] c, float points) {
		int i=offset;
		int p=0;
		float angle=0;
		float dangle=(float) (2 * Math.PI / (points));

		for(p=0; i<points+offset; i++, angle+=dangle) {
			c[i][p++]=x;
			c[i][p++]=(float) (Math.sin(angle) * radius);
			c[i][p]=(float) (Math.cos(angle) * radius);
			p=0;
		}
	}

	public static void genCircleSamples1(float radius, float[][] c, float points, float mirror_angle) {
		int i=0;
		int p=0;
		float angle=(float) (Math.PI);
		float dangle=(float) ((mirror_angle) / (points));

		//for(i=(int) points-1; i>-1; i--, angle-=dangle) {
			for(; i<points; i++, angle-=dangle) {
			// x=r*cos(theta)
			c[i][p++]=(float) (radius + (Math.cos(angle) * radius));
			// y=r*sin(theta)
			c[i][p++]=(float) ((Math.sin(angle) * radius));
			// z is const
			c[i][p]=0;
			p=0;
		}
	}

	public static void genCircleSamples2(float radius, float[][] c, float points, float mirror_angle, float length) {
		int i=0;
		int p=0;
		// starting at 45 degrees
		float angle=(float) (0);
		float dangle=(float) ((mirror_angle) / (points));

		//for(i=(int) points-1; i>-1; i--, angle-=dangle) {
			for(i=0; i<points; i++, angle+=dangle) {
			// x=r*cos(theta)
			c[i][p++]=(float) ((Math.cos(angle) * radius));
			//c[i][p++]=(float) (radius + (Math.cos(angle) * radius));
			// y=r*sin(theta)
			c[i][p++]=(float) (Math.sin(angle) * radius);
			// z is const
			c[i][p]=0;
			p=0;
		}
	}

	public static void genBeamTEMXY(float w, float x_position, int offset, float[][] c, float points, float max_circles) {
		int i=offset;
		int p=0;
		int q=0;
		float x;
		float x_step=(1.5802f - 0.1324f) / (points-1);

		int nextOffset=(int) (offset + ((points*2)-1));

		// section 1

		for(x=0.1324f,p=0; i<points+offset; i++) {
			c[i][p++]=x_position;
			c[nextOffset][q++]=c[i][p-1];

			c[i][p++]=(float) (Math.pow((2.039721f + Math.log(x) - Math.pow(x,2)),0.5f) * w);
			c[nextOffset][q++]=-1f * c[i][p-1];

			c[i][p]=(float) (x*w);
			c[nextOffset][q]=c[i][p];

			p=0;
			q=0;
			x+=x_step;
			nextOffset--;
		}
	}

	public static void genBeamTEM11(float w, float x_position, int offset, float[][] c, float points, float max_circles) {
		int i=offset;
		int p=0;
		int q=0;
		float r;
		float r_step=(1.9192f - 0.3192f) / (points-1);

		int nextOffset=(int) (offset + ((points*2)-1));

		// section 1

		for(r=0.3192f,p=0; i<points+offset; i++) {
			c[i][p++]=x_position;
			c[nextOffset][q++]=c[i][p-1];

			//c[i][p++]=(float) (Math.pow((2.039721f + Math.log(x) - Math.pow(x,2)),0.5f) * w);
			float theta_r=(float) (0.5f * Math.asin((Math.exp(Math.pow(r,2)-1))/(4*Math.pow(r,2))) );
			c[i][p++]=(float) ((r*Math.sin(theta_r)) * w);
			c[nextOffset][q++]=(float) ((r*Math.cos(theta_r)) * w);

			c[i][p]=(float) ((r*Math.cos(theta_r)) * w);
			c[nextOffset][q]=(float) ((r*Math.sin(theta_r)) * w);

			p=0;
			q=0;
			r+=r_step;
			nextOffset--;
		}
	}
}
