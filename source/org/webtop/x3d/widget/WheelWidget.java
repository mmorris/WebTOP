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

import org.web3d.x3d.sai.SFFloat;
import org.webtop.x3d.*;

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
public class WheelWidget extends ScalarWidget {
        /**
         * Event name used by the VRMLLib <code>WheelWidget</code>.
         */
        public static final String VALUE_OUT = "value_changed", VALUE_IN = "set_value",
                           MIN_IN = "set_minValue", MAX_IN = "set_maxValue",
                           MAXROTS_IN = "set_maxRotations";

        //This class has no need for a typeID; it doesn't override readableFieldChanged.

        /**
         * The <code>X3DField</code> with which to set the number of rotations needed
         * to cover the wheel's full range.
         */
        private SFFloat set_maxRots;

        /**
         * Constructs a <code>WheelWidget</code> for the named widget.
         * <br>See next constructor for parameter details.
         */
        /*public WheelWidget(EAI sai,String widgetName,short widgetId,String help) {
                super(sai,widgetName,widgetId,help,ISOVER_OUT,ISACTIVE_OUT,null,ISACTIVE_IN,ENABLED_IN,VALUE_OUT,VALUE_IN,MIN_IN,MAX_IN);
                getEvent(sai);
                     }*/
        /**
         * Constructs a <code>WheelWidget</code> for the given <code>NamedNode</code>.
         *
         * @param sai the <code>EAI</code> object through which to access the VRML browser.
         * @param widgetNode the <code>NamedNode</code> to associate with this <code>WheelWidget</code>.
         * @param widgetId an arbitrary integer to be used to identify this <code>WheelWidget</code>.
         * @param help an arbitrary String, as for a help message.
         */
        public WheelWidget(SAI sai, NamedNode widgetNode, short widgetId, String help) {
            super(sai, widgetNode, widgetId, help, ISOVER_OUT, ISACTIVE_OUT, null,
                  ISACTIVE_IN, ENABLED_IN, VALUE_OUT, VALUE_IN, MIN_IN, MAX_IN);

            set_maxRots = (SFFloat) sai.getInputField(getNode(), MAXROTS_IN);
        }

        public void setMaxRotations(float value) {
            set_maxRots.setValue(value);
        }

}
