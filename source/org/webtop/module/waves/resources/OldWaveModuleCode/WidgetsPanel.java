package webtop.wave;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import webtop.wsl.client.*;
import webtop.wsl.event.*;

public class WidgetsPanel extends Panel implements ActionListener,ItemListener {
	private final WaveSimulation parent;
	private Engine engine;		//can't be final; is new each applet start()
	private final WidgetSwitcher switcher;
	private final WSLPlayer wslPlayer;

	private final Label selectSourceLabel;
	private final Choice sourceList;
	private final Button remove;
	private final Button widgets;
	private final Button grid;
	private final Button reset;

	private PoolWidget selectedWidget;
	private WaveSource selectedSource;	//null if currently selected widget doesn't exist or is not a source

	public static final int WIDGET_HIDE = 0;
	public static final int WIDGET_ICON = 1;
	public static final int WIDGET_FULL = 2;

	private int widgetVisible = WIDGET_ICON;
	private boolean autoSelect = false;
	private boolean gridVisible = true;

	public WidgetsPanel(WaveSimulation wave) {
		parent = wave;
		wslPlayer = parent.getWSLPlayer();
		setLayout(null);

		selectSourceLabel = new Label(" Select Source: ");
		add(selectSourceLabel);

		sourceList = new Choice();
		sourceList.setForeground(Color.black);
		sourceList.setBackground(Color.white);
		sourceList.addItemListener(this);
		add(sourceList);

		widgets = new Button("Show Widgets");
		widgets.setForeground(Color.black);
		widgets.setBackground(Color.gray.brighter());
		widgets.addActionListener(this);
		add(widgets);

		remove = new Button("Remove");
		remove.setEnabled(false);
		remove.setForeground(Color.black);
		remove.setBackground(Color.gray.brighter());
		remove.addActionListener(this);
		add(remove);

		grid = new Button("Hide Grid");
		grid.setForeground(Color.black);
		grid.setBackground(Color.gray.brighter());
		grid.addActionListener(this);
		add(grid);

		reset = new Button("Reset");
		reset.setForeground(Color.black);
		reset.setBackground(Color.gray.brighter());
		reset.addActionListener(this);
		add(reset);

		switcher = new WidgetSwitcher(wslPlayer);
		add(switcher);
	}

	public void setBounds(int x, int y, int width, int height) {
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
	}

	public void reset() {
		sourceList.removeAll();
		switcher.show(null);
		remove.setEnabled(false);
		grid.setLabel("Hide Grid");
		widgets.setLabel("Show Widgets");
		autoSelect = false;
		gridVisible = true;
		widgetVisible = WIDGET_ICON;
	}

	public void setEngine(Engine e) {
		engine = e;
		switcher.setEngine(e);
	}

	public WidgetSwitcher getSwitcher() {return switcher;}

	public PoolWidget getSelectedWidget() {
		return selectedWidget;
	}

	public WaveSource getSelectedSource() {
		return selectedSource;
	}

	public void setAutoSelect(boolean auto) {
		autoSelect = auto;
	}

	public void addSource(LinearSource s) {
		sourceList.addItem("Line Source " + engine.linearCounter);
		//if(autoSelect) show(s);
	}

	public void addSource(RadialSource s) {
		sourceList.addItem("Point Source " + engine.radialCounter);
		//if(autoSelect) show(s);
	}

	public void addSource(PluckedSource s) {
		sourceList.addItem("Plucked Source " + engine.pluckedCounter);
	}

	public void addSource(StruckSource s) {
		sourceList.addItem("Struck Source " + engine.struckCounter);
	}

	public void addWidget(SamplingStick s) {
		sourceList.addItem("Sampling Stick " + engine.sampleCounter);
		//if(autoSelect) show(s);
	}

	private void removeWidget(PoolWidget pw) {
		int index = engine.widgetIndex(pw);
		if(index==-1) return;
		if(engine.removeWidget(pw)) {
			sourceList.remove(index);
			show(sourceList.getSelectedIndex());
		}
	}

	private void removeWidget(int index) {
		if(engine.removeWidget(index)) {
			sourceList.remove(index);
			show(sourceList.getSelectedIndex());
		}
	}

