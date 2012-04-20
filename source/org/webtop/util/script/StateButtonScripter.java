/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//StateButtonScripter.java
//Automates scripting with StateButtons.
//Davis Herring
//Created August 27 2003
//Updated March 31 2004
//Version 0.11

package org.webtop.util.script;

import org.webtop.component.StateButton;
import java.awt.event.*;

public class StateButtonScripter extends Scripter implements
		StateButton.Listener {
	private final StateButton button;
	private final String[] values;
	private final int defaultState;

	public StateButtonScripter(StateButton b,
			org.webtop.wsl.client.WSLPlayer player, String id, String param,
			String[] vals, int defVal) {
		super(player, id, param);
		if (b == null) {
			super.destroy(); // broken object should be discarded
			throw new NullPointerException("StateButton cannot be null.");
		}
		if (vals == null) {
			super.destroy();
			throw new NullPointerException("Values cannot be null.");
		}
		// defensive copying, yay
		values = (String[]) vals.clone();
		if (values.length != b.stateCount) {
			super.destroy();
			throw new IllegalArgumentException(
					"Must have a value for each StateButton state.");
		}
		if (defVal < 0 || defVal >= values.length) {
			super.destroy();
			throw new IndexOutOfBoundsException("Default value " + defVal
					+ " outside of range [0," + values.length + ").");
		}
		button = b;
		button.addListener(this);
		defaultState = defVal;
	}

	protected void setValue(String value) {
		int i;
		for (i = 0; i < values.length
				&& !org.webtop.util.WTString.equal(value, values[i]); ++i)
			;
		if (i == values.length)
			i = defaultState;

		button.setState(i);
	}

	protected String getValue() {
		return values[button.getState()];
	}

	protected void setEnabled(boolean on) {
		button.setEnabled(on);
	}

	// This is a bit overkill, but to be complete:
	protected void destroy() {
		button.removeListener(this);
		super.destroy();
	}

	public void stateChanged(StateButton sb, int state) {
		if (sb == button)
			recordActionPerformed(values[state]); // have to use new state
		else
			System.err
					.println("StateButtonScripter: unexpected stateChanged from "
							+ sb);
	}
}
