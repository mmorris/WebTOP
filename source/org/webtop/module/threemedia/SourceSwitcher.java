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

import javax.swing.JPanel;
import java.awt.*;

import javax.swing.*;
import org.webtop.util.*;

import org.webtop.wsl.client.*;
import org.webtop.wsl.script.*;

public class SourceSwitcher extends JPanel {
	public static final long serialVersionUID = 0; //to shut eclipse up
	//Class variables
	public static final int NONE = 0;
	public static final int LINEAR = 1;
	
	private LinearPanel linear;
	private GridBagLayout layout;

	private int showing;

	
	public SourceSwitcher(ThreeMedia wave){
		setLayout(layout = new GridBagLayout());
		linear = new LinearPanel(wave);
		add(linear, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
	}
	
	public void setEngine(Engine e) {linear.setEngine(e);}
	
	public void show(int which) {
		switch (which) {
		case NONE:
			showing = NONE;
			break;
		case LINEAR:
			showing = LINEAR;
			break;
		}
	}
	
	public void show(WaveSource s) {
		if(s == null) {
			show(NONE);
		} else if(s instanceof LinearSource) {
			show(LINEAR);
			linear.showSource((LinearSource) s);
		}
	}
	
	public int getShowing() {return showing;}

	public void setAmplitude(float amplitude) {linear.setAmplitude(amplitude);}
	
	public void setWavelength(float wavelength)
	{linear.setWavelength(wavelength);}

	public void setAngle(float angle) {linear.setAngle(angle);}

	public void setN1(float a) {linear.setN1(a);}
	public void setN2(float a) {linear.setN2(a);}
	public void setN3(float a) {linear.setN3(a);}

	public void setDistance(float a) {linear.setDistance(a);}
	
	//WSL Method used in ThreeMedia.java
	protected void toWSLNode(WSLNode node){
		linear.toWSLNode(node);
	}

}
