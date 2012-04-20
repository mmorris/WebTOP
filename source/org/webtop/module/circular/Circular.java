/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

/**
 * <p>Title: Fresnel Circular</p>
 * 
 * <p>Description: The X3D version of The Optics Project
 * for the web(WebTOP)</p>
 * 
 * <p>Company:MSU Department of Physics and Astronomy</p>
 * 
 * @author Kiril Vidimce Updated by: Jeremy Davis, Paul Cleveland, Matt Hogan, Brian Thomas
 *@version 0.0
 */
package org.webtop.module.circular;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.sdl.gui.numberbox.*;
import org.sdl.math.Lambda;
import org.sdl.gui.numberbox.NumberBox.Listener;
import org.web3d.x3d.sai.MFColor;
import org.web3d.x3d.sai.MFInt32;
import org.web3d.x3d.sai.MFVec3f;
import org.webtop.component.*;
import org.webtop.util.DebugPrinter;
import org.webtop.util.WTMath;
import org.webtop.wsl.script.WSLNode;
import org.webtop.x3d.widget.*;
import org.webtop.x3d.SAI;
import org.webtop.x3d.output.*;
import org.sdl.math.*;
import org.webtop.util.*;
import org.webtop.util.script.*;

//WSL imports
import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;


/**
 * @author Kiril Vidimce(reworked by Davis Herring)
 * Updated to use WApplication by: Jeremy  Davis, Paul Cleveland, Matt Hogan, Brian Thomas
 *
 */
public class Circular extends WApplication implements NumberBox.Listener, ActionListener, SpatialWidget.Listener
{

	
	//****The following methods are general Wapplication methods used in each module********\\

	//Wapplication calls this before calling the constructor
	//This allows set up of data structures before calling 
	//any methods that use them
	public void preconstructor(){
		
		//set the defaults for the Screen variables
		int xMeshPoints,yMeshPoints,radialResolution;
		
		//From Screen.java
		xStart=DEF_XSTART;
		xEnd=DEF_XEND;
		yStart=DEF_YSTART;
		yEnd=DEF_YEND;
		screenZ=Circular.Z_DISTANCE_DEFAULT;
		wavelength=Circular.WAVELENGTH_DEFAULT;
		radius=new WTFloat(Circular.DIAMETER_DEFAULT/2);
		
		
		
		
		for(int res=0;res<2;++res) {
			 boolean loRes=res==1;

			// if active, use lower res
			if(loRes) {
				xMeshPoints = X_RES_LOW;
				yMeshPoints = Y_RES_LOW;
			} else {
				xMeshPoints = X_RES_HIGH;
				yMeshPoints = Y_RES_HIGH;
			}
			float xStep=(xEnd-xStart)/xMeshPoints,
						yStep=(yEnd-yStart)/yMeshPoints;

			float[][] intensities;		//pointer to current array
			
			//We allocate our data arrays here, but we mostly use them in evaluate().

			// Since the screen is rectangular, we need to compute enough points to
			// reach the corners of the screen. We choose 1.5 instead of sqrt(2) for
			// simplicity.
			//Note: this code assumes a square screen! [Davis]
			if(loRes) {
				radialResolution=radialResolutionLow=(int) (1.5f * xMeshPoints);
				maxRadiusLow=radialResolutionLow*xStep;
				intensities=intensitiesLow=new float[radialResolutionLow][3];

				radialColorsLow=new float[radialResolutionLow][3];
				colorsLow=new float[xMeshPoints*yMeshPoints][3];
			} else {
				radialResolution=radialResolutionHigh=(int) (1.5f * xMeshPoints);
				maxRadiusHigh=radialResolutionHigh*xStep;
				intensities=intensitiesHigh=new float[radialResolutionHigh][3];

				radialColorsHigh=new float[radialResolutionHigh][3];
				colorsHigh=new float[xMeshPoints*yMeshPoints][3];
			}
			
			
		     points = new float[xMeshPoints*yMeshPoints][];
			 cindex = new int[(xMeshPoints - 1) * (yMeshPoints - 1) * 5];
			 ils_cindex = new int[xMeshPoints];
			int index = 0;
			for(int iy = 0; iy < yMeshPoints; iy++) {
				for(int ix = 0; ix < xMeshPoints; ix++) {
					points[ix*yMeshPoints+iy]=new float[] {ix*xStep,iy*yStep,0};

					//For the intensity plot (only one ix loop needed)
					if(iy==0) {
						ils_cindex[ix]=ix;
						intensities[ix][0]=ix*xStep;		//z is left at 0; y is calculated in evaluate()
					}

					if(ix==xMeshPoints-1) break;		//last iteration of this loop not used for cindex
					if(iy==yMeshPoints-1) continue;	//nor that of this loop
					cindex[index++] = iy * xMeshPoints + ix;
					cindex[index++] = (iy + 1) * xMeshPoints + ix;
					cindex[index++] = (iy + 1) * xMeshPoints + ix + 1 ;
					cindex[index++] = (iy * xMeshPoints) + ix + 1 ;
					cindex[index++] = -1;
				}
			}
		}
		
	}
	
