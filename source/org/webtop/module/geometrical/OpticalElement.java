/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Should this perhaps instead extend VRMLObject and manage a panel, or maybe just manage a VRMLObject? [Davis]

package org.webtop.module.geometrical;

import java.awt.*;
import javax.swing.*;
//import vrml.external.field.*;
import org.webtop.util.WTString;
//import webtop.vrml.*;
import org.sdl.gui.numberbox.*;
//import webtop.wsl.script.*;
//import webtop.wsl.client.*;
//import webtop.wsl.event.*;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;

abstract class OpticalElement extends JPanel implements X3DFieldEventListener {
    protected static final int X = 0, Y = 1, Z = 2;
    public static final float NEW_OFFSET = 40, MIN_POSITION = 1, MAX_POSITION = 200;
    //event-designation integers (and a guest)
    public static final int POSITION = getNextEventID(), CREATED = getNextEventID(),
            REMOVED = getNextEventID(), //unused by OE
                      GUI_PRECISION = 4;
    public static final float POSITION_SCALE = 4; //run for your lives! (applet:VRML)

    public static final int MOUSE_OVER = getNextEventID(), MOUSE_CLICKED = getNextEventID();


    private Geometrical wapp;
    private SAI sai;
    private NamedNode node;
    protected SFBool set_enabled;
    private SFBool widgetVisibility;
    protected SFFloat nodePosition;
    private boolean nodeReady = false;

    //===========
    // LISTENERS
    //===========
    /*private static X3DFieldEventListener positionListener = new SAI.Try(new PositionListener()),
            mouseOverListener = new SAI.Try(new MouseOverListener()),
                                mouseClickListener = new SAI.Try(new MouseClickListener());*/

    //An object of this type will listen to each element's position box.
    private class PositionFieldListener extends NumberBox.Adapter {
        public void numChanged(NumberBox src, Number newVal) {
            //WSLPlayer wslPlayer = getWSLPlayer();
            clearWarning();
            float p = newVal.floatValue() / POSITION_SCALE;
            //DebugPrinter.println(getName() + ": position -> " + p * POSITION_SCALE +
            //                     (draggingWidget() ? " (drag)" : " (not a drag)"));
            if (!draggingWidget()) {
                nodePosition.setValue(p);
            }
            if (constructed) {
                appletUpdate(POSITION);
            }
            // peter - added !draggingWidget
            //if (!draggingWidget() && !wapp.isAddingElement()) {
            //    wslPlayer.recordActionPerformed(getName(), "position", String.valueOf(newVal));
            //}
        }

        public void invalidEntry(NumberBox src, Number badVal) {
            setWarning("Positions must be between " + MIN_POSITION + " and " + MAX_POSITION +
                       " cm.");
        }
    };

    protected FloatBox fbPosition;
    private int id = getNextID();
    private static int nextId, nextEventId;
    protected String help;
    private final boolean constructed;

    //Debugging information about time:
    private static long t0 = System.currentTimeMillis();
    private static float t() {
        return (System.currentTimeMillis() - t0) / 1000f;
    }

    //These guarantee unique identifiers for objects and events.
    private static synchronized int getNextID() {
        return nextId++;
    }

    //This isn't the cleanest thing ever, but it's nicer-looking [Davis]
    public static synchronized void resetIDs() {
        nextId = 0;
    }

    protected static synchronized int getNextEventID() {
        return nextEventId++;
    }

    public OpticalElement(Geometrical main, String nodeType, String fields, float position,
                          String tip) {
        wapp = main;
        help = tip;
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        fbPosition = new FloatBox(MIN_POSITION, MAX_POSITION, position, 3);
        fbPosition.addNumberListener(new PositionFieldListener());
        add(new JLabel("Position:", Label.RIGHT));
        add(fbPosition);
        add(new JLabel("cm"));

        X3DFieldEventListener waitOnElement = new X3DFieldEventListener() {
            public void readableFieldChanged(X3DFieldEvent e) {
                synchronized (this) {
                    setReady();
                    notifyAll();
                }
            }
        };
        sai = wapp.getSAI();
        //DebugPrinter.println(this +" waiting on node... t=" + t());
        synchronized (waitOnElement) {
            // node = sai.generateProto(nodeType + '{' + fields +
            //        " position " + position / POSITION_SCALE + '}');
            node = sai.generateProto(nodeType);
            //wapp.getAddElements().set1Value(0, node.node);
            //wapp.getAddElements().setValue(0, new X3DNode[] {node.node});
            sai.getScene().addRootNode(node.node);
            //node.node.realize();
            /*sai.getOutputField(node, "initialized", new SAI.Try(waitOnElement),null);
                         while (!nodeReady) {
                try {
                    waitOnElement.wait();
                } catch (InterruptedException e) {}
                         }*/
        }
        //DebugPrinter.println(this +" done waiting.  t=" + t());
        nodePosition = (SFFloat) sai.getInputField(node, "set_position");
        widgetVisibility = (SFBool) sai.getInputField(node, "set_widgetsVisible");
        set_enabled = (SFBool) sai.getInputField(node, "enabled");
        /*sai.getOutputField(node, "position_changed", positionListener, this);
                 sai.getOutputField(node, "isOver_out", mouseOverListener, this);
                 sai.getOutputField(node, "underDrag_out", mouseClickListener, this);
                 sai.getOutputField(node, "underDrag_out", wapp.saitry, null);*/

        sai.getOutputField(node, "position_changed", this, new Integer(POSITION));
        sai.getOutputField(node, "isOver_out", this, new Integer(MOUSE_OVER));
        sai.getOutputField(node, "underDrag_out", this, new Integer(MOUSE_CLICKED));
        //sai.getOutputField(node, "underDrag_out", wapp, new Integer(MOUSE_CLICKED));

        //For practical reasons explained in the VRML file, elements always show up with widgets visible:
        setWidgetVisibility(false);

        //Neither of these should be called yet; subclasses have not been
        //constructed, and other functions may need calling before external
        //interaction occurs.  In general, this breaks encapsulation badly.
        //[Davis] [Peter]
        //applet.registerElement(this);
        //activate();

        //new for X3D
        nodePosition.setValue(position / POSITION_SCALE);

        constructed = true;
    }

