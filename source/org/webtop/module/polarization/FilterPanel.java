package org.webtop.module.polarization;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

import org.sdl.gui.numberbox.*;
import org.sdl.math.FPRound;
import org.webtop.component.*;
import org.webtop.util.*;
//import webtop.wsl.client.*;
//import webtop.wsl.event.*;

public class FilterPanel extends Panel
	implements ActionListener, NumberBox.Listener{ //,WSLScriptListener {
	private Polarization applet;
	private Engine engine;
	//private WSLPlayer wslPlayer;

	private Label TypeLabel1;
	private Label ZLabel;
	private Label AngleLabel;
	private Label TypeLabel2;
	private Label ThicknessLabel;
	private Label IntensityLabel;
	private StatusBar IntensityValue;

	private FloatBox zField,angleField,thickField;

	private Panel panel;

	private Button removeButton;

	private Filter activeFilter;

	public FilterPanel(Polarization applet_) {
		applet = applet_;

		panel = new Panel(new FlowLayout(FlowLayout.CENTER, 2, 0));

		setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));

		panel.add((TypeLabel1 = new Label("Type:", Label.RIGHT)));
		TypeLabel2 = new Label("                   ");
		panel.add(TypeLabel2);

		panel.add((ZLabel = new Label("Z:", Label.RIGHT)));
		zField=new FloatBox(0,20,0,8);
		zField.setEnabled(false);
		zField.addNumberListener(this);
		panel.add(zField);

		panel.add((AngleLabel = new Label("Angle:", Label.RIGHT)));
		angleField=new FloatBox(0,360,0,8);
		angleField.setEnabled(false);
		angleField.addNumberListener(this);
		panel.add(angleField);

		panel.add(new Label("degrees"));

		ThicknessLabel = new Label("N:", Label.RIGHT);
		thickField=new FloatBox(0,1,0,8);
		thickField.addNumberListener(this);

		IntensityLabel = new Label("Intensity:", Label.RIGHT);
		IntensityValue = new StatusBar();
		Dimension d = new Dimension(50, 20);
		IntensityValue.setMinimumSize(d);
		IntensityValue.setPreferredSize(d);

		panel.setVisible(false);
		add(panel);

		removeButton = new Button("Remove");
		removeButton.setEnabled(false);
		removeButton.addActionListener(this);
		removeButton.setVisible(false);
		add(removeButton);

		//wslPlayer = applet.getWSLPlayer();
		//wslPlayer.addListener(this);
	}

	public Dimension getMinimumSize() {
		Dimension d = getParent().getSize();
		return new Dimension(d.width, panel.getMinimumSize().height);
	}

	public Dimension getPreferredSize() {
		Dimension d = getParent().getSize();
		return new Dimension(d.width, panel.getMinimumSize().height);
	}

	public void reset() {
		showFilter(null);
		zField.setEnabled(false);
		angleField.setEnabled(false);
		thickField.setEnabled(false);
		removeButton.setEnabled(false);
	}

	public void setEngine(Engine e) {engine = e;}

	public void showFilter(Filter p) {
		activeFilter=null;
		if(p!=null) {
			TypeLabel2.setText(p.getType());
			zField.setFixValue(p.getZ(),3);
			zField.setEnabled(true);
			angleField.setFixValue(WTMath.toDegs(p.getAngle()),3);
			angleField.setEnabled(true);
			if(p instanceof WavePlate) {
				panel.remove(IntensityLabel);
				panel.remove(IntensityValue);
				panel.add(ThicknessLabel);
				panel.add(thickField);
				thickField.setEnabled(true);
				thickField.setFixValue(((WavePlate)p).getThickness(), 3);
			} else {
				panel.remove(ThicknessLabel);
				panel.remove(thickField);
				panel.add(IntensityLabel);
				panel.add(IntensityValue);
			}
			removeButton.setEnabled(true);
			removeButton.setVisible(true);
			activeFilter = p;

			if(!panel.isVisible()) panel.setVisible(true);
			validate();
		} else {
			TypeLabel2.setText("");
			panel.remove(ThicknessLabel);
			panel.remove(thickField);
			panel.setVisible(false);
			removeButton.setVisible(false);
			validate();
		}
	}

	public Filter getActiveFilter() {return activeFilter;}

	public void setZ(float z) {zField.setFixValue(z,3);}
	public void setAngle(float angle) {angleField.setFixValue(WTMath.toDegs(angle),3);}
	public void setThickness(float thickness) {thickField.setFixValue(thickness,3);}

	public void setIntensity(float intensity) {
		if(activeFilter==null || !(activeFilter instanceof Polarizer)) return;

		IntensityValue.setText(String.valueOf(FPRound.toFixVal(intensity,3)));
	}

	public void actionPerformed(ActionEvent e) {
		//if(activeFilter==null || wslPlayer.isPlaying()) return;
		if(activeFilter==null) return;
			
		//wslPlayer.recordObjectRemoved(activeFilter.getID());

		engine.removeFilter(activeFilter);
		activeFilter = null;
		TypeLabel2.setText("");
		panel.setVisible(false);
		removeButton.setVisible(false);
	}

	public void numChanged(NumberBox src,Number newVal) {
		//if(activeFilter==null || wslPlayer.isPlaying()) return;
		if(activeFilter==null) return;

		//if(applet.getEventManager().isWidgetActive()) return;

		float f=newVal.floatValue();

		if(src==zField) {
			engine.moveFilter(activeFilter, f, true);
			//wslPlayer.recordActionPerformed(activeFilter.getID(), "z", String.valueOf(f));
		} else if(src==angleField) {
			f=WTMath.toRads(f);
			engine.setFilterAngle(activeFilter, f, true);
			//wslPlayer.recordActionPerformed(activeFilter.getID(), "angle", String.valueOf(f));
		} else if(src==thickField) {
			engine.setWavePlateThickness((WavePlate) activeFilter, f, true);
			//wslPlayer.recordActionPerformed(activeFilter.getID(), "thickness", String.valueOf(f));
		}
	}

	public void invalidEntry(NumberBox src,Number badVal) {
		if(src==zField) {
			//applet.setWarningText("Z position must be between 0 and 20.");
		} else if(src==angleField) {
			//applet.setWarningText("Angle must be between 0 and 360.");
		} else if(src==thickField) {
			//applet.setWarningText("Wave plate thickness must be between 0 and 1.");
		}
	}

	public void boundsForcedChange(NumberBox src,Number oldVal) {}		//doesn't happen

	//public void initialize(WSLScriptEvent event) {}

	/*public void scriptActionFired(WSLScriptEvent event) {
		if(activeFilter==null) return;
		if(event.getID()==event.ACTION_PERFORMED) {
			String target = event.getTarget();
			String param = event.getParameter();
			String value = event.getValue();

			if(WTString.isNull(target)) return;

			if("z".equals(param)) {
				zField.setFixValue(activeFilter.getZ(),3);
			} else if("angle".equals(param)) {
				angleField.setFixValue(activeFilter.getAngle(),3);
			} else if("thickness".equals(param)) {
				if(activeFilter instanceof WavePlate) {
					thickField.setFixValue(((WavePlate) activeFilter).getThickness(),3);
				}
			}
		}
	}*/
}
