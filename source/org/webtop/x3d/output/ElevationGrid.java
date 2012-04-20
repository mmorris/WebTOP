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

import org.webtop.x3d.SAI;
import org.webtop.x3d.NamedNode;
import org.webtop.x3d.NodeInputField;
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
public class ElevationGrid extends Grid {
    //VRML event name for an ElevationGrid's input
    public static final String SET_HEIGHT="set_height";

    private final SAI sai;
    private NamedNode geometry;

    //public ElevationGrid(SAI sai,String shapeNode,int[][] res,float w,float h)
    //{this(sai,sai.getNode(shapeNode),res,w,h);}
    public ElevationGrid(SAI sai, NamedNode geometryNode, int[][] res, float w, float h) {
        super(res, w, h);

        this.sai = sai;
        this.geometry = geometryNode;
        /*Need to create dynamic ElevationGrid geomery node, then add it to shapeNode's geometry*/
    }

    protected void setup0() {
        /*Note that this dynamic creation could create major problems with the memory leak issue of Xj3D
          found in PhotoElectric.  Imagine the Waves modules.  If setup0() must be called everytime there
          is any change in the ElevationGrid, memory will very quickly run out.  Note that the ElevationGrid
          should only change when setting the heights, which shouldn't necessitate dynamically regenerating
          an entirely new EG every time.  However, if it does, we have problems.  [PC]*/

        /*control.node.dispose();  //release resources t
        control = sai.generateNode("ElevationGrid");*/
        /****Need to replace this section with an SAI function to create nodes from an X3D String
        control.create("ElevationGrid {\n\txDimension " + getMRes() +
                       "\n\tzDimension " + getNRes() +
                       "\n\txSpacing " + (getWidth() / (getMRes() - 1)) +
                       "\n\tzSpacing " + (getHeight() / (getNRes() - 1)) +
                       "\n\t" + egridOptions + "\n}", "<ElevationGrid>");
         */
        //Initialize with 0 height
        setHeights(new float[getMRes() * getNRes()]);
    }

    /*protected void addGeometry(NamedNode shapeNode) {
        NodeInputField geoField = new NodeInputField(shapeNode.node.getField("geometry"));  //as much abstraction as possible
        geoField.set(control);  //set the shape's geometry to dynmically created ElevationGrid
    }*/

    public NamedNode getNode() {
        return geometry;
    }

    //Completely abstracted
    public void setHeights(float[] array) {
        sai.setMultipleFloats(sai.getField(geometry, SET_HEIGHT), array);
    }

    //Completely abstracted
    public void setColors(float[][] array) {
        sai.setMultipleVectors(sai.getGeometricProperty(geometry, "color"), array);
    }
}
