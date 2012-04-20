/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//UnknownLens.java
//Declares a ThinLens subclass with random focal length.
//Peter Gilbert
//Created July 29 2004
//Updated July 29 2004
//Version 0.01

package org.webtop.module.geometrical;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//import vrml.external.field.*;
//import vrml.external.exception.*;

import org.sdl.gui.numberbox.*;
import org.webtop.util.*;
import org.web3d.x3d.sai.*;
//import webtop.vrml.*;
//import webtop.wsl.script.*;
//import webtop.wsl.client.*;
//import webtop.wsl.event.*;

import org.sdl.gui.numberbox.FloatBox;
import org.sdl.math.FPRound;

class UnknownLens extends CircularElement {

    public static final float MIN_FOCUS = -100, MAX_FOCUS = 100, DEF_FOCUS = 0;

    private static final float[] fValues = new float[] {10, 20, 25, 40, 50, -12, -24, -36, -48, -80};
    private static final String[] letters = new String[] {"A", "B", "C", "D", "E", "F", "G", "H",
                                            "I", "J"};

    //private float focus = (float)(FPRound.toFixVal(Math.random()*100,1));
    private float focus;

    private int index;

    private String letterIdent;

    private FloatBox fbFocus;

    private SFFloat set_radius, set_focus;
    private SFFloat radius_changed, focus_changed;

    public static final int FOCUS = getNextEventID();

    //An object of this type will listen to each element's position box.
    private class FocusFieldListener extends NumberBox.Adapter {
        public void numChanged(NumberBox src, Number newVal) {
            //WSLPlayer wslPlayer = getWSLPlayer();
            clearWarning();
            float guess = newVal.floatValue();
            if (guess == 0.0f) {
                return;
            }
            if (Math.abs((focus - guess) / focus) <= .01f) {
                help = "Correct.  f = " + focus;
                fbFocus.setValue(focus);
            } else {
                help = "Incorrect f value of " + guess;
                fbFocus.setValue(0.0f);
            }

            appletUpdate(FOCUS);

            //wslPlayer.recordActionPerformed(getName(),"focus",String.valueOf(newVal));

            //if(!draggingWidget() && !applet.isAddingElement())
            //	wslPlayer.recordActionPerformed(getName(),"position",String.valueOf(newVal));
        }

        public void invalidEntry(NumberBox src, Number badVal) {
            setWarning("f must be between " + MIN_FOCUS + " and " + MAX_FOCUS + " cm.");
        }
    };

    public UnknownLens(Geometrical main, float position, int place, String vrmlString) {
        super(main, "UnknownLens", vrmlString, position,
              "To guess f, type in the 'Unknown f' input box.");
        //set_focus = (EventInSFFloat) getEAI().getEI(getNode(),"set_focalLength");

        index = place;
        focus = fValues[index];
        letterIdent = letters[index];

        //The indices put this box between the position and diameter boxen
        add(new JLabel("Unknown f:", Label.RIGHT), 3);
        add(fbFocus = makeFocusBox(), 4);
        add(new JLabel("cm"), 5);
        fbFocus.addNumberListener(new FocusFieldListener());
        validate();

        //EAI.Try eaitry=new EAI.Try(this);
        //focus_changed = (EventOutSFFloat) getEAI().getEO(getNode(),"focalLength_changed",eaitry,new Integer(FOCUS));
    }

    public void setFields() {
        //vrmlString
    }


    public float getFocalLength() {
        return focus;
    }

    public String getHelp() {
        //return help + getApplet().computeDistances(getID());
        getApplet().computeDistances(getID());
        return help;
    }

    protected FloatBox makeFocusBox() {
        return new FloatBox(MIN_FOCUS, MAX_FOCUS, 0, 3);
    }

    public void process(RayList rays) {
        RayList previous = rays;
        rays = rays.next;
        while (rays != null) {
            rays.xv -= rays.x / getFocalLength();
            rays.yv -= rays.y / getFocalLength();
            rays = rays.next;
        }
    }

    public String getName() {
        return getNamePrefix() + letterIdent;
    }

    public String getLetter() {
        return letterIdent;
    }

    public int getIndex() {
        return index;
    }

    protected String getNamePrefix() {
        return "UnknownLens";
    }

    public void guessFocus(float focus) {
        //focus=focus0;
        fbFocus.setValue(focus);
        //set_focus.setValue(focus);
    }

    public String toString() {
        try {
            return getClass().getName() + "[#" + getID() + ",pos=" + getPosition() +
                    ",d=" + getDiameter() + ",f=" + getFocalLength() + ']';
        } catch (NullPointerException e) { //not set up yet
            return super.toString();
        }
    }

    /*public WSLNode toWSLNode() {
     //Assuming that the prefix is the same as the WSL node name except for case:
     WSLNode node=new WSLNode(getNamePrefix().toLowerCase());
     final WSLAttributeList atts=node.getAttributes();

     atts.add("id", String.valueOf(getID()));
     atts.add("name", String.valueOf(getName()));
     atts.add("diameter", String.valueOf(getDiameter()));
     atts.add("focus", String.valueOf(focus));
     atts.add("position", String.valueOf(getPosition()));
     atts.add("index", String.valueOf(getIndex()));
     return node;
      }*/
}
