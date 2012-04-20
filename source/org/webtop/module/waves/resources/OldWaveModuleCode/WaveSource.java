//Updated May 13 2004

package webtop.wave;

import vrml.external.field.*;

import webtop.component.*;
import webtop.util.*;
import webtop.wsl.client.*;
import webtop.wsl.script.*;
import webtop.wsl.event.*;

public abstract class WaveSource extends PoolWidget {
	protected static final float WAVE_SPEED = 1;
	protected float amplitude;
	protected float wavelength;
	protected float phase;
	protected float k;

	protected EventInSFFloat set_amplitude;
	protected EventInSFFloat set_wavelength;
	protected EventInSFFloat set_phase;

	public static final float MAX_AMPLITUDE = 10.0f;
	public static final float MAX_WAVELENGTH = 50.0f;
	//For reasons of simplicity and widget sensitivity, we only use half of the
	//possible range of phases:
	public static final float MAX_PHASE = (float) Math.PI;

	public static final int TYPE_NONE = 0;
	public static final int TYPE_LINEAR = 1;
	public static final int TYPE_RADIAL = 2;

	/*public WaveSource(Engine e, WidgetsPanel panel, StatusBar bar) {
		super(e);
		amplitude = wavelength = phase = k = 0.0f;
		widgetPanel = panel;
		statusBar = bar;
	}*/

	public WaveSource(Engine e, WidgetsPanel panel, float A, float L, float E, float x, float y) {
		super(e,panel,x,y);
		amplitude = A;
		wavelength = L;
		phase = E;
		k = (float) (2 * Math.PI / wavelength);
	}

	protected void create(String vrml) {
		super.create(vrml);

		set_amplitude = (EventInSFFloat) eai.getEI(getNode(),"set_amplitude");
		set_wavelength = (EventInSFFloat) eai.getEI(getNode(),"set_wavelength");
		set_phase = (EventInSFFloat) eai.getEI(getNode(),"set_phase");


		eai.getEO(getNode(),"wavelength_changed",this, "wavelength_changed");
		eai.getEO(getNode(),"phase_changed",this, "phase_changed");
		eai.getEO(getNode(),"mouseOverAmplitude",this, "mouseOverAmplitude");
		eai.getEO(getNode(),"mouseOverWavelength",this, "mouseOverWavelength");
		eai.getEO(getNode(),"mouseOverPhase",this, "mouseOverPhase");
		eai.getEO(getNode(),"amplitude_changed",this, "amplitude_changed");
	}

	public float getAmplitude() {return amplitude;}
	public float getWavelength() {return wavelength;}
	public float getPhase() {return phase;}

	public void setAmplitude(float A, boolean setVRML) {
		amplitude = A;
		if(setVRML) set_amplitude.setValue(amplitude);
	}

	public void setWavelength(float L, boolean setVRML) {
		wavelength = L;
		k = (float) (2 * Math.PI / wavelength);
		if(setVRML) set_wavelength.setValue(wavelength);
	}

	public void setPhase(float E, boolean setVRML) {
		phase = E;
		if(setVRML) {
			if(this instanceof LinearSource)
				set_phase.setValue(phase*2);
			else
				set_phase.setValue(phase);
		}
	}

	public abstract float getValue(float x, float y, float t);
	protected boolean passive() {return false;}	//certainly not!

	//This should be called by subclasses from their callback() if they do not handle event
	public void callback(EventOut e,double when,Object data) {
		String arg = (String) data;
		WSLPlayer wslPlayer = engine.getWSLPlayer();

		if(arg.equals("amplitude_changed")) {
			//System.out.println(((EventOutSFFloat) e).getValue());
			setAmplitude(((EventOutSFFloat) e).getValue(), false);
			widgetsPanel.getSwitcher().getActiveSourcePanel().setAmplitude(amplitude);
			if(wslPlayer!=null)
				wslPlayer.recordMouseDragged(getID(), "amplitude", String.valueOf(amplitude));
			engine.update();
		} else if(arg.equals("phase_changed")) {
			if(this instanceof LinearSource)
				setPhase((((EventOutSFFloat) e).getValue())/2.0f, false);
			else
				setPhase(((EventOutSFFloat) e).getValue(), false);
			widgetsPanel.getSwitcher().getActiveSourcePanel().setPhase(phase);
			if(wslPlayer!=null)
				wslPlayer.recordMouseDragged(getID(), "phase", String.valueOf(WTMath.toDegs(phase)));
			engine.update();
		} else if(arg.equals("wavelength_changed")) {
			setWavelength(((EventOutSFFloat) e).getValue(), false);
			widgetsPanel.getSwitcher().getActiveSourcePanel().setWavelength(wavelength);
			if(wslPlayer!=null)
				wslPlayer.recordMouseDragged(getID(), "wavelength", String.valueOf(wavelength));
			engine.update();
		} else if(arg.equals("mouseOverAmplitude")) {
			if(((EventOutSFBool) e).getValue())
				engine.getStatusBar().setText("Use this widget to change the amplitude.  Up - increase.  Down - decrease.");
			else if(!dragging)
				engine.getStatusBar().reset();
		} else if(arg.equals("mouseOverWavelength")) {
			if(((EventOutSFBool) e).getValue())
				engine.getStatusBar().setText("Use this widget to change the wavelength.  Outward - increase.  Inward - decrease.");
			else if(!dragging)
				engine.getStatusBar().reset();
		} else if(arg.equals("mouseOverPhase")) {
			if(((EventOutSFBool) e).getValue())
				engine.getStatusBar().setText("Use this widget to change the phase.  Outward - increase.  Inward - decrease.");
			else if(!dragging)
				engine.getStatusBar().reset();
		} else super.callback(e,when,data);
	}

	public WSLNode toWSLNode() {
		WSLNode node;

		if(this instanceof LinearSource) node = new WSLNode("linesource");
		else if(this instanceof RadialSource) node = new WSLNode("pointsource");
		else return null;

		final WSLAttributeList atts=node.getAttributes();

		atts.add("id", id);
		atts.add("amplitude", String.valueOf(amplitude));
		atts.add("wavelength", String.valueOf(wavelength));
		atts.add("phase", String.valueOf(WTMath.toDegs(phase)));
		if(widgetVisible) atts.add("selected", "true");
		if(this instanceof RadialSource) atts.add("position", X + "," + Y);

		return node;
	}
}
