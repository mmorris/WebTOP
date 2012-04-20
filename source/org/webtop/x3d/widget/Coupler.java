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

import org.sdl.gui.numberbox.NumberBox;

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
public abstract class Coupler implements NumberBox.Listener, org.webtop.util.Helper {
    private boolean fixed=true;		//partly to allow construction with 0 digits
    private int digits; //places or significant digits to keep from widget's value

    public Coupler(int digs) {
        digits = digs;
    }

    //How many digits to use
    public void setDigits(int digs) {
        if (digs < (fixed ? 0 : 1))throw new IllegalArgumentException(
                "Bad number of digits: " + digs);
        digits = digs;
    }

    public int getDigits() {
        return digits;
    }

    //If false, digits is significant digits
    public void setFixed(boolean fix) {
        if (fixed ^ fix) {
            if (digits == 0 && !fix)throw new IllegalStateException(
                    "Can't make 0 digits significant.");
            fixed = fix;
        }
    }

    public boolean isFixed() {
        return fixed;
    }

}
