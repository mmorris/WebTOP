/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//FabryPerot.java
//Defines the applet class for the Fabry-Perot Interferometer Module.
//Rhett Maxwell and Davis Herring
//Updated April 18 2005
//Version 2.3.1

package org.webtop.module.fabryperot;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.webtop.component.*;
import org.webtop.wsl.script.WSLNode;
import org.webtop.x3d.widget.*;
import org.webtop.x3d.output.*;
import org.web3d.x3d.sai.*;

import org.webtop.util.*;
import org.webtop.util.script.*;
import org.webtop.util.script.*;

//WSL imports
import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;

import org.sdl.gui.numberbox.*;
import org.sdl.math.FPRound;
import org.webtop.x3d.widget.Widget;

public class FabryPerot extends WApplication implements ActionListener, StateButton.Listener,
        NumberBox.Listener, Widget.Listener, SpatialWidget.Listener {

    public static final int HIGH_RES = 150, HIGH_RES_CHOICE = 0;
    public static final int LOW_RES = 60, LOW_RES_CHOICE = 1;
    public static final int LINE_RES = 300;

    //float[][][] color = {new float[(HIGH_RES + 1) * (HIGH_RES + 1)][3], new float[(LOW_RES + 1) * (LOW_RES + 1)][3]};
    float[][][] color = {new float[(HIGH_RES + 1) * (HIGH_RES + 1)][3], new float[(LOW_RES + 1) * (LOW_RES + 1)][3]};


    protected String getModuleName() {
        return "Fabry-Perot Interferometer";
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
        return "April 14 2004";
    }

    protected String getAuthor() {
        return "Rhett Maxwell, Davis Herring";
    }

    protected Component getFirstFocus() {
        return indexField;
    }

    private static final short WAVELENGTH_WHEEL = 0, INDEX_WHEEL = 1,
            DEPTH_DRAGGER = 2, REFLECTIVITY_DRAGGER = 3,
            SCREEN_TOUCH = 4;

    private static final float SCREEN_SIZE = 7;

    private static final float DEF_WAVELENGTH = 550, MIN_INDEX = 1, MAX_INDEX = 3,
            DEF_INDEX = 1.5f, MIN_DEPTH = 0, MAX_DEPTH = 2,
            DEF_DEPTH = 1, MIN_REFLECTIVITY = 0, MAX_REFLECTIVITY = 1,
            DEF_REFLECTIVITY = 0.5f;

    private static final float INTENSITY_SCALE = 2 / 3f; // for color model


    // Math Constants
    private static final float L = 25.0f, Lp = 150.0f, n = 1.0f;

    //private float intensity_dx;


    //private float icache[];radius

    // Physics Variables
    private double F;
    private double smThCONST;
    //private double u1,u2,u3;

    //private double wOver2d;
    private double n_primeSqr;

    private double Imin /*,Imax*/;

    //**************************************
     // GUI Elements
     //**************************************
    private FloatBox wavelengthField, indexField, depthField, reflectivityField;

    private Panel buttonPanel;

    private JButton resetButton;
    private StateButton toggleAxisButton, toggleWidgetsButton;

    private NumberBoxScripter wavelengthScripter,indexScripter, depthScripter,reflectivityScripter;
	private ButtonScripter resetScripter;
    private StateButtonScripter widgetsScripter,axisScripter;

    //********************************************//
    // VRML Interface Objects											//
    //********************************************//
    //========Widgets========
    private WheelWidget wavelengthWidget, indexWidget;
    private XDragWidget depthWidget, reflectivityWidget;

    //========Output========
    private MultiGrid observationScreen;
    private LinePlot observationLine;

    //========Control========
    private SFInt32 widgetToggler, axisToggler;
    private ScalarCoupler  wavelengthCoupler;
    private ScalarCoupler indexCoupler;
    private ScalarCoupler depthCoupler;
    private ScalarCoupler reflectCoupler;


    public FabryPerot(String title, String world)
    {
        super(title,world,true,false);//ALWAYS true,false...need the statusBar (3rd argument)
        //getSAI().disableDraw(introductionChoice);
        evaluate();

    }

    private static int getResolution(boolean high) {
        return high ? HIGH_RES : LOW_RES;
    }

    public void setupGUI() {
        wavelengthField = new FloatBox(WApplication.MIN_WAVELENGTH, WApplication.MAX_WAVELENGTH, DEF_WAVELENGTH, 5);
        indexField = new FloatBox(MIN_INDEX, MAX_INDEX, DEF_INDEX, 5);
        depthField = new FloatBox(MIN_DEPTH, MAX_DEPTH, DEF_DEPTH, 5);
        reflectivityField = new FloatBox(MIN_REFLECTIVITY, MAX_REFLECTIVITY, DEF_REFLECTIVITY, 5);

        controlPanel.setLayout(new VerticalLayout());
       
        JPanel inputPanel = new JPanel();

        inputPanel.add(new JLabel("Index of Refraction"));
        inputPanel.add(indexField);
        //setTooltip(indexField,"index_tooltip");

        inputPanel.add(new JLabel("d:"));
        inputPanel.add(depthField);
        inputPanel.add(new JLabel("cm"));
        //setTooltip(depthField, "depth_label");

        inputPanel.add(new JLabel("R:"));
        inputPanel.add(reflectivityField);

        //(reflectivityField, "r_tooltip");

        inputPanel.add(new JLabel("Wavelength:"));
        inputPanel.add(wavelengthField);
        inputPanel.add(new JLabel("nm"));
        //setTooltip(wavelengthField, "wavelength_tooltip");

        wavelengthField.addNumberListener(this);
        indexField.addNumberListener(this);
        depthField.addNumberListener(this);
        reflectivityField.addNumberListener(this);

        controlPanel.add(inputPanel);

        buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        buttonPanel.add(resetButton = new JButton("Reset"));
        buttonPanel.add(toggleAxisButton = new StateButton("Axis"));
        buttonPanel.add(toggleWidgetsButton = new StateButton("Widgets"));
        resetButton.addActionListener(this);
        toggleAxisButton.addListener(this);
        toggleWidgetsButton.addListener(this);

        //setTooltip(resetButton, "resetButton_tooltip");
        //setTooltip(toggleAxisButton, "toggleAxisButton_tooltip");
        //setTooltip(toggleWidgetsButton, "toggleWidgetsButton_tooltip");

        //buttonPanel.add(new Label("Resolution:", Label.RIGHT));

        controlPanel.add(buttonPanel);

      /*  getStatusBar().setPreferredSize(new Dimension(636, 20));

        add(getStatusBar());
        getStatusBar().setFont(helv(11)); */

		wavelengthScripter=new NumberBoxScripter(wavelengthField,getWSLPlayer(),null,"wavelength",new Float(DEF_WAVELENGTH));
		indexScripter=new NumberBoxScripter(indexField,getWSLPlayer(),null,"indexOfRefraction",new Float(DEF_INDEX));
		depthScripter=new NumberBoxScripter(depthField,getWSLPlayer(),null,"d",new Float(DEF_DEPTH));
		reflectivityScripter=new NumberBoxScripter(reflectivityField,getWSLPlayer(),null,"reflectivity",new Float(DEF_REFLECTIVITY));

		resetScripter = new ButtonScripter(resetButton,getWSLPlayer(),null,"reset");

		final String hideValues[]={"visible","hidden"};
		widgetsScripter=new StateButtonScripter(toggleWidgetsButton,getWSLPlayer(),null,"widgets",hideValues,StateButton.VISIBLE);
		axisScripter=new StateButtonScripter(toggleAxisButton,getWSLPlayer(),null,"axis",hideValues,StateButton.VISIBLE);
         

        wavelengthCoupler = new ScalarCoupler(wavelengthWidget, wavelengthField, 4);
        indexCoupler = new ScalarCoupler(indexWidget, indexField, 4);
        depthCoupler = new ScalarCoupler(depthWidget, depthField, 4);
        reflectCoupler = new ScalarCoupler(reflectivityWidget, reflectivityField, 4);
		
		//Set up the toolbar
		ToolBar toolBar = getToolBar();
		toolBar.addBrowserButton("Directions", "/org/webtop/html/fabryperot/directions.html");
		toolBar.addBrowserButton("Theory","/org/webtop/html/fabryperot/theory.html");
		toolBar.addBrowserButton("Examples","/org/webtop/html/fabryperot/examples.html");
		toolBar.addBrowserButton("Exercises", "/org/webtop/html/fabryperot/exercises.html");
		toolBar.addBrowserButton("Images","/org/webtop/html/fabryperot/topimages.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");

    }

    public void setupX3D() {
        // show the "loading please wait" message
        //introductionChoice = (SFInt32) getSAI().getInputField("IntroductionSwitch","set_whichChoice");
        //getSAI().enableDraw(introductionChoice);

        //icache=new float[3*HIGH_RES];

        //Imax=1;
        //Imin=0;
        IFSScreen ifsHigh = new IFSScreen(new IndexedSet(getSAI(), getSAI().getNode("ifsNode")), new int[][] {{HIGH_RES, HIGH_RES}},
                    SCREEN_SIZE / 2, SCREEN_SIZE / 2);
        ifsHigh.setup();
        IFSScreen ifsLow = new IFSScreen(new IndexedSet(getSAI(), getSAI().getNode("ifsNode_low")), new int[][] {{LOW_RES, LOW_RES}},
                    SCREEN_SIZE / 2, SCREEN_SIZE / 2);
        ifsLow.setup();

        observationScreen = new MultiGrid(new Switch(getSAI(), getSAI().getNode("ResolutionSwitch"), 2), new Grid[] {ifsHigh, ifsLow});
        observationScreen.showGrid(0);

        observationLine = new LinePlot(new IndexedSet(getSAI(), getSAI().getNode("ilsNode")),SCREEN_SIZE, LINE_RES);

        widgetToggler = (SFInt32) getSAI().getInputField("WidgetSwitch", "whichChoice");
        axisToggler = (SFInt32) getSAI().getInputField("AxisSwitch", "whichChoice");

        wavelengthWidget = new WheelWidget(getSAI(), getSAI().getNode("WavelengthWidget"),
                                           WAVELENGTH_WHEEL, "Spin to change the wavelength.");
        indexWidget = new WheelWidget(getSAI(), getSAI().getNode("RefractionWidget"), INDEX_WHEEL,
                                      "Spin to change the index of refraction.");
        depthWidget = new XDragWidget(getSAI(), getSAI().getNode("DepthWidget"), DEPTH_DRAGGER,
                                      "Slide to change the thickness of the etalon.");
        reflectivityWidget = new XDragWidget(getSAI(), getSAI().getNode("ReflectivityWidget"),
                                             REFLECTIVITY_DRAGGER,
                                             "Slide to change the reflectivity of the coating.");
        final TouchSensor ts = new TouchSensor(getSAI(), getSAI().getNode("ScreenTouch"),
                                               SCREEN_TOUCH, null);

        wavelengthWidget.addListener(this);
        indexWidget.addListener(this);
        depthWidget.addListener(this);
        reflectivityWidget.addListener(this);
        //ts.addListener((Widget.Listener)this);
        ts.setOver(true);
        ts.addListener((SpatialWidget.Listener)this);

        //getManager().addHelper(new ScalarCoupler(wavelengthWidget, wavelengthField, 4));
        //getManager().addHelper(new ScalarCoupler(indexWidget, indexField, 4));
        //getManager().addHelper(new ScalarCoupler(depthWidget, depthField, 4));
        //getManager().addHelper(new ScalarCoupler(reflectivityWidget, reflectivityField, 4));

        //new DragSilencer(wavelengthWidget, wavelengthScripter);
        //new DragSilencer(indexWidget, indexScripter);
        //new DragSilencer(depthWidget, depthScripter);
        //new DragSilencer(reflectivityWidget, reflectivityScripter);

        //getManager().addHelper(new ScalarScripter(wavelengthWidget, getWSLPlayer(), null,
                                                  //"wavelength", DEF_WAVELENGTH));
        //getManager().addHelper(new ScalarScripter(indexWidget, getWSLPlayer(), null,
                                                  //"indexOfRefraction", DEF_INDEX));
        //getManager().addHelper(new ScalarScripter(depthWidget, getWSLPlayer(), null, "d", DEF_DEPTH));
        //getManager().addHelper(new ScalarScripter(reflectivityWidget, getWSLPlayer(), null,
                                                  //"reflectivity", DEF_REFLECTIVITY));
    }

    protected void scriptCleanup() {
        evaluate(true);
    }

    protected void setDefaults() {
        depthField.setValue(DEF_DEPTH);
        wavelengthField.setValue(DEF_WAVELENGTH);
        indexField.setValue(DEF_INDEX);
        reflectivityField.setValue(DEF_REFLECTIVITY);

        evaluate(true);
    }



    private void evaluate() {
        evaluate(true);
        //evaluate(!draggingWidget());
    }

    private void evaluate(boolean high) {
        //getStatusBar().setText("Calculating - please wait...");

        final int cells = getResolution(high);

        cacheIntensities(cells);

        final double math_dx = SCREEN_SIZE / 2 / (cells - 1),
                               math_dy = SCREEN_SIZE / 2 / (cells - 1);
        color = new float[][][] {new float[(HIGH_RES + 1) * (HIGH_RES + 1)][3], new float[(LOW_RES + 1) * (LOW_RES + 1)][3]};
        if(color == null)
            System.out.println("FabryPerot.evaluate()::color is null");
        final float[][] array = color[high?HIGH_RES_CHOICE:LOW_RES_CHOICE];

        for (int x = 0; x < cells; x++)
            for (int y = 0; y < cells; y++)
                array[x + y * cells] = getRGB(intensity(x * math_dx, y * math_dy) * INTENSITY_SCALE);

        System.out.println("FabryPerot.evaluate()::array.length = " + array.length);
        observationScreen.setColors(array);
        observationScreen.setup();

        final float line_values[] = new float[LINE_RES];
        for (int i = 0; i < LINE_RES; i++) {
            final float x = -SCREEN_SIZE / 2 + i * SCREEN_SIZE / (LINE_RES - 1);
            line_values[i] = intensity(x, 0);
        }
        observationLine.setValues(line_values);

        resetStatus();
    }

    /*************************************************************/
    /* this function precalculates intensity for radial symmetry */
    /*************************************************************/
    private void cacheIntensities(int cells) {
        //We make the casts only once:
        final double r = reflectivityField.getValue(), w = wavelengthField.getValue(),
                n = indexField.getValue(), d = depthField.getValue();

        // initial calculations
        F = 4 * r / ((1 - r) * (1 - r));
        Imin = 1 / (1 + F);
        smThCONST = (4.0 * Math.PI * d) / (w * 1e-7f);
        // Dr. Foley's calculations of a1, a2, and a3 are actually Imin
        // with the values of 0.02, 0.17, and 0.47 added, respectively.
        // Therefore this will be the manner of how u1, u2, and u3, will be
        // defined.
        /*u1					=	 Math.asin( Math.sqrt( (1-Imin+0.02) / F /
                           (Imin+0.02) ))/Math.PI;

           u2					=	 Math.asin( Math.sqrt( (1-Imin+0.17) / F /
                           (Imin+0.17) ))/Math.PI;

           u3					=	 Math.asin( Math.sqrt( (1-Imin+0.47) / F /
                           (Imin+0.47) ))/Math.PI;*/

        // optimize math
        //wOver2d			=	 (w*1e-7f)/(2*d);
        n_primeSqr = n * n;

        //intensity_dx=SCREEN_SIZE/(cells-1);

// 		//calculate preliminary value, then the real thing
// 		double th=(Math.floor(2*n*d/(w*1e-7f))-0.5)*(w*1e-7f)/(2*d);
// 		th=Math.asin(Math.sqrt(n*n-th*th));
// 		final int valleyCutOff=(int) Math.floor(((L+d+Lp)*Math.tan(th))/intensity_dx);

// 		final int cutOff=(int) Math.ceil(3.5/intensity_dx);
// 		// Calculate Intensities
// 		for(int i=0;i<cutOff;i++) {
// 			icache[i]=trueIntensity(i*intensity_dx);
// 			if(r>0.65 && i>valleyCutOff && icache[i]>=0.25) icache[i]=1.0f;
// 			if(r>0.80 && i>valleyCutOff && icache[i]>0.10) icache[i]=1.0f;

// 			if(Imax<icache[i]) Imax=icache[i];
// 			if(Imin>icache[i]) Imin=icache[i];
// 		}

// 		// Normalize icache
// 		for(int i=0;i<cutOff;i++) icache[i]=(float) icache[i];
// 		for(int i=cutOff;i<3*cells;i++) icache[i]=0;
    }

    private double theta(double r) {
        return Math.atan(r / (L + Lp + depthField.getValue()));
    }

    private float trueIntensity(double radius) {
        if (radius > SCREEN_SIZE / 2)return 0;
        final double theta = theta(radius),
                             smTheta = smThCONST * Math.sqrt(n_primeSqr - n * n * Math.sin(theta) *
                Math.sin(theta));

        return 1 / (float) (1 + F * Math.sin(smTheta / 2) * Math.sin(smTheta / 2));
    }

    private float intensity(double X, double Z) {
// 		float f=(float) (Math.sqrt(X*X+Z*Z)/intensity_dx);
// 		float f1=(float) Math.floor(f);
// 		float f2=(float) Math.ceil(f);

// 		int n1=(int) f1;
// 		int n2=(int) f2;

// 		return icache[n1]+(f-f1)*(icache[n2]-icache[n1]);
        return trueIntensity(Math.sqrt(X * X + Z * Z));
    }

    private float[] getRGB(float light) {
        float[] rgb = new float[3];
        WTMath.hls2rgb(rgb, WTMath.hue(wavelengthField.getValue()),
                       light, WTMath.SATURATION);
        return rgb;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == resetButton) {
            setDefaults();
        } else System.err.println("FabryPerot: unexpected ActionEvent " + e);
    }

    public void stateChanged(StateButton sb, int state) {
        //This still seems a bit clumsy -- is there a better way to check for
        //'visibility of a hide/show object button'?  Perhaps a special
        //StateButton function? [Davis]
        if (sb == toggleWidgetsButton)
            getSAI().setDraw(widgetToggler, state == StateButton.VISIBLE);
        else if (sb == toggleAxisButton)
            getSAI().setDraw(axisToggler, state == StateButton.VISIBLE);
    }

    public void numChanged(NumberBox source, Number newVal) {
        //Is there some better way to check for an 'expected' NumberBox? [Davis]
        if (source == indexField);
        else if (source == depthField);
        else if (source == reflectivityField);
        else if (source == wavelengthField);
        else {
            System.err.println("Engine: unexpected numChanged from " + source);
            return;
        }

        evaluate();
    }

    public void boundsForcedChange(NumberBox source, Number oldVal) {} //never happens

    public void invalidEntry(NumberBox source, Number badVal) {
        if (source == indexField)
            getStatusBar().setWarningText("index_warning" + MIN_INDEX + "and" + MAX_INDEX + ".");
        else if (source == depthField)
            getStatusBar().setWarningText("depth_warning" + MIN_DEPTH + "and" + MAX_DEPTH + ".");
        else if (source == reflectivityField)
            getStatusBar().setWarningText("reflectivity_warning" + MIN_REFLECTIVITY + "and" +
                                          MAX_REFLECTIVITY + ".");
        else if (source == wavelengthField)
            getStatusBar().setWarningText("wavelength_warning" + WApplication.MIN_WAVELENGTH +
                                          " and " + WApplication.MAX_WAVELENGTH + "nanometers");
        else System.err.println("FabryPerot: unexpected invalidEntry from " + source);
    }

    protected void setWidgetDragging(Widget w, boolean drag) {
        observationScreen.showGrid(drag ? LOW_RES_CHOICE : HIGH_RES_CHOICE);
        evaluate();
    }

    public void valueChanged(SpatialWidget src, float x, float y, float z) {
    	
        final float r = (float) Math.sqrt(x * x + y * y);
        //This assumes that the only SpatialWidget is the screen TouchSensor
        getStatusBar().setText("\u03b8 = " + FPRound.toFixVal(WTMath.toDegs(theta(r)), 3) +
                               "\u00b0    " + " Intensity = " + FPRound.toFixVal(trueIntensity(r), 3));
    }

    // ----------------------------------------------------------------------
    // WSL Methods
    // ----------------------------------------------------------------------
    public String getWSLModuleName() {
        return "fabryperot";
    }
    
    protected void setupMenubar() {
    }

    public void invalidEvent(String node, String event) {
    }

    public void toolTip(Tooltip src, String tip) {
    }

    public void mousePressed(Widget src) {
    }

    public void mouseReleased(Widget src) {
    }

    public void mouseEntered(Widget src) {
    }

    public void mouseExited(Widget src) {
    }

    public static void main(String args[])
    {
        FabryPerot fp = new FabryPerot("Fabry Perot","/org/webtop/x3dscene/fabryperot.x3dv");
    }
    
	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
		wavelengthScripter.addTo(node);
		indexScripter.addTo(node);
		depthScripter.addTo(node);
		reflectivityScripter.addTo(node);
		widgetsScripter.addTo(node);
		axisScripter.addTo(node);
	}
    
    
}
