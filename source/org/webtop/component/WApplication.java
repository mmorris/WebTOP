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
 * <p>Title: WApplication</p>
 * 
 * <p>Description: Base class for all WebTOP modules</p>
 * 
 * <p>Company:MSU Department of Physics and Astronomy</p>
 * 
 * @author WebTOP Team
 *@version 0.1
 */

package org.webtop.component;

import java.awt.event.KeyEvent;
import java.awt.*;
import java.util.HashMap;
import javax.swing.*;
import org.web3d.x3d.sai.*;
import org.webtop.x3d.SAI;
import org.webtop.x3d.widget.*;
import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;
import org.webtop.util.WTString;
import org.webtop.util.script.NavigationPanelScripter;
import org.webtop.component.ToolBar;
import java.net.*;


public abstract class WApplication
        extends JFrame
        implements SAI.InvalidEventListener,Tooltip.Listener,Widget.Listener,WSLModule,WSLPlayerListener {
    public static final int MAJOR_VERSION = 6;
    public static final int MINOR_VERSION = 1;
    public static final int REVISION = 1;

    //WApplication standards/constants
    protected static final int DEF_WAPPLICATION_WIDTH   = 990;  //The WebTOP standard width for 1024x768 resolutions.
    protected static final int DEF_WAPPLICATION_HEIGHT  = 680;  //The WebTOP standard height for 1024x768 resolutions
    protected static final int MIN_WAPPLICATION_WIDTH   = 766;  //Minimum resolution supported is 800x600.
    protected static final int MIN_WAPPLICATION_HEIGHT  = 500;  //Minimum resolution supported is 800x600.
    protected static final int CONTROL_PANEL_WIDTH      = 650;  //All control panels are the same width.  Height varies.
    protected static final int MIN_CONTROL_PANEL_HEIGHT =  120;  //Minimum height for a control panel.
    protected static final int STATUS_BAR_HEIGHT        = 20;
    protected static final String NAV_PANEL_NAME        = "NavPanel";
    public static final Color BACKGROUND = Color.DARK_GRAY;
    public static final Color FOREGROUND = Color.WHITE;
    

    //****** UI descriptive elements ******//
    protected final String welcome="Welcome to the "+getModuleName()+" Module", moduleVersion="Version is "+getVersionString()+'.';
    protected String defaultStatus;

    //****** X3D variables ******//
    private SAI sai;
    private String worldURL;
    private X3DComponent x3dComponent;
    private ExternalBrowser browser;
    private X3DScene scene;
    private X3DNode mainView;
    private SFBool bind;
    
    //****** Navigation Panel ******//
    private NavigationPanel navPanel;
    
    //****** Navigation Panel Scripter ******//
    private NavigationPanelScripter navPanelScripter;

    //****** Event variables ******//
    boolean widgetActive;


    //****** GUI elements for the application window ******//
    //Content pane for the JFrame
    private Container content;
    //A menubar for the module...can contain navigation buttons like the old web pages did.  Use setJMenuBar().
    protected JMenuBar menuBar = new JMenuBar();
    protected ToolBar toolBar = new ToolBar(this);
    //A WebTOP status bar for the module
    protected StatusBar statusBar = new StatusBar();
    //protected JLabel statusBar = new JLabel();
    
    //A Box container to hold the panels.
    protected Box box;
    //The icon for the minimized window.
    protected Image icon;
    //The JPanel for displaying the X3D scene
    private JComponent x3dPanel;
    //The JPanel for displaying the WebTOP console
    protected JPanel console = new JPanel();
    //The JPanel for displaying the WebTOP control panel.
    protected JPanel controlPanel = new JPanel();
    protected JPanel outerControlPanel = new JPanel();
    //Representation of the minimum dimensions of a WApplication.
    protected Dimension minSize = new Dimension(MIN_WAPPLICATION_WIDTH, MIN_WAPPLICATION_HEIGHT);
    //Used to set the maximum size (dependent upon screen resolution) in initGUI().
    protected Dimension maxSize;

    //****** WSL elements ******//
    private final WSLPlayer wslPlayer=new WSLPlayer(this);
    private final WSLPanel wslPanel=new WSLPanel(getWSLPlayer());



    //****** Universal WApplication constants ******//
    /**
     * Minimum wavelength of light allowed (400).  (Where is this used? [Paul])
     */
    public static final float MIN_WAVELENGTH=400;
    /**
     * Maximum wavelength of light allowed (700).  (Where is this used? [Paul])
     */
    public static final float MAX_WAVELENGTH=700;
    /**
     * Key to print current viewpoint to Java console.  Used in conjunction with <code>VIEW_READ_MODIFIERS</code> modifier key.
     */
    public static final int VIEW_KEY=KeyEvent.VK_F11;
    /**
     * The key to reset to the default viewpoint
     */
    public static final int RESET_KEY=KeyEvent.VK_F12;
    /**
     * Modifier key for <code>VIEW_KEY</code>.  Shift+<code>VIEW_KEY</code> performs the print.
     */
    public static final int VIEW_READ_MODIFIERS=KeyEvent.SHIFT_MASK;
    /**
     * Modifier key for <code>RESET_KEY</code>.  Not currently used.
     */
    public static final int VIEW_RESET_MODIFIERS=0;

    //****** UI methods ******//
    protected StatusBar getStatusBar() {
        return statusBar;
    }
    protected void resetStatus() {
        getStatusBar().setText(defaultStatus);
    }
    protected void clearWarning() {
        if (getStatusBar().isWarning()) resetStatus();
    }


    //********* Utility methods *********//
    //This should return true if clicks on the widget are "important" drags.
    protected boolean isDraggable(Widget w) {
        return!(w instanceof TouchSensor);
    }


    //********* Event handling *********//


    //==== ToolTip.Listener implementation ====//
    public void toolTip(Tooltip src, String tip) {
        getStatusBar().setText(tip == null ? defaultStatus : tip);
    }

    //==== Widget.Listener implementation ====//
    public void mouseEntered(Widget src) {
        getStatusBar().setText(src.tooltip);
    }

    public void mouseExited(Widget src) {
        resetStatus();
    }

    //Drags on the navigation panel shouldn't count for the module.
    public void mousePressed(Widget src) {
        if (isDraggable(src)) setWidgetDragging0(src, true);
    }

    public void mouseReleased(Widget src) {
        if (isDraggable(src)) setWidgetDragging0(src, false);
    }

    //==== Other Widget event methods ====//

    //Called when a widget is clicked/released (if approved by isDraggable()).
    //Note that in certain bizarre situations, it is possible for this function
    //to be called twice successively with the same argument. [Davis...PC]
    //I am unsure of what circumstances necessitate the overriding of this method.  [PC]
    protected void setWidgetDragging(Widget w,boolean drag) {}

    //Updates state and calls the overrideable method
    private void setWidgetDragging0(Widget w, boolean drag) {
        widgetActive = drag;
        setWidgetDragging(w, drag);
    }

    protected boolean draggingWidget() {return widgetActive;}

   
    //****** WSL Methods ******//
    public WSLPlayer getWSLPlayer() {
        return wslPlayer;
    }

    public WSLPanel getWSLPanel() {
        return wslPanel;
    }

    //WSLModule implementation
    //We shouldn't implement getWSLModuleName() because it's not likely to equal
    //getModuleName()

    //This method is a sign: WSLPlayer should not automatically interface with a
    //NavigationPanel; there should be a NavPanelScripter which WApplet
    //automatically installs.
    //public vrml.external.Node getNavigationPanelNode() {return getEAI().world==null?null:getEAI().getNode(getNavPanelName());}

    //This funky arrangement is actually what WSL should do (eventually); not
    //rely so much on the usefulness of the module.
    protected void toWSLNode(WSLNode node) {
    	navPanelScripter.addTo(node);
    }

    public WSLNode toWSLNode() {
        WSLNode node = new WSLNode(getWSLModuleName());
        toWSLNode(node);
        //nps.addTo(node);  //Adding node to NavigationPanel.  Problem?
        return node;
    }

    //WSLPlayerListener implementation
    //The WSL player events are likely to change (once I get around to redoing
    //them), but having them handled in just one place should ease the
    //transition.  [Davis]
    public void playerStateChanged(WSLPlayerEvent e) {
        switch (e.getID()) {
        case WSLPlayerEvent.PLAYER_STARTED:

            //Secure module while playing script
            //These disabled for now, as they didn't seem to do anything in WApplet anyway.  [PC]
            //setWidgetsEnabled(false);
            //setGUIEnabled(false);

            //This fails when the user pauses and then resumes after having checked
            //'enable viewpoint manipulation'.  But when the events change, that
            //should stop being a problem.
            //vpReset.enabled = !getWSLPlayer().isViewpointEventEnabled();  //NavigationPanel stuff [PC]
            defaultStatus = getPlaybackStatus();
            resetStatus();
            break;
        case WSLPlayerEvent.PLAYER_STOPPED:

            //Resume normal input
            //These disabled for now, as they didn't seem to do anything in WApplet anyway.  [PC]
            //setWidgetsEnabled(true);
            //setGUIEnabled(true);
            //vpReset.enabled = true;
            //scriptCleanup();
            defaultStatus = getDefaultStatus();
            resetStatus();
            break;
        case WSLPlayerEvent.RECORDER_STARTED:
            defaultStatus = getRecordingStatus();
            resetStatus();
            break;
        case WSLPlayerEvent.RECORDER_STOPPED:
            defaultStatus = getDefaultStatus();
            resetStatus();
            break;
        }
    }


    //****** Subclass Methods ******//
    /**
    * Implemented by extending module to give its name.
    *
    * @return String holding the module's name
    */
   protected abstract String getModuleName();

   /**
    * Implemented by extending module to give its major version number.
    * The current Major Version of WebTOP is 6
    *
    * @return int of the Major Version
    */
   protected abstract int getMajorVersion();

   /**
    * Implemented by extending module to give its minor version number.
    * The current Minor Version of WebTOP is 1
    *
    * @return int of the Minor Version
    */
   protected abstract int getMinorVersion();

   /**
    * Implemented by extending module to give its revision number.
    *
    * @return int of the Revision Number
    */
   protected abstract int getRevision();

   /**
    * Implemented by extending module to give its last modified date.
    *
    * @return String containing the date.
    */
   protected abstract String getDate();

   /**
    * Implemented by extending module to give the name of it's author(s).
    *
    * @return String containing the author's name.
    */
   protected abstract String getAuthor();

   /**
    * Implemented by extending class to give the Component that has the initial focus.
    * This will not be called before setupGUI().
    *
    * @return Component with initial focus.
    */
   protected abstract Component getFirstFocus();
   protected String getVersionString() {
       return WTString.versionString(getMajorVersion(),getMinorVersion(),getRevision())+" ("+getDate()+')';
   }
   protected String getWelcomeStatus() {
       return welcome;
   }
   protected String getDefaultStatus() {
       return "Ready";
   }
   protected String getRecordingStatus() {
       return "Recording...";
   }
   protected String getPlaybackStatus() {
       return "Running...";
   }

   //********* Functions to simplify font-set calls *********//
   public static Font sans(int size) {
       return new Font("SansSerif", Font.PLAIN, size);
   }

   public static Font sans(int size, int style) {
       return new Font("SansSerif", style, size);
   }

   public static Font helv(int size) {
       return new Font("Helvetica", Font.PLAIN, size);
   }

   public static Font helv(int size, int style) {
       return new Font("Helvetica", style, size);
   }



   //********* Module Customization *********//
   /**
    * Executed immediately after the call to JFrame's constructor via super().  This
    * method should be used to set up any variables that need to be non-null before
    * anything else executes.  It is not necessary to implement this method.  It is
    * primarily available to resolve strange issues such as class-global arrays being
    * null despite assignment at their declaration.
    */
   protected void preconstructor() {}
   /**
     * Called during WApplication constructor; should establish user interface objects.  The
     * WSLPlayer will be available during this method's invocation.
     */
   protected abstract void setupGUI();
   /**
    * Called during WApplication constructor; should use the SAI object (obtained with getSAI())
    * to connect to X3D objects.
    */
   protected abstract void setupX3D();
   /**
    * Modules should implement their menubars here, ending this method with a call to setMenuBar().
    */
   protected abstract void setupMenubar();
   /**
    * This should reset the module (and will be used to set it up in start())
    * There may be subtleties here...
    */
   protected abstract void setDefaults();


   //********* WApplication services *********//
   /**
    * Returns a reference to the WApplication's <code>org.webtop.x3d.SAI</code> object.  This method is normally
    * called in order to gain access to X3D fields and to create Named Nodes.
    * @see org.webtop.x3d.SAI.getNode
    * @return SAI object for the WApplication.
    */
   public SAI getSAI() {
       return sai;
   }

   public ToolBar getToolBar() {
	   return toolBar;
   }
   
   /**
    * Adds a component to the WApplication's control GUI.
    * @param c Component to be added
    */
   public void addToConsole(Component c) {
       controlPanel.add(c);
       //Set preferred size of the console to new size
   }

   /**
    * Sets the menu bar for a WApplication.  Use in conjunction with setupMenubar().
    * @param menubar JMenuBar
    */
   protected void setMenubar() {
       setJMenuBar(menuBar);
   }

   //Initializes the X3D world.  Called after content pane acquired in constructor.  Not to be confused with setupX3D().
   private void initX3D(String world, HashMap params) {
	   //Check to see if we can access a file for the world
	   
	    URL worldURL = this.getClass().getResource(world);
	   
	   //So, xj3d requires three slashes when using the file protocol to work properly
	   //for some reason.  We add those here.  I think this should work in most cases?
	   world = worldURL.toString();
	   world = world.replaceAll("file:/", "file:///"); 
	   System.out.println("URL:");
	   System.out.println(world);
	   
       x3dComponent = BrowserFactory.createX3DComponent(params);
       x3dPanel = (JComponent) x3dComponent.getImplementation();
       browser = x3dComponent.getBrowser();
       scene = browser.createX3DFromURL(new String[] {world}); //Need try/catch here for error catching.  also a waiting loop to ensure load.
       sai = new SAI(scene, this);
       browser.replaceWorld(scene);
       
   }
   	
   //Initializes the Java Swing GUI--the same for all WApplications.  Not to be confused with setupGUI().
   private void initGUI(boolean showStatusBar) {
       //Set up the layout for the main window and the constraints object to control added objects
       GridBagLayout layout = new GridBagLayout();
       GridBagConstraints constraints = new GridBagConstraints();
       setLayout(layout);
       content.setBackground(Color.BLACK);
       setForeground(FOREGROUND);

       /* We can dynamically resize the window to respond to the size of the screen. */
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

       //set min/max screen size
       maxSize = new Dimension(new Dimension((int) (screenSize.width * 0.8),
                                             (int) (screenSize.height * 0.8)));
       Dimension size = new Dimension(new Dimension((int) (1024 * 0.8),
                                             (int) (768 * 0.8)));
       
       int panelWidth = (int)(1024 *.8);
       int panelHeight = (int) (768 * .8) - MIN_CONTROL_PANEL_HEIGHT;
       Dimension panelSize = new Dimension(panelWidth, panelHeight);
       //*** These lines cause problems.  Why?  [PC] ***//
       /*setMinimumSize(minSize);
       setMaximumSize(maxSize);*/
       setSize(size);

       //set colors for GUI elements
       UIDefaults uiDefaults = UIManager.getDefaults();
       UIManager.put("Panel.background", BACKGROUND);
       UIManager.put("Panel.foreground", FOREGROUND);
       UIManager.put("Label.foreground", FOREGROUND);
       UIManager.put("TextField", FOREGROUND);

       //set component sizes
       controlPanel.setMinimumSize(new Dimension(CONTROL_PANEL_WIDTH,
                                            MIN_CONTROL_PANEL_HEIGHT));

       //controlPanel.setPreferredSize(controlPanel.getMinimumSize());
       console.setSize(CONTROL_PANEL_WIDTH, MIN_CONTROL_PANEL_HEIGHT);

       //Set size of the world display
       x3dPanel.setPreferredSize(panelSize);
 
       //Set up display of panels
       console.setBackground(BACKGROUND);
       console.setForeground(FOREGROUND);
       console.setAlignmentX(Component.CENTER_ALIGNMENT);
       controlPanel.setBackground(BACKGROUND);
       controlPanel.setForeground(FOREGROUND);
       //controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
       x3dPanel.setBackground(Color.BLACK);
       System.out.println("JFrame height = " + getHeight());
       System.out.println("x3dPanel height = " +
                          (getHeight() - console.getSize().height));
       //x3dPanel.setSize(getSize().width,
                       // getHeight() - console.getSize().height);
       //x3dPanel.setPreferredSize(new Dimension(getSize().width,getHeight() - console.getSize().height));
       System.out.println("x3dPanel height = " + x3dPanel.getHeight());


       //Set up control panel

       outerControlPanel.setLayout(new GridBagLayout());
       if (showStatusBar) {
    	   outerControlPanel.add(statusBar, new GridBagConstraints(0, 1, 1, 1, 0, 0
                   , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0,0));
       
           statusBar.setText("Welcome to WebTOP");
       }
       
       
       outerControlPanel.setBackground(Color.BLACK);
       outerControlPanel.add(controlPanel, new GridBagConstraints(0, 0, 1, 1, 0,0
               , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
       outerControlPanel.add(getWSLPanel(), new GridBagConstraints(0, 2, 1, 1, 0,0
               , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
       
       //Add x3dPanel and controlPanel to the window
       //x3dPanel at 0,0 with width 4 and height 1, controlPanel at 1,1 with width 3 and height 1
       /*this.getContentPane().add(toolBar, new GridBagConstraints(0, 0, 4, 1, 1.0, 0.0
               , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0,0));
        this.getContentPane().add(x3dPanel, new GridBagConstraints(0, 1, 4, 1, 1.0, 1.0
               , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
       this.getContentPane().add(outerControlPanel, new GridBagConstraints(0, 2, 3, 2, 1.0, 0.0
               , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0,0));*/
       this.getContentPane().add(toolBar, new GridBagConstraints(0, 0, 4, 1, 0, 0
               , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0,0));
        this.getContentPane().add(x3dPanel, new GridBagConstraints(0, 1, 4, 1, 100, 100
               , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
       this.getContentPane().add(outerControlPanel, new GridBagConstraints(0, 2, 3, 2, 100, 20
               , GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0,0));
  
   }
   
   public void initNavPanel() {
	   navPanel = new NavigationPanel(getSAI(),getSAI().getNode(NAV_PANEL_NAME), (short)0, "Selects and changes your viewpoint in the scene.");
       navPanelScripter = new NavigationPanelScripter(navPanel,wslPlayer);
       navPanel.addListener(this);
   }

   public JFrame createWindow(Container container){ //STILL NEEDS TESTING(Matt)
       //Creates a second window with the passed container as its content,
       //returns the JFrame in case further modification is needed

       JFrame window = new JFrame();
       window.add(container);
       window.setVisible(true);
       return window;
   }
   
   public WApplication() {
       System.out.println("Default WApplication constructor called.  Module will not run.");
   }

   public WApplication(String title, String world) {
       this(title, world, false, false);
   }

   public WApplication(String title, String world, boolean showConsole) {
       this(title, world, false, showConsole);
   }
   

   //Constructor takes a URL for the X3D file to display
   public WApplication(String title, String world, boolean showStatusBar, boolean showConsole) {
       setTitle(title);

       //Initialize event variables
       widgetActive = false;

       //Setup the JFrame for customization
       worldURL = world;
       HashMap<String,Object> params = new HashMap<String,Object>();
       params.put("Xj3D_NavbarShown", Boolean.FALSE);
       params.put("Xj3D_LocationShown", Boolean.FALSE);
       params.put("Antialiased", Boolean.TRUE);
       params.put("Xj3D_AntialiasingQuality", "medium");
       
       //The 
       params.put("Xj3D_Culling_Mode", "none");
       if(showConsole)
           params.put("Xj3D_ShowConsole", Boolean.TRUE);
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       content = getContentPane();

       //Take care of anything that has to be done before
       //X3D initialization
       preconstructor();

       //Very last thing called is to show the WApplication
       setVisible(true);

       //Initiate the X3D variables
       initX3D(world, params);
       
       //Initialize all universal GUI features (the JFrame, etc.)
       initGUI(showStatusBar);

       //Initialize navigation panel in modules
       //As of 6/08, we cannot connect to nodes before the scene is added to the
       //GUI, so this must come after
       initNavPanel();
       
       //Called by module to connect X3DFields, Widgets, etc.
       setupX3D();

       //Called by module to layout the user interface for the module.
       setupGUI();

       pack();

       //View point setup.  Come up with a better method. [PC]
       try {
           mainView = getSAI().getScene().getNamedNode("MainVP");
           bind = (SFBool) mainView.getField("set_bind");
           bind.setValue(true);
       } catch (InvalidNodeException ine) {}

       //Called by module to set all interactive controls to their default values.
       setDefaults();
   }
}
