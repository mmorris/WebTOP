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
 * @author Ben Wyser Updated by: Jeremy Davis
 * @version 0.0
 */


package org.webtop.module.threemedia;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.applet.Applet;
import org.web3d.x3d.sai.*;
import org.webtop.util.*;
import org.webtop.x3d.*;

import org.webtop.component.VerticalLayout;
import org.webtop.component.WApplication;
import org.webtop.wsl.script.WSLNode;
import org.webtop.component.*;

import org.webtop.wsl.client.*;
import org.webtop.wsl.script.WSLNode;
import org.webtop.wsl.event.*;

public class ThreeMedia extends WApplication {
	
	private Engine engine;
	private SourcePanel sourcePanel;
	private Controls controls;

	//Added to shut eclipse up - JD
	public static final long serialVersionUID = 1;
	
	protected String getAuthor() {
		return "Updated to Use WApplication by: Jeremy Davis";
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
		return "WavesThreeMedia";
	}


	protected int getRevision() {
		return 1;
	}

	
	protected void setDefaults() {

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
		toolbar.addBrowserButton("Directions", "/org/webtop/html/threemedia/directions.html");
		toolbar.addBrowserButton("Theory", "/org/webtop/html/threemedia/theory.html");
		toolbar.addBrowserButton("Examples", "/org/webtop/html/threemedia/examples.html");
		toolbar.addBrowserButton("Exercises", "/org/webtop/html/threemedia/exercises.html");
		toolbar.addBrowserButton("Images", "/org/webtop/html/threemedia/images.html");
		toolBar.addBrowserButton("About", "/org/webtop/html/license.html");
		
	}

	
	protected void setupMenubar() {

	}

	
	protected void setupX3D() {
		
	}


	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
		controls.toWSLNode(node);
		sourcePanel.toWSLNode(node);
	}

	public void invalidEvent(String node, String event) {
		statusBar.setWarningText("ERROR: Cannot load module.  See Java Console for details.");
		setEnabled(false);
	}

	public String getWSLModuleName() {
		return "threemedia";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ThreeMedia threeMedia = new ThreeMedia("Three Media", 
				"/org/webtop/x3dscene/ThreeMedia.x3dv");

	}
	
	public ThreeMedia(String title, String world){
		super(title, world, true, false);
		engine = new Engine(this);
		engine.start();
		
	}
	
	public SourcePanel getSourcePanel(){
		return sourcePanel;
	}
	
	public Controls getControls(){
		return controls;
	}
	
	public StatusBar getStatusBar(){
		return statusBar;
	}
	
	//THREE MEDIA METHODS \\ 
	public void start(){
		engine = new Engine(this);
		engine.start();
	}
	
	public synchronized void stop(){
		engine.play();
		engine.exit();
	}

}
