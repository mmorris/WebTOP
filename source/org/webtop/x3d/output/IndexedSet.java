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
import org.web3d.x3d.sai.*;

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
public class IndexedSet extends PointSet implements AbstractIS {
    public static final String SET_COORDINDEX="set_coordIndex", SET_COLORINDEX="set_colorIndex";

    private final MFInt32 set_coordIndex, set_colorIndex;

    private float[][] points;

    public IndexedSet(SAI sai, NamedNode isNode) {
        this(sai, isNode, SET_COORD, SET_COORDINDEX, SET_COLOR, SET_COLORINDEX);
    }

    public IndexedSet(SAI sai, NamedNode isNode, String coord, String coordIndex, String color, String colorIndex) {
        //old prototype: super(sai, isNode, coord, color);
        super(sai, isNode, coord, color);
        points = new float[][] {};
        set_coordIndex = (MFInt32) sai.getInputField(isNode, coordIndex);
        set_colorIndex = (MFInt32) sai.getInputField(isNode, colorIndex);
    }

    public void setCoords(float[][] coords) {
        points = coords;
        super.setCoords(coords);
    }

    public void setColors(float[][] colors) {
        super.setCoords(points);
        super.setColors(colors);
    }

    public void setCoordIndices(int[] indices) {
        /*I believe that the first argument to setValue should be
          the number of int32 values contained in 'array'.  [PC]*/
        set_coordIndex.setValue(indices.length, indices);
    }

    public void setColorIndices(int[] indices) {
        /*See comment in setCoordIndices() [PC]*/
        set_colorIndex.setValue(indices.length, indices);
    }
}