	/**
	 * Constructor for Circular Object
	 * @param title - Title of the Module
	 * @param world - x3dv file to be used in this module
	 */
	public Circular(String title, String world)
	{
		super(title, world, true, false);
		//called to allow for a dropdown menu. 
		menu = getJMenuBar();
		//menu.getComponentPopupMenu().setLightWeightPopupEnabled(false);
	}
	
	/**
	 * Returns the name of the author(s).
	 */
	protected String getAuthor() {
		return "Jeremy Davis, Paul Cleveland, Matt Hogan, Brian Thomas";
	}
	
	/**
	 * Returns the date this module was completed
	 */
	protected String getDate() {
		
		return null;
	}

	/**
	 * Places the cursor in its initial position.
	 */
	protected Component getFirstFocus() {
		return null;
	}

	/**
	 * Returns major version.
	 */
	protected int getMajorVersion() {
		return 6;
	}

	/**
	 * Returns minor version.
	 */
	protected int getMinorVersion() {
		return 1;
	}

	/**
	 * Returns the name of the module.
	 */
	protected String getModuleName() {
		return "Fresnel Circular";
	}
	
	/** 
	 * Returns the number of which revision this module is.
	 */
	protected int getRevision() {
		return 1;
	}
	
	//End general Wapplication methods****************************************
	
	//Constants to be used in this module
	public static final float		WAVELENGTH_MIN = 400f, 
									WAVELENGTH_MAX = 700f, 
									WAVELENGTH_DEFAULT = 500f,
									DIAMETER_MIN = 0.0001f,
									DIAMETER_MAX = 1.0f,
									DIAMETER_DEFAULT = 0.70710678f, //same as sqrt(2)/2
									Z_DISTANCE_MIN = 20f,
									Z_DISTANCE_MAX = 200f, 
									Z_DISTANCE_DEFAULT = 50f,
									Z_SCALE 			= 20f,
									NM_TO_MM		  = 0.000001f;//converts wavelength wheel's nm units to mm for math
	
	private static final int		QUAD_WIDTH 		   = 500, //width of 1 x3d quadrant(x3d units)of the observation screen
									HIRES_SCREEN	   = 100, //High Resolution Screen
									LORES_SCREEN      = 50,  // Low Resolution Screen
									HIRES 			  = 100,
									LORES			  = 50, 
									HIGRID			  = HIRES*HIRES, //Used in updateScreen() to determine GRID
									LOGRID 			  = LORES*LORES; //Used in updateScreen() to determine GRID
	
	//Static constants used to select between aperture and obstacle mode
	public static final int			APERTURE_MODE = 1,
									OBSTACLE_MODE = 0,
									OBSTACLE_SCALE=3;
	//private int used to select between aperture and obstacle mode
	private int mode = APERTURE_MODE;
	
	
	//Used in evaluate()************************************************
	private static final int X_RES_HIGH = 100,Y_RES_HIGH = 100,
							 X_RES_LOW = 50,Y_RES_LOW = 50;
	private static final int SERIES_TERMS_L1 = 5,
	 						 SERIES_TERMS_L2 = 7,
	 						 SERIES_TERMS_L3 = 30;
	
	public static final float DEF_XSTART = 0f,DEF_XEND = 500f,
							  DEF_YSTART = 0f,DEF_YEND = 500f;
							  
	

