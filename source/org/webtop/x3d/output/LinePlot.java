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

//It might make more sense to support an arbitrary number of resolutions --
//replace the 'high' and 'low' variables with an array a la Grid.  Less likely
//would be the worthiness of a MultiLinePlot, but it'd be good for symmetry...

//It is assumed that 'up' in the plot is in VRML's +y direction; the plot runs from -x to +x,
//although this can be reversed with negative values for dx(es).
//Scaling/rotation of the plot can be done in the VRML file.  [Davis...PC]

public class LinePlot {
    private final AbstractIS ils;

    private boolean high = false; //this causes a full update at construction
    private final int lowRes, highRes;
    private final float lowDx, highDx;

    private final int[] highIndices, lowIndices;
    private final float[][] highPoints, lowPoints;

    public LinePlot(AbstractIS ais, float len, int res) {
        this(ais, len, res, res);
    }

    public LinePlot(AbstractIS ais, float len, int highres, int lowres) {
        if (highres < 1 || lowres < 1)
            throw new IllegalArgumentException("Resolutions must be positive.");
        if (lowres > highres)
            throw new IllegalArgumentException("resolutions in wrong order");
        ils = ais;
        highRes = highres;
        lowRes = lowres;
        highDx = len / (highRes - 1);
        lowDx = len / (lowRes - 1);
        highIndices = new int[highRes];
        lowIndices = new int[lowRes];
        highPoints = new float[highRes][3];
        lowPoints = new float[highRes][3];
        int i;
        final float lowMiddle = (lowRes - 1) / 2f * lowDx,
                                highMiddle = (highRes - 1) / 2f * highDx;
        for (i = 0; i < lowRes; lowIndices[i] = highIndices[i] = i++) {
            lowPoints[i][0] = lowDx * i - lowMiddle;
            highPoints[i][0] = highDx * i - highMiddle;
        }
        for (; i < highRes; highIndices[i] = i++) highPoints[i][0] = highDx * i - highMiddle;
        setValues(new float[highRes]);
    }

    public void setValues(float[] vals) {
        vals = (float[]) vals.clone(); //defensive copying, yay
        int len = vals.length;
        float[][] array; //will point to appropriate array
        if (len == highRes) {
            if (!high) {
                ils.setCoordIndices(highIndices);
                high = true;
            }
            array = highPoints;
        } else if (len == lowRes) {
            if (high) {
                ils.setCoordIndices(lowIndices);
                high = false;
            }
            array = lowPoints;
        } else throw new IllegalArgumentException("Wrong number of values (" +
                                                  len + "); expected " + highRes +
                                                  " or " + lowRes + '.');

        for (int i = 0; i < len; array[i][1] = vals[i++]); //load y values
        ils.setCoords(array);
    }
}
