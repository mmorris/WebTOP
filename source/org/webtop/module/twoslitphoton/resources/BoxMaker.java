////////////////////////////////////////////////////////////////////////////
//													 The Optics Project
//													(top@cs.msstate.edu)
//	 NSF/Mississippi State University Engineering Research Center for CFS
//					 Copyright (C) 1996-2002 Mississippi State University
//
// Author(s): Kiril Vidimce (vkire@erc.msstate.edu)
//						Rachel Mueller (rcm9@ra.msstate.edu)
//
// $Id: BoxMaker.java,v 1.2 2001/07/12 16:24:53 tze Exp $
////////////////////////////////////////////////////////////////////////////

package webtop.twoslit;

import vrml.external.field.*;

import webtop.util.DebugPrinter;
import webtop.vrml.EAI;

public class BoxMaker
{
	////////////////////////////////////////////////////////////////////////
	// public input fields/parameters that control the computation engine
	//
	public static final float
		width				 = 1000.0f,								// (width / 2.0f) of the box
		height			 =	300.0f,								// height of the box
		depth				 =		0.1f;								// depth of the box

	public float
		distance	 =	250.0f,		// distance between centers of two consecutive slits
		slitWidth	 =	 40.0f;		// (width / 2.0f) of the slit(s)

	public int n = 2;												// the number of slits
	//
	// end of input fields/parameters
	////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////
	// Pointers to the VRML events that the engine uses.
	private EventInMFVec3f m_set_point;
	private EventInMFVec3f m_set_normal;
	private EventInMFInt32 m_set_coordIndex;
	private EventInMFInt32 m_set_normalIndex;
	////////////////////////////////////////////////////////////////////////

	float []p0 = new float[3];
	float []p1 = new float[3];
	float []p2 = new float[3];
	float []p3 = new float[3];
	float []p4 = new float[3];
	float []p5 = new float[3];
	float []p6 = new float[3];
	float []p7 = new float[3];

	int
		[]index = new int[4];

	float [][]normal = new float[6][3];

	////////////////////////////////////////////////////////////////////
	public BoxMaker(EAI eai) {
		///////////////////////////////////////////////////////////////////
		// Normals
		///////////////////////////////////////////////////////////////////
		//
		// front
		normal[0] = new float[3];
		normal[0][0] = 0.0f;
		normal[0][1] = 0.0f;
		normal[0][2] = 1.0f;

		// back
		normal[1] = new float[3];
		normal[1][0] = 0.0f;
		normal[1][1] = 0.0f;
		normal[1][2] = -1.0f;

		// left
		normal[2] = new float[3];
		normal[2][0] = -1.0f;
		normal[2][1] =	0.0f;
		normal[2][2] =	0.0f;

		// left
		normal[3] = new float[3];
		normal[3][0] =	1.0f;
		normal[3][1] =	0.0f;
		normal[3][2] =	0.0f;

		// top
		normal[4] = new float[3];
		normal[4][0] =	0.0f;
		normal[4][1] =	1.0f;
		normal[4][2] =	0.0f;

		// bottom
		normal[5] = new float[3];
		normal[5][0] =	0.0f;
		normal[5][1] = -1.0f;
		normal[5][2] =	0.0f;

		// the events
		m_set_point = (EventInMFVec3f) eai.getEI("slitCoordinateNode","set_point");

		// coordIndices
		m_set_coordIndex =
			(EventInMFInt32) eai.getEI("slitIFSNode","set_coordIndex");
		m_set_normalIndex =
			(EventInMFInt32) eai.getEI("slitIFSNode","set_normalIndex");
	}

