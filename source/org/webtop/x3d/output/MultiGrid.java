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
public class MultiGrid extends AbstractGrid {
    private final AbstractSwitch s;
    private final AbstractGrid[] g;
    private int which; // may differ from s's current value

    public MultiGrid(AbstractGrid[] grids) {
        this(null, grids, 0);
    }
    
    public MultiGrid(AbstractSwitch sw, AbstractGrid[] grids) {
        this(sw, grids, 0);
    }

    public MultiGrid(AbstractSwitch sw, AbstractGrid[] grids, int init) {
        if (sw == null)System.out.println("no AbstractSwitch given");
        if (grids == null || grids.length == 0)
            throw new NullPointerException("no grids given");
        g = (AbstractGrid[]) grids.clone(); // defensive copying -- yay
        s = sw;
        for (int i = 0; i < g.length; ++i) {
            if (g[i] == null)throw new NullPointerException("null grid given");
            //else g[i].setup();
        }
        which = init;
    }

    public void showGrid(int w) {
        which = w;
    } // setup() makes this take effect

    public AbstractGrid current() {
        return g[which];
    }

    public void setup() {
        current().setup();
        if(s!=null) s.setChoice(which);
    }


    public void setColors(float[][] colors) {
        current().setColors(colors);
    }

    public String toString() {
        return getClass().getName() + "[switch=" + s + ';' + which + '/' + g.length + ']';
    }
}
