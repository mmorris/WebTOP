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
import javax.swing.*;
//import vrml.external.field.*;
import org.sdl.gui.numberbox.*;
import org.webtop.util.*;
import org.web3d.x3d.sai.*;
import org.webtop.x3d.*;

//import webtop.vrml.*;
//import webtop.wsl.client.*;
//import webtop.wsl.script.*;

class DonutStop extends Stop implements X3DFieldEventListener {
    public static final float MIN_DONUT_DIAMETER = MIN_DIAMETER, MAX_DONUT_DIAMETER = MAX_DIAMETER,
    DEF_DONUT_DIAMETER = 2;

    public static final int DONUT_DIAMETER = getNextEventID();

    private FloatBox fbDonutDiameter = new FloatBox(MIN_DONUT_DIAMETER, MAX_DIAMETER,
            DEF_DONUT_DIAMETER, 3);

    private SFFloat set_donutRadius;
    private SFFloat donutRadius_changed;

    private class DonutDiameterFieldListener extends NumberBox.Adapter {
        public void numChanged(NumberBox src, Number newVal) {
            clearWarning();
            float dd = newVal.floatValue();
            // This is a hack to avoid modifying CircularElement.
            if (dd > getDiameter()) {
                src.revert();
                return;
            }
            DebugPrinter.println(getName() + ": donutDiameter -> " + dd);
            if (!draggingWidget()) {
                set_donutRadius.setValue(dd / 2);
                setDonutDiameter(dd);
            }
            setMinDiameter(dd);
            appletUpdate(DIAMETER);

            //if(!draggingWidget())
            //getWSLPlayer().recordActionPerformed(getNamePrefix()+getID(),"donutDiameter",String.valueOf(dd));
        }

        public void invalidEntry(NumberBox src, Number badVal) {
            setWarning("Donut diameter must be between " + MIN_DONUT_DIAMETER + " and " +
                       getDiameter() + " cm (the diameter of the outer stop).");
        }
    };

    public DonutStop(Geometrical main, float position) {
        super(main, "DonutStop", "", position,
              "Use the red cones to change the diameter or position of the stop and the disk.");

        add(new JLabel("Inner D:", Label.RIGHT));
        add(fbDonutDiameter);
        add(new JLabel("cm"));
        validate();

        fbDonutDiameter.addNumberListener(new DonutDiameterFieldListener());

        set_donutRadius = (SFFloat) getSAI().getInputField(getNode(), "set_donutRadius");
        donutRadius_changed = (SFFloat) getSAI().getOutputField(getNode(), "donutRadius_changed",
                this, this);

        //This breaks scripting -- is there a reason for it?  [Davis]
        //fbDonutDiameter.ping();
    }

    protected String getNamePrefix() {
        return "DonutStop";
    }

    public float getDonutDiameter() {
        return fbDonutDiameter.getValue();
    }

    public void setDonutDiameter(float d) {
        fbDonutDiameter.setSigValue(d, GUI_PRECISION);
    }

    public int clip(RayList rays) {
        return super.clip(rays) + clip0(rays, fbDonutDiameter.getValue(), false);
    }


    public void readableFieldChanged(X3DFieldEvent e) {
        //this is only called for donut radius drags -- data is the appropriate DonutStop
        int mode = ((Integer) e.getData()).intValue();
        if (mode == DONUT_DIAMETER) { // fix this peter
            float dr = ((SFFloat) e.getSource()).getValue();
            //WSLPlayer wslPlayer = ds.getWSLPlayer();
            if (draggingWidget()) {
                //DebugPrinter.println("DonutRadiusListener: "+ds.getName()+" got "+dr);
                //wslPlayer.recordMouseDragged(ds.getNamePrefix()+ds.getID(),"donutDiameter",String.valueOf(dr*2));
                setDonutDiameter(dr * 2);
            }
        }
        else
            super.readableFieldChanged(e);
        //else DebugPrinter.println("DonutRadiusListener: "+ds.getName()+" ignored "+dr);
    }


//There should perhaps be more intelligent inheritance going on here
    /*public WSLNode toWSLNode() {
     WSLNode node = new WSLNode("donutstop");
     final WSLAttributeList atts=node.getAttributes();

     atts.add("id", String.valueOf(getID()));
     atts.add("diameter", String.valueOf(getDiameter()));
     atts.add("donutDiameter", String.valueOf(getDonutDiameter()));
     atts.add("position", String.valueOf(getPosition()));
     return node;
      }*/

    public String toString() {
        try {
            return getClass().getName() + "[#" + getID() + ",pos=" + getPosition() +
                    ",d=" + getDiameter() + ",dd=" + getDonutDiameter() + ']';
        } catch (NullPointerException e) { //not set up yet
            return super.toString();
        }
    }
}