	private void
	makeBox(float []p0, float []p1, float []p2, float []p3,
					float []p4, float []p5, float []p6, float []p7,
					float [][]point, float [][]normal,
					int []coordIndices, int []normalIndices,
					int []index) {
		///////////////////////////////////////////////////////////////////
		//
		//		 6--------------7
		//		/|						 /|
		//	 / |						/ |
		//	3--------------2	|
		//	|	 |					 |	|
		//	|	 |					 |	|
		//	|	 |					 |	|
		//	|	 |					 |	|
		//	|	 5-----------|--4
		//	| /						 | /
		//	|/						 |/
		//	0--------------1
		//
		DebugPrinter.println(p0[0] + ", " + p0[1] + ", " + p0[2]);
		DebugPrinter.println(p1[0] + ", " + p1[1] + ", " + p1[2]);
		DebugPrinter.println(p2[0] + ", " + p2[1] + ", " + p2[2]);
		DebugPrinter.println(p3[0] + ", " + p3[1] + ", " + p3[2]);
		DebugPrinter.println(p4[0] + ", " + p4[1] + ", " + p4[2]);
		DebugPrinter.println(p5[0] + ", " + p5[1] + ", " + p5[2]);
		DebugPrinter.println(p6[0] + ", " + p6[1] + ", " + p6[2]);
		DebugPrinter.println(p7[0] + ", " + p7[1] + ", " + p7[2]);
		DebugPrinter.println("i0 = " + index[0]);
		DebugPrinter.println("i1 = " + index[1]);
		DebugPrinter.println("i2 = " + index[2]);
		DebugPrinter.println("i3 = " + index[3]);

		int
			pointIndex	= index[0],
			normalIndex = index[2];

		///////////////////////////////////////////////////////////////////
		// Vertices
		///////////////////////////////////////////////////////////////////
		// p0
		point[index[0]] = new float[3];
		point[index[0]][0] = p0[0];
		point[index[0]][1] = p0[1];
		point[index[0]][2] = p0[2];
		index[0]++;

		///////////////////////////////////////////////////////////////////
		// p1
		point[index[0]] = new float[3];
		point[index[0]][0] = p1[0];
		point[index[0]][1] = p1[1];
		point[index[0]][2] = p1[2];
		index[0]++;

		///////////////////////////////////////////////////////////////////
		// p2
		point[index[0]] = new float[3];
		point[index[0]][0] = p2[0];
		point[index[0]][1] = p2[1];
		point[index[0]][2] = p2[2];
		index[0]++;

		///////////////////////////////////////////////////////////////////
		// p3
		point[index[0]] = new float[3];
		point[index[0]][0] = p3[0];
		point[index[0]][1] = p3[1];
		point[index[0]][2] = p3[2];
		index[0]++;

		///////////////////////////////////////////////////////////////////
		// p4
		point[index[0]] = new float[3];
		point[index[0]][0] = p4[0];
		point[index[0]][1] = p4[1];
		point[index[0]][2] = p4[2];
		index[0]++;

		///////////////////////////////////////////////////////////////////
		// p5
		point[index[0]] = new float[3];
		point[index[0]][0] = p5[0];
		point[index[0]][1] = p5[1];
		point[index[0]][2] = p5[2];
		index[0]++;

		///////////////////////////////////////////////////////////////////
		// p6
		point[index[0]] = new float[3];
		point[index[0]][0] = p6[0];
		point[index[0]][1] = p6[1];
		point[index[0]][2] = p6[2];
		index[0]++;

		///////////////////////////////////////////////////////////////////
		// p7
		point[index[0]] = new float[3];
		point[index[0]][0] = p7[0];
		point[index[0]][1] = p7[1];
		point[index[0]][2] = p7[2];
		index[0]++;
		//
		///////////////////////////////////////////////////////////////////
/*

		///////////////////////////////////////////////////////////////////
		// Normals
		///////////////////////////////////////////////////////////////////
		//
		// front
		normal[index[3]] = new float[3];
		normal[index[3]][0] = 0.0f;
		normal[index[3]][1] = 0.0f;
		normal[index[3]][2] = 1.0f;
		index[3]++;

		// back
		normal[index[3]] = new float[3];
		normal[index[3]][0] = 0.0f;
		normal[index[3]][1] = 0.0f;
		normal[index[3]][2] = -1.0f;
		index[3]++;

		// left
		normal[index[3]] = new float[3];
		normal[index[3]][0] = -1.0f;
		normal[index[3]][1] =	 0.0f;
		normal[index[3]][2] =	 0.0f;
		index[3]++;

		// left
		normal[index[3]] = new float[3];
		normal[index[3]][0] =	 1.0f;
		normal[index[3]][1] =	 0.0f;
		normal[index[3]][2] =	 0.0f;
		index[3]++;

		// top
		normal[index[3]] = new float[3];
		normal[index[3]][0] =	 0.0f;
		normal[index[3]][1] =	 1.0f;
		normal[index[3]][2] =	 0.0f;
		index[3]++;

		// bottom
		normal[index[3]] = new float[3];
		normal[index[3]][0] =	 0.0f;
		normal[index[3]][1] = -1.0f;
		normal[index[3]][2] =	 0.0f;
		index[3]++;
*/
		//
		///////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////
		// We will create quads in a counter-clockwise fashion (0, 1, 2, 3)
		///////////////////////////////////////////////////////////////////
		// front
		coordIndices[index[1]++] = pointIndex + 0;
		coordIndices[index[1]++] = pointIndex + 1;
		coordIndices[index[1]++] = pointIndex + 2;
		coordIndices[index[1]++] = pointIndex + 3;
		coordIndices[index[1]++] = -1;

		// back
		coordIndices[index[1]++] = pointIndex + 4;
		coordIndices[index[1]++] = pointIndex + 5;
		coordIndices[index[1]++] = pointIndex + 6;
		coordIndices[index[1]++] = pointIndex + 7;
		coordIndices[index[1]++] = -1;

		// left
		coordIndices[index[1]++] = pointIndex + 5;
		coordIndices[index[1]++] = pointIndex + 0;
		coordIndices[index[1]++] = pointIndex + 3;
		coordIndices[index[1]++] = pointIndex + 6;
		coordIndices[index[1]++] = -1;

		// right
		coordIndices[index[1]++] = pointIndex + 1;
		coordIndices[index[1]++] = pointIndex + 4;
		coordIndices[index[1]++] = pointIndex + 7;
		coordIndices[index[1]++] = pointIndex + 2;
		coordIndices[index[1]++] = -1;

		// top
		coordIndices[index[1]++] = pointIndex + 3;
		coordIndices[index[1]++] = pointIndex + 2;
		coordIndices[index[1]++] = pointIndex + 7;
		coordIndices[index[1]++] = pointIndex + 6;
		coordIndices[index[1]++] = -1;

		// bottom
		coordIndices[index[1]++] = pointIndex + 5;
		coordIndices[index[1]++] = pointIndex + 4;
		coordIndices[index[1]++] = pointIndex + 1;
		coordIndices[index[1]++] = pointIndex + 0;
		coordIndices[index[1]++] = -1;

		// front
		normalIndex = 0;
		normalIndices[index[2]++] = normalIndex + 0;

		// back
		normalIndices[index[2]++] = normalIndex + 1;

		// left
		normalIndices[index[2]++] = normalIndex + 2;

		// right
		normalIndices[index[2]++] = normalIndex + 3;

		// top
		normalIndices[index[2]++] = normalIndex + 4;

		// bottom
		normalIndices[index[2]++] = normalIndex + 5;
		//
		///////////////////////////////////////////////////////////////////
	}

