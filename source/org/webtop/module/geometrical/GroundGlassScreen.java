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
import org.webtop.util.*;
//import webtop.vrml.*;
import org.webtop.component.StateButton;
//import webtop.util.script.StateButtonScripter;
import org.sdl.gui.numberbox.*;
import org.sdl.math.FPRound;
import org.web3d.x3d.sai.*;
import org.webtop.x3d.*;

//import webtop.wsl.script.*;
//import webtop.wsl.client.*;
//import webtop.wsl.event.*;

class GroundGlassScreen extends OpticalElement implements X3DFieldEventListener, NumberBox.Listener,
        StateButton.Listener {
    public static final float MIN_WIDTH = 0.1f, MIN_HEIGHT = 0.1f, MAX_WIDTH = 25, MAX_HEIGHT = 50,
            DEF_WIDTH = 12, DEF_HEIGHT = 14, DEF_TRANS = 0;

    private SFVec2f set_size;
    private SFFloat set_transparency;
    private SFVec2f size_changed;
    private SFFloat transparency_changed;
    private SFVec3f mousePosition_changed;
    private SFBool set_textVisible;

    // for ruler
    private SFBool set_rulerVisible;
    private MFInt32 set_coordIndex;
    private MFVec3f set_coords;
    private SFVec3f set_ruler_translation;

    // ruler
    private float[][] points;
    private int[] indices;

    ////////////////////////////////////////////////////////////////////////
    // Constants that denote type of event
    public static final int SIZE = getNextEventID(), TRANSPARENCY = getNextEventID(),
            MOUSE_POSITION = getNextEventID(),
                             MOUSE_OUT = getNextEventID();

    private FloatBox fbWidth = new FloatBox(MIN_WIDTH, MAX_WIDTH, DEF_WIDTH, 3),
                               fbHeight = new FloatBox(MIN_HEIGHT, MAX_HEIGHT, DEF_HEIGHT, 3);

    private StateButton rulerToggle = new StateButton("", new String[] {"None", "Ruler"},
            new String[] {"", ""});
    //private StateButtonScripter rulerToggleScripter;

    // for poofing
    private Image theImage;

    private float transparency;

    public GroundGlassScreen(Geometrical main, float position) {
        super(main, "ObservationScreen", "size " + DEF_WIDTH + ' ' + DEF_HEIGHT, position,
              "Use the red cone to change the position of the screen.  Use the blue cube to change its size.");

        add(new JLabel("Width:", Label.RIGHT));
        add(fbWidth);
        add(new JLabel("cm"));
        add(new JLabel("Height:", Label.RIGHT));
        add(fbHeight);
        add(new JLabel("cm"));
        add(rulerToggle);
        validate();

        // for poofing
        //theImage=main.getImage();
        theImage = new Image(main, "ImagePoints2", "ImageColors2");

        fbWidth.addNumberListener(this);
        fbHeight.addNumberListener(this);

        set_size = (SFVec2f) getSAI().getInputField(getNode(), "set_size");
        set_transparency = (SFFloat) getSAI().getInputField(getNode(), "set_transparency");
        set_textVisible = (SFBool) getSAI().getInputField(getNode(), "set_textVisible");

        set_rulerVisible = (SFBool) getSAI().getInputField(getNode(), "set_rulerVisible");
        set_coordIndex = (MFInt32) getSAI().getInputField(getNode(), "set_rulerCoordIndex");
        set_coords = (MFVec3f) getSAI().getInputField(getNode(), "set_rulerCoords");
        set_ruler_translation = (SFVec3f) getSAI().getInputField(getNode(), "set_ruler_translation");

        SAI.Try saitry = new SAI.Try(this);
        size_changed = (SFVec2f) getSAI().getOutputField(getNode(), "size_changed", saitry,
                new Integer(SIZE));
        transparency_changed = (SFFloat) getSAI().getOutputField(getNode(), "transparency_changed",
                saitry, new Integer(TRANSPARENCY));
        mousePosition_changed = (SFVec3f) getSAI().getOutputField(getNode(), "mousePosition_out",
                saitry, new Integer(MOUSE_POSITION));

        rulerToggle.addListener(this);
        //rulerToggleScripter = new StateButtonScripter(rulerToggle,getWSLPlayer(),getName(),"ruler",new String[] {"Off","On"},1);
        rulerToggle.setState(1);

        resizeRuler(fbHeight.getValue());
        set_rulerVisible.setValue(false);
        set_transparency.setValue(0.5f);
        set_ruler_translation.setValue(new float[] {0f, 0f, -.3f});

    }

    public void setFields() {
        set_size.setValue(new float[] {DEF_WIDTH,DEF_HEIGHT});
    }

    public float getDiameter() {
        return (float) Math.sqrt(getX3DWidth() * getX3DWidth() + getX3DHeight() * getX3DHeight());
    }

    protected String getNamePrefix() {
        return "GroundGlassScreen";
    }

    public float getX3DWidth() {
        return fbWidth.getValue();
    }

    public float getX3DHeight() {
        return fbHeight.getValue();
    }

    public void setWidth(float w) {
        fbWidth.setSigValue(w, GUI_PRECISION);
    }

    public void setHeight(float h) {
        fbHeight.setSigValue(h, GUI_PRECISION);
    }

    public void setSigSize(float[] size) {
        setWidth(size[0]);
        setHeight(size[1]);
    }

    public void setTransparency(float t) {
        transparency = t;
        set_transparency.setValue(transparency);
    }

    public float getTransparency() {
        return transparency;
    }

    public void setTextVisible(boolean visible) {
        if (visible) {
            set_textVisible.setValue(true);
        } else {
            set_textVisible.setValue(false);
        }
    }

    public void resizeRuler(float height) {
        points = new float[50 * 2][3];
        indices = new int[50 * 3];
        int bound = (int) height / 2;
        int counter = 0;
        int indexCounter = 0;
        float size;
        for (int i = -bound; i <= bound; i++) {
            size = (i % 2 == 0) ? 1f : 0.5f;
            indices[indexCounter] = counter;
            points[counter][0] = -1 * size;
            points[counter][1] = i;
            points[counter][2] = 0f;
            counter++;
            indexCounter++;
            indices[indexCounter] = counter;
            points[counter][0] = size;
            points[counter][1] = i;
            points[counter][2] = 0f;
            counter++;
            indexCounter++;
            indices[indexCounter] = -1;
            indexCounter++;
        }
        set_coords.setValue(50 * 2, points);
        set_coordIndex.setValue(50 * 3, indices);
    }

    public void clear() {
        theImage.clear();
    }

    public void raytrace(RayList rays, int count) {
        theImage.process(rays, count);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event manager
    public void readableFieldChanged(X3DFieldEvent e) {
        int mode = ((Integer) e.getData()).intValue();

        if (mode == SIZE) {
            if (!draggingWidget()) {
                return;
            }
            float[] size = null;
            size_changed.getValue(size);
            DebugPrinter.println("GroundGlassScreen: size -> {" + size[0] + "w," + size[1] + "h}");
            fbWidth.setSigValue(size[0], GUI_PRECISION);
            fbHeight.setSigValue(size[1], GUI_PRECISION);

            //WSLPlayer wslPlayer = getWSLPlayer();
            //wslPlayer.recordMouseDragged(getNamePrefix() + getID(), "width", String.valueOf(size[0]));
            //wslPlayer.recordMouseDragged(getNamePrefix() + getID(), "height", String.valueOf(size[1]));
        } else if (mode == TRANSPARENCY) {
            setTransparency(transparency_changed.getValue());
            //getWSLPlayer().recordMouseDragged(getNamePrefix() + getID(), "transparency",
            //                                  String.valueOf(getTransparency()));
        } else if (mode == MOUSE_POSITION) {
            if (draggingWidget()) {
                return;
            }
            float[] pos = null;
            mousePosition_changed.getValue(pos);
            setStatus("{x,y,z}={" + FPRound.toSigVal(pos[0], 3) + ',' + FPRound.toSigVal(pos[1], 3) +
                      ',' + getPosition() + '}');
            return; //don't update!	 we haven't actually changed
        } else if (mode == MOUSE_OUT) {
            //Clear status bar on mouse exit
            if (!((SFBool) e.getSource()).getValue()) {
                setStatus(null);
            }
        } else {
            System.err.println("GroundGlassScreen::callback: unexpected mode " + mode + '!');
            return;
        }
        appletUpdate(mode);
    }

    public void stateChanged(StateButton button, int k) {
        if (button == rulerToggle) {
            System.out.println("test, " + "k=" + k);
            set_rulerVisible.setValue(k == 0);
        }
    }

    public void numChanged(NumberBox src, Number value) {
        //WSLPlayer wslPlayer = getWSLPlayer();
        float newValue = value.floatValue();
        DebugPrinter.print(getName() + "::numChanged()... ");

        if (src == fbWidth) {
            DebugPrinter.println("width -> " + newValue);
            if (!draggingWidget()) {
                set_size.setValue(new float[] {newValue, fbHeight.getValue()});
            }
            appletUpdate(SIZE);

            if (!draggingWidget()) {
                //wslPlayer.recordActionPerformed(getNamePrefix() + getID(), "width",
                //                                String.valueOf(newValue));
            }

            // for poofing
            //theImage.setIFSSize(newValue,fbHeight.getValue());

        } else if (src == fbHeight) {
            DebugPrinter.println("height -> " + newValue);
            if (!draggingWidget()) {
                set_size.setValue(new float[] {fbWidth.getValue(), newValue});
            }
            appletUpdate(SIZE);
            resizeRuler(newValue);

            if (!draggingWidget()) {
                //wslPlayer.recordActionPerformed(getNamePrefix() + getID(), "height",
                //                                String.valueOf(newValue));
            }

            // for poofing
            //theImage.setIFSSize(fbWidth.getValue(),newValue);

        } else {
            System.err.println("GroundGlassScreen: unexpected numChanged event from " + src + '!');
        }
    }

    public void invalidEntry(NumberBox src, Number value) {
        if (src == fbWidth) {
            setWarning("Width must be between " + MIN_WIDTH + " and " + MAX_WIDTH + " cm.");
        } else if (src == fbHeight) {
            setWarning("Height must be between " + MIN_HEIGHT + " and " + MAX_HEIGHT + " cm.");
        } else {
            System.err.println("GroundGlassScreen: unexpected invalidEvent from " + src);
        }
    }

    public void boundsForcedChange(NumberBox src, Number oldValue) {}

    public synchronized int clip(RayList rays) {
        int raysMissed = 0;
        float maxX = getX3DWidth() / 2, maxY = getX3DHeight() / 2;

        for (RayList walker = rays.next, previous = rays; walker != null; ) {
            if (Math.abs(walker.x) < maxX && Math.abs(walker.y) < maxY) {
                previous = walker;
                walker = walker.next;
            } else {
                raysMissed++;
                walker = previous.unhook();
            }
        }
        return raysMissed;
    }

    public void process(RayList rays) {}

    /*public WSLNode toWSLNode() {
        WSLNode node = new WSLNode("groundglassscreen");
        final WSLAttributeList atts = node.getAttributes();

        atts.add("id", String.valueOf(getID()));
        atts.add("width", fbWidth.getText());
        atts.add("height", fbHeight.getText());
        atts.add("position", String.valueOf(getPosition()));
        atts.add("transparency", String.valueOf(getTransparency()));
        rulerToggleScripter.addTo(node);
        return node;
    }*/

    public String toString() {
        try {
            return getClass().getName() + "[#" + getID() + ",pos=" + getPosition() +
                    ",size={" + getX3DWidth() + "w," + getX3DHeight() + "h}]";
        } catch (NullPointerException e) { //not set up yet
            return super.toString();
        }
    }
}
