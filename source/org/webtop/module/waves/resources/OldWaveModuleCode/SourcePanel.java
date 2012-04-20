package webtop.wave;

import java.awt.*;
import java.awt.event.*;

import webtop.util.WTMath;
import sdl.math.FPRound;
import webtop.wsl.client.*;
import sdl.gui.numberbox.*;

public class SourcePanel extends WidgetPanel implements NumberBox.Listener{
	private FloatBox amplitude;
	private FloatBox wavelength;
	private FloatBox phase;


	private WaveSource source;

	public SourcePanel(WSLPlayer player) {
		super(player);

		add(new Label("Amplitude:",Label.RIGHT));
		amplitude = new FloatBox(0,WaveSource.MAX_AMPLITUDE,4,3);
		amplitude.addNumberListener(this);
		add(amplitude);

		add(new Label("Wavelength:",Label.RIGHT));
		wavelength = new FloatBox(0,WaveSource.MAX_WAVELENGTH,8,4);
		wavelength.addNumberListener(this);
		add(wavelength);

		add(new Label("Phase:",Label.RIGHT));
		phase = new FloatBox(0,WTMath.toDegs(WaveSource.MAX_PHASE),0,3);
		phase.addNumberListener(this);
		add(phase);
	}

	public void show(WaveSource ws) {
		source = ws;
		if(ws==null) {
			amplitude.setEnabled(false);
			wavelength.setEnabled(false);
			phase.setEnabled(false);
		} else {
			amplitude.setEnabled(true);
			wavelength.setEnabled(true);
			phase.setEnabled(true);
			setAmplitude(ws.getAmplitude());
			setWavelength(ws.getWavelength());
			setPhase(ws.getPhase());
		}
	}

	public void setAmplitude(float a) {
		amplitude.silence();
		amplitude.setValue(FPRound.toSigVal(a,5));
	}

	public void setWavelength(float w) {
		wavelength.silence();
		wavelength.setValue(FPRound.toSigVal(w,5));
	}

	public void setPhase(float p) {
		phase.silence();
		phase.setValue(FPRound.toSigVal(WTMath.toDegs(p),5));
	}

	// Only needed for the Plucked and Struck optics
	/*
	public void setWidth(float w) {
		width.silence();
		width.setValue(FPRound.toSigVal(w,5));
	}
	*/

	public void numChanged(NumberBox eventSource, Number newVal){
		if(wslPlayer.isPlaying()) return;

		float f = newVal.floatValue();

		if(eventSource==amplitude) {
			source.setAmplitude(f,true);
			if(wslPlayer!=null)
				wslPlayer.recordActionPerformed(source.getID(), "amplitude", String.valueOf(source.getAmplitude()));
		} else if(eventSource==wavelength) {
			source.setWavelength(f,true);
			if(wslPlayer!=null)
				wslPlayer.recordActionPerformed(source.getID(), "wavelength",
																				String.valueOf(source.getWavelength()));
		} else if(eventSource==phase) {
			source.setPhase((float) WTMath.toRads(f),true);
			if(wslPlayer!=null)
				wslPlayer.recordActionPerformed(source.getID(), "phase",
																				String.valueOf(WTMath.toDegs(source.getPhase())));
		}

		if(!engine.isPlaying()) engine.update();
	}
	public void invalidEntry(NumberBox src, Number badVal) {
		if(src == amplitude)
			engine.getStatusBar().setText("The amplitude must be between 0 and 100.");
		else if(src == wavelength)
			engine.getStatusBar().setText("The wavelength must be between 0 and 100.");
		else if(src == phase)
			engine.getStatusBar().setText("The phase must be between 0 and 180.");
	}

	public void boundsForcedChange(NumberBox src, Number oldVal){}
}
