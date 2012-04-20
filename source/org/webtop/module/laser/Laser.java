/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.laser;

/**
 * @author Sara Smolensky (original)
 * @author Grant Patten (X3D/WApplication)
 */


//Grant -- TODO
/*
 * Incrase Size of Intensity Scroller?
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
import org.webtop.component.Separator;
import org.webtop.component.ToolBar;
import org.webtop.component.WApplication;
import org.webtop.module.nslit.NSlit;
import org.webtop.util.*;
import org.webtop.util.script.ButtonScripter;
import org.webtop.util.script.ChoiceScripter;
import org.webtop.util.script.NumberBoxScripter;
import org.webtop.util.script.ScrollbarScripter;
import org.webtop.util.script.StateButtonScripter;
import org.webtop.util.script.ChoiceScripter;
import org.webtop.wsl.client.WSLPlayer;
import org.webtop.wsl.event.WSLScriptEvent;
import org.webtop.wsl.event.WSLScriptListener;
import org.webtop.wsl.script.WSLNode;
import org.webtop.x3d.output.Switch;
import org.webtop.x3d.widget.*;
import org.webtop.component.StatusBar;


public class Laser extends WApplication implements NumberBox.Listener,
org.webtop.x3d.widget.SpatialWidget.Listener, WSLScriptListener, ItemListener, AdjustmentListener {

	//MISC Module Info
	protected String getAuthor() {
		return "Grant Patten";
	}

	
	protected String getDate() {
		return null;
	}

	
	protected Component getFirstFocus() {
		return wavelengthField;
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
		return "Laser";
	}
	
	public StatusBar getStatusBar() {
		return statusBar;
	}

	//CONSTRUCTOR
	public Laser(String title, String world) {
		super(title, world,true,false);
	}
	

	////*******MODULE SPECIFIC CODE*******////
	
	//*** Constants ***//
	final static float 	MIN_SPACE		= 200;		//minimum space between end of laser and screen
	//Float Boxes
	final static float 	WAVELENGTH_MIN	= 400f,
						WAVELENGTH_MAX	= 700f,
						WAVELENGTH_DEF	= 550f,
						
						MIRROR1RAD_MIN	= Float.NEGATIVE_INFINITY,
						MIRROR1RAD_MAX	= Float.POSITIVE_INFINITY,
						MIRROR1RAD_DEF	= 1000f,
						
						MIRROR2RAD_MIN	= Float.NEGATIVE_INFINITY,
						MIRROR2RAD_MAX	= Float.POSITIVE_INFINITY,
						MIRROR2RAD_DEF	= 1000f,

						CAVITYLEN_MIN	= 250f,
						CAVITYLEN_MAX	= 1000f,
						CAVITYLEN_DEF	= 500f,

						SCREENDIST_MIN	= CAVITYLEN_DEF + MIN_SPACE,
						SCREENDIST_MAX	= 2000f,
						SCREENDIST_DEF	= 2000f,
						SCREENDIST_SCALE= 100f;
	
	final static int	INTENSITY_MIN	= 0,
						INTENSITY_MAX	= 500,
						INTENSITY_DEF	= 125, 		//INTENSITY_MAX/4.0f
	
						MODE_DEF		= 0;
	//*** X3D Controls ***//
	// Screen, line, aperture, and custom controls
	Engine engine;						// Ties all of the elements of Lasers together
	
	//Widgets & Couplers
	WheelWidget  	wavelengthWidget;	// Changes the wavelength of the laser
	XDragWidget   	screenDistWidget;  	// Moves the screen
	XDragWidget 	cavityLenWidget;   	// Changes the Length of the laser Cavity
	
	ScalarCoupler 	wavelengthCoupler,	// Connects the Widgets and Fields
					screenDistCoupler, 
					cavityLenCoupler;
	
	Switch			widgetsSwitch;      // Switch widgets on/off
	
	//Scripters
	private NumberBoxScripter 	cavityLenScripter, screenDistanceScripter, 
								mirrorRadius1Scripter, mirrorRadius2Scripter,
								waveLengthScripter;
	private ButtonScripter		resetScripter,resetIntensityScripter;
	private StateButtonScripter widgetScripter;
	private ScrollbarScripter	intensityScrollbarScripter;
	private ChoiceScripter		modeChoiceScripter;

	
	//WSL player
	private WSLPlayer wslPlayer;
	
	
	//GUI Elements
	FloatBox		wavelengthField,	// wavelength of the light
					mirror1radField,	// Radius of 1st mirror
					mirror2radField,	// Radius of 2nd mirror
					cavityLenField,		// length of Cavity
					screenDistField;	// distance from left mirror to screen

	JButton 		resetIntensity,
					reset;
	
	ToggleButton 	hideWidgets;
	
	JComboBox		modeChoice;
	
	JScrollBar 		intensityScrollbar;
	
	JLabel			w0Label,
					zRLabel,
					zLabel,
					wLabel,
					ILabel,
					L1Label;

	
	//*** Setup Methods ***//
	
	//Set up X3D connections
	protected void setupX3D() {
		//Sets up Hide Widgets Switch                                                                     
		widgetsSwitch = new Switch(getSAI(), getSAI().getNode("WidgetsHolder"), 1);
		
		
		//Set up Widgets
		screenDistWidget = new XDragWidget(getSAI(), getSAI().getNode("ScreenWidget"), 		(short)1, "Screen Distance Widget"); 
		cavityLenWidget  = new XDragWidget(getSAI(), getSAI().getNode("LengthWidget"),  	(short)2, "Cavity Length Widget"); 
		wavelengthWidget = new WheelWidget(getSAI(), getSAI().getNode("WavelengthWidget"), 	(short)3, "Wavelength Widget");
			
		/***The Engine Class Handles Most of the X3D SetUp***/
	}
	
	
	//Set up GUI
	protected void setupGUI() {
		//Set up Output labels
		w0Label	= new JLabel();
		zRLabel	= new JLabel();
		zLabel	= new JLabel();
		wLabel	= new JLabel();
		ILabel	= new JLabel(); // on screen on axis
		L1Label	= new JLabel();
		
		//Set up NumberBoxes
		wavelengthField = new FloatBox(WAVELENGTH_MIN,WAVELENGTH_MAX,WAVELENGTH_DEF,5);
		mirror1radField = new FloatBox(MIRROR1RAD_MIN,MIRROR1RAD_MAX,MIRROR1RAD_DEF,5);
		mirror2radField = new FloatBox(MIRROR2RAD_MIN,MIRROR2RAD_MAX,MIRROR2RAD_DEF,5);
		cavityLenField  = new FloatBox(CAVITYLEN_MIN,CAVITYLEN_MAX,CAVITYLEN_DEF,5);
		screenDistField = new FloatBox(SCREENDIST_MIN,SCREENDIST_MAX,SCREENDIST_DEF,5);
		
		//Set up NumberListeners
		wavelengthField.addNumberListener(this);
		mirror1radField.addNumberListener(this);
		mirror2radField.addNumberListener(this);
		cavityLenField.addNumberListener(this);
		screenDistField.addNumberListener(this);
		
		//Set Up ModeChoice
		modeChoice = new JComboBox();
		modeChoice.addItem("Gaussian Beam -- TEM 00");
		modeChoice.addItem("Vertical Wire -- TEM 10");
		modeChoice.addItem("Horizontal Wire -- TEM 01");
		modeChoice.addItem("Both Wires -- TEM 11");
		modeChoice.addItemListener(this);
		
		
		//Set up Intensity Bar
		intensityScrollbar=new JScrollBar(JScrollBar.HORIZONTAL,INTENSITY_DEF,1,INTENSITY_MIN,INTENSITY_MAX+1);
		//TODO -- FIX THE SIZE OF INTENSITY SCROLL BAR
		intensityScrollbar.addAdjustmentListener(this); 
          
        
		
		//Set up Buttons
		reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDefaults();
			}
		});
		
		resetIntensity=new JButton("Reset Intensity");
		resetIntensity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				intensityScrollbar.setValue(INTENSITY_DEF);
				engine.getBeam().setIntensity(INTENSITY_DEF);
			}
		});
		
		hideWidgets = new ToggleButton("Hide Widgets", "Show Widgets", false);
        hideWidgets.addListener(new StateButton.Listener() {
            public void stateChanged(StateButton sb, int state) {
            	widgetsSwitch.setVisible(hideWidgets.getStateBool());
            }
        });
		
		///////////////////
		//Set up Couplers//
		///////////////////
		cavityLenCoupler	= new ScalarCoupler(cavityLenWidget,  cavityLenField,  3, new ScalarCoupler.Converter(Lambda.linear(.01, 0),Lambda.linear(100, 0)));
		screenDistCoupler	= new ScalarCoupler(screenDistWidget, screenDistField, 3, new ScalarCoupler.Converter(Lambda.linear(.01, 0),Lambda.linear(100, 0)));
		wavelengthCoupler	= new ScalarCoupler(wavelengthWidget, wavelengthField, 3);
		
        //Initialize Scripters 
		cavityLenScripter 	= new NumberBoxScripter(cavityLenField,
    		    getWSLPlayer(), null, "length", new Float(CAVITYLEN_DEF));
		screenDistanceScripter	= new NumberBoxScripter(screenDistField,
    		    getWSLPlayer(), null, "screenDist", new Float(SCREENDIST_DEF));
		mirrorRadius1Scripter	= new NumberBoxScripter(mirror1radField,
    		    getWSLPlayer(), null, "radius1", new Float(MIRROR1RAD_DEF));
		mirrorRadius2Scripter	= new NumberBoxScripter(mirror2radField,
    		    getWSLPlayer(), null, "radius2", new Float(MIRROR2RAD_DEF));
		waveLengthScripter		= new NumberBoxScripter(wavelengthField,
    		    getWSLPlayer(), null, "wavelength", new Float(WAVELENGTH_DEF));

		widgetScripter = new StateButtonScripter(hideWidgets, getWSLPlayer(), null, 
    			"hideWidgets", new String[] {"Hide Widgets", "Show Widgets"}, 0);
    		
    	resetScripter = new ButtonScripter(reset, getWSLPlayer(), null, "reset");
		
    	resetIntensityScripter = new ButtonScripter(resetIntensity,getWSLPlayer(),null,"resetIntensity");
    	
    	intensityScrollbarScripter = new ScrollbarScripter(intensityScrollbar, getWSLPlayer(), null, "intensity", 
        		INTENSITY_DEF, null);
    	
    	modeChoiceScripter = new ChoiceScripter(modeChoice,getWSLPlayer(), null, "aperture", 
        		new String[] {"Gaussian Beam -- TEM 00", "Vertical Wire -- TEM 10", "Horizontal Wire -- TEM 01", "Both Wires -- TEM 11"}, MODE_DEF, this);
        

    	
		/* GUI Layout:
         *  |---------------------|
         *  | FLOAT BOX PANEL	  |
         *  |---------------------|
         *  | BUTTON PANEL		  |
         *  |---------------------|
         *  | INTENSITY PANEL	  |
         *  |---------------------|
         *  | LABEL PANEL		  |
         *  |---------------------|
         */        
    	//Sets up Panels
        controlPanel.setLayout(new GridLayout(4,1));
        
        JPanel 	boxPanel		= new JPanel(),
				buttonPanel 	= new JPanel(),
				intensityPanel	= new JPanel(),
				labelPanel		= new JPanel(new GridLayout(1,6));
        

		//Float Box Panel
		boxPanel.add(new JLabel("Wavelength:", JLabel.RIGHT));
		boxPanel.add(wavelengthField);

		boxPanel.add(new JLabel("Radius 1:", JLabel.RIGHT));
		boxPanel.add(mirror1radField);
		
		boxPanel.add(new JLabel("Radius 2:", JLabel.RIGHT));
		boxPanel.add(mirror2radField);
		
		boxPanel.add(new JLabel("Length:", JLabel.RIGHT));
		boxPanel.add(cavityLenField);
			
		boxPanel.add(new JLabel("Screen:", JLabel.RIGHT));
		boxPanel.add(screenDistField);
		
		//Button Panel
		buttonPanel.add(reset);//.setBounds(20,5,80,20);
		buttonPanel.add(hideWidgets);//.setBounds(110,5,80,20);
		buttonPanel.add(modeChoice);//.setBounds(200,5,150,14);
		
		
		//Intensity Panel
		intensityPanel.add(resetIntensity);//.setBounds(563,6,40,18);
		intensityPanel.add(new JLabel("Beam Brightness:",JLabel.RIGHT));//.setBounds(365,8,90,14);
		intensityPanel.add(intensityScrollbar);//.setBounds(458,8,100,15);
		
		
		
		//Label Panel
		labelPanel.add(w0Label);
		labelPanel.add(zRLabel);
		labelPanel.add(L1Label);
		labelPanel.add(zLabel);
		labelPanel.add(wLabel);
		labelPanel.add(ILabel);
		clearLabels();
		
		//Add everything to Control Panel
		controlPanel.add(boxPanel);
		controlPanel.add(buttonPanel);
		controlPanel.add(intensityPanel);
		controlPanel.add(labelPanel);
		
		//Set up the toolbar
		ToolBar toolBar = getToolBar();
		toolBar.addBrowserButton("Directions", "/org/webtop/html/laser/directions.html");
		toolBar.addBrowserButton("Theory","/org/webtop/html/laser/theory.html");
		toolBar.addBrowserButton("Examples","/org/webtop/html/laser/examples.html");
		toolBar.addBrowserButton("Exercises", "/org/webtop/html/laser/exercises.html");
		toolBar.addBrowserButton("Images","/org/webtop/html/laser/images.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
		
		//Sets up and links the engine to the GUI and X3D
		engine = new Engine(this);
		
	}
	
	//called by Engine
	public void setEngine(Engine e) {
		engine=e;
		engine.setModuleStatus("Welcome to the Lasers Module");
		engine.getBeam().setIntensity(INTENSITY_DEF);
		engine.setLength(cavityLenField.getValue());
		engine.setLambda(wavelengthField.getValue());
		engine.setR1(mirror1radField.getValue());
		engine.setR2(mirror2radField.getValue());
		engine.setScreenDist(screenDistField.getValue());
		
		//Set up the engine as a listener
		cavityLenWidget.addListener(engine);
		screenDistWidget.addListener(engine);
		
		//The engine will render when it finishes its setup
	}
		
	public void updateLabels() {
		
		if(engine.getBeam().isLasing()) {
			zLabel.setText("Z:   "+FPRound.toSigVal(engine.getZ(),3)+" mm");
			w0Label.setText("w0:   "+FPRound.toSigVal(engine.getW0(),3)+" mm");
			L1Label.setText("L1: "+FPRound.toSigVal(engine.getL1(),3)+" mm");
			zRLabel.setText("zR:   "+FPRound.toSigVal(engine.getzR(),3)+" mm");
			wLabel.setText("w:   "+FPRound.toSigVal(engine.getBeam().getWScreen(),3)+" mm");
			ILabel.setText("I:   "+FPRound.toFixVal(engine.getBeam().getI(),4));
		} else {
			clearLabels();
		}
		
	}
	
	public void clearLabels() {
		zLabel.setText("Z:");
		w0Label.setText("w0:");
		zRLabel.setText("zR:");
		wLabel.setText("zR:");
		ILabel.setText("I:");
		L1Label.setText("L1:");
	}
	
	//Sets up the menubar
	protected void setupMenubar() {	
	}
	
	

	protected void setDefaults() {
		cavityLenField.setValue(CAVITYLEN_DEF);
		mirror1radField.setValue(MIRROR1RAD_DEF);
		mirror2radField.setValue(MIRROR2RAD_DEF);
		screenDistField.setValue(SCREENDIST_DEF);
		wavelengthField.setValue(WAVELENGTH_DEF);

		modeChoice.setSelectedIndex(MODE_DEF);
		engine.getBeam().setApertureMode(MODE_DEF);
		engine.render(true);
		
		intensityScrollbar.setValue(INTENSITY_DEF);
		engine.getBeam().setIntensity(INTENSITY_DEF);

	
	}
	
	//*** Numberbox.Listener Interface ***//
	//Called when numberBox Numbers are changed
	public void numChanged(NumberBox source, Number newVal) {
		DebugPrinter.println("ControlPanel::numChanged("+source+","+newVal+")");

		float f=newVal.floatValue();
		
		if(source==wavelengthField) {
			engine.setLambda(f);
		}
		
		else if(source==mirror1radField) {
			if(Math.abs(f)<200) {
				source.revert();
				statusBar.setWarningText("The mirrors' radii of curvature must be at least 200 mm in absolute value.");
				return;
			}
			engine.setR1(f);
		}
		
		else if(source==mirror2radField) {
			if(Math.abs(f)<200) {
				source.revert();
				statusBar.setWarningText("The mirrors' radii of curvature must be at least 200 mm in absolute value.");
				return;
			}
			engine.setR2(f);
		} 
		
		else if(source==cavityLenField) {
			engine.setLength(f);
		} 
		
		else if(source==screenDistField) {
			engine.setScreenDist(f);
;		}

		engine.render(source!=screenDistField);
		
		if(statusBar.isWarning()) engine.setModuleStatus(null);

		updateLabels();
	}
	
	
	public void boundsForcedChange(NumberBox source, Number oldVal) {		
	}

	public void invalidEntry(NumberBox source, Number badVal) {
		DebugPrinter.println("ControlPanel::invalidEntry("+source+","+badVal+")");
		if(source==wavelengthField)
			statusBar.setWarningText("The wavelength must be in the visible -- between 400 and 700 nanometers.");
		else if(source==cavityLenField)
			statusBar.setWarningText("The cavity must be at least 250 mm long and no more than 1000 mm long.");
		else if(source==screenDistField)
			statusBar.setWarningText("The screen must be at least 200 mm beyond the cavity and no more than 2000 mm away from the rear end of the laser.");
		
	}
	
	//Used by ScreenSensor to find intensity at a point on the screen
	public void valueChanged(SpatialWidget src, float x, float y, float z) {
	}
	
	public void itemStateChanged(ItemEvent e) {
		if(e.getSource()==modeChoice) {
			int v = modeChoice.getSelectedIndex();
			engine.getBeam().setApertureMode(v);
			engine.render(true);
		}
	}
	
	//Called when Intensity Scroll Bar Adjusted
	 public void adjustmentValueChanged(AdjustmentEvent e) {
		//intensityScrollbar.setValue(e.getValue());
  		engine.getBeam().setIntensity(e.getValue());
  		}
      
	
	//*** Screen Calculation/Updating ***//
	protected void setWidgetDragging(Widget w, boolean drag) {
	}
	
	public void invalidEvent(String node, String event) {
	}



	


	
	//***********************WSL ROUTINES********************************//
	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
		cavityLenScripter.addTo(node);
		screenDistanceScripter.addTo(node); 
		mirrorRadius1Scripter.addTo(node); 
		mirrorRadius2Scripter.addTo(node);
		waveLengthScripter.addTo(node);
		intensityScrollbarScripter.addTo(node);
		modeChoiceScripter.addTo(node);
	}

	public String getWSLModuleName() {
		return new String("laser");
	}
	
	public void initialize(WSLScriptEvent event) {}

	public void scriptActionFired(WSLScriptEvent event) {}
		
	//********************END WSL ROUTINES*******************************//
	
	public static void main(String[] args) {
		Laser laser = new Laser("Laser", "/org/webtop/x3dscene/Laser.x3dv");
	}
	
	
}
