package webtop.wave;

import vrml.external.field.*;

import webtop.component.*;
import webtop.util.WTMath;
import webtop.wsl.client.*;
import webtop.wsl.event.*;

import sdl.math.*;

public class PluckedSource extends SingleWaveSource {
	Complex wivt;

	public PluckedSource(Engine e, WidgetsPanel widgetPanel, float A, float W, float x, float y) { 
		super(e, widgetPanel, A, W, x, y);
		wivt = new Complex();
		createVRMLNode();

		System.out.println(getValue(0,0,0));
	}

	/*
	public float getValue(float x, float y, float t) {
		t-=T;
		wivt = Complex.add(Complex.multiply(Complex.i(),t), width);

		return (float) Complex.divide(wivt,Complex.pow(Complex.add(Complex.pow(wivt,2),
															Math.pow(x-getX(),2)+Math.pow(y-getY(),2)),
															pwr)).R * amplitude * (float)Math.pow(width,2);
	} 
	*/
	
	public float getValue(float x, float y, float t) {
		t-=T;
		//double alphamod = Math.pow(width*width+t*t,.5);
		//double chi = Math.atan2(t,width);
		double one = Math.pow(x-getX(),2)+Math.pow(y-getY(),2)+width*width-t*t;
		//double tsi = Math.pow(one*one+4*width*width*t*t,.5);
		//double phi = Math.atan2(2*width*t,one);

		return (float)amplitude*width*width*(float)Math.sqrt(width*width+t*t)*(float)Math.pow(one*one+4*width*width*t*t,-.75f)*(float)Math.cos(Math.atan2(t,width)-(1.5f)*Math.atan2(2*width*t,one));
	}

	protected void createVRMLNode() {
		String vrml = "RadialWidget2 { amplitude " + amplitude + "\n wavelength " + 8 +
									"\n phase "+10 +"\n x " + X + "\n y " + Y + "}\n";
		create(vrml);

		set_position = (EventInSFVec3f) engine.getEAI().getEI(getNode(),"set_position");

		engine.getEAI().getEO(getNode(),"position_changed",this, "position_changed");
		engine.getEAI().getEO(getNode(),"mouseOverPosition",this, "mouseOverPosition");
	}

	protected String getNodeName() {return "<RadialWidget>";}

	public String toString() {
		return "PluckedSource(Amplitude=" + amplitude + ", Width=" + width
			+", X=" + X + ", Y=" + Y + ")";
	}
}