	public boolean 					isActive=false,
									isColor=true;
	
	private int radialResolutionHigh;
	private float maxRadiusHigh;
	private int radialResolutionLow;
	private float maxRadiusLow;
	public float xStart,xEnd,yStart,yEnd,screenZ,wavelength;
	public WTFloat radius;
	private static final float minIntensity=-0.1f;
	private float maxIntensity=4;

	private float[][] radialColorsHigh,colorsHigh;
	private float[][] intensitiesHigh;
	private float[][] radialColorsLow,colorsLow;
	private float[][] intensitiesLow;
	float[] lineIntensity;

	//Used in preconstructor() and evaluate()
	private int[] cindex;
	private float[][] points;
	private int[] ils_cindex;
	
	//Used for the LinePlot
	private static final int LINE_RES_HIGH = 150;
	private static final int LINE_RES_LOW = 75;
	private static final float SCREEN_SIZE =1000f;
	////////////////////////////////////////////////////////
	
    //*************************End variables used in evaluate()******************************/
	
	/* NODE DECLARATIONS FOR X3DV FILE*/
	//Widgets
	XDragWidget zDistanceWidget;
	XDragWidget apertureWidget;
	WheelWidget waveLengthWidget;
	
	//Screens-HiRes and LoRes
	IFSScreen ifsHigh;
	IFSScreen ifsLow;
	MultiGrid observationScreen; 
	
	//Switches
	Switch widgetSwitch;
	LinePlot intensityLine;
	
	//Touch Sensor that allows screen intensity to be read
	TouchSensor screenTouch;
	
	//ApertureEngine is the circular hole/obstacle.  
	ApertureEngine apertureEngine;
	
	/*GUI Elements*/
	FloatBox waveLengthBox;
	FloatBox diameterBox;
	FloatBox zDistanceBox;
	//JLabel to hold Fresnel Number
	JLabel fresnelPanel;
	JLabel fresnelDisplay;
	public JMenuBar menu; //for the menubar at the top of the module
	//Buttons on the Display Panel
	JButton resetButton;
	ToggleButton hideWidgets;
	StateButton obstacleButton;
	
	/*Couplers*/
	ScalarCoupler waveLengthCoupler;
	ScalarCoupler zDistanceCoupler;
	ScalarCoupler apertureCoupler;
	
