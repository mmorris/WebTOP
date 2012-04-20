/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.x3d.output;

/**
 * A standardized class for creating a slitted aperture
 *
 * @author Kiril Vidimce
 * @author Rachel Mueller
 * @author Paul Cleveland
 * @version 1.3
 * 
 * Grant Patten:
 * Because there was a problem when reducing the number of slits, I now have a set number of boxes created
 * at all times.  The unused boxes are made small and moved off the screen when not needed.
 */

import org.web3d.x3d.sai.*;
import org.webtop.util.DebugPrinter;
import org.webtop.x3d.SAI;
import org.webtop.x3d.widget.ScalarWidget;
import org.webtop.util.WTInt;

public class BoxMaker implements ScalarWidget.Listener
{
        ////////////////////////////////////////////////////////////////////////
        // public input fields/parameters that control the computation engine
        //
        public static final float
                width		= 	1000.0f,	// (width / 2.0f) of the box
                height	 	=	300.0f,		// height of the box
                depth		=	0.1f;		// depth of the box

        public float
                distance	 =	220.0f,		// distance between centers of two consecutive slits
                slitWidth	 =	 40.0f;		// (width / 2.0f) of the slit(s)
        
        public final int maxSlits = 10;		// The maximum number of slits allowed
        
        
        public WTInt N;						// the number of slits
        //
        // end of input fields/parameters
        ////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////
        // Pointers to the events that the engine uses.
        private MFVec3f m_set_point;
        private MFInt32 m_set_coordIndex;
   
        private SFNode coordField;
        private X3DNode coordNode;
              

        //////////////////////////////////////////////////////////////////////// 
        //Points
        float []p0 = new float[3];
        float []p1 = new float[3];
        float []p2 = new float[3];
        float []p3 = new float[3];
        float []p4 = new float[3];
        float []p5 = new float[3];
        float []p6 = new float[3];
        float []p7 = new float[3];

        int []index = new int[2];

        float [][]normal = new float[6][3];
        
        //**** Widgets to listen to ****//
        private ScalarWidget widthWidget, distanceWidget;
        
        //**** Default names for input fields [PC] ****//
        public static final String COORD_FIELD = "coord",
        						   SET_POINT = "set_point",
                                   SET_NORMAL = "set_normal",
                                   SET_COORD_INDEX = "set_coordIndex",
                                   SET_NORMAL_INDEX = "set_normalIndex";

        ////////////////////////////////////////////////////////////////////
        public BoxMaker(SAI sai, String apertureNodeName, ScalarWidget wWidget, ScalarWidget dWidget, WTInt n) {
        	this(sai, apertureNodeName, COORD_FIELD, SET_POINT, SET_NORMAL, SET_COORD_INDEX, SET_NORMAL_INDEX, wWidget, dWidget,n);
        }
        public BoxMaker(SAI sai,
        		        String apertureNodeName,
        		        String coordFieldName,
        		        String setPointFieldName,
        		        String setNormalFieldName,
        		        String setCoordIndexFieldName,
        		        String setNormalIndexFieldName,
        		        ScalarWidget wWidget,
        		        ScalarWidget dWidget,
        		        WTInt n)
        {
        	
			
            
            // Get the Coordinate node [PC]
            coordField = (SFNode) sai.getInputField(apertureNodeName, coordFieldName);  //get the coord field (SFNode)
            coordNode = coordField.getValue();
            
            // the events
            m_set_point = (MFVec3f) coordNode.getField(setPointFieldName);           
          
            // coordIndices
            m_set_coordIndex =
                    (MFInt32) sai.getInputField(apertureNodeName,setCoordIndexFieldName);
         
            
            

            //** Connect to WTInt **//
            N=n;
            
            //** Connect to widget **//
            widthWidget = wWidget;
            distanceWidget = dWidget;
            
            //Add widget Listeners --Grant
            widthWidget.addListener(this);
            distanceWidget.addListener(this);
           
        }
        
        

        private void
        makeBox(float []p0, float []p1, float []p2, float []p3,
                                        float []p4, float []p5, float []p6, float []p7,
                                        float [][]point, int []coordIndices, 
                                        int []index) {
                ///////////////////////////////////////////////////////////////////
                //
                //	   6--------------7
                //	  /|			 /|
                //	 / |			/ |
                //	3--------------2  |
                //	|  |  		   |  |
                //	|  |	       |  |
                //	|  |	 	   |  |
                //	|  |		   |  |
                //	|  5-----------|--4
                //	| /			   | /
                //	|/	     	   |/
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


                int
                        pointIndex	= index[0];

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
        }
        
   
        public static float totWidth(int n,float width,float dist) {
                return (n * width + (n - 1) * (dist - width));
        }

        ////////////////////////////////////////////////////////////////////
        // This is where the engine computes the polygons for the box.
        // Returns true upon successful completion and false if the given
        // parameters generate an aperture that exceeds the given (width / 2.0f) constraints
        
