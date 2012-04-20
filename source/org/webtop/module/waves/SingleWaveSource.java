/**
 * <p> SingleWaveSource.java </p>
 * <p> @author Jeremy Davis </p>
 * <p> Updated: June 25, 2008
 * @version 0.0
 * <p> This is a base class for wave sources that do not persist.  Undecided as to whether 
 * the waves should go away after leaving the pool </p>
 */


package org.webtop.module.waves;

import org.webtop.wsl.script.WSLNode;
import org.webtop.component.*;
import org.webtop.util.*;
import org.webtop.wsl.client.*;
import org.webtop.wsl.script.*;
import org.webtop.wsl.event.*;
import org.web3d.x3d.sai.*;
//import org.web3d.sai.*;

public abstract class SingleWaveSource extends PoolWidget {

	protected static final float WAVE_SPEED = 1; 
	protected float amplitude; 
	protected float width; 
	protected float x; 
	protected float y; 
	
	protected float T; 
	
	protected SFFloat set_amplitude; 
	protected SFFloat set_width; 
	protected SFFloat set_x; 
	protected SFFloat set_y;
	
	public static final float MAX_AMPLITUDE = 50.0f; 
	public static final float MAX_WIDTH = 50.0f; 
	public static final float MAX_X = 50.0f; 
	public static final float MAX_Y = 50.0f; 
	
	public static final int TYPE_NONE = 0;
	public static final int TYPE_STRUCK = 1; 
	public static final int TYPE_PLUCKED = 2; 
	
	public SingleWaveSource(Engine e, WidgetsPanel panel, float A, float W, float x, float y){
		super(e, panel, x, y);
		amplitude = A; 
		width = W; 
		T=0;
	}
	
	public void setT(float tee) {T = tee;}
	
	public void create(String x3d){
		super.create(x3d);
		
		set_amplitude = (SFFloat) sai.getInputField(getNode(), "set_amplitude");
		set_width = (SFFloat) sai.getInputField(getNode(), "set_wavelength");
		
		sai.getOutputField(getNode(), "wavelength_changed", this, "wavelength_changed");
		sai.getOutputField(getNode(), "phase_changed", this, "phase_changed");
		sai.getOutputField(getNode(), "mouseOverAmplitude", this, "mouseOverAmplitude");
		sai.getOutputField(getNode(), "mouseOverWavelength", this, "mouseOverWavelength");
		sai.getOutputField(getNode(), "mouseOverPhase", this, "mouseOverPhase");
		sai.getOutputField(getNode(),"amplitude_changed", this, "amplitude_changed");
	}
	
	public float getAmplitude() {return amplitude;}
	public void setAmplitude(float A, boolean setX3D){
		amplitude = A;
		if(setX3D) 
			set_amplitude.setValue(amplitude);
	}
	
	public float getWidth() { return width;}
	public void setWidth(float W, boolean setX3D){
		width = W; 
		if(setX3D)
			set_width.setValue(width);
	}
	
	public abstract float getValue(float x, float y, float t);
	
	protected boolean passive() {
		//Definitely not
		return false;
	}
	
    //This should be called by subclasses from their readableFieldChanged() if they do not handle events
	public void readableFieldChanged(X3DFieldEvent arg0){
		String arg = (String) arg0.getData();
		Object e = arg0.getSource();
		
		System.out.println(arg);
		
		if(arg.equals("amplitude_changed")){
			setAmplitude(((SFFloat)e).getValue(), false);
			widgetsPanel.getSwitcher().getActiveSourcePanel().setAmplitude(amplitude);
			engine.update();
		}
		else if(arg.equals("wavelength_changed")){
			setWidth(((SFFloat)e).getValue(), false);
			widgetsPanel.getSwitcher().getActiveSourcePanel().setWidth(width);
			engine.update();
		}
		else{
			super.readableFieldChanged(arg0);
		}
	}

	public WSLNode toWSLNode() {
		//This code was located in the old source for SingleWaveSource [JD]
		/*WSLNode node;

		if(this instanceof PluckedSource) node = new WSLNode("pluckedsource");
		else if (this instanceof StruckSource) node = new WSLNode("strucksource");

		else return null;

		final WSLAttributeList atts=node.getAttributes();

		atts.add("id", id);
		atts.add("amplitude", String.valueOf(amplitude));
		atts.add("width", String.valueOf(width));
		if(widgetVisible) atts.add("selected", "true");
		atts.add("position", X + "," + Y);
		return node;*/
		return null;
	}

}
