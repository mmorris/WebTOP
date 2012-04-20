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

import org.web3d.x3d.sai.*;
import org.webtop.x3d.AbstractNode;
import org.webtop.x3d.NamedNode;
import org.webtop.x3d.NodeInputField;
import org.webtop.x3d.SAI;

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
public class FloatMatrixNode extends AbstractNode {
    public static final String SET_POINT="point",SET_COLOR="color";

    private final NamedNode node;
    private final MFVec3f input3;
    private final MFVec2f input2;
    private final MFColor inputC;

    public FloatMatrixNode(SAI sai, NamedNode nn, String event) {
        node = nn;

        /*nn contains the geometric property (color, coord, etc.) node.*/
       // SFNode sfnode = (SFNode)nn.node;
        final X3DField input = nn.node.getField(event);

        if (input == null)throw new IllegalArgumentException("no such event");

        //Only one of these will yield non-null:
        input3 = input instanceof MFVec3f ? (MFVec3f) input : null;
        input2 = input instanceof MFVec2f ? (MFVec2f) input : null;
        inputC = input instanceof MFColor ? (MFColor) input : null;
        if (input3 == null && input2 == null & inputC == null)
            throw new ClassCastException("Not a float-matrix accepting EventIn");
    }
    public NamedNode getNode() {
        return node;
    }
    public void set(float[][] array) {
        //Only one of these will ever run:
        /*I believe that the first argument to setValue should be
          the number of vec3f values contained in 'array'.  [PC]*/
        if(array==null) System.out.println("FloatMatrixNode.set()::array is null");
        if (input3 != null) input3.setValue(array.length, array);
        if (input2 != null) input2.setValue(array.length, array);
        if (inputC != null) inputC.setValue(array.length, array);
    }

}
