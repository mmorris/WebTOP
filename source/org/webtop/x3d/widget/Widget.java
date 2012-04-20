/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.x3d.widget;

import java.util.Enumeration;
import java.util.EventListener;
import java.util.Vector;

import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.X3DField;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.webtop.x3d.InputNode;
import org.webtop.x3d.NamedNode;
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
public class Widget extends InputNode implements X3DFieldEventListener {
    /**
     * An object implementing <code>Widget.Listener</code> can be registered
     * to be notified of the events that <code>Widget</code> observes:
     * mouse entry and exit of a widget (as for highlighting/tooltips) and
     * mouse click and release on a widget (as for activation/parameter change).
     */
    public interface Listener extends EventListener {
        public void mouseEntered(Widget src);
        public void mouseExited(Widget src);
        public void mousePressed(Widget src);
        public void mouseReleased(Widget src);
    }


    /**
     * An adapter class for receiving <code>Widget</code> events.  This class
     * implements <code>Listener</code> but does nothing.
     */
    public static abstract class Adapter implements Listener {
        public void mouseEntered(Widget src) {}
        public void mouseExited(Widget src) {}
        public void mousePressed(Widget src) {}
        public void mouseReleased(Widget src) {}
    }


    /**
     * Standard event name used by the WebTOP VRML Library.
     */
    public static final String ISOVER_OUT = "isOver_out",
                                        ISACTIVE_OUT = "isActive_out",
                                        ISOVER_IN = "set_isOver",
                                        ISACTIVE_IN = "set_isActive", ENABLED_IN = "set_enabled";

    /**
     * Message emitted when an event handling routine throws a <code>RuntimeException</code>.
     */
    protected static final String EXCEPTION_MSG =
            "Exception occurred during widget event handling:";

    //Each widget class should record its type ID
    private static final short typeID = getNextInputTypeID(),
                                        //...And make their own event IDs (without regard as to uniqueness)
                                        ISOVER = 0,
                                        ISACTIVE = 1;

    /**
     * The help string for the <code>Widget</code>.  Provided for convenience;
     * never used by <code>Widget</code>.
     */
    public String tooltip;
    /**
     * The VRML name of the widget given upon construction of this <code>Widget</code>.
     * If the widget was constructed using an explicit node reference, is null.
     */
    //public final String name;
    /**
     * The VRML node associated with this <code>Widget</code>.
     */
    private final NamedNode node;
    /**
     * An arbitrary identification integer, given at construction.
     */
    public final short id;
    /**
     * Whether the mouse cursor is over this widget.
     */
    private boolean over;
    /**
     * Whether the user is interacting with this widget.
     */
    private boolean active;

    /**
     * The <code>Listener</code>s to inform of events.
     */
    private final Vector<Listener> listeners = new Vector<Listener>(1, 2);

    /**
     * <code>EventIn</code> through which to make the widget highlighted or not
     * for activity.
     */
    private SFBool set_isActive,
            /**
             * <code>EventIn</code> through which to make the widget highlighted or not
             * for mouse traversal.
             */
            set_isOver,
            /**
             * <code>EventIn</code> with which to enable or disable the widget.
             */
            set_enabled;

    /**
     * Constructs a <code>Widget</code> for the named widget.
     * <br>See next constructor for parameter details.
     *
     * <p>(The <code>sai</code> parameter for this constructor must have a valid
     * VRML browser reference, unlike that for the next constructor.)
     */
    /*public Widget(SAI sai,String widgetName,short widgetId,String help,
                                                            String isOver_out,String isActive_out,String isOver_in,String isActive_in,String enabled_in) {
            name=widgetName;
            id=widgetId;
            node=sai.getNode(widgetName);
     getEvents(sai,help,isOver_out,isActive_out,isOver_in,isActive_in,enabled_in);
                 }*/

    /**
     * Constructs a <code>Widget</code> for a <code>NamedNode</code>.
     * <p>If any of the event names given is <code>null</code>, no attempt will be made to
     * access that event.	 However, for the <code>eventIn</code>s,
     * <code>NullPointerException</code>s will be thrown if an attempt is later made to use
     * that event.	This does not apply to <code>isActive_in</code> or to <code>isOver_in</code>.
     * <p>All the events should have a data type of SFBool.
     *
     * <p>An <code>SAI</code> reference is required by this method to allow its error-checking
     * capacity to function.	Even an object given as '<code>new SAI()</code>' will suffice.
     *
     * @param sai the <code>SAI</code> object through which to access the VRML browser.
     * @param widgetNode the <code>NamedNode</code> to associate with this <code>Widget</code>.
     * @param widgetId an arbitrary integer to be used to identify this <code>Widget</code>.
     * @param help an arbitrary String, as for a help message.
     * @param isOver_out the name of the VRML <code>eventOut</code> sending <code>true</code>
     *									 when the mouse moves over the widget and <code>false</code> when it
     *									 moves away from it.
     * @param isActive_out the name of the VRML <code>eventOut</code> sending <code>true</code>
     *										 when the user clicks the mouse on the widget and <code>false</code>
     *										 when the user releases it.
     * @param isOver_in the name of the VRML <code>eventIn</code> through which one may cause
     *									the widget to appear traversed (with <code>true</code>) or inactive
     *									(with <code>false</code>).
     * @param isActive_in the name of the VRML <code>eventIn</code> through which one may cause
     *										the widget to appear active (with <code>true</code>) or inactive
     *										(with <code>false</code>).
     * @param enabled_in the name of the VRML <code>eventIn</code> through which one may cause
     *									 the widget to respond to the user (with <code>true</code>) or not to
     *									 do so (with <code>false</code>).
     *
     * @exception ClassCastException if the given events are not of the proper data type.
     */
    public Widget(SAI sai, NamedNode widgetNode, short widgetId, String help,
                  String isOver_out, String isActive_out, String isOver_in,
                  String isActive_in, String enabled_in) {
        id = widgetId;
        node = widgetNode;
        tooltip = help;
        X3DField dummy; //to give the casts somewhere to go
        if (isOver_out != null) dummy = (SFBool)
                                        sai.getOutputField(node, isOver_out, this,
                                        new Event(typeID, ISOVER));
        if (isActive_out != null) dummy = (SFBool)
                                          sai.getOutputField(node, isActive_out, this,
                                          new Event(typeID, ISACTIVE));
        if (isOver_in != null) set_isOver = (SFBool) sai.getInputField(node, isActive_in);
        if (isActive_in != null) set_isActive = (SFBool) sai.getInputField(node, isActive_in);
        if (enabled_in != null) set_enabled = (SFBool) sai.getInputField(node, enabled_in);
    }

