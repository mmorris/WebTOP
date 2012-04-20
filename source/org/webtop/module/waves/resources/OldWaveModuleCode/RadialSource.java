package webtop.wave;

import vrml.external.field.*;

import webtop.component.*;
import webtop.util.WTMath;
import webtop.wsl.client.*;
import webtop.wsl.event.*;

public class RadialSource extends WaveSource {
	private float beta;

	/*public RadialSource(Engine e, WidgetsPanel widgetPanel, StatusBar bar) {
		super(e, widgetPanel, bar);
		X = Y = 0.0f;
		beta = 0;
		createVRMLNode();
	}*/

	public RadialSource(Engine e, WidgetsPanel widgetPanel, float A, float L, float E, float x, float y) {
		super(e, widgetPanel, A, L, E, x, y);
		beta = k * 0.01f * wavelength;
		createVRMLNode();
	}

	public float getValue(float x, float y, float t) {
		if(k*R(x, y) <= 6.0f) {
			return 4.0f * (float) (U1(x, y) * Math.cos(k*WAVE_SPEED*t - phase) + U2(x,y) * Math.sin(k*WAVE_SPEED*t - phase));
		} else {
			return 4.0f * (float) ( (amplitude/(2*beta)) * (WTMath.j1(beta) * Math.sqrt(2/(Math.PI*k*R(x, y)))
				* Math.cos(k * (R(x, y) - WAVE_SPEED*t) + phase + (Math.PI/4))));
		}
	}

	public void setWavelength(float L, boolean setVRML) {
		super.setWavelength(L, setVRML);
		beta = k * 0.01f * wavelength;
	}

	private float R(float x, float y) {
		return (float) Math.sqrt( (X-x)*(X-x) + (Y-y)*(Y-y) );
	}

	private float U1(float x, float y) {
		if(R(x, y)<=0.01) {
			return (float) (amplitude * (-1.0/(beta*beta)) * (1.0/Math.PI + 0.5*beta * WTMath.j0(k*R(x, y))
				* WTMath.y1(beta)));
		} else {
			return (float) (amplitude*(-1/(2*beta))*WTMath.j1(beta)*WTMath.y0(k*R(x, y)));
		}
	}

	private float U2(float x, float y) {
		return (float) (amplitude / (2*beta) * WTMath.j1(beta)*WTMath.j0(k*R(x, y)));
	}

	protected void createVRMLNode() {
		String vrml = "RadialWidget { amplitude " + amplitude + "\n wavelength " + wavelength +
									"\n phase "+phase +"\n x " + X + "\n y " + Y + "}\n";
		create(vrml);

		set_position = (EventInSFVec3f) engine.getEAI().getEI(getNode(),"set_position");

		engine.getEAI().getEO(getNode(),"position_changed",this, "position_changed");
		engine.getEAI().getEO(getNode(),"mouseOverPosition",this, "mouseOverPosition");
	}

	protected String getNodeName() {return "<RadialWidget>";}

	public String toString() {
		return "RadialSource(Amplitude=" + amplitude + ", Wavelength=" + wavelength
			+ ", Phase=" + phase + ", X=" + X + ", Y=" + Y + ")";
	}
}
