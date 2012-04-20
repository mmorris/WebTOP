		package org.webtop.module.polarization;

import javax.swing.*;
import java.awt.event.*;
import java.awt.FlowLayout;
import java.awt.Color;



//Polarization module needs StateButtons badly [Davis]

public class ControlPanel extends JPanel implements ActionListener{
	private static final int POLARIZER=0,WAVEPLATE=1;	// Choice indices

	private final Polarization applet;
	private Engine engine;
	private FilterPanel filterPanel;

	private final JButton histButton1,histButton2,modeButton;
	private final JComboBox FilterChoice;
	private final JButton addButton,hideWidgetsButton,playButton,resetButton;
	private int fieldMode;
	private int histMode1,histMode2;
	private boolean isPolarized = true;
	private boolean widgetsVisible = true;

	public ControlPanel(Polarization p) {
		applet = p;

		setLayout(new FlowLayout(FlowLayout.CENTER, 4, 2));

		//FilterChoice = new Choice();
		FilterChoice = new JComboBox();
		FilterChoice.addItem("Polarizer");
		FilterChoice.addItem("Wave Plate");
		FilterChoice.setForeground(Color.black);
		FilterChoice.setBackground(Color.white);
		//FilterChoice.addItemListener(this);
		add(FilterChoice);

		addButton = new JButton("Add");
		addButton.setBackground(Color.gray.darker());
		addButton.setForeground(Color.white);
		addButton.addActionListener(this);
		add(addButton);

		modeButton = new JButton("Composite");
		modeButton.setBackground(Color.gray.darker());
		modeButton.setForeground(Color.white);
		modeButton.addActionListener(this);
		add(modeButton);
		fieldMode = Engine.COMPOSITE;

		histButton1 = new JButton("  History 1: Dots  ");
		histButton1.setBackground(Color.gray.darker());
		histButton1.setForeground(Color.white);
		histButton1.addActionListener(this);
		add(histButton1);
		histMode1 = 1;

		histButton2 = new JButton("  History 2: Dots  ");
		histButton2.setBackground(Color.gray.darker());
		histButton2.setForeground(Color.white);
		histButton2.addActionListener(this);
		add(histButton2);
		histMode2 = 1;

		hideWidgetsButton = new JButton(" Hide Widgets ");
		hideWidgetsButton.setBackground(Color.gray.darker());
		hideWidgetsButton.setForeground(Color.white);
		hideWidgetsButton.addActionListener(this);
		add(hideWidgetsButton);

		playButton = new JButton("Stop");
		playButton.setBackground(Color.gray.darker());
		playButton.setForeground(Color.white);
		playButton.addActionListener(this);
		add(playButton);

		resetButton = new JButton("Reset");
		resetButton.setBackground(Color.gray.darker());
		resetButton.setForeground(Color.white);
		resetButton.addActionListener(this);
		add(resetButton);

		//wslPlayer = applet.getWSLPlayer();
	}

	public void reset() {
		setFieldMode(Engine.COMPOSITE);
		//Perhaps these default values are ignored? [Davis]
		setHistoryMode(Engine.BEGIN_HIST, Engine.HIST_DOTS);
		setHistoryMode(Engine.END_HIST, Engine.HIST_DOTS);
		setPlaying(true);
		setWidgetsVisible(true);
	}

	public void setEngine(Engine engine_) {
		engine = engine_;
		engine.setHistoryMode(Engine.BEGIN_HIST,histMode1);
		engine.setHistoryMode(Engine.END_HIST,histMode2);
	}

	public void setFilterPanel(FilterPanel panel) {
		filterPanel = panel;
	}

	public void setHistoryMode(int which, int mode) {
		JButton histButton;
		String histID,histStatus;
		//Switch on mode first to avoid assigning invalid values later
		switch(mode) {
		case Engine.HIST_OFF: histStatus="Off"; break;
		case Engine.HIST_DOTS: histStatus="Dots"; break;
		case Engine.HIST_LINES: histStatus="Lines"; break;
		default:
			throw new IllegalArgumentException("bad history mode");
		}
		switch(which) {
		case Engine.BEGIN_HIST:
			histButton=histButton1;
			histID="History 1";
			histMode1=mode;
			break;
		case Engine.END_HIST:
			histButton=histButton2;
			histID="History 2";
			histMode2=mode;
			break;
		default:
			throw new IllegalArgumentException("no such history");
		}
		histButton.setLabel("  "+histID+": "+histStatus+"  ");
	}

	public void setPlaying(boolean playing) {
		if(playing) playButton.setLabel("Stop");
		else playButton.setLabel("Play");
	}

