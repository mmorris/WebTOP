package org.webtop.module.polarization;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.JTabbedPane;
import javax.swing.event.*;

import org.webtop.component.*;
import org.webtop.util.*;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;

import org.sdl.gui.numberbox.*;
import org.sdl.math.FPRound;

public class SourcePanel extends Panel
		implements ChangeListener, NumberBox.Listener {
	private Polarization applet;
	private Engine engine;

	private FloatBox Wavelength;
	private FloatBox Ex;
	private FloatBox Ey;
	private FloatBox Epsilon;
	private StatusBar InitialIntensityValue;

	private Panel polarized;
	private Panel unpolarized;

	private JTabbedPane folder;
	private boolean initialized = false;

	private SFFloat WavelengthEIn;
	private SFFloat XAMPEIn;
	private SFFloat YAMPEIn;
	private SFFloat PhaseEIn;

	public SourcePanel(Polarization applet_) {
		applet = applet_;

		setLayout(new VerticalLayout());

		polarized = new Panel(new FlowLayout(FlowLayout.CENTER,2,0));

		polarized.add(new Label("Wavelength:", Label.RIGHT));

		Wavelength = new FloatBox(400,700,550,4);
		Wavelength.addNumberListener(this);
		polarized.add(Wavelength);

		polarized.add(new Label("nm"));
		polarized.add(new Label("E0x:", Label.RIGHT));

		Ex = new FloatBox(0,1,1,3);
		Ex.addNumberListener(this);
		polarized.add(Ex);

		polarized.add(new Label(" E0y:", Label.RIGHT));

		Ey = new FloatBox(0,1,1,3);
		Ey.addNumberListener(this);
		polarized.add(Ey);

		polarized.add(new Label(" Phase Diff.:", Label.RIGHT));

		Epsilon = new FloatBox(0,360,90,3);
		Epsilon.addNumberListener(this);
		polarized.add(Epsilon);

		polarized.add(new Label("degrees"));

		polarized.add(new Label("Initial Intensity:", Label.RIGHT));
		InitialIntensityValue = new StatusBar();
		Dimension d = new Dimension(50, 20);
		InitialIntensityValue.setMinimumSize(d);
		InitialIntensityValue.setPreferredSize(d);
		polarized.add(InitialIntensityValue);

		unpolarized = new Panel();
		unpolarized.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		unpolarized.add(new Label("Initial Intensity:", Label.RIGHT));
		StatusBar sb = new StatusBar("1");
		d = new Dimension(50, 20);
		sb.setMinimumSize(d);
		sb.setPreferredSize(d);
		unpolarized.add(sb);
		//unpolarized.add(new Label("No parameters for unpolarized light source."));

		folder = new JTabbedPane();
		folder.add("Polarized", polarized);
		folder.add("Unpolarized", unpolarized);
		folder.setSelectedComponent(polarized);
		//folder.setPreferredSize(new Dimension(applet.getSize().width-8, 60));
		add(folder);
		folder.addChangeListener(this);

		//wslPlayer = applet.getWSLPlayer();
		//if(wslPlayer!=null)
		//	wslPlayer.addListener(this);

		initialized = true;
	}

	//The current source's panel is not (necessarily) removed when a reset occurs.	Needs to be fixed.	[Davis]
	public void reset() {
		Wavelength.setValue(Engine.DEF_WAVELENGTH);
		Ex.setValue(Engine.DEF_XAMP);
		Ey.setValue(Engine.DEF_YAMP);
		Epsilon.setValue(90);
		folder.setSelectedComponent(polarized);
	}

	public void eaiSetup(SAI eai) {
		WavelengthEIn = (SFFloat) eai.getField("WavelengthWidget","set_value");
		XAMPEIn = (SFFloat) eai.getField("XAMPDragger","set_position");
		YAMPEIn = (SFFloat) eai.getField("YAMPDragger","set_position");
		PhaseEIn = (SFFloat) eai.getField("PhaseDragger","set_phaseDifference");
	}

	public void setEngine(Engine e) {
		engine = e;
	}

	public void setEx(float x) {
		Ex.setFixValue(x,3);
	}

	public void setEy(float y) {
		Ey.setFixValue(y,3);
	}

	public void setWavelength(float w) {
		Wavelength.setFixValue(w,3);
	}

	public void setEpsilon(float e) {
		Epsilon.setFixValue(WTMath.toDegs(e),3);
	}

	public void setInitialIntensity(float intensity) {
		InitialIntensityValue.setText(String.valueOf(FPRound.toFixVal(intensity,3)));
	}

	public void setPolarized(boolean polarized) {
		if(polarized) folder.setSelectedComponent(this.polarized);
		else folder.setSelectedComponent(this.unpolarized);
	}

	
	public void stateChanged(ChangeEvent e) {
		JTabbedPane tabSource = (JTabbedPane) e.getSource();
        String tab = tabSource.getTitleAt(tabSource.getSelectedIndex());
        if(tab.equals("Polarized")) {
        	engine.setPolarized(true);
        }
        else if(tab.equals("Unpolarized")) {
        	engine.setPolarized(false);
        	engine.removeWavePlates();
        }
	}

	public void numChanged(NumberBox src, Number newVal){
		//if(wslPlayer.isPlaying()) return;

		//if(applet.getEventManager().isWidgetActive()) return;

		//applet.resetStatus();

		float f=newVal.floatValue();

		if(src==Wavelength) {
			engine.setWavelength(f);
			WavelengthEIn.setValue(f);
			//wslPlayer.recordActionPerformed("wavelength",String.valueOf(f));
		} else if(src==Ex) {
			engine.setEx(f);
			XAMPEIn.setValue(f);
			//wslPlayer.recordActionPerformed("amplitudex",String.valueOf(f));
		} else if(src==Ey) {
			engine.setEy(f);
			YAMPEIn.setValue(f);
			//wslPlayer.recordActionPerformed("amplitudey",String.valueOf(f));
		} else if(src==Epsilon) {
			f=WTMath.toRads(f);
			engine.setEpsilon(f);
			PhaseEIn.setValue(f);
			//wslPlayer.recordActionPerformed("phaseDifference",String.valueOf(f));
		}
	}

	public void invalidEntry(NumberBox src, Number badVal) {
		//if(src == Wavelength)
		//	applet.setWarningText("Wavelength must be between 400 and 700 nm.");
		//else if(src == Ex)
		//	applet.setWarningText("E0x must be between 0 and 1.");
		//else if(src == Ey)
		//	applet.setWarningText("E0y must be between 0 and 1.");
		//else if(src == Epsilon)
		//	applet.setWarningText("Phase Difference must be between 0 and 360 degrees.");
	}

	public void boundsForcedChange(NumberBox src, Number oldVal) {}

	//public void playerStateChanged(WSLPlayerEvent event) {
	//	switch(event.getID()) {
		//case WSLPlayerEvent.PLAYER_STARTED: folder.setEnabled(false); break;
		//case WSLPlayerEvent.PLAYER_STOPPED: folder.setEnabled(true); break;
	//	}
	//}
}
