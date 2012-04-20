/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Engine.java
//Manages a LaserBeam and generally connects the Lasers Module together.
//Sara Smolensky
//Started August 24 2000
//Updated November 20 2002
//Converted to X3D June 7 2008
//Grant Patten
//Version 3.0

package org.webtop.module.laser;

import org.webtop.util.*;
import org.web3d.x3d.sai.*;
import org.webtop.x3d.SAI;
import org.webtop.x3d.output.*;
import org.webtop.x3d.widget.ScalarWidget;
import org.webtop.x3d.widget.WheelWidget;
import org.webtop.x3d.widget.XDragWidget;
import org.webtop.component.*;
import org.webtop.wsl.client.*;
import org.webtop.wsl.script.*;
import org.webtop.wsl.event.*;
import org.webtop.x3d.widget.*;

public class Engine implements ScalarWidget.Listener/*implements WSLScriptListener, WSLPlayerListener*/ {
	public static final float MIN_SPACE=Laser.MIN_SPACE;		//minimum space between end of laser and screen (200)

	public interface Widget {
		public String getTip();
	}

	private Laser parent;
	private Mirror mirror1;
	//private Mirror2 mirror2;
	private LaserBeam laserbeam;

	private boolean widgetsOn=true;
	private SFBool[] flipMirror=new SFBool[2];

	private float	length=0,
					z=0,			// distance from beam waist to the observation point
					w1=0,			//spot size on mirror 1
					w2=0,			//on mirror 2
					lambda,			//light's wavelength
					L1,				//distance from mirror 1 to waist
					L2,				//from 2 to waist
					L3,				//from 2 to screen
					screenDist,		//from 1 to screen
					g1,				//g-parameters
					g2,
					w0,				//spot size at beam waist
					zR,				//Rayleigh beam length
					R1,R2,			//radii of curvature of mirrors
					yp1,xp1,xp2,yp2,
					radius_mirror1,	//radii of mirrors
					radius_mirror2,
					mirror1_angle,
					mirror2_angle;

	// TEMxx

	
	private StatusBar statusBar;

	private boolean draggingWidget;

	private String modStat,statStr="";		//String description of lasing-ness or not

	public Engine(Laser laser) {
		//First, setup the objects that depend only on us
		parent=laser;
		mirror1=new Mirror(parent.getSAI());
		//mirror2=new Mirror2(parent.getEAI());
		mirror1.drawMirror(radius_mirror1=8,mirror1_angle=0.2f);
		//mirror2.drawMirror(radius_mirror2=8,mirror2_angle=0.2f);
		flipMirror[0]=(SFBool) parent.getSAI().getField("Mirror1Flip","flip");
		flipMirror[1]=(SFBool) parent.getSAI().getField("Mirror2Flip","flip");
		laserbeam=new LaserBeam(this,parent.getSAI());

		//The calculations for both xp1 and xp2 (currently in setLength()) will
		//need to be moved if the mirrors ever are allowed to change shape.
		xp1=(float) (Math.cos(mirror1_angle) * radius_mirror1);

		//We need the control panel reference to create the widgets
		//controlPanel=parent.getControlPanel();
		statusBar=parent.getStatusBar();


		//The control panel will respond by setting our input values.
		parent.setEngine(this);

		//Now get our own EAI references

		//parent.getWSLPlayer().addListener(this);

		render(true);
	}

