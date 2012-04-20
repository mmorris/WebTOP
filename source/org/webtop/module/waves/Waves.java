/**
 * <p>Title: Wave Simulation</p>
 * 
 * <p>Description: The X3D version of The Optics Project
 * for the web(WebTOP)</p>
 * 
 * <p>Company:MSU Department of Physics and Astronomy</p>
 * 
 * @author Davis Herring updated by Jeremy Davis
 *@version 0.0
 */

package org.webtop.module.waves;

import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;

import org.webtop.component.WApplication;
import org.webtop.component.*;
import org.webtop.wsl.script.WSLNode;
import org.webtop.wsl.script.*;
import org.webtop.wsl.client.*;
import org.web3d.x3d.sai.*;
//import org.web3d.sai.*;
import org.webtop.x3d.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Waves extends WApplication implements ActionListener {
	
	private SAI sai; 
	
	private Engine engine;
	private WidgetsPanel widgetsPanel;
	private Controls controls; 
	public StatusBar statusBar = getStatusBar();
	
	
	//private WSLPlayer wslPlayer = getWSLPlayer(); //For the other class' sake [JD]

	public Waves(String title, String world){
		super(title, world, true, true);
		System.out.println("Waves.start is creating a new engine");
		engine = new Engine(this);
	}
	
	protected String getAuthor() {
		return "Jeremy Davis";
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
		return "WavesSimulation";
	}

	protected int getRevision() {
		return 1;
	}

	// Initial Setup Methods \\
	
	protected void setupGUI() {
		//setLayout(new FlowLayout()); //YEAH RIGHT...know I'm going to have to use GB [JD]
		getStatusBar().setText("Welcome to the Wave Simulation module");
		
		controls = new Controls(this);
		add(controls);
		
		widgetsPanel = new WidgetsPanel(this);
		add(widgetsPanel);
	}

	protected void setupX3D() {

	}

	protected void setupMenubar() {

	}
	
	// End Initial Setup Methods \\
	
	
	// Event Handling Methods \\ 
	
	//May not need ActionListener after all [JD]
	public void actionPerformed(ActionEvent e){
		
	}
	
	public void invalidEvent(String node, String event) {
		getStatusBar().setWarningText("ERROR: Cannot load module.  See Java console for details");
	}
	
	protected void setDefaults() {

	}
	
	// WSL Methods \\
	
	protected void toWSLNode(WSLNode node) {
		super.toWSLNode(node);
	}
	
	public String getWSLModuleName() {
		return "WavesSimulation";
	}
	
	// END WSL Methods \\

	
	//Native waves methods \\
	public WidgetsPanel getSourcePanel() { return widgetsPanel;}

	public Controls getControls() { return controls;}
	
	//Can't use this method...it overrides WApplication.getStatusBar() which is not good...
	//Made the StatusBar Object "statusBar" public so it is visible to all classes that use the 
	//status bar [JD]
	//public StatusBar getStatusBar() {return statusBar;}
	
	//Every other class seems to need access to the WSL Player...return it [JD]
	//Calling this method from other classes calls WApplication.getWSLPlayer()...this method is not needed [JD]
	/*public WSLPlayer getWavesWSLPlayer() { 
		return wslPlayer; 
	}*/
	
	//Animation methods
	public void start(){
		sai = new SAI();
		//System.out.println("Waves.start is creating a new engine");
		//engine = new Engine(this);
		
		engine.start();
	}
	
	//may not need to synchronize on stop [JD]
	public synchronized void stop(){
		//Snap the engine out of any kind of stupor it may be in, and kill off its thread
		synchronized(engine){engine.notifyAll(); }
		
		engine.exit();
		engine = null;    //Why? [JD]
		sai.scene = null; //Why? [JD]
	}
	
	
	
	public static void main(String[] args) {
		Waves waves = new Waves("WavesSimulation", "/org/webtop/x3dscene/WaveSimulation.x3dv");
	}

}
