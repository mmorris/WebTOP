/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//CircularElement.java
//Defines clipping functions and diameter-entry support for optical elements.
//Davis Herring
//Created February 24 2003
//Updated January 29 2004
//Version 1.02

package org.webtop.module.geometrical;

import java.awt.*;
import javax.swing.*;
//import vrml.external.field.*;
import org.sdl.gui.numberbox.*;
import org.webtop.util.*;
//import webtop.vrml.*;
//import webtop.wsl.client.*;
//import webtop.wsl.event.*;
//import webtop.wsl.script.*;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;

public abstract class CircularElement extends OpticalElement {
    public static final float MIN_DIAMETER = 0, MAX_DIAMETER = 10, DEF_DIAMETER = 5;

    ////////////////////////////////////////////////////////////////////////
    // Constants that denote type of event
    public static final int DIAMETER = getNextEventID(), RADIUS = getNextEventID();

    private FloatBox fbDiameter = new FloatBox(MIN_DIAMETER, MAX_DIAMETER, DEF_DIAMETER, 3);

    private SFFloat set_radius;
    private SFFloat radius_changed;

    //===========
    // LISTENERS
    //===========

    //An object of this type will listen to each element's diameter box.
    private class DiameterFieldListener extends NumberBox.Adapter {
        public void numChanged(NumberBox src, Number newVal) {
            //WSLPlayer wslPlayer = getWSLPlayer();
            clearWarning();
            float d = newVal.floatValue();
            DebugPrinter.println(getName() + ": diameter -> " + d);
            if (!draggingWidget()) {
                set_radius.setValue(d / 2);
                setDiameter(d);
            }
            appletUpdate(DIAMETER);

            //if(!draggingWidget())
            //	wslPlayer.recordActionPerformed(getName(),"diameter",String.valueOf(d));
        }

        public void invalidEntry(NumberBox src, Number badVal) {
            //Is there an elegant way to put the "(the disk's diameter)" bit into this string? [Davis]
            setWarning("Diameter must be between " + ((FloatBox) src).getMin() + " and " +
                       ((FloatBox) src).getMax() + " cm.");
        }
    };

    protected CircularElement(Geometrical main, String nodeType, String fields, float position,
                              String tip) {
        super(main, nodeType, fields, position, tip);

        if (this instanceof DonutStop) {
            add(new JLabel("Outer D:", Label.RIGHT));
        } else {
            add(new JLabel("Diameter:", Label.RIGHT));
        }
        add(fbDiameter);
        add(new Label("cm"));
        validate();

        fbDiameter.addNumberListener(new DiameterFieldListener());

        set_radius = (SFFloat) getSAI().getInputField(getNode(), "set_radius");
        radius_changed = (SFFloat) getSAI().getOutputField(getNode(), "radius_changed", this,
                new Integer(RADIUS));
    }

    public void readableFieldChanged(X3DFieldEvent e) {
        //this is only called for radius drags -- data is the appropriate CircularElement
        int mode = ((Integer) e.getData()).intValue();
        if (mode==RADIUS) {
            float r = ((SFFloat) e.getSource()).getValue();
            //WSLPlayer wslPlayer = ce.getWSLPlayer();
            //if (ce.draggingWidget()) {
                //DebugPrinter.println("RadiusListener: "+ce.getName()+" got "+r);
                //wslPlayer.recordMouseDragged(ce.getName(),"diameter",String.valueOf(r*2));
                setDiameter(r * 2);
            //}
        }
        else
            super.readableFieldChanged(e);
        //else DebugPrinter.println("RadiusListener: "+ce.getName()+" ignored "+r);
    }


    public float getDiameter() {
        return fbDiameter.getValue();
    }

    public void setDiameter(float d) {
        fbDiameter.setSigValue(d, GUI_PRECISION);
    }

    protected void setMinDiameter(float d) {
        fbDiameter.setMin(d);
    }

    protected void setMaxDiameter(float d) {
        fbDiameter.setMax(d);
    }

    //Now returns the number of rays that -miss-
    protected int clip0(RayList rays, float d, boolean in) {
        return rays.prune(getPosition(), d, in);
    }

    //The default is to clip to within our diameter
    public int clip(RayList rays) {
        return clip0(rays, getDiameter(), true);
    }
}
