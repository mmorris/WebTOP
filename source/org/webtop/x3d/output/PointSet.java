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
public class PointSet extends AbstractNode implements AbstractPS{
    public static final String SET_COORD="coord", SET_COLOR="color";

    private final SAI sai;
    private final NamedNode node;  //a geometry node

    private final NodeInputField set_coord, set_color;
    private FloatMatrixNode coordNode, colorNode;

    public PointSet(SAI sai_, NamedNode psNode) {
        this(sai_, psNode, SET_COORD, SET_COLOR);
    }

    public PointSet(SAI sai_, NamedNode psNode, String coord, String color) {
        sai = sai_;
        node = psNode;
        set_coord = new NodeInputField(node.node.getField(coord));
        //set_coord = sai.getGeometricPropery(node,coord);
        //Need to get (MFColor color) field of (SFNode color) field of geometry node referenced by 'node'
        //set_color = sai.getGeometricPropery(node,color);
        set_color = new NodeInputField(node.node.getField(color));
    }

    public NamedNode getNode() {
        return node;
    }

    public FloatMatrixNode getCoordinateNode() {
        return coordNode;
    }

    public FloatMatrixNode getColorNode() {
        return colorNode;
    }

    //Typically, one of the following functions will be called immediately after
    //construction -- they return 'this' so they can be added onto the
    //constructor (or onto each other) without complications.
    public PointSet setCoordinateNode(FloatMatrixNode fmn) {
        coordNode = fmn;
        set_coord.set(coordNode.getNode());
        return this;
    }

    public PointSet setColorNode(FloatMatrixNode fmn) {
        colorNode = fmn;
        set_color.set(colorNode.getNode());
        return this;
    }

    /*The make...Node() methods need access to the geometry node so they can
      set a null geometric property (color, coord) field.  The FloatMatrixNode
      variables need access to the geometric property node, so that they can
      set the correct field. [PC]
    */

   /**
    * A default method for initializing the coordinate field of a geometry node,
    * as well as that field's underlying Coordinate node.
    * @return This PointSet.
    */
   public PointSet makeCoordinateNode() {
        return makeCoordinateNode(SET_COORD, FloatMatrixNode.SET_POINT);
    }

    /**
     * A generic method for initializing the coordinate field of a geometry node,
     * as well as that field's underlying Coordinate node.
     * @param geoCoordField String containing the field name of the geometry's coordinate field.
     * @param coordPointField String containing the field name of the point field of the Coordinate node referenced by the <code>geoCoordField</code>.
     * @return This PointSet.
     */
    public PointSet makeCoordinateNode(String geoCoordField, String coordPointField) {
        /*The geometry always includes an empty (but non-null) color field, even
          if there is no "color" field included in a geometry instantiation.
          Therefore, there should neve be a need to dynamically generate a Color
          node and attach it to the geometry.  [PC]
         */
        SFNode geometryCoord = (SFNode)node.node.getField(geoCoordField);    //reference to geometry field <coordField>
        System.out.println("makeCoordinateNode()::geometryCoord.getValue()'s type is " + geometryCoord.getDefinition().getFieldTypeString());
        if(geometryCoord.getValue()==null) {
            System.out.println("makeCoordinateNode()::geometryCoor.getValue() is null");
        }
        NamedNode nn = new NamedNode(geometryCoord.getValue());              //NamedNode with reference to node controlling geometry field <coordField>
        coordNode = new FloatMatrixNode(sai, nn, coordPointField);           //create FloatMatrixNode with access to controlling field of node referenced by NamedNode
        return this;
    }

    /**
     * A default method for initializing the color field of a geometry node, as well
     * as that field's underlying Color node.
     * @return This PointSet.
     */
    public PointSet makeColorNode() {
        return makeColorNode(SET_COLOR, FloatMatrixNode.SET_COLOR);
    }

    /**
     * A generic method for initializing the color field of a geometry node, as well
     * as that field's underlying Color node.
     * @param geoColorField String containing the field name of the geometry's color field.
     * @param colorColorField String containing the field name of the color field for the Color node referenced by <code>geoColorField</code>.
     * @return PointSet
     */
    public PointSet makeColorNode(String geoColorField, String colorColorField) {
        /*The geometry always includes an empty (but non-null) color field, even
          if there is no "color" field included in a geometry instantiation.
          Therefore, there should neve be a need to dynamically generate a Color
          node and attach it to the geometry.  [PC]
         */
        SFNode geometryColor = (SFNode)node.node.getField(geoColorField);  //reference to geometry field <colorField>
        NamedNode nn = new NamedNode(geometryColor.getValue());            //NamedNode with reference to node controlling geometry field <colorField>
        colorNode = new FloatMatrixNode(sai, nn, colorColorField);         //create FloatMatrixNode with access to controlling field of node referenced by NamedNode
        return this;
    }

    //It might be better to raise an exception than to auto-make default nodes,
    //but this is horribly convenient for what is by far the most common case.
    public void setCoords(float[][] coords) {
        if (coordNode == null) makeCoordinateNode();
        coordNode.set(coords);
    }

    public void setColors(float[][] colors) {
        if (colorNode == null) makeColorNode();
        colorNode.set(colors);
    }

    public void printColors() {
        SFNode geometryColor = (SFNode)node.node.getField("color");
        MFColor mfColor = (MFColor)geometryColor.getValue().getField("color");

        float [][] colorArray = new float[22500][3];
        mfColor.getValue(colorArray);

        System.out.println("colorArray[" + 5000 + "] = " + colorArray[5000][0] + ", " + colorArray[5000][1] + ", " + colorArray[5000][2]);

        /*for (int i = 0; i<22500; i=i+100) {
            System.out.println("colorArray[" + i + "] = " + colorArray[i][0] + ", " + colorArray[i][1] + ", " + colorArray[i][2]);
        }*/

    }

}
