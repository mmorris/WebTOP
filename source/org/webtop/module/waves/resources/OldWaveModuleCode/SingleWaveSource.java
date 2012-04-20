//Updated May 13 2004

package webtop.wave;

import vrml.external.field.*;

import webtop.component.*;
import webtop.util.*;
import webtop.wsl.client.*;
import webtop.wsl.script.*;
import webtop.wsl.event.*;

// This is a base class for wave sources that do not persist.
// TBD... Make it go away after the wave leaves the pool
public abstract class SingleWaveSource extends PoolWidget {
	protected static final float WAVE_SPEED = 1;
	protected float amplitude;
	protected float width;
	protected float x;
	protected float y;

	protected float T;

	protected EventInSFFloat set_amplitude;
	protected EventInSFFloat set_width;
	protected EventInSFFloat set_x;
	protected EventInSFFloat set_y;

	public static final float MAX_AMPLITUDE = 50.0f;
	public static final float MAX_WIDTH = 50.0f;
	public static final float MAX_X = 50.0f;
	public static final float MAX_Y = 50.0f;

	public static final int TYPE_NONE = 0;
	public static final int TYPE_STRUCK = 1;
	public static final int TYPE_PLUCKED = 2;

	public SingleWaveSource(Engine e, WidgetsPanel panel, float A, float W, float x, float y) {
		super(e,panel,x,y);
		amplitude = A;
		width = W;
		T=0;
	}

	public void setT(float tee) {T = tee;}

	protected void create(String vrml) {
		super.create(vrml);

		set_amplitude = (EventInSFFloat) eai.getEI(getNode(),"set_amplitude");
		set_width = (EventInSFFloat) eai.getEI(getNode(),"set_wavelength");

		eai.getEO(getNode(),"wavelength_changed",this, "wavelength_changed");
		eai.getEO(getNode(),"phase_changed",this, "phase_changed");
		eai.getEO(getNode(),"mouseOverAmplitude",this, "mouseOverAmplitude");
		eai.getEO(getNode(),"mouseOverWavelength",this, "mouseOverWavelength");
		eai.getEO(getNode(),"mouseOverPhase",this, "mouseOverPhase");
		eai.getEO(getNode(),"amplitude_changed",this, "amplitude_changed");
	}

	public float getAmplitude() {return amplitude;}
	public void setAmplitude(float A, boolean setVRML) {
		amplitude = A;
		if(setVRML) set_amplitude.setValue(amplitude);
	}

	public float getWidth() {return width;}
	public void setWidth(float W, boolean setVRML) {
		width = W;
		if(setVRML) set_width.setValue(width);
	}

	public abstract float getValue(float x, float y, float t);
	protected boolean passive() {return false;}	//certainly not!

	//This should be called by subclasses from their callback() if they do not handle event
	public void callback(EventOut e,double when,Object data) {

		String arg = (String) data;
		WSLPlayer wslPlayer = engine.getWSLPlayer();

		System.out.println(arg);

		if(arg.equals("amplitude_changed")) {
			setAmplitude(((EventOutSFFloat) e).getValue(), false);
			widgetsPanel.getSwitcher().getActiveSSourcePanel().setAmplitude(amplitude);
			if(wslPlayer!=null)
				wslPlayer.recordMouseDragged(getID(), "amplitude", String.valueOf(amplitude));
			engine.update();

		} else if(arg.equals("wavelength_changed")) {
				setWidth(((EventOutSFFloat) e).getValue(),false);
			widgetsPanel.getSwitcher().getActiveSSourcePanel().setWidth(width);
			if(wslPlayer!=null)
				wslPlayer.recordMouseDragged(getID(), "width", String.valueOf(width));
			engine.update();
		} else super.callback(e,when,data);
	}

	public WSLNode toWSLNode() {
		WSLNode node;

		if(this instanceof PluckedSource) node = new WSLNode("pluckedsource");
		else if (this instanceof StruckSource) node = new WSLNode("strucksource");

		else return null;

		final WSLAttributeList atts=node.getAttributes();

		atts.add("id", id);
		atts.add("amplitude", String.valueOf(amplitude));
		atts.add("width", String.valueOf(width));
		if(widgetVisible) atts.add("selected", "true");
		atts.add("position", X + "," + Y);
		return node;
	}
}
