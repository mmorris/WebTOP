/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.geometrical;

import java.awt.*;
//import vrml.external.field.*;
import org.sdl.gui.numberbox.*;
import org.webtop.util.*;
import org.web3d.x3d.sai.*;

//import webtop.wsl.script.*;

class Stop extends CircularElement {
    public static final float
            width = Math.max(7, MAX_DIAMETER * 7 / 6), // width of the aperture
                    height = Math.max(6, MAX_DIAMETER), // height of the aperture
                             thickness = 0.4f; // thickness of the aperture

    ////////////////////////////////////////////////////////////////////////
    // public input fields/parameters that control the computation engine
    public int
            segments = 20; // number of circular segments

    public static final String texture = "oak.rgb";
    public static final boolean useTexture = false;
    ////////////////////////////////////////////////////////////////////////


    private MFVec3f set_point;

    private MFInt32 set_pointIndex;

    protected Stop(Geometrical main, String nodeType, String fields, float position, String tip) {
        super(main, nodeType,
              fields + (useTexture ? " texture ImageTexture { url " + texture + "}" : ""), position,
              tip);
        set_point = (MFVec3f) getSAI().getInputField(getNode(), "set_coordPoint");
        set_pointIndex = (MFInt32) getSAI().getInputField(getNode(), "set_coordIndex");

        evaluate();
    }

    public Stop(Geometrical main, float position) {
        this(main, "Stop", "", position,
             "Use the red cones to change the diameter or position of the stop.");
    }

    public void setFields() {}


    protected String getNamePrefix() {
        return "Stop";
    }

    public void setDiameter(float d) {
        super.setDiameter(d);
        evaluate();
    }

    public void process(RayList rays) {}

