/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.threemedia;

import javax.swing.JPanel;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.webtop.wsl.client.*;
import org.webtop.wsl.script.*;
import org.webtop.util.script.*;

public class SourcePanel extends JPanel implements ActionListener, ItemListener{
	
	public static final long serialVersionUID = 0; //to shut eclipse up
	//Declare Instance Variables
	private ThreeMedia parent; 
	private Engine engine;
	private SourceSwitcher switcher;
	private Controls threeMediaControls;
	
	private JLabel selectSourceLabel;
	private JComboBox sourceList;
	
	private JLabel angleRefraction1;
	private JLabel angleRefraction2;
	private JLabel criticalAngle1;
	private JLabel criticalAngle2;
	private boolean arrowsVisible;
	
	//Taken from Controls.java [JD]
	private JButton selectWaves;
	private JButton widgets;
	private JButton grid;
	private JButton reset;
	private JButton arrows;
	
	private int linearCount = 0;
	private int mode = 0;
	private WaveSource selectedSource;
	
	public static final int WIDGET_HIDE = 0;
	public static final int WIDGET_ICON = 1;
	public static final int WIDGET_FULL = 2;

	private int widgetVisible = WIDGET_ICON;
	private boolean autoSelect = false;
	private boolean gridVisible = true;
	
	//WSL Scripting Stuffs
	private ButtonScripter selectWavesScripter, 
						   widgetsScripter, 
						   gridScripter, 
						   resetScripter,
						   arrowsScripter;
	
	
	//Constructor 
	public SourcePanel(ThreeMedia wave, Controls tmControls){
		parent = wave;
		threeMediaControls = tmControls;
		setLayout(new GridBagLayout());
		
		selectSourceLabel = new JLabel(" Select Source: ");

		sourceList = new JComboBox();
		sourceList.addItemListener(this);
		
		//To push all of the buttons over some, add a JLabel [JD]
		JLabel push = new JLabel("    ");
		add(push, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, 
				GridBagConstraints.NONE, new Insets(1,2,1,2), 0,0));
		
		widgets = new JButton("Show Widgets");
		widgets.setVisible(true);
		widgets.addActionListener(this);
		add(widgets, new GridBagConstraints(1,0,1,1,0,0, GridBagConstraints.EAST,
				GridBagConstraints.BOTH, new Insets(1,2,1,2),0,0));
		
		selectWaves = new JButton("Show Incident");
		selectWaves.setEnabled(false);
		selectWaves.addActionListener(this);
		add(selectWaves, new GridBagConstraints(2,0,1,1,0,0, GridBagConstraints.EAST,
				GridBagConstraints.BOTH, new Insets(1,2,1,2),0,0));
		