	public void render(boolean drawbeam) {
		//This may not be where this should go, but it works [Davis]
		flipMirror[0].setValue(R1<0);
		flipMirror[1].setValue(R2<0);

		// Mirror g-parameters
		g1=1 - (length / R1);
		g2=1 - (length / R2);
		DebugPrinter.println("Engine::render(): g1="+g1+", g2="+g2);

		//Spot sizes on mirrors
		w1=(float)(Math.sqrt((lambda/1e6*length)/Math.PI)*Math.pow(g2/(g1*(1-g1*g2)),0.25));
		w2=(float)(Math.sqrt((lambda/1e6*length)/Math.PI)*Math.pow(g1/(g2*(1-g1*g2)),0.25));
		DebugPrinter.println("Engine::render(): w1="+w1+", w2="+w2);

		boolean stable=true;
		if((g1*g2 < 0) || (g1*g2 > 1)) {
			newStatus("Unstable resonator");
			stable=false;
		} else if(g1==0 && g2==0) {		//Exceptional case
			L1=length / 2;
			L2=length / 2;
			z=screenDist - (length / 2);
			L3=screenDist - length;
			w0=(float) (Math.pow(length*lambda/(1e6*2*Math.PI),0.5));
		} else {
			L1=(length*g2*(1-g1))/(g1+g2-(2*g1*g2));
			z=screenDist - L1;
			L2=length - L1;
			L3=z + L1 - length;
			// Spot size at beam waist
			w0=(float) (Math.sqrt((lambda/1e6*length)/Math.PI)*Math.pow((g1*g2*(1-g1*g2))/(Math.pow((g1+g2-2*g1*g2),2)),0.25));
		}

		if(w1>Mirror.MIRROR_RADIUS || w2>Mirror.MIRROR_RADIUS) {
			newStatus("Beam radius exceeds mirror radius");
			stable=false;
		}

		if(stable) {
			// Rayleigh length of the beam
			zR=(float) ((Math.PI*w0*w0)/(lambda/1e6));
			newStatus("");
			calculate(drawbeam);
			parent.updateLabels();
		} else {
			//status set appropriately above
			laserbeam.setLasing(false);
			parent.clearLabels();
		}
	}

	private void calculate(boolean drawbeam) {
		laserbeam.setLasing(true);
		laserbeam.calculate();
		if(drawbeam) laserbeam.drawBeam();
		//something about far beam would go here if needed
		laserbeam.drawSpot();
		laserbeam.calculateLine();
	}

	
	
	//Listens For Changes in the Widget
	public void valueChanged(ScalarWidget src, float value) {
	setDragging(src,src.isActive());
	}
	
	//Sets low resolution mode if a widget is being dragged
	public void setDragging(ScalarWidget source, boolean drag) {
		if(draggingWidget==drag) return;
		draggingWidget=drag;
		laserbeam.setLowResolution(drag);
		render(!(source == parent.screenDistWidget));		//Don't recalc beam for dist drag
	}
	public boolean isDragging() {return draggingWidget;}
	public void setMouseOver(Widget source,boolean over) {
		setModuleStatus(over?source.getTip():null);
	}

	private void newStatus(String stat) {
		statStr=stat;
		updateStatii();
	}

	//This sets the status bar's principal text but keeps the engine's status text around
	public void setModuleStatus(String stat) {
		modStat=stat;
		updateStatii();
	}

	private void updateStatii() {
		if(modStat==null||"".equals(modStat)) statusBar.setText(statStr);
		else if("".equals(statStr)) statusBar.setText(modStat);
		else statusBar.setText(modStat+" ("+statStr+")");
	}


	public void printAllData() {
		DebugPrinter.println("Engine::printAllData()...\n  wavelength="+lambda+"\tlength="+length+
												 "\n  R1="+R1+"\tR2="+R2+"\tscreenDist="+screenDist+"\n---------"+
												 "\n  z="+z+"\nw0="+w0+"\tzR="+zR+
												 "\n  L1="+L1+"\tL2="+L2+"\tL3="+L3+
												 "\n  xp2="+xp2);
	}

	public float getZ() {return z;}
	public float getW0() {return w0;}
	public float getzR() {return zR;}
	public float getL1() {return L1;}
	public float getL2() {return L2;}
	public float getL3() {return L3;}

	public void setScreenDist(float loc) {
		screenDist=loc;
		z=screenDist - L1;
		parent.cavityLenField.setMax(loc-MIN_SPACE);
		parent.cavityLenWidget.setMax((loc-MIN_SPACE)/100.0f);	
		if(!draggingWidget) parent.screenDistWidget.setValue(loc/100.0f);
	}
	public float getScreenDist() {return screenDist;}

