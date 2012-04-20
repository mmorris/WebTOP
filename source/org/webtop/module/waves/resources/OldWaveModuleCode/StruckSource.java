package webtop.wave;

import vrml.external.field.*;

import webtop.component.*;
import webtop.util.WTMath;
import webtop.wsl.client.*;
import webtop.wsl.event.*;

import sdl.math.*;

public class StruckSource extends SingleWaveSource {
	Complex wivt,tsi;

	public StruckSource(Engine e, WidgetsPanel widgetPanel, float A, float W, float x, float y) { 
		super(e, widgetPanel, A, W, x, y);
		createVRMLNode();
		System.out.println(getValue(0,0,0));
	}

	/*
	public float getValue(float x, float y, float t) {

		t-=T;		
		return (float) (-10)*width*width* (float)(Complex.pow(Complex.pow(Complex.add(Complex.pow(Complex.add(Complex.multiply(Complex.i(),-t),width),2),(float)Math.pow(x-getX(),2)+(float)Math.pow(y-getY(),2)),.5),-1)).I;
	}
*/

	// TBD: Determine if this should return 0 for x=y=t=0, which it
	// currently does.
	public float getValue(float x, float y, float t) {
		t-=T;
		double one = Math.pow(x-getX(),2)+Math.pow(y-getY(),2)+width*width-t*t;
		return (float)10*width*width*(float)Math.sin(Math.atan2(-2*width*t,one)/2)*((float)Math.pow(one*one+4*width*width*t*t,-.25));
	}

	
	protected void createVRMLNode() {
		String vrml = "RadialWidget2 { amplitude " + amplitude + "\n wavelength " + 5 +
									"\n phase "+10 +"\n x " + X + "\n y " + Y + "}\n";
		create(vrml);

		set_position = (EventInSFVec3f) engine.getEAI().getEI(getNode(),"set_position");

		engine.getEAI().getEO(getNode(),"position_changed",this, "position_changed");
		engine.getEAI().getEO(getNode(),"mouseOverPosition",this, "mouseOverPosition");
	}

	protected String getNodeName() {return "<RadialWidget>";}

	public String toString() {
		return "StruckSource(Amplitude=" + amplitude + ", Width=" + width
			+", X=" + X + ", Y=" + Y + ")";
	}
}
