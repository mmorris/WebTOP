/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.nslit;

/**
 * @author Kiril Vidimce (original)
 * @author Rachel Mueller (additions)
 * @author Paul Cleveland (X3D/WApplication)
 * @author Grant Patten	   (additions)
 */


//Grant -- Stuff
/* 
 * Remaining BUGS:
 * If you drag a widget too fast, and it gives an invalid value, the aperture screen doesn't update until after you release the mouse
 * nSquareText is not right justified properly->Submit X3D Bug Report? 
 */

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.sdl.gui.numberbox.*;
import org.sdl.gui.numberbox.NumberBox.BoundsException;
import org.sdl.math.FPRound;
import org.sdl.math.Function;
import org.sdl.math.Lambda;
import org.webtop.component.StateButton;
import org.webtop.component.ToggleButton;
import org.webtop.component.ToolBar;
import org.webtop.component.WApplication;
import org.webtop.util.*;
import org.webtop.util.script.ButtonScripter;
import org.webtop.util.script.NumberBoxScripter;
import org.webtop.util.script.StateButtonScripter;
import org.webtop.wsl.client.WSLPlayer;
import org.webtop.wsl.event.WSLScriptEvent;
import org.webtop.wsl.event.WSLScriptListener;
import org.webtop.wsl.script.WSLNode;
import org.webtop.x3d.output.*;
import org.webtop.x3d.widget.*;
import org.web3d.x3d.sai.MFString;



