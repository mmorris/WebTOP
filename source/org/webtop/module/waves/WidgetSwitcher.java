/**
 * WidgetSwitcher.java
 * This class switches between the different kind of widget panels that are displayed on the screen. 
 * Updated by: Jeremy Davis July 1, 2008
 */

package org.webtop.module.waves;

import javax.swing.*;
import java.awt.*;
import org.webtop.wsl.client.*;

public class WidgetSwitcher extends JPanel {
	
	private static final int NONE=0, 
							 LINEAR=1, 
							 RADIAL=2,
							 SAMPLE=3, 
							 PLUCKED=4, 
							 STRUCK=5;
	
	private final LinearPanel linear;
	//Uncomment when working on other widgets
	/*
	private final RadialPanel radial; 
	private final SamplePanel sample; 
	private final PluckedPanel plucked; 
	private final PluckedPanel struck; 
	*/
	private final CardLayout layout; //GridBagLayout more than likely [JD]
	
	private int showing; 
	
	private JPanel activePanel; 
	private SourcePanel activeSourcePanel; //null if no panel or if not source panel
	//Uncomment when working on PluckedPanel
	/*
	private PluckedPanel activeTSPanel; //null if not a TS Panel 
	*/
	//Need to pass in WSLPlayer? [JD]
	public WidgetSwitcher(WSLPlayer player){
		setLayout(layout = new CardLayout());
		linear = new LinearPanel(player);
		//Uncomment when working on other widgets
		/*
		radial = new RadialPanel(player);
		sample = new SamplePanel(player);
		plucked = new PluckedPanel(player);
		struck = new PluckedPanel(player);
		*/
		add("none", new JPanel());
		add("linear", linear);
		//Uncomment when working on other widgets
		/*
		add("radial", radial);
		add("sample", sample);
		add("plucked", plucked);
		add("struck", struck);
		*/
	}
	
	public void setEngine(Engine e){
		linear.setEngine(e);
		//Uncomment when working on RadialSource
		/*
		radial.setEngine(e);
		*/
	}
	
	private void show(int which){
		switch(which){
		case NONE:
			layout.show(this, "none");
			showing = NONE;
			activePanel=activeSourcePanel=null; 
			//Uncomment when working on PluckedSource
			/*
			activeTSPanel = null;
			*/
			break;
		case LINEAR:
			layout.show(this, "linear");
			showing = LINEAR;
			activePanel=activeSourcePanel=linear;
			break;
		case RADIAL:
			layout.show(this, "radial");
			showing = RADIAL; 
			//PluckedSource (I think...go back and check)
			/*
			activePanel=activeSourcePanel=radial;
			*/
			break;
		case SAMPLE: 
			layout.show(this, "sample");
			showing = SAMPLE; 
			//Sampling Stick
			/*
			activePanel = sample; 
			*/
			activeSourcePanel = null;
			//?
			/*
			activeTSPanel = null; 
			*/
			break; 
		case PLUCKED: 
			layout.show(this, "plucked");
			showing = PLUCKED; 
			//PluckedSource
			/*
			activePanel = plucked; 
			activeTSPanel = plucked; 
			*/
			activeSourcePanel = null; 
			break; 
		case STRUCK: 
			layout.show(this, "struck");
			showing = STRUCK; 
			//StruckSource
			/*
			activePanel = struck; 
			activeTSPanel = struck; 
			*/
			activeSourcePanel = null; 
		}
	}
	
	public void show(PoolWidget s){
		if(s==null){
			show(NONE);
		}
		else if(s instanceof LinearSource){
			show(LINEAR);
			linear.show((LinearSource) s);
		}
		//Uncomment when working on other widgets
		/*
		else if(s instanceof RadialSource){
			show(RADIAL);
			radial.show((RadialSource) s);
		}
		else if(s instanceof SamplingStick){
			show(SAMPLE);
			sample.show((SamplingStick) s);
		}
		else if(s instanceof PluckedSource){
			show(PLUCKED); 
			plucked.show((PluckedSource) s);
		}
		else if(s instanceof StruckSource){
			show(STRUCK);
			struck.show((StruckSource) s);
		}
		*/
	}
	
//	Is this needed?	 Won't CardLayout do it for us?
	/*public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		linear.setBounds(0, 0, width, height);
		radial.setBounds(0, 0, width, height);
	}*/
	
	public int getShowing(){
		return showing; 
	}
	
	public JPanel getActivePanel(){
		return activePanel; 
	}
	
	public SourcePanel getActiveSourcePanel(){
		return activeSourcePanel;
	}
	//Uncomment when working on PluckedPanel
	/*
	public PluckedPanel getActiveSSourcePanel(){
		return activeTSPanel;
	}
	*/
}