	//Scripters
	private NumberBoxScripter waveLengthScripter, zDistanceScripter, apertureScripter;
	private ButtonScripter resetScripter;
	private StateButtonScripter obstacleButtonScripter, widgetScripter;
	//private ToggleButtonScripter widgetScripter1;
	//private NavigationPanelScripter nps;
	//WSL player
	private WSLPlayer wslPlayer;
	
	
	//******************************Begin Module Specific Code For Circular*******************/
	/**
	 * Sets the initial GUI settings for this module. 
	 * 
	 */
	protected void setupGUI() {
		//Set the layout for the control panel of the module
		controlPanel.setLayout(new GridLayout(3,1));
		
		
		
		//Grouping Panels
		JPanel topPanel = new JPanel();
		JPanel middlePanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		//set up the float boxes for the x3dv file
		waveLengthBox = new FloatBox(WAVELENGTH_MIN, WAVELENGTH_MAX, WAVELENGTH_DEFAULT,5);
		waveLengthBox.addNumberListener(this);
		diameterBox = new FloatBox(DIAMETER_MIN, DIAMETER_MAX, DIAMETER_DEFAULT, 5);
		diameterBox.addNumberListener(this);
		zDistanceBox = new FloatBox(Z_DISTANCE_MIN, Z_DISTANCE_MAX, Z_DISTANCE_DEFAULT, 5);
		zDistanceBox.addNumberListener(this);
		
		//Add components to topPanel
		topPanel.add(new JLabel("Wavelength:"));
		topPanel.add(waveLengthBox);
		topPanel.add(new JLabel("nm"));
		topPanel.add(new JLabel("Diameter:"));
		topPanel.add(diameterBox);
		topPanel.add(new JLabel("mm"));
		topPanel.add(new JLabel("z:"));
		topPanel.add(zDistanceBox);
		topPanel.add(new JLabel("mm"));
		fresnelPanel = new JLabel("Fresnel Number:"); //fresnel number must be calculated somewhere
		topPanel.add(fresnelPanel);
		fresnelDisplay = new JLabel("");
		topPanel.add(fresnelDisplay);
		
		//Add components to the middlePanel
		
		JLabel empty2 = new JLabel("                                                   ");
		middlePanel.add(empty2);
		obstacleButton = new StateButton("Show Obstacle", "Show Aperture",false);
		/*Listen for changes made to the obstacle button.
		 * Use this on the fly method to listen to changes in
		 * the button.  The StateButton argument is used to
		 * select between 2 states.*/
		obstacleButton.addListener(new StateButton.Listener()
		{
			public void stateChanged(StateButton sb, int state)
			{
				//Get the state of the StateButton object
				mode = sb.getState();
				//System.out.println("Obstacle Button is: "+ sb.getState());
				
				//call updateScreen() to reflect changes
				updateScreen();
			}
		}
		);
		//add the obstacle button to the JPanel
		middlePanel.add(obstacleButton);
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		//add the reset button to the JPanel
		middlePanel.add(resetButton);
		
		
		hideWidgets = new ToggleButton("Hide Widgets","Show Widgets", false);
		/*Use this on the fly method to hide the widgets on the X3DV 
		 * screen. Uses a boolean variable to selcet between states.*/
		hideWidgets.addListener(new StateButton.Listener()
		{
			public void stateChanged(StateButton sb, int state)
			{
				//Switch between visible and hidden on the screen based on the state of the ToggleButton
				widgetSwitch.setVisible(hideWidgets.getStateBool());
			}
		});
		//add the hidewidgets button to the Jpanel
		middlePanel.add(hideWidgets);
		
		JLabel empty = new JLabel("                                                         ");
		middlePanel.add(empty);
		
		//Add Couplers
		waveLengthCoupler = new ScalarCoupler(waveLengthWidget, waveLengthBox,1,
							new ScalarCoupler.Converter(Lambda.linear(1, 0),Lambda.linear(1, 0))) ;
		
		zDistanceCoupler = new ScalarCoupler(zDistanceWidget, zDistanceBox,1,
							new ScalarCoupler.Converter(Lambda.linear(1,0),Lambda.linear(1, 0)));
		//For the zDistanceCoupler--supposed to use 1/Z_SCALE in first calculation and Z_SCALE in the 2nd
		//but this gives an invalid input from widget error
		
		apertureCoupler = new ScalarCoupler(apertureWidget,diameterBox,4,
						  new ScalarCoupler.Converter(Lambda.linear(1,0),Lambda.linear(1,0)));
		
		//Initialize Scripters
		waveLengthScripter = new NumberBoxScripter(waveLengthBox,
                getWSLPlayer(), null, "wavelength", new Float(WAVELENGTH_DEFAULT));
		
		zDistanceScripter = new NumberBoxScripter(zDistanceBox,
                getWSLPlayer(), null, "z", new Float(Z_DISTANCE_DEFAULT));
		
		apertureScripter = new NumberBoxScripter(diameterBox,
                getWSLPlayer(), null, "diameter", new Float(DIAMETER_DEFAULT));
		
		obstacleButtonScripter = new StateButtonScripter(obstacleButton, getWSLPlayer(), 
				null, "mode", new String[] {"Obstacle", "Aperture"}, 0);
		
		widgetScripter = new StateButtonScripter(hideWidgets, getWSLPlayer(), null, 
				"widgets", new String[] {"Hide Widgets", "Show Widgets"}, 0);
		
		resetScripter = new ButtonScripter(resetButton, getWSLPlayer(), null, "reset");
		
		//Adding Sub Panels to GUI
		controlPanel.add(topPanel);
		controlPanel.add(middlePanel);
		controlPanel.add(bottomPanel);
		
		//Set up the toolbar
		ToolBar toolBar = getToolBar();
		toolBar.addBrowserButton("Directions", "/org/webtop/html/circular/directions.html");
		toolBar.addBrowserButton("Theory","/org/webtop/html/circular/theory.html");
		toolBar.addBrowserButton("Examples","/org/webtop/html/circular/examples.html");
		toolBar.addBrowserButton("Exercises", "/org/webtop/html/circular/exercises.html");
		toolBar.addBrowserButton("Images","/org/webtop/html/circular/images.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
		
		//Call Update Screen to reflect the changes being made
		setDefaults();
	}
	
	/**
	 * Set up and connect to the x3dv file.  Connect
	 * to each used widget or component in the x3dv file.
	 */
	protected void setupX3D() {		
		/*set up the X3D Nodes from the .x3dv file associated with this module */
		waveLengthWidget = new WheelWidget(getSAI(),getSAI().getNode("WavelengthWidget"),(short)1,
				"Use this wheel to adjust the wavelength");
		waveLengthWidget.addListener(this);
		apertureWidget = new XDragWidget(getSAI(), getSAI().getNode("DiameterDragger"),(short)2, 
				"Use this red cone dragger to adjust the diameter of the Aperture/Obstacle");
		apertureWidget.addListener(this);
		zDistanceWidget = new XDragWidget(getSAI(), getSAI().getNode("ScreenDragger"),(short)3, 
				"Use this red cone dragger to adjust the z Distance");
		zDistanceWidget.addListener(this);
		widgetSwitch = new Switch(getSAI(), getSAI().getNode("WidgetsSwitch"),1);
		intensityLine = new LinePlot(new IndexedSet(getSAI(), getSAI().getNode("ilsNode")),SCREEN_SIZE/2,LINE_RES_HIGH,LINE_RES_LOW);
		
		//Set up the touch sensor for screen intensity readouts
		screenTouch = new TouchSensor(getSAI(), getSAI().getNode("ScreenTouch"),(short)4,
				null);
		screenTouch.setOver(true);
		screenTouch.addListener((SpatialWidget.Listener)this);
		
		/*Set up observatiion screen:
		for high resolution display surface--*/
		ifsHigh = new IFSScreen(new IndexedSet(getSAI(), getSAI().getNode("ifsNode")),new int [][] {{HIRES_SCREEN, HIRES_SCREEN}}, QUAD_WIDTH,QUAD_WIDTH);
		ifsHigh.setup();
		/*for low resolution display surface--*/
		ifsLow = new IFSScreen(new IndexedSet(getSAI(),getSAI().getNode("ifsNodeLowRes")),new int[][]{{LORES_SCREEN, LORES_SCREEN}}, QUAD_WIDTH, QUAD_WIDTH);
		ifsLow.setup();
		/*End Set up observation screen*/
		
		//Combine the hiRes and loRes screens and display both.  Use a switch to change between grids
		observationScreen = new MultiGrid(new Switch(getSAI(), getSAI().getNode("ResolutionSwitch"),2),
							new AbstractGrid[] {ifsHigh,ifsLow});
		
		apertureEngine = new ApertureEngine(getSAI(), radius);	
		
	}
	
	/**
	 * Sets default values used on initial startup.
	 */
	protected void setDefaults() {
		//Set the defaults shown on the Display Screen
		widgetSwitch.setChoice(0);
		obstacleButton.setState(0);
		waveLengthBox.setValue(WAVELENGTH_DEFAULT);
		diameterBox.setValue(FPRound.toSigVal(DIAMETER_DEFAULT,3));		
		zDistanceBox.setValue(Z_DISTANCE_DEFAULT);
		setMode(APERTURE_MODE);
		updateScreen();
	}
	
//**********************Event Handling Methods*************************\\
	//numberBoxListener() methods
	public void boundsForcedChange(NumberBox source, Number oldVal) {
		// TODO Auto-generated method stub

	}
	
	/**
	 *  Change the appropriate fields on the screen that
	 * correspond to the respective number boxes. 
	 * @param source - the corresponding number box that was changed
	 * @param newVal - the new value to be returned to the number box.
	 */
	public void numChanged(NumberBox source, Number newVal) {
		//If the diameter of the circular hole/obstacle changed then call evaluate on the 
		//ApertureEngine object to change the diameter on the screen
		if(source.equals(diameterBox))
		{
			//get the value stored in the diameterBox and store it in radius 
			//so that evaluate can recalculate the radius of the circle
			radius.setValue(diameterBox.getValue()/2);
			
			//Set the apertureEngine's internal radius variable.  This should be somehow automated for simplicity. [PC]
			//Changed type of radius to WTFLoat and passed to constructor of ApertureEngine.
			//ApertureEngine now performs all of the calculations needed for the aperture screen.
			updateScreen();
		}		
		else if(source.equals(waveLengthBox))
		{
			//get the value stored in waveLengthBox and store it in 
			//wavelength so that evaluate can recalculate the wavelentgh
			//above the wheel widget
			wavelength = waveLengthBox.getValue(); 
			updateScreen();			
		}
		else if(source.equals(zDistanceBox))
		{
			//get the value stored in the zDistanceBox and store it in
			//screenZ so that evaluate can color the screen according to
			//the distance it is away from the light
			screenZ = zDistanceBox.getValue();
			updateScreen();
		}

	}
	
	/**
	 * Exception Handling Method
	 * 
	 */
	public void invalidEntry(NumberBox source, Number badVal) {
		getStatusBar().setWarningText("Invalid Entry");
	}
	
	/**
	 * Handles the exception that is thrown
	 * when an invalid even occurs.
	 */
	public void invalidEvent(String node, String event) {
		// TODO Auto-generated method stub

	}
	
	//ActionPerformed Method
	/**
	 * Listens for changes being made in the GUI.
	 * @param e - ActionEvent that is the source of the action being performed.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==resetButton)
		{
			setDefaults();
		}
	}
	
	/**
	 * valueChanged - This method is called when the Touch Sensor is active and the hit point
	 * changed value changes values.  Sets the text of the status bar to correspond to the 
	 * intensity value of the point on the screen. 
	 */
	public void valueChanged(SpatialWidget src, float x, float y, float z) {
		float r = (float)Math.sqrt(x*x + z*z);
		
		//Sets the status Bar to 4 significant figures.  
		statusBar.setText("p"+"="+FPRound.toSigVal(r/1000,4)+
				"  intensity="+
				FPRound.toSigVal(getIntensity(r)/
												 (getMode()==OBSTACLE_MODE?
													OBSTACLE_SCALE:1),4));
		//statusBar.setText("x:" + x + " z:" +z);
		//System.out.println(r);
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
		
		//Call updateScreen() to reflect the changes made
		updateScreen();
	}
	//***********END EVENT HANDLING**************************\\


	
	/**
	 * Use to setup the Menu Bar.
	 */
	protected void setupMenubar() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * used to show tooltips for each method
	 */
	public void toolTip(Tooltip src, String tip){
	}
	
	//***********MATH AND OTHER CALCULATION METHODS HERE******************\\
	
	/**
	 * Used to return the Fresnel number specific to the current 
	 * module properties.  Performs necessary calculations to display
	 * the appropriate colors to either the high resolution or low
	 * resolution screen.  
	 * @param mode - corresponds to the type of aperture being shown.
	 * 				 Either Aperture or Obstacle mode. 
	 * @return NF - the appropriate Fresnel Number
	 */
	public float evaluate(int mode) {
		if(mode==Circular.APERTURE_MODE)
			maxIntensity=4;
		else if(mode==Circular.OBSTACLE_MODE)
			maxIntensity=6;

		// constants
		float
			nanoToMicro	 = 0.001f,
			milliToMicro = 1000;

		int xMeshPoints;
		int yMeshPoints;

		//Pointers to data arrays
		float[][] radialColors,colors;
		float[][] intensities;
		 
		int radialResolution;
		float maxRadius;

		// if active, use lower res
		if(draggingWidget()) {
			xMeshPoints = X_RES_LOW;
			yMeshPoints = Y_RES_LOW;
			radialColors=radialColorsLow;
			colors=colorsLow;
			intensities=intensitiesLow;
			radialResolution=radialResolutionLow;
			maxRadius=maxRadiusLow;
			lineIntensity = new float[LINE_RES_LOW];

		} else {
			xMeshPoints = X_RES_HIGH;
			yMeshPoints = Y_RES_HIGH;
			radialColors=radialColorsHigh;
			colors=colorsHigh;
			intensities=intensitiesHigh;
			radialResolution=radialResolutionHigh;
			maxRadius=maxRadiusHigh;
			lineIntensity = new float[LINE_RES_HIGH];
		}

		float l=wavelength*nanoToMicro,	//wavelength, um
					a=radius.getValue()*milliToMicro,		//radius, um
					xStep=(float)(xEnd-xStart)/xMeshPoints,
					yStep=(float)(yEnd-yStart)/yMeshPoints,
					hue=WTMath.hue(wavelength);

		// various goodies to speed up the calculations
		float x, y, intensity, _l;
		int ix, iy;

		final float z = screenZ * milliToMicro;
		final float NF = (float) Math.pow(a+.0001, 2) / (l * z);//HA HA...Dr. Foley and I are laughing 
		//like crazy right now...we got you NF
		final float u = 2 * (float) Math.PI * NF;

		// In order to exploit the radial symmetry, we will precompute the
		// intensity values along the x axis, and then traverse a quadrant of
		// the observation screen and lookup the intensity values by computing
		// the distance from the given point to the center (i.e., radius).

		/*System.out.println("radialResolution = " + radialResolution);
		System.out.println("maxRadius = " +maxRadius);
		System.out.println("Wavelength = " + l + " micrometers");
		System.out.println("Current z  = " + z + " micrometers");
		System.out.println("Radius     = " + a + " micrometers");
		System.out.println("NF         = " + NF);*/

		for(ix = 0; ix < radialResolution; ix++) {
			x=ix*xStep;
			_l = x / a;

			// determine the number of terms needed based on location on screen
			int expansion = SERIES_TERMS_L1;
			if((0 < _l && _l <= 0.8f) || (_l > 1.2f))
				expansion = SERIES_TERMS_L2;
			else if((0.8f < _l && _l < 1) || (1 < _l && _l <= 1.2f))
				expansion = SERIES_TERMS_L3;

			// compute the intensity
			//store intensity in 1D array and pass to line plot.  Use a switch to change
			//between lines.
			intensity= (getMode()==Circular.OBSTACLE_MODE?Circular.OBSTACLE_SCALE:1)*
											     FresnelMath.computeCircleIntensity(_l, u, expansion, getMode());
			
			lineIntensity[ix]=intensity;
			// normalize/bound the intensity
			intensity = WTMath.bound((intensity - minIntensity) / (maxIntensity - minIntensity),0,1);
			//lineIntensity[ix]=intensity;
			
			float[] target=radialColors[ix];
			
			WTMath.hls2rgb(target,hue,intensity,1);
		}

		for(ix = 0; ix < xMeshPoints; ix++) {
			x=ix*xStep;
			for(iy = 0; iy < yMeshPoints; iy++) {
				y=iy*yStep;

				// compute the radius
				float ro = (float) Math.sqrt(x*x+y*y);

				// compute the real index in our lookup array
				float findex = (ro / maxRadius) * radialResolution;
				int iindex = (int) findex;

				//System.out.println("At (x="+x+",y="+y+"): findex="+findex);
				float[] curcolor=colors[ix*yMeshPoints+iy];
				if(findex==iindex) {
					float[] lookup=radialColors[iindex];
					curcolor[0]=lookup[0];
					curcolor[1]=lookup[1];
					curcolor[2]=lookup[2];
				} else {
					//Linearly interpolate between colors in array
					int ciel=iindex+1;
					float[] c1=radialColors[iindex],c2=radialColors[ciel];
					curcolor[0]=c1[0]*(ciel-findex)+c2[0]*(findex-iindex);
					curcolor[1]=c1[1]*(ciel-findex)+c2[1]*(findex-iindex);
					curcolor[2]=c1[2]*(ciel-findex)+c2[2]*(findex-iindex);
				}
			}
		}
		
		//set the intensity of the line on top and set the colors of the screen
		intensityLine.setValues(lineIntensity);
		
		//testing draw point in middle
		//colors[0][0]=0;
		//colors[0][1]=0;
		//colors[0][2]=255;
		
		observationScreen.setColors(colors);
		//Return the Fresnel Number
		return NF;
	}
	
//	This is used to display screen intensities
	/* Added this method to return the intensity in order to set the status bar's
	 * text properly.
	 */
	
	/**
	 * getIntensity(floar r) - returns intensity calculation needed to set the status bar
	 * @param r - float ro value calculated in setValue()
	 * @return val - the float value of the intensity at a specific point on the screen
	 */
	public float getIntensity(float r) {
		//Pick arrays and parameters based on resolution:
		float[][] radialColors,colors;
		float[][] intensities;
		int radialResolution;
		int xMeshPoints, yMeshPoints;
		float maxRadius;

		if(draggingWidget()) {
			xMeshPoints = X_RES_LOW;
			yMeshPoints = Y_RES_LOW;
			intensities=intensitiesLow;
			radialResolution=radialResolutionLow;
			maxRadius=maxRadiusLow;
		} else {
			xMeshPoints = X_RES_HIGH;
			yMeshPoints = Y_RES_HIGH;
			intensities=intensitiesHigh;
			radialResolution=radialResolutionHigh;
			maxRadius=maxRadiusHigh;
		}

		// compute the real index in our lookup array
	
		float findex = (r / maxRadius) * radialResolution;
		//System.out.println("findex:" + findex);
		//System.out.println("maxRadius: " + maxRadius);
		
		float offset = findex - (int)findex;
		float val = lineIntensity[(int)findex]+offset*(lineIntensity[(int)findex+1]-lineIntensity[(int)findex]);
		return val;

		
		
	}
	
	/**
	 * Returns the current mode. Either Aperture or Obstacle
	 * @return mode - Aperture(0)/Obstacle(1).
	 */
	public int getMode(){
		return mode;
	}
	
	/**
	 * Sets the current mode.  Either Aperture or Obstacle.
	 * @param m - integer to determine which mode is to be selected.
	 */
	public void setMode(int m){
		mode = m;
		apertureEngine.evaluate(m);
	}
	
	
	/**
	 * Reflects the changes made to the screen by either a widget being 
	 * dragged, or by a number box change.  The appropriate grid (hires/lores)
	 * should be selected before calling this method.
	 */
	protected void updateScreen()
	{
		/*draggingWidget() returns true if a widget is being dragged,
		 * false otherwise; drag will be used here to:
		 * 1) determine if a widget is being dragged
		 * 2) set the colors in the LORES_SCREEN if a widget is being dragged,
		 * 	  else set the colors in the HIRES_SCREEN if a widget is not being dragged*/
		boolean drag = draggingWidget();
		
		/*Determine which Resolution screen to display
		 *based on if a widget is being dragged or not*/
		int RES = drag?LORES:HIRES;	
		
		/*Determine which grid to select based on 
		 *if a widget is being dragged or not */
		int GRID = drag?LOGRID:HIGRID;
		
		//Print RES, drag, and GRID for debugging purposes
		//System.out.println("updateScreen(): drag= " + drag + ", RES = "+ RES+ ", GRID = "+ GRID);
		apertureEngine.evaluate(mode);
		observationScreen.setup();
		
		setNF(evaluate(mode));		
	}
	
	/**
	 * Sets the precision of the JLabel for the Fresnel Number
	 * @param fn - mode variable
	 */
	public void setNF(float fn)
	{
		//fresnelDisplay.setText(Float.toString(FPRound.toSigVal(fn,3)));
		if(fn < (float) 0.01)	
			//fresnelDisplay.setText(Float.toString(FPRound.toSigVal(fn,2)));
			fresnelDisplay.setText(FPRound.showZeros(fn, 2));
		else if(fn < 10)
			fresnelDisplay.setText(FPRound.showZeros(fn, 2));
		else if(fn > 10)
			fresnelDisplay.setText(FPRound.showZeros(fn, 1));
	}
	
	/**
	 * Sets the precision of the floatBox diameterBox.
	 * @param diam - The number to be stored in the box.
	 */
	public void setDiameter(float diam)
	{
		diameterBox.setSigValue(diam, 5);
	}
	

	
	//***********************WSL ROUTINES********************************//
	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
		waveLengthScripter.addTo(node);
		zDistanceScripter.addTo(node);
		apertureScripter.addTo(node);
		obstacleButtonScripter.addTo(node);
		widgetScripter.addTo(node);
		
	}

	public String getWSLModuleName() {
		
		return new String("circular");
	}
	
	
	//***********************END WSL ROUTINES***************************//

	/**
	 * MAIN hopefully no explanation necessary for this one
	 * */
	public static void main(String[] args) {
		Circular circular = new Circular("Circular", "/org/webtop/x3dscene/circular.x3dv");
	}

}
