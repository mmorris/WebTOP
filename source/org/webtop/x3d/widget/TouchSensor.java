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
public class TouchSensor extends SpatialWidget {
        /**
             * Event name used by the VRML97 Specification.
             */
            public static final String ISOVER_OUT="isOver",ISACTIVE_OUT="isActive",HITPOINT_OUT="hitPoint_changed";

            //This class has no need for a typeID; it doesn't override callback().

            /**
             * Constructs a <code>TouchSensor</code> for the named sensor.
             * <br>See next constructor for parameter details.
             */
            /*public TouchSensor(EAI eai,String widgetName,short widgetId,String help) {
                    super(eai,widgetName,widgetId,help,ISOVER_OUT,ISACTIVE_OUT,null,null,ENABLED_IN,HITPOINT_OUT,null,null,null);
            }*/
            /**
             * Constructs a <code>TouchSensor</code> for the given <code>NamedNode</code>.
             *
             * @param eai the <code>EAI</code> object through which to access the VRML browser.
             * @param widgetNode the <code>NamedNode</code> to associate with this <code>TouchSensor</code>.
             * @param widgetId an arbitrary integer to be used to identify this <code>TouchSensor</code>.
             * @param help an arbitrary String, as for a help message.
             */
            public TouchSensor(SAI sai,NamedNode widgetNode,short widgetId,String help) {
                    super(sai,widgetNode,widgetId,help,ISOVER_OUT,ISACTIVE_OUT,null,null,ENABLED_IN,HITPOINT_OUT,null,null,null);
            }

            /**
             * Returns a <code>Widget</code> for the named VRML <code>TouchSensor</code>.
             * Parameters are as for the similar <code>TouchSensor</code> constructor.
             *
             * <p>The objects returned by <code>simpleTouch()</code> are more efficient
             * than full <code>TouchSensor</code> objects when the latter's
             * <code>hitPoint_changed</code> events are unneeded.
             */
            /*public static Widget simpleTouch(EAI eai,String widgetName,short widgetId,String help) {
                    return new Widget(eai,widgetName,widgetId,help,ISOVER_OUT,ISACTIVE_OUT,null,null,ENABLED_IN);
                    }*/
            /**
             * Returns a <code>Widget</code> for the given VRML <code>TouchSensor</code>.
             * Parameters are as for the similar <code>TouchSensor</code> constructor.
             *
             * <p>The objects returned by <code>simpleTouch()</code> are more efficient
             * than full <code>TouchSensor</code> objects when the latter's
             * <code>hitPoint_changed</code> events are unneeded.
             */
            public static Widget simpleTouch(SAI sai,NamedNode widgetNode,short widgetId,String help) {
                    return new Widget(sai,widgetNode,widgetId,help,ISOVER_OUT,ISACTIVE_OUT,null,null,ENABLED_IN);
            }

}
