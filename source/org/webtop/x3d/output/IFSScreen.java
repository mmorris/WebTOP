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

import org.webtop.x3d.*;

/**
 * <p>Title: X3DWebTOP</p>
 *
 * <p>Description: The X3D version of The Optics Project for the Web
 * (WebTOP)</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: MSU Department of Physics and Astronomy</p>
 *
 * @author Paul Cleveland, Peter Gilbert
 * @version 0.0
 */

//IFSes appear to be more efficient than ElevationGrids.

//Note that this does not implement AbstractIS because it's not a general
//purpose IFS class.

public class IFSScreen extends Grid {
    private final AbstractIS ifs;
    private float[][] points;

    //public IFSScreen(EAI eai,String ifsNode,int[][] res,float w,float h)
    //{this(eai,eai.getNode(ifsNode),res,w,h);}
    public IFSScreen(AbstractIS ais, int[][] res, float w, float h) {
        super(res, w, h);

        ifs = ais;
    }

    protected void setup0() {
        final int m = getMRes(), n = getNRes();

        //The convention here (as in ElevationGrid) is that M is associated with
        //width, and N with height.  For further consistency with ElevationGrid we
        //use the x and z axes for width and height, respectively.
        {
            //make points
            final float w = getWidth(), h = getHeight(), dx = w / (m - 1), dz = h / (n - 1);
            /*final float[][]*/ points = new float[m * n][];

            for (int i = 0; i < m; ++i)
                for (int j = 0; j < n; ++j)
                    points[n * i + j] = new float[] {dx * i, 0, dz * j};

            ifs.setCoords(points);
        }

        {
            //make indices: cells are one fewer in number than points
            final int cm = m - 1, cn = n - 1;
            final int[] indices = new int[5 * cm * cn];

            //This could make triangles instead of quads if it works better
            for (int i = 0, offset = 0; i < cm; ++i)
                for (int j = 0; j < cn; ++j) {
                    indices[offset++] = n * i + j;
                    indices[offset++] = n * i + (j + 1);
                    indices[offset++] = n * (i + 1) + (j + 1);
                    indices[offset++] = n * (i + 1) + j;
                    indices[offset++] = -1; //end this quad
                }

            ifs.setCoordIndices(indices);
        }
    }

    public void setColors(float[][] array) {
        //Setting the coords is necessary to make the IFS redraw when the
        //colors are changed.  This is temporary:  there are less expensive ways
        //to do this. [Peter]
        //ifs.setCoords(points);  //Xj3D fixed this in a 2.0+ dev release. [PC]
        ifs.setColors(array);
    }

}