	public void setFieldMode(int mode) {
		switch (mode) {
		case Engine.NONE: modeButton.setLabel("None"); break;
		case Engine.X_ONLY: modeButton.setLabel("X Only"); break;
		case Engine.Y_ONLY: modeButton.setLabel("Y Only"); break;
		case Engine.X_AND_Y: modeButton.setLabel("X and Y"); break;
		case Engine.COMPOSITE: modeButton.setLabel("Composite"); break;
		case Engine.ALL: modeButton.setLabel("All"); break;
		default: throw new IllegalArgumentException("bad field mode");
		}

		this.fieldMode = mode;
	}

	public void setWidgetsVisible(boolean visible) {
		widgetsVisible = visible;
		if(widgetsVisible) hideWidgetsButton.setLabel("Hide Widgets");
		else hideWidgetsButton.setLabel("Show Widgets");
	}

	public void setPolarized(boolean p) {
		if(isPolarized^p) {		//only act on changes; Choice breaks otherwise
			isPolarized=p;
			if(isPolarized) FilterChoice.addItem("Wave Plate");
			else FilterChoice.removeItem("Wave Plate");
		}
	}

	public void actionPerformed(ActionEvent event) {
		//if(wslPlayer.isPlaying()) return;

		final Object source = event.getSource();

		if(source==addButton) {
			Filter f = null;
			switch(FilterChoice.getSelectedIndex()) {
			case POLARIZER:
				f = engine.addPolarizer();
				break;
			case WAVEPLATE:
				if(engine.getPolarized()) f = engine.addWavePlate();
				//else applet.setWarningText("Can't use wave plates in Unpolarized mode.");
				break;
			default:
				System.err.println("Unexpected selection in filter choice: "+FilterChoice.getSelectedIndex());
			}
			//if(f!=null) wslPlayer.recordObjectAdded(f.toWSLNode());
		} else if(source==resetButton) {
			//applet.reset();						// this will call our own reset()
			//wslPlayer.recordActionPerformed("reset");
		} else if(source==modeButton) {
			fieldMode = (fieldMode+1) % 6;
			engine.setFieldMode(fieldMode);
			if(fieldMode==Engine.NONE) {
				modeButton.setLabel("None");
				//wslPlayer.recordActionPerformed("fieldMode", "none");
			} else if(fieldMode==Engine.X_ONLY) {
				modeButton.setLabel("X Only");
				//wslPlayer.recordActionPerformed("fieldMode", "x_only");
			} else if(fieldMode==Engine.Y_ONLY) {
				modeButton.setLabel("Y Only");
				//wslPlayer.recordActionPerformed("fieldMode", "y_only");
			} else if(fieldMode==Engine.X_AND_Y) {
				modeButton.setLabel("X and Y");
				//wslPlayer.recordActionPerformed("fieldMode", "x_and_y");
			} else if(fieldMode==Engine.COMPOSITE) {
				modeButton.setLabel("Composite");
				//wslPlayer.recordActionPerformed("fieldMode", "composite");
			} else if(fieldMode==Engine.ALL) {
				modeButton.setLabel("All");
				//wslPlayer.recordActionPerformed("fieldMode", "all");
			}
		} else if(source==histButton1) {
			histMode1 = (histMode1 + 1) % 3;
			setHistoryMode(Engine.BEGIN_HIST,histMode1);
			//wslPlayer.recordActionPerformed("history1",Engine.HIST_VALUES[histMode1]);
			engine.setHistoryMode(Engine.BEGIN_HIST, histMode1);
		} else if(source==histButton2) {
			histMode2 = (histMode2 + 1) % 3;
			setHistoryMode(Engine.END_HIST,histMode2);
			//wslPlayer.recordActionPerformed("history2",Engine.HIST_VALUES[histMode2]);
			engine.setHistoryMode(Engine.END_HIST, histMode2);
		} else if(source==hideWidgetsButton) {
			if(widgetsVisible) {
				engine.hideWidgets();
				filterPanel.showFilter(null);
				hideWidgetsButton.setLabel("Show Widgets");
				widgetsVisible = false;
			} else {
				engine.showWidgets();
				hideWidgetsButton.setLabel("Hide Widgets");
				widgetsVisible = true;
			}
			//wslPlayer.recordActionPerformed("widgets", widgetsVisible ? "visible" : "hidden");
		} else if(source==playButton) {
			if(engine.isPlaying()){
				engine.setPlaying(false);
				playButton.setLabel("Play");
			} else {
				engine.setPlaying(true);
				playButton.setLabel("Stop");
			}
			//wslPlayer.recordActionPerformed("animation", engine.isPlaying() ? "play" : "stop");
		}
	}
}