	public void setLength(float loc) {
		length=loc;
		//A length change requires recalculating Mirror2's position-ish thing:
		xp2=(float) (length - Math.cos(mirror2_angle)*radius_mirror2);
		laserbeam.setXPs(xp1,xp2);
		parent.screenDistField.setMin(loc+MIN_SPACE);
		parent.screenDistWidget.setMin((loc+MIN_SPACE)/100.0f);	
		
		if(!draggingWidget) parent.cavityLenWidget.setValue(loc/100.0f);
	}
	public float getLength() {return length;}

	public void setLambda(float l) {
		lambda=l;
		if(!draggingWidget) parent.wavelengthWidget.setValue(l);
	}
	public float getLambda() {return lambda;}

	public void setR1(float r1) {R1=r1;}
	public float getR1() {return R1;}

	public void setR2(float r2) {R2=r2;}
	public float getR2() {return R2;}

	public Laser getParent() {return parent;}

	public LaserBeam getBeam() {return laserbeam;}

	// -------------------------------------------------------------------------------
	// WSL callbacks
	// -------------------------------------------------------------------------------
	/*
	public WSLNode toWSLNode() {
		WSLNode laser=new WSLNode("laser");
		final WSLAttributeList atts=laser.getAttributes();

		atts.add("wavelength", String.valueOf(lambda));
		atts.add("radius1", String.valueOf(R1));
		atts.add("radius2", String.valueOf(R2));
		atts.add("length", String.valueOf(length));
		atts.add("screen", String.valueOf(screenDist));
		atts.add("aperture", String.valueOf(controlPanel.getAperture()));
		atts.add("intensity", String.valueOf(controlPanel.getIntensity()));
		atts.add("widgetsOn", String.valueOf(widgetsOn));

		return laser;
	}

	private void setParameter(String target, String param, String value) {
		float v=Float.NaN;
		try{v=new Float(value).floatValue();}catch(NumberFormatException e){}
		if("wavelength".equals(param))
			controlPanel.setWavelength(v);
		else if("radius1".equals(param))
			controlPanel.setR1(v);
		else if("radius2".equals(param))
			controlPanel.setR2(v);
		else if("length".equals(param))
			controlPanel.setLength(v);
		else if("screenDist".equals(param))
			controlPanel.setScreen(v);
		else if("reset".equals(param))
			controlPanel.setDefaults();
		else if("toggleWidgets".equals(param))
			setWidgetsVisible(!widgetsOn);
		else if("aperture".equals(param))
			controlPanel.setAperture(Integer.parseInt(value));
		else if("intensity".equals(param))
			controlPanel.setIntensity(Integer.parseInt(value));
		else if("resetIntensity".equals(param))
			controlPanel.resetIntensity();
	}

	public void scriptActionFired(WSLScriptEvent event) {
		int id=event.getID();
		String target=event.getTarget();
		String param=event.getParameter();
		String value=event.getValue();

		if(id==event.ACTION_PERFORMED || id==event.MOUSE_DRAGGED) {
			setParameter(target, param, value);
		}
	}

	public void initialize(WSLScriptEvent event) {
		WSLAttributeList atts=event.getNode().getAttributes();
		controlPanel.setWavelength(atts.getFloatValue("wavelength", WavelengthWidget.DEFAULT));
		controlPanel.setR1(atts.getFloatValue("radius1", ControlPanel.DEF_ROC));
		controlPanel.setR2(atts.getFloatValue("radius2", ControlPanel.DEF_ROC));
		controlPanel.setScreen(ScreenDistanceWidget.MAX);	//To allow any length
		controlPanel.setLength(atts.getFloatValue("length", LengthWidget.DEFAULT));
		controlPanel.setScreen(atts.getFloatValue("screen", ScreenDistanceWidget.DEFAULT));
		controlPanel.setAperture(atts.getIntValue("aperture", 0));
		controlPanel.setIntensity(atts.getIntValue("intensity", ControlPanel.DEF_INTENSITY));
		setWidgetsVisible(atts.getBooleanValue("widgetsOn", true));
	}

	//Should this have anything in it? [Davis]
	public void playerStateChanged(WSLPlayerEvent event) {
		switch(event.getID()) {
		case WSLPlayerEvent.PLAYER_STARTED: break; //disable stuff
		case WSLPlayerEvent.PLAYER_STOPPED: break; //enable stuff
		}
	}
	*/
}