    public void readableFieldChanged(X3DFieldEvent e) {
        int mode = ((Integer) e.getData()).intValue();
        if (mode == POSITION) {
            float p = ((SFFloat) e.getSource()).getValue() * POSITION_SCALE;
            //if (oe.draggingWidget()) {
            //wslPlayer.recordMouseDragged(oe.getName(), "position", String.valueOf(p));
            //DebugPrinter.println("PositionListener: " + oe.getName() + " got " + p);
            setPosition(p);
        }
        else if (mode == MOUSE_OVER) {
            if (((SFBool) e.getSource()).getValue())
                activate();
        }
        else if (mode == MOUSE_CLICKED) {
        }

    }


public abstract void setFields();

private void setReady() {
    nodeReady = true;
}

public void setEnabled(boolean enabled) {
    set_enabled.setValue(enabled);
}

//Methods meant for external access
public float getPosition() {
    return fbPosition.getValue();
}

public void setPosition(float position) {
    fbPosition.setSigValue(position, GUI_PRECISION);
}

/*public void setPosition(float positionarg) {
 position=positionarg;
 float p=positionarg/POSITION_SCALE;
 nodePosition.setValue(p);
 System.out.println(positionarg);
  }*/
//public float getPosition() {return position;}

//This function exists particularly for hokeyness; it is an 'effective
//diameter' where a literal diameter would not make sense
//(i.e. ObservationScreen-s).
public abstract float getDiameter();

public String getName() {
    return getNamePrefix() + id;
}

public String getHelp() {
    return help;
}

public Geometrical getApplet() {
    return wapp;
}

public SAI getSAI() {
    return sai;
}

public NamedNode getNode() {
    return node;
}

public boolean draggingWidget() {
    return wapp.draggingWidget();
}

public int getID() {
    return id;
}

public void setID(String ID) {
    id = WTString.toInt(ID, id);
}

protected abstract String getNamePrefix();

public void setWidgetVisibility(boolean visible) {
    //DebugPrinter.println("widgets "+(visible?"ON":"OFF")+" for "+this);
    widgetVisibility.setValue(visible);
}

protected void activate() {
    wapp.setActiveElement(this);
}

protected void appletUpdate(int event) {
    wapp.update(this, event);
}

protected void setStatus(String s) {
    wapp.getStatusBar().setText(s);
}

protected void setWarning(String s) {
    wapp.getStatusBar().setWarningText(s);
}

protected void clearWarning() {
    wapp.getStatusBar().clearWarning();
}

public void destroyElement() {
    wapp.getRemoveElements().set1Value(0, node.node);
}

//Returns number of rays discarded
public abstract int clip(RayList rays);

//Type-specific action to be performed on rays that clip() admits
public abstract void process(RayList rays);

//Draws the border around the panel
/*public void paint(Graphics g) {
    Dimension size = getSize();
    g.setColor(getBackground());
    g.draw3DRect(1, 1, size.width - 3, size.height - 3, false);
    g.draw3DRect(2, 2, size.width - 5, size.height - 5, true);
}*/

//This forces the layout manager to leave space for the border.
public Insets getInsets() {
    Insets isets = super.getInsets();
    return new Insets(isets.top + 4, isets.left + 4, isets.bottom + 4, isets.right + 4);
}

//Does not print position, because can't know things to go with it.
//So this is just the generic string usable before an element is setup fully.
public String toString() {
    return getClass().getName() + "[#" + id + ']';
}

//public abstract WSLNode toWSLNode();

/*public WSLPlayer getWSLPlayer() {
    return applet.getWSLPlayer();
     }*/
}
