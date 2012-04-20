/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Michelson.java
//Defines code for the Michelson Module
//Rhett Maxwell - Original VRML version
//Davis Herring - Updated Module
//Shane Fry - X3D/WApplication
//Updated August 18 2008

package org.webtop.module.michelson;

import org.webtop.component.WApplication;
import java.awt.Component;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.webtop.wsl.script.WSLNode;
import org.webtop.x3d.widget.*;
import org.webtop.x3d.output.*;
import org.sdl.gui.numberbox.*;
import org.webtop.component.*;
import org.sdl.math.Lambda;
import java.awt.event.*;
import org.webtop.component.ToggleButton;
import org.webtop.util.*;

import org.webtop.util.script.*;

//WSL imports
import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;


public class Michelson extends WApplication implements NumberBox.Listener, StateButton.Listener{
	public static void main(String[] args) {
		Michelson m = new Michelson("Michelson Interferometer", "/org/webtop/x3dscene/michelson.x3dv");
	}

	protected String getModuleName() {
		return "Michelson Interferometer";
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
		return "";
	}

	protected String getAuthor() {
		return "Davis Herring, Shane Fry, Paul Cleveland";
	}

	protected Component getFirstFocus() {
		return null;
	}

	//***************  Module-Specific Code ********************//

	RotationWidget phiBall;
	WheelWidget pathDiff;
	WheelWidget waveLength;
	Switch axisSwitch;
	Switch widgetSwitch;
	MultiGrid screen;
	LinePlot linePlot;
	
	
	/////Scripters////////
	private NumberBoxScripter phiScripter, diffScripter, waveLengthScripter;
	private ButtonScripter resetScripter;
	private StateButtonScripter widgetScripter, axisScripter;

	final float WAVELENGTH = 500f;
	final float PATHDIFF = 0.0070f;
	final float PHI = 0.00f;

	FloatBox phiBox;
	FloatBox pathDiffBox;
	FloatBox waveLengthBox;
	JButton reset;
	ToggleButton axisToggle;
	ToggleButton widgetToggle;
	boolean hideAxisToggle = true;
	boolean hideWidgetToggle = true;

	ScalarCoupler waveLengthCoupler;
	ScalarCoupler pathDiffCoupler;
	ScalarCoupler phiBallCoupler;

	//Screen variables
	public static final float SIZE = 30; //Length of one side of the screen
	private static final float _BLUE_WAVELENGTH_ = 400.0f;
	private static final float _RED_WAVELENGTH_ = 700.0f;
	private static final float _COLORMODEL_RED_WAVELENGTH_ = 630.0f;
	private static final float _SATURATION_ = 1.0f;
	private static final float _HUE_SCALE_ = 240.0f;
	private static final float MIN_I = 0, MAX_I = 9e-4f;

	int cell_number;
	float rgb[] = new float[3];
	float color[][];
	float highColors[][];
	float lowColors[][];
	int[] coordIndices;
	float point[][];

	//Resolutions
	private static final int HIGH_RES = 150;
	private static final int HIGHGRID = HIGH_RES * HIGH_RES / 2 + 1;
	private static final int SUPER_HIGH_RES = 350;
	private static final int LOW_RES = 75;
	private static final int LOWGRID = LOW_RES * LOW_RES / 2 +  1;

	//Module status variables
	private int x_cells = HIGH_RES, y_cells = LOW_RES;
	private double intensity_dx;


	//Math variables
	private double intensityCache[];			//Array of precalculated values for circularly-symmetric case
	private double beta;									//Intermediate value for position of off-axis virtual source
	private double cone_R2;								//Radius of light-cone from (possibly) off-axis virtual source
	private double cone_R2Squared;
	private double Cx;										//Displacement of light-cone from off-axis virtual source
	private double k;


	//Math constants
	private static final double TILT_MIRROR_RADIUS=3;	//cm
	private static final double L=10;		//cm
	private static final double L_O=50;	//cm
	private static final double PSI=Math.atan(TILT_MIRROR_RADIUS/(2*L));
	private static final double ALPHA=0;
	private static final double CONE_R1 = (3 * L + L_O) * Math.tan(PSI);
	
	
	protected void preconstructor()
	{
		intensityCache = new double[3 * SUPER_HIGH_RES];
		beta = 0;
		Cx = 0;
		cone_R2 = 0;
		cone_R2Squared = 0;
		k = 0;
	}
	
