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

//Notes on the Grid class hierarchy:

//Resolutions for Grid objects are in terms of the points (a.k.a. nodes)
//between cells.  For example, if you use colorPerVertex FALSE, you will need
//to pass (getMRes()-1)*(getNRes()-1) values to setColors().

//Also note that calls to alter a Grid's geometry will not take effect until
//the next call to setup().  (It would be horribly inconvenient to change
//multiple values otherwise.)  Note that setup() will return immediately if no
//changes need to be made, so it's safe to call it whenever it might be
//needed.

//Grid classes should be able to take node objects directly (such that even
//clients' VRMLObjects can be used)!

public abstract class Grid extends AbstractGrid {
    //VRML's event name for Color nodes -- used by all kinds of grids!
    public static final String SET_COLOR = "set_color";

    public static final int CUSTOM_RES = -1;

    private final int resolutions[][]; //the predefined resolutions
    private final int rescount; //shorthand for resolutions.length
    private int resolution = CUSTOM_RES; //index into resolutions or else CUSTOM_RES

    private int mres, nres; //the actual resolution values (could be array, but why?)

    private float width, height;

    private boolean dirty = true; //do we need to re-setup the grid?

    public Grid(int[][] res, float w, float h) {
        //Lots of defensive copying -- I think this is the proper order of
        //operations...
        resolutions = (int[][]) res.clone();
        rescount = resolutions.length;
        for (int i = 0; i < rescount; ++i) resolutions[i] = (int[]) resolutions[i].clone();

        setWidth(w);
        setHeight(h);

        //Should this "setup" one of the resolutions?  If so, it should provide an
        //option to pick which one is used initially.
        //It shouldn't; subclasses have not constructed yet, and it's not implied in the concept of a Grid object anyway.
        //However, setup() should make sense immediately after construction, so:
        if (rescount > 0) setResolution(0);
    }

    public int getResolution() {
        return resolution;
    }

    public int getMRes() {
        return mres;
    }

    public int getNRes() {
        return nres;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setWidth(float w) {
        if (w <= 0)throw new IllegalArgumentException("Must have positive width");
        if (width != w) {
            width = w;
            dirty();
        }
    }

    public void setHeight(float h) {
        if (h <= 0)throw new IllegalArgumentException("Must have positive height");
        if (height != h) {
            height = h;
            dirty();
        }
    }

    public void setResolution(int which) {
        if (which < 0 || which >= rescount)throw new IndexOutOfBoundsException(
                "Bad resolution index");
        if (resolution != which) {
            resolution = which;
            mres = resolutions[resolution][0];
            nres = resolutions[resolution][1];
            dirty();
        }
    }

    public void setResolution(int m, int n) {
        //Should this check to see if the given values match a known resolution?
        if (m <= 0 || n <= 0)throw new IllegalArgumentException("Resolutions must be positive");
        if (mres != m || nres != n) {
            mres = m;
            nres = n;
            resolution = CUSTOM_RES;
            dirty();
        }
    }

    protected void dirty() {
        dirty = true;
    }

    protected abstract void setup0();

    public void setup() {
        if (dirty) {
            dirty = false;
            setup0();
        }
    }

    public String toString() {
        return getClass().getName() + '[' + mres + 'x' + nres + ',' + width + 'x' + height +
                (dirty ? " (dirty)" : "") + ']';
    }

}
