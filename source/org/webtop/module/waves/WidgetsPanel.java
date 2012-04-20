//WidgetsPanel.java
//Updated June 6 2008 by Jeremy Davis 

package org.webtop.module.waves;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;

import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;

public class WidgetsPanel extends JPanel implements ActionListener, ItemListener {

	private final Waves parent; 
	private Engine engine; //can't be final...it's new every applet start [JD]
	private final WidgetSwitcher switcher; //must implement this class [JD]
	private final WSLPlayer wslPlayer; 
	
	private final JLabel selectSourceLabel; 
	private final JComboBox sourceList; 
	private final JButton remove; 
	private final JButton widgets; 
	private final JButton grid; 
	private final JButton reset; 
	
	private PoolWidget selectedWidget; //Must implement this class as well [JD]
	//selectedSource is null if currently selected source doesn't exist or isn't a source [JD]
	private WaveSource selectedSource; //Must implement this class also [JD]
	
	public static final int WIDGET_HIDE = 0;
	public static final int WIDGET_ICON = 1; 
	public static final int WIDGET_FULL = 2;
	
	private int widgetVisible = WIDGET_ICON; 
	private boolean autoSelect = false; 
	private boolean gridVisible = true; 
	
	public WidgetsPanel(Waves wave){
		parent = wave; 
		wslPlayer = parent.getWSLPlayer();
		setLayout(null); //? [JD]
		
		selectSourceLabel = new JLabel(" Select Source: ");
		add(selectSourceLabel);
		
		
		sourceList = new JComboBox();
		sourceList.addItemListener(this);
		add(sourceList);
		
		widgets = new JButton("Show Widgets");
		widgets.addActionListener(this);
		add(widgets);
		
		remove = new JButton("Remove");
		remove.addActionListener(this);
		add(remove);
		
		grid = new JButton("Hide Grid");
		grid.addActionListener(this);
		add(grid);
		
		reset = new JButton("Reset"); 
		reset.addActionListener(this);
		add(reset);
		
		switcher = new WidgetSwitcher(wslPlayer);
		add(switcher);
	}
	
	//This method was in the old module, but probably going to have to use GridBagLayout
	//so going to comment it out for now, leaving it just in case. [JD]
	/*public void setBounds(int x, int y, int width, int height) {
		Dimension d;
		super.setBounds(x, y, width, height);

		d = getSize();

		selectSourceLabel.setBounds(10, 0, 80, 20);
		sourceList.setBounds(95, 0, 120, 20);
		widgets.setBounds(220, 0, 80, 20);
		remove.setBounds(305, 0, 80, 20);
		grid.setBounds(d.width - 90, 0, 80, 20);
		reset.setBounds(d.width-175, 0, 80, 20);
		switcher.setBounds(10, 22, d.width-20, 28);
	}*/
	
	public void reset(){
		sourceList.removeAll();
		switcher.show(null);
		remove.setEnabled(false);
		grid.setText("Hide Grid");
		widgets.setText("Show Widgets");
		autoSelect = false;
		gridVisible = true; 
		widgetVisible = WIDGET_ICON;
	}
	
	public void setEngine(Engine e){
		engine = e; 
		switcher.setEngine(e);
	}
	
	public WidgetSwitcher getSwitcher(){
		return switcher; 
	}
	
	public PoolWidget getSelectedWidget(){
		return selectedWidget; 
	}
	
	public WaveSource getSelectedSource(){
		return selectedSource; 
	}
	
	public void setAutoSelect(boolean auto){
		autoSelect = auto;
	}
	
	public void addSource(LinearSource s){
		sourceList.addItem("Line Source" + engine.linearCounter);
	}
	//Uncomment these when working with other widgets
	/*
	public void addSource(RadialSource s){
		sourceList.addItem("Point Source" + engine.radialCounter);
	}
	
	public void addSource(PluckedSource s){
		sourceList.addItem("Plucked Source" + engine.pluckedCounter);
	}
	*/
	//Uncomment when working with other widgets
	/*
	public void addSource(StruckSource s){
		sourceList.addItem("Struck Source" + engine.struckCounter);
	}
	
	public void addWidget(SamplingStick s){
		sourceList.addItem("Sampling Stick" + engine.sampleCounter);
	}
	*/
	private void removeWidget(PoolWidget pw){
		int index = engine.widgetIndex(pw);
		if(index == -1)
			return;
		sourceList.remove(index);
		show(sourceList.getSelectedIndex());
	}
	
	public void removeWidget(int index){
		if(engine.removeWidget(index)){
			sourceList.remove(index);
			show(sourceList.getSelectedIndex());
		}
	}
	
	public void removeSourceFromList(int index){
		sourceList.remove(index);
		show(sourceList.getSelectedIndex());
	}
	