		grid = new JButton("Hide Grid");
		grid.addActionListener(this);
		add(grid, new GridBagConstraints(3,0,1,1,0,0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		
		reset = new JButton("Reset");
		reset.addActionListener(this);
		add(reset, new GridBagConstraints(4,0,1,1,0,0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(1,2,1,2),0,0));
		
		arrows = new JButton("Hide Vectors");
		arrows.addActionListener(this);
		add(arrows, new GridBagConstraints(5,0,1,1,0,0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		
		JLabel angleText1 = new JLabel("Critical Angle1:  ");
		add(angleText1, new GridBagConstraints(0,1,1,1,0,0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(1,2,1,0),0,0));
		criticalAngle1 = new JLabel("None");
		add(criticalAngle1, new GridBagConstraints(1,1,1,1,0,0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(1,2,1,0),0,0));

		JLabel refractText1 = new JLabel("Angle of Refraction1:");
		add(refractText1, new GridBagConstraints(2,1,1,1,0,0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(1,2,1,5),0,0));
		angleRefraction1 = new JLabel("30.0");
		add(angleRefraction1, new GridBagConstraints(3,1,1,1,0,0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));

		JLabel angleText2 = new JLabel("Critical Angle2:  ");
		add(angleText2, new GridBagConstraints(4,1,1,1,0,0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		criticalAngle2 = new JLabel("None");
		add(criticalAngle2, new GridBagConstraints(5,1,1,1,0,0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		
		//Because GridBagConstraints is stubborn [JD]
		JLabel empty = new JLabel(" ");
		add(empty, new GridBagConstraints(6,1,1,1,0,0, GridBagConstraints.WEST, 
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));

		JLabel refractText2 = new JLabel("Angle of Refraction2:");
		add(refractText2, new GridBagConstraints(7,1,1,1,0,0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		angleRefraction2 = new JLabel("30.0");
		add(angleRefraction2, new GridBagConstraints(8,1,1,1,0,0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		
		switcher = new SourceSwitcher(wave);
		add(switcher, new GridBagConstraints(0,2,8,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		
		//added so that the Custom option in the drop down menu will not be overlapped by the status bar[JD]
		add(new JLabel(" "), new GridBagConstraints(5,3,1,1,0,0, GridBagConstraints.CENTER, 
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		
		selectWavesScripter = new ButtonScripter(selectWaves, wave.getWSLPlayer(), null, "visible");
		widgetsScripter = new ButtonScripter(widgets, wave.getWSLPlayer(), null, "widgets");
		gridScripter = new ButtonScripter(grid, wave.getWSLPlayer(), null, "grid");
		resetScripter = new ButtonScripter(reset, wave.getWSLPlayer(), null, "reset");
		arrowsScripter = new ButtonScripter(arrows, wave.getWSLPlayer(), null, "arrows");

	}
	
	public void setBounds(int x, int y, int width, int height) {
		Dimension d;
		super.setBounds(x, y, width, height);

		d = getSize();

		selectSourceLabel.setBounds(10, 0, 80, 20);
		sourceList.setBounds(95, 0, 120, 20);

		switcher.setBounds(10, 22, d.width-20, 28);
	}
	
	public void reset() {
		sourceList.removeAll();
		linearCount = 0;
		switcher.show(switcher.NONE);
		
		//Taken from Controls.java[JD]
		widgets.setText("Show Widgets");
		selectWaves.setEnabled(false);
		selectWaves.setText("Incident Only");
		grid.setText("Hide Grid");

		autoSelect = false;
		gridVisible = true;
		widgetVisible = WIDGET_ICON;
	}
	
	public void setFirstAngleOfRefraction(String a) {
		angleRefraction1.setText(a);
	}

	public void setFirstCriticalAngle(String a) {
		criticalAngle1.setText(a);
	}

	public void setSecondAngleOfRefraction(String a) {
		angleRefraction2.setText(a);
	}

	public void setSecondCriticalAngle(String a) {
		criticalAngle2.setText(a);
	}
	
	//set the engine for this class
	public void setEngine(Engine e){
		engine = e;
		switcher.setEngine(e);
	}
	
	public WaveSource getSelectedSource() {
		return selectedSource;
	}
	
	public void setAutoSelect(boolean auto) {
		autoSelect = auto;
	}
	
	public void addSource(LinearSource s) {
		linearCount++;
		sourceList.addItem("Line Source " + linearCount);
		if(autoSelect) show(s);
	}	
	
	private void removeSource(WaveSource s) {
		int index = engine.sourceIndex(s);
		if(index==-1) return;
		if(engine.removeSource(s)) {
			sourceList.remove(index);
			show(sourceList.getSelectedIndex());
		}
	}
	
	private void removeSource(int index) {
		if(engine.removeSource(index)) {
			sourceList.remove(index);
			show(sourceList.getSelectedIndex());
		}
	}

	public void removeSourceFromList(int index) {
		sourceList.remove(index);
		show(sourceList.getSelectedIndex());
	}
	
	public boolean show(WaveSource s) {
		if(s==null) {
			switcher.show(switcher.NONE);
			selectedSource = null;
			selectWaves.setEnabled(false);

			return false;
		}

		int index;
		if((index=engine.sourceIndex(s))==-1) return false;

		if(selectedSource!=null && selectedSource!=s) selectedSource.hideWidgets();

		// Switch the panel to show the appropriate parameters
		switcher.show(s);

		sourceList.setSelectedIndex(index);
		selectedSource = s;
		selectedSource.showWidgets();
		selectWaves.setEnabled(true);


		widgets.setText("Hide Widgets");
		widgetVisible = WIDGET_FULL;

		return true;
	}
	
	public boolean show(int index) {
		WaveSource s;
		if(index<0 || (s=engine.getSource(index))==null) {
			selectedSource = null;
			switcher.show(switcher.NONE);
			selectWaves.setEnabled(false);

			return false;
		}

		if(selectedSource!=null && selectedSource!=s) {
			selectedSource.hideWidgets();
		}

		switcher.show(s);

		sourceList.setSelectedIndex(index);
		selectedSource = s;
		selectedSource.showWidgets();
		engine.setWidgetsVisible(true);

		widgets.setText("Hide Widgets");
		widgetVisible = WIDGET_FULL;

		selectWaves.setEnabled(true);
		return true;
	}

	public void setAmplitude(float a) {
		switcher.setAmplitude(a);
	}

	public void setWavelength(float w) {
		switcher.setWavelength(w);
	}

	public void setAngle(float a) {
		switcher.setAngle(a);
	}

	public void setN1(float a) {
		switcher.setN1(a);
	}

	public void setN2(float a) {
		switcher.setN2(a);
	}

	public void setN3(float a) {
		switcher.setN3(a);
	}

	public void setDistance(float a) {
		switcher.setDistance(a);
	}
	
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if(source==sourceList) {
			show(sourceList.getSelectedIndex());
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if(source==widgets) {
			switch (widgetVisible) {
			case WIDGET_HIDE: setWidgetVisible(WIDGET_ICON); break;
			case WIDGET_ICON: setWidgetVisible(WIDGET_FULL); break;
			case WIDGET_FULL: setWidgetVisible(WIDGET_HIDE); break;
			default: setWidgetVisible(WIDGET_ICON); break;
			}
		}
		
		else if(source==selectWaves) {
			mode=(mode+1)%3;
			setWavesVisible(mode);
		}
		
		else if(source==grid) {
			gridVisible = !gridVisible;
			if(gridVisible) //grid.setLabel("Hide Grid");
				grid.setText("Hide Grid");
			else //grid.setLabel("Show Grid");
				grid.setText("Show Grid");
			engine.setGridVisible(gridVisible);
		}
		
		else if(source == reset){
			engine.reset();
			if(threeMediaControls.getPlayButtonState() == 1){
				threeMediaControls.getPlayButton().doClick();
			}
			if(arrows.getText() == "Show Vectors"){ //reset the vectors label
				arrows.doClick();
			}
		}
		
		else if(source == arrows) {
			setVectorsVisible(!arrowsVisible);
		}
	}
	
	//Implemented from Controls [JD]
	public void setWavesVisible(int modeInt) {
		switch(modeInt) {
		case Engine.REFLECTED_ONLY:
			selectWaves.setText("Show Both");
			break;
		case Engine.INCIDENT_AND_REFLECTED:
			selectWaves.setText("Show Incident" );
			break;
		case Engine.INCIDENT_ONLY:
			selectWaves.setText("Show Reflected");
			break;
		}

		engine.setWhichWavesAreVisible(modeInt);
	}
	
	public void setWidgetVisible(int visible) {
		if(visible==WIDGET_HIDE) {
			widgets.setText("Show Markers");
			engine.setWidgetsVisible(false);
		} else if(visible==WIDGET_ICON) {
			widgets.setText("Show Widgets");
			if(selectedSource!=null) selectedSource.hideWidgets();
			engine.setWidgetsVisible(true);
		} else if(visible==WIDGET_FULL) {
			widgets.setText("Hide Widgets");
			if(selectedSource!=null) selectedSource.showWidgets();
			engine.setWidgetsVisible(true);
		} else return;
		widgetVisible = visible;
	}
	
	public void setGridVisible(boolean visible) {
		gridVisible = visible;
		if(visible) 
			grid.setText("Hide Grid");
		else 
			grid.setText("Show Grid");
	}
	
	public void setVectorsVisible(boolean vis) {
		if(vis) {
			engine.setVectorsVisible(true);
			arrows.setText("Hide Vectors");
			arrowsVisible = true;
		} else {
			engine.setVectorsVisible(false);
			arrows.setText("Show Vectors");
			arrowsVisible = false;
		}
	}
	
	//WSL Method used in ThreeMedia.java [JD]
	protected void toWSLNode(WSLNode node){
		switcher.toWSLNode(node);
	}
}
