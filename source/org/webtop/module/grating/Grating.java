/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.grating;
//anything with the comments "old" was used when user defined was still an option of the drop down menu.  Do
//Not Delete!
//anything with the comments "new" was used when implementing the temporary alamo solution.  Delete when putting 
//user defined back into the drop down menu
/**
 * <p>Title: X3DWebTOP</p>
 *
 * <p>Description: The X3D version of The Optics Project for the Web
 * (WebTOP)</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: MSU Department of Physics and Astronomy</p>
 *
 * @author Jeremy Davis, Paul Cleveland, Peter Gilbert
 * @version 0.0
 */

import org.webtop.component.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import java.util.Vector;
import java.util.Iterator;
import org.sdl.gui.numberbox.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.ChangeListener;

import org.webtop.x3d.widget.*;
import org.webtop.x3d.output.*;
import org.web3d.x3d.sai.*;
import org.webtop.util.WTMath;
import org.sdl.math.FPRound;
import org.webtop.util.WTString;
import org.webtop.x3d.NamedNode;
import javax.swing.event.ListSelectionEvent;

import org.webtop.util.script.*;
import org.webtop.wsl.client.*;
import org.webtop.wsl.script.*;


//finished...except you have to have values in the FloatBoxes for wavelengths
public class Grating extends WApplication implements NumberBox.Listener, ActionListener, SpatialWidget.Listener,
        ItemListener, ChangeListener{

	public Grating(String title, String world) {
        super(title,world,true, false);
    }

    public static void main(String[] args) {
        Grating grating = new Grating("TransmissionGrating", "/org/webtop/x3dscene/grating.x3dv");
    }

    
	protected String getModuleName() {
        return "transmissionGrating";
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
        return "Jeremy Davis";
    }


    //Array indices (not atomic numbers!)
    private static final int HELIUM = 0, HYDROGEN = 1, MERCURY = 2, SODIUM = 3, USER = 4, DEF_LAMP =
            HELIUM;
    private static final float[][] BUILTIN_LINES = { {438.8f, 447.1f, 471.3f, 492.2f, 501.6f,
            504.8f, 587.6f, 667.8f, 706.5f}, {410.2f, 434.1f, 486.1f, 656.3f}, {404.7f, 433.9f,
            434.8f, 546.1f, 577.0f, 579.0f, 615.0f, 690.8f}, {589.0f, 589.6f}
    },
    BUILTIN_STRENGTHS = { {0.0167f, 0.375f, 0.0567f, 0.0333f, 0.167f, 0.0167f, 1.000f, 0.167f,
            0.383f}, {0.050f, 0.100f, 0.267f, 1.000f}, {0.409f, 0.0568f, 1.000f, 0.250f, 0.0545f,
            0.0864f, 0.227f, 0.0568f}, {1.000f, 0.500f}
    };
 
    private static final float THETA_STEP = 0.2f; // degrees
    private static final int POINT_COUNT = 2 + (int) (360 / THETA_STEP), FACE_COUNT = (POINT_COUNT -
            2) / 2;

    //All in microns:
    private static final float MIN_WIDTH = .1f, MAX_WIDTH = 1, DEF_WIDTH = .1f, MIN_DISTANCE = 1,
            MAX_DISTANCE = 4, DEF_DISTANCE = 2;
    private static final int MIN_SLITS = 30, MAX_SLITS = 200, DEF_SLITS = 50, MIN_INTENSITY = 1,
            MAX_INTENSITY = 15, DEF_INTENSITY = 1;
    
    private int whichPane = 0; //new
 
    // how much is one scrollbar tick?
    private static final float INTENSITY_SCALE = 0.5f;
    
    //used in userRenderNew
    private float tempUserLines[] = new float[6], tempUserStrengths[]= new float[6];

    private IndexedSet ifs;
    private TouchSensor ts;

    private JPanel panel;
    private JPanel topPanel;
    private JPanel middlePanel;
    private JPanel bottomPanel;

    private IntBox numberOfSlits;
    private FloatBox slitWidth;
    private FloatBox slitDistance;
    //What to do with this horrid thing?  [Davis] I agree [JD]
    private NumberBox wavelength;

    private JButton addButton, removeButton, resetButton;
    private JTabbedPane tabbedPane;//new
    private JPanel defaultPanel; //new
    private JPanel userPanel; //new
    private JPanel userTopPanel; //new
    private JPanel userMiddlePanel; //new
    private JPanel userBottomPanel; //new
    private IntBox userNumberOfSlits;//new
    private FloatBox userSlitWidth;//new
    private FloatBox userSlitDistance; //new
    private FloatBox lambda1,lambda2,lambda3, lambda4, lambda5, lambda6; //new
    private JScrollBar userIntensity;//new

    //The real maximum value for a scrollbar is maximum-visible; visible must be
    //at least 1.
    JScrollBar intensity;
    private JLabel intensityLabel;// for sizing
    private JLabel userIntensityLabel; //new
    private JButton userResetButton; //new

    private JComboBox lampChoice;
    private JList wavelengthList;
    private DefaultListModel wavelengthListModel;
    private JScrollPane wavelengthPane;
    
    private Vector addVec = new Vector();//Used in addWavelength() to add user wavelengths [JD]
    private Iterator itr = addVec.iterator(); //Used in addWavelength() to add user waves on playback [JD]
    private Vector removeVec = new Vector();//Used in removeWavelength()to remove user wavelengths [JD]
    private Iterator itr2 = removeVec.iterator();//Used in removeWavelength() to remove user wavelengths on playback [JD]
    
    //Scripting Elements
    private ButtonScripter addScripter, removeScripter, resetScripter, userResetScripter;
    private NumberBoxScripter numberOfSlitsScripter, slitWidthScripter, slitDistanceScripter, wavelengthScripter;
    private NumberBoxScripter userNumberOfSlitsScripter, userSlitWidthScripter, userSlitDistanceScripter;
    private NumberBoxScripter l1Scripter, l2Scripter, l3Scripter, l4Scripter, l5Scripter, l6Scripter;
    private ScrollbarScripter intensityScripter, userIntensityScripter;
    private ChoiceScripter lampChoiceScripter;
    private FolderPanelScripter panelScripter; //new

    protected Component getFirstFocus() {
        return null;
    }

    protected void setupGUI() {
    	
    	//panel = new JPanel(new GridBagLayout());//old
    	defaultPanel = new JPanel(new GridBagLayout());//new
    	userPanel = new JPanel(new GridBagLayout());//new
    	topPanel = new JPanel(new FlowLayout());
    	middlePanel = new JPanel(new FlowLayout());
    	bottomPanel = new JPanel(new FlowLayout());

        numberOfSlits=new IntBox(MIN_SLITS,MAX_SLITS,DEF_SLITS,4);
        slitWidth=new FloatBox(MIN_WIDTH,MAX_WIDTH,DEF_WIDTH,4);
        slitDistance=new FloatBox(MIN_DISTANCE,MAX_DISTANCE,DEF_DISTANCE,0);

        wavelength=new FloatBox(MIN_WAVELENGTH,MAX_WAVELENGTH,400f,4);

        addButton=new JButton("Add Line");
        addButton.setEnabled(false);
        removeButton=new JButton("Remove Line");
        removeButton.setEnabled(false);
        resetButton=new JButton("Reset");
        intensity=new JScrollBar(JScrollBar.HORIZONTAL,DEF_INTENSITY,1,MIN_INTENSITY,MAX_INTENSITY+1);
        intensity.setSize(20,200);
        
        intensity.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
            	displayIntensity();
                update();
            }
        });
       
        intensityLabel=new JLabel("Intensity factor:  "+MAX_INTENSITY);	// for sizing

        lampChoice=new JComboBox();
        
        wavelengthListModel =  new DefaultListModel();
        wavelengthList = new JList(wavelengthListModel);
        
        wavelengthPane = new JScrollPane(wavelengthList);
        wavelengthPane.setPreferredSize(new Dimension(100,100));

        controlPanel.setLayout(new VerticalLayout());
       

       

        numberOfSlits.addNumberListener(this);
        slitWidth.addNumberListener(this);
        slitDistance.addNumberListener(this);

        lampChoice.addItem("Helium");
        lampChoice.addItem("Hydrogen");
        lampChoice.addItem("Mercury");
        lampChoice.addItem("Sodium");
        //lampChoice.addItem("User Defined"); //old
       
       
        //First row of elements
        
        /*panel.add(new JLabel("Number of Grooves:",JLabel.RIGHT), new GridBagConstraints(0, 0, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));  
        panel.add(numberOfSlits, new GridBagConstraints(1, 0, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));   
        panel.add(new JLabel("Groove Width",JLabel.RIGHT), new GridBagConstraints(2, 0, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(slitWidth, new GridBagConstraints(3, 0, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(new JLabel("microns"), new GridBagConstraints(4, 0, 1, 1, 0,0
               , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(new JLabel("Distance:",JLabel.RIGHT), new GridBagConstraints(5, 0, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(slitDistance, new GridBagConstraints(6, 0, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(new JLabel("microns"), new GridBagConstraints(7, 0, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));*/
        topPanel.add(new JLabel("Number of Grooves:",JLabel.RIGHT));
        topPanel.add(numberOfSlits);
        topPanel.add(new JLabel("Groove Width",JLabel.RIGHT));
        topPanel.add(slitWidth);
        topPanel.add(new JLabel("microns"));
        topPanel.add(new JLabel("Distance:",JLabel.RIGHT));
        topPanel.add(slitDistance);
        topPanel.add(new JLabel("microns"));
        
        
        
        
        //Second row of elements
        /*panel.add(lampChoice, new GridBagConstraints(1, 0, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(wavelength, new GridBagConstraints(1, 1, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(addButton, new GridBagConstraints(0, 2, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(removeButton, new GridBagConstraints(0, 2, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(resetButton, new GridBagConstraints(0, 2, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));*/
        middlePanel.add(lampChoice);
       // middlePanel.add(wavelength);//old
        //middlePanel.add(addButton);//old
       // middlePanel.add(removeButton);//old
        middlePanel.add(resetButton);
        middlePanel.add(intensity);//new
        middlePanel.add(intensityLabel);//new
        
        //Third row of elements
        
        /*panel.add(new JLabel("Wavelength:",JLabel.RIGHT), new GridBagConstraints(0, 2, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(wavelengthPane, new GridBagConstraints(0, 2, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(new JLabel("nm",JLabel.RIGHT), new GridBagConstraints(0, 2, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(intensity, new GridBagConstraints(0, 2, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(intensityLabel, new GridBagConstraints(0, 2, 1, 1, 0,0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));*/
        bottomPanel.add(new JLabel("Wavelength:",JLabel.RIGHT));
        bottomPanel.add(wavelengthPane);
        bottomPanel.add(new JLabel("nm",JLabel.RIGHT));
       // bottomPanel.add(intensity);//old
       // bottomPanel.add(intensityLabel);//old

        
       /* panel.add(topPanel, new GridBagConstraints(0, 0, 1, 1, 100,100
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(middlePanel, new GridBagConstraints(0, 1, 1, 1, 100,100
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        panel.add(bottomPanel, new GridBagConstraints(0, 2, 1, 1, 100,20
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));*/ //old
        
        //new
        defaultPanel.add(topPanel, new GridBagConstraints(0, 0, 1, 1, 100,100
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        defaultPanel.add(middlePanel, new GridBagConstraints(0, 1, 1, 1, 100,100
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        defaultPanel.add(bottomPanel, new GridBagConstraints(0, 2, 1, 1, 100,20
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        //end new
        
        //start on the user defined panel //new
        //new until end new is encountered
        userTopPanel = new JPanel(new FlowLayout());
        userMiddlePanel = new JPanel(new FlowLayout()); 
        userBottomPanel = new JPanel(new FlowLayout());
       
        userNumberOfSlits=new IntBox(MIN_SLITS,MAX_SLITS,DEF_SLITS,4);
        userSlitWidth=new FloatBox(MIN_WIDTH,MAX_WIDTH,DEF_WIDTH,4);
        userSlitDistance=new FloatBox(MIN_DISTANCE,MAX_DISTANCE,DEF_DISTANCE,0);

        userIntensity=new JScrollBar(JScrollBar.HORIZONTAL,DEF_INTENSITY,1,MIN_INTENSITY,MAX_INTENSITY+1);
        userIntensity.setSize(20,200);
        
        userIntensity.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
            	displayIntensity();
                update();
            }
        });
        userTopPanel.add(new JLabel("Number of Grooves:",JLabel.RIGHT));
        userTopPanel.add(userNumberOfSlits);
        userTopPanel.add(new JLabel("Groove Width",JLabel.RIGHT));
        userTopPanel.add(userSlitWidth);
        userTopPanel.add(new JLabel("microns"));
        userTopPanel.add(new JLabel("Distance:",JLabel.RIGHT));
        userTopPanel.add(userSlitDistance);
        userTopPanel.add(new JLabel("microns"));
        
        
        
        //addToConsole(panel);//old
        
        userNumberOfSlits.addNumberListener(this);
        userSlitWidth.addNumberListener(this);
        userSlitDistance.addNumberListener(this);
        userIntensityLabel=new JLabel("Intensity factor:  "+ 0.5);//new
        userResetButton = new JButton("Reset");
        userResetButton.addActionListener(this);
        
        userMiddlePanel.add(userResetButton);
        userMiddlePanel.add(userIntensity);//new
        userMiddlePanel.add(userIntensityLabel);//new
        
        //create all 6 of the lambda boxes here and add them to the bottom panel/s
        lambda1 = new FloatBox(MIN_WAVELENGTH, MAX_WAVELENGTH, 400.1f, 4);
        lambda2 = new FloatBox(MIN_WAVELENGTH, MAX_WAVELENGTH, 400.1f, 4);
        lambda3 = new FloatBox(MIN_WAVELENGTH, MAX_WAVELENGTH, 400, 4);
        lambda4 = new FloatBox(MIN_WAVELENGTH, MAX_WAVELENGTH, 400, 4);
        lambda5 = new FloatBox(MIN_WAVELENGTH, MAX_WAVELENGTH, 400, 4);
        lambda6 = new FloatBox(MIN_WAVELENGTH, MAX_WAVELENGTH, 400, 4);
        
        //adding actionListeners to these...I think..need to look at actionPerformed and addWavelength() to finish
       // lambda1.addActionListener(this);
        lambda1.addNumberListener(this);
        //lambda2.addActionListener(this);
        lambda2.addNumberListener(this);
        lambda3.addActionListener(this);
        lambda3.addNumberListener(this);
        lambda4.addActionListener(this);
        lambda4.addNumberListener(this);
        lambda5.addActionListener(this);
        lambda5.addNumberListener(this);
        lambda6.addActionListener(this);
        lambda6.addNumberListener(this);
        
        
        
        userBottomPanel.add(new JLabel("Lambda1"));
        userBottomPanel.add(lambda1);
        userBottomPanel.add(new JLabel(" Lambda2"));
        userBottomPanel.add(lambda2);
        /*userBottomPanel.add(new JLabel(" Lambda3"));
        userBottomPanel.add(lambda3);
        userBottomPanel.add(new JLabel(" Lambda4"));
        userBottomPanel.add(lambda4);
        userBottomPanel.add(new JLabel(" l5"));
        userBottomPanel.add(lambda5);
        userBottomPanel.add(new JLabel(" l6"));
        userBottomPanel.add(lambda6);*/
        
        
        //still have to script and add functionality to all of these
        userPanel.add(userTopPanel, new GridBagConstraints(0, 0, 1, 1, 100,100
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        userPanel.add(userMiddlePanel, new GridBagConstraints(0, 1, 1, 1, 100,100
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        userPanel.add(userBottomPanel, new GridBagConstraints(0, 2, 1, 1, 100,20
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
        
        tabbedPane = new JTabbedPane(); 
        tabbedPane.add("Lamps", defaultPanel);
        tabbedPane.add("User Defined", userPanel);
        tabbedPane.addChangeListener(this);
        addToConsole(tabbedPane);
        //end new
        
        lampChoice.addItemListener(this);
        wavelength.addActionListener(this);
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        resetButton.addActionListener(this);
        
        //Create Scripting Elements
        addScripter = new ButtonScripter(addButton, getWSLPlayer(), null, "addButton");
        removeScripter = new ButtonScripter(removeButton, getWSLPlayer(), null, "removeButton");
        resetScripter = new ButtonScripter(resetButton, getWSLPlayer(), null, "resetButton");
        userResetScripter = new ButtonScripter(userResetButton, getWSLPlayer(), null, "userResetButton");
        numberOfSlitsScripter = new NumberBoxScripter(numberOfSlits, getWSLPlayer(), null, 
        		"slits", new Integer(4));
        userNumberOfSlitsScripter = new NumberBoxScripter(userNumberOfSlits, getWSLPlayer(), null, 
        		"userSlits", new Integer(4));
        slitWidthScripter = new NumberBoxScripter(slitWidth, getWSLPlayer(), null, 
        		"width", new Float(4.0));
        userSlitWidthScripter = new NumberBoxScripter(userSlitWidth, getWSLPlayer(), null, 
        		"userWidth", new Float(4.0));
        slitDistanceScripter = new NumberBoxScripter(slitDistance, getWSLPlayer(), null, 
        		"distance", new Float(0.0));
        userSlitDistanceScripter = new NumberBoxScripter(userSlitDistance, getWSLPlayer(), null, 
        		"userDistance", new Float(0.0));
        intensityScripter = new ScrollbarScripter(intensity, getWSLPlayer(), null, "intensity", 
        		DEF_INTENSITY, null);
        userIntensityScripter = new ScrollbarScripter(userIntensity, getWSLPlayer(), null, "userIntensity", 
        		DEF_INTENSITY, null);
        lampChoiceScripter = new ChoiceScripter(lampChoice,getWSLPlayer(), null, "lampChoice", 
        		new String[] {"Helium", "Hydrogen", "Mercury", "Sodium", "User Defined"}, DEF_LAMP, this);
        wavelengthScripter = new NumberBoxScripter(wavelength, getWSLPlayer(), null, "wavelength", null);
        
        //new
        panelScripter = new FolderPanelScripter(tabbedPane, getWSLPlayer(), null, "mode", null);
        l1Scripter = new NumberBoxScripter(lambda1,getWSLPlayer(), null, "lambda1",new Float(400.1f));
        l2Scripter = new NumberBoxScripter(lambda2, getWSLPlayer(), null, "lambda2", new Float(400.1f));
        /*l3Scripter = new NumberBoxScripter(lambda3, getWSLPlayer(), null, "lambda3", new Float(500f));
        l4Scripter = new NumberBoxScripter(lambda4, getWSLPlayer(), null, "lambda4", new Float(550f));
        l5Scripter = new NumberBoxScripter(lambda5, getWSLPlayer(), null, "lambda5", new Float(600f));
        l6Scripter = new NumberBoxScripter(lambda6, getWSLPlayer(), null, "lambda6", new Float(650f));*/
        
        //Construct toolbar for the top of the GUI and add all of the elements to it [JD]
        ToolBar toolbar = getToolBar();
        toolbar.addBrowserButton("Directions", "/org/webtop/html/grating/directions.html");
        toolbar.addBrowserButton("Theory", "/org/webtop/html/grating/theory.html");
		toolbar.addBrowserButton("Examples", "/org/webtop/html/grating/examples.html");
		toolbar.addBrowserButton("Exercises", "/org/webtop/html/grating/exercises.html");
        //Need to make a page to display the appropriate images here [JD]
        toolbar.addBrowserButton("Images", "/org/webtop/html/grating/images.html");
        toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
    }

    protected void setupX3D() {

        ifs = new IndexedSet(getSAI(),getSAI().getNode("ifsNode"));

        createProjectionScreen();
        //This should reduce the initial screen flicker [Davis]
        ifs.setColors(new float[FACE_COUNT][3]);
        
        ts = new TouchSensor(getSAI(), getSAI().getNode("TS9"), (short)1, null);
        ts.setOver(true);
        ts.addListener((SpatialWidget.Listener)this);
    }

    protected void setupMenubar() {
    }

    protected void setDefaults() {
        numberOfSlits.setValue(DEF_SLITS);
        slitWidth.setValue(DEF_WIDTH);
        slitDistance.setValue(DEF_DISTANCE);
        intensity.setValue(DEF_INTENSITY);
        displayIntensity();
        lampChoice.setSelectedIndex(DEF_LAMP);
        setSpectrum(DEF_LAMP);

    }
    protected void setUserDefaults(){ //new -- method used for the userResetButton
    	userNumberOfSlits.setValue(DEF_SLITS);
        userSlitWidth.setValue(DEF_WIDTH);
        userSlitDistance.setValue(DEF_DISTANCE);
        userIntensity.setValue(DEF_INTENSITY);
        lambda1.setValue(400.1f); lambda1.selectAll(); lambda1.cut(); 
        lambda2.setValue(400.1f); lambda2.selectAll(); lambda2.cut();
        lambda3.setValue(400f); lambda3.selectAll(); lambda3.cut();
        lambda4.setValue(400f); lambda4.selectAll(); lambda4.cut();
        lambda5.setValue(400f); lambda5.selectAll(); lambda5.cut();
        lambda6.setValue(400f); lambda6.selectAll(); lambda6.cut();
        for(int i = 0; i<6; i++){
        	tempUserStrengths[i] = 0f;
        }
        displayIntensity();
        update();
    }

    private void createProjectionScreen() {
            final float[][] pointSet=new float[POINT_COUNT][3];
            int index=0;
            for(float theta = 0; theta <= 180; theta += THETA_STEP) {
                    pointSet[index][0] = pointSet[index+1][0] = (float) -Math.cos(WTMath.toRads(theta));
                    pointSet[index][1] = 1;
                    pointSet[index][2] = pointSet[index+1][2] = (float) -Math.sin(WTMath.toRads(theta));
                    pointSet[index+1][1] = -1;

                    index += 2;
            }

            
            ifs.setCoords(pointSet);

            final int[] indices=new int[3*POINT_COUNT-6];
            int j = 0;
            for(int i = 0; i <= indices.length - 6; i += 6) {
                    indices[i] = 2 * j;
                    indices[i+1] = 1 + 2 * j;
                    indices[i+2] = 3 + 2 * j;
                    indices[i+3] = 2 + 2 * j;
                    indices[i+4] = 2 * j;
                    indices[i+5] = -1;
                    j++;
            }

            
            ifs.setCoordIndices(indices);

            final int[] colorIndices=new int[FACE_COUNT];
            for(int i = 0; i < FACE_COUNT; i++) colorIndices[i] = i;

            
            ifs.setColorIndices(colorIndices);
	}

    private void displayIntensity() {
    	if(this.whichPane == 0)//new
           intensityLabel.setText("Intensity factor:  " + intensity.getValue()*INTENSITY_SCALE);
    	else if(this.whichPane == 1)//new
    		userIntensityLabel.setText("Intensity factor:  " + userIntensity.getValue()*INTENSITY_SCALE);//new
    }

    private void setSpectrum(final int index) {
    		wavelengthListModel.removeAllElements();
            if(index!=USER) {
                    final float[] lines=BUILTIN_LINES[index];
            		for(int i=0;i<lines.length;++i)
            			wavelengthListModel.addElement(Float.toString(FPRound.toFixVal(lines[i],1)));
            }

            update();
    }

    private void update() {
            final int index=lampChoice.getSelectedIndex();
            if(index==USER) userRender();
            else if(this.whichPane == 1) userRenderNew(); //new
            else render(BUILTIN_LINES[index],BUILTIN_STRENGTHS[index]);
    }

    
    private void userRender() {
    	final int n=wavelengthListModel.getSize();

		final float[] userLines = new float[n], userStrengths = new float[n];

		for(int i = 0; i < n; i++) {
			userLines[i] = Float.valueOf((String)wavelengthListModel.getElementAt(i)).floatValue();
			userStrengths[i] = 1;
		}
		render(userLines,userStrengths);
	}
    
    //new -- method used in the alamo
    private void userRenderNew(){ //look at userRender to do this
    	System.out.println("User Render New");
    	//for some reason, the scene is not updating unless you hit enter twice..figure out why
    	float num1 = lambda1.getValue(), num2 = lambda2.getValue(), num3 = lambda3.getValue(), 
    	num4 = lambda4.getValue(), num5 = lambda5.getValue(), num6 = lambda6.getValue();
    	
    	tempUserLines[0] = num1; tempUserLines[1] = num2; tempUserLines[2] = num3; tempUserLines[3] = num4; 
    	tempUserLines[4]=num5; tempUserLines[5] = num6;
    	/*for(int i=0; i<6; i++){
    		tempUserStrengths[i] = 1.0f;
    	}*/
    	render(tempUserLines, tempUserStrengths);
    }
    //use this method to only update the number box that was changed..will have to do some tweaking though
    //maybe use the userLines[] to a global var in order to use them in both places?
    private void userRenderNew(int lambda){//use the first userRenderNew only for setUserDefaults
    	System.out.println("User Render New with int");
    	float temp = 0;
    	if(lambda==1){
    		temp = lambda1.getValue();
    	}
    	else if(lambda==2) {
    		temp = lambda2.getValue(); 
    	}
    	else if (lambda == 3){
    		temp = lambda3.getValue(); 
    	}
    	else if(lambda == 4){
    		temp = lambda4.getValue(); 
    	}
    	else if(lambda == 5){
    		temp = lambda5.getValue();
    	}
    	else if(lambda == 6){
    		temp = lambda6.getValue();
    	}
    	System.out.println("userRenderNew(int)::value:: " + temp);
    	tempUserLines[lambda - 1]  = temp;
    	tempUserStrengths[lambda - 1] = 1.0f;
    	
    	render(tempUserLines, tempUserStrengths);
    	
    }
	

    //The calculate*() functions and getMaxOrder() think of l in microns!

    private float calculatePhi(final int m,final float l) {
    	if(whichPane == 0){
            if((m - 1f / numberOfSlits.getValue()) * l / slitDistance.getValue() < -1)
                    return (float) (-Math.PI / 2);
            else if((m - 1f / numberOfSlits.getValue()) * l / slitDistance.getValue() > 1)
                    return (float) (Math.PI / 2);
            else
                    return (float) Math.asin((m - 1f / numberOfSlits.getValue()) * l / slitDistance.getValue());
    	}
    	else if(whichPane ==1){
    		if((m - 1f / userNumberOfSlits.getValue()) * l / userSlitDistance.getValue() < -1)
                return (float) (-Math.PI / 2);
        else if((m - 1f / userNumberOfSlits.getValue()) * l / userSlitDistance.getValue() > 1)
                return (float) (Math.PI / 2);
        else
                return (float) Math.asin((m - 1f / userNumberOfSlits.getValue()) * l / userSlitDistance.getValue());
    	}
    	else{
    		return 0f;
    	}
    }

    private float calculatePsi(final int m,final float l) {
    	if(whichPane==0){
            if((m + 1f / numberOfSlits.getValue()) * l / slitDistance.getValue() < -1)
                    return (float) (-Math.PI / 2);
            else if((m + 1f / numberOfSlits.getValue()) * l / slitDistance.getValue() > 1)
                    return (float) (Math.PI / 2) + 1;
            else
                    return (float) Math.asin((m + 1f / numberOfSlits.getValue()) * l / slitDistance.getValue());
    	}
    	else if(whichPane == 1){
    		 if((m + 1f / userNumberOfSlits.getValue()) * l / userSlitDistance.getValue() < -1)
                 return (float) (-Math.PI / 2);
         else if((m + 1f / userNumberOfSlits.getValue()) * l / userSlitDistance.getValue() > 1)
                 return (float) (Math.PI / 2) + 1;
         else
                 return (float) Math.asin((m + 1f / userNumberOfSlits.getValue()) * l / userSlitDistance.getValue());
    	}
    	else 
    		return 0f;
    }

    private float calculateTheta(final int m,final float l) {
    	if(whichPane == 0){
            if(m * l / slitDistance.getValue() < -1)
                    return (float) (-Math.PI / 2);
            else if(m * l / slitDistance.getValue() > 1)
                    return (float) (Math.PI / 2);
            else
                    return (float) Math.asin(m * l / slitDistance.getValue());
    	}
    	else if(whichPane==1){
    		 if(m * l / userSlitDistance.getValue() < -1)
                 return (float) (-Math.PI / 2);
         else if(m * l / userSlitDistance.getValue() > 1)
                 return (float) (Math.PI / 2);
         else
                 return (float) Math.asin(m * l / userSlitDistance.getValue());
    	}
    	else 
    		return 0f;
    }

    private int getMaxOrder(final float l) {
    	if(whichPane == 0)
            return (int) (Math.floor(FPRound.toFixVal(slitDistance.getValue() / l, 3)));
    	else if(whichPane==1)
    		return (int) (Math.floor(FPRound.toFixVal(userSlitDistance.getValue() / l, 3)));
    	else 
    		return 0;
    }

    private int stepFunction(final double u,final double theta,final double v) {
            return (theta >= u && theta <= v)?1:0;
    }

    private float calculateIntensity(final float l,final float t,final int M) {
            float H,alpha=0,F;
            float totalH = 0;
            final float theta = t - (float) Math.PI / 2;
            

            for(int i = -M; i <= M; i++)
                    totalH += stepFunction(calculatePhi(i,l),theta,calculatePsi(i,l));
            if(whichPane == 0)
            	alpha = (float) (Math.PI / l * slitWidth.getValue() * Math.sin(theta));
            else if(whichPane == 1)
            	alpha = (float) (Math.PI / l * userSlitWidth.getValue() * Math.sin(theta));

            if(theta == 0) F = 1;
            else F = (float) Math.pow(Math.sin(alpha) / alpha,2);

            return F * totalH;
    }

    private int getFaceIndex(final float theta) {
            return WTMath.bound((int)Math.floor(theta/THETA_STEP),0,FACE_COUNT-1);
    }

    private void render(final float[] wavelengths,final float[] intensities) {
            final float[][] colors=new float[FACE_COUNT][3];
           
            final float[] rgb = new float[3];
            float printable = 0; //new
            for(int i = 0; i < wavelengths.length; i++) {
                    final float lambda=wavelengths[i]/1000;	// convert to microns, whee
                    final int M = getMaxOrder(lambda);
                    //System.out.println("Wavelength: "+wavelengths[i]+" nm -> max order: "+M);
                    final float[] phi = new float[2 * M + 1],
                                                                            psi = new float[2 * M + 1],
                                                                            theta = new float[2 * M + 1];
                    final float hue=WTMath.hue(wavelengths[i]);	// hue() wants nanometers
                    //final float intensityFactor = intensity.getValue()*INTENSITY_SCALE;//old
                    float intensityFactor = 1.0f;
                    if(whichPane == 0){//new
                    	intensityFactor=intensity.getValue()*INTENSITY_SCALE;
                    	System.out.println("Def Intensity: " + intensityFactor);
                    	System.out.println("				LAMP RENDERING");
                    }//new
                    else if(whichPane == 1){//new
                    	intensityFactor = userIntensity.getValue()*INTENSITY_SCALE;//new
                    	System.out.println("User Intensity: " + intensityFactor);
                    	System.out.println("				USER RENDERING");
                    }//new
                    	
                    for(int j = -M; j <= M; j++) {
                            phi[j+M] = (float) ((Math.PI / 2) + calculatePhi(j,lambda));
                            psi[j+M] = (float) ((Math.PI / 2) + calculatePsi(j,lambda));
                            theta[j+M] = (float) ((Math.PI / 2) + calculateTheta(j,lambda));
                    }
                 
                    for(int p = 0; p < phi.length; p++) {
                            final int leftFace = getFaceIndex(WTMath.toDegs(phi[p]));
                            final int rightFace = getFaceIndex(WTMath.toDegs(psi[p]));
                            //System.out.println("For p="+p+", [leftFace,rightFace]=["+leftFace+','+rightFace+"]; delta="+(rightFace-leftFace));
                            float intensity = calculateIntensity(lambda,theta[p],M);
                            intensity *= 0.5f * intensities[i] * intensityFactor;
                            //intensity = (float) Math.pow(intensity,0.7f);

                            for(int q = leftFace; q <= rightFace; q++) {
                                    WTMath.hls2rgb(rgb,hue,intensity,WTMath.SATURATION);
                                    colors[q][0] += rgb[0];
                                    colors[q][1] += rgb[1];
                                    colors[q][2] += rgb[2];
                            }
                            printable = intensity;
                    }
            }
            //look at if(...) intensity/=2.5;
            System.out.println("Final Intensity: " + printable);

            //Clamp colors to [0,1]^3
            for(int i=0;i<FACE_COUNT;i++)
                    for(int j=0;j<3;++j) if(colors[i][j]>1) colors[i][j]=1;

            	ifs.setColors(colors);
    }

    /*
     * addWavelength() is used to add user defined wavelengths to the screen.  Handles adding wavelengths when 
     * the WSLPlayer is/is not playing.
     */
    private void addWavelength(){
    	//if(!(getWSLPlayer().isPlaying())){ //if the WSL Player is not playing, set the vector up for playback [JD]
    	if(true) {
    		final String toAdd = wavelength.getText();
    		if((!(addVec.contains(toAdd))) || removeVec.contains(toAdd)){
    			addVec.add(toAdd);
    			System.out.println("Adding: " + toAdd);
    			wavelengthListModel.addElement(toAdd);
    			update();
    			wavelength.selectAll();
    			wavelength.cut();
    			System.out.println("Calling Add:  WSL Player is not playing");
    			getStatusBar().clearWarning();
    		}
    		else{
    			System.out.println("Wavelength is already in the list: returning");
    			getStatusBar().setWarningText("Wavelength has already been added");
    			wavelength.selectAll();
    			wavelength.cut();
    			return;
    		}
    		itr = addVec.iterator();
    	}
    	else if(getWSLPlayer().isPlaying()){ //if the WSL Player is playing, use the vector for playback [JD]
    			System.out.println("In Add action performed: WSL Player is playing");
    			while(itr.hasNext()){
    				String temp = itr.next().toString();
    				System.out.println("Next in vector: " + temp);
    				System.out.println("addVec position: " + addVec.indexOf(temp));
    				//wavelengthList.add(temp);
    				update();
    				break;
    			}
    		}
    }
    
    /*
     * removeWavlength() is used to remove user defined wavelengths from the screen.  Handles removing wavelengths
     * when the WSL Player is/is not playing. 
     */
    private void removeWavelength(){
    	final String remove = (String)wavelengthList.getSelectedValue();
    	//if(!(getWSLPlayer().isPlaying())){ //if the WSL Player is not playing, set the vector up for playback [JD]
    	if(true) {
    		if(remove!=null || addVec.contains(remove)){
    				getStatusBar().clearWarning();
    				removeVec.add(remove);
    				System.out.println("WSL Player is not playing: Removing: " + remove);
    				wavelengthListModel.removeElement(remove);
    				update();
    			}
    			else if(remove == null){
    				getStatusBar().setWarningText("First select a wavelength from the list to remove");
    			}
    		itr2 = removeVec.iterator();
    	}
    	else if(getWSLPlayer().isPlaying()){ //if the WSL Player is playing, use the vector for playback [JD]
			System.out.println("Remove: WSL Player is playing");
			while(itr2.hasNext()){
				String temp = itr2.next().toString();
				System.out.println("Next in removeVec: " + temp);
				System.out.println("Position in removeVec: " + removeVec.indexOf(temp));
				//wavelengthList.remove(temp);
				update();
				break;
			}	
    	}
    }



    public void invalidEvent(String node, String event) {
    }

    public void toolTip(Tooltip src, String tip) {
    }

    public String getWSLModuleName() {
        return "grating";
    }

    public void mouseEntered(Widget src) {
    }

    public void mouseExited(Widget src) {
    }

    public void mousePressed(Widget src) {
    }

    public void mouseReleased(Widget src) {
    }

    public void numChanged(NumberBox src, Number newVal) {
            getStatusBar().clearWarning();
            NumberBox source = src;
            System.out.println("NumChanged::src:: " + src.getName());
            String param;
            if(whichPane == 0){
            	if(src==numberOfSlits) param="slits";
            	else if(src==slitWidth) param="width";
            	else if(src==slitDistance) param="distance";
            	else {
            			System.err.println("Grating: unexpected numChanged from "+src);
            			return;
            	}
            }
            else if(whichPane == 1){
            	if(src == userNumberOfSlits) param = "slits";
            	else if(src==userSlitWidth) param = "width";
            	else if(src==userSlitDistance) param = "distance";
            	else if(source.equals(lambda1))
            		userRenderNew(1);
            	else if(source.equals(lambda2))
            		userRenderNew(2);
            	else if(source.equals(lambda3))
            		userRenderNew(3);
            	else if(source.equals(lambda4))
            		userRenderNew(4);
            	else if(source.equals(lambda5))
            		userRenderNew(5);
            	else if(source.equals(lambda6))
            		userRenderNew(6);
            	else{
            		System.err.println("Grating: unexpected numChanged from " + src);
            		return;
            	}
            }

            update();
	}

    public void boundsForcedChange(NumberBox source, Number oldVal) {
    }

    public void invalidEntry(NumberBox src, Number badVal) {
            if(src == slitWidth)
                    getStatusBar().setWarningText("Slit width must be between "+MIN_WIDTH+" and "+MAX_WIDTH+" microns.");
            else if(src == slitDistance)
                    getStatusBar().setWarningText("Distance between slits must be between "+MIN_DISTANCE+" and "+MAX_DISTANCE+" microns.");
            else if(src == numberOfSlits)
                    getStatusBar().setWarningText("Number of slits must be between "+MIN_SLITS+" and "+MAX_SLITS+'.');
            else if(src == userSlitWidth)
                getStatusBar().setWarningText("Slit width must be between "+MIN_WIDTH+" and "+MAX_WIDTH+" microns.");
            else if(src == userSlitDistance)
                getStatusBar().setWarningText("Distance between slits must be between "+MIN_DISTANCE+" and "+MAX_DISTANCE+" microns.");
            else if(src == userNumberOfSlits)
                getStatusBar().setWarningText("Number of slits must be between "+MIN_SLITS+" and "+MAX_SLITS+'.');
            else if(src==lambda1||src==lambda2||src==lambda3||src==lambda4||src==lambda5||src==lambda6)
            	getStatusBar().setWarningText("Wavelength must be between 400 and 700");
            else System.err.println("Grating: unexpected invalidEntry from "+src);
	}

    public void actionPerformed(ActionEvent e) {
        final Object source = e.getSource();
        if(this.whichPane == 0){//new..indicates default pane
        	if(source == addButton || source==wavelength) {
        		addWavelength();
        	} else if(source == removeButton) {
        		removeWavelength(); 
        	} else if(source == resetButton) {
                	setDefaults();
                	getStatusBar().clearWarning();
                	//Clear the vectors containing user defined wavelengths [JD]
                	addVec.removeAllElements();
                	removeVec.removeAllElements();
        	} 
        }
        else if(whichPane ==1){//new...indicates User Defined pane
        	if(source.equals(lambda1))
        		userRenderNew(1);
        	else if(source.equals(lambda2))
        		userRenderNew(2);
        	else if(source.equals(lambda3))
        		userRenderNew(3);
        	else if(source.equals(lambda4))
        		userRenderNew(4);
        	else if(source.equals(lambda5))
        		userRenderNew(5);
        	else if(source.equals(lambda6))
        		userRenderNew(6);
        	else if(source.equals(userResetButton)){
        		System.out.println("User Reset");
        		setUserDefaults();
        	}
        }//end new
    }
    
   
    public void itemStateChanged(ItemEvent e) {

		final int index=lampChoice.getSelectedIndex();

		wavelengthList.removeAll();

		if(index==USER) {
			wavelength.setEnabled(true);
			addButton.setEnabled(true);
			removeButton.setEnabled(true);
		} else {
			wavelength.setEnabled(false);
			addButton.setEnabled(false);
			removeButton.setEnabled(false);
			//Clear the vectors containing user defined wavelengths only if the WSL Player is not playing [JD]
			if(!getWSLPlayer().isPlaying()){
				addVec.removeAllElements();
				removeVec.removeAllElements();
			}
		}

		setSpectrum(index);
	}
    
    public void valueChanged(SpatialWidget src, float x, float y, float z) {
    	float theta = WTMath.toDegs((float) Math.atan(x/z));
    	//statusBar.setText("Theta: " + FPRound.toFixVal(theta, 1) + " degrees");
    	statusBar.setText("\u03b8: " + FPRound.toFixVal(theta, 1) + " \u00b0");
		
	}
    
    //implement ChangeListener..all of this is new...need to remove when updating
    public void stateChanged(ChangeEvent e){
    	JTabbedPane tabSource = (JTabbedPane)e.getSource();
    	String tab = tabSource.getTitleAt(tabSource.getSelectedIndex());
    	if(tab.equals("Lamps")){
    		System.out.println("Default Selected");
    		this.whichPane = 0; 
    		setDefaults();
            getStatusBar().clearWarning();
    	}
    	if(tab.equals("User Defined")){
    		System.out.println("User Defined Selected");
    		this.whichPane = 1; 
    		setUserDefaults();
    	}
    }
    
    protected void toWSLNode(WSLNode node) {
    	super.toWSLNode(node);
    	numberOfSlitsScripter.addTo(node);
    	userNumberOfSlitsScripter.addTo(node);
    	slitWidthScripter.addTo(node);
    	userSlitWidthScripter.addTo(node);
    	slitDistanceScripter.addTo(node);
    	userSlitDistanceScripter.addTo(node);
    	intensityScripter.addTo(node);
    	userIntensityScripter.addTo(node);
    	lampChoiceScripter.addTo(node);  
    	wavelengthScripter.addTo(node);
    	panelScripter.addTo(node);//new
    	//lambda box scripters
    	l1Scripter.addTo(node);
    	l2Scripter.addTo(node);
    	/*l3Scripter.addTo(node);
    	l4Scripter.addTo(node);
    	l5Scripter.addTo(node);
    	l6Scripter.addTo(node);*/
    }
}
