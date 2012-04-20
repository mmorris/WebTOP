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

//One end of the spine of the line is at the origin; it runs in the positive z
//direction. [Davis...PC]

public class VectorLine {
    private final AbstractIS ils;

    private final float[][] points;

    public VectorLine(AbstractIS ais, float len, int res) {
        this(ais, len, res, 1, 0);
    }

    public VectorLine(AbstractIS ais, float L, int N, int n, float r) {
        //r is ratio of group length to group separation

        if (N < 1 || n < 1)
            throw new IllegalArgumentException("Resolution must be positive.");
        ils = ais;

        final float group_delta = L / (N - 1 + r), vector_delta = r * group_delta / n;

        points = new float[N * n * 2][3];
        int[] indices = new int[N * n * 3];
        for (int i = 0; i < N; ++i)
            for (int j = 0; j < n; ++j) {
                int index = i * n + j;
                indices[3 * index] = 2 * index;
                indices[3 * index + 1] = 2 * index + 1;
                indices[3 * index + 2] = -1;
                points[2 *
                        index][2] = points[2 * index + 1][2] = i * group_delta + j * vector_delta;
            }
        ils.setCoordIndices(indices);
        ils.setCoords(points);
    }

    public void setValues(float[][] vals) { // each element has length 2
        vals = (float[][]) vals.clone(); //defensive copying, yay
        final int n = vals.length;
        if (n != points.length)
            throw new IllegalArgumentException("Wrong number of values (" +
                                               n + "); expected " +
                                               points.length + '.');

        //Load X and Y values
        for (int i = 0; i < n; ++i) {
            final float[] src = vals[i], dest = points[i];
            dest[0] = src[0];
            dest[1] = src[1];
        }
        ils.setCoords(points);
    }
}
