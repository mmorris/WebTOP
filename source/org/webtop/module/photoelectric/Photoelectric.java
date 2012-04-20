/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Photoelectric.java
//WebTOP applet file for the Photoelectric Effect module.
//Karolina Sarnowska & Peter Gilbert
//Created August 26 2004
//Updated June 2 2005
//Version 0.1

package org.webtop.module.photoelectric;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.web3d.x3d.sai.*;

import org.sdl.gui.numberbox.*;

import org.webtop.component.*;
import org.webtop.util.*;
//import webtop.vrml.EAI;
import org.webtop.x3d.widget.*;
import org.webtop.x3d.output.*;
import org.webtop.util.script.*;

import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;

import org.sdl.math.*;

public class Photoelectric extends WApplication implements NumberBox.Listener, StateButton.Listener,
        ActionListener, X3DFieldEventListener /*, EventOutObserver*/, WSLScriptListener

{


    protected String getModuleName() {
        return "Photoelectric";
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
        return "Aug 26 2004";
    }

    protected String getAuthor() {
        return "Karolina Sarnowska & Peter Gilbert";
    }

    //WSL/animation classes
    private Animation animation;
    private Engine engine;
    //private WSLPlayer wslPlayer;
    private Data data;

    //Constants
    private static final float WORK_FUNCTIONS[] = {4.28f, 4.98f, 4.22f, 2.87f, 5.0f, 2.9f, 5.0f,
                                                  4.65f, 5.1f, 4.5f, 4.25f, 3.66f, 4.49f, 5.15f,
                                                  4.3f, 5.65f, 2.3f, 5.9f, 4.26f, 2.75f, 3.63f,
                                                  4.33f};
    private static final float WAVELENGTH_DEF = 400;
    private static final float WAVELENGTH_MIN = 200;
    private static final float WAVELENGTH_MAX = 700;
    private static final int INTENSITY_DEF = 5;
    private static final int INTENSITY_MIN = 0;
    private static final int INTENSITY_MAX = 10;
    private static final float VOLTAGE_DEF = 0;
    private static final float VOLTAGE_MIN = 0;
    private static final float VOLTAGE_MAX = 5;
    private static final int WORK_FUNCTION_INDEX_DEF = 0;
    private static final float WORK_FUNCTION_DEF = 2.5f;
    private static final float WORK_FUNCTION_MIN = 2;
    private static final float WORK_FUNCTION_MAX = 6;
    private static final int RATE_DEF = 10;

    private static final int VOLTAGE_WIDGET = 0;
    private static final int WORK_FUNCTION_DRAGGING = 1;

    //user defined work function?
    private static int USER = 0;

    //how do these fit in?
    private static final int ANIMATION_PERIOD = 25;
    private boolean dragging = false;


    //GUI elements
    private FloatBox wavelengthField;
    private FloatBox intensityField;
    private IntBox electronRateField;
    private FloatBox workFunctionField;
    private FloatBox voltageField;

    private JLabel photonEnergyLabel, kMaxLabel, currentLabel;

    private JButton resetButton, clearButton;
    private StateButton animationButton, hideWidgetsButton;

    //private Choice elementChoice;
    private JComboBox workFunctionList;


    //GUI Scripting Elements
      private NumberBoxScripter wavelengthScripter;
      private NumberBoxScripter intensityScripter;
      private NumberBoxScripter workFunctionScripter;
      private NumberBoxScripter voltageScripter;

      private ButtonScripter resetScripter;
      private ButtonScripter clearScripter;
      private StateButtonScripter hideWidgetsScripter;
      private StateButtonScripter animationScripter;
      private ChoiceScripter listScripter;

      private ScalarScripter wavelengthWidgetScripter;
      private ScalarScripter intensityWidgetScripter;
      private ScalarScripter workFunctionWidgetScripter;

    //Couplers
    ScalarCoupler wavelengthCoupler, intensityCoupler, workFunctionCoupler;

    //VRML
    private SFFloat transparency;
    private WheelWidget wavelengthWidget;
    private XDragWidget intensityWidget;
    private XDragWidget workFunctionWidget;
    private SFColor lightColor;
    private SFFloat set_intensity;
    private SFFloat set_workfunction;
    private SFFloat set_voltage;
    private SFFloat voltage_in;
    private SFBool work_function_dragging;

    //vrml for hiding widgets
    private SFInt32 showWavelengthWidget;
    private SFInt32 showIntensityWidget;
    private SFInt32 showWorkFunctionWidget;

    //??Animation data
    public static final class Data implements Animation.Data, Cloneable {
        public float wavelength;
        public float intensity;
        public float workFunction;
        public float voltage;

        public Animation.Data copy() {
            try {
                return (Data) clone(); //all data is primitive; clone() is fine
            } catch (CloneNotSupportedException e) {
                return null;
            } //can't happen
        }
    }


    public Photoelectric(String title, String world) {
        super(title, world);
    }

    protected Component getFirstFocus() {
        return wavelengthField;
    }


    protected void setupGUI() {
        //setLayout(new BorderLayout());
        //add(getStatusBar(),BorderLayout.CENTER);
        //add(getWSLPanel(),BorderLayout.SOUTH);

        //JPanel panel;
        //panel = new JPanel();
        //panel.setSize(100,100);
        //panel.setBounds(0,0,10,10);
        controlPanel.setLayout(new VerticalLayout());


        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel middle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));

        top.add(new JLabel("Wavelength:", JLabel.RIGHT));
        top.add(wavelengthField = new FloatBox(WAVELENGTH_MIN, WAVELENGTH_MAX, 45, 5));
        top.add(new JLabel("nm"));

        top.add(new JLabel("Intensity:", JLabel.RIGHT));
        top.add(intensityField = new FloatBox(INTENSITY_MIN, INTENSITY_MAX, 45, 5));
        top.add(new Label(" "));

        //setup dropdown menus for work function elements
        String[] workFunctions = {"User Defined", "Aluminum", "Beryllium", "Cadmium",
                                 "Calcium", "Carbon", "Cesium", "Cobalt", "Copper", "Gold", "Iron",
                                 "Lead", "Magnesium", "Mercury", "Nickel", "Niobium", "Platinum",
                                 "Potassium", "Selenium", "Silver", "Sodium", "Uranium", "Zinc"};
        workFunctionList = new JComboBox(workFunctions);

        top.add(workFunctionList);

        top.add(new JLabel("Work function:", JLabel.RIGHT));
        top.add(workFunctionField = new FloatBox(WORK_FUNCTION_MIN, WORK_FUNCTION_MAX, 45,
                                                 5));
        top.add(new JLabel("eV"));
        //workFunctionField.setEnabled(false);
        workFunctionField.setEditable(false);

        top.add(new JLabel("Voltage", JLabel.RIGHT));
        top.add(voltageField = new FloatBox(VOLTAGE_MIN, VOLTAGE_MAX, 45, 5));
        top.add(new JLabel("volts"));

        middle.add(new JLabel("Photon energy:", JLabel.RIGHT));
        middle.add(photonEnergyLabel = new JLabel("0.0"));
        photonEnergyLabel.setHorizontalAlignment(JLabel.RIGHT);
        middle.add(new JLabel("eV"));

        middle.add(new JLabel("Kmax:", JLabel.LEFT));
        middle.add(kMaxLabel = new JLabel("0.0"));
        kMaxLabel.setHorizontalAlignment(JLabel.RIGHT);
        middle.add(new JLabel("eV"));

        middle.add(new JLabel("Current:", JLabel.RIGHT));
        middle.add(currentLabel = new JLabel("0.0"));
        middle.add(new JLabel(" "));

        resetButton = new JButton("Reset");
        clearButton = new JButton("Clear");
        animationButton = new StateButton("", new String[] {"Start", "Stop"},
                                          new String[] {"", ""});
        hideWidgetsButton = new StateButton("Widgets", new String[] {"Hide ",
                                            "Show "}, new String[] {"", ""});

        bottom.add(resetButton);
        bottom.add(clearButton);
        bottom.add(hideWidgetsButton);
        bottom.add(animationButton);

        controlPanel.add(top);
        controlPanel.add(middle);
        controlPanel.add(bottom);

        wavelengthCoupler = new ScalarCoupler(wavelengthWidget, wavelengthField, 1,
                                              new
                                              ScalarCoupler.Converter(Lambda.linear(1.0f, 0f),
                Lambda.linear(1f, 0f)));
        intensityCoupler = new ScalarCoupler(intensityWidget, intensityField, 1,
                                             new ScalarCoupler.
                                             Converter(Lambda.linear(19f, 100f),
                Lambda.linear(1f / 19f, -100f / 19f)));
        workFunctionCoupler = new ScalarCoupler(workFunctionWidget, workFunctionField, 1,
                                                new
                                                ScalarCoupler.Converter(Lambda.linear(350f / 4f,
                -700f / 4f), Lambda.linear(4f / 350f, 2f)));

        data = new Data();

        wavelengthField.addNumberListener(this);
        intensityField.addNumberListener(this);
        voltageField.addNumberListener(this);
        workFunctionField.addNumberListener(this);
        workFunctionList.addActionListener(this);
        resetButton.addActionListener(this);
        clearButton.addActionListener(this);
        hideWidgetsButton.addListener(this);
        animationButton.addListener(this);

        setDefaultValues();

        //scripters
          wavelengthScripter = new NumberBoxScripter(wavelengthField,getWSLPlayer(),null,"wavelength",new Float(WAVELENGTH_DEF));
          intensityScripter = new NumberBoxScripter(intensityField,getWSLPlayer(),null,"intensity",new Float(INTENSITY_DEF));
           workFunctionScripter = new NumberBoxScripter(workFunctionField,getWSLPlayer(),null,"workFunction",new Float(WORK_FUNCTION_DEF));
           voltageScripter = new NumberBoxScripter(voltageField,getWSLPlayer(),null,"voltage",new Float(VOLTAGE_DEF));

           resetScripter = new ButtonScripter(resetButton,getWSLPlayer(),null,"reset");
           clearScripter = new ButtonScripter(clearButton,getWSLPlayer(),null,"clear");
           hideWidgetsScripter = new StateButtonScripter(hideWidgetsButton,getWSLPlayer(),null,"hideWidgets",new String[] {"Hide ","Show "},1);
           animationScripter = new StateButtonScripter(animationButton,getWSLPlayer(),null,"animation",new String[] {"Start","Stop"},1);
           listScripter = new ChoiceScripter(workFunctionList, getWSLPlayer(), null, "list", new String[] {"User Defined","Aluminum", "Beryllium", "Cadmium", "Calcium", "Carbon", "Cesium", "Cobalt", "Copper", "Gold", "Iron", "Lead", "Magnesium", "Mercury", "Nickel", "Niobium", "Platinum", "Potassium","Selenium","Silver", "Sodium","Uranium","Zinc"}, 1, null);
    
           //Set up the toolbar
           ToolBar toolbar = getToolBar();
           toolbar.addBrowserButton("Directions", "/org/webtop/html/photoelectric/directions.html");
           toolbar.addBrowserButton("Theory", "/org/webtop/html/photoelectric/theory.html");
		   toolbar.addBrowserButton("Examples", "/org/webtop/html/photoelectric/examples.html");
		   toolbar.addBrowserButton("Exercises", "/org/webtop/html/photoelectric/exercises.html");
		   toolBar.addBrowserButton("Images","/org/webtop/html/photoelectric/images.html");
		   toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
	}

    public void setWorkFunctionValue(int index) {
        workFunctionField.setValue(WORK_FUNCTIONS[index - 1]);
    }

    protected void setupX3D() {
        engine = new Engine(this, getSAI());
        animation = new Animation(engine, data, ANIMATION_PERIOD);
        //transparency=(EventInSFFloat)getEAI().getEI(getEAI().getNode("TRANS_WORKER"),"transparency_in");
        engine.setRate(INTENSITY_DEF);
        lightColor = (SFColor) getSAI().getInputField(getSAI().getNode("LightPencilMaterial"),
                "diffuseColor");
        set_intensity = (SFFloat) getSAI().getInputField(getSAI().getNode("LightPencilMaterial"),
                "transparency");
        set_workfunction = (SFFloat) getSAI().getInputField(getSAI().getNode("PlateMaterial"),
                "transparency");
        set_voltage = (SFFloat) getSAI().getInputField(getSAI().getNode("Worker5"), "voltageIn");
        set_voltage.setValue(0f);
        //EAI.Try eaitry=new EAI.Try(this);
        voltage_in = (SFFloat) getSAI().getOutputField(getSAI().getNode("Worker5"), "voltageOut", this,
                new Integer(VOLTAGE_WIDGET));
        work_function_dragging = (SFBool) getSAI().getOutputField(getSAI().getNode(
                "WorkFunctionDragger"), "isActive_out", this, new Integer(WORK_FUNCTION_DRAGGING));
        setLightConeColor(WAVELENGTH_DEF);
        setLightConeIntensity(INTENSITY_DEF);
        setWorkFunction(WORK_FUNCTION_DEF);
        wavelengthWidget = new WheelWidget(getSAI(), getSAI().getNode("WavelengthWidget"),
                                           (short) 2, "Turn to change the wavelength.");
        //wavelengthWidget.addListener(this);
        intensityWidget = new XDragWidget(getSAI(), getSAI().getNode("IntensityDragger"), (short) 3,
                                          "Drag to change the intensity.");
        //intensityWidget.addListener(this);
        workFunctionWidget = new XDragWidget(getSAI(), getSAI().getNode("WorkFunctionDragger"),
                                             (short) 4, "Drag to change the work function.");
        //workFunctionWidget.addListener(this);
        workFunctionWidget.setEnabled(false);
        //getManager().addHelper(new ScalarCoupler(wavelengthWidget,wavelengthField,1,new ScalarCoupler.Converter(Lambda.linear(1.0f,0f),Lambda.linear(1f,0f))));
        //getManager().addHelper(new ScalarCoupler(intensityWidget,intensityField,1,new ScalarCoupler.Converter(Lambda.linear(19f,100f),Lambda.linear(1f/19f,-100f/19f))));
        //getManager().addHelper(new ScalarCoupler(workFunctionWidget,workFunctionField,1,new ScalarCoupler.Converter(Lambda.linear(350f/4f,-700f/4f),Lambda.linear(4f/350f,2f))));

        wavelengthWidgetScripter = new ScalarScripter(wavelengthWidget,getWSLPlayer(),null,"wavelengthdrag",WAVELENGTH_DEF);
           intensityWidgetScripter = new ScalarScripter(intensityWidget,getWSLPlayer(),null,"intensitydrag",INTENSITY_DEF);
           workFunctionWidgetScripter = new ScalarScripter(workFunctionWidget,getWSLPlayer(),null,"workFunctiondrag",WORK_FUNCTION_DEF);

        showWavelengthWidget = (SFInt32) getSAI().getInputField("wavelengthWidgetSwitch",
                "set_whichChoice");
        showIntensityWidget = (SFInt32) getSAI().getInputField("intensityDraggerSwitch",
                "set_whichChoice");
        showWorkFunctionWidget = (SFInt32) getSAI().getInputField("workFunctionDraggerSwitch",
                "set_whichChoice");
    }

    protected void setWidgetsEnabled(boolean yes) {}

    protected void setGUIEnabled(boolean yes) {}

    protected void setDefaults() {
        animation.setData(data);
        //animation.setPaused(true);
        animationButton.setState(1);
        animation.setPlaying(true);
        workFunctionField.setEditable(true);
        workFunctionWidget.setEnabled(true);
    }

    public void setDefaultValues() {
        data.wavelength = WAVELENGTH_DEF;
        data.intensity = INTENSITY_DEF;
        data.workFunction = WORK_FUNCTION_DEF;
        data.voltage = VOLTAGE_DEF;

        wavelengthField.setValue(WAVELENGTH_DEF);
        intensityField.setValue(INTENSITY_DEF);
        workFunctionField.setValue(WORK_FUNCTION_DEF);
        voltageField.setValue(VOLTAGE_DEF);
        workFunctionList.setSelectedIndex(WORK_FUNCTION_INDEX_DEF);
        //setWorkFunctionValue(WORK_FUNCTION_INDEX_DEF);

        updateKMaxLabel();
        updatePhotonEnergyLabel();
    }

    public void setLightConeColor(float wavelength) {
        float[] target = new float[3];
        WTMath.hls2rgb(target, WTMath.hue(wavelength), .5f, 1f);
        lightColor.setValue(target);
    }

    public void setLightConeIntensity(float intensity) {
        set_intensity.setValue(1f - 0.6f * (intensity / INTENSITY_MAX));
    }

    public void setWorkFunction(float workFunction) {
        set_workfunction.setValue(0.8f - 0.8f * (workFunction / WORK_FUNCTION_MAX));
    }

    public float getVoltage() {
        return voltageField.getValue();
    }

    public Engine getEngine() {
        return engine;
    }


    public void hideWidgets(boolean t) {
        if (t) {
            showWavelengthWidget.setValue(0);
            showIntensityWidget.setValue(0);
            showWorkFunctionWidget.setValue(0);
        } else {
            showWavelengthWidget.setValue( -1);
            showIntensityWidget.setValue( -1);
            showWorkFunctionWidget.setValue( -1);
        }
    }


    public void setCurrent(float current) {
        currentLabel.setText(String.valueOf(current));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == resetButton) {
            animation.setPaused(true);
            animationButton.setState(1);
            engine.removeAllElectrons();
            setCurrent(0);
            setDefaultValues();
            animation.setPaused(false);
        } else if (e.getSource() == clearButton) {
            engine.removeAllElectrons();
        } else if (e.getSource() == workFunctionList) {
            int index = workFunctionList.getSelectedIndex();
            System.out.println(index);

            if (index == USER) {
                //workFunctionField.setEnabled(true);
                workFunctionField.setEditable(true);
                workFunctionWidget.setEnabled(true);
            } else {
                //workFunctionField.setEnabled(false);
                workFunctionField.setEditable(false);
                workFunctionWidget.setEnabled(false);
                setWorkFunctionValue(index);
            }

        }
    }

    public void stateChanged(StateButton button, int k) {
        if (button == animationButton) {
            animation.setPaused(k == 0);
        } else if (button == hideWidgetsButton) {
            System.out.println("hide");
            hideWidgets(k == 0);
        }
    }

    public void updatePhotonEnergyLabel() {
        photonEnergyLabel.setText(String.valueOf(FPRound.showZeros(1240f / wavelengthField.getValue(),
                2)));
    }

    public void updateKMaxLabel() {
        float kMax = 1240f / wavelengthField.getValue() - workFunctionField.getValue();
        if (kMax > 0) {
            kMaxLabel.setText(FPRound.showZeros(kMax, 2));
        } else {
            kMaxLabel.setText("0.00");
        }
    }

    public void numChanged(NumberBox source, Number newVal) {
        if (source == wavelengthField) {
            data.wavelength = newVal.floatValue();
            setLightConeColor(newVal.floatValue());
            updatePhotonEnergyLabel();
            updateKMaxLabel();
        } else if (source == intensityField) {
            data.intensity = newVal.floatValue();
            setLightConeIntensity(newVal.floatValue());
            engine.setRate(newVal.floatValue());
        } else if (source == workFunctionField) {
            data.workFunction = newVal.floatValue();
            setWorkFunction(newVal.floatValue());
            engine.removeAllElectrons();
            updateKMaxLabel();
        } else if (source == voltageField) {
            data.voltage = newVal.floatValue();
            if (!voltageDragging) {
                set_voltage.setValue(newVal.floatValue());
            }

        }
        animation.setData(data);
    }

    public void invalidEvent(String node, String event) {
    }

    public void invalidEntry(NumberBox source, Number newVal) {
        //this.showStatus("Invalid Entry");
        DebugPrinter.println("invalidEntry(" + newVal + ')');
    }

    public void boundsForcedChange(NumberBox source, Number oldVal) {}

    boolean voltageDragging = false;

    public void readableFieldChanged(X3DFieldEvent e) {
        if (e.getData() instanceof Integer) {
            int mode = ((Integer) e.getData()).intValue();
            if (mode == VOLTAGE_WIDGET) {
                voltageDragging = true;
                voltageField.setValue(FPRound.toSigVal(((SFFloat) e.getSource()).getValue(), 2));
                voltageDragging = false;
            } else if (mode == WORK_FUNCTION_DRAGGING) {
                setDragging(((SFBool) e.getSource()).getValue());
            }
        }
    }

    public void setDragging(boolean d) {
        if (d) {
            animation.setPaused(true);
        } else {
            animation.setPaused(false);
        }
    }


    // -----------------------------------------------------------------------------------
    // WSL Routines
    // -----------------------------------------------------------------------------------

    public String getWSLModuleName() {
        return "photoelectric";
    }

    protected void toWSLNode(WSLNode node) {
     super.toWSLNode(node);
     wavelengthScripter.addTo(node);
     intensityScripter.addTo(node);
     workFunctionScripter.addTo(node);
     voltageScripter.addTo(node);

     hideWidgetsScripter.addTo(node);
     animationScripter.addTo(node);
     listScripter.addTo(node);
      }
    
    public void initialize(WSLScriptEvent event){
    	
    }
   
    public void scriptActionFired(WSLScriptEvent event){
    	
    }
    
    //*******END WSL ROUTINES************//

    public static void main(String[] args) {
        Photoelectric photoelectric = new Photoelectric("Photoelectric Effect",
                "/org/webtop/x3dscene/photoelectric.x3dv");
    }

    protected void setupMenubar() {
    }

    public void toolTip(Tooltip src, String tip) {
    }

    public void mouseEntered(Widget src) {
    }

    public void mouseExited(Widget src) {
    }

    public void mousePressed(Widget src) {
    }

    public void mouseReleased(Widget src) {
    }


}
