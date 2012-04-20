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

import org.web3d.x3d.sai.*;
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
public class ScalarWidget extends Widget implements X3DFieldEventListener {
    /**
     * An object implementing <code>ScalarWidget.Listener</code> can be
     * registered to be notified of changes to the value of a
     * <code>ScalarWidget</code>.
     */
    public interface Listener extends EventListener {
        public void valueChanged(ScalarWidget src, float value);
    }


    private static final short typeID = getNextInputTypeID(),
                                        VALUE_CHANGED = 0;

    /**
     * The <code>Listener</code>s to inform of events.
     */
    private final Vector<Listener> listeners = new Vector<Listener>(1, 2);

    /**
     * The <code>EventIn</code> with which to set the value of the widget.
     */
    private SFFloat set_value,
            /**
             * The <code>EventIn</code> with which to set the minimum value of the widget.
             */
            set_min,
            /**
             * The <code>EventIn</code> with which to set the maximum value of the widget.
             */
            set_max;

    /**
     * Constructs a <code>ScalarWidget</code> for the named widget.
     * <br>See next constructor for parameter details.
     *
     * <p>(The <code>sai</code> parameter for this constructor must have a valid
     * VRML browser reference, unlike that for the next constructor.)
     */


    //////////////////////////////////////////////////////////////////////////////
    // TODO: This block also commented out- again breaking wavefront
    //public ScalarWidget(SAI sai,String widgetName,short widgetId,String help,
    //										String isOver_out,String isActive_out,String isOver_in,String isActive_in,String enabled_in,
    //										String value_out,String value_in,String min_in,String max_in) {
    //	super(sai,widgetName,widgetId,help,isOver_out,isActive_out,isOver_in,isActive_in,enabled_in);
    //	getEvents(sai,value_in,value_out,min_in,max_in);
    //	}



    /**
     * Constructs a <code>ScalarWidget</code> for a <code>NamedNode</code>.
     * <p>If any of the event names given is <code>null</code>, no attempt will be made to
     * access that event.	 However, for the <code>eventIn</code>s,
     * <code>NullPointerException</code>s will be thrown if an attempt is later made to use
     * that event.
     * <p>The events for widget values should have a data type of SFFloat; others
     * should be of type SFBool.
     *
     * <p>An <code>SAI</code> reference is required by this method to allow its error-checking
     * capacity to function.	Even an object given as '<code>new SAI()</code>' will suffice.
     *
     * @param sai the <code>SAI</code> object through which to access the VRML browser.
     * @param widgetNode the <code>NamedNode</code> to associate with this <code>ScalarWidget</code>.
     * @param widgetId an arbitrary integer to be used to identify this <code>ScalarWidget</code>.
     * @param help an arbitrary String, as for a help message.
     * @param isOver_out the name of the VRML <code>eventOut</code> sending <code>true</code>
     *									 when the mouse moves over the widget and <code>false</code> when it
     *									 moves away from it.
     * @param isActive_out the name of the VRML <code>eventOut</code> sending <code>true</code>
     *										 when the user clicks the mouse on the widget and <code>false</code>
     *										 when the user releases it.
     * @param isActive_in the name of the VRML <code>eventIn</code> through which one may cause
     *										the widget to appear active (with <code>true</code>) or inactive
     *										(with <code>false</code>).
     * @param enabled_in the name of the VRML <code>eventIn</code> through which one may cause
     *									 the widget to respond to the user (with <code>true</code>) or not to
     *									 do so (with <code>false</code>).
     * @param value_out the name of the VRML <code>eventOut</code> reporting new values for the
     *									widget.
     * @param value_in the name of the VRML <code>eventIn</code> through which to post new
     *								 values for the widget.
     * @param min_in the name of the VRML <code>eventIn</code> with which to set the minimum
     *							 value for the widget.
     * @param max_in the name of the VRML <code>eventIn</code> with which to set the maximum
     *							 value for the widget.
     *
     * @exception ClassCastException if the given events are not of the proper data type.
     */
    public ScalarWidget(SAI sai, NamedNode widgetNode, short widgetId,
                        String help,
                        String isOver_out, String isActive_out,
                        String isOver_in, String isActive_in, String enabled_in,
                        String value_out, String value_in, String min_in,
                        String max_in) {
        super(sai, widgetNode, widgetId, help, isOver_out, isActive_out,
              isOver_in, isActive_in, enabled_in);

        X3DField dummy;
        if (value_out != null) dummy = (SFFloat)
                                       sai.getOutputField(getNode(), value_out, this,
                                                 new Event(typeID, VALUE_CHANGED));
        if (value_in != null) set_value = (SFFloat) sai.getInputField(getNode(),
                value_in);
        if (min_in != null) set_min = (SFFloat) sai.getInputField(getNode(),
                min_in);
        if (max_in != null) set_max = (SFFloat) sai.getInputField(getNode(),
                max_in);


    }

    /**
     * Sets the minimum value allowed by this <code>ScalarWidget</code>.
     *
     * <p>In general, setting the maximum of a widget below its minimum
     * will cause it to be unbounded.
     */
    public void setMin(float value) {
        set_min.setValue(value);
    }

    /**
     * Sets the maximum value allowed by this <code>ScalarWidget</code>.
     *
     * <p>In general, setting the maximum of a widget below its minimum
     * will cause it to be unbounded.
     */
    public void setMax(float value) {
        set_max.setValue(value);
    }

    /**
     * Sets the value of this <code>ScalarWidget</code>.
     */
    public void setValue(float value) {
        set_value.setValue(value);
    }

    /**
     * Registers the given object to be notified of VRML events listened to by this Widget.
     */
    public void addListener(Listener toAdd) {
        if (toAdd != null && !listeners.contains(toAdd)) listeners.addElement(
                toAdd);
    }

    /**
     * Stops notifying the given object of VRML events.	 Does nothing if the given object
     * is null or not registered to be notified of such events.
     */
    public void removeListener(Listener toRemove) {
        listeners.removeElement(toRemove);
    }

    public void readableFieldChanged(X3DFieldEvent x3dFieldEvent) {
        final Event e=(Event) x3dFieldEvent.getData();
        if (e.typeID == typeID) {
            if (e.eventID == VALUE_CHANGED) {
                float value = ((SFFloat) x3dFieldEvent.getSource()).getValue();
                Enumeration en = listeners.elements();
                while (en.hasMoreElements())
                    try {
                        ((Listener) en.nextElement()).valueChanged(this, value);
                    } catch (RuntimeException re) {
                        System.err.println(EXCEPTION_MSG);
                        re.printStackTrace();
                    }
            } else System.err.println(
                    "ScalarWidget: unexpected Event in callback: " + e);
        } else super.readableFieldChanged(x3dFieldEvent);
    }
}