	public boolean show(PoolWidget pw){
		if(pw == null){
			switcher.show(null);
			selectedWidget = selectedSource = null;
			remove.setEnabled(false);
			return false;
		}
		
		int index; 
		if((index=engine.widgetIndex(pw)) == -1) 
				return false; 
		
		if(selectedWidget != null && selectedWidget != pw)
			selectedWidget.hideWidgets();
		
		sourceList.setSelectedIndex(index);
		selectedWidget = pw; 
		selectedWidget.showWidgets();
		
		selectedSource = (selectedWidget instanceof WaveSource) ? (WaveSource) selectedWidget:null; 
		
		remove.setEnabled(true);
		
		//Switch the panel to show the appropriate parameters [JD]
		switcher.show(pw);
		
		widgets.setText("Hide Widgets");
		widgetVisible = WIDGET_FULL;
		
		return true; 
	}
	
	public boolean show(int index){
		return show(engine.getWidget(index));
		
		//Commented out in the old module [JD]
		/* This single line ought to be able to replace all this... [Davis]
		PoolWidget pw;
		if(index<0 || (pw=engine.getWidget(index))==null) {
			selectedWidget = null;
			switcher.show(switcher.NONE);
			remove.setEnabled(false);
			return false;
		}

		if(selectedWidget!=null && selectedWidget!=pw) {
			selectedWidget.hideWidgets();
		}

		switcher.show(pw);

		sourceList.select(index);
		selectedWidget = pw;
		selectedWidget.showWidgets();
		engine.setWidgetsVisible(true);
		widgets.setLabel("Hide Widgets");
		widgetVisible = WIDGET_FULL;

		remove.setEnabled(true);
		return true; */
	}
	
	public int getWidgetVisible(){
		return widgetVisible;
	}
	
	public String widgetVisibilityString(){
		switch(widgetVisible){
		case WIDGET_HIDE: 
			return "hidden";
		case WIDGET_ICON:
			return "minimal";
		case WIDGET_FULL: 
			return "visible";
		default:
			System.err.println("WidgetsPanel::Bad Visibility Integer: " + widgetVisible);
			return "";
		}
	}
	
	public void setWidgetVisible(int visible){
		if(visible==WIDGET_HIDE){
			widgets.setText("Show Markers");
			engine.setWidgetsVisible(false);
		}
		else if(visible==WIDGET_ICON){
			widgets.setText("Show Widgets");
			if(selectedWidget!=null)
				selectedWidget.hideWidgets();
			engine.setWidgetsVisible(true);
		}
		else if(visible==WIDGET_FULL){
			widgets.setText("Hide Widgets");
			if(selectedWidget!=null)
				selectedWidget.showWidgets();
			engine.setWidgetsVisible(true);
		}
		else 
			return;
		widgetVisible = visible;
	}
	
	public void setGridVisible(boolean visible){
		gridVisible = visible; 
		if(visible)
			grid.setText("Hide Grid");
		else 
			grid.setText("Show Grid");
	}
	
	//Implement ItemListener
	public void itemStateChanged(ItemEvent e){
		if(wslPlayer.isPlaying())
			return; 
		
		Object source = e.getSource();
		if(source == sourceList){
			show(sourceList.getSelectedIndex());
		}
	}
	
	
	//Implement ActionListener
	public void actionPerformed(ActionEvent e){
		if(wslPlayer.isPlaying())
			return;
		
		Object source = e.getSource();
		if(source == reset){
			engine.reset();
		}
		else if(source == grid){
			gridVisible = !gridVisible; 
			if(gridVisible)
				grid.setText("Hide Grid");
			else
				grid.setText("Show Grid");
			engine.setGridVisible(gridVisible);
		}
		else if(source == widgets){
			switch(widgetVisible){
			case WIDGET_HIDE:
				setWidgetVisible(WIDGET_ICON);
				break;
			case WIDGET_ICON: 	
				setWidgetVisible(WIDGET_FULL);
				break;
			case WIDGET_FULL:	
				setWidgetVisible(WIDGET_HIDE);
				break;
			default: 
				setWidgetVisible(WIDGET_ICON);
				break;
			}
		}
		else if(source == remove){
			PoolWidget ws = engine.getWidget(sourceList.getSelectedIndex());
			if(sourceList.getSelectedIndex() >= 0){
				removeWidget(ws);
			}
		}
	}
	
	//These methods were used in the old module to set sizes...keeping them just in case [JD]
	
	/*public Dimension getMinimumSize() {
		Dimension d = parent.getSize();
		return new Dimension(d.width, 55);
	}

	public Dimension getPreferredSize() {
		Dimension d = parent.getSize();
		return new Dimension(d.width, 55);
	}*/
	
}
