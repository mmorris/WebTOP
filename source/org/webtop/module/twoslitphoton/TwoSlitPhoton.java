package org.webtop.module.twoslitphoton;

import java.awt.*;
import java.awt.event.*; 

import javax.swing.*; 

import org.web3d.x3d.sai.*;
import org.web3d.x3d.*; 
import org.sdl.gui.numberbox.*; 
import org.sdl.gui.*;
import org.sdl.math.FPRound;
import org.webtop.wsl.client.*; 
import org.webtop.wsl.event.*;
//import org.webtop.module.twoslitphoton.resources.BoxMaker;
//import org.webtop.module.twoslitphoton.resources.Engine;
//import org.webtop.module.twoslitphoton.resources.NSlitDragger;
import org.webtop.util.*;
import org.webtop.util.script.*;
import org.webtop.x3d.output.*; 
import org.webtop.x3d.widget.*;

import org.webtop.component.*;
import org.webtop.wsl.script.*;

public class TwoSlitPhoton extends WApplication implements 
	ActionListener, NumberBox.Listener, AdjustmentListener, X3DFieldEventListener{
	
	//May not be extending the right class...will need to check later
	/*private static class WidgetEvent extends org.webtop.x3d.widget.Widget
	{
		//[These constants also index into wslStrings]
		public static final short
			WAVELENGTH = 1,
			WIDTH = 2,
			DISTANCE = 3,
			SCREEN = 4,
			SCREEN_MESH = 5;

		//public WidgetEvent(short w,short a) {super(w,a);
	}*/
	
	
	//Various variables used in the animation part of the module
	private Animation animation; 
	private Engine engine;
	private Data data = new Data(); 
	
	//Create the data class here
	public static final class Data implements Animation.Data, Cloneable{
		public float wavelength; 
		public float width; 
		public float distance; 
		
		public Animation.Data copy(){
			try{
				return((Data)clone()); //all data is primitive, so clone is fine
			}
			catch(CloneNotSupportedException e){
				return null; //can't happen
			}
		}
	}
	
	//Global Vars
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
	
	//Slit Stuffs
	private BoxMaker slitEngine;					//slit geometry creator
	private NSlitDragger nslitDraggerEngine;	//slit widget coordinator
	private SFFloat	nsd_set_width;
	private SFFloat	nsd_set_distance;
	
	//	UI elements
	private FloatBox wavelengthField;
	private IntBox rateField;
	private FloatBox widthField;
	private FloatBox distanceField;
	private FloatBox exposureTimeField;
	private JButton resetButton;
	private JButton clearButton;
	private JButton animationButton;
	private StateButton audioButton;
	private JLabel photonCount;
	private JLabel elapsedTime;
	private JScrollBar brightnessScrollbar;
	
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
	
	//X3D Stuffs
	private LinePlot plot;
	private SFFloat transparency;
	private WheelWidget wavelengthWidget;
	
	
	public TwoSlitPhoton(String title, String world)
	{
		super(title, world, true, true);
	}
	
	protected String getAuthor() {
		return "Lamar Barnett, Jeremy Davis";
	}

	protected String getDate() {
		return null;
	}

	protected Component getFirstFocus() {
		return null;
	}

	protected int getMajorVersion() {
		return 6;
	}

	protected int getMinorVersion() {
		return 1;
	}

	protected String getModuleName() {
		return "TwoSlitPhoton";
	}

	protected int getRevision() {
		return 1;
	}

	protected void setDefaults() {

	}

	protected void setupGUI() {
		JPanel panel;
		panel = new JPanel();
		JPanel panel2 = new JPanel(); 
		JPanel panel3 = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		
		photonCount =new JLabel("    0");
		elapsedTime =new JLabel("   0.0");
		resetButton = new JButton("Reset"); 
		clearButton= new JButton("Clear Screen"); 
		animationButton = new JButton(" Play ");
		audioButton =new StateButton("Audio ", new String[] {"",""},new String[]{"On","Off"});
		panel.add(new JLabel("        Wavelength:",JLabel.RIGHT));
		panel.add(wavelengthField=new FloatBox(WAVELENGTH_MIN,WAVELENGTH_MAX,45,5));
		panel.add(new JLabel("nm"));
		
		panel.add(new JLabel("Width:",JLabel.RIGHT));
		panel.add(widthField=new FloatBox(WIDTH_MIN,WIDTH_MAX,45,5));
		panel.add(new JLabel("mm"));
		
		panel.add(new JLabel("Distance:",JLabel.RIGHT));
		panel.add(distanceField=new FloatBox(DISTANCE_MIN,DISTANCE_MAX,45,5));
		panel.add(new JLabel("mm   "));
		
		panel.add(new JLabel("Photons/sec:",JLabel.RIGHT));
		panel.add(rateField=new IntBox(RATE_MIN,RATE_MAX,45,5));
		
		panel2.add(new JLabel("Exposure time:",JLabel.RIGHT));
		panel2.add(exposureTimeField=new FloatBox(EXPOSURE_MIN,EXPOSURE_MAX,45,5));
		panel2.add(new JLabel("sec"));
	
		panel2.add(new JLabel("Photons:",JLabel.RIGHT) );
		photonCount.setHorizontalAlignment(JLabel.RIGHT);
		panel2.add(photonCount);
		
		panel2.add(new JLabel("Elapsed time:",JLabel.RIGHT) );
		elapsedTime.setHorizontalAlignment(JLabel.RIGHT);
		panel2.add(elapsedTime);
		panel2.add(new JLabel("sec"));
		
		panel3.add(animationButton);
		panel3.add(clearButton);
		panel3.add(resetButton);
		panel3.add(audioButton);
		
		panel2.add(new JLabel("Brightness:",JLabel.RIGHT) );
		brightnessScrollbar=new JScrollBar(JScrollBar.HORIZONTAL,DEF_BRIGHTNESS,1,0,MAX_BRIGHTNESS);
		brightnessScrollbar.addAdjustmentListener(this);
		panel2.add(brightnessScrollbar);
		
		//add(panel, BorderLayout.CENTER);
		controlPanel.add(panel, BorderLayout.NORTH);
		controlPanel.add(panel2, BorderLayout.CENTER);
		controlPanel.add(panel3, BorderLayout.SOUTH);
		setDefaults();
		
		
		//Add Listeners
		wavelengthField.addNumberListener(this);
		rateField.addNumberListener(this);
		widthField.addNumberListener(this);
		distanceField.addNumberListener(this);
		exposureTimeField.addNumberListener(this);
		animationButton.addActionListener(this);
		resetButton.addActionListener(this);
		clearButton.addActionListener(this);
		
		audioButton.addListener(new StateButton.Listener(){
			public void stateChanged(StateButton sb, int state){
				engine.setAudio(state==1);
			}
		});
		
		//Don't worry about setting up the scripters just yet.  Do this later when 
		//we have the module working
		/*
		wavelengthScripter = new NumberBoxScripter(wavelengthField,getWSLPlayer(),null,"wavelength",new Float(WAVELENGTH_DEF));
		rateScripter = new NumberBoxScripter(rateField,getWSLPlayer(),null,"photonRate",new Float(RATE_DEF));
		widthScripter = new NumberBoxScripter(widthField,getWSLPlayer(),null,"slitWidth",new Float(WIDTH_DEF));
		distanceScripter = new NumberBoxScripter(distanceField,getWSLPlayer(),null,"slitDistance",new Float(DISTANCE_DEF));
		exposureTimeScripter = new NumberBoxScripter(exposureTimeField,getWSLPlayer(),null,"exposureTime",new Float(EXPOSURE_DEF));
		animationScripter = new ButtonScripter(animationButton,getWSLPlayer(),null,"animation",this);
		audioScripter = new StateButtonScripter(audioButton,getWSLPlayer(),null,"audio",new String[] {"On","Off"},1);
		resetScripter = new ButtonScripter(resetButton,getWSLPlayer(),null,"reset",this);
		clearScripter = new ButtonScripter(clearButton,getWSLPlayer(),null,"clearScreen",this);	
		*/
	}

	protected void setupMenubar() {

	}

	protected void setupX3D() {
		engine = new Engine(this,getSAI());
		slitEngine = new BoxMaker(getSAI());
		nslitDraggerEngine = new NSlitDragger(getSAI());
		animation = new Animation(engine,data,ANIMATION_PERIOD);
		plot=new LinePlot(new IndexedSet(getSAI(),getSAI().getNode("ilsNode")),1000,PLOT_RES);
		transparency = (SFFloat) getSAI().getOutputField(getSAI().getNode("TRANS_WORKER"), 
				"transparency_in" ,this, "transparency");
		setBrightness(DEF_BRIGHTNESS);
		engine.setRate(RATE_DEF);
		
		wavelengthWidget=new WheelWidget(getSAI(),getSAI().getNode("WavelengthWidget"),(short)2,"Turn to change the wavelength.");
		wavelengthWidget.addListener(this);
		//getManager().addHelper(new ScalarCoupler(wavelengthWidget,wavelengthField,1,new ScalarCoupler.Converter(Lambda.linear(0.04f,0f),Lambda.linear(1,0f))));
		
		wavelengthWidgetScripter = new ScalarScripter(wavelengthWidget,getWSLPlayer(),null,"wavelengthdrag",WAVELENGTH_DEF);
	}
	
	//Module Specific Methods
	public void setStopped() {
		exposureTimeField.setEnabled(true);
		setState(STOPPED);
		animationButton.setText("Play");
		animation.setPaused(true);
		engine.reset();
		engine.updateScreen();
	}
	
	public void setElapsedTime(float time) {
		elapsedTime.setText(String.valueOf(FPRound.toFixVal(time,1)));
	}
	public void setPhotonCount(int count) {
		photonCount.setText(String.valueOf(count));
	}
	public float getExposureTime() {
		return exposureTimeField.getValue();
	}	
	
	/*
	 * EVENT HANDLING METHODS
	 */
	public void invalidEvent(String node, String event) {

	}
	//implement actionListener
	public void actionPerformed(ActionEvent e){
		
	}
	//implement NumberBox.Listener
	public void invalidEntry(NumberBox source, Number badVal){
		getStatusBar().setWarningText("Invalid Entry");
	}
	public void numChanged(NumberBox source, Number num){
		
	}
	public void boundsForcedChange(NumberBox source, Number num){
		
	}
	
	//implement adjustmentListener
	//@Override
	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	//implement X3DFieldEventListener
	//@Override
	public void readableFieldChanged(X3DFieldEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	/*END EVENT HANDLING
	 */
	
	//native methods
	public void setBrightness(float value) {
		transparency.setValue(((float)MAX_BRIGHTNESS-value)/(float)MAX_BRIGHTNESS);
	}
	
	public String getWSLModuleName() {
		return "twoslit";
	}
	
	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TwoSlitPhoton twoslitphoton = new TwoSlitPhoton("TwoSlitPhoton", 
				"/org/webtop/x3dscene/twoslitnotsure.x3dv");
	}

	



}