	protected void setupGUI() {
	
		controlPanel.setLayout(new GridLayout(3,1));
		
		//Grouping Panels
		JPanel topPanel = new JPanel();
		JPanel middlePanel = new JPanel();
		JPanel bottomPanel = new JPanel();
	
	
		phiBox = new FloatBox( -0.005f, 0.005f, PHI);
		pathDiffBox = new FloatBox( -0.02f, 0.02f, PATHDIFF);
		waveLengthBox = new FloatBox(400f, 700f, WAVELENGTH);
		axisToggle = new ToggleButton("Hide Axis", "Show Axis");
		widgetToggle = new ToggleButton("Hide Widgets", "Show Widgets");
		reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDefaults();
			}
		});
		phiBox.addNumberListener(this);
		pathDiffBox.addNumberListener(this);
		waveLengthBox.addNumberListener(this);

		axisToggle.addListener(this);
		widgetToggle.addListener(this);

		highColors = new float[HIGHGRID][3];
		lowColors = new float[LOWGRID][3];

		waveLengthCoupler = new ScalarCoupler(waveLength,waveLengthBox,2,new ScalarCoupler.Converter(Lambda.linear(1,0),Lambda.linear(1,0)));
		pathDiffCoupler = new ScalarCoupler(pathDiff, pathDiffBox,2,new ScalarCoupler.Converter(Lambda.linear(1,0),Lambda.linear(1,0)));
		phiBallCoupler = new ScalarCoupler(phiBall,phiBox,2,new ScalarCoupler.Converter(Lambda.linear(209.44,1.570793878),Lambda.linear(1.0/209.44,-.0075)));


		//initialize scripters
        waveLengthScripter = new NumberBoxScripter(waveLengthBox, getWSLPlayer(), null, "wavelength",
												   new Float(WAVELENGTH));
        phiScripter = new NumberBoxScripter(phiBox, getWSLPlayer(), null, "tiltAngle", 
											   new Float(PHI));
        diffScripter = new NumberBoxScripter(pathDiffBox, getWSLPlayer(), null, "pathLengthDifference", 
											   new Float(PATHDIFF));
        resetScripter = new ButtonScripter(reset, getWSLPlayer(), null, "reset");
        widgetScripter = new StateButtonScripter(widgetToggle, getWSLPlayer(), null, "widgets", 
												 new String[] {"Hide Widgets", "Show Widgets"},0);
		axisScripter = new StateButtonScripter(axisToggle, getWSLPlayer(), null, "axis", 
												 new String[] {"Hide Axis", "Show Axis"},0);

		////end initialize scripters
		


		topPanel.add(new JLabel("Phi:"));
		topPanel.add(phiBox);
		topPanel.add(new JLabel("deg"));
		topPanel.add(new JLabel("Path Difference:"));
		topPanel.add(pathDiffBox);
		topPanel.add(new JLabel("cm"));
		topPanel.add(new JLabel("Wavelength"));
		topPanel.add(waveLengthBox);
		topPanel.add(new JLabel("nm"));
		
		middlePanel.add(axisToggle);
		middlePanel.add(widgetToggle);
		middlePanel.add(reset);
		
		controlPanel.add(topPanel);
		controlPanel.add(middlePanel);
		controlPanel.add(bottomPanel);
		
		//Set up the toolbar
		ToolBar toolBar = getToolBar();
		toolBar.addBrowserButton("Directions", "/org/webtop/html/michelson/directions.html");
		toolBar.addBrowserButton("Theory","/org/webtop/html/michelson/theory.html");
		toolBar.addBrowserButton("Examples","/org/webtop/html/michelson/examples.html");
		toolBar.addBrowserButton("Exercises", "/org/webtop/html/michelson/exercises.html");
		toolBar.addBrowserButton("Images","/org/webtop/html/michelson/topimages.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
		
		
		/*addToConsole(new JLabel("Phi:"));
		addToConsole(phiBox);
		addToConsole(new JLabel("deg"));
		addToConsole(new JLabel("Path Difference:"));
		addToConsole(pathDiffBox);
		addToConsole(new JLabel("cm"));
		addToConsole(new JLabel("Wavelength"));
		addToConsole(waveLengthBox);
		addToConsole(new JLabel("nm"));
		addToConsole(axisToggle);
		addToConsole(widgetToggle);
		addToConsole(reset);
		*/
		
		
		pathDiffBox.setValue(0.0050f);
		waveLengthBox.setValue(556f);
		setDefaults();
	}

	protected void setupX3D()
	{
		phiBall = new RotationWidget(getSAI(), getSAI().getNode("PhiTrackWidget"), (short)1, "PHIBALL");
		phiBall.addListener(this);
		pathDiff = new WheelWidget(getSAI(), getSAI().getNode("PathDiffWheel"), (short)2, "PATHDIFF");
		pathDiff.addListener(this);
		waveLength = new WheelWidget(getSAI(), getSAI().getNode("WavelengthWheel"), (short)3, "WAVELENGTH");
		waveLength.addListener(this);

		axisSwitch = new Switch(getSAI(), getSAI().getNode("AxisToggle"), 2);
		widgetSwitch = new Switch(getSAI(), getSAI().getNode("WidgetsToggle"), 2);
		IFSScreen ifsHigh = new IFSScreen( new IndexedSet(getSAI(), getSAI().getNode("IFSImageHigh")), new int[][]{{HIGH_RES,HIGH_RES/2}}, 1000f, 500f);
		ifsHigh.setup();
		IFSScreen ifsLow = new IFSScreen( new IndexedSet(getSAI(), getSAI().getNode("IFSImageLow")), new int[][]{{LOW_RES,LOW_RES/2}}, 1000f, 500f);
		ifsLow.setup();
		screen = new MultiGrid(new Switch(getSAI(),getSAI().getNode("IFSSwitch"), 2), new AbstractGrid[]{ ifsHigh, ifsLow},0);
		screen.setup();

		//System.out.println("`@~@~@~@~@~@~@~@Current Screen: " + screen.current() + "~@~@~@@~@~@~@~@~@~@~@~@~@~@~@~@");
		//float tempColors[][] = new float[HIGH_RES*(HIGH_RES/2)][3];
		//for(int count = 0; count<HIGH_RES*(HIGH_RES/2);count++)
		//{
		//	tempColors[count][0] = 1f;
		//	tempColors[count][1] = 0f;
		//	tempColors[count][2] = 0f;
		//}
		//screen.setColors(tempColors);
		//screen.setup();

		linePlot = new LinePlot( new IndexedSet(getSAI(), getSAI().getNode("ilsNode")), 30f, 451, 226);
	}

	public void invalidEvent(String node, String event){}

	protected void setDefaults() {
		phiBox.setValue(0.00f);
		if(pathDiffBox == null)
			System.out.println("pathDiffBox == null");
		else
			pathDiffBox.setValue(0.0070f);
		waveLengthBox.setValue(500f);
		axisToggle.setState(0);
		axisSwitch.setVisible(true);
		widgetToggle.setState(0);
		widgetSwitch.setVisible(true);

		updateScreen(false);
	}
	protected void setupMenubar(){}

	public Michelson(String title, String world) {
		super(title, world, true, false);
		
	}

	protected void setWidgetDragging(Widget w, boolean dragging){
		screen.showGrid(dragging ? LOW_RES : HIGH_RES);
		updateScreen(dragging);
	}

	public void numChanged(NumberBox source, Number newVal) {
		if(source == pathDiffBox){
			cone_R2=(3.0*L+2.0*pathDiffBox.getValue()+L_O)*Math.tan(PSI);
			cone_R2Squared=cone_R2*cone_R2;
		}
		else if(source == phiBox){
			beta=-2.0*L*Math.sin(2.0*WTMath.toRads(phiBox.getValue()));
			Cx=(3.0*L+L_O-ALPHA)*Math.tan(2.0*WTMath.toRads(phiBox.getValue()))-beta;
		}
		else if(source == waveLengthBox){
			k=(2.0*Math.PI/(waveLengthBox.getValue()/1e7));
		}

		//System.out.println("`````````Number Changed````````");
		evaluate();
	}

	protected void updateScreen(boolean dragging) {

		//dragging = !dragging;
		dragging = draggingWidget();
		//System.out.println("*****Updating Screen******");
		if (dragging)
		{
			color = lowColors;
			x_cells = LOW_RES;
			y_cells = LOW_RES;
			//if(screen.current()==0)
			//System.out.println(screen.current().toString());
			screen.showGrid(1);
			System.out.println("Dragging widgets");
		}
		else
		{
			color = highColors;
			x_cells = HIGH_RES;
			y_cells = HIGH_RES;
			screen.showGrid(0);
			System.out.println("Not dragging widgets");
		}

		System.out.println("x_cells: " + x_cells);
		System.out.println("y_cells: " + y_cells);

		// setup the screen
		double dx = SIZE / (x_cells - 1);
		double dy = SIZE / (y_cells - 1);

		//*********************************
		// Define intensities
		//*********************************

			// TEST GRIG INDEXING
//		for(int x=0;x<x_cells;x++){
//			for(int y=0;y<y_cells/2;y++){
//				if(y%2==0)
//					color[y+x*y_cells/2]=getRGB(1);
//				else
//					color[y+x*y_cells/2]=getRGB(0);
//			}
//		}
	
		int temp = x_cells/2-1;
		for(int a=0;a<temp;a++){
			for(int b=0;b<y_cells-1;b++){
//				if(a == (temp-1))
//					color[a+b*temp]=getRGB(1);
//				if(b == 0 || b == (y_cells -2))
					color[a+b*temp]=getRGB(intensity(-SIZE/2+b*dx,a*dy));
			}
		}
//		color[0][0]=1;
//		color[0][1]=0;
//		color[0][2]=0;
//		
//		color[x_cells/2-1][0]=0;
//		color[y_cells/2-1][1]=1;
//		color[y_cells/2-1][2]=0;
		
//		for(int x=0;x<x_cells;x++){
//			for(int y=0;y<y_cells / 2;y++)
//			{
//		//		System.out.println("x = " + x + "\ty = " + y);// + "\tcolno = ") + colno);
//				try{
//					//color[y+x*y_cells / 2]=getRGB(intensity(-SIZE/2+x*dx,y*dy) );
//					color[x+y*x_cells]=getRGB(intensity(-SIZE/2+x*dx,y*dy));
//				}
//				catch(java.lang.ArrayIndexOutOfBoundsException e){
//					System.out.println("Indexing ERROR: " + e);
//					//e.printStackTrace();
//					System.exit(0);
//				}
//				//color[x*y_cells+y]=getRGB(intensity(-SIZE/2+x*dx,y*dy) );
//			}
//			//colno++;
//		}

		screen.setColors(color);
		screen.setup();
		evaluateLine(x_cells*3);

	}

		// TEST RGB FUNCTION
