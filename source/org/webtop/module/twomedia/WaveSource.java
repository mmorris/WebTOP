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


import org.web3d.x3d.sai.*;

import org.webtop.component.*;
import org.webtop.x3d.*;


public abstract class WaveSource extends X3DObject {
	static protected float speed = 1;
	protected float amplitude;
	protected float wavelength;
	protected float phase;
	protected float X;
	protected float Y;
	protected boolean widgetVisible=false,dragging=false;

	protected final Engine engine;
	protected final SourcePanel sourcePanel;
	protected final StatusBar statusBar;

	protected SFBool	 set_enabled;
	protected SFFloat set_amplitude;
	protected SFFloat set_wavelength;
	protected SFFloat set_phase;
	protected SFVec3f set_position;
	protected SFBool	 set_widgetVisible;

	public static final float MAX_AMPLITUDE = 10;
	public static final float MAX_WAVELENGTH = 50;
	public static final float MAX_PHASE=(float)(4*Math.PI);

	public static final int TYPE_NONE=0,TYPE_LINEAR=1,TYPE_RADIAL=2;

	protected String id;

	public WaveSource(Engine e, SourcePanel panel, StatusBar bar) {
		super(e.getEAI(),e.getEAI().getNode("Widget-GROUP"));
		amplitude = wavelength = phase = 0;
		engine = e;
		sourcePanel = panel;
		statusBar = bar;
	}

	public WaveSource(Engine e, SourcePanel panel, StatusBar bar, float A, float L, float E, float x, float y) {
		this(e,panel,bar);
		amplitude = A;
		wavelength = L;
		phase = E;
		X = x;
		Y = y;
	}

	public String getID() {return id;}
	public void setID(String ID) {id = ID;}

	public float getAmplitude() {return amplitude;}

	public float getWavelength() {return wavelength;}

	public float getPhase() {return phase;}

	public float getX() {return X;}
	public float getY() {return Y;}

	public void setAmplitude(float A, boolean setVRML) {
		amplitude = A;
		if(amplitude<0) amplitude=0;
		else if(amplitude>MAX_AMPLITUDE) amplitude = MAX_AMPLITUDE;
		if(setVRML) set_amplitude.setValue(amplitude);
	}

	public void setWavelength(float L, boolean setVRML) {
		wavelength = L;
		if(wavelength<0) wavelength = 0;
		else if(wavelength>MAX_WAVELENGTH) wavelength = MAX_WAVELENGTH;
		if(setVRML) set_wavelength.setValue(wavelength);
	}

	public void setPhase(float E, boolean setVRML) {
		phase = E;
		if(phase<0) phase = 0;
		else if(phase>MAX_PHASE) phase = MAX_PHASE;
		if(setVRML) set_phase.setValue(phase);
	}

	public void setXY(float xy[], boolean setVRML) {
		float xyz[] = new float[3];
		if(xy[0]<-50) xy[0] = -50;
		else if(xy[0]>50) xy[0] = 50;
		if(xy[1]<-50) xy[1] = -50;
		else if(xy[1]>50) xy[1] = 50;
		X = xyz[0] = xy[0];
		Y = xyz[1] = xy[1];
		xyz[2] = 0;
		if(setVRML) set_position.setValue(xyz);
	}

	public void setXY(float x, float y, boolean setVRML) {
		float xyz[] = new float[3];
		if(x<-50) x = -50;
		else if(x>50) x = 50;
		if(y<-50) y = -50;
		else if(y>50) y = 50;
		X = xyz[0] = x;
		Y = xyz[1] = y;
		xyz[2] = 0;
		if(setVRML) set_position.setValue(xyz);
	}

	public void setX(float x, boolean setVRML) {
		float xyz[] = new float[3];
		if(x<-50) x = -50;
		else if(x>50) x = 50;
		X = xyz[0] = x;
		xyz[1] = Y;
		xyz[2] = 0;
		if(setVRML) set_position.setValue(xyz);
	}

	public void setY(float y, boolean setVRML) {
		float xyz[] = new float[3];
		if(y<-50) y = -50;
		else if(y>50) y = 50;
		xyz[0] = X;
		Y = xyz[1] = y;
		xyz[2] = 0;
		if(setVRML) set_position.setValue(xyz);
	}

	public void setEnabled(boolean enabled) {set_enabled.setValue(enabled);}

	public void showWidgets() {
		set_widgetVisible.setValue(true);
		widgetVisible = true;
	}

	public void hideWidgets() {
		set_widgetVisible.setValue(false);
		widgetVisible = false;
	}

	public boolean getWidgetVisible() {return widgetVisible;}

	public abstract float getValue(float x, float y, float t);

	public boolean equals(Object obj) {
		return (obj instanceof LinearSource && this instanceof LinearSource) &&
			(obj == this || ((WaveSource)obj).id!=null && ((WaveSource)obj).id.equals(this.id));
	}

}
