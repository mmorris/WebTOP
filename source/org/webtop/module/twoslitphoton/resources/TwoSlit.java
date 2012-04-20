//TwoSlit.java
//WebTOP applet file for the TwoSlit module.
//Peter Gilbert
//Created June 15 2004
//Updated May 31 2005
//Version 0.1

package webtop.twoslit;

import java.awt.*;
import java.awt.event.*;

import org.webtop.x3d.output.LinePlot;

import vrml.external.field.*;

import sdl.gui.numberbox.*;

import webtop.component.*;
import webtop.util.*;
import webtop.vrml.*;
import webtop.vrml.widget.*;
import webtop.vrml.output.*;
import webtop.util.script.*;

import webtop.wsl.client.*;
import webtop.wsl.event.*;
import webtop.wsl.script.*;

import sdl.math.*;


public class TwoSlit extends WApplet
	implements NumberBox.Listener,StateButton.Listener,ActionListener,AdjustmentListener,WSLPlayerListener,WSLModule,WSLScriptListener,EventOutObserver
{
	
	private static class WidgetEvent extends webtop.vrml.WidgetEvent
	{
		//[These constants also index into wslStrings]
		public static final short
			WAVELENGTH = 1,
			WIDTH = 2,
			DISTANCE = 3,
			SCREEN = 4,
			SCREEN_MESH = 5;

		public WidgetEvent(short w,short a) {super(w,a);}
	}
		
	protected String getModuleName() {return "TwoSlit";}
	protected int getMajorVersion() {return 0;}
	protected int getMinorVersion() {return 1;}
	protected int getRevision() {return 0;}
	protected String getDate() {return "Jun 15 2004";}
	protected String getAuthor() {return "Peter Gilbert";}
	
	//WSL/animation classes
	private Animation animation;
	private Engine engine;
	private WSLPlayer wslPlayer;
	private Data data=new Data();
	
	//Constants
	private static final float	WAVELENGTH_DEF=550;
	private static final float	WAVELENGTH_MIN=400;
	private static final float	WAVELENGTH_MAX=800;
	private static final int	RATE_DEF=40;
	private static final int	RATE_MIN=1;
	private static final int	RATE_MAX=5000;
	private static final float	WIDTH_DEF=0.04f;
	private static final float	WIDTH_MIN=0f;
	private static final float	WIDTH_MAX=1f;
	private static final float	DISTANCE_DEF=0.25f;
	private static final float	DISTANCE_MIN=0f;
	private static final float	DISTANCE_MAX=1f;
	private static final float	EXPOSURE_DEF=20;
	private static final float	EXPOSURE_MIN=1;
	private static final float	EXPOSURE_MAX=30;
	//private static final int ANIMATION_PERIOD=50;
	private static final int ANIMATION_PERIOD=100;
	private static final int PLOT_RES = 1500;
	private static final float PLOT_SCALE = 250;
	private static final int DEF_BRIGHTNESS = 2;
	private static final int MAX_BRIGHTNESS = 10;
	private static final int STOPPED = 0;
	private static final int PLAYING = 1;
	private static final int PAUSED = 2;
	private static final int UPDATE=0;
	private static final int NO_UPDATE=1;
	
	private int animationState = STOPPED;
	
	//Slit stuff
	private BoxMaker slitEngine;					//slit geometry creator
	private NSlitDragger nslitDraggerEngine;	//slit widget coordinator
	private EventInSFFloat	nsd_set_width;
	private EventInSFFloat	nsd_set_distance;
	
	//GUI elements
	private FloatBox wavelengthField;
	private IntBox rateField;
	private FloatBox widthField;
	private FloatBox distanceField;
	private FloatBox exposureTimeField;
	private Button resetButton=new Button("Reset");
	private Button clearButton=new Button("Clear Screen");
	private Button animationButton=new Button("  Play  ");
	private StateButton audioButton=new StateButton("Audio ", new String[] {"",""},new String[]{"On","Off"});
	private Label photonCount=new Label("    0");
	private Label elapsedTime=new Label("   0.0");
	private Scrollbar brightnessScrollbar;
	
	//GUI Scripting Elements
	//Don't worry about setting these up yet. Do when module is working 
	private NumberBoxScripter wavelengthScripter;
	private NumberBoxScripter rateScripter;
	private NumberBoxScripter widthScripter;
	private NumberBoxScripter distanceScripter;
	private NumberBoxScripter exposureTimeScripter;
	private ButtonScripter resetScripter;
	private ButtonScripter clearScripter;
	private ButtonScripter animationScripter;
	private StateButtonScripter audioScripter;
	
	private ScalarScripter wavelengthWidgetScripter;
	
	//VRML
	private LinePlot plot;
	private EventInSFFloat transparency;
	private WheelWidget wavelengthWidget;
	
	//Animation data
	public static final class Data implements Animation.Data,Cloneable{
		public float wavelength;
		public float width;
		public float distance;
		
		public Animation.Data copy() {
			try {
				return (Data)clone();		//all data is primitive; clone() is fine
			} catch(CloneNotSupportedException e) {return null;}	//can't happen
		}
	}
	
	protected Component getFirstFocus() {return wavelengthField;}
	
	
	protected void setupGUI() {
		setLayout(new BorderLayout());
		//Don't have to do the following 2 lines
		/*
		add(getStatusBar(),BorderLayout.CENTER);
		add(getWSLPanel(),BorderLayout.SOUTH);*/
		
		Panel panel;
		panel = new Panel();
		
		panel.add(new Label("        Wavelength:",Label.RIGHT));
		panel.add(wavelengthField=new FloatBox(WAVELENGTH_MIN,WAVELENGTH_MAX,45,5));
		panel.add(new Label("nm"));
		
		
		panel.add(new Label("Width:",Label.RIGHT));
		panel.add(widthField=new FloatBox(WIDTH_MIN,WIDTH_MAX,45,5));
		panel.add(new Label("mm"));
		
		
		panel.add(new Label("Distance:",Label.RIGHT));
		panel.add(distanceField=new FloatBox(DISTANCE_MIN,DISTANCE_MAX,45,5));
		panel.add(new Label("mm        "));
		
		
		panel.add(new Label("Photons/sec:",Label.RIGHT));
		panel.add(rateField=new IntBox(RATE_MIN,RATE_MAX,45,5));
		
		
		panel.add(new Label("Exposure time:",Label.RIGHT));
		panel.add(exposureTimeField=new FloatBox(EXPOSURE_MIN,EXPOSURE_MAX,45,5));
		panel.add(new Label("sec"));
		
	
		panel.add(new Label("Photons:",Label.RIGHT) );
		photonCount.setAlignment(Label.RIGHT);
		panel.add(photonCount);
		
		panel.add(new Label("Elapsed time:",Label.RIGHT) );
		elapsedTime.setAlignment(Label.RIGHT);
		panel.add(elapsedTime);
		panel.add(new Label("sec"));
		
		panel.add(animationButton);
		panel.add(clearButton);
		panel.add(resetButton);
		panel.add(audioButton);
		
		panel.add(new Label("Brightness:",Label.RIGHT) );
		brightnessScrollbar=new Scrollbar(Scrollbar.HORIZONTAL,DEF_BRIGHTNESS,1,0,MAX_BRIGHTNESS);
		brightnessScrollbar.addAdjustmentListener(this);
		panel.add(brightnessScrollbar);
		add(panel, BorderLayout.CENTER);
		
		setDefaultValues();
		
		wavelengthField.addNumberListener(this);
		rateField.addNumberListener(this);
		widthField.addNumberListener(this);
		distanceField.addNumberListener(this);
		exposureTimeField.addNumberListener(this);
		animationButton.addActionListener(this);
		audioButton.addListener(this);
		resetButton.addActionListener(this);
		clearButton.addActionListener(this);
		
		//Don't worry about setting up the scripters just yet.  Do this later when 
		//we have the module working
		wavelengthScripter = new NumberBoxScripter(wavelengthField,getWSLPlayer(),null,"wavelength",new Float(WAVELENGTH_DEF));
		rateScripter = new NumberBoxScripter(rateField,getWSLPlayer(),null,"photonRate",new Float(RATE_DEF));
		widthScripter = new NumberBoxScripter(widthField,getWSLPlayer(),null,"slitWidth",new Float(WIDTH_DEF));
		distanceScripter = new NumberBoxScripter(distanceField,getWSLPlayer(),null,"slitDistance",new Float(DISTANCE_DEF));
		exposureTimeScripter = new NumberBoxScripter(exposureTimeField,getWSLPlayer(),null,"exposureTime",new Float(EXPOSURE_DEF));
		animationScripter = new ButtonScripter(animationButton,getWSLPlayer(),null,"animation",this);
		audioScripter = new StateButtonScripter(audioButton,getWSLPlayer(),null,"audio",new String[] {"On","Off"},1);
		resetScripter = new ButtonScripter(resetButton,getWSLPlayer(),null,"reset",this);
		clearScripter = new ButtonScripter(clearButton,getWSLPlayer(),null,"clearScreen",this);	
	}
	//will map to setupX3D() -- DON'T WORRY ABOUT THIS. I'LL DO IT.
	protected void setupVRML() {
		engine = new Engine(this,getEAI());
		slitEngine = new BoxMaker(getEAI());
		nslitDraggerEngine = new NSlitDragger(getEAI());
		animation = new Animation(engine,data,ANIMATION_PERIOD);
		plot=new LinePlot(new IndexedSet(getEAI(),getEAI().getNode("ilsNode")),1000,PLOT_RES);
		transparency=(EventInSFFloat)getEAI().getEI(getEAI().getNode("TRANS_WORKER"),"transparency_in");
		setBrightness(DEF_BRIGHTNESS);
		engine.setRate(RATE_DEF);
		
		wavelengthWidget=new WheelWidget(getEAI(),getEAI().getNode("WavelengthWidget"),(short)2,"Turn to change the wavelength.");
		wavelengthWidget.addListener(this);
		getManager().addHelper(new ScalarCoupler(wavelengthWidget,wavelengthField,1,new ScalarCoupler.Converter(Lambda.linear(0.04f,0f),Lambda.linear(1,0f))));
		wavelengthWidgetScripter = new ScalarScripter(wavelengthWidget,getWSLPlayer(),null,"wavelengthdrag",WAVELENGTH_DEF);
	}
	
	protected void setWidgetsEnabled(boolean yes) {}
	protected void setGUIEnabled(boolean yes) {}
	
	protected void setDefaults() {
		data.wavelength=WAVELENGTH_DEF;
		data.width=WIDTH_DEF;
		data.distance=DISTANCE_DEF;
		
		animation.setData(data);
		animation.setPaused(true);
		animation.setPlaying(true);
	}
	
	public void setDefaultValues() {
		wavelengthField.setValue(WAVELENGTH_DEF);
		widthField.setValue(WIDTH_DEF);
		distanceField.setValue(DISTANCE_DEF);
		rateField.setValue(RATE_DEF);
		exposureTimeField.setValue(EXPOSURE_DEF);
	}
	
	protected void startup() {
		
		// update the nslitDragger
		nslitDraggerEngine.n = 2;
		nslitDraggerEngine.evaluate(true, true);
		nslitDraggerEngine.setMode(NSlitDragger.NSLIT);
		nslitDraggerEngine.updateWidthDraggerConstraints();
		nslitDraggerEngine.updateDistanceDraggerConstraints();	
		
		// width
		nsd_set_width = (EventInSFFloat) getEAI().getEI("nslitDragger","width");
		getEAI().getEO("nslitDragger","width",this,new WidgetEvent(WidgetEvent.WIDTH,WidgetEvent.MOUSE_DRAG));

		getEAI().getEO("nslitDragger","wd_isOver_changed",this,new WidgetEvent(WidgetEvent.WIDTH,WidgetEvent.MOUSE_OVER));
		getEAI().getEO("nslitDragger","wd_isActive_changed",this,new WidgetEvent(WidgetEvent.WIDTH,WidgetEvent.MOUSE_CLICK));

		// distance
		nsd_set_distance = (EventInSFFloat) getEAI().getEI("nslitDragger","distance");
		getEAI().getEO("nslitDragger","distance",this,new WidgetEvent(WidgetEvent.DISTANCE,WidgetEvent.MOUSE_DRAG));

		getEAI().getEO("nslitDragger","dd_isOver_changed",this,new WidgetEvent(WidgetEvent.DISTANCE,WidgetEvent.MOUSE_OVER));
		getEAI().getEO("nslitDragger","dd_isActive_changed",this,new WidgetEvent(WidgetEvent.DISTANCE,WidgetEvent.MOUSE_CLICK));
		
		// is this it??
		slitEngine.evaluate();
		
		evaluatePlot();
	}
	//IGNORE THIS METHOD
	public void callback(EventOut who, double when, Object which) {
		//////////////////////////////////////////////////////////////////////////
		// ignore events due to updates in the text fields
		//if(setFromTextField)
		//	return;

		//////////////////////////////////////////////////////////////////////////
		// extract the type of event
		WidgetEvent wevt = (WidgetEvent) which;

		switch(wevt.getAction()) {
		case WidgetEvent.MOUSE_OVER:
		/*	if(movingDraggers) break;
			if(((EventOutSFBool)who).getValue())
				switch(wevt.getWidget()) {
				case WidgetEvent.SCREEN: statusBar.setText(HELP_SCREEN_DRAGGER); break;
				case WidgetEvent.WIDTH: statusBar.setText(HELP_WIDTH_DRAGGER); break;
				case WidgetEvent.DISTANCE: statusBar.setText(HELP_DISTANCE_DRAGGER); break;
				case WidgetEvent.WAVELENGTH: statusBar.setText(HELP_WAVELENGTH_DRAGGER); break;
				} else statusBar.reset();
			*/
			break;
		case WidgetEvent.MOUSE_CLICK:
			/*
			if(engine.interacting=movingDraggers=((EventOutSFBool)who).getValue())
				wslPlayer.recordMousePressed(wslStrings[wevt.getWidget()]);
			else {
				wslPlayer.recordMouseReleased(wslStrings[wevt.getWidget()]);
				statusBar.setText(CALCULATING_MESSAGE);
				engine.evaluate();
				statusBar.reset();
			}
			*/
			break;
		case WidgetEvent.MOUSE_DRAG:
			float fVal=((EventOutSFFloat)who).getValue();
			switch(wevt.getWidget()) {
			case WidgetEvent.SCREEN:
				//engine.zCurrent=fVal;
				//infoPanel.setZ(fVal);
				break;
			case WidgetEvent.WAVELENGTH:
				//engine.wavelength=fVal;
				//infoPanel.setWavelength(fVal);
				break;
			case WidgetEvent.WIDTH:
				//slitEngine and nslitDraggerEngine use micrometers...
				slitEngine.slitWidth=fVal;
				slitEngine.evaluate();
				//Then convert to millimeters for engine/UI/WSL
				fVal/=1000;
				//engine.width=fVal;
				//infoPanel.setWidth(fVal);
				widthField.setValue(FPRound.toFixVal(fVal,2));
				break;
			case WidgetEvent.DISTANCE:
				//slitEngine and nslitDraggerEngine use micrometers...
				//slitEngine.distance=nslitDraggerEngine.distance;
				slitEngine.distance=fVal;
				slitEngine.evaluate();
				//Then convert to millimeters for engine/UI/WSL
				fVal/=1000;
				//engine.distance=fVal;
				//infoPanel.setDistance(fVal);
				distanceField.setValue(FPRound.toFixVal(fVal,2));
				break;
			}
			//wslPlayer.recordMouseDragged(wslStrings[wevt.getWidget()],""+fVal);
			//engine.evaluate();
			break;
		case WidgetEvent.SCREEN_MESH:
			//Print coordinate (scaled from [-500,500] for VRML to [-10,10] mm)
			//statusBar.setText("Position on screen: "+sdl.math.FPRound.toFixVal(((EventOutSFVec3f)who).getValue()[0]/50,2)+" mm; intensity = "+sdl.math.FPRound.toFixVal(engine.intensityAt(((EventOutSFVec3f)who).getValue()[0]/50),2));
			//statusBar.setText(traducir("position_label")+sdl.math.FPRound.toFixVal(((EventOutSFVec3f)who).getValue()[0]/50,2)+traducir("intensity_label")+sdl.math.FPRound.toFixVal(engine.intensityAt(((EventOutSFVec3f)who).getValue()[0]/50),2));
			break;
		}
	} //END IGNORE
	
	// Change the width of the slits
	boolean setWidth(float width) {
		//setFromTextField = true;

		//////////////////////////////////////////////////////////////////////////
		// update the slit aperture
		float oldWidth=slitEngine.slitWidth;
		slitEngine.slitWidth = width * 1000.0f;
		if(!slitEngine.evaluate()) {
			slitEngine.slitWidth=oldWidth;
			//setFromTextField=false;
			return false;
		}

		//////////////////////////////////////////////////////////////////////////
		// update the draggers
		nsd_set_width.setValue(width * 1000.0f);

		//////////////////////////////////////////////////////////////////////////
		// run the simulation
		//engine.width = width;
		//if(autoUpdate) engine.evaluate();

		//setFromTextField = false;
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	// Change the distance between the slits
	boolean setDistance(float distance) {
		//setFromTextField = true;

		//////////////////////////////////////////////////////////////////////////
		// update the slit aperture
		float oldDistance=slitEngine.distance;
		slitEngine.distance = distance * 1000.0f;
		if(!slitEngine.evaluate()) {
			slitEngine.distance=oldDistance;
			//setFromTextField=false;
			return false;
		}

		//////////////////////////////////////////////////////////////////////////
		// update the draggers
		nsd_set_distance.setValue(distance * 1000.0f);

		//////////////////////////////////////////////////////////////////////////
		// run the simulation
		//engine.distance = distance;
		//if(autoUpdate) engine.evaluate();

		//setFromTextField = false;
		return true;
	}
	
	public void setPhotonCount(int count) {
		photonCount.setText(String.valueOf(count));
	}
	
	public void setElapsedTime(float time) {
		elapsedTime.setText(String.valueOf(FPRound.toFixVal(time,1)));
	}
	
	public void evaluatePlot() {
		float plotValues[] = new float[PLOT_RES];
		float xstep = 20000/(float)PLOT_RES;
		float x = -10000;
		for (int q=0; q<PLOT_RES; q++) {
			 plotValues[q] = PLOT_SCALE*engine.compute2SlitIntensity(wavelengthField.getValue()*1000,widthField.getValue(),x*1000,1000,distanceField.getValue());
			 x+=xstep;
		}
		plot.setValues(plotValues);
	}
	
	// -----------------------------------------------------------------------------------
	// Animation Controls
	// -----------------------------------------------------------------------------------
	
	public void setPlaying() {
		exposureTimeField.setEnabled(false);
		setState(PLAYING);
		animationButton.setLabel("Pause");
		animation.setPaused(false);	
	}
	
	public void setPaused() {
		setState(PAUSED);
		animationButton.setLabel("Resume");
		animation.setPaused(true);
	}
	
	public void setStopped() {
		//rateField.setEnabled(true);
		exposureTimeField.setEnabled(true);
		setState(STOPPED);
		animationButton.setLabel("Play");
		animation.setPaused(true);
		engine.reset();
		engine.updateScreen();
	}
	
	public int getState() {
		return animationState;
	}

	public void setState(int st) {
		animationState = st;
	}
	
	public void setBrightness(float value) {
		transparency.setValue(((float)MAX_BRIGHTNESS-value)/(float)MAX_BRIGHTNESS);
	}
	
	public float getExposureTime() {
		return exposureTimeField.getValue();
	}	
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==animationButton) {
			if (getState()==STOPPED) {
				setPlaying();
			}
			else if (getState()==PLAYING) {
				setPaused();
			}
			else if (getState()==PAUSED) {
				setPlaying();
			}
		}
		else if (e.getSource()==clearButton)
			engine.clearScreen(UPDATE);
		else if (e.getSource()==resetButton) {
			setStopped();
			engine.reset();
			engine.clearScreen(UPDATE);
			setDefaultValues();
		}
	}
	
	public void stateChanged(StateButton button, int k) {
		if (button==audioButton) {
			engine.setAudio(k==1);
		}
	}
	
	public void numChanged(NumberBox source, Number newVal) {
		if (source == wavelengthField) {
			data.wavelength=newVal.floatValue();
			if (!wavelengthWidget.isActive())
				wavelengthWidget.setValue(data.wavelength);
		}
		else if (source == widthField) {
			data.width=newVal.floatValue();
			//slitEngine.slitWidth=newVal.floatValue();
			//slitEngine.evaluate();
			setWidth(newVal.floatValue());
		}
		else if (source == distanceField) {
			data.distance=newVal.floatValue();
			//slitEngine.distance=nslitDraggerEngine.distance;
			//slitEngine.evaluate();
			setDistance(newVal.floatValue());
		}
		else if (source == rateField) {
			engine.setRate(newVal.intValue());
		}
		animation.setData(data);
		if (!animation.isPaused())
			animation.update();
		evaluatePlot();
	}
	
	public void adjustmentValueChanged(AdjustmentEvent evt) {
		//System.out.println((100-evt.getValue())/100f);
		setBrightness(evt.getValue());
	}
	
	public void invalidEntry(NumberBox source,Number newVal) {
		this.showStatus("Invalid Entry");
		DebugPrinter.println("invalidEntry("+newVal+')');
	}
	
	public void boundsForcedChange(NumberBox source,Number oldVal) {}
	
	// -----------------------------------------------------------------------------------
	// WSL Routines
	// -----------------------------------------------------------------------------------
	
	public String getWSLModuleName() {
		return "twoslit";
	}
	/*Won't even use the following methods...ignore*
	public void initialize(WSLScriptEvent event) {
		WSLAttributeList init=event.getNode().getAttributes();
		for(int i=0; i<init.getLength(); i++) {
			if (init.getName(i).equals("animationState")) {
				System.out.println(getState());
				setState((WTString.toInt(init.getValue(i),2)));
			}
		}
	}
	
	public void playerStateChanged(WSLPlayerEvent event) {
		final int id = event.getID();
		switch(id) {
		case WSLPlayerEvent.PLAYER_STARTED:
			if (getState()==PLAYING)
				setPlaying();
			else if (getState()==PAUSED)
				setPaused();
			else if (getState()==STOPPED)
				setStopped();
			break;
		case WSLPlayerEvent.PLAYER_STOPPED:
			animation.setPaused(true);
		}
	}
	
	public void scriptActionFired(WSLScriptEvent event) {};*/
	
	//Will use this method, but for now ignore...we'll put the scripting stuffs in later [JD]
	protected void toWSLNode(WSLNode node) {
		/*won't use the following two lines
		final WSLAttributeList atts=node.getAttributes();
		atts.add("animationState",String.valueOf(getState()));*/
		
		wavelengthScripter.addTo(node);
		rateScripter.addTo(node);
		widthScripter.addTo(node);
		distanceScripter.addTo(node);
		exposureTimeScripter.addTo(node);
		//animationScripter.addTo(node);
		audioScripter.addTo(node);
	}
	
}
