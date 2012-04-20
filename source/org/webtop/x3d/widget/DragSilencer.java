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

//import webtop.util.script.NumberBoxScripter;

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

/*
public class DragSilencer implements Widget.Listener {
    private final Widget widget;
    private final NumberBoxScripter nbscripter;
    private boolean wasEnabled; //stores prior value of nbscripter.enabled

    public DragSilencer(Widget widget, NumberBoxScripter scripter) {
        if (widget == null)throw new NullPointerException("Widget cannot be null.");
        if (scripter == null)throw new NullPointerException("NumberBoxScripter cannot be null.");
        this.widget = widget;
        nbscripter = scripter;
        widget.addListener(this);
    }

    //We don't care about these events; we just check that they're from whom they should be
    public void mouseEntered(Widget src) {
        if (src != widget) System.err.println("DragSilencer: unexpected mouseEntered from " + src);
    }

    public void mouseExited(Widget src) {
        if (src != widget) System.err.println("DragSilencer: unexpected mouseEntered from " + src);
    }

    public void mousePressed(Widget src) {
        if (src == widget) {
            wasEnabled = nbscripter.enabled;
            nbscripter.enabled = false;
        } else System.err.println("DragSilencer: unexpected mousePressed from " + src);
    }

    public void mouseReleased(Widget src) {
        if (src == widget) {
            //do nothing if scripter has otherwise been restored
            if (!nbscripter.enabled)
                nbscripter.enabled = wasEnabled;
        } else System.err.println("DragSilencer: unexpected mouseReleased from " + src);
    }

}*/
