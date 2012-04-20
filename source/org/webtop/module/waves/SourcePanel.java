/**
 * SourcePanel.java
 * This class is an extension of the WidgetPanel class.  It contains amplitude, wavelength, and phase 
 * FloatBoxes and event handling methods for them.  Other Sources that have these fields extend this
 * class. 
 * Updated by: Jeremy Davis July 1, 2008
 */

package org.webtop.module.waves;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.webtop.util.WTMath;
import org.sdl.math.FPRound; 
import org.sdl.gui.numberbox.*;
import org.webtop.wsl.client.*;



public class SourcePanel extends WidgetPanel implements NumberBox.Listener {
	
	private FloatBox amplitude; 
	private FloatBox wavelength; 
	private FloatBox phase; 
	
	private WaveSource source; 
	
	//Need to pass in WSLPlayer player? [JD]
	public SourcePanel(WSLPlayer player){
		super(player);
		
		
		add(new JLabel("Amplitude:", JLabel.RIGHT));
		amplitude = new FloatBox(0, WaveSource.MAX_AMPLITUDE, 4, 3);
		amplitude.addNumberListener(this);
		add(amplitude);
		
		add(new JLabel("Wavelength:", JLabel.RIGHT));
		wavelength = new FloatBox(0, WaveSource.MAX_WAVELENGTH, 8,4);
		wavelength.addNumberListener(this);
		add(wavelength);
		
		add(new JLabel("Phase:", JLabel.RIGHT));
		phase = new FloatBox(0, WTMath.toDegs(WaveSource.MAX_PHASE), 0, 3); 
		phase.addNumberListener(this);
		add(phase);
	}
	
	public void show(WaveSource ws){
		source = ws; 
		if(ws==null){
			amplitude.setEnabled(false);
			wavelength.setEnabled(false);
			phase.setEnabled(false);
		}
		else{
			amplitude.setEnabled(true);
			wavelength.setEnabled(true);
			phase.setEnabled(true);
			setAmplitude(ws.getAmplitude());
			setWavelength(ws.getWavelength());
			setPhase(ws.getPhase());
		}
	}
	
	public void setAmplitude(float a){
		amplitude.silence();
		amplitude.setValue(FPRound.toSigVal(a, 5));
	}
	
	public void setWavelength(float w){
		wavelength.silence();
		wavelength.setValue(FPRound.toSigVal(w,5));
	}
	
	public void setPhase(float p){
		phase.silence();
		phase.setValue(FPRound.toSigVal(WTMath.toDegs(p), 5));
	}
	
	//Only needed for the Plucked and Struck optics
	
	public void setWidth(float w) {
		//width.silence();
		//width.setValue(FPRound.toSigVal(w,5));
	}
	
	
	
	
	//Following 3 methods implement NumberBox.Listener
	public void numChanged(NumberBox eventSource, Number newVal){
		if(wslPlayer.isPlaying())
			return; 
		
		float f = newVal.floatValue();
		
		if(eventSource == amplitude){
			source.setAmplitude(f, true);
		}
		else if(eventSource == wavelength){
			source.setWavelength(f, true);
		}
		else if(eventSource == phase){
			source.setPhase(f, true);
		}
		
		if(!engine.isPlaying()){
			engine.update();
		}
	}
	
	public void invalidEntry(NumberBox src, Number badVal){
		if(src == amplitude){
			engine.statusBar.setText("The amplitude must be between 0 and 100");
		}
		else if(src == wavelength){
			engine.statusBar.setText("The wavelength must be between 0 and 100");
		}
		else if(src == phase){
			engine.statusBar.setText("The phase must be between 0 and 180");
		}
	}
	
	public void boundsForcedChange(NumberBox src, Number oldVal){}
	//End Implementation of NumberBox.Listener
}
