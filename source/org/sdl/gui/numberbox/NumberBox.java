/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//NumberBox.java
//Davis Herring
//Defines the NumberBox, the superclass for numeric-text entry fields, as well
//as the listener interface, adapter, and an exception class for use with said
//fields
//Converted to Swing on July 12 2006 [Peter Gilbert]
//Created August 10 2001
//Updated July 12 2006
//Version 1.5.3

package org.sdl.gui.numberbox;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public abstract class NumberBox extends JTextField {
    public class Filter extends DocumentFilter {
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String t, AttributeSet attr) throws BadLocationException {
                if (check(t)) {
                    curStr = t;
                    fb.insertString(offset,t,attr);
                }
        }
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String t, AttributeSet attr) throws BadLocationException {
                if (check(t)) {
                    curStr = t;
                    fb.replace(offset,length,t,attr);
                }
        }
    }

    public interface Listener extends EventListener {
        public void numChanged(NumberBox source, Number newVal);

        public void boundsForcedChange(NumberBox source, Number oldVal);

        public void invalidEntry(NumberBox source, Number badVal);
    }


    public static abstract class Adapter implements Listener {
        public void numChanged(NumberBox source, Number newVal) {}

        public void boundsForcedChange(NumberBox source, Number oldVal) {}

        public void invalidEntry(NumberBox source, Number badVal) {}
    }


    public static class BoundsException extends java.lang.RuntimeException {
        private final NumberBox source;

        public BoundsException(NumberBox generator) {
            source = generator;
        }

        public BoundsException(NumberBox generator, String message) {
            super(message);
            source = generator;
        }

        public final NumberBox getSource() {
            return source;
        }
    }


    protected static final short EVENT_valChanged = 0,
            EVENT_invalidKey = 1,
                               EVENT_boundsForcedChange = 2,
            EVENT_invalidEntry = 3,
                                 EVENT_redundantEntry = 4;

    private Vector<Listener> listeners = new Vector<Listener>(2, 2);
    private boolean hush = false;
    private int lastCursorPos;
    protected String curStr;
    private Filter filter = new Filter();

    public NumberBox() {
        this(10);
    }

    public NumberBox(int columns) {
        super("0", columns);
        enableEvents(AWTEvent.FOCUS_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.TEXT_EVENT_MASK);
        ((AbstractDocument)getDocument()).setDocumentFilter(filter);
    }

    public final void setText(String s) {}

    protected final void setTheText(String s) {
        super.setText(curStr = s);
        if (isShowing()) {
            setCaretPosition(Math.min(lastCursorPos, s.length()));
        }
    }

    public abstract void setValue(Number val);

    public abstract Number getNumberValue();

    //Restores the -previous- good value.  Can be used in numChanged (perhaps in
    //combination with silence()) for more advanced error checking.
    public abstract void revert();

    public abstract boolean validateEntry();

    public final void addNumberListener(Listener toTell) {
        if (!listeners.contains(toTell)) {
            listeners.addElement(toTell);
        }
    }

    public final void removeNumberListener(Listener notToTell) {
        listeners.removeElement(notToTell);
    }

    //Prevents the posting of the next event this NumberBox would generate.
    //Use before 'housekeeping' calls you want to just occur.
    public final void silence() {
        hush = true;
    }

    //numChanged events that are already not sent because the new value is the
    //same as the old value count as the 'next' event.  Thus, (assuming that 2
    //and 3 are within the bounds of the NumberBox nb) nb.silence();
    //nb.setValue(2); nb.setValue(3); will always send a numChanged event on the
    //second set, even if the box held 2 initially.

    //Raises a numChanged() event with the box's current value.
    public void ping() {
        processNumberEvent(EVENT_valChanged, getNumberValue());
    }

    protected final void processNumberEvent(short evtType, Number val) {
        //Send no event if we've been silenced recently:
        if (hush) {
            hush = false;
            return;
        }

        Enumeration toTell = listeners.elements();
        while (toTell.hasMoreElements()) {
            Listener l = (Listener) toTell.nextElement();
            switch (evtType) {
            case EVENT_valChanged:
                l.numChanged(this, val);
                break;
            case EVENT_boundsForcedChange:
                l.boundsForcedChange(this, val);
                break;
            case EVENT_invalidEntry:
                l.invalidEntry(this, val);
                break;
            case EVENT_redundantEntry: //Don't need to send an event; just clear hush if set
            }
        }
    }

    protected void reset() {
        setTheText(curStr = getNumberValue().toString());
    }

    //Checks for could-be validity
    protected abstract boolean check(String s);

    public void processFocusEvent(FocusEvent e) {
        if (e.getID() == FocusEvent.FOCUS_LOST) {
            validateEntry();
        }
        super.processFocusEvent(e);
    }

    public void processKeyEvent(KeyEvent e) {
        if (isEditable() && e.getID() == e.KEY_TYPED) {
            if (e.getKeyChar() == '\n' && e.getModifiers() == 0) { // Enter
                validateEntry();
                selectAll();
            } else if (e.getKeyChar() == '\033' && e.getModifiers() == 0) { // Escape
                reset();
                selectAll();
            } else {
                lastCursorPos = getCaretPosition();
            }
        }
        super.processKeyEvent(e);
    }

    public String toString() {
        return hush ? " (silenced)" : "";
    }
}
