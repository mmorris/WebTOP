/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//ApertureEngine.java
//Creates the polygons for the circular aperture
//Kiril Vidimce and Ming-Hoe Kiu (edited by Davis Herring and Peter Gilbert)
//Updated February 10 2004
//Version 3.01

package org.webtop.module.circular;

import org.web3d.x3d.sai.*;
import org.webtop.x3d.SAI;
import org.webtop.util.*;

class ApertureEngine
{
	public int	 segments;
	//All dimensions in millimeters
	public float width,
							 height,
							 thickness,
							 radius;
	
	public WTFloat WTRadius;

	MFVec3f set_point;
	MFInt32 set_pointIndex;

	public ApertureEngine(SAI sai, WTFloat CirRadius) {
		setDefaults();
		set_point = (MFVec3f)sai.getInputField("apertureCoordNode","point");
		set_pointIndex = (MFInt32)sai.getInputField("apertureIFSNode","set_coordIndex");
		WTRadius = CirRadius;		
	}

	public void setDefaults() {
		segments	= 40;
		width			= 1;
		height		= 1;
		thickness = 0.002f;
		radius		= 0.375f;
	}

	public void evaluate(int mode) {
		DebugPrinter.println("ApertureEngine.evaluate().start");

		float[] point = new float[3];
		
		radius = WTRadius.getValue();

		// clamp the radius
		if(radius > width / 2)
			radius = width / 2;
		if(radius > height / 2)
			radius = height / 2;
		if(radius < 0)
			radius = 0;

		float[][] vertices = new float[segments * 2 + 8][3];

		final float dt = (float) Math.PI * 2 / (float) segments;
		for(int i = 0; i < segments; i++) {
			float theta = i * dt;
			vertices[i][0]=vertices[i+segments][0]=(float)Math.sin(theta)*radius;
			vertices[i][1]=vertices[i+segments][1]=(float)Math.cos(theta)*radius;
			vertices[i+segments][2]=-(vertices[i][2]=thickness/2);
		}

		if(mode==Circular.APERTURE_MODE) {
			//Front
			// top right corner
			int vertex = segments * 2;
			vertices[vertex][0] = width/2;
			vertices[vertex][1] = height/2;
			vertices[vertex][2] = thickness/2;
			++vertex;

			// bottom right corner
			vertices[vertex][0] = width/2;
			vertices[vertex][1] = -height/2;
			vertices[vertex][2] = thickness/2;
			++vertex;

			// bottom left corner
			vertices[vertex][0] = -width/2;
			vertices[vertex][1] = -height/2;
			vertices[vertex][2] = thickness/2;
			++vertex;

			// top left corner
			vertices[vertex][0] = -width/2;
			vertices[vertex][1] = height/2;
			vertices[vertex][2] = thickness/2;
			++vertex;

			//Back
			// top right corner
			vertices[vertex][0] = width/2;
			vertices[vertex][1] = height/2;
			vertices[vertex][2] = -thickness/2;
			++vertex;

			// bottom right corner
			vertices[vertex][0] = width/2;
			vertices[vertex][1] = -height/2;
			vertices[vertex][2] = -thickness/2;
			++vertex;

			// bottom left corner
			vertices[vertex][0] = -width/2;
			vertices[vertex][1] = -height/2;
			vertices[vertex][2] = -thickness/2;
			++vertex;

			// top left corner
			vertices[vertex][0] = -width/2;
			vertices[vertex][1] = height/2;
			vertices[vertex][2] = -thickness/2;
		}

		int resolution = segments * 5 + (segments + 4) * 4;
		int[] vindex = new int[resolution];

		// Quads for inner part of the aperture
		int index = 0;
		for(int i = 0; i < segments - 1; i++) {
			vindex[index++] = i + 1 + segments;
			vindex[index++] = i + 1;
			vindex[index++] = i;
			vindex[index++] = i + segments;
			vindex[index++] = -1;
		}

		vindex[index++] = segments;
		vindex[index++] = 0;
		vindex[index++] = segments - 1;
		vindex[index++] = 2 * segments - 1;
		vindex[index++] = -1;

		// triangles
		int idx1 = 0, idx2 = 0;
		for(int i = 0; i < segments; i++) {
			vindex[index++] = i;
			idx1 = (i == segments - 1) ? 0 : i + 1;
			vindex[index++] = idx1;
			idx1 = i * 4 / segments + segments * 2;
			vindex[index++] = idx1;
			vindex[index++] = -1;
			if(i % (segments / 4) == 0) {
				if(i == 0) idx2 = segments * 2 + 3;
				else idx2 = idx1 - 1;
				vindex[index++] = i;
				vindex[index++] = idx1;
				vindex[index++] = idx2;
				vindex[index++] = -1;
			}
		}

		set_point.setValue(vertices.length,vertices);
		set_pointIndex.setValue(vindex.length,vindex);

		DebugPrinter.println("resolution = " + resolution);
		DebugPrinter.println("indices    = " + index);
		DebugPrinter.println("indices    = " + index);

		DebugPrinter.println("ApertureEngine.evaluate().end");
	}
}
