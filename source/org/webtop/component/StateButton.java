/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//StateButton.java
//Declares a class for a button with multiple states (toggle, for 2).
//Davis Herring
//Created November 9 2002
//Updated February 11 2004
//Version 1.04

package org.webtop.component;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

public class StateButton extends JButton implements ActionListener {
    public interface Listener {
        //sb.getState() is old state
        public void stateChanged(StateButton sb, int state);
    }


    private final Vector<Listener> listeners = new Vector<Listener>(2, 2);
    private final String[] prefixes, postfixes;
    private String rootLabel;
    public final int stateCount; //may be useful for such things as Scripters
    protected int state;  //Changed from private to protected to allow ToggleSwitchButton
    private boolean actioncmd; //is one explicitly set?

    //For the usual case of hiding/showing stuff:
    public static final int VISIBLE = 0, HIDDEN = 1;
    private static final String[] togglePrefixes = {"Hide ", "Show "},
            togglePostfixes = {"", ""};

    //Creates a toggle button with the toggleP*fixes
    public StateButton(String label) {
        this(label, false);
    }

    public StateButton(String label, boolean hide) {
        this(label, togglePrefixes, togglePostfixes, hide ? 1 : 0);
    }

    //Creates a generalized toggle button
    public StateButton(String label1, String label2, boolean initialState) {
        /*Ultimately all StateButton constructors should call the "Full constructor,"
          even the generalized toggle button type constructors.  [PC]*/
        this("", new String[] {label1, label2}, new String[] {"",""}, initialState ? 1 : 0);

        //The below is an older version which resulted in a button that never changed anything.  [PC]
        /*if (label1 == null || label2 == null) {
            throw new NullPointerException("Labels may not be null.");
        }
        prefixes = new String[] {label1, label2};
        postfixes = new String[] {"", ""};
        stateCount = 2;
        rootLabel = "";
        state = two ? 1 : 0;
        init();*/
    }

    public StateButton(String label, String[] pre, String[] post) {
        this(label, pre, post, 0);
    }

    //Full constructor: specifies root label, prefixes, postfixes, and initial state
    public StateButton(String label, String[] pre, String[] post, int state0) {
        addActionListener(this);
        //Defensive copying -- yay
        prefixes = (String[]) pre.clone();
        postfixes = (String[]) post.clone();
        if ((stateCount = prefixes.length) < 2) {
            throw new IllegalArgumentException("Must have at least two states.");
        }
        if (postfixes.length != stateCount) {
            throw new IllegalArgumentException("String count mismatch.");
        }
        for (int i = 0; i < stateCount; ++i) {
            if (prefixes[i] == null) {
                throw new NullPointerException("Prefix strings may not be null.");
            }
            if (postfixes[i] == null) {
                throw new NullPointerException("Postfix strings may not be null.");
            }
        }
        rootLabel = label;
        state = state0;
        init();
    }

    //Some common stuff
    private void init() {
        updateLabel();
        //So we don't have to have a listener to switch states:
        //enableEvents(AWTEvent.ACTION_EVENT_MASK);
    }

    public void setText(String label) {
        if (label == null) {
            throw new NullPointerException("Label cannot be null.");
        }
        rootLabel = label;
        updateLabel();
    }

    public void setState(int nustate) {
        if (nustate >= stateCount || nustate < 0) {
            throw new IndexOutOfBoundsException("State number out of range.");
        }
        setState0(nustate);
    }

    public int getState() {
        return state;
    }

    public void addListener(Listener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    public void removeListener(Listener l) {
        listeners.removeElement(l);
    }

    //Raises an ActionEvent and stateChanged event from this button (does not affect state)
    public void ping() {
        super.fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                                                  getActionCommand()));
        processStateChangedEvent(state);
    }

    //Override to make more interesting state transition functions.
    protected int nextState(int curState) {
        return (curState + 1) % stateCount;
    }

    public void setActionCommand(String s) {
        actioncmd = (s != null);
        super.setActionCommand(s);
    }

    //The action command for a StateButton defaults to its root label instead of
    //to its current label for ease of recognition.
    public String getActionCommand() {
        return actioncmd ? super.getActionCommand() : rootLabel;
    }

    public void actionPerformed(ActionEvent e) {
        setState0(nextState(state));
    }

    private void updateLabel() {
        super.setText(prefixes[state] + rootLabel + postfixes[state]);
    }

    //Changed from private to protected to allow ToggleSwitchButton
    protected void setState0(int nustate) {
        if (state == nustate) {
            return;
        }
        processStateChangedEvent(nustate);
        state = nustate;
        updateLabel();
    }

    private void processStateChangedEvent(int nustate) {
        synchronized (listeners) {
            Enumeration e = listeners.elements();
            while (e.hasMoreElements()) {
                try {
                    ((Listener) e.nextElement()).stateChanged(this, nustate);
                } catch (RuntimeException re) {
                    System.err.println("Exception occurred during component event handling:");
                    re.printStackTrace();
                }
            }
        }
    }

    public String toString() {
        return getClass().getName() + "[state=" + state + " (" + getText() + ")]";
    }
}
