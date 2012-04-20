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

import org.sdl.gui.numberbox.*;
import org.webtop.x3d.widget.PlanarWidget;
import org.sdl.math.Function;

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
public class PlanarCoupler extends Coupler implements NumberBox.Listener, PlanarWidget.Listener {
    public static class Converter {
        public Function.TwoD toWidget,toFB;
        public Converter(Function.TwoD towidget, Function.TwoD tofb) {
            toWidget = towidget;
            toFB = tofb;
        }
    }


    //Random idea: would an array of (all 2) FloatBoxes work better?
    private final FloatBox xfield, yfield;
    private final PlanarWidget widget;
    private final Converter converter;

    public PlanarCoupler(PlanarWidget pw, FloatBox fbx, FloatBox fby, int digs) {
        this(pw, fbx, fby, digs, null);
    }

    public PlanarCoupler(PlanarWidget pw, FloatBox fbx, FloatBox fby, int digs,
                         Converter con) {
        super(digs);
        xfield = fbx;
        yfield = fby;
        widget = pw;
        converter = con;
        fbx.addNumberListener(this);
        fby.addNumberListener(this);
        pw.addListener(this);
    }

    //These should be used instead of the FloatBoxes' corresponding methods!
    public void setXMax(float max_x) {
        xfield.setMax(max_x);
        widget.setMax(max_x, yfield.getMax());
    }
    public void setYMax(float max_y) {
        yfield.setMax(max_y);
        widget.setMax(xfield.getMax(), max_y);
    }
    public void setMax(float max_x, float max_y) {
        xfield.setMax(max_x);
        yfield.setMax(max_y);
        widget.setMax(max_x, max_y);
    }
    public void setXMin(float min_x) {
        xfield.setMin(min_x);
        widget.setMin(min_x, yfield.getMin());
    }
    public void setYMin(float min_y) {
        yfield.setMin(min_y);
        widget.setMin(xfield.getMin(), min_y);
    }
    public void setMin(float min_x, float min_y) {
        xfield.setMin(min_x);
        yfield.setMin(min_y);
        widget.setMin(min_x, min_y);
    }

    public void release() {
        xfield.removeNumberListener(this);
        yfield.removeNumberListener(this);
        widget.removeListener(this);
    }

    //======EVENT HANDLING CODE======\\
    public void valueChanged(PlanarWidget src, float valuex, float valuey) {
        if (src == widget) {
            if (widget.isActive()) {
                if (converter != null) {
                    double[] value = converter.toFB.eval(valuex, valuey);
                    valuex = (float) value[0];
                    valuey = (float) value[1];
                }
                if (isFixed()) {
                    xfield.setFixValue(valuex, getDigits());
                    yfield.setFixValue(valuey, getDigits());
                } else {
                    xfield.setSigValue(valuex, getDigits());
                    yfield.setSigValue(valuey, getDigits());
                }
            }
        } else System.err.println(
                "PlanarCoupler: unexpected valueChanged() from " + src);
    }

    public void numChanged(NumberBox source, Number newVal) {
        float x, y;
        if (source == xfield) {
            x = newVal.floatValue();
            y = yfield.getValue();
        } else if (source == yfield) {
            x = xfield.getValue();
            y = newVal.floatValue();
        } else {
            System.err.println("PlanarCoupler: unexpected numChanged() from " +
                               source);
            return;
        }
        if (converter != null) {
            double[] value = converter.toWidget.eval(x, y);
            x = (float) value[0];
            y = (float) value[1];
        }
        if (!widget.isActive()) widget.setValue(x, y);
    }

    //No action needed here; just check that it's from whom it should be
    public void boundsForcedChange(NumberBox source, Number oldVal) {
        if (source != xfield && source != yfield)
            System.err.println(
                    "PlanarCoupler: unexpected boundsForcedChange() from " +
                    source);
    }

    //Similarly no action needed; just check that it's from whom it should be
    public void invalidEntry(NumberBox source, Number badVal) {
        if (source == xfield || source == yfield) {
            if (widget.isActive()) System.err.println(
                    "PlanarCoupler: invalid input from widget");
        } else System.err.println(
                "PlanarCoupler: unexpected invalidEntry() from " + source);
    }
}
