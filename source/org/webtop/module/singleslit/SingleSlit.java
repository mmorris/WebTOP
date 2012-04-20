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
 * <p>Title: Fresnel Single Slit</p>
 * 
 * <p>Description: The X3D version of The Optics Project for the web (WebTOP)</p>
 * 
 * <p>Company:MSU Department of Physics and Astronomy</p>
 * 
 * @author Grant
 * @version 0.0 
 */
package org.webtop.module.singleslit;



import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.sdl.gui.numberbox.FloatBox;
import org.sdl.gui.numberbox.NumberBox;
import org.sdl.gui.numberbox.NumberBox.Listener;
import org.sdl.math.FPRound;
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
import org.webtop.x3d.widget.XDragWidget;
import org.webtop.component.Separator;
import org.webtop.module.singleslit.FresnelMath;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SingleSlit extends WApplication implements NumberBox.Listener,
		org.webtop.x3d.widget.SpatialWidget.Listener, WSLScriptListener,
		ActionListener {

	public SingleSlit(String title, String world) {
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
		return "Fresnel Single Slit";
	}

	
	protected int getRevision() {
		// TODO Auto-generated method stub
		return 1;
	}

	
/* CONSTANST TO BE USED WITH THIS MODULE */

public static final float		WAVELENGTH_MIN = 400f, 
								WAVELENGTH_MAX = 700f, 
								WAVELENGTH_DEFAULT = 550f,
								WIDTH_MIN = 0.0001f,
								WIDTH_MAX = 1f,
								WIDTH_DEFAULT = 0.4f, //same as sqrt(2)/2
								Z_DISTANCE_MIN = 5.15f,
								Z_DISTANCE_MAX = 100f, 
								Z_DISTANCE_DEFAULT = 50f,
								Z_SCALE 			= 20f,
								NM_TO_MM		  = 0.000001f,//converts wavelength wheel's nm units to mm for math	
								
								MILI_TO_MICRO	= 1000.0f, //converts Millimeters to Micrometers
								SCREEN_SCALE	 =	 0.65f,				// scales the screen intensity
								PLOT_SCALE		 =	8.0f,				// scales the 2D plot    //-GRANT was 200.0f
								M_MIN_INTENSITY =	-0.1f,			//Should be betwen 2 and 0, but that is not case becasue of numerical errors
								M_MAX_INTENSITY =	 3.0f;  		//THIS WAS 2
private static final int		QUAD_WIDTH 		   = 500, //width of 1 x3d quadrant(x3d units)of the observation screen
								HIRES_SCREEN	   = 150, //High Resolution Screen WAS 100
								HIRES 			  = 100,
								HIGRID			  = HIRES*HIRES; //Used in updateScreen() to determine GRID
/*
 * END GENERAL WAPPLICATION METHODS*****************************
 */
	
	/* NODE DECLARATIONS FOR X3DV FILE*/
	//Widgets
	XDragWidget zDistanceWidget;
	XDragWidget widthWidget;
	WheelWidget waveLengthWidget;
	
	//Screens-HiRes and LoRes
	IFSScreen ifsHigh;
	//MultiGrid observationScreen; 
	AbstractGrid observationScreen;
	
	//Switches
	Switch widgetSwitch;
	LinePlot intensityLine;
	
	//Touch Sensor that allows screen intensity to be read
	TouchSensor screenTouch;
	
	/*GUI Elements*/
	FloatBox waveLengthBox;
	FloatBox widthBox;
	FloatBox zDistanceBox;
	//JLabel to hold Fresnel Number
	JLabel fresnelPanel;
	JLabel fresnelDisplay;
	public JMenuBar menu; //for the menubar at the top of the module
	//Buttons on the Display Panel
	JButton resetButton;
	ToggleButton hideWidgets;
	
	/*Couplers*/
	ScalarCoupler waveLengthCoupler;
	ScalarCoupler zDistanceCoupler;
	ScalarCoupler widthCoupler;
	
	//Scripters
	private NumberBoxScripter waveLengthScripter, zDistanceScripter, widthScripter;
	private ButtonScripter resetScripter;
	private StateButtonScripter widgetScripter;
	
	/* USED TO EVAULUATE */
	//Used for the LinePlot
	private static final int LINE_RES_HIGH = 300; //150 
	private static final int LINE_RES_LOW = 75;
	private static final float SCREEN_SIZE =1000f;
	
	
	
	protected void setDefaults() {
		//Set the defaults shown on the Display Screen
		hideWidgets.setState(0);
		waveLengthBox.setValue(WAVELENGTH_DEFAULT);
		widthBox.setValue(FPRound.toSigVal(WIDTH_DEFAULT,4));		
		zDistanceBox.setValue(Z_DISTANCE_DEFAULT);
		
		updateScreen();

	}


	
	
	
	protected void setupGUI() {		
		//Set the layout for the control panel of the module
		controlPanel.setLayout(new GridLayout(2,1));
		
		//Grouping Panels
		JPanel topPanel = new JPanel();
		JPanel middlePanel = new JPanel();
		//set up the float boxes for the x3dv file
		waveLengthBox = new FloatBox(WAVELENGTH_MIN, WAVELENGTH_MAX, WAVELENGTH_DEFAULT,5);
		waveLengthBox.addNumberListener(this);
		widthBox = new FloatBox(WIDTH_MIN, WIDTH_MAX, WIDTH_DEFAULT, 5);
		widthBox.addNumberListener(this);
		zDistanceBox = new FloatBox(Z_DISTANCE_MIN, Z_DISTANCE_MAX, Z_DISTANCE_DEFAULT, 5);
		zDistanceBox.addNumberListener(this);
		
		//Add components to topPanel
		topPanel.add(new JLabel("Wavelength:"));
		topPanel.add(waveLengthBox);
		topPanel.add(new JLabel("nm"));
		topPanel.add(new JLabel("      "));
		
		topPanel.add(new JLabel("Width:"));
		topPanel.add(widthBox);
		topPanel.add(new JLabel("nm"));
		topPanel.add(new JLabel("      "));
		
		topPanel.add(new JLabel("z:"));
		topPanel.add(zDistanceBox);
		topPanel.add(new JLabel("mm"));
		topPanel.add(new JLabel("      "));
		
		fresnelPanel = new JLabel("Fresnel Number: "); //fresnel number must be calculated somewhere
		topPanel.add(fresnelPanel);
		fresnelDisplay = new JLabel("");
		topPanel.add(fresnelDisplay);
		
		//Add components to the middlePanel
		
		JLabel empty2 = new JLabel("                                                   ");
		middlePanel.add(empty2);
		middlePanel.add(empty2);
		middlePanel.add(empty2); 
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		//add the reset button to the JPanel
		middlePanel.add(resetButton);
		middlePanel.add(new JLabel("   "));
		
		hideWidgets = new ToggleButton("Hide Widgets","Show Widgets", false);
		/*Use this on the fly method to hide the widgets on the X3DV 
		 * screen. Uses a boolean variable to select between states.*/
		hideWidgets.addListener(new StateButton.Listener()
		{
			public void stateChanged(StateButton sb, int state)
			{
				//Switch between visible and hidden on the screen based on the state of the ToggleButton
				widgetSwitch.setVisible(hideWidgets.getStateBool());
			}
		});
		
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
		
		widthCoupler = new ScalarCoupler(widthWidget,widthBox,4,
					  new ScalarCoupler.Converter(Lambda.linear(1,0),Lambda.linear(1,0)));
		
		
		//Initialize Scripters 
		waveLengthScripter = new NumberBoxScripter(waveLengthBox,
		    getWSLPlayer(), null, "wavelength", new Float(WAVELENGTH_DEFAULT));
		
		zDistanceScripter = new NumberBoxScripter(zDistanceBox,
		    getWSLPlayer(), null, "z", new Float(Z_DISTANCE_DEFAULT));
		
		widthScripter = new NumberBoxScripter(widthBox,
		    getWSLPlayer(), null, "slitWidth", new Float(WIDTH_DEFAULT));
		
		widgetScripter = new StateButtonScripter(hideWidgets, getWSLPlayer(), null, 
			"hideWidgets", new String[] {"Hide Widgets", "Show Widgets"}, 0);
		
		resetScripter = new ButtonScripter(resetButton, getWSLPlayer(), null, "reset");
		
		
		//Adding Sub Panels to GUI
		controlPanel.add(topPanel);
		controlPanel.add(middlePanel);

		
		//Set up the toolbar
		ToolBar toolBar = getToolBar();
		toolBar.addBrowserButton("Directions", "/org/webtop/html/singleslit/directions.html");
		toolBar.addBrowserButton("Theory","/org/webtop/html/singleslit/theory.html");
		toolBar.addBrowserButton("Examples","/org/webtop/html/singleslit/examples.html");
		toolBar.addBrowserButton("Exercises", "/org/webtop/html/singleslit/exercises.html");
		toolBar.addBrowserButton("Images","/org/webtop/html/singleslit/topimages.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
		
		
		//Call Update Screen to reflect the changes being made
		setDefaults();
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
		
		widthWidget = new XDragWidget(getSAI(), getSAI().getNode("rightWidthDragger"),(short)2, 
				"Use this red cone dragger to adjust the width of the slit"); 
		widthWidget.addListener(this);
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
		
		
		
		//Set up observation screen:
		//for high resolution display surface
		//There is no low resolution display for single slit 
		
		ifsHigh = new IFSScreen(new IndexedSet(getSAI(), getSAI().getNode("ifsNode")),new int [][] {{HIRES_SCREEN, HIRES_SCREEN}}, QUAD_WIDTH,QUAD_WIDTH);
		ifsHigh.setup();
		
		
		/*End Set up observation screen*/
		}

	
	//***********************WSL ROUTINES********************************//
	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
		waveLengthScripter.addTo(node);
		zDistanceScripter.addTo(node);
		widthScripter.addTo(node);
		widgetScripter.addTo(node);
		
	}

	public String getWSLModuleName() {	
		return new String("singleslit");
	}
	
	
	//***********************END WSL ROUTINES***************************//

	
	public void boundsForcedChange(NumberBox source, Number oldVal) {
		// TODO Auto-generated method stub

	}
	
	
	public void invalidEntry(NumberBox source, Number badVal) {
		if(source.equals(waveLengthBox))
			getStatusBar().setWarningText("Invalid Entry: Wave Length Must be Between "+ WAVELENGTH_MIN + " and " + WAVELENGTH_MAX);
		if(source.equals(zDistanceBox))
			getStatusBar().setWarningText("Invalid Entry: Z Distance Must be Between " + Z_DISTANCE_MIN + " and " + Z_DISTANCE_MAX);
		if(source.equals(widthBox))
			getStatusBar().setWarningText("Invalid Entry: Diamaeter Must be Between " + WIDTH_MIN + " and " + WIDTH_MAX);	
	}

	
	public void numChanged(NumberBox source, Number newVal) {
		updateScreen();
	}

	//Calculates Intensity for use in the status bar
	float getIntensity(float x)
	{
		float l,w;
		l = waveLengthBox.getValue() * NM_TO_MM;
		w = widthBox.getValue()	* MILI_TO_MICRO;
		
		float
		z	= zDistanceBox.getValue() * MILI_TO_MICRO,
		sub1 = 2.0f / (l * (z) * 1000),
		sub2 = w / 2.0f,
		w1, w2, intensity, value;
		
		//////////////////////////////////////////////////////////////////
		// compute the intensity
		w1 = (float) -Math.sqrt(sub1) * (sub2 + x);
		w2 = (float)	Math.sqrt(sub1) * (sub2 - x);
		
		return FresnelMath.computeSlitIntensity(w1, w2);
	}
	
	
	
	public void valueChanged(SpatialWidget src, float x, float y, float z) {
		//Sets the status Bar to 4 significant figures.  
		statusBar.setText("X"+"="+FPRound.toSigVal(x/1000,4) + "nm"
				+ "  intensity="+
				FPRound.toSigVal(getIntensity(x),4));
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
	//observationScreen.setup();	
	ifsHigh.setup();
	setNF();
	evaluate();
	}
	
	public void setNF(float fn)
	{
		if(fn < (float) 0.01)	
			fresnelDisplay.setText(FPRound.showZeros(fn, 2));
		else if(fn < 10)
			fresnelDisplay.setText(FPRound.showZeros(fn, 2));
		else if(fn > 10)
			fresnelDisplay.setText(FPRound.showZeros(fn, 1));
	}
	
	//Doesn't depend on evaluate function
	public void setNF()
	{
		float
		l = waveLengthBox.getValue() * NM_TO_MM,
		w = widthBox.getValue()	* MILI_TO_MICRO,
		z = zDistanceBox.getValue() * MILI_TO_MICRO; 
		// Compute the Fresnel number
		float fn = (w * w) / (4.0f * l * z * 1000.0f);
		if(fn < (float) 0.01)	
			fresnelDisplay.setText(FPRound.showZeros(fn, 2));
		else if(fn < 10)
			fresnelDisplay.setText(FPRound.showZeros(fn, 2));
		else if(fn > 10)
			fresnelDisplay.setText(FPRound.showZeros(fn, 1));
	}
	
	
	

	
	
	//Returns Fresnel Number
	float evaluate() {
		////////////////////////////////////////////////////////////////////
		// if any of the widgets are active, use lower resolution
		int xResolution = LINE_RES_HIGH + 1; //was 1501.. I had it at 151?

		float
			rgb[] = new float[3],					// rgb of a point on the observation screen
			l,														// wavelength
			w,														// width of the slit
			xStep;												// step between two grid point along x

		int xEnd=-512,
			xStart=512;
		
		final float hue=WTMath.hue(waveLengthBox.getValue());

		////////////////////////////////////////////////////////////////////
		// change of units to micrometers
		l = waveLengthBox.getValue() * NM_TO_MM;
		w = widthBox.getValue()	* MILI_TO_MICRO;

		////////////////////////////////////////////////////////////////////
		// compute the step between two grid point along the x axis
		xStep			 = (xEnd - xStart) / xResolution; 
			
		////////////////////////////////////////////////////////////////////
		// various goodies to speed up the calculations
		float
			x,
			z		 = zDistanceBox.getValue() * MILI_TO_MICRO,
			sub1 = 2.0f / (l * (z) * 1000),
			sub2 = w / 2.0f,
			w1, w2, intensity, value;

		////////////////////////////////////////////////////////////////////
		// compute the number of points along the positive and negative
		// axis (half of total) and the number of points & colors that need to
		// be allocated for the screen (2 * xResolution; one for the top, one
		// for the bottom)
		int
			xResolutionHalf = (xResolution + 1) / 2,
			numPoints	=	 HIGRID, 
			pointIndex;

		////////////////////////////////////////////////////////////////////
		// allocate memory for the screen colors and for the 2D plot
		// points (the color of the 2D plot is always the same and is
		// determined in the VRML world file; the screen points were
		// calculated by makeCoords() at initialization time)
		//float [][]screen_color = new float[numPoints][3];
		float [][]screen_color = new float[HIRES_SCREEN*HIRES_SCREEN][3];		
		float []plot_point =new float[LINE_RES_HIGH];			
		
		//////////////////////////////////////////////////////////////////
		// loop variables
		int ix;

		x = (xResolutionHalf - 1) * xStep;
		for(ix = 0; ix < xResolutionHalf; ix++) {
			
			//////////////////////////////////////////////////////////////////
			// compute the intensity
			w1 = (float) -Math.sqrt(sub1) * (sub2 + x);
			w2 = (float)	Math.sqrt(sub1) * (sub2 - x);
			intensity = FresnelMath.computeSlitIntensity(w1, w2);
			
			//////////////////////////////////////////////////////////////////
			// normalize the intensity
			value = WTMath.bound((intensity - M_MIN_INTENSITY) / (M_MAX_INTENSITY - M_MIN_INTENSITY),0,1);
			
		
			//////////////////////////////////////////////////////////////////
			// compose an HLS representation of the color on the screen and
			// convert it to RGB
			WTMath.hls2rgb(rgb, hue, value, WTMath.SATURATION);

			//////////////////////////////////////////////////////////////////
			// Observation screen
			// ------------------
			// compose the coordinates and assign the RGB values			
			//Paints each strip at a time (need to make this (the 150) a constant somewhere probably --Grant)
			for(int column=0;column<150&&ix!=150;column++)
			{
				screen_color[ix*150+column][0]=rgb[0];
				screen_color[ix*150+column][1]=rgb[1];
				screen_color[ix*150+column][2]=rgb[2];
			}
			//
			//////////////////////////////////////////////////////////////////

			//////////////////////////////////////////////////////////////////
			// 2D Plot
			// -------
			// assign the coordinates to the 2D plot based on the intensities
			//
			// Assigns to left and right sides at same time
		
			plot_point[ix]=plot_point[xResolution-ix-2]=value * PLOT_SCALE; 
		
			// move to the next sample along the x axis
			x -= xStep;
		}

		////////////////////////////////////////////////////////////////////
		// send the coordinates, colors, and indices to the VRML browser
		//
		// Send plots:
		
		intensityLine.setValues(plot_point);		
		ifsHigh.setColors(screen_color);
		
		
		//Update the indicies and verticies only if resolution swapped on us
		/*
		This could be used if the module was using a highand low res screen
		if(interacting^wasinteractive) {
			m_screen_set_point.setValue(interacting?screen_lowpoint:screen_hipoint);
			m_screen_set_coordIndex.setValue(interacting?screen_lowindex:screen_hiindex);
			m_screen_set_colorIndex.setValue(interacting?screen_lowindex:screen_hiindex);
			m_plot_set_coordIndex.setValue(interacting?plot_lowindex:plot_hiindex);
		}
		*/
		//
		////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////
		// Compute the Fresnel number
		float NF = (w * w) / (4.0f * l * z * 1000.0f);

		// make note of which mesh used for next time /////////
		//wasinteractive=interacting;
		
		return NF;
	}
	
	

	
	
	public static void main(String[] args) {
		SingleSlit singleslit=new SingleSlit("Single Slit", "/org/webtop/x3dscene/singleslit.x3dv");
		
	}
}
	

