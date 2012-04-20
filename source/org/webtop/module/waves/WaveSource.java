//Updated June 10 2008 by Jeremy Davis

package org.webtop.module.waves;

import org.webtop.wsl.script.WSLNode;
import org.web3d.x3d.sai.*;
import org.webtop.util.*;
import org.webtop.wsl.client.*;
import org.webtop.wsl.script.*;
import org.webtop.wsl.event.*; 


public abstract class WaveSource extends PoolWidget {
	
	protected static final float WAVE_SPEED =1; 
	protected float amplitude; 
	protected float wavelength; 
	protected float phase; 
	protected float k; 
	
	protected SFFloat set_amplitude; 
	protected SFFloat set_wavelength; 
	protected SFFloat set_phase;
	
	public static final float MAX_AMPLITUDE = 10.0f; 
	public static final float MAX_WAVELENGTH = 50.0f; 
	
	//For reasons of simplicity and widget sensitivity, we only use half of the
	//possible range of phases:
	public static final float MAX_PHASE =  (float) Math.PI; 
	
	public static final int TYPE_NONE = 0; 
	public static final int TYPE_LINEAR = 1; 
	public static final int TYPE_RADIAL = 2; 
	
	
	public WaveSource(Engine e, WidgetsPanel panel, float A, float L, float E, float x, float y){
		super(e, panel, x, y);
		amplitude = A; 
		wavelength = L; 
		phase = E; 
		k = (float) (2*Math.PI/wavelength);
	}
	
	protected void create(String x3d){
		super.create(x3d);
		
		set_amplitude = (SFFloat) sai.getInputField(getNode(), "set_amplitude");
		set_wavelength = (SFFloat) sai.getInputField(getNode(), "set_wavelength"); 
		set_phase = (SFFloat) sai.getInputField(getNode(), "set_phase"); 
		
		sai.getOutputField(getNode(), "wavelength_changed", this, "wavelength_changed");
		sai.getOutputField(getNode(),"phase_changed", this, "phase_changed");
		sai.getOutputField(getNode(), "mouseOverAmplitude", this, "mouseOverAmplitude");
		sai.getOutputField(getNode(), "mouseOverWavelength", this, "mouseOverWavelength");
		sai.getOutputField(getNode(), "mouseOverPhase", this, "mouseOverPhase");
		sai.getOutputField(getNode(), "amplitude_changed", this, "amplitude_changed");
	}
	
	public float getAmplitude(){
		return amplitude;
	}
	
	public float getWavelength(){
		return wavelength; 
	}
	
	public float getPhase(){
		return phase;
	}
	
	public void setAmplitude(float A, boolean setX3D){
		amplitude = A; 
		if(setX3D)
			set_amplitude.setValue(amplitude);
	}
	
	public void setWavelength(float L, boolean setX3D){
		wavelength = L; 
		k = (float) (2*Math.PI/wavelength);
		if(setX3D)
			set_wavelength.setValue(wavelength);
	}
	
	public void setPhase(float E, boolean setX3D){
		phase = E; 
		if(setX3D){
			if(this instanceof LinearSource)
				set_phase.setValue(phase*2);
			else
				set_phase.setValue(phase);
		}	
	}
	
	public abstract float getValue(float x, float y, float t);
	

	protected boolean passive() {
		//certainly not
		return false;
	}
	
	//This should be called by subclasses from their callback() if they do not handle events
	public void readableFieldChanged(X3DFieldEvent arg0){
		String arg = (String) arg0.getData();
		Object e = arg0.getSource();
		
		//WSLPlayer wslplayer = engine.getWSLPlayer();
		
		if(arg.equals("amplitude_changed")){
			System.out.println(((SFFloat)e).getValue() + " amplitude_changed::getValue()");
			setAmplitude(((SFFloat)e).getValue(), false);
			widgetsPanel.getSwitcher().getActiveSourcePanel().setAmplitude(amplitude);
			//if(wslPlayer!=null)
			//wslPlayer.recordMouseDragged(getID(), "amplitude", String.valueOf(amplitude));
			engine.update();
		}
		else if(arg.equals("phase_changed")){
			if(this instanceof LinearSource)
				setPhase((((SFFloat)e).getValue())/2.0f, false);
			else
				setPhase(((SFFloat)e).getValue(), false);
			widgetsPanel.getSwitcher().getActiveSourcePanel().setPhase(phase);
			//if(wslPlayer!=null)
			//wslPlayer.recordMouseDragged(getID(), "phase", String.valueOf(WTMath.toDegs(phase)));
			engine.update();
		}
		else if(arg.equals("wavelength_changed")){
			setWavelength(((SFFloat)e).getValue(), false);
			widgetsPanel.getSwitcher().getActiveSourcePanel().setWavelength(wavelength);
			//if(wslPlayer!=null)
			//wslPlayer.recordMouseDragged(getID(), "wavelength", String.valueOf(wavelength));
			engine.update();
		}
		else if(arg.equals("mouseOverAmplitude")){
			if(((SFBool)e).getValue())
				engine.statusBar.setText("Use this widget to change the amplitude" +
						"Up - increase.  Down - decrease");
			else if(!dragging)
				engine.statusBar.reset();
		}
		else if(arg.equals("mouseOverWavelength")){
			if(((SFBool)e).getValue())
				engine.statusBar.setText("Use this widget to change the wavelength" +
						"Outward - increase.  Inward - decrease.");
			else if(!dragging)
				engine.statusBar.reset();
		}
		else if(arg.equals("mouseOverPhase")){
			if(((SFBool)e).getValue())
				engine.statusBar.setText("Use this widget to change the phase. " +
						"Outward - increase.  Inward - decrease");
			else if(!dragging)
				engine.statusBar.reset();
		}
		else
			super.readableFieldChanged(arg0);
	}

	//Don't know if we really need this, so I'm going to comment the inside of the method out [JD]
	public WSLNode toWSLNode() {
		/*WSLNode node;

		if(this instanceof LinearSource) node = new WSLNode("linesource");
		else if(this instanceof RadialSource) node = new WSLNode("pointsource");
		else return null;

		final WSLAttributeList atts=node.getAttributes();

		atts.add("id", id);
		atts.add("amplitude", String.valueOf(amplitude));
		atts.add("wavelength", String.valueOf(wavelength));
		atts.add("phase", String.valueOf(WTMath.toDegs(phase)));
		if(widgetVisible) atts.add("selected", "true");
		if(this instanceof RadialSource) atts.add("position", X + "," + Y);

		return node;*/
		return null;
	}

}
