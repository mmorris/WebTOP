package org.webtop.module.polarization;

import org.webtop.component.WApplication;
import org.webtop.module.polarization.ControlPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.*;
import javax.swing.*;
import java.util.HashMap;
import java.awt.Component;

import org.sdl.gui.numberbox.*;
import org.webtop.wsl.script.WSLNode;
import org.webtop.x3d.widget.*;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;
import org.webtop.util.*;
import org.webtop.component.*;
import org.webtop.module.wavefront.Engine;



public class Polarization extends WApplication {

	private Engine engine;
	private Animation anim;
	private SourcePanel sourcePanel;
	private ControlPanel controlPanel;
	private FilterPanel filterPanel;
	static private Polarization polarization;



	protected void setDefaults() {

	}

	protected void setupMenubar() {

	}

	protected Component getFirstFocus() {
		return controlPanel;
	}

	protected String getAuthor() {
		return "test";
	}

	protected String getDate() {
		return "date";
	}

	protected int getMajorVersion() {return 6;}
	protected int getMinorVersion() {return 1;}
	protected int getRevision() {return 1;}
	protected String getModuleName() { return "Polarization";}

	//Initiate the X3D variables


	protected void initX3D(String world, HashMap params) {

	}

	//Called by module to connect X3DFields, Widgets, etc.
	protected void setupX3D() {
	
	}

	//Called by module to layout the user interface for the module.
	protected void setupGUI() {
		controlPanel = new ControlPanel(polarization);
		filterPanel  = new FilterPanel(polarization);
		sourcePanel  = new SourcePanel(polarization);
		addToConsole(controlPanel);
		addToConsole(filterPanel);
		addToConsole(sourcePanel);
	}


	public static void main(String[] args) 
	{
		polarization = new Polarization("Polarization", "Polarization.x3dv");
	}

	public Polarization(String title, String world) 
	{
		super(title, world, true);
	}


	public void invalidEvent(String p1, String p2) {


	}
	
	
	
	public SourcePanel getSourcePanel() {return sourcePanel;}
	public ControlPanel getControlPanel() {return controlPanel;}
	public FilterPanel getFilterPanel() {return filterPanel;}

	@Override
	protected void toWSLNode(WSLNode node) {
		// TODO Auto-generated method stub
		
	}

	public String getWSLModuleName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
	