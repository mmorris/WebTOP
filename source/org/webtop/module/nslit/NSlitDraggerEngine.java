/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.nslit;

/**
 * Defines a class that takes two XDrag widgets and controls them
 * to conform to the boundaries of the NSlit aperture screen.
 */

import org.webtop.x3d.widget.*;

public class NSlitDraggerEngine {

    //***** Widgets to control. *****//
    XDragWidget distanceWidget, widthWidget;

    public NSlitDraggerEngine(XDragWidget distDragger, XDragWidget widthDragger) {
        distanceWidget = distDragger;
        widthWidget    = widthDragger;


    }
}
