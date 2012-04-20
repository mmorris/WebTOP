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
import java.awt.event.*;
import javax.swing.*;

//import vrml.external.field.*;
//import vrml.external.exception.*;

import org.sdl.gui.numberbox.*;
import org.webtop.util.*;
import org.web3d.x3d.sai.*;
import org.webtop.x3d.*;

//import webtop.vrml.*;
//import webtop.wsl.script.*;
//import webtop.wsl.client.*;
//import webtop.wsl.event.*;

abstract class ThinLens extends CircularElement implements X3DFieldEventListener { //,NumberBox.Listener
    public static final float MIN_FOCUS = 1, MAX_FOCUS = 100, DEF_FOCUS = 20; //minimum focal length WAS current radius
    private FloatBox fbFocus;

    private SFFloat set_radius, set_focus;
    private SFFloat radius_changed, focus_changed;


    ////////////////////////////////////////////////////////////////////////
    // Constants that denote type of event
    public static final int FOCUS = getNextEventID();

    ////////////////////////////////////////////////////////////////////////
    // Constructor
    protected ThinLens(Geometrical main, String fields, float position) {
        super(main, "Lens", fields, position,
              "Use the red cones to change the diameter or position of the lens.  Use the green cone to change its focal length.");

        //The indices put this box between the position and diameter boxen
        add(new JLabel("Focal Length:", Label.RIGHT), 3);
        add(fbFocus = makeFocusBox(), 4);
        add(new JLabel("cm"), 5);
        validate();

        //fbFocus.addNumberListener(this);
        fbFocus.addNumberListener(new FocusFieldListener());

        set_focus = (SFFloat) getSAI().getInputField(getNode(), "set_focalLength");

        /*SAI.Try saitry = new SAI.Try(this);
        focus_changed = (SFFloat) getSAI().getOutputField(getNode(), "focalLength_changed", saitry,
                new Integer(FOCUS));*/

        focus_changed = (SFFloat) getSAI().getOutputField(getNode(), "focalLength_changed", this,
                new Integer(FOCUS));

    }

    protected abstract FloatBox makeFocusBox();

    public float getFocalLength() {
        return fbFocus.getValue();
    }

    public String getHelp() {
        return help + getApplet().computeDistances(getID());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event manager
    public void readableFieldChanged(X3DFieldEvent e) {
       // if (!draggingWidget()) {
        //    return;
        //}
        int mode = ((Integer) e.getData()).intValue();

        //WSLPlayer wslPlayer = getWSLPlayer();

        if (mode == FOCUS) {
            setFocus(focus_changed.getValue() * POSITION_SCALE); // may be causing acceleration
            /*if (wslPlayer != null) {
                wslPlayer.recordMouseDragged(getNamePrefix() + getID(), "focus",
                                             String.valueOf(focus_changed.getValue() *
                        POSITION_SCALE));
                         }*/
        } else {
            super.readableFieldChanged(e);
            //System.err.println("ThinLens::callback: unexpected mode " + mode + '!');
        }
        return;
    }

    public abstract void setFocus(float focus);

    protected void setFocus0(float focus) {
        fbFocus.setSigValue(focus, GUI_PRECISION);
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

    private class FocusFieldListener extends NumberBox.Adapter {
        public void numChanged(NumberBox src, Number newVal) {
            //WSLPlayer wslPlayer = getWSLPlayer();
            clearWarning();
            float newValue = newVal.floatValue();
            //fbDiameter.setMax(Math.min(newValue/2,MAX_DIAMETER));
            DebugPrinter.println("focus -> " + newValue);
            //The focal length may be negative; we make it positive for the scene's benefit
            if (!draggingWidget()) {
                set_focus.setValue((float) Math.abs(newValue) / POSITION_SCALE);
            }
            appletUpdate(FOCUS);

            if (!draggingWidget()) {
                //wslPlayer.recordActionPerformed(getNamePrefix() + getID(), "focus",
                //                                String.valueOf(newVal));
            }
        }

        public void invalidEntry(NumberBox src, Number badVal) {
            setWarning("Focal length must be between " + fbFocus.getMin() + " and " +
                       fbFocus.getMax() + " cm");
        }
    };


    /*
      public void numChanged(NumberBox src, Number value) {
     WSLPlayer wslPlayer = getWSLPlayer();

     if(wslPlayer!=null &&
       (wslPlayer.getState()==wslPlayer.PLAYING ||
       wslPlayer.getState()==wslPlayer.PAUSING)) {
      return;
     }
     float newValue = value.floatValue();

     clearWarning();

     DebugPrinter.print(getName()+"::numChanged()... ");
     if(src == fbFocus) {
      //fbDiameter.setMax(Math.min(newValue/2,MAX_DIAMETER));
      DebugPrinter.println("focus -> " + newValue);
      //The focal length may be negative; we make it positive for the scene's benefit
      if(!draggingWidget()) set_focus.setValue((float)Math.abs(newValue)/POSITION_SCALE);
      appletUpdate(FOCUS);

      if(wslPlayer!=null &&
       (wslPlayer.getState()!=wslPlayer.PLAYING ||
        wslPlayer.getState()!=wslPlayer.PAUSING) && !draggingWidget()) {
      wslPlayer.recordActionPerformed(getName(),"focus",String.valueOf(newValue));
      }
     } else System.err.println("ThinLens::numChanged: unexpected NumberBox "+src+'!');
      }
      public void invalidEntry(NumberBox src, Number value) {
     setWarning("Focal length must be between "+fbFocus.getMin()+" and "+fbFocus.getMax()+" cm");
      }
      public void boundsForcedChange(NumberBox src, Number oldValue) {}
     */

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
        WSLNode node = new WSLNode(getNamePrefix().toLowerCase());
        final WSLAttributeList atts = node.getAttributes();

        atts.add("id", String.valueOf(getID()));
        atts.add("diameter", String.valueOf(getDiameter()));
        atts.add("focus", fbFocus.getText());
        atts.add("position", String.valueOf(getPosition()));
        return node;
         }*/
}
