/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Photoelectron.java
//Karolina Sarnowska & Peter Gilbert
//Created October 26 2004
//Updated June 2 2005
//Version 0.1

package org.webtop.module.photoelectric;

import org.webtop.util.*;
import org.webtop.x3d.*;
//import vrml.external.field.*;
//import vrml.external.exception.*;
import java.math.*;
import org.web3d.x3d.sai.*;

public class Photoelectron extends X3DObject {
    private NamedNode node;

    private static final int IN_RANGE = 0, OUT_OF_RANGE_LEFT = 1, OUT_OF_RANGE_RIGHT = 2;

    private final int X = 0;
    private final int Y = 1;
    private final int Z = 2;
    private final float e = 1.602e-19f; // charge of electron
    private final float mass = 9.109e-31f; // mass of electron
    private final float hc = 1240f;

    // will probably be needed
    //private final float XY_SCALE = 50000f;
    private final float XY_SCALE = 2000f;
    private final float Z_SCALE = 2000f;
    private final float X_SIZE = 175f * XY_SCALE;
    private final float Y_SIZE = 175f * XY_SCALE;
    private final float Z_SIZE = 700f * Z_SCALE;
    private final float a = 115f * XY_SCALE;
    private final float b = 150f * XY_SCALE;

    private final float d = 700f * Z_SCALE; // distance between plates

    private SFFloat set_x_position;
    private SFFloat set_y_position;
    private SFFloat set_z_position;
    private SFFloat set_transparency;


    private float position[] = new float[3];
    private float velocity[] = new float[3];
    private float theta;
    private float az; // acceleration in z direction
    private float vm; // magnitude of initial velocity vector

    private Photoelectric wapp;

    protected String getNodeName() {
        return "Sphere";
    }

    public Photoelectron(float wavelength, float workFunction, float voltage, Photoelectric _wapp,
                         SAI sai) {
        super(sai, sai.getNode("ElectronHolder"));
        wapp = _wapp;
        vm = (float) Math.sqrt(2f / mass * ((hc / wavelength - workFunction) * e));
        az = -e * voltage / (mass * d);
        //float phi = (float) (2 * Math.PI * Math.random());
        //theta = (float)(Math.random()*0.245);

        position[X] = (float) ((Math.random() - 0.5f) * 2 * a); // needs to be randomly generated inside ellipse
        position[Y] = (float) ((Math.random() - 0.5f) * 2 *
                               Math.sqrt((b * b * (1f - position[X] * position[X] / (a * a))))); // ditto
        position[Z] = 0;

        //System.out.println("(x,y) = " + position[X] + "," + position[Y]);

        //velocity[X]=(float)(vm*Math.sin(theta)*Math.cos(phi));
        //velocity[Y]=(float)(vm*Math.sin(theta)*Math.sin(phi));
        //velocity[Z]=(float)(vm*Math.cos(theta));
        velocity[Z] = (float) (vm);

        //node=new NamedNode(eai.world.createVrmlFromString("Photoelectron {}")[0],getName());

        createProto("Photoelectron");
        //try {
        place();
        //} catch(InvalidVrmlException fake) {}

        set_x_position = (SFFloat) sai.getInputField(this.getNode(), "set_x_position");
        set_y_position = (SFFloat) sai.getInputField(this.getNode(), "set_y_position");
        set_z_position = (SFFloat) sai.getInputField(this.getNode(), "set_z_position");
        set_transparency = (SFFloat) sai.getInputField(this.getNode(), "set_transparency");

        //set_x_position.setValue(position[X]/XY_SCALE);
        //set_y_position.setValue(position[Y]/XY_SCALE);
        //set_z_position.setValue(position[Z]/Z_SCALE);
    }

    public void reset(float wavelength, float workFunction, float voltage) {
        vm = (float) Math.sqrt(2f / mass * ((hc / wavelength - workFunction) * e));
        az = -e * voltage / (mass * d);

        position[X] = (float) ((Math.random() - 0.5f) * 2 * a); // needs to be randomly generated inside ellipse
        position[Y] = (float) ((Math.random() - 0.5f) * 2 *
                               Math.sqrt((b * b * (1f - position[X] * position[X] / (a * a))))); // ditto
        position[Z] = 0;
        velocity[Z] = (float) (vm);
        set_transparency.setValue(0f);
    }

    public void hide() {
        set_transparency.setValue(1f);
    }

    public void passTime(float t) {
        //if (!(Math.abs(position[X])>X_SIZE || Math.abs(position[Y])>Y_SIZE))
        az = -e * wapp.getVoltage() / (mass * d);
        //position[X]+=(velocity[X]*t);
        //position[Y]+=(velocity[Y]*t);
        position[Z] += (velocity[Z] * t + 0.5f * az * t * t);
        //if (Math.abs(position[X])>X_SIZE || Math.abs(position[Y])>Y_SIZE)
        //	az = 0;
        velocity[Z] += az * t;
        if (checkBounds() != IN_RANGE) {
            set_transparency.setValue(1f);
        }
        updateX3DPosition();
    }

    public void updateX3DPosition() {
        set_x_position.setValue(position[X] / XY_SCALE);
        set_y_position.setValue(position[Y] / XY_SCALE);
        set_z_position.setValue(position[Z] / Z_SCALE);
    }

    public int checkBounds() {
        if (position[Z] < 0) {
            return OUT_OF_RANGE_LEFT;
        } else if (position[Z] > Z_SIZE) {
            return OUT_OF_RANGE_RIGHT;
        } else {
            return IN_RANGE;
        }
    }

    public String getName() {
        return "photoelectron";
    }
}