//	public float[] getRGB(float light) {
//		float a[] = new float[3];
//		
//		if(light == 1){
//			a[0]=0f;
//			a[1]=1f;
//			a[2]=0f;
//		}
//		else if(light==0){
//			a[0]=1f;
//			a[1]=1f;
//			a[2]=1f;
//		}
//
//		return a;
//	}
//	
	public float[] getRGB(float light) {
		//THE FOLLOWING SECTION OF CODE DETERMINES HUE FOR THE HLS TO RGB COLOR
		//CONVERSION
		//{SECTION OF CODE DESIGNED AND IMPLEMENTED BY KIRIL VIDIMCE, 1996/11/22}

		// * Modified for WebTOP by Rhett Maxwell, 2000/03/03

		//float hue = (float) (wavelength*1E5 - _BLUE_WAVELENGTH_) / (_RED_WAVELENGTH_ - _BLUE_WAVELENGTH_);

		float hue = (float) (waveLengthBox.getValue() - _BLUE_WAVELENGTH_) /
		(_COLORMODEL_RED_WAVELENGTH_ - _BLUE_WAVELENGTH_);

		hue *= _HUE_SCALE_;
		if (hue > _HUE_SCALE_) {
			hue = _HUE_SCALE_;
		} else if (hue < 0) {
			hue = 0;
		}
		hue = _HUE_SCALE_ - hue;
		float saturation = _SATURATION_; //we use a const. saturation
		//{END SECTION OF CODE DESIGNED AND IMPLEMENTED BY KIRIL VIDIMCE, 1996/11/22}

		//return HLS_2_RGB(hue,light,saturation);
		float[] rgb = new float[3];
		WTMath.hls2rgb(rgb, hue, light, saturation);
		return rgb;
	}

	public void evaluate() {
		long s = System.currentTimeMillis();
		evaluateIntensity();
		System.out.println("eI: " + (System.currentTimeMillis() - s) / 1000f);
		updateScreen(draggingWidget() );
		//observationLine.evaluate(x_cells * 3);
		//status_bar.setText("done");
	}

	/*    public void evaluate(int x, int y) {
        x_cells = x;
        y_cells = y;

        evaluate();
    }
	 */
	private float intensity(final double x, final double y)
	{
		return trueIntensity(x,y) / MAX_I;
	}

	private void evaluateLine(int cells) {
		// setup the screen

		float dx=SIZE/cells;
		float startX=-SIZE/2;

		float point[]=new float[cells+1];
		
		for(int x=0;x<cells+1;x++) {
			//point[x][0]=startX+x*dx;
			point[x]=intensity(startX+x*dx,0);
			//point[x][2]=0;
		}

		linePlot.setValues(point);
	}
	private void evaluateIntensity() {
		intensity_dx = SIZE / x_cells;

		for (int i = 0; i < 3 * x_cells; i++) {
			intensityCache[i] = (trueIntensity(i * intensity_dx, 0) - MIN_I) /
			(MAX_I - MIN_I);
		}

		double dx = SIZE / (x_cells - 1);
		double dy = SIZE / (y_cells - 1);

		long s = System.currentTimeMillis();
		for (int x = 0; x < x_cells; x++) {
			for (int y = 0; y < y_cells / 2; y++) {
				intensity( -SIZE / 2 + x * dx, y * dy);
			}
		}
		//System.out.println("i: "+(System.currentTimeMillis()-s)/1000f);

		//s=System.currentTimeMillis();
		for (int x = 0; x < x_cells; x++) {
			for (int y = 0; y < y_cells / 2; y++) {
				trueIntensity( -SIZE / 2 + x * dx, y * dy);
			}
		}
		//System.out.println("tI: "+(System.currentTimeMillis()-s)/1000f);
	}




	private float trueIntensity(final double X, final double Z) {
		//final long start=System.currentTimeMillis();
		//++ticount;

		//if(ticount % 9253 == 0) System.out.println("tIp: "+(float)tiprof/ticount);

		final double xSquared = X * X;
		final double zSquared = Z * Z;

		final double r = Math.sqrt(xSquared + zSquared +
				(3 * L + L_O) * (3 * L + L_O));

		final double A1 = ((X - Cx) * (X - Cx) + zSquared) <= CONE_R1 * CONE_R1 ?
				1 : 0,
				A2 = (xSquared + zSquared) <= cone_R2Squared ? 1 : 0,
						r1 = Math.sqrt((X - beta) * (X - beta) +
								(3 * L + L_O - ALPHA) *
								(3 * L + L_O - ALPHA) + Z * Z),
								r2 = Math.sqrt(X * X +
										(3 * L + 2 * pathDiffBox.getValue() + L_O) *
										(3 * L + 2 * pathDiffBox.getValue() + L_O) + Z * Z);

		return
		/*final float ret= */ (float) ( ((A1 + A2 -
				2 * A1 * A2 *
				Math.cos(k * (r2 - r1))) /
				(r * r)) ) ;

	}
	public void boundsForcedChange(NumberBox source, Number oldVal) {
	}

	public void invalidEntry(NumberBox source, Number badVal) {
	}

	public void stateChanged(StateButton sb, int state) {
		if(sb == axisToggle) {
			axisSwitch.setVisible(axisToggle.getStateBool());
		}
		else if(sb == widgetToggle) {
			widgetSwitch.setVisible(widgetToggle.getStateBool());
		}
	}
	

	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
		waveLengthScripter.addTo(node);
		diffScripter.addTo(node);
		phiScripter.addTo(node);
		//resetScripter.addTo(node);
		widgetScripter.addTo(node);
		axisScripter.addTo(node);
		// TODO Auto-generated method stub
		
	}


	public String getWSLModuleName() {
		return new String("michelson");
	}
}
