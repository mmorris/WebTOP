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
 * <p> Title: Waves Three Media </p>
 * <p> Description: The X3D version of the 
 * 	Optics Project for the Web (WebTOP) </p>
 * <p> Company: MSU Department of Physics and Astronomy </p>
 * 
 * @author Yong Tze Che Updated by: Matt Hogan, Jeremy Davis
 * @version 0.0
 */

package org.webtop.module.twomedia;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.applet.Applet;
import org.web3d.x3d.sai.*;

//import vrml.external.field.*;

import org.webtop.component.*;
import org.webtop.util.*;
import org.webtop.x3d.*;

//import org.webtop.x3d.widget.NavigationPanel;
//import org.webtop.util.script.NavigationPanelScripter;
import org.webtop.wsl.client.*;
import org.webtop.wsl.script.WSLNode;
import org.webtop.wsl.event.*;

public class TwoMedia extends WApplication /*implements WSLScriptListener*/ {
    /*This versionUID can be removed, 
     just added to shut Eclipse up -MH*/
	private static final long serialVersionUID = 1L;
	
    //private SAI sai;
    private Engine engine;
    private Controls controls;
    private SourcePanel sourcePanel;

	protected String getModuleName() {
        return "";
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
        return "Updated to WApplication by: Matt Hogan and Jeremy Davis";
    }

    protected Component getFirstFocus() {
        return null;
    }

    public static void main(String[] args) {
        TwoMedia twomedia = new TwoMedia("Two Media", "/org/webtop/x3dscene/TwoMedia.x3dv");
        
    }
    
    public TwoMedia(String title, String world)
    {
    	super(title, world, true, false);
    	engine = new Engine(this);
    	engine.start();
    }
    
    protected void setupGUI() {
       	controls = new Controls(this);

        sourcePanel = new SourcePanel(this, controls);
        
        JPanel myPanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        myPanel.setLayout(layout);
        myPanel.add(controls, new GridBagConstraints(0,0,1,1,1,0, GridBagConstraints.CENTER,
        				GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
        myPanel.add(sourcePanel, new GridBagConstraints(0,1,1,1,1,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
        addToConsole(myPanel);
        
        //set up the toolbar
        ToolBar toolbar = getToolBar();
        toolbar.addBrowserButton("Directions", "/org/webtop/html/twomedia/directions.html");
        toolbar.addBrowserButton("Theory", "/org/webtop/html/twomedia/theory.html");
		toolbar.addBrowserButton("Examples", "/org/webtop/html/twomedia/examples.html");
		toolbar.addBrowserButton("Exercises", "/org/webtop/html/twomedia/exercises.html");
		toolbar.addBrowserButton("Images", "/org/webtop/html/twomedia/images.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
	}

    protected void setupX3D() {
    	
    }

    protected void setupMenubar() {
    }

    protected void setDefaults() {
    }

    public void invalidEvent(String node, String event) {
    }


    public void start() {

        engine = new Engine(this);

        engine.start();
    }

    public SourcePanel getSourcePanel() {
        return sourcePanel;
    }

    public Controls getControls() {
        return controls;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void stop() {
        engine.play();
        engine.exit();
    }

    // ---------------------------------------------------------------------------------
    // WSL Methods
    // ---------------------------------------------------------------------------------

    public String getWSLModuleName() {
        return "rrwaves";
    }

    protected void  toWSLNode(WSLNode node) {
    	//call other toWSLNode methods to get all of the information needed for scripting
    	super.toWSLNode(node);
    	controls.toWSLNode(node);
    	sourcePanel.toWSLNode(node);

    }
    /////***********END WSL METHODS*******************//////
    
    
}
