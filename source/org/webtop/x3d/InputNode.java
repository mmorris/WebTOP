/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.x3d;

import org.web3d.x3d.sai.X3DFieldEventListener;

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
public abstract class InputNode extends AbstractNode implements X3DFieldEventListener {
    /**
     * Objects of type Event will be used to indicate which class should handle
     * an X3D event, and what kind of event it is.  Each class should define its
     * own event type constants (really constant, so switch and the like can be
     * used (binary incompatibility is impossible because only these classes
     * will read those constants).  Each class should also statically store a
     * type id (as returned from getNextWidgetTypeID()): then, if an Event's
     * typeID doesn't match a class's typeID, it should just call
     * super.callback() and return. [Davis...PC]
     */
    protected static class Event {
        public final short typeID, eventID;
        public Event(short tid, short eid) {
            typeID = tid;
            eventID = eid;
        }

        public String toString() {
            return getClass().getName() + "[type=" + typeID + ",event=" +
                    eventID + ']';
        }
    }


    /**
     * The next input class type ID to use.
     */
    private static short nextTypeID;
    /**
     * Returns a typeID value for use by a class. Should only be called once by
     * any given class.
     */
    protected static synchronized short getNextInputTypeID() {
        return nextTypeID++;
    }
}
