/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//DivergingLens.java
//Declares a ThinLens subclass with negative focal length.
//Davis Herring
//Created February 12 2003
//Updated February 10 2004
//Version 0.42

package org.webtop.module.geometrical;

import org.sdl.gui.numberbox.FloatBox;

class DivergingLens extends ThinLens {
    public DivergingLens(Geometrical main, float position) {
        super(main,
              "lensGeometry ConcaveLens { resolution 40 depthResolution 10 } focusRotation 0 1 0 3.14159265",
              position);
    }

    public void setFields() {
        //lensGeometry ConcaveLens { resolution 40 depthResolution 10 } focusRotation 0 1 0 3.14159265
    }


    protected FloatBox makeFocusBox() {
        return new FloatBox( -MAX_FOCUS, -MIN_FOCUS, -DEF_FOCUS, 3);
    }

    protected String getNamePrefix() {
        return "DivergingLens";
    }

    public void setFocus(float focus) {
        setFocus0( -focus);
    }
}
