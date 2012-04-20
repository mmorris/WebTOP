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
import org.sdl.gui.numberbox.FloatBox;
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
public class ScalarCoupler extends Coupler implements NumberBox.Listener, ScalarWidget.Listener {
    public static class Converter {
        public Function toWidget,toFB;
        public Converter(Function towidget, Function tofb) {
            toWidget = towidget;
            toFB = tofb;
        }
    }


    private final FloatBox field;
    private final ScalarWidget widget;
    private final Converter converter;

    public ScalarCoupler(ScalarWidget sw, FloatBox fb, int digs) {
        this(sw, fb, digs, null);
    }

    //If con is null, no conversion (values assumed to be the same for widget/box)
    public ScalarCoupler(ScalarWidget sw, FloatBox fb, int digs, Converter con) {
        super(digs);
        field = fb;
        widget = sw;
        converter = con;
        fb.addNumberListener(this);
        sw.addListener(this);
    }

    public void release() {
        field.removeNumberListener(this);
        widget.removeListener(this);
    }

    //======EVENT HANDLING CODE======\\
    public void valueChanged(ScalarWidget src, float value) {
        if (src == widget) {
            if (widget.isActive()) {
                if (converter != null) value = (float) converter.toFB.eval(
                        value);
                if (isFixed()) field.setFixValue(value, getDigits());
                else field.setSigValue(value, getDigits());
            }
        } else System.err.println(
                "ScalarCoupler: unexpected valueChanged() from " + src);
    }

    public void numChanged(NumberBox source, Number newVal) {
        if (source == field) {
            if (!widget.isActive())
                widget.setValue(converter == null ? newVal.floatValue() :
                                (float)
                                converter.toWidget.eval(newVal.floatValue()));
        } else System.err.println(
                "ScalarCoupler: unexpected numChanged() from " + source);
    }

    //No action needed here; just check that it's from whom it should be
    public void boundsForcedChange(NumberBox source, Number oldVal) {
        if (source != field) System.err.println(
                "ScalarCoupler: unexpected boundsForcedChange() from " + source);
    }

    //Similarly no action needed; just check that it's from whom it should be
    public void invalidEntry(NumberBox source, Number badVal) {
        if (source == field) {
            if (widget.isActive()) System.err.println(
                    "ScalarCoupler: invalid input from widget");
        } else System.err.println(
                "ScalarCoupler: unexpected invalidEntry() from " + source);
    }
}