        public boolean evaluate() {
        		DebugPrinter.println("n = " + N.getValue());
                DebugPrinter.println("BoxCreate.evaluate().begin");

                ////////////////////////////////////////////////////////////////////
                // check if the number of slits does not create an aperture greater
                // than the given constraints
                DebugPrinter.println("n = " + N.getValue());
                DebugPrinter.println("slitWidth = " + slitWidth);
                DebugPrinter.println("distance = " + distance);
                DebugPrinter.println("width = " + width);
                DebugPrinter.println("= " + totWidth(N.getValue(),slitWidth,distance));
                
                if(totWidth(N.getValue(),slitWidth,distance) > width) {
                        return false;
                }

                ////////////////////////////////////////////////////////////////////
                // allocate memory for the box vertices and normal vectors
                float [][]point	 = new float[8 * (maxSlits + 1)][3];
                int []coordIndices	= new int[30 * (maxSlits + 1)];
                int []normalIndices = new int[ 6 * (maxSlits + 1)];
                
                float tmp;

                ///////////////////////////////////////////////////////////////////
                // initialize the indices
                index[0] = index[1] = 0;

                ///////////////////////////////////////////////////////////////////
                //
                //	   6--------------7
                //	  /|			 /|
                //	 / |			/ |
                //	3--------------2  |
                //	|  |		   |  |			 
                //	|  |		   |  |		 
                //	|  |		   |  |
                //	|  |		   |  |
                //	|  5-----------|--4
                //	| /			   | /
                //	|/			   |/
                //	0--------------1
                //
                ///////////////////////////////////////////////////////////////////
                // Create outer edge of aperture on negative x-axis
                ///////////////////////////////////////////////////////////////////
                p0[0] = -(width / 2.0f);
                p0[1] = -height / 2.0f;
                p0[2] =	 depth / 2.0f;

                p1[0] =	 (N.getValue() - 1) * (-distance / 2.0f) - (slitWidth / 2.0f);
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
                                                point, coordIndices, index);
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
                                                point, coordIndices, index);
                //
                ///////////////////////////////////////////////////////////////////

                ///////////////////////////////////////////////////////////////////
                // case of even number of slits
                if(N.getValue() % 2 == 0) {
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
                                                        point, coordIndices, index);
                        //
                        ///////////////////////////////////////////////////////////////////

                        // Create n-1 inner boxes
                        for(int i = 3; i < (N.getValue() + 4) / 2; i++) {
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
                                                                point, coordIndices, index);
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
                                                                point, coordIndices, index);
                                //
                                ///////////////////////////////////////////////////////////////////
                         } // end of for loop
                } // end of then-branch for even number of slits
                // else odd number of slits
                else {
                        for(int i = 2; i < (N.getValue() + 3) / 2; i++) {
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
                                                                point, coordIndices, index);
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
                                                                point, coordIndices, index);
                                //
                                ///////////////////////////////////////////////////////////////////
                         } // end of for loop
                } // end of else for odd number of slits
                
                //Makes the remaining boxes small and off screen
                for(int num=N.getValue()+2;num<=(maxSlits+1);num++)
                {	
                	p0[0]=p3[0]=p5[0]=p6[0]=-.1f;
                	p1[0]=p2[0]=p4[0]=p7[0]=0.1f;
                	
                	p0[1]=p5[1]=p1[1]=p4[1]=-.1f;
                	p3[1]=p6[1]=p2[1]=p7[1]=0.1f;
                	
                	p0[2]=p3[2]=p2[2]=p1[2]=-500.1f;
                	p5[2]=p6[2]=p7[2]=p4[2]=-500.0f;
                	
                	makeBox(p0, p1, p2, p3, p4, p5, p6, p7,
                             point, coordIndices, index);
                }
                
                
                ////////////////////////////////////////////////////////////////////
                // send the coordinates and coord-indices to the x3d browser
                //                
                m_set_coordIndex.setValue(coordIndices.length, coordIndices);
                m_set_point.setValue(point.length, point);
           
                DebugPrinter.println("BoxCreate.evaluate().end");
                return true;
        }
        
        //Updates the variables using values from float boxes
        public void updateDistance(float d)
        {
        	distance = d*500;
        }
        
        public void updateWidth(float w)
        {
        	slitWidth = 500*w*2;
        }
        
        public void updateDistanceW(float value)
        {
        	slitWidth = value*2; 
        }
        
        public void updateWidthW(float value)
        {
        	distance = 2*value/(N.getValue() - 1);
        }
        
        //***** ScalarWidget.Listener Interface *****//
        
        public void valueChanged(ScalarWidget src, float value) {
        	if(src==widthWidget) {
        		slitWidth = value*2; 
        	}
        	else if(src==distanceWidget) {	
        		distance = 2*value/(N.getValue() - 1);
        			if(N.getValue()==1)
        				distance=1;
        		DebugPrinter.println("BoxMaker()::Distance Changed to: " + distance);
        	}
        	else {
        		System.out.println("BoxMaker.valueChanged(): Invalid widget source " + src);
        	}
        }
        
}