	public void removeSourceFromList(int index) {
		sourceList.remove(index);
		show(sourceList.getSelectedIndex());
	}

	public boolean show(PoolWidget pw) {
		if(pw==null) {
			switcher.show(null);
			selectedWidget = selectedSource = null;
			remove.setEnabled(false);
			return false;
		}

		int index;
		if((index=engine.widgetIndex(pw))==-1) return false;

		if(selectedWidget!=null && selectedWidget!=pw) selectedWidget.hideWidgets();

		sourceList.select(index);
		selectedWidget = pw;
		selectedWidget.showWidgets();

		selectedSource=(selectedWidget instanceof WaveSource)?(WaveSource)selectedWidget:null;

		remove.setEnabled(true);

		// Switch the panel to show the appropriate parameters
		switcher.show(pw);

		widgets.setLabel("Hide Widgets");
		widgetVisible = WIDGET_FULL;

		return true;
	}

	public boolean show(int index) {
		return show(engine.getWidget(index));
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

	public int getWidgetVisible() {return widgetVisible;}
	public String widgetVisibilityString() {
		switch(widgetVisible) {
		case WIDGET_HIDE: return "hidden";
		case WIDGET_ICON: return "minimal";
		case WIDGET_FULL: return "visible";
		default:
			System.err.println("WidgetsPanel: bad visibility integer "+widgetVisible);
			return "";
		}
	}

	public void setWidgetVisible(int visible) {
		if(visible==WIDGET_HIDE) {
			widgets.setLabel("Show Markers");
			engine.setWidgetsVisible(false);
		} else if(visible==WIDGET_ICON) {
			widgets.setLabel("Show Widgets");
			if(selectedWidget!=null) selectedWidget.hideWidgets();
			engine.setWidgetsVisible(true);
		} else if(visible==WIDGET_FULL) {
			widgets.setLabel("Hide Widgets");
			if(selectedWidget!=null) selectedWidget.showWidgets();
			engine.setWidgetsVisible(true);
		} else return;
		widgetVisible = visible;
	}

	public void setGridVisible(boolean visible) {
		gridVisible = visible;
		if(visible) grid.setLabel("Hide Grid");
		else grid.setLabel("Show Grid");
	}

	public void itemStateChanged(ItemEvent e) {
		if(wslPlayer.isPlaying()) return;
		Object source = e.getSource();
		if(source==sourceList) {
			show(sourceList.getSelectedIndex());
			wslPlayer.recordMouseEntered(engine.getWidget(sourceList.getSelectedIndex()).getID());
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(wslPlayer.isPlaying()) return;
		Object source = e.getSource();
		if(source==reset) {
			engine.reset();
			wslPlayer.recordActionPerformed("action", "reset");
		} else if(source==grid) {
			gridVisible = !gridVisible;
			if(gridVisible) grid.setLabel("Hide Grid");
			else grid.setLabel("Show Grid");
			engine.setGridVisible(gridVisible);
			wslPlayer.recordActionPerformed("grid", gridVisible ? "visible" : "hidden");
		} else if(source==widgets) {
			switch (widgetVisible) {
			case WIDGET_HIDE: setWidgetVisible(WIDGET_ICON); break;
			case WIDGET_ICON: setWidgetVisible(WIDGET_FULL); break;
			case WIDGET_FULL: setWidgetVisible(WIDGET_HIDE); break;
			default: setWidgetVisible(WIDGET_ICON); break;
			}
			wslPlayer.recordActionPerformed("widgets",widgetVisibilityString());
		} else if(source==remove) {
			PoolWidget ws = engine.getWidget(sourceList.getSelectedIndex());
			if(sourceList.getSelectedIndex()>=0) {
				removeWidget(ws);
				wslPlayer.recordObjectRemoved(ws.getID());
			}
		}
	}

	public Dimension getMinimumSize() {
		Dimension d = parent.getSize();
		return new Dimension(d.width, 55);
	}

	public Dimension getPreferredSize() {
		Dimension d = parent.getSize();
		return new Dimension(d.width, 55);
	}
}