    ////////////////////////////////////////////////////////////////////////
    // This is the calculation engine that reconstructs the geometry based
    // on the input fields (width, height, thickness and radius)
    public void evaluate() {
        DebugPrinter.println(getName() + "::evaluate()");
        int i; // index
        float theta, dt; // Angle
        float[] t = new float[3]; //Tanget to circle
        float[] n = new float[3]; //normal to circle
        float[] r1 = new float[3];
        float[] r2 = new float[3];
        float[] point = new float[3];
        final float R = getDiameter() / 2;

        theta = 0.0f;
        r1[0] = 0.0f;
        r1[1] = 1.0f;
        r1[2] = 0.0f;

        float[][] vertices = new float[segments * 2 + 8][3];

        dt = (float) Math.PI * 2.0f / (float) segments;
        for (i = 0; i < segments; i++) {
            theta += dt;
            r2[0] = (float) Math.sin(theta);
            r2[1] = (float) Math.cos(theta);
            r2[2] = 0.0f;

            vertices[i] = new float[3];
            vertices[i][0] = r1[0] * R;
            vertices[i][1] = r1[1] * R;
            vertices[i][2] = thickness / 2.0f;

            vertices[i + segments] = new float[3];
            vertices[i + segments][0] = r1[0] * R;
            vertices[i + segments][1] = r1[1] * R;
            vertices[i + segments][2] = -thickness / 2.0f;

            r1[0] = r2[0];
            r1[1] = r2[1];
            r1[2] = r2[2];
        }

        //Front
        // top right corner
        int vertex = segments * 2;
        vertices[vertex] = new float[3];
        vertices[vertex][0] = width / 2.0f;
        vertices[vertex][1] = height / 2.0f;
        vertices[vertex][2] = thickness / 2.0f;
        ++vertex;

        // bottom right corner
        vertices[vertex] = new float[3];
        vertices[vertex][0] = width / 2.0f;
        vertices[vertex][1] = -height / 2.0f;
        vertices[vertex][2] = thickness / 2.0f;
        ++vertex;

        // bottom left corner
        vertices[vertex] = new float[3];
        vertices[vertex][0] = -width / 2.0f;
        vertices[vertex][1] = -height / 2.0f;
        vertices[vertex][2] = thickness / 2.0f;
        ++vertex;

        // top left corner
        vertices[vertex] = new float[3];
        vertices[vertex][0] = -width / 2.0f;
        vertices[vertex][1] = height / 2.0f;
        vertices[vertex][2] = thickness / 2.0f;
        ++vertex;

        //Back
        // top right corner
        vertices[vertex] = new float[3];
        vertices[vertex][0] = width / 2.0f;
        vertices[vertex][1] = height / 2.0f;
        vertices[vertex][2] = -thickness / 2.0f;
        ++vertex;

        // bottom right corner
        vertices[vertex] = new float[3];
        vertices[vertex][0] = width / 2.0f;
        vertices[vertex][1] = -height / 2.0f;
        vertices[vertex][2] = -thickness / 2.0f;
        ++vertex;

        // bottom left corner
        vertices[vertex] = new float[3];
        vertices[vertex][0] = -width / 2.0f;
        vertices[vertex][1] = -height / 2.0f;
        vertices[vertex][2] = -thickness / 2.0f;
        ++vertex;

        // top left corner
        vertices[vertex] = new float[3];
        vertices[vertex][0] = -width / 2.0f;
        vertices[vertex][1] = height / 2.0f;
        vertices[vertex][2] = -thickness / 2.0f;

        int
                resolution = segments * 5 + (segments + 4) * 4 + (segments + 4) * 4 + 4 * 5;

        int[] vindex = new int[resolution];

        // Quads for inner part of the aperture
        int index = 0;
        for (i = 0; i < segments - 1; i++) {
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
        for (i = 0; i < segments; i++) {
            vindex[index++] = i;
            idx1 = (i == segments - 1) ? 0 : i + 1;
            vindex[index++] = idx1;
            idx1 = i * 4 / segments + segments * 2;
            vindex[index++] = idx1;
            vindex[index++] = -1;
            if (i % (segments / 4) == 0) {
                if (i == 0) {
                    idx2 = segments * 2 + 3;
                } else {
                    idx2 = idx1 - 1;
                }
                vindex[index++] = i;
                vindex[index++] = idx1;
                vindex[index++] = idx2;
                vindex[index++] = -1;
            }
        }

        // rear face
        for (i = segments; i < segments * 2; i++) {
            idx1 = (i == segments * 2 - 1) ? segments : i + 1;
            vindex[index++] = idx1;
            vindex[index++] = i;
            idx1 = (i - segments) * 4 / segments + segments * 2 + 4;
            vindex[index++] = idx1;
            vindex[index++] = -1;

            if (i % (segments / 4) == 0) {
                if (i == segments) {
                    idx2 = segments * 2 + 7;
                } else {
                    idx2 = idx1 - 1;
                }
                vindex[index++] = idx2;
                vindex[index++] = idx1;
                vindex[index++] = i;
                vindex[index++] = -1;
            }
        }

        // right face
        int segments2 = segments * 2;
        vindex[index++] = segments2 + 1;
        vindex[index++] = segments2 + 5;
        vindex[index++] = segments2 + 4;
        vindex[index++] = segments2 + 0;
        vindex[index++] = -1;

        // left face
        vindex[index++] = segments2 + 3;
        vindex[index++] = segments2 + 7;
        vindex[index++] = segments2 + 6;
        vindex[index++] = segments2 + 2;
        vindex[index++] = -1;

        // bottom face
        vindex[index++] = segments2 + 1;
        vindex[index++] = segments2 + 2;
        vindex[index++] = segments2 + 6;
        vindex[index++] = segments2 + 5;
        vindex[index++] = -1;

        // top face
        vindex[index++] = segments2 + 0;
        vindex[index++] = segments2 + 4;
        vindex[index++] = segments2 + 7;
        vindex[index++] = segments2 + 3;
        vindex[index++] = -1;

        set_point.setValue(segments * 2 + 8, vertices);
        set_pointIndex.setValue(resolution, vindex);
    }

    /*public WSLNode toWSLNode() {
     WSLNode node = new WSLNode("stop");
     final WSLAttributeList atts=node.getAttributes();

     atts.add("id", String.valueOf(getID()));
     atts.add("diameter", String.valueOf(getDiameter()));
     atts.add("position", String.valueOf(getPosition()));
     return node;
      }*/

    public String toString() {
        try {
            return getClass().getName() + "[#" + getID() + ",pos=" + getPosition() +
                    ",d=" + getDiameter() + ']';
        } catch (NullPointerException e) { //not set up yet
            return super.toString();
        }
    }
}
