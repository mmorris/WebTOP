/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.component;

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
public class ToggleButton extends StateButton {

    //Strings to hold the title of each state, in
    //case the module wants them for something
    private String state0, state1;

    //Creates the default toggle button, which starts in the "on" state.
    public ToggleButton(String turnOffLabel, String turnOnLabel) {
        this(turnOffLabel, turnOnLabel, false);
    }

    public ToggleButton(String turnOffLabel, String turnOnLabel, boolean off)
    {
        super(turnOffLabel,turnOnLabel,off);
        state0 = turnOffLabel;
        state1 = turnOnLabel;
    }

    //Returns the button label associated with the current state
    //Can be used for more intuitive control code if desired
    public String getStateString()
    {
        if(getStateBool()==false)
            return state0;
        else
            return state1;
    }

    //Returns the current state as a boolean variable.  Since state
    public boolean getStateBool()
    {
        //If super.getState()==0, the toggle is switched off and the turnOnLabel is shown.
        if(super.getState()==0)
            return false;
        else
            return true;
    }
}