    /**
     * Returns the VRML node associated with this widget.
     *
     * @return a <code>NamedNode</code> for the VRML node for this widget.
     */
    public NamedNode getNode() {
        return node;
    }

    /**
     * Checks if this widget is currently traversed.
     *
     * @return true if the user has placed the mouse over widget, or if
     *				 the widget has been set traversed; false otherwise.
     */
    public boolean isOver() {
        return over;
    }

    /**
     * Checks if this widget is currently active.
     *
     * @return true if the user has the widget "grabbed" with the mouse, or if
     *				 the widget has been set active; false otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Causes this widget to be traversed (or not).  The widget (if applicable) will
     * be drawn as if the user had indicated it, and events will be sent to <code>Listener</code>s
     * as if the user had entered (or exited) this widget with the mouse.  However, no action is taken if the widget is already in the state specified by the argument.
     *
     * @param on true to indicate the widget; false to deindicate it.
     */
    public void setOver(boolean on) {
        if (on ^ over) {
            over = on;
            if (set_isOver != null) set_isOver.setValue(on);
            processEvent(ISOVER, on);
        }
    }

    /**
     * Causes this widget to be active (or not).	The widget (if applicable) will
     * be drawn as if the user was interacting with it, and events will be sent
     * to <code>Listener</code>s as if the user had pressed (or released) the mouse
     * on this widget.	However, no action is taken if the widget is already in
     * the state specified by the argument.
     *
     * @param on true to activate the widget; false to deactivate it.
     */
    public void setActive(boolean on) {
        if (on ^ active) {
            active = on;
            if (set_isActive != null) set_isActive.setValue(on);
            processEvent(ISACTIVE, on);
        }
    }

    /**
     * Affects whether this widget is responsive to the user.
     *
     * @param true to let the user use the widget; false to cause the widget to be inert.
     */
    public void setEnabled(boolean on) {
        set_enabled.setValue(on);
    }

    /**
     * Registers the given object to be notified of VRML events listened to by this Widget.
     * Does nothing if parameter is null.
     */
    public void addListener(Listener toAdd) {
        if (toAdd != null && !listeners.contains(toAdd)) listeners.addElement(toAdd);
    }

    /**
     * Stops notifying the given object of VRML events.	 Does nothing if the given object
     * is null or not registered to be notified of such events.
     */
    public void removeListener(Listener toRemove) {
        listeners.removeElement(toRemove);
    }

    public void readableFieldChanged(X3DFieldEvent x3dFieldEvent) {
        final Event e = (Event) x3dFieldEvent.getData();
        if(e.typeID!=typeID) {
            //nobody else to handle it; give up
            System.err.println("Widget: unexpected event: " + e);
            return;
        }
        boolean on = ((SFBool) x3dFieldEvent.getSource()).getValue();
        if (e.eventID == ISACTIVE) {
            if (active == on)return; //redundant call -- possibly setActive()s mixed with user interaction
            active = on;
        }
        processEvent(e.eventID, on);
    }

    //===INTERNAL EVENT HELPER METHODS===\\
    protected void processEvent(int event, boolean on) {
        Enumeration en = listeners.elements();
        while (en.hasMoreElements())
            try {
                Listener target = (Listener) en.nextElement();
                switch (event) {
                case ISACTIVE:
                    processMouseClick(target, on);
                    break;
                case ISOVER:
                    processMouseTraversal(target, on);
                    break;
                default:
                    System.err.println("Widget: unexpected eventID: " + event);
                    return;
                }
            } catch (RuntimeException e) {
                System.err.println(EXCEPTION_MSG);
                e.printStackTrace();
            }
    }

    private void processMouseTraversal(Listener target, boolean into) {
        if (into) target.mouseEntered(this);
        else target.mouseExited(this);
    }

    private void processMouseClick(Listener target, boolean down) {
        if (down) target.mousePressed(this);
        else target.mouseReleased(this);
    }
}
