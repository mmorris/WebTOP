/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

/*
 * <p>Title: Fraunhofer Rayleigh Resolution</p>
 * 
 * <p>Description: The X3D version of The Optics Project for the web (WebTOP)</p>
 * 
 * <p>Company:MSU Department of Physics and Astronomy</p>
 * 
 * @author Grant
 * @version 0.0 
 */
package org.webtop.module.rayleigh;



import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.sdl.gui.numberbox.FloatBox;
import org.sdl.gui.numberbox.NumberBox;
import org.sdl.gui.numberbox.NumberBox.Listener;
import org.sdl.math.FPRound;
import org.sdl.math.Function;
import org.sdl.math.Lambda;
import org.webtop.component.StateButton;
import org.webtop.component.ToggleButton;
import org.webtop.component.ToolBar;
import org.webtop.component.WApplication;
import org.webtop.util.script.ButtonScripter;
import org.webtop.util.script.NumberBoxScripter;
import org.webtop.util.script.StateButtonScripter;
import org.webtop.util.WTMath;
import org.webtop.wsl.client.WSLPlayer;
import org.webtop.wsl.event.WSLScriptEvent;
import org.webtop.wsl.event.WSLScriptListener;
import org.webtop.wsl.script.WSLNode;
import org.webtop.x3d.output.AbstractGrid;
import org.webtop.x3d.output.IFSScreen;
import org.webtop.x3d.output.IndexedSet;
import org.webtop.x3d.output.LinePlot;
import org.webtop.x3d.output.MultiGrid;
import org.webtop.x3d.output.Switch;
import org.webtop.x3d.widget.ScalarCoupler;
import org.webtop.x3d.widget.SpatialWidget;
import org.webtop.x3d.widget.TouchSensor;
import org.webtop.x3d.widget.WheelWidget;
import org.webtop.x3d.widget.Widget;
import org.webtop.x3d.widget.XDragWidget;
import org.webtop.component.Separator;
import org.webtop.x3d.widget.RotationWidget;
import org.webtop.x3d.X3DObject;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.web3d.x3d.sai.*;