	public static float totWidth(int n,float width,float dist) {
		return (n * width + (n - 1) * (dist - width));
	}

	////////////////////////////////////////////////////////////////////
	// This is where the engine computes the polygons for the box.
	// Returns true upon successful completion and false if the given
	// parameters generate an aperture that exceeds the given (width / 2.0f) constraints
	public boolean evaluate() {
		DebugPrinter.println("n = " + n);
		DebugPrinter.println("BoxCreate.evaluate().begin");

		////////////////////////////////////////////////////////////////////
		// check if the number of slits does not create an aperture greater
		// than the given constraints
		DebugPrinter.println("n = " + n);
		DebugPrinter.println("slitWidth = " + slitWidth);
		DebugPrinter.println("distance = " + distance);
		DebugPrinter.println("width = " + width);
		DebugPrinter.println("= " + totWidth(n,slitWidth,distance));
		if(totWidth(n,slitWidth,distance) > width) {
			return false;
		}

		////////////////////////////////////////////////////////////////////
		// allocate memory for the box vertices and normal vectors
		float [][]point	 = new float[8 * (n + 1)][3];
		int []coordIndices	= new int[30 * (n + 1)];
		int []normalIndices = new int[ 6 * (n + 1)];
		//float [][]point	 = new float[8 * 2][3];
		//float [][]normal = new float[6 * 2][3];
		//int []coordIndices	= new int[30 * 2];
		//int []normalIndices = new int[6 * 2];
		float tmp;

		///////////////////////////////////////////////////////////////////
		// initialize the indices
		index[0] = index[1] = index[2] = index[3] = 0;

		///////////////////////////////////////////////////////////////////
		//
		//		 6--------------7
		//		/|						 /|
		//	 / |						/ |
		//	3--------------2	|
		//	|	 |					 |	|
		//	|	 |					 |	|
		//	|	 |					 |	|
		//	|	 |					 |	|
		//	|	 5-----------|--4
		//	| /						 | /
		//	|/						 |/
		//	0--------------1
		//
		///////////////////////////////////////////////////////////////////
		// Create outer edge of aperture on negative x-axis
		///////////////////////////////////////////////////////////////////
		p0[0] = -(width / 2.0f);
		p0[1] = -height / 2.0f;
		p0[2] =	 depth / 2.0f;

		p1[0] =	 (n - 1) * (-distance / 2.0f) - (slitWidth / 2.0f);
		p1[1] =	 p0[1];
		p1[2] =	 p0[2];

		p2[0] =	 p1[0];
		p2[1] = -p0[1];
		p2[2] =	 p0[2];

		p3[0] =	 p0[0];
		p3[1] = -p0[1];
		p3[2] =	 p0[2];

		p4[0] =	 p1[0];
		p4[1] =	 p0[1];
		p4[2] = -p0[2];

		p5[0] =	 p0[0];
		p5[1] =	 p0[1];
		p5[2] = -p0[2];

		p6[0] =	 p0[0];
		p6[1] = -p0[1];
		p6[2] = -p0[2];

		p7[0] =	 p1[0];
		p7[1] = -p0[1];
		p7[2] = -p0[2];

		makeBox(p0, p1, p2, p3, p4, p5, p6, p7,
						point, normal, coordIndices, normalIndices, index);
		//
		///////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////
		// Create outer edge of aperture on positive x-axis
		///////////////////////////////////////////////////////////////////
		tmp = p0[0];
		p0[0] = -p1[0];
		p1[0] = -tmp;
		p2[0] =	 p1[0];
		p3[0] =	 p0[0];
		p4[0] =	 p1[0];
		p5[0] =	 p0[0];
		p6[0] =	 p0[0];
		p7[0] =	 p1[0];

		makeBox(p0, p1, p2, p3, p4, p5, p6, p7,
						point, normal, coordIndices, normalIndices, index);
		//
		///////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////
		// case of even number of slits
		if(n % 2 == 0) {
			// Create outer edge of aperture on positive x-axis
			///////////////////////////////////////////////////////////////////
			p0[0] = -(distance / 2.0f) + (slitWidth / 2.0f);
			p0[1] = -height / 2.0f;
			p0[2] =	 depth / 2.0f;

			p1[0] =	 (distance / 2.0f) - (slitWidth / 2.0f);
			p1[1] =	 p0[1];
			p1[2] =	 p0[2];

			p2[0] =	 p1[0];
			p2[1] = -p0[1];
			p2[2] =	 p0[2];

			p3[0] =	 p0[0];
			p3[1] = -p0[1];
			p3[2] =	 p0[2];

			p4[0] =	 p1[0];
			p4[1] =	 p0[1];
			p4[2] = -p0[2];

			p5[0] =	 p0[0];
			p5[1] =	 p0[1];
			p5[2] = -p0[2];

			p6[0] =	 p0[0];
			p6[1] = -p0[1];
			p6[2] = -p0[2];

			p7[0] =	 p1[0];
			p7[1] = -p0[1];
			p7[2] = -p0[2];

			makeBox(p0, p1, p2, p3, p4, p5, p6, p7,
							point, normal, coordIndices, normalIndices, index);
			//
			///////////////////////////////////////////////////////////////////

			// Create n-1 inner boxes
			for(int i = 3; i < (n + 4) / 2; i++) {
				///////////////////////////////////////////////////////////////////
				// Create aperture on positive x-axis
				///////////////////////////////////////////////////////////////////
				// Vertices
				p0[0] =	 (2 * i - 5) * (distance / 2.0f) + (slitWidth / 2.0f);
				p0[1] = -height / 2.0f;
				p0[2] =	 depth / 2.0f;

				p1[0] =	 (2 * i - 5) * (distance / 2.0f) + (slitWidth / 2.0f) +
								 (distance - slitWidth);
				p1[1] =	 p0[1];
				p1[2] =	 p0[2];

				p2[0] =	 p1[0];
				p2[1] = -p0[1];
				p2[2] =	 p0[2];

				p3[0] =	 p0[0];
				p3[1] = -p0[1];
				p3[2] =	 p0[2];

				p4[0] =	 p1[0];
				p4[1] =	 p0[1];
				p4[2] = -p0[2];

				p5[0] =	 p0[0];
				p5[1] =	 p0[1];
				p5[2] = -p0[2];

				p6[0] =	 p0[0];
				p6[1] = -p0[1];
				p6[2] = -p0[2];

				p7[0] =	 p1[0];
				p7[1] = -p0[1];
				p7[2] = -p0[2];

				makeBox(p0, p1, p2, p3, p4, p5, p6, p7,
								point, normal, coordIndices, normalIndices, index);
				//
				///////////////////////////////////////////////////////////////////

				///////////////////////////////////////////////////////////////////
				// Use symmetry to create aperture on negative x-axis, change x only
				///////////////////////////////////////////////////////////////////
				// Vertices
				tmp = p0[0];
				p0[0] = -p1[0];
				p1[0] = -tmp;
				p2[0] =	 p1[0];
				p3[0] =	 p0[0];
				p4[0] =	 p1[0];
				p5[0] =	 p0[0];
				p6[0] =	 p0[0];
				p7[0] =	 p1[0];

				makeBox(p0, p1, p2, p3, p4, p5, p6, p7,
								point, normal, coordIndices, normalIndices, index);
				//
				///////////////////////////////////////////////////////////////////
			 } // end of for loop
		} // end of then-branch for even number of slits
		// else odd number of slits
		else {
			for(int i = 2; i < (n + 3) / 2; i++) {
				///////////////////////////////////////////////////////////////////
				// Create aperture on positive x-axis
				///////////////////////////////////////////////////////////////////
				// Vertices
				p0[0] =	 (i - 1) * (distance) - (slitWidth / 2.0f) - (distance - slitWidth);
				p0[1] = -height / 2.0f;
				p0[2] =	 depth / 2.0f;

				p1[0] =	 (i - 1) * (distance) - (slitWidth / 2.0f);
				p1[1] =	 p0[1];
				p1[2] =	 p0[2];

				p2[0] =	 p1[0];
				p2[1] = -p0[1];
				p2[2] =	 p0[2];

				p3[0] =	 p0[0];
				p3[1] = -p0[1];
				p3[2] =	 p0[2];

				p4[0] =	 p1[0];
				p4[1] =	 p0[1];
				p4[2] = -p0[2];

				p5[0] =	 p0[0];
				p5[1] =	 p0[1];
				p5[2] = -p0[2];

				p6[0] =	 p0[0];
				p6[1] = -p0[1];
				p6[2] = -p0[2];

				p7[0] =	 p1[0];
				p7[1] = -p0[1];
				p7[2] = -p0[2];

				makeBox(p0, p1, p2, p3, p4, p5, p6, p7,
								point, normal, coordIndices, normalIndices, index);
				//
				///////////////////////////////////////////////////////////////////

				///////////////////////////////////////////////////////////////////
				// Use symmetry to create aperture on negative x-axis, change x only
				///////////////////////////////////////////////////////////////////
				// Vertices
				tmp = p0[0];
				p0[0] = -p1[0];
				p1[0] = -tmp;
				p2[0] =	 p1[0];
				p3[0] =	 p0[0];
				p4[0] =	 p1[0];
				p5[0] =	 p0[0];
				p6[0] =	 p0[0];
				p7[0] =	 p1[0];

				makeBox(p0, p1, p2, p3, p4, p5, p6, p7,
								point, normal, coordIndices, normalIndices, index);
				//
				///////////////////////////////////////////////////////////////////
			 } // end of for loop
		} // end of else for odd number of slits
		////////////////////////////////////////////////////////////////////
		// send the coordinates, normals, coordIndices, and normalIndices
		//	to the VRML browser
		//
		int []empty = new int[] {0,1,2,3};
		m_set_coordIndex.setValue(empty);
		m_set_point.setValue(point);
		m_set_coordIndex.setValue(coordIndices);
		//m_set_normal.setValue(normal);
		//m_set_normalIndex.setValue(normalIndices);
		//
		////////////////////////////////////////////////////////////////////

		DebugPrinter.println("BoxCreate.evaluate().end");

		return true;
	}
}
