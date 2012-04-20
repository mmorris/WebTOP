/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.twomedia;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.webtop.component.*;
//Scripting
import org.webtop.util.script.*;
import org.webtop.wsl.script.*;
import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;


public class SourcePanel extends JPanel implements ActionListener,ItemListener {
	private TwoMedia parent;
	private Engine engine;
	private SourceSwitcher switcher;
	private Controls twoMediaControls;

	private JLabel selectSourceLabel;
	private JComboBox sourceList; //not used in this module, but will be used in waves
	private JButton waveVisibility;
	private JButton widgets;
	private JButton grid;
	private JButton reset;
	private JButton arrows;
	private JLabel angleRefraction;
	private JLabel criticalAngle;
	private boolean arrowsVisible;

	private int linearCount = 0;
	private int mode = 0;
	private WaveSource selectedSource;

	public static final int WIDGET_HIDE = 0;
	public static final int WIDGET_ICON = 1;
	public static final int WIDGET_FULL = 2;

	private int widgetVisible = WIDGET_ICON;
	private boolean autoSelect = false;
	private boolean gridVisible = true;
	
	//Scripting elements
	private ButtonScripter waveVisibilityScripter;
	private ButtonScripter widgetsScripter; 
	private ButtonScripter gridScripter;
	private ButtonScripter resetScripter; 
	private ButtonScripter arrowsScripter; 
	private ChoiceScripter sourceListScripter; //just to be consistent

	public SourcePanel(TwoMedia wave, Controls tmControls) {
		parent = wave;
		twoMediaControls = tmControls;
		setLayout(new GridBagLayout());
		arrowsVisible = true;

		selectSourceLabel = new JLabel(" Select Source: ");
	
		sourceList = new JComboBox();
		sourceList.addItemListener(this);

		widgets = new JButton("Show Widgets");
		widgets.setVisible(true);
		widgets.addActionListener(this);
		add(widgets, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(1,2,1,2),0,0));

		waveVisibility = new JButton("Show Incident");
		waveVisibility.setEnabled(false);
		waveVisibility.addActionListener(this);
		add(waveVisibility, new GridBagConstraints(1,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(1,2,1,2),0,0));

		grid = new JButton("Hide Grid");
		grid.addActionListener(this);
		add(grid, new GridBagConstraints(2,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));

		reset = new JButton("Reset");
		reset.addActionListener(this);
		add(reset, new GridBagConstraints(3,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));

		arrows = new JButton("Hide Vectors");
		arrows.addActionListener(this);
		add(arrows, new GridBagConstraints(4,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		

		JLabel refractText = new JLabel(" Angle of Refraction: ");
		add(refractText, new GridBagConstraints(5,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		
		angleRefraction = new JLabel("30.0");
		add(angleRefraction, new GridBagConstraints(6,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		
		JLabel angleText = new JLabel(" Critical Angle: ");
		add(angleText, new GridBagConstraints(7,0,1,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));
		
		criticalAngle = new JLabel("None");
		add(criticalAngle, new GridBagConstraints(8,0,1,1,0,0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));

		switcher = new SourceSwitcher(wave);
		add(switcher, new GridBagConstraints(0,1,8,1,0,0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(1,2,1,2),0,0));

		//Setup scripting for the JButtons contained in this file
		waveVisibilityScripter = new ButtonScripter(waveVisibility, wave.getWSLPlayer(),
			null,"visible");
		widgetsScripter = new ButtonScripter(widgets, wave.getWSLPlayer(), null, 
				"widgets");
		gridScripter = new ButtonScripter(grid, wave.getWSLPlayer(), null, "grid");
		resetScripter = new ButtonScripter(reset, wave.getWSLPlayer(), null, "reset");
		arrowsScripter = new ButtonScripter(arrows, wave.getWSLPlayer(), null, "arrows");
		
	}

	public void reset() {
		sourceList.removeAll();
		linearCount = 0;
		switcher.show(switcher.NONE);
		waveVisibility.setEnabled(false);
		waveVisibility.setText("Show Incident");
		mode = 0;
		grid.setText("Hide Grid");
		widgets.setText("Show Widgets");
		autoSelect = false;
		gridVisible = true;
		widgetVisible = WIDGET_ICON;
		
	}

	public void setAngleOfRefraction(String a) {
		angleRefraction.setText(a);
	}

	public void setCriticalAngle(String a) {
		criticalAngle.setText(a);
	}

	public void setEngine(Engine e) {
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
		if(engine.removeSource(index)) removeSourceFromList(index);
	}

	public void removeSourceFromList(int index) {
		sourceList.remove(index);
		show(sourceList.getSelectedIndex());
	}

	public boolean show(WaveSource s) {
		if(s==null) {
			switcher.show(switcher.NONE);
			selectedSource = null;
			waveVisibility.setEnabled(false);
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
		waveVisibility.setEnabled(true);

		widgets.setText("Hide Widgets");
		widgetVisible = WIDGET_FULL;

		return true;
	}

	public boolean show(int index) {
		WaveSource s;
		if(index<0 || (s=engine.getSource(index))==null) {
			selectedSource = null;
			switcher.show(switcher.NONE);
			waveVisibility.setEnabled(false);
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

		waveVisibility.setEnabled(true);
		return true;
	}

	public void setAmplitude(float a) {switcher.setAmplitude(a);}

	public void setWavelength(float w) {switcher.setWavelength(w);}

	public void setAngle(float a) {switcher.setAngle(a);}

	public void setN1(float a) {switcher.setN1(a);}

	public void setN2(float a) {switcher.setN2(a);}

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

	public void setWavesVisible(int modeInt) {
		switch(modeInt) {
		case Engine.REFLECTED_ONLY:
			waveVisibility.setText("Show Both");
			break;
		case Engine.INCIDENT_AND_REFLECTED:
			waveVisibility.setText("Show Incident");
			break;
		case Engine.INCIDENT_ONLY:
			waveVisibility.setText("Show Reflected");
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

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if(source==sourceList) {
			show(sourceList.getSelectedIndex());
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source==reset) {
			engine.reset();
			//Reset the Play Button Text Accordingly [JD]
			if(twoMediaControls.getPlayButtonState() == 1){
				twoMediaControls.getPlayButton().doClick();
			}
			if(arrows.getText() == "Show Vectors"){
				arrows.doClick();
			}
		} else if(source==grid) {
			gridVisible = !gridVisible;
			if(gridVisible) 
				grid.setText("Hide Grid");
			else 
				grid.setText("Show Grid");
			engine.setGridVisible(gridVisible);
		} else if(source==widgets) {
			switch (widgetVisible) {
			case WIDGET_HIDE: setWidgetVisible(WIDGET_ICON); break;
			case WIDGET_ICON: setWidgetVisible(WIDGET_FULL); break;
			case WIDGET_FULL: setWidgetVisible(WIDGET_HIDE); break;
			default: setWidgetVisible(WIDGET_ICON); break;
			}
		} else if(source==waveVisibility) {
			mode=(mode+1)%3;
			setWavesVisible(mode);
		} else if(source == arrows) setVectorsVisible(!arrowsVisible);
	}

	public boolean vectorsVisible() {
		return arrowsVisible;
	}

	//implement the toWSLNode method to call from TwoMedia
	protected void toWSLNode(WSLNode node){
		switcher.toWSLNode(node);
	}
}
