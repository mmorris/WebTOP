/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.twomedia;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;

import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;
import org.webtop.util.script.*;

import org.webtop.util.*;
import org.sdl.math.FPRound;
import org.sdl.gui.numberbox.*;

public class LinearPanel extends JPanel implements NumberBox.Listener {
	private Engine engine;
	private JPanel panel1;
	private JPanel panel2;

	private FloatBox
		amplitude,
		wavelength,
		angle,
		n1,
		n2;

	private boolean internalChange;

	private LinearSource source;

	private NumberFormat nf;
	
	//Scripters for the float boxes
	private NumberBoxScripter amplitudeScripter, wavelengthScripter, angleScripter, 
			n1Scripter, n2Scripter;

	public LinearPanel(TwoMedia wave) {
		setLayout(new GridBagLayout());

		add(new JLabel("Amplitude:",JLabel.RIGHT), new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		amplitude = new FloatBox(0,10,4,4);
		amplitude.addNumberListener(this);
		add(amplitude, new GridBagConstraints(1,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));

		add(new JLabel("Wavelength:",JLabel.RIGHT), new GridBagConstraints(2,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		wavelength = new FloatBox(0,50,16,4);
		wavelength.addNumberListener(this);
		add(wavelength, new GridBagConstraints(3,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));

		add(new JLabel("Angle:",JLabel.RIGHT), new GridBagConstraints(4,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		angle = new FloatBox(0,90,45,4);
		angle.addNumberListener(this);
		add(angle, new GridBagConstraints(5,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));
		
		add(new JLabel("n1:",JLabel.RIGHT), new GridBagConstraints(6,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		n1 = new FloatBox(0,1000,1,4);
		n1.addNumberListener(this);
		add(n1, new GridBagConstraints(7,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));

		add(new JLabel("n2:",JLabel.RIGHT), new GridBagConstraints(8,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		n2 = new FloatBox(0,1000,1.5f,4);
		n2.addNumberListener(this);
		add(n2, new GridBagConstraints(9,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));

		
		//set up the float box scripters
		amplitudeScripter = new NumberBoxScripter(amplitude, wave.getWSLPlayer(), null, 
				"amplitude", 4.0);
		wavelengthScripter = new NumberBoxScripter(wavelength, wave.getWSLPlayer(), null, 
				"wavelength", 16.0);
		angleScripter = new NumberBoxScripter(angle, wave.getWSLPlayer(), null, 
				"angle", 45.0);
		n1Scripter = new NumberBoxScripter(n1, wave.getWSLPlayer(), null, 
				"n1", 1.0);
		n2Scripter = new NumberBoxScripter(n2, wave.getWSLPlayer(), null, 
				"n2", 1.5);
	}

	public void setEngine(Engine e) {
		engine = e;
	}

	public void show(LinearSource s) {
		if(s!=null) {
			amplitude.setValue(s.getAmplitude());
			amplitude.setEnabled(true);
			wavelength.setValue(s.getWavelength());
			wavelength.setEnabled(true);
			angle.setValue(WTMath.toDegs(s.getAngle()));
			angle.setEnabled(true);
			n1.setValue(s.getN1());
			n1.setEnabled(true);
			n2.setValue(s.getN2());
			n2.setEnabled(true);
		} else {
			amplitude.setEnabled(false);
			wavelength.setEnabled(false);
			angle.setEnabled(false);
			n1.setEnabled(false);
			n2.setEnabled(false);
		}
		source = s;
	}

	public void setWavelength(float w) {
		internalChange=true;
		wavelength.setValue(w);
		internalChange=false;
	}

	public void setAngle(float a) {
		internalChange=true;
		angle.setValue(WTMath.toDegs(a));
		internalChange=false;
	}

	public void setN1(float a) {
		internalChange=true;
		n1.setValue(a);
		internalChange=false;
	}

	public void setN2(float a) {
		internalChange = true;
		n2.setValue(a);
		internalChange = false;
	}

	public void setAmplitude(float a) {
		internalChange=true;
		amplitude.setFixValue(a, 2);
		internalChange=false;
	}

	public void numChanged(NumberBox eventSource, Number newVal) {
		//if(wslPlayer.isPlaying()) return;

		if(internalChange) return;

		float n;

		if(eventSource == amplitude) {
			DebugPrinter.println("setting amp");
			source.setAmplitude(amplitude.getValue());
		} else if(eventSource == wavelength) {
			DebugPrinter.println("setting wavelength");
			source.setWavelength(wavelength.getValue());
		} else if(eventSource == angle) {
			DebugPrinter.println("setting angle");
			float a = WTMath.toRads(angle.getValue());
			source.setAngle(a);
		} else if(eventSource == n1) {
			DebugPrinter.println("setting n1");
			n = n1.getValue();
			source.setN1(n);
		} else {
			DebugPrinter.println("setting n2");
			n = n2.getValue();
			source.setN2(n);
		}

		if(!engine.isPlaying()) engine.update();
	}

	public void invalidEntry(NumberBox src, Number badVal) {
		if(src == amplitude)
			source.statusBar.setWarningText("Amplitude must be between 0.0 and 10.0");
		else if(src == wavelength)
			source.statusBar.setWarningText("Wavelength must be between 0.0 and 50.0");
		else if(src == angle)
			source.statusBar.setWarningText("Angle must be between 0.0 and 90.0 degrees");
		else if(src == n1)
			source.statusBar.setWarningText("Index of refraction must be a positive value");
		else if(src == n2)
			source.statusBar.setWarningText("Index of refraction must be a positive value");
	}

	public void boundsForcedChange(NumberBox src, Number oldVal) {}
	
	protected void toWSLNode(WSLNode node){
		amplitudeScripter.addTo(node);
		wavelengthScripter.addTo(node);
		angleScripter.addTo(node);
		n1Scripter.addTo(node);
		n2Scripter.addTo(node);
	}
}
