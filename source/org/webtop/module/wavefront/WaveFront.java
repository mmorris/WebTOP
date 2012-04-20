/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.wavefront;


import org.webtop.component.WApplication;
import java.awt.Component;
//import java.awt.Button;
import java.awt.*;
//import java.awt.Panel;
import javax.swing.JTabbedPane;
import javax.swing.event.*;
import javax.swing.*;

import org.sdl.gui.numberbox.*;
import org.webtop.wsl.script.WSLNode;
import org.webtop.wsl.client.*;
import org.webtop.x3d.widget.*;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;
import org.sdl.math.Lambda;
import org.webtop.util.*;
import org.webtop.component.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.webtop.module.wavefront.Engine;
import org.webtop.util.script.*;

//WSL imports
import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;

/**
 * <p>Title: WebTOP</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Mississippi State University Department of Physics and
 * Astronomy</p>
 *
 * @author not attributable
 * @version 0.0
 */
public class WaveFront extends WApplication
        implements NumberBox.Listener,StateButton.Listener,ActionListener, ChangeListener,
        			X3DFieldEventListener, WSLModule, WSLScriptListener {

	protected String getModuleName() {return "Wavefront";}
    protected int getMajorVersion() {return 6;}
    protected int getMinorVersion() {return 1;}
    protected int getRevision() {return 1;}
    protected String getDate() {return "Feb 17 2004";}
    protected String getAuthor() {return "David Moore, Brian Thomas";}

    //WSL/animation classes
    private Animation animation;
    private AnimationEngine engine;
    //private WSLPlayer wslPlayer;
    private Data data;

    //GUI elements
    private FloatBox sphereWavelengthField;
    private JButton sphereResetButton;
    private StateButton spherePauseButton;
    private StateButton sphereHideButton;

    private FloatBox planeWavelengthField;
    private JButton planeResetButton;
    private StateButton planePauseButton;
    private StateButton planeHideButton;
    //private FloatBox phiField;
   // private FloatBox thetaField;
    private JTabbedPane tabbedPane;
    private JPanel spherePanel, sphereTop, sphereBottom; 


    ScalarCoupler wavelengthCoupler;
    ScalarCoupler wavelengthCoupler2;
    ScalarCoupler phiCoupler;
    ScalarCoupler thetaCoupler;
    
    PlaneTransform planeTransform;
    
    //GUI scripting stuff
   // private NumberBoxScripter	wavelengthScripter; /*, phiFieldScripter, thetaFieldScripter;*///[JD]
    private NumberBoxScripter sphereWavelengthScripter, planeWavelengthScripter; 
    private StateButtonScripter	spherePauseScripter, planePauseScripter, sphereHideScripter, planeHideScripter;//[JD]
    private FolderPanelScripter panelScripter;
    private ButtonScripter sphereResetScripter, planeResetScripter; //[JD]

    //Constants
    public static final float	WAVELENGTHDEF=600;
    public static final float	WAVELENGTHMIN=400;
    public static final float	WAVELENGTHMAX=800;
    public static final int     ANIMATION_PERIOD=30;
    public static final int	SPHEREMODE=0;
    public static final int	PLANEMODE=1;
    public static final float 	PI = (float)3.14159;

    //Widgets
    private WheelWidget wavelengthWidget , wavelengthWidget2;
    private WheelWidget phiWidget;
    private WheelWidget thetaWidget;
    
    //Other vrml events
    private SFInt32 showWavelengthWidget;
    private SFInt32 showKVector;

    //Animation data
    public static final class Data implements Animation.Data,Cloneable{
            public float planeWavelength, sphereWavelength;
            public int mode;
            public Animation.Data copy() {
                    try {
                            return (Data)clone();		//all data is primitive; clone() is fine
                    } catch(CloneNotSupportedException e) {return null;}	//can't happen
            }
    }

    public static void main(String[] args) {
        WaveFront waveFront = new WaveFront("Wavefronts", "/org/webtop/x3dscene/wavefront.x3dv");
    }

    public WaveFront(String title, String world) {
        super(title, world, true, false);
    }

    protected Component getFirstFocus() {
        return null;
    }

    String traducir(String in) {
        return in;
    }

    protected void setupGUI() {
        sphereResetButton = new JButton(traducir("Reset"));
        //private StateButton hideButton=new StateButton(" Widgets  ",new String[] {"  Hide","  Show"},new String[] {"",""});
        spherePauseButton = new StateButton("", new String[] {traducir("Stop"),
                                            traducir("Play")}, new String[] {"",
                                            ""});
        sphereHideButton = new StateButton("", new String[] {traducir("Hide"),
                                           traducir("Show")}, new String[] {"",
                                           ""});



        sphereTop = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sphereBottom = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        spherePanel = new JPanel(); 
        spherePanel.setLayout(new VerticalLayout());
        sphereBottom.add(sphereHideButton);
        sphereBottom.add(sphereResetButton);
        sphereBottom.add(spherePauseButton);
        sphereTop.add(new JLabel("  Wavelength: "));
        sphereTop.add(sphereWavelengthField = new FloatBox(WAVELENGTHMIN,
                WAVELENGTHMAX, 45, 5));
        sphereTop.add(new JLabel("nm"));
        sphereWavelengthField.setValue(WAVELENGTHDEF);
        sphereWavelengthField.addNumberListener(this);
        spherePauseButton.addListener(this);
        sphereResetButton.addActionListener(this);
        sphereHideButton.addListener(this);
        
        spherePanel.add(sphereTop); 
        spherePanel.add(sphereBottom);

        planeResetButton = new JButton(traducir("Reset"));
        //private StateButton hideButton=new StateButton(" Widgets  ",new String[] {"  Hide","  Show"},new String[] {"",""});
        planePauseButton = new StateButton("", new String[] {traducir("Stop"),
                                           traducir("Play")}, new String[] {"",
                                           ""});
        planeHideButton = new StateButton("", new String[] {traducir("Hide"),
                                          traducir("Show")}, new String[] {"",
                                          ""});
        ////phiField = new FloatBox((float)-1.5708,(float)1.5708,0,3);
       // phiField = new FloatBox(0f,360f,0,3);
        //phiField.addNumberListener(this);
        //thetaField = new FloatBox((float)-1.5708,(float)1.5708,0,3);
       // thetaField = new FloatBox(0f,180f,0f,3);
       // thetaField.addNumberListener(this);

        JPanel planePanel = new JPanel();
        JPanel planeTop = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel planeBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        planePanel.setLayout(new VerticalLayout());
      //  planeTop.add(new JLabel("Theta:",JLabel.RIGHT));
        //planeTop.add(thetaField);
       // planeTop.add(new JLabel("Phi:",JLabel.RIGHT));
       // planeTop.add(phiField);
        planeTop.add(new JLabel("Wavelength: ",JLabel.RIGHT));
        planeTop.add(planeWavelengthField = new FloatBox(WAVELENGTHMIN,
                WAVELENGTHMAX, 45, 5));
        planeTop.add(new JLabel("nm"));
        planeBottom.add(planeHideButton);
        planeBottom.add(planeResetButton);
        planeBottom.add(planePauseButton);
        planeWavelengthField.setValue(WAVELENGTHDEF);
        planeWavelengthField.addNumberListener(this);
        planePauseButton.addListener(this);
        planeResetButton.addActionListener(this);
        planeHideButton.addListener(this);
        
        planePanel.add(planeTop);
        planePanel.add(planeBottom);
        
        
        tabbedPane = new JTabbedPane();
        tabbedPane.add("Spherical Wave", spherePanel);
        tabbedPane.add("Plane Wave",  planePanel);
        tabbedPane.addChangeListener(this);
        addToConsole(tabbedPane);


        
        //set up WSL scripting items
        sphereWavelengthScripter = new NumberBoxScripter(sphereWavelengthField,
                getWSLPlayer(), null, "shpereWavelength", new Float(WAVELENGTHDEF));
        planeWavelengthScripter = new NumberBoxScripter(planeWavelengthField, getWSLPlayer(), 
        		null, "planeWavelength", new Float(WAVELENGTHDEF));
        
        spherePauseScripter = new StateButtonScripter(spherePauseButton, getWSLPlayer(), null,
                                                "animation",
                                                new String[] {traducir("stop"),
                                                traducir("play")}, 0);
        planePauseScripter = new StateButtonScripter(planePauseButton, getWSLPlayer(), null, 
        		"planeanimation", new String[] {"Stop", "Play"}, 0);//[JD]
        panelScripter = new FolderPanelScripter(tabbedPane, getWSLPlayer(), null,
                                                "mode", null);
        sphereResetScripter = new ButtonScripter(sphereResetButton, getWSLPlayer(), null, 
        		"SphereReset");//[JD]
        planeResetScripter = new ButtonScripter(planeResetButton, getWSLPlayer(), null, 
        		"PlaneReset");//[JD]
        sphereHideScripter = new StateButtonScripter(sphereHideButton, getWSLPlayer(), null, 
        		"SphereHideWidgets", new String[] {"Hide", "Show"}, 0);//[JD]
        planeHideScripter = new StateButtonScripter(planeHideButton, getWSLPlayer(), null, 
        		"PlaneHideWidgets", new String[] {"Hide", "Show"}, 0);//[JD]
        //phiFieldScripter = new NumberBoxScripter(phiField, getWSLPlayer(), null, "phi", 
        //		new Float(0));//[JD]
       // thetaFieldScripter = new NumberBoxScripter(thetaField, getWSLPlayer(), null, "theta", 
       // 		new Float(0));  //[JD]  
        ////END WSL IMPLEMENTATIONS
    

        wavelengthCoupler = new ScalarCoupler(wavelengthWidget,sphereWavelengthField,3,new ScalarCoupler.Converter(Lambda.linear(0.04f,0f),Lambda.linear(25,0f)));
        wavelengthCoupler2 = new ScalarCoupler(wavelengthWidget2,planeWavelengthField,3,new ScalarCoupler.Converter(Lambda.linear(0.04f,0f),Lambda.linear(25,0f)));
        //phiCoupler		  = new ScalarCoupler(phiWidget, phiField, 3);
        //phiCoupler		  = new ScalarCoupler(phiWidget, phiField, 3, new ScalarCoupler.Converter(Lambda.linear(180/PI, 0f),Lambda.linear(180/PI, 0f)));
        //thetaCoupler	  = new ScalarCoupler(thetaWidget, thetaField, 3);
        
        ToolBar toolbar = getToolBar();
        toolBar.addBrowserButton("Directions", "/org/webtop/html/wavefront/default.html");
		toolBar.addBrowserButton("Theory","/org/webtop/html/wavefront/default.html");
		toolBar.addBrowserButton("Examples","/org/webtop/html/wavefront/examples.html");
		toolBar.addBrowserButton("Exercises", "/org/webtop/html/wavefront/exercises.html");
		toolBar.addBrowserButton("Images","/org/webtop/html/wavefront/images.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
        
    }

    protected void setupX3D() {
        DebugPrinter.println("setupVRML()");
        engine = new Engine(getSAI());
        animation = new Animation(engine,data,ANIMATION_PERIOD);
        
        planeTransform = new PlaneTransform(getSAI());

        wavelengthWidget=new WheelWidget(getSAI(),getSAI().getNode("WavelengthWidget"),
                           (short)2,"Turn to change the sphere wavelength.");
        //TODO: constant stuffs
        wavelengthWidget2 = new WheelWidget(getSAI(), getSAI().getNode("WavelengthWidget2"),
        					(short)3, "Turn to change the plane wavelength");
        wavelengthWidget.addListener(this);
        wavelengthWidget2.addListener(this);
        
        //phiWidget=new WheelWidget(getSAI(),getSAI().getNode("phiWidget"),(short)2,"Turn to change phi.");
       // phiWidget.addListener(this);
        
       // thetaWidget=new WheelWidget(getSAI(),getSAI().getNode("thetaWidget"),(short)2,"Turn to change theta.");
       // thetaWidget.addListener(this);
        
        //getManager().addHelper(new ScalarCoupler(wavelengthWidget,sphereWavelengthField,3,new ScalarCoupler.Converter(Lambda.linear(0.04f,0f),Lambda.linear(25,0f))));

        showWavelengthWidget = (SFInt32) getSAI().getInputField("WavelengthWidgetSwitch",
        "set_whichChoice");
        showKVector = (SFInt32) getSAI().getInputField("kVectorSwitch", "set_whichChoice");
        //showWavelengthWidget = (SFInt32) getSAI().getInputField("WavelengthWidgetSwitch","set_whichCoice");
        //NamedNode test1 = getSAI().getNode("WavelengthWidgetSwitch");
        //showWavelengthWidget = (SFInt32) getSAI().getInputField(test1,"whichChoice");

    }

    protected void setupMenubar() {
    }

    protected void setDefaults() {
        data=new Data();
        data.planeWavelength=WAVELENGTHDEF;
        data.sphereWavelength = WAVELENGTHDEF;
        sphereWavelengthField.setValue(WAVELENGTHDEF);
        data.mode=SPHEREMODE;
        animation.setData(data);
        animation.setPlaying(true);
        tabbedPane.setSelectedIndex(0);
        planeWavelengthField.setValue(WAVELENGTHDEF);
        showKVector.setValue(1);

    }


    public void actionPerformed(ActionEvent e) {setDefaults();}

    public void stateChanged(StateButton button, int i) {
            DebugPrinter.println("stateChanged()");
            System.out.println("stateChanged()");
            if(button==spherePauseButton || button==planePauseButton) {
                    DebugPrinter.println("Pause button event received");
                    animation.setPaused(i==1);
            }
            else if(button==sphereHideButton || button==planeHideButton) {
                    if(button.getState()==0) {
                            System.out.println("State 0");
                            showWavelengthWidget.setValue(2);
                    }
                    else {
                            System.out.println("State 1");
                            if(tabbedPane.getSelectedIndex() == 0)
                            	showWavelengthWidget.setValue(0);
                            else if(tabbedPane.getSelectedIndex() == 1)
                            	showWavelengthWidget.setValue(1);
                            
                    }
            }
    }

    public void stateChanged(ChangeEvent e) {
        JTabbedPane tabSource = (JTabbedPane) e.getSource();
        String tab = tabSource.getTitleAt(tabSource.getSelectedIndex());
        if(tab.equals("Spherical Wave")) {
            data.mode=0;
            animation.setData(data);
            animation.update();
            showKVector.setValue(1);
            showWavelengthWidget.setValue(0);
        }
        else if(tab.equals("Plane Wave")) {
            data.mode=1;
            animation.setData(data);
            animation.update();
            planeTransform.setTheta(0f);
            planeTransform.setPhi(0f);
            showKVector.setValue(0);
            showWavelengthWidget.setValue(1);
        }
    }

    public void numChanged(NumberBox source, Number newVal) {
            if(source == sphereWavelengthField) {
            	System.out.println("sphereWavelengthField Changed to: " + newVal.floatValue());
            	data.sphereWavelength=newVal.floatValue();
            	animation.setData(data);
            	animation.update();
            }
            else if(source == planeWavelengthField) {
            	System.out.println("planeWavelength Changed to: " + newVal.floatValue());
            	data.planeWavelength=newVal.floatValue();
            	animation.setData(data);
            	animation.update();
            }
           /* else if(source == phiField) {
            	System.out.println("phi changed");
            	planeTransform.setPhi(newVal.floatValue()*PI/180);
            }
            else if(source == thetaField) {
            	planeTransform.setTheta(newVal.floatValue()*PI/180);
            }*/
            
    }

    public void invalidEntry(NumberBox source,Number newVal) {
            //this.showStatus(traducir("invalid_entry"));
    	DebugPrinter.println("invalidEntry("+newVal+')');
    }

    public void readableFieldChanged(X3DFieldEvent e) {
    	
    	
    	
    }
    
    public void invalidEvent(String p1, String p2) {


    }

    public void boundsForcedChange(NumberBox source,Number oldVal) {}

    
    
    
	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
		//wavelengthScripter, pauseScripter, panelScripter
		//wavlengthScripter, pauseScripter, panelScripter, 
		//sphereHideScripter, planeHideScripter, phiFieldScripter, thetaFieldScripter
		sphereWavelengthScripter.addTo(node);
		planeWavelengthScripter.addTo(node);
		spherePauseScripter.addTo(node);//[JD]
		planePauseScripter.addTo(node);//[JD]
		//panelScripter.addTo(node);
		sphereHideScripter.addTo(node);//[JD]
		planeHideScripter.addTo(node);//[JD]
		//phiFieldScripter.addTo(node);//[JD]
		//thetaFieldScripter.addTo(node);//[JD]
	        panelScripter.addTo(node);  	
	}
    
	
	public String getWSLModuleName() {
		
		return "Wavefront";
	}

	//The following methods aren't necessary...at least I think they're not [JD]
	public void initialize(WSLScriptEvent event){
		
	}
	
	
	public void scriptActionFired(WSLScriptEvent event){
		
	}



	private class PlaneTransform {
		private final SFRotation phiRotation;

		private final SFRotation thetaRotation;

		public PlaneTransform(SAI sai) {
			phiRotation = (SFRotation) sai.getInputField("PlaneRotationPhi",
					"set_rotation");
			thetaRotation = (SFRotation) sai.getInputField(
					"PlaneRotationTheta", "set_rotation");
		}

		public void setPhi(float angle) {
			float[] rot = { 1, 0, 0, angle }; //not sure which to use
			//float[] rot = {0,0,1,angle}; //fixes theta = 0 condition, but breaks everything else
			phiRotation.setValue(rot);
			System.out.println("setPhi called: " );
		}

		public void setTheta(float angle) {
			float[] rot = { 0, 1, 0, 3.14159265f - angle }; //makes it a right handed coord system
			//float[] rot = {0,1,0,angle};
			//float[] rot = {(float) Math.sin(phiField.getValue()), 
			//		(float)Math.cos(phiField.getValue()), 0, angle};
			System.out.println("setTheta called");
			thetaRotation.setValue(rot);
		}
	}
}
