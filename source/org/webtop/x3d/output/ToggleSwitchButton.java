/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.x3d.output;

import org.webtop.component.ToggleButton;
import org.webtop.x3d.output.Switch;
import java.awt.event.ActionEvent;

public class ToggleSwitchButton extends ToggleButton {
    //Switch to manage
    Switch toggler;

    public ToggleSwitchButton(Switch sw, String turnOnLabel, String turnOffLabel, boolean on) {
        this(turnOnLabel, turnOffLabel, on);
        toggler = sw;
    }

    private ToggleSwitchButton(String turnOnLabel, String turnOffLabel, boolean on) {
        super(turnOnLabel, turnOffLabel, on);
    }

    //Overridden to allow changing of state in Switch also
    public void actionPerformed(ActionEvent e) {
        setState0(nextState(state));
        toggler.setVisible(this.getStateBool());
    }

}
