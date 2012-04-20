/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.threemedia;

import java.awt.*;

import javax.swing.JPanel;
import javax.swing.JLabel;
import org.sdl.gui.numberbox.*;
import org.webtop.util.WTMath;
import org.webtop.x3d.widget.*;
import org.webtop.component.*;

import org.webtop.wsl.client.*;
import org.webtop.wsl.script.*;
import org.webtop.util.script.*;

public class LinearPanel extends JPanel implements NumberBox.Listener{
 
	public static final long serialVersionUID = 0; //to shut eclipse up
	private Engine engine;
	private JPanel panel1;
	private JPanel panel2;

	private FloatBox ampField;
	private FloatBox wavelength;
	private FloatBox angle;
	private FloatBox n1;
	private FloatBox n2;
	private FloatBox n3;
	private FloatBox distance;
	
	private ScalarCoupler distCoupler;
	private ScalarWidget boundary; 

	private boolean internalChange;

	private LinearSource source;
	
	//WSL Scripting stuffs
	private NumberBoxScripter ampFieldScripter, 
							  wavelengthScripter, 
							  angleScripter, 
							  n1Scripter, 
							  n2Scripter, 
							  n3Scripter, 
							  distanceScripter;
	
	
	public LinearPanel(ThreeMedia wave) {
		setLayout(new GridBagLayout());

		add(new JLabel("Amplitude:",JLabel.RIGHT), new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		ampField = new FloatBox(0,10,4,4);
		ampField.addNumberListener(this);
		add(ampField, new GridBagConstraints(1,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));

		add(new JLabel("Wavelength:",JLabel.RIGHT), new GridBagConstraints(2,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		wavelength = new FloatBox(2,40,15,4);
		wavelength.addNumberListener(this);
		add(wavelength, new GridBagConstraints(3,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));

		add(new JLabel("Angle:",JLabel.RIGHT), new GridBagConstraints(4,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		angle = new FloatBox(0,89.999999f,45,4);
		angle.addNumberListener(this);
		add(angle, new GridBagConstraints(5,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));

		add(new JLabel("n1:",JLabel.RIGHT), new GridBagConstraints(6,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		n1 = new FloatBox(1,Float.POSITIVE_INFINITY,1.0f,4);
		n1.addNumberListener(this);
		add(n1, new GridBagConstraints(7,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));

		add(new JLabel("n2:",JLabel.RIGHT), new GridBagConstraints(8,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		n2 = new FloatBox(1,Float.POSITIVE_INFINITY,1.25f,4);
		n2.addNumberListener(this);
		add(n2, new GridBagConstraints(9,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));

		add(new JLabel("n3:",JLabel.RIGHT), new GridBagConstraints(10,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		n3 = new FloatBox(1,Float.POSITIVE_INFINITY,1.50f,4);
		n3.addNumberListener(this);
		add(n3, new GridBagConstraints(11,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));

		add(new JLabel("d:",JLabel.RIGHT), new GridBagConstraints(12,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,2),0,0));
		distance = new FloatBox(0,Float.POSITIVE_INFINITY,25,4);
		distance.addNumberListener(this);
		add(distance, new GridBagConstraints(13,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2,3,2,6),0,0));
		
		ampFieldScripter = new NumberBoxScripter(ampField,wave.getWSLPlayer(), null, 
				"amplitude", 4);
		wavelengthScripter = new NumberBoxScripter(wavelength, wave.getWSLPlayer(), null, 
				"wavelength", 15);
		angleScripter = new NumberBoxScripter(angle, wave.getWSLPlayer(), null, 
				"angle", 45);
		n1Scripter = new NumberBoxScripter(n1, wave.getWSLPlayer(), null, 
				"n1", 1);
		n2Scripter = new NumberBoxScripter(n2, wave.getWSLPlayer(), null, 
				"n2", 1.25);
		n3Scripter = new NumberBoxScripter(n3, wave.getWSLPlayer(), null, 
				"n3", 1.50);
		distanceScripter = new NumberBoxScripter(distance, wave.getWSLPlayer(), null,
				"distance", 25);
		
	}
	
	public void setEngine(Engine e) {
		engine = e;
	}

	public void showSource(LinearSource s) {
		if(s!=null) {
			ampField.setEnabled(true);
			wavelength.setEnabled(true);
			angle.setEnabled(true);
			n1.setEnabled(true);
			distance.setEnabled(true);
			setAmplitude(s.getAmplitude());
			setWavelength(s.getWavelength());
			setAngle(s.getAngle());
			setN1(s.getN1());
			setN2(s.getN2());
			setN3(s.getN3());
			setDistance(s.getDistance());
		} else {
			ampField.setEnabled(false);
			wavelength.setEnabled(false);
			angle.setEnabled(false);
		}
		source = s;
	}

	public void setAmplitude(float a) {
		internalChange=true;
		ampField.setFixValue(a, 2);
		internalChange=false;
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

	public void setN3(float a) {
		internalChange = true;
		n3.setValue(a);
		internalChange = false;
	}

	public void setDistance(float a) {
		internalChange=true;
		distance.setValue(a);
		internalChange=false;
	}
	
	///////IMPLEMENT NUMBERBOX.LISTENER////////////
	public void numChanged(NumberBox src, Number newVal) {

		if(internalChange) return;		//The usual accelerating-widget fix

		float f=newVal.floatValue();

		source.statusBar.reset();

		if(src==ampField) {
			source.setAmplitude(f);
		} else if(src == wavelength){
			source.setWavelength(f);
		} else if(src == angle){
			source.setAngle((float)WTMath.toRads(f));
		} else if(src == n1){
			source.setN1(f);
		} else if(src == n2){
			source.setN2(f);
		} else if(src == n3){
			source.setN3(f);
		} else {	//Only other possibility is distance
			source.setDistance(f);
		}
		if(!engine.isPlaying()) engine.update();
	}

	public void invalidEntry(NumberBox src, Number badVal) {
		if(src==ampField)
			source.statusBar.setWarningText("Amplitude must be between 0 and 10.");
		else if(src==wavelength)
			source.statusBar.setWarningText("The wavelength must be between 2 and 40.");
		else if(src==angle)
			source.statusBar.setWarningText("The incident angle must be at least zero and less than 90 degrees.");
		else if(src==n1)
			source.statusBar.setWarningText("The initial index of refraction must be at least 1 (and finite).");
		else if(src == n2)
			source.statusBar.setWarningText("The middle index of refraction must be at least 1 (and finite).");
		else if(src == n3)
			source.statusBar.setWarningText("The final index of refraction must be at least 1 (and finite).");

		else	//distance
			source.statusBar.setWarningText("The distance between the interfaces must be non-negative.");
	}
	
	public void boundsForcedChange(NumberBox src, Number oldVal) {}
	
	//WSL Method used in ThreeMedia.java
	protected void toWSLNode(WSLNode node){
		ampFieldScripter.addTo(node);
		wavelengthScripter.addTo(node);
		angleScripter.addTo(node);
		n1Scripter.addTo(node);
		n2Scripter.addTo(node);
		n3Scripter.addTo(node);
		distanceScripter.addTo(node);
	}
}
