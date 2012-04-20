/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.rectangularslit;

import org.webtop.component.*;
import org.webtop.x3d.SAI;
import org.webtop.x3d.NamedNode;
import org.webtop.x3d.widget.*;
import org.webtop.x3d.output.*;
import org.sdl.gui.numberbox.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.sdl.math.Lambda;
import org.webtop.util.WTMath;
import org.web3d.x3d.sai.*;
import org.webtop.util.script.*;
import org.webtop.util.script.*;
import org.sdl.math.*; 

//WSL imports
import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;

/**
 * <p>Title: RectangularSlit</p>
 *
 * <p>Description: The X3D version of The Optics Project for the Web
 * (WebTOP)</p>
 *
 * <p>Company: MSU Department of Physics and Astronomy</p>
 *
 * @author Paul Cleveland, Peter Gilbert
 * @version 0.0
 */
public class RectangularSlit extends WApplication
        implements NumberBox.Listener, ActionListener, SpatialWidget.Listener{
    //General WApplication functions
    protected String getModuleName() {
        return "RectangularSlit";
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

    protected String getDate() {
        return "05 July 2006";
    }

    protected String getAuthor() {
        return "Paul Cleveland, Peter Gilbert";
    }

    protected Component getFirstFocus() {
       // return wavelengthBox;
    	return null;
    }

    /************************Module-specific code*****************************/
    public RectangularSlit(String title, String world) {
        super(title, world, true, false);
        //go ahead and update the screen
        updateScreen();
    }

    /* Constants and Utility Variables */
    private static final float WAVELENGTH_DEF = 500,
                               WAVELENGTH_MIN = 400,
                               WAVELENGTH_MAX = 700,
                               XWIDTH_DEF     = 0.2f,
                               XWIDTH_MIN     = 0.0f,
                               XWIDTH_MAX     = 1.0f,
                               YWIDTH_DEF     = 0.2f,
                               YWIDTH_MIN     = 0.0f,
                               YWIDTH_MAX     = 1.0f,
                               ZDIST_DEF      = 1000f,
                               ZDIST_MIN      = 1000f,
                               ZDIST_MAX      = 2000f,
                               TRANSPARENCY_DEF = 0.8f,
                               EXPOSURE_SCALE = 0.005f,   //scaling from [0,100] -> [0,0.5].  Used in updateScreen()
                               NM_TO_MM       = 0.000001f, //converts wavelength wheel's nanometer units to millimeters for math
                               Z_SCALE        = 20f;
    private static final int   EXPOSURE_DEF   = 70,
                               EXPOSURE_MIN   = 0,
                               EXPOSURE_MAX   = 100, 
                               HIRES          = 150,
                               LOWRES         = 50,
                               HIGRID         = HIRES*HIRES,
                               LOWGRID        = LOWRES*LOWRES,
                               EG_RES         = 100,
                               QUAD_WIDTH     = 500,      //width of the X3D quadrant (in X3D units) of the observation screen
                               HIRES_SCREEN   = 0,
                               LOWRES_SCREEN  = 1,
                               EG_SCREEN      = 2;

    private boolean widgetsVisible = true;
    private int screenShown = 0;
   private float displayIntensity; //for displaying intensity on the console [JD]

    /* X3D Variables */
    WheelWidget wavelengthWidget;
    XYDragWidget apertureWidget;
    XDragWidget zDistWidget;
    XDragWidget transparencyWidget;
    Switch widgetsSwitch;
    IFSScreen ifsHigh;  //High-resolution IFSScreen
    IFSScreen ifsLow;  //High-resolution IFSScreen
    ElevationGrid eg;
    MultiGrid observationScreen;
    TouchSensor ts; //provide display of parameters to console [JD]

    /* GUI Elements */
    FloatBox wavelengthBox, xWidthBox, yWidthBox, zDistBox, transparencyBox;
    JButton reset;
    ToggleButton hideWidgets;
    JScrollBar exposureScrollbar;   //Should replace with a JSlider that has inverted values.  Eliminates need for calculating exposure.  [PC]

    /* Couplers */
    ScalarCoupler wavelengthCoupler, zDistCoupler, transparencyCoupler;
    PlanarCoupler apertureCoupler;
    
    ////Scripters/////
    private NumberBoxScripter wavelengthScripter, xWidthScripter, yWidthScripter, zDistScripter;
    private NumberBoxScripter transparencyScripter; 
    private ButtonScripter resetScripter;
    private StateButtonScripter widgetScripter;
    private ScrollbarScripter exposureScripter;

    //remember to set up couplers here
    protected void setupGUI() {
        //Set up the control panel in a 3x1 grid
        controlPanel.setLayout(new GridLayout(3, 1));

        //Sub-panels
        JPanel topPanel = new JPanel();
        JPanel middlePanel = new JPanel();
        JPanel bottomPanel = new JPanel();

        //Set up and add GUI elements
        wavelengthBox = new FloatBox(WAVELENGTH_MIN, WAVELENGTH_MAX, WAVELENGTH_DEF, 5);
        wavelengthBox.addNumberListener(this);
        topPanel.add(new JLabel("Wavelength:"));
        topPanel.add(wavelengthBox);
        JLabel nm = new JLabel("nm  "); 
        topPanel.add(nm); 

        xWidthBox = new FloatBox(XWIDTH_MIN, XWIDTH_MAX, XWIDTH_DEF, 5);
        xWidthBox.addNumberListener(this);
        topPanel.add(new JLabel("X width:"));
        topPanel.add(xWidthBox);
        JLabel mm = new JLabel("mm  ");
        topPanel.add(mm); 
        
        yWidthBox = new FloatBox(YWIDTH_MIN, YWIDTH_MAX, YWIDTH_DEF, 5);
        yWidthBox.addNumberListener(this);
        topPanel.add(new JLabel("Y width:"));
        topPanel.add(yWidthBox);
        JLabel mm2 = new JLabel("mm  ");
        topPanel.add(mm2);

        zDistBox = new FloatBox(ZDIST_MIN, ZDIST_MAX, ZDIST_DEF, 5);
        zDistBox.addNumberListener(this);
        middlePanel.add(new JLabel("Z distance:"));
        middlePanel.add(zDistBox);
        JLabel mm3 = new JLabel("mm  ");
        middlePanel.add(mm3); 
        

        exposureScrollbar = new JScrollBar(JScrollBar.HORIZONTAL, EXPOSURE_DEF, 1, EXPOSURE_MIN, EXPOSURE_MAX);
        exposureScrollbar.setSize(20, 200);
        exposureScrollbar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                updateScreen();
            }
        });
        middlePanel.add(new JLabel("Exposure:"));
        middlePanel.add(exposureScrollbar);

        reset = new JButton("Reset");
        reset.addActionListener(this);
        bottomPanel.add(reset);

        hideWidgets = new ToggleButton("Hide Widgets", "Show Widgets", false);
        hideWidgets.addListener(new StateButton.Listener() {
            public void stateChanged(StateButton sb, int state) {
                    widgetsSwitch.setVisible(hideWidgets.getStateBool());
            }
        });
        bottomPanel.add(hideWidgets);
        //make a float box for the transparency widget but don't add to the control panel
        transparencyBox = new FloatBox(0f, 1000f, 0f,4);
        //couple the transparency box
        transparencyCoupler = new ScalarCoupler(transparencyWidget, transparencyBox,2, new ScalarCoupler.Converter(Lambda.linear(1,0),Lambda.linear(1,0)));
        //Add Couplers
        wavelengthCoupler = new ScalarCoupler(wavelengthWidget,wavelengthBox,2,new ScalarCoupler.Converter(Lambda.linear(1,0),Lambda.linear(1,0)));
        zDistCoupler = new ScalarCoupler(zDistWidget, zDistBox, 2,new ScalarCoupler.Converter(Lambda.linear(1/Z_SCALE,0),Lambda.linear(Z_SCALE,0)));
        apertureCoupler = new PlanarCoupler(apertureWidget, xWidthBox, yWidthBox, 2, new PlanarCoupler.Converter(Lambda.TwoD.parallel(Lambda.linear(500f,-100f),Lambda.linear(-500f,100f)),Lambda.TwoD.parallel(Lambda.linear(0.002f,0.2f),Lambda.linear(-0.002f,0.2f))));
        
        //initialize scripters
        wavelengthScripter = new NumberBoxScripter(wavelengthBox, getWSLPlayer(), null, "wavelength",
        		new Float(WAVELENGTH_DEF));
        xWidthScripter = new NumberBoxScripter(xWidthBox, getWSLPlayer(), null, "xWidth", 
        		new Float(XWIDTH_DEF));
        yWidthScripter = new NumberBoxScripter(yWidthBox, getWSLPlayer(), null, "yWidth", 
        		new Float(YWIDTH_DEF));
        zDistScripter = new NumberBoxScripter(zDistBox, getWSLPlayer(), null, "zDistance",
        		new Float(ZDIST_DEF));
        resetScripter = new ButtonScripter(reset, getWSLPlayer(), null, "reset");
        widgetScripter = new StateButtonScripter(hideWidgets, getWSLPlayer(), null, "hideWidgets", 
        		new String[] {"Hide Widgets", "Show Widgets"},0);
        exposureScripter = new ScrollbarScripter(exposureScrollbar, getWSLPlayer(), null, "exposureScrollbar",
        		EXPOSURE_DEF, null);
        transparencyScripter = new NumberBoxScripter(transparencyBox, getWSLPlayer(), null, 
        		"transparency",0.8f);
        ////end initialize scripters
        
        //Add sub-panels
        controlPanel.add(topPanel);
        controlPanel.add(middlePanel);
        controlPanel.add(bottomPanel);
        
        ToolBar toolbar = getToolBar();
        toolBar.addBrowserButton("Directions", "/org/webtop/html/rectangularslit/default.html");
		toolBar.addBrowserButton("Theory","/org/webtop/html/rectangularslit/default.html");
		toolBar.addBrowserButton("Examples","/org/webtop/html/rectangularslit/examples.html");
		toolBar.addBrowserButton("Exercises", "/org/webtop/html/rectangularslit/exercises.html");
		toolBar.addBrowserButton("Images","/org/webtop/html/rectangularslit/images.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
        //Now call updateScreen()
        updateScreen();
    }

    protected void setupX3D() {
        //set up widgets
        wavelengthWidget = new WheelWidget(getSAI(), getSAI().getNode("WavelengthWidget"), (short) 1, 
        		"Spin this wheel to adjust the wavelength");
        wavelengthWidget.addListener(this);
        apertureWidget = new XYDragWidget(getSAI(), getSAI().getNode("ApertureWidget"), (short) 2, 
        		"Use this widget to adjust the size of the aperture.");
        apertureWidget.addListener(this);
        zDistWidget = new XDragWidget(getSAI(), getSAI().getNode("ZDistWidget"), (short) 3, 
        		"Use this widget to move the observation screen.");
        zDistWidget.addListener(this);
        transparencyWidget = new XDragWidget(getSAI(), getSAI().getNode("TransparencyWidget"), (short) 4, 
        		"Use this widget to adjust the transparency of the aperture.");
        widgetsSwitch = new Switch(getSAI(), getSAI().getNode("WidgetsSwitch"), 1);

        //set up observation screen
        //Set up high-resolution display surface
        //System.out.println("setupX3D(): setup ifsHigh");
        ifsHigh=new IFSScreen(new IndexedSet(getSAI(),getSAI().getNode("IFSImageHigh")),new int[][] {{HIRES, HIRES}},QUAD_WIDTH,QUAD_WIDTH);
        //[Davis change. PC]//ifsHigh.setResolution(HIRES, HIRES);
        ifsHigh.setup();
        //System.out.println("setupX3D(): ifsHigh set");
        //Set up low-resolution display surface (used when widgets active)
        //System.out.println("setupX3D(): setup ifsLow");
        ifsLow = new IFSScreen(new IndexedSet(getSAI(), getSAI().getNode("IFSImageLow")),new int[][] {{LOWRES,LOWRES}}, QUAD_WIDTH, QUAD_WIDTH);
        //[Davis change. PC]//ifsLow.setResolution(LOWRES, LOWRES);
        ifsLow.setup();
        //System.out.println("setupX3D(): ifsLow set");
        //Set up ElevationGrid
        //eg = new ElevationGrid(getSAI(), getSAI().getNode("IrradianceImage"), new int[][]{{EG_RES, 2*EG_RES}}, QUAD_WIDTH, 2*QUAD_WIDTH);
        //eg.setup();

        //Combine grid screens together
        observationScreen = new MultiGrid(new Switch(getSAI(), getSAI().getNode("IFSSwitch"), 2),
        									new AbstractGrid[] {ifsHigh, ifsLow}
        );
        
        //For displaying x and y coords to the console [JD]
        ts = new TouchSensor(getSAI(), getSAI().getNode("IFSQuads_TS"),(short)1, "Display Screen Touch Sensor");
        ts.setOver(true); 
        ts.addListener((SpatialWidget.Listener)this);
        //observationScreen.showGrid(HIRES_SCREEN);
        //updateScreen();  //Must be done after setupGIU() is called, because updateScreen() uses the scrollbar
        

        /*float heights[] = new float[150*150];
        for (int i = 0; i<150*150; i++)
            heights[i]=100f;

        MFFloat testHeights = (MFFloat)getSAI().getNode("egrid").node.getField("set_height");
        testHeights.setValue(150*150,heights);*/

    }

    protected void setDefaults() {
        widgetsSwitch.setChoice(0);
        wavelengthBox.setValue(WAVELENGTH_DEF);
        xWidthBox.setValue(XWIDTH_DEF);
        yWidthBox.setValue(YWIDTH_DEF);
        zDistBox.setValue(ZDIST_DEF);
        transparencyWidget.setValue(TRANSPARENCY_DEF);
        exposureScrollbar.setValue(EXPOSURE_DEF);  //does swing fire an event?  [PC]
    }

    //******** Event Handling Methods ********//

    //NumberBox.Listener methods
    public void boundsForcedChange(NumberBox source, Number oldVal) {/*Come back and finish. [PC]*/}

    public void numChanged(NumberBox source, Number newVal) {
        if(source==wavelengthBox || source==xWidthBox || source==yWidthBox || source==zDistBox)
            updateScreen();
    }

    public void invalidEntry(NumberBox source, Number badVal) {
        System.out.println("Error: Invalid value of " + badVal+ " entered in " + source.getName());
        getStatusBar().setWarningText("Error:: Invalid value of " + badVal);
    }

    public void invalidEvent(String node, String event) {/*Come back and finish. [PC]*/}

    //ActionListener Method
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==reset)
            setDefaults();
    }

    @Override
    protected void setWidgetDragging(Widget w, boolean drag) {
    	//System.out.println("RectangularSlit.setWidgetDragging(" + drag + " ) called");
    	observationScreen.showGrid(drag?LOWRES_SCREEN:HIRES_SCREEN);  //tells observationScreen to swap to low-res screen
    	//System.out.println("observationScreen.current()==");
        updateScreen();  
    }
    
    ///////////////////WSL METHODS///////////////////////
    protected void toWSLNode(WSLNode node) {
    	super.toWSLNode(node);
    	wavelengthScripter.addTo(node);
    	xWidthScripter.addTo(node);
    	yWidthScripter.addTo(node);
    	zDistScripter.addTo(node);
    	widgetScripter.addTo(node);
    	exposureScripter.addTo(node);
    	transparencyScripter.addTo(node);
    }
    
    public String getWSLModuleName(){
    	return "RectangularSlit";
    }
    //////////////////////END WSL METHODS////////////////
    protected void setupMenubar() {
    }

    public void toolTip(Tooltip src, String tip) {
    }

    /*Math and drawing methods*/
    /**
     * Performs the sinc(u) function required by the intensity equation.  Takes a number
     * and performs sinc() on it.  Can use Math.pow() to square later.
     * @param u The argument number.
     * @return 1 if u==0, (float)(Math.sin(u)/u) otherwise.
     */
    private static float sinc(float u) {
        if(u==0f)
            return 1f;
        else
            return (float)(Math.sin(u)/u);
    }//end sinc()

    /**
     * Evaluates the intensity equation.
     * @param x x-coordinate
     * @param y y-coordinate
     * @param w_x Slit x-width
     * @param w_y Slit y-width
     * @param l Lambda (wavelength)
     * @param z Distance from aperture to screen
     * @return The intensity
     */
    private static float computeIntensity(float x, float y, float w_x, float w_y, float l, float z) {
    	x = (float)(10/8.85)*x; //To correct display {JD]
    	y = (float)(10/8.85)*y; //To correct display [JD]
        float ux = (float)((Math.PI*x*w_x)/(l*z));
        float uy = (float)((Math.PI*y*w_y)/(l*z));
        return (float) (Math.pow(sinc(ux), 2) * Math.pow(sinc(uy), 2));
    }//end computeIntensity()
    
     /* Used to update the whatever screen is currently showing.  Before calling, the caller
     * should set the proper grid for display using the <code>observationScreen.showGrid(int)</code>
     * method.
     */
    protected void updateScreen() {
        //'Pattern' variables
        //float[][] colors = intensity.getState()?intensityColors:(dragging?colorsLow:colorsHigh);

        //'Intensity' variables
        //float[] heights;

        /* While converting this to X3D/SAI, I attempted to remove most of the scaling
           of variables and values in the previous version whenever it wasn't necessary.
           The following approach to calculating the proper x,y values may not work right.
           Keep this in mind while debugging, and refer back to the VRML version.  [PC] */

    	boolean drag = draggingWidget();
    	
    	int RES = drag ? LOWRES : HIRES;
        int GRID = drag ? LOWGRID : HIGRID;
        System.out.println("updateScreen(): drag=" + drag + ", RES=" + RES + ", GRID=" + GRID);
        /*Need to map X3D's grid of [0,500] to [0, 10].  Going from X3D units to mm. [PC]*/
        /* Comment on the next line appears to be incorrect when viewing the new and old
           implementations side-by-side, but looking at the VRML version it would seem the
           last constant factor should be 0.2, not 0.02.  I'm missing where that extra
           factor of 10 was added in the VRML. [PC] */
        float SCALE = (500f/RES) * 0.02f; // factor of 0.02 is incorrect

        float[][] colors = new float[GRID][3];
        float[] gridPoint;
        //heights = new float[2*GRID];

        float[] gridPoint1;
        //float[] gridPoint2;
        //float x, y, intensity;
        float x, y, intensity;
        //Sending 'exposure' to Math.pow to calculate intensity inside the for loop below
        //causes the scrollbar to adjust the power to which the intensity is raised
        //from 1 (on the left) to ~0.5 (on the right).  While mathematically things
        //seem to be done in reverse, the visual effect is better for the user.
        float exposure = 1-exposureScrollbar.getValue()*EXPOSURE_SCALE;

        //int y1 = 0, y2 = 0, x1 = 0;
        boolean debug = false;

        float[] heights = new float[RES*RES];

        //Calculate changes to observation screen
        for(int i=0; i<GRID; i++) {
        	//Reversed the x and y calcuations...Screen pattern was wrong [JD]
            y = (i % (RES-1)) * SCALE;
            x = (i / (RES-1)) * SCALE;
            if(!debug) {
                System.out.println("values: " + xWidthBox.getValue() + " " + yWidthBox.getValue() + " " + wavelengthBox.getValue()*NM_TO_MM + " " + zDistBox.getValue());
                debug = true;
            }
            intensity = computeIntensity(x, -y, xWidthBox.getValue(), yWidthBox.getValue(), wavelengthBox.getValue()*NM_TO_MM, zDistBox.getValue());
            intensity = (float)Math.pow(intensity, exposure);
            gridPoint = colors[i];
            /*
            //If working with the intensity screen, also calculate heights
            if(irScreen) {
                y1 = i / (RES-1) + RES;
                y2 = RES - i / (RES-1) - 1;
                x1 = i % (RES-1);
                                if (y1<RES*2) {
                gridPoint1 = colors[y1*RES+x1];
                gridPoint2 = colors[y2*RES+x1];
                WTMath.hls2rgb(gridPoint1, WTMath.hue(wavelengthBox.getValue()), intensity, 1f);
                WTMath.hls2rgb(gridPoint2, WTMath.hue(wavelengthBox.getValue()), intensity, 1f);

                                // setting height
                                heights[y1*RES+x1] = intensity*400;
                                heights[y2*RES+x1] = intensity*400;

                                }
                                //else {
                                //	System.out.println("GRID: + " + GRID + ", i: " + i + ", y1: " + y1 + ", y2: " + y2);
                                //}
            }*/

            /*Uncomment the following line when implementing the ElevationGrid.*/
            //else
            WTMath.hls2rgb(gridPoint, WTMath.hue(wavelengthBox.getValue()), intensity, 1f);
            heights[i] = 100;
        }//end for
        //Now apply changes to observation screen
        //System.out.println("updateScreen():before setColors");
        observationScreen.setColors(colors);
        //System.out.println("updateScreen():after setColors");
        //System.out.println("updateScreen():before setup");
        observationScreen.setup();
        //System.out.println("updateScreen():after setup");
        //eg.setColors(colors);
        //eg.setHeights(heights);
        System.out.println("TransValue: " + transparencyBox.getValue());
    }

    //Show parameters from the display screen on the console [JD]
    public void valueChanged(SpatialWidget src, float x, float y, float z){
    	x = x/50; 
    	y = y/50; 
    	displayIntensity = computeIntensity(x, -y, xWidthBox.getValue(), yWidthBox.getValue(),
    			wavelengthBox.getValue()*NM_TO_MM, zDistBox.getValue());
    	
    	
    	statusBar.setText( "x: " + FPRound.toSigVal(x, 3) + 
    			"   y: " + FPRound.toSigVal(y,3)
    			+ "   Intensity: " + FPRound.toSigVal(displayIntensity, 3)); 
    	
    	
    }
    
    //**************Main****************//
    public static void main(String args[]) {
        RectangularSlit rectSlit = new RectangularSlit("RectangularSlit", 
        		"/org/webtop/x3dscene/RectangularSlit.x3dv");
    }
}