public class NSlit extends WApplication implements NumberBox.Listener,
org.webtop.x3d.widget.SpatialWidget.Listener, WSLScriptListener {
	

	
	
	public void initialize(WSLScriptEvent event) {
	}

	
	public void scriptActionFired(WSLScriptEvent event) {		
	}

	
	protected String getAuthor() {
		return "Kiril Vidimce, Rachel Mueller, Paul Cleveland, Grant Patten";
	}

	
	protected String getDate() {
		return null;
	}

	
	protected Component getFirstFocus() {
		return slitNumField;
	}

	
	protected int getMajorVersion() {
		return 6;
	}

	
	protected int getMinorVersion() {
		return 1;
	}
	
	
	protected int getRevision() {
		return 1;
	}

	protected String getModuleName() {
		return "Fraunhofer N-slit Diffraction";
	}
	
	public NSlit(String title, String world) {
		super(title, world,true,false);
	}
	
	
	////*******MODULE SPECIFIC CODE*******////
	
	//*** Constants ***//
	final static float  nanoToMicro  = 0.001f,  // nanometers to micrometers
						milliToMicro = 1000.0f, // millimeters to micrometers
						screenScale  = 1.0f,    // scales the screen intensity
						plotScale    = 400.0f,  // scales the 2D plot
						nanoToMilli  = 0.000001f,
						WAVELENGTH_MIN = 400,
						WAVELENGTH_MAX = 700,
						SLIT_DIST_MIN  = 0.08f,
						SLIT_DIST_MAX  = 0.96f,
						SLIT_WIDTH_MIN  = 0.0f,
						SLIT_WIDTH_MAX = .2680603f,
						ZDIST_MIN      = 1000f,
						ZDIST_MAX      = 2000f,
						ZSCALE         = 20f,
						BLUE_WAVE      = 400.0f,
						RED_WAVE       = 660.0f,
						RED_WAVE_COLORMODEL  = 630.0f,
						HUE_SCALE      = 240.0f,
						SATURATION     = 1.0f,
						PEAK_TOL       = 0.01f,
						APERTURE_WIDTH = 500f,
						APERTURE_HEIGHT = 150f,
						LINE_PLOT_WIDTH = 1000f;
	final static int    SINGLE = 0,
     					NSLIT  = 1,
     					HIRES  = 800,
     					HIRES_X = HIRES/2,
						HIRES_Y = HIRES/6,
     					LOWRES = 200,
     					LOWRES_X = LOWRES/2,
     					LOWRES_Y = LOWRES/6,
     					ORIG_HIRES = 1500,
     					ORIG_LOWRES = 160,
     					SLIT_NUM_MIN = 1,
     					SLIT_NUM_MAX = 10;

	//Defaults
	final static float  SLIT_WIDTH_DEF = 0.1f,
						SLIT_DIST_DEF  = 0.5f,
	 					WAVELENGTH_DEF = 500,
	 					ZDIST_DEF      = 1000;
	final static int	SLIT_NUM_DEF   = 2;
	
	//*** X3D Controls ***//
	// Screen, line, aperture, and custom NSlit controls
	MultiGrid observationScreen;  //NSlit uses a hi- and low-res screen
	LinePlot plot;                //Intensity line plot
	BoxMaker apertureScreen;      //Calculation engine for aperture screen
	NSlitDragger nslitDragger;    //Calculation engine for slit dragger constraints
	WTInt N;                      //Wrapper for N=number of slits, shared between this, BoxMaker, and NSlitDragger
	Switch widgetsSwitch;         //Switch widgets on/off
	Switch distWidgetSwitch;      //Switch cloned slitDistWidget on/off
	TouchSensor screenSensor;     //Reads position of cursor over the screen
	
	//Widgets & Couplers
	XDragWidget   slitWidthWidget;  //width dragger (original, changes USE copied to clone)
	XDragWidget   slitDistWidget;   //distance dragger (original, changes USE copied to clone)
	XDragWidget   zDistWidget;      //screen dragger
	WheelWidget   wavelengthWidget;
	ScalarCoupler wavelengthCoupler, zDistCoupler, slitWidthCoupler, slitDistCoupler;

	MFString nSquareText;				//Text displayed in upper right of screen
	
	
	
	
	//Scripters
	private NumberBoxScripter waveLengthScripter, zDistanceScripter, slitDistScripter, slitWidthScripter, slitNumScripter;
	private ButtonScripter resetScripter;
	private StateButtonScripter widgetScripter;
	
	//GUI Elements
	IntBox slitNumField;
	FloatBox slitWidthField, slitDistField, wavelengthField, zDistField;
	JButton reset;
	ToggleButton hideWidgets;
	
	//*** Setup Methods ***//

	//Set up X3D connections
	protected void setupX3D() {
		//Set up screen
		observationScreen = new MultiGrid(
				new Switch(getSAI(), getSAI().getNode("ScreenSwitch"), 2),
				new IFSScreen[] {
					new IFSScreen(new IndexedSet(getSAI(), getSAI().getNode("IFSHigh")), new int[][]{{HIRES_X, HIRES_Y}}, APERTURE_WIDTH, APERTURE_HEIGHT),
					new IFSScreen(new IndexedSet(getSAI(), getSAI().getNode("IFSLow")), new int[][]{{LOWRES_X, LOWRES_Y}}, APERTURE_WIDTH, APERTURE_HEIGHT)
				}
		);
		observationScreen.setup();
		
		
		
		plot = new LinePlot(new IndexedSet(getSAI(), getSAI().getNode("LinePlot")), LINE_PLOT_WIDTH, HIRES_X*2,LOWRES_X*2);
		
		N = new WTInt(SLIT_NUM_DEF);
		
		//Sets up Switches
		widgetsSwitch = new Switch(getSAI(), getSAI().getNode("WidgetsSwitch"), 1);
		distWidgetSwitch = new Switch(getSAI(), getSAI().getNode("DistWidgetSwitch"), 1);
		
		//Sets up screen sensor
		screenSensor = new TouchSensor(getSAI(), getSAI().getNode("ScreenSensor"), (short)1, "Screen touch sensor");
		screenSensor.setOver(true);
		screenSensor.addListener((SpatialWidget.Listener)this);
		
		//Set up Widgets
		slitWidthWidget  = new XDragWidget(getSAI(), getSAI().getNode("SlitWidthWidget"), (short)2, "Slit width widget");  
		slitDistWidget   = new XDragWidget(getSAI(), getSAI().getNode("SlitDistWidget"),  (short)3, "Slit distance widget"); 
		zDistWidget      = new XDragWidget(getSAI(), getSAI().getNode("ZDistWidget"),     (short)4, "Z distance widget");
		wavelengthWidget = new WheelWidget(getSAI(), getSAI().getNode("WavelengthWidget"),     (short)5, "Wavelength widget");
		
		//Sets up listeners for widgets
		slitWidthWidget.addListener(this);
		slitDistWidget.addListener(this);

		
		//Sets Up Text for Scale
		nSquareText = (MFString)getSAI().getField(getSAI().getNode("NSQUARE_TEXT"),"string");
		
		//Create NSlitDragger NSD
		nslitDragger = new NSlitDragger(slitWidthWidget, slitDistWidget, N);
		nslitDragger.evaluateDistance();
		
		
		//Create BoxMaker B
		apertureScreen = new BoxMaker(getSAI(), "ApertureScreen", slitWidthWidget, slitDistWidget,N);
		
		}
	
	
	
	//Set up GUI
	protected void setupGUI() {
		//Set up NumberBoxes
		slitNumField    = new IntBox(SLIT_NUM_MIN, SLIT_NUM_MAX, SLIT_NUM_DEF, 3);
		slitWidthField  = new FloatBox(0, nslitDragger.getMaxWidth(), SLIT_WIDTH_DEF, 5);			// Was 0 - .25
		slitDistField   = new FloatBox(nslitDragger.getMinDistance(), nslitDragger.getMaxDistance(), SLIT_DIST_DEF, 5);			// Was 0 - .48
		wavelengthField = new FloatBox(WAVELENGTH_MIN, WAVELENGTH_MAX, WAVELENGTH_DEF, 5);
		zDistField      = new FloatBox(ZDIST_MIN, ZDIST_MAX, ZDIST_DEF, 5);
		
		//Set up NumberListeners
		slitNumField.addNumberListener(this);
		slitWidthField.addNumberListener(this);
		slitDistField.addNumberListener(this);
		wavelengthField.addNumberListener(this);
		zDistField.addNumberListener(this);
		
		/////////////////////////
		//Set up Couplers
		
		//Functions for Distance Widget
		Function distWidgetToBox = new Function() {
			public double eval(double widgetIn) 
			{
				float toRet = (float) (2*widgetIn/(N.getValue() - 1)/500.0f);
				System.out.println("\n\n\ndistWidgetToBox value = " + toRet);
				return toRet;
			}
		};
		
		Function distBoxToWidget = new Function() {
			public double eval(double boxIn) 
			{
					float toRet = (float)(boxIn/2.0 * (N.getValue() - 1)*500.0f);
					System.out.println("\n\n\ndistBoxToWidget value = " + toRet);
					return toRet;
			}				
		};
		
		slitWidthCoupler  = new ScalarCoupler(slitWidthWidget,  slitWidthField, 3, new ScalarCoupler.Converter(Lambda.linear(500.0,0),Lambda.linear(1.0/500, 0)));
		slitDistCoupler   = new ScalarCoupler(slitDistWidget,   slitDistField,  3, new ScalarCoupler.Converter(distBoxToWidget,distWidgetToBox));
		wavelengthCoupler = new ScalarCoupler(wavelengthWidget, wavelengthField, 3);
		zDistCoupler      = new ScalarCoupler(zDistWidget,      zDistField, 3, new ScalarCoupler.Converter(Lambda.linear(.5,-500),Lambda.linear(2, 1000)));
		//
		////////////////////////////////////////
		
		//Set up buttons
		reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDefaults();
			}
		});
		
		hideWidgets = new ToggleButton("Hide Widgets", "Show Widgets", false);
        hideWidgets.addListener(new StateButton.Listener() {
            public void stateChanged(StateButton sb, int state) {
                    widgetsSwitch.setVisible(hideWidgets.getStateBool());
            }
        });
        
        //Initialize Scripters 
        waveLengthScripter = new NumberBoxScripter(wavelengthField,
    		    getWSLPlayer(), null, "wavelength", new Float(WAVELENGTH_DEF));
    		
    	zDistanceScripter = new NumberBoxScripter(zDistField,
    		    getWSLPlayer(), null, "z", new Float(ZDIST_DEF));
    		
    	slitWidthScripter = new NumberBoxScripter(slitWidthField,
    		    getWSLPlayer(), null, "slitWidth", new Float(SLIT_WIDTH_DEF));
    		
    	slitDistScripter = new NumberBoxScripter(slitDistField,
    			    getWSLPlayer(), null, "slitDistance", new Float(SLIT_DIST_DEF));
    	
    	slitNumScripter = new NumberBoxScripter(slitNumField,
    			 getWSLPlayer(), null, "numberOfSlits", new Float(SLIT_NUM_DEF));
    		
    	widgetScripter = new StateButtonScripter(hideWidgets, getWSLPlayer(), null, 
    			"hideWidgets", new String[] {"Hide Widgets", "Show Widgets"}, 0);
    		
    	resetScripter = new ButtonScripter(reset, getWSLPlayer(), null, "reset");
        
    	/* GUI Layout:
         *  |---------------------|
         *  | Slit Controls       |
         *  |---------------------|
         *  | Wavelength, Z Dist. |
         *  |---------------------|
         *  | Reset, Hide Widgets |
         *  |---------------------|
         */        
        controlPanel.setLayout(new GridLayout(3,1));
        
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Slits:"));
        topPanel.add(slitNumField);
        topPanel.add(new JLabel(" Slit Width:"));
        topPanel.add(slitWidthField);
        topPanel.add(new JLabel(" Slit Distance:"));
        topPanel.add(slitDistField);
        controlPanel.add(topPanel);
        
        JPanel middlePanel = new JPanel();
        middlePanel.add(new JLabel("Wavelength:"));
        middlePanel.add(wavelengthField);
        middlePanel.add(new JLabel(" Z Distance:"));
        middlePanel.add(zDistField);
        controlPanel.add(middlePanel);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(reset);
        bottomPanel.add(hideWidgets);
        controlPanel.add(bottomPanel);

		//Set up the toolbar
		ToolBar toolBar = getToolBar();
		toolBar.addBrowserButton("Directions", "/org/webtop/html/nslit/directions.html");
		toolBar.addBrowserButton("Theory","/org/webtop/html/nslit/theory.html");
		toolBar.addBrowserButton("Examples","/org/webtop/html/nslit/examples.html");
		toolBar.addBrowserButton("Exercises", "/org/webtop/html/nslit/exercises.html");	
		toolBar.addBrowserButton("Images","/org/webtop/html/nslit/images.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
		
	updateScreen();
	setDefaults();
	}
	
	//Sets up the menubar
	protected void setupMenubar() {
	}

	//Resets the module to defaults
	protected void setDefaults() {
		//Set Default Values
		slitWidthField.setValue(0);
		slitNumField.setValue(SLIT_NUM_DEF);
		slitDistField.setValue(SLIT_DIST_DEF);
		slitWidthField.setValue(SLIT_WIDTH_DEF);
		wavelengthField.setValue(WAVELENGTH_DEF);
		zDistField.setValue(ZDIST_DEF);
	
		//Set Bounds
		setMinMax(slitDistField,SLIT_DIST_MIN,SLIT_DIST_MAX);
		setMinMax(slitWidthField,SLIT_WIDTH_MIN,SLIT_WIDTH_MAX);
		
		slitDistWidget.setMin(0f);
		slitDistWidget.setMax(480.0f);
		slitDistWidget.setMin(40.0f);
		
		slitWidthWidget.setMin(0f);
		slitWidthWidget.setMax(110.0f);
		 
		//Send Values and Update Screens
		apertureScreen.updateDistance(SLIT_DIST_DEF);
		apertureScreen.updateWidth(SLIT_WIDTH_DEF);
		
		updateScreen();
	}

	//*** Numberbox.Listener Interface ***//
	
	public void boundsForcedChange(NumberBox source, Number oldVal) {
		
		if(source==slitWidthField)
			DebugPrinter.println("NSlit.boundsForcedChange(): slitWidthField bound changed from " + oldVal);
		else if(source==slitDistField)
			DebugPrinter.println("NSlit.boundsForcedChange(): slitDistField bound changed from " + oldVal);
		//If neither of these, let's just see what else gets changed.
		else
			DebugPrinter.println("NSlit.boundsForcedChange(): " + source + " bound changed from " + oldVal);
	}

	//Notify of invalid value entries
	public void invalidEntry(NumberBox source, Number badVal) {
		if(source==slitWidthField)
			statusBar.setWarningText("Slit width bounds are [" + slitWidthField.getMin() + ", " + slitWidthField.getMax() + "]");
		else if(source==slitDistField)
			statusBar.setWarningText("Slit distance bounds are [" + slitDistField.getMin() + ", " + slitDistField.getMax() + "]");
		else if(source==slitNumField)
			statusBar.setWarningText("Slit number bounds are [" + SLIT_NUM_MIN + ", " + SLIT_NUM_MAX + "]");
		else if(source==wavelengthField)
			statusBar.setWarningText("Wavelength bounds are [" + WAVELENGTH_MIN+ ", " + WAVELENGTH_MAX + "]");
		else if(source==zDistField)
			statusBar.setWarningText("Screen distance bounds are [" + ZDIST_MIN + ", " + ZDIST_MAX + "]");
		else
			statusBar.setWarningText("Unknown field has changed bounds");
	}

	public void numChanged(NumberBox source, Number newVal) {
		//** DEBUG: Tracking when a numberbox changes **//
		if(source==slitWidthField)
			DebugPrinter.println("NSlit.numChanged(): Slit width changed to " + slitWidthField.getValue());
		else if(source==slitDistField)
			DebugPrinter.println("NSlit.numChanged(): Slit dist changed to " + slitDistField.getValue());
		else if(source==slitNumField)
			DebugPrinter.println("NSlit.numChanged(): Slit num changed to " + slitNumField.getValue());
		else if(source==zDistField)
			DebugPrinter.println("NSlit.numChanged(): z Distance changed to " + zDistField.getValue());
		//** END DEBUG **//
		
		//If slit count changed, update N, NSD, and BoxMaker 
		if(source==slitNumField) {	
			int oldVal=N.getValue();		//Keeps the old value of N
			int val = newVal.intValue();	//Stores the new value
			
			//Checks to make sure the new N value is valid
			//If it is not, print a warning message and restore the values
			if(!nslitDragger.validN(val))
			{
				statusBar.setWarningText("N too large for current slit width and distance");
				val=oldVal;	//Resets N if N is invalid
				slitNumField.setValue(val);
			}
			
			N.setValue(val);
			
			nslitDragger.updateDistanceDraggerConstraints();
			nslitDragger.updateWidthDraggerConstraints();
			setMinMax(slitDistField,nslitDragger.getMinDistance(),nslitDragger.getMaxDistance());
			setMinMax(slitWidthField,nslitDragger.getMinWidth(),nslitDragger.getMaxWidth());
			
			//EVALUATE THE NEW DISTANCE
			//If it was a single slit
			if(oldVal==1)
			{
				apertureScreen.updateDistance(nslitDragger.evaluateSingle());
			}
			else
				nslitDragger.evaluateDistance();
			
			//Sets The Scale Display
			nSquareText.setValue(1,new String[]{""+(val*val)});
			
			
			//If N==1, hide distance widgets and then update NSD and BoxMaker
			if(val==1) {
				distWidgetSwitch.setVisible(false);
			}
			else
				distWidgetSwitch.setVisible(true);
		}
		
		
		else if(source==slitDistField) {
			apertureScreen.updateDistance(newVal.floatValue());
			setMinMax(slitDistField,nslitDragger.getMinDistance(),nslitDragger.getMaxDistance());
			setMinMax(slitWidthField,nslitDragger.getMinWidth(),nslitDragger.getMaxWidth());
		}
		
		else if(source==slitWidthField){
			apertureScreen.updateWidth(newVal.floatValue());
			setMinMax(slitDistField,nslitDragger.getMinDistance(),nslitDragger.getMaxDistance());
			setMinMax(slitWidthField,nslitDragger.getMinWidth(),nslitDragger.getMaxWidth());
		}
		
		//If wavelength or zDistField changed, no worries 
		else {
			//If unknown source, notify of the problem
			if(!source.equals(wavelengthField) && !source.equals(zDistField)) {
				System.out.println("NSlit.numChanged(): Invalid event source!");
				return;
			}
		}
		
		//In any event, update the screen
		updateScreen();
	}
	
	public void valueChanged(ScalarWidget src, float value)
	{
	}
	

	// This function takes a field, and sets the min and max at the same time so as not to throw an exception
	public void setMinMax(FloatBox fb, float min, float max)
	{
		String source;
		if(fb==slitWidthField){
			source="Slit Width Field";
			System.out.println("setMinMax values: fb: " + source + " min: " + min + " max: " + max);
		}
		else if(fb==slitDistField){
			source="Slit Dist Field";
			System.out.println("setMinMax values: fb: " + source + " min: " + min + " max: " + max);
		}
		else
			source="UNKNOWN SOURCE";
		
		DebugPrinter.println("Setting " + source + ":: MIN->" + min + " MAX->" + max);
		
		if(min>=max)
			throw new BoundsException(fb,"Max cannot be less than min.");
		if(min>fb.getMax())
		{
			fb.setMax(max);
			fb.setMin(min);
		}
		else
		{
			fb.setMin(min);
			fb.setMax(max);
		}
	}
	

	
	//Used by ScreenSensor to find intensity at a point on the screen
	public void valueChanged(SpatialWidget src, float x, float y, float z) {
		
	float 
		wavelength 	= wavelengthField.getValue(),
		width 		= slitWidthField.getValue(),
		distance 	= slitDistField.getValue(),
		zCurrent	= zDistField.getValue();	  
	
	float
		l = wavelength 		* nanoToMicro,  //was * shrinkx
		w = width			* milliToMicro, //was * shrinkx
		d = distance		* milliToMicro, //was * shrinkx
		zDist = zCurrent	* milliToMicro;
		int n = N.getValue();
		
		float
		lz	 = l * zDist,
		w2	 = w * w;
		
		x/=50;
		float xDist=x*milliToMicro; //Converts X3D units into mm
		
		float intensity=computeNSlitIntensity(lz, w, w2, xDist, zDist, n, d); //X was * shrinkx
		
		statusBar.setText("X = " + FPRound.toSigVal(x,3) + "mm" + " Intensity = " + FPRound.toSigVal(intensity,4));
	}
	
	public void invalidEvent(String node, String event) {
		updateScreen();
	}

	
	//*** Screen Calculation/Updating ***//
		
	//Updates the screen after a widget is released
	protected void setWidgetDragging(Widget w, boolean drag) {
		if(!drag)
			updateScreen();
	}
	
	protected void updateScreen()
	{
	if(!apertureScreen.evaluate())
		System.out.println("Aperture Screen Evaluation Failed");
	evaluate();					//Calculates points for screen
	observationScreen.setup();	//Displays hiRes or low Res Screen
	}
	

	public static float
	computeNSlitIntensityOld(	float lz, 	// lambda * z
							float w,	// width
							float w2, 	// width^2
							float x,	// x
							float z,	// z
							float n,	// N
							float d)	// distance
	{
		final float
			xp = x * (float) Math.PI,	// x * Pi
			alpha = xp * w / lz,		// alpha(x, z)
			beta = xp * d / lz;			// beta(x, z)
		float f, g;						// f(x, z), g(x, z)

		if(x == 0.0f)
			f = 1;
		else
			f = (float) Math.pow(Math.sin(alpha) / alpha, 2);

		if(Math.abs(beta-Math.PI*Math.round(beta/Math.PI)) < PEAK_TOL)
			g = n * n;
		else
			g = (float) Math.pow(Math.sin(n * beta) / Math.sin(beta), 2);

		return w2 * f * g / lz;
	}
	
	public static float
	computeNSlitIntensity(	float lz, 	// lambda * z
							float w,	// width
							float w2, 	// width^2
							float x,	// x
							float z,	// z
							float n,	// N
							float d)	// distance
	{
		final float
			xp = x * (float) Math.PI,		// x * Pi
			phi = 2 * xp * d / lz,			// phi(x, z)
			beta = 2* xp * w / lz;			// beta(x, z)
		float f, g;							// f(x, z), g(x, z)

		if(x==0&&w!=0)
			g = 1;
		else
			g = (float) Math.pow( Math.sin(beta/2) / (beta/2), 2);

		if(Math.abs(phi-2*Math.PI*Math.round(phi/(2*Math.PI))) < PEAK_TOL)
			f = n * n;
		else
			f = (float) Math.pow(Math.sin(n*phi/2) / Math.sin(phi/2), 2);

		return f*g;
	}
	
	//Evaluates the display screen
	void evaluate() {
		//*** Additional variables from Screen needed for calculations.  May not all be necessary. [PC] ***//
		float wavelength = wavelengthField.getValue(),
			  width = slitWidthField.getValue(),
			  distance = slitDistField.getValue(),
			  zCurrent = zDistField.getValue(),
			  xStart	=	0,	//Left edge of the screen
				xEnd	=	10000;	//Right edge
			  
		
		////////////////////////////////////////////////////////////////////
		// if any of the widgets are active, use lower resolution
		//*** int xResolution = interacting?xDynamic:xMeshPoints;
		boolean drag=this.draggingWidget();
		int ROWS = drag ? LOWRES_Y : HIRES_Y;
		int COLS = drag ? LOWRES_X : HIRES_X;
		
		
		float
			rgb[] = new float[3],					// rgb of a point on the observation screen
			l,										// wavelength
			w,										// width of the slits
			d,										// distance between the slits
			z,										// position of the screen
			xStep,									// step between two grid point along x
			hue;									// hue used to compute the RGB value


		////////////////////////////////////////////////////////////////////
		// change of units to micrometers
		l = wavelength 	* nanoToMicro; 
		w = width		* milliToMicro; 
		d = distance	* milliToMicro; 
		z = zCurrent	* milliToMicro;
		int n = N.getValue();
		
		////////////////////////////////////////////////////////////////////
		// compute the step between two grid point along the x axis
		xStep			 = (xEnd - xStart) / (float)COLS;


		////////////////////////////////////////////////////////////////////
		// compute the hue; saturation is constant and determined by SATURATION
		hue = WTMath.hue(wavelengthField.getValue());
		
		////////////////////////////////////////////////////////////////////
		// various goodies to speed up the calculations
		float
			x,
			lz	 = l * z,
			w2	 = w * w;
		
		////////////////////////////////////////////////////////////////////
		// OLD NORMALIZATION FOR OLD CALCULATE NSLIT INTNSITY
		// Normalization for the intensity. We normalize by:
		//	 (w^2 / (lambda * z)) * N^2	
		//float
		//	normalization = w2 / lz * (n * n);
		
		////////////////////////////////////////////////////////////////////
		// Normalization for the intensity. We normalize by:
		// N^2	
		float
			normalization = (n * n);
		

		////////////////////////////////////////////////////////////////////
		// intensity related variables
		float
			intensity, value, clampedNormalizedValue;

		
		float [][]screen_color = new float[ROWS*COLS][3];
		float []plot_points	 = new float[COLS*2];

		DebugPrinter.println("Wavelength in " + l + " micrometers");
		DebugPrinter.println("Current z  in " + z + " micrometers");
		DebugPrinter.println("Width      in " + w + " micrometers");
		DebugPrinter.println("Distance   in " + d + " micrometers");

		x = 0;
		for(int xctr = 0; xctr < COLS; xctr++, x+=xStep) {
			//////////////////////////////////////////////////////////////////
			// compute the intensity
			intensity = computeNSlitIntensity(lz, w, w2, x, z, n, d); //x was * shrinkx

			//////////////////////////////////////////////////////////////////
			// normalize the intensity
			intensity /= normalization;
			
			//////////////////////////////////////////////////////////////////
			// clamp the normalized intensity
			clampedNormalizedValue=WTMath.bound(intensity, 0, 1);
			
			//////////////////////////////////////////////////////////////////
			// to avoid excessive brightness, we take the square root of the
			// actual intensity
			value = (float) Math.sqrt(clampedNormalizedValue); 
			
			//////////////////////////////////////////////////////////////////
			// compose an HSV representation of the color on the screen and
			// convert it to RGB
			WTMath.hsv2rgb(rgb, hue, WTMath.SATURATION, value);
			
			
			//Assigns the color to the entire column
			for(int y=0;y<ROWS;y++)
			{
				screen_color[xctr*ROWS+y][0]=rgb[0];
				screen_color[xctr*ROWS+y][1]=rgb[1];
				screen_color[xctr*ROWS+y][2]=rgb[2];
			}
			//Assigns the value to the lineplot
			plot_points[xctr+COLS]=plot_points[COLS-xctr-1]=clampedNormalizedValue*plotScale;
		}

		////////////////////////////////////////////////////////////////////
		// send the coordinates, colors, and indices to the X3D browser
	
		
		plot.setValues(plot_points);
		observationScreen.showGrid(drag?1:0);
		observationScreen.setColors(screen_color);
		observationScreen.setup();
	}
	
	
	//***********************WSL ROUTINES********************************//
	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
    	waveLengthScripter.addTo(node);
    	zDistanceScripter.addTo(node);
    	slitDistScripter.addTo(node);
    	slitWidthScripter.addTo(node);
    	slitNumScripter.addTo(node);
	}

	public String getWSLModuleName() {
		return new String("nslit");
	}
		
	public static void main(String[] args) {
		NSlit nslit = new NSlit("Fraunhofer N-slit Diffraction", "/org/webtop/x3dscene/nslit.x3dv");
	}
}