public class Rayleigh extends WApplication implements NumberBox.Listener,
		org.webtop.x3d.widget.SpatialWidget.Listener, WSLScriptListener,
		ActionListener {

	public Rayleigh(String title, String world) {
		super(title,world,true,false);
		
		menu=getJMenuBar();
	}
	
	
	protected String getAuthor() {
		// TODO Auto-generated method stub
		return "Grant Patten";
	}

	
	protected String getDate() {
		// TODO Auto-generated method stub
		return null;
	}

	
	protected Component getFirstFocus() {
		// TODO Auto-generated method stub
		return null;
	}

	
	protected int getMajorVersion() {
		// TODO Auto-generated method stub
		return 6;
	}

	
	protected int getMinorVersion() {
		// TODO Auto-generated method stub
		return 1;
	}

	
	protected String getModuleName() {
		// TODO Auto-generated method stub
		return "Fraunhofer Rayleigh Resolution";
	}

	
	protected int getRevision() {
		// TODO Auto-generated method stub
		return 1;
	}

	
/* CONSTANST TO BE USED WITH THIS MODULE */

public static final float		WAVELENGTH_MIN = 400f, 
								WAVELENGTH_MAX = 700f, 
								WAVELENGTH_DEFAULT = 550f,
								DIAMETER_MIN = 0.125f,
								DIAMETER_MAX = 7.5f,
								DIAMETER_DEFAULT = 3.355f,
								ANGLE_MIN = 0f,   
								ANGLE_MAX = 0.01f, //1.57079796327f
								ANGLE_DEFAULT = 6.0E-5f,
								Z_DISTANCE_MIN = 5.15f,
								Z_DISTANCE_MAX = 100f, 
								Z_DISTANCE_DEFAULT = 50f,
								Z_SCALE 			= 20f,
								
								SHRINK = 4f, //SHRINKS along the z axis (Visual HACK (LIE/WRONG/NON-PHYSICAL/MISLEADING!)) 4.0f
								
								
								NM_TO_MM		  = 0.000001f,//converts wavelength wheel's nm units to mm for math	
								MILI_TO_MICRO	= 1000.0f, //converts Millimeters to Micrometers
								NANO_TO_MICRO = 0.001f,		//Converts Nanometers to Micrometers

								SCREEN_SCALE	 =	 0.65f,				// scales the screen intensity
								PLOT_SCALE		 =	2.0f,				// scales the 2D plot  
								
								M_MIN_INTENSITY =	0.0f,			//-.1 Should be betwen 2 and 0, but that is not case becasue of numerical errors
								M_MAX_INTENSITY =	1500.0f;		//GRANT -- Lower than actual max intensity...looks good though! (Might recalc live)

private static final int		QUAD_WIDTH 		   = 500, //width of 1 x3d quadrant(x3d units)of the observation screen
								QUAD_HEIGHT		   = 500,								
																	//THESE WERE:
								X_POINTS_HIGH=500,Y_POINTS_HIGH=300,  //161 and 81
								X_POINTS_LOW=82,Y_POINTS_LOW=42;	//69 and 35 162/82
/*
 * END GENERAL WAPPLICATION METHODS*****************************
 */
	
	/* NODE DECLARATIONS FOR X3DV FILE*/
	//Widgets
	XDragWidget zDistanceWidget;
	XDragWidget diameterWidget;
	RotationWidget angleWidget;     
	RotationWidget angle2Widget;
	WheelWidget waveLengthWidget;
	
	
	
	//Screens-HiRes and LoRes
	IFSScreen ifsHigh;
	IFSScreen ifsLow;
	MultiGrid observationScreen; 
	public boolean isDrag=false;
	
	//Switches
	Switch widgetSwitch;
	Switch hideFrontSwitch;
	LinePlot intensityLine;
	
	//Touch Sensor that allows screen intensity to be read
	TouchSensor screenTouch;
	
	/*GUI Elements*/
	FloatBox waveLengthBox;
	FloatBox diameterBox;
	FloatBox zDistanceBox;
	FloatBox angleBox;
	
	//JLabel to hold the Minimum Angle
	JLabel minAnglePanel;
	JLabel minAngleDisplay;
	public JMenuBar menu; //for the menubar at the top of the module
	
	//Buttons on the Display Panel
	JButton resetButton;
	ToggleButton hideWidgets;
	ToggleButton hideFront;
	
	/*Couplers*/
	ScalarCoupler waveLengthCoupler;
	ScalarCoupler zDistanceCoupler;
	ScalarCoupler diameterCoupler;
	ScalarCoupler angleCoupler;
	ScalarCoupler angle2Coupler;
	
	//Scripters
	private NumberBoxScripter waveLengthScripter, zDistanceScripter, diameterScripter, angleScripter;
	private ButtonScripter resetScripter;
	private StateButtonScripter widgetScripter;
	
	/* USED TO EVAULUATE */
	//Used for the LinePlot
	private static final int LINE_RES_HIGH = X_POINTS_HIGH*2; 
	private static final int LINE_RES_LOW = X_POINTS_LOW*2;	
	private static final float SCREEN_SIZE =1000f;
	
	
	
	protected void setDefaults() {
		//Set the defaults shown on the Display Screen
		hideWidgets.setState(0);
		waveLengthBox.setValue(WAVELENGTH_DEFAULT);
		diameterBox.setValue(FPRound.toSigVal(DIAMETER_DEFAULT,4));		
		zDistanceBox.setValue(Z_DISTANCE_DEFAULT);
		angleBox.setValue(ANGLE_DEFAULT);
		
		updateScreen();

	}


	
	
	
	protected void setupGUI() {		
		//Set the layout for the control panel of the module
		controlPanel.setLayout(new GridLayout(2,1));
		
		//Grouping Panels
		JPanel topPanel = new JPanel();
		JPanel middlePanel = new JPanel();
		//GRANT -- Might want to add a new group of panels to house Angle and Min Angle? and the new switch...GUI Needs retouch
		
		
		//set up the float boxes for the x3dv file
		waveLengthBox = new FloatBox(WAVELENGTH_MIN, WAVELENGTH_MAX, WAVELENGTH_DEFAULT,5);
		waveLengthBox.addNumberListener(this);
		diameterBox = new FloatBox(DIAMETER_MIN, DIAMETER_MAX, DIAMETER_DEFAULT, 5);
		diameterBox.addNumberListener(this);
		zDistanceBox = new FloatBox(Z_DISTANCE_MIN, Z_DISTANCE_MAX, Z_DISTANCE_DEFAULT, 5);
		zDistanceBox.addNumberListener(this);
		angleBox = new FloatBox(ANGLE_MIN, ANGLE_MAX, ANGLE_DEFAULT,5);
		angleBox.addNumberListener(this);
		
		
		//Add components to topPanel
		topPanel.add(new JLabel("Wavelength:"));
		topPanel.add(waveLengthBox);
		topPanel.add(new JLabel("nm"));
		topPanel.add(new JLabel("      "));
		
		topPanel.add(new JLabel("Diameter:"));
		topPanel.add(diameterBox);
		topPanel.add(new JLabel("cm"));
		topPanel.add(new JLabel("      "));
		
		topPanel.add(new JLabel("f:"));
		topPanel.add(zDistanceBox);
		topPanel.add(new JLabel("mm"));
		topPanel.add(new JLabel("      "));
		
		topPanel.add(new JLabel("Angle:"));
		topPanel.add(angleBox);
		topPanel.add(new JLabel("rad"));
		topPanel.add(new JLabel("      "));
		
		minAnglePanel = new JLabel("Min Angle: "); 
		topPanel.add(minAnglePanel);
		minAngleDisplay = new JLabel("");  //minAngle number must be calculated somewhere
		topPanel.add(minAngleDisplay);
		topPanel.add(new JLabel("rad"));
		
		//Add components to the middlePanel
		JLabel empty2 = new JLabel("                                                   ");
		middlePanel.add(empty2);
		middlePanel.add(empty2);
		middlePanel.add(empty2); 
		
		//add the reset button to the JPanel
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		
		

		/*Use this on the fly method to hide the widgets on the X3DV 
		 * screen. Uses a boolean variable to select between states.*/
		hideWidgets = new ToggleButton("Hide Widgets","Show Widgets", false);
		hideWidgets.addListener(new StateButton.Listener()
		{
			public void stateChanged(StateButton sb, int state)
			{
				//Switch between visible and hidden on the screen based on the state of the ToggleButton
				widgetSwitch.setVisible(hideWidgets.getStateBool());
			}
		});
		
		hideFront = new ToggleButton("Hide Screen and Lens","Show Screen and Lens",false);
		hideFront.addListener(new StateButton.Listener()
		{
			public void stateChanged(StateButton sb, int state)
			{
				//Switch between visible and hidden on the screen based on the state of the ToggleButton
				hideFrontSwitch.setVisible(hideFront.getStateBool());
			}
		});
		
		
		middlePanel.add(resetButton);
		middlePanel.add(new JLabel("   "));
		middlePanel.add(hideWidgets);
		middlePanel.add(new JLabel("   "));
		middlePanel.add(hideFront);
		
		JLabel empty = new JLabel("                                                         ");
		middlePanel.add(empty);
		
		//Add Couplers
		waveLengthCoupler = new ScalarCoupler(waveLengthWidget, waveLengthBox,1,
				new ScalarCoupler.Converter(Lambda.linear(1, 0),Lambda.linear(1, 0))) ;

		zDistanceCoupler = new ScalarCoupler(zDistanceWidget, zDistanceBox,1,
						new ScalarCoupler.Converter(Lambda.linear(1,0),Lambda.linear(1, 0)));
		//For the zDistanceCoupler--supposed to use 1/Z_SCALE in first calculation and Z_SCALE in the 2nd
		//but this gives an invalid input from widget error
		
		diameterCoupler = new ScalarCoupler(diameterWidget,diameterBox,4,
					  new ScalarCoupler.Converter(Lambda.linear(1,0),Lambda.linear(1,0)));
		
		
		/* FUNCTIONS FOR ANGLE COUPLERS */
		Function angleToBox = new Function() {
			public double eval(double theta) 
			{
				return (float) Math.atan(Math.tan(theta)/1000.0f)/5.0f;
			}
		};
		
		Function boxToAngle = new Function() {
			public double eval(double boxIn) 
			{
					return (float) Math.atan(4f*(float)Math.tan(5.0f*boxIn)*1000.0f/4.0f);
			}				
		};
		
		Function angle2ToBox = new Function() {
			public double eval(double theta) 
			{
				return (float)-1 * Math.atan(Math.tan(theta)/1000.0f)/5.0f;
			}
		};
		
		Function boxToAngle2 = new Function() {
			public double eval(double boxIn) 
			{
					return (float) -1 * Math.atan(4f*(float)Math.tan(5.0f*boxIn)*1000.0f/4.0f);
			}				
		};
		/*END ANGLE COUPLER FUNCTIONS*/
		
		angleCoupler = new ScalarCoupler(angleWidget,angleBox,4,
					  new ScalarCoupler.Converter(boxToAngle,angleToBox));
		
		angle2Coupler = new ScalarCoupler(angle2Widget,angleBox,4,
				  new ScalarCoupler.Converter(boxToAngle2,angle2ToBox));
		
		
		
		//Initialize Scripters 
		waveLengthScripter = new NumberBoxScripter(waveLengthBox,
		    getWSLPlayer(), null, "wavelength", new Float(WAVELENGTH_DEFAULT));
		
		zDistanceScripter = new NumberBoxScripter(zDistanceBox,
		    getWSLPlayer(), null, "z", new Float(Z_DISTANCE_DEFAULT));
		
		diameterScripter = new NumberBoxScripter(diameterBox,
		    getWSLPlayer(), null, "diameter", new Float(DIAMETER_DEFAULT));
		
		angleScripter = new NumberBoxScripter(angleBox,
			    getWSLPlayer(), null, "angle", new Float(ANGLE_DEFAULT));
		
		widgetScripter = new StateButtonScripter(hideWidgets, getWSLPlayer(), null, 
			"hideWidgets", new String[] {"Hide Widgets", "Show Widgets"}, 0);
		
		resetScripter = new ButtonScripter(resetButton, getWSLPlayer(), null, "reset");
		
		
		//Adding Sub Panels to GUI
		controlPanel.add(topPanel);
		controlPanel.add(middlePanel);

		
		//Set up the toolbar
		ToolBar toolBar = getToolBar();
		toolBar.addBrowserButton("Directions", "/org/webtop/html/rayleigh/directions.html");
		toolBar.addBrowserButton("Theory","/org/webtop/html/rayleigh/theory.html");
		toolBar.addBrowserButton("Examples","/org/webtop/html/rayleigh/examples.html");
		toolBar.addBrowserButton("Exercises", "/org/webtop/html/rayleigh/exercises.html");
		toolBar.addBrowserButton("Images","/org/webtop/html/rayleigh/topimages.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
		
		
		//Call Update Screen to reflect the changes being made
		setDefaults();
		updateScreen();
		
	}

	/**
	 * Used to listen for widget events and to select
	 * between high resolution/low resolution screen.
	 * @param w - Widget object that is checked for drag/no drag.
	 * @param drag - boolean variable that is true if widget is being dragged. 
	 * 				False otherwise.
	 */
	protected void setWidgetDragging(Widget w, boolean drag) {
		//Switch to the low res screen if a widget is being dragged
		//Switch to the high res screen if nothing is being dragged
		observationScreen.showGrid(drag?1:0);
		isDrag=drag?true:false;
		//Call updateScreen() to reflect the changes made
		updateScreen();
	}
	
	
	protected void setupMenubar() { //Grant -- Does this do anything?
		// TODO Auto-generated method stub

	}

	
	protected void setupX3D() {
		/*set up the X3D Nodes from the .x3dv file associated with this module */
			
		waveLengthWidget = new WheelWidget(getSAI(),getSAI().getNode("wavelengthWidget"),(short)1,
				"Use this wheel to adjust the wavelength");
		waveLengthWidget.addListener(this);
		
		diameterWidget = new XDragWidget(getSAI(), getSAI().getNode("rightWidthDragger"),(short)2, 
				"Use this red cone dragger to adjust the width of the slit"); 
		diameterWidget.addListener(this);
		
		zDistanceWidget = new XDragWidget(getSAI(), getSAI().getNode("ScreenDragger"),(short)3, 
				"Use this red cone dragger to adjust the z Distance");
		zDistanceWidget.addListener(this);
		
		angleWidget = new RotationWidget(getSAI(), getSAI().getNode("angleWidget"),(short)4, 
		"Use this rod to adjust the angle");
		angleWidget.addListener(this);
		angle2Widget = new RotationWidget(getSAI(), getSAI().getNode("angleWidget2"),(short)5, 
		"Use this rod to adjust the angle");
		angle2Widget.addListener(this);

		
		widgetSwitch = new Switch(getSAI(), getSAI().getNode("WidgetsSwitch"),1);
		hideFrontSwitch = new Switch(getSAI(), getSAI().getNode("hideFrontSwitch"),1);
		intensityLine = new LinePlot(new IndexedSet(getSAI(), getSAI().getNode("ilsNode")),SCREEN_SIZE/2,LINE_RES_HIGH,LINE_RES_LOW);
		
		
		//Set up the touch sensor for screen intensity readouts
		screenTouch = new TouchSensor(getSAI(), getSAI().getNode("ScreenTouch"),(short)5,
				null);
		screenTouch.setOver(true);
		screenTouch.addListener((SpatialWidget.Listener)this);
		
		
		
		//Set up observation screen:
		//for high resolution display surface
		ifsHigh = new IFSScreen(new IndexedSet(getSAI(), getSAI().getNode("ifsNode")),new int [][] {{X_POINTS_HIGH, Y_POINTS_HIGH}}, QUAD_WIDTH,QUAD_HEIGHT); 
		ifsLow = new IFSScreen(new IndexedSet(getSAI(), getSAI().getNode("ifsNodeLowRes")),new int [][] {{X_POINTS_LOW, Y_POINTS_LOW}}, QUAD_WIDTH,QUAD_HEIGHT);
		observationScreen = new MultiGrid(new Switch(getSAI(), getSAI().getNode("ResolutionSwitch"),2),
				new AbstractGrid[] {ifsHigh,ifsLow});
		observationScreen.setup();
		
		/*End Set up observation screen*/
		}

	
	//***********************WSL ROUTINES********************************//
	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
		waveLengthScripter.addTo(node);
		zDistanceScripter.addTo(node);
		diameterScripter.addTo(node);
		widgetScripter.addTo(node);
		angleScripter.addTo(node);
	}

	public String getWSLModuleName() {	
		return new String("rayleigh");
	}
	
	
	//***********************END WSL ROUTINES***************************//
	
	//**EVENT HANDLING**
	
	public void boundsForcedChange(NumberBox source, Number oldVal) {
		// TODO Auto-generated method stub

	}
	
	
	public void invalidEntry(NumberBox source, Number badVal) {
		if(source.equals(waveLengthBox))
			getStatusBar().setWarningText("Invalid Entry: Wave Length Must be Between "+ WAVELENGTH_MIN + " and " + WAVELENGTH_MAX);
		if(source.equals(zDistanceBox))
			getStatusBar().setWarningText("Invalid Entry: Z Distance Must be Between " + Z_DISTANCE_MIN + " and " + Z_DISTANCE_MAX);
		if(source.equals(diameterBox))
			getStatusBar().setWarningText("Invalid Entry: Diamaeter Must be Between " + DIAMETER_MIN + " and " + DIAMETER_MAX);	
		if(source.equals(angleBox))
			getStatusBar().setWarningText("Invalid Entry: Angle Must be Between " + ANGLE_MIN + " and " + ANGLE_MAX);
	}
	

	public void numChanged(NumberBox source, Number newVal) {
		updateScreen();
	}

	//Calculates Intensity for use in the status bar
	float rayleighIntensity(float x, float y, 
					float z, float l, float d, float t) 
		{
		final float ft2 = z * (float) Math.tan(t / 2.0f),
		lplus = (float) Math.sqrt((float) Math.pow(x - ft2, 2.0f) +
						(float) Math.pow(y, 2.0f)),
		lminus = (float) Math.sqrt((float) Math.pow(x + ft2, 2.0f) +
						 (float) Math.pow(y, 2.0f));
		
		final float ic			= rayleighIc(z, d, l),
		parenth = ((float) Math.PI * d) / (l * z);
		
		float u, besincplus, besincminus;
		
		u = parenth * lplus;
		besincplus = (float) Math.pow(2 * WTMath.j1(u) / u, 2);
		u = parenth * lminus;
		besincminus = (float) Math.pow(2 * WTMath.j1(u) / u, 2);
		return (ic * (besincplus + besincminus));
		}

		float rayleighIc(float z, float d, float l) {
		return ((float) Math.pow((0.25 * 0.4 * Math.PI * d * d) / (l * z), 2));
		}
	
	
	public void valueChanged(SpatialWidget src, float x, float y, float z) {
		float
		zD 		= zDistanceBox.getValue() * 10 * MILI_TO_MICRO / SHRINK,
		theta 	= angleBox.getValue(),
		d		= diameterBox.getValue() * MILI_TO_MICRO,
		l		= waveLengthBox.getValue() * NANO_TO_MICRO;
		
		//X -500,500  = -25 , 25
		//Y -150,150  = -7.5, 7.5
		
		//GRANT -- Status bar is kind of weird...because of the SHRINK applied
		// 	the X and Y Values aren't what they really are... Same way the Angle widgets
		//  aren't to scale...Need to figure out how to do status bar....
		//Sets the status Bar to 4 significant figures.  
		statusBar.setText("X = "+FPRound.toSigVal(x/SHRINK,4) //+ "cm"
				+ " Y = "+FPRound.toSigVal(y/SHRINK,4) //+ "cm"
				+ "  intensity = "
				+ FPRound.toSigVal(rayleighIntensity(x/SHRINK , y/SHRINK, zD, l, d, theta*10),4)
				);
	}

	
	public void initialize(WSLScriptEvent event) {
		// TODO Auto-generated method stub

	}

	
	public void scriptActionFired(WSLScriptEvent event) {
		// TODO Auto-generated method stub

	}

	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(resetButton))
			setDefaults();
	}

	
	public void invalidEvent(String node, String event) {
		// TODO Auto-generated method stub

	}
	
	
	/****************************************************/
	protected void updateScreen()
	{
	evaluate();					//Calculates points for screen
	observationScreen.setup();	//Displays hiRes or low Res Screen
	}
	

	
	//NEW EVALUATE
	void evaluate() {
	int xMeshPoints;
	int yMeshPoints;

	if(isDrag) {
		xMeshPoints = X_POINTS_LOW;
		yMeshPoints = Y_POINTS_LOW;
	} else {
		xMeshPoints = X_POINTS_HIGH;
		yMeshPoints = Y_POINTS_HIGH;
	}

	float
	rgb[] = new float[3],					// rgb of a point on the observation screen
	l,										// wavelength
	d,										// diameter of lens (Micrometers)
	theta,									// Angle 
	z,										// Z Distance of Screen
	yStep,									// step between two grid points along y
	xStep;									// step between two grid points along x

	int xEnd=500,			//-512 512		
		xStart=0;
	
	int yEnd=-150,			//-512 512
		yStart=150;

	final float hue=WTMath.hue(waveLengthBox.getValue());
	
	
			   
	

	z 		= zDistanceBox.getValue() * 10 * MILI_TO_MICRO / SHRINK;
	theta 	= angleBox.getValue();
	d		= diameterBox.getValue() * 10 /*cm->mm*/ * MILI_TO_MICRO;
	l		= waveLengthBox.getValue() * NANO_TO_MICRO;

	//SETS THE MIN ANGLE
	minAngleDisplay.setText("" + FPRound.toSigVal(1.22f * l / d,4));

	//Apparently, d actually has to be in 10s of microns.	 So we do that.
	d/=10;
	
	xStep			 = (float)(xEnd - xStart) / xMeshPoints; //Moves a total distance over so many points
	yStep			 = (float)(yEnd - yStart) / yMeshPoints;
	
	// various goodies to speed up the calculations
	float x, y;
	int i, j;
	int	yMeshPointsHalf = (yMeshPoints + 1) / 2;
	float intensity, value, normalizedIntensity;
	int	resolution = xMeshPoints * yMeshPoints,
		pointIndex;
	
	float [][]color = new float[resolution][3];	//Stores 1 quadrant (Half) worth of colors (Was xMeshPointsHalf * YMeshPoints

	// 2D graph
	float []graphPoint = new float[xMeshPoints*2];

	
	
	//Y goes from .150 to -.150 nm...
	//X goes from 0 to .5 nm...
	//COLOR FLOWS TOP TO BOTTOM/LEFT
	
	float mini=1000, maxi=0;
		for(x=0, j = 0; j < xMeshPoints; j++) {
			for(y = 150, i = 0; i < yMeshPointsHalf; i++) {	
			// compute the intensity
			
			intensity =
				rayleighIntensity(x/SHRINK, y/SHRINK, z, l, d, theta * 10);	//Theta is Multiplied by 10
			
			if(intensity<mini)
				mini=intensity;
			if(intensity>maxi)
				maxi=intensity;
			//////////////////////////////////////////////////////////////////
			// normalize the intensity
			normalizedIntensity =  (intensity - M_MIN_INTENSITY) / (M_MAX_INTENSITY - M_MIN_INTENSITY);
			value = WTMath.bound(normalizedIntensity,0,1);
			value = (float) Math.sqrt(value);
			
			
			//////////////////////////////////////////////////////////////////
			// compose an HLS representation of the color on the screen and
			// convert it to RGB
			//GRANT -- could add this option somewhere else...
			boolean isColor = true;
			WTMath.hls2rgb(rgb, hue, value, isColor?WTMath.SATURATION:0);
			
			pointIndex=i+j*yMeshPoints;
			color[pointIndex][0]=rgb[0];
			color[pointIndex][1]=rgb[1];
			color[pointIndex][2]=rgb[2];
			
			pointIndex=(j+1)*yMeshPoints-(i+1);
			color[pointIndex][0]=rgb[0];
			color[pointIndex][1]=rgb[1];
			color[pointIndex][2]=rgb[2];
			
			// do this only for the y = 0 axis
			if(i == yMeshPointsHalf-1) {
				//graphPoint[j]=graphPoint[xMeshPoints-j-1]=value * PLOT_SCALE;
				graphPoint[xMeshPoints+j]=graphPoint[xMeshPoints-j-1]= (float) Math.log(normalizedIntensity + 1.0) * PLOT_SCALE;
				//They set it equal to (float) Math.log(normalizedIntensity + 1.0) * graphScale;
			}
			
			// move to the next Y position
			y += yStep;
		}
		// move to the next X position
		x += xStep;
	}
		
		//System.out.println("MIN = " + mini);
		//System.out.println("MAX = " + maxi);
	
		intensityLine.setValues(graphPoint);		
		observationScreen.setColors(color);
}
	
  


	
	
	public static void main(String[] args) {
		Rayleigh rayleigh=new Rayleigh("Rayleigh Resolution", "/org/webtop/x3dscene/rayleigh.x3dv");  
		
	}
}
	

