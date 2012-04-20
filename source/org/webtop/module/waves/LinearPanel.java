/**
 * LinearPanel.java
 * This class extends the SourcePanel class and contains the definition of the LinearSource angle 
 * field, and event handling for it. 
 * Updated by: Jeremy Davis July 1, 2008
 */

package org.webtop.module.waves;

import javax.swing.*;
import java.awt.event.*; 

import org.webtop.util.WTMath; 
import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.sdl.gui.numberbox.*;

public class LinearPanel extends SourcePanel implements NumberBox.Listener {

	private FloatBox angle; 
	
	private LinearSource lSource; 
	
	public LinearPanel(WSLPlayer player){
		super(player);
		
		add(new JLabel("Angle:", JLabel.RIGHT));
		angle = new FloatBox(0, 360, 4, 4);
		angle.addNumberListener(this);
		add(angle);
	}
	
	public void show(LinearSource s){
		super.show(s); 
		lSource = s; 
		if(s!=null){
			angle.setValue(WTMath.toDegs(s.getAngle()));
			angle.setEnabled(true);
		}
		else{
			angle.setEnabled(false);
		}
	}
	
	public void setAngle(float a){
		angle.silence(); 
		angle.setValue(WTMath.toDegs(a));
	}	
	
	//The following 3 methods implement NumberBox.Listener
	public void numChanged(NumberBox eventSource, Number newValue){
		if(wslPlayer.isPlaying())
			return; 
		
		if(eventSource == angle){
			lSource.setAngle(WTMath.toRads(angle.getValue()), true);
		}
		
		super.numChanged(eventSource, newValue);
	}
	public void invalidEntry(NumberBox src, Number badVal){
		if(src == angle){
			engine.statusBar.setText("Angle must be between 0 and 360 degrees");
		}
	}
	public void boundsForcedChange(NumberBox src, Number oldVal){}
	
}
