package org.webtop.module.twoslitphoton;

import org.webtop.x3d.SAI;
import org.webtop.util.*;
import org.web3d.x3d.sai.*;

public class NSlitDragger implements X3DFieldEventListener {
	
	public float apertureWidth	 = 1000.0f;
	public int n = 2;
	public String nslitDraggerName = "nslitDragger";
	
	public float width = 40.0f, distance = 250.0f;
	
	public SFFloat	m_set_width,m_set_distance;
	public SFFloat	m_set_wd_position,m_set_dd_position;
	public SFFloat	m_set_min_dd_position,m_set_max_dd_position, m_set_min_wd_position,m_set_max_wd_position;
	public SFInt32	m_set_dd_switch;
	
	float old_width_translation, old_distance_translation;
	
	private static final int 
		N					 = 1,
		WIDTH				 = 2,
		DISTANCE			 = 3,
		WIDTH_TRANSLATION	 = 4,
		DISTANCE_TRANSLATION = 5;
	
	public static final int
	SINGLE = 0,
	NSLIT	 = 1;

	private int m_mode = NSLIT;

	private boolean
		m_internal_set_wd = false,
		m_internal_set_dd = false,
		m_internal_set_width = false,
		m_internal_set_distance = false;
	
	
	
	public NSlitDragger(SAI sai){
		DebugPrinter.println("NSlitDragger.init().begin");

		sai.getOutputField(nslitDraggerName,"n",this,new Integer(N));

		m_set_width = (SFFloat) sai.getInputField(nslitDraggerName,"width");
		sai.getOutputField(nslitDraggerName,"width",this,new Integer(WIDTH));

		m_set_distance = (SFFloat) sai.getInputField(nslitDraggerName,"distance");
		sai.getOutputField(nslitDraggerName,"distance",this,new Integer(DISTANCE));

		m_set_wd_position = (SFFloat)sai.getInputField(nslitDraggerName,"set_wd_position");
		sai.getOutputField(nslitDraggerName,"wd_position_changed",this,new Integer(WIDTH_TRANSLATION));

		m_set_dd_position = (SFFloat)sai.getInputField(nslitDraggerName,"set_dd_position");
		sai.getOutputField(nslitDraggerName,"dd_position_changed",this,new Integer(DISTANCE_TRANSLATION));

		m_set_min_dd_position = (SFFloat)sai.getInputField(nslitDraggerName,"set_min_dd_position");
		m_set_max_dd_position = (SFFloat)sai.getInputField(nslitDraggerName,"set_max_dd_position");
		m_set_min_wd_position = (SFFloat)sai.getInputField(nslitDraggerName,"set_min_wd_position");
		m_set_max_wd_position = (SFFloat)sai.getInputField(nslitDraggerName,"set_max_wd_position");

		m_set_dd_switch = (SFInt32)sai.getInputField(nslitDraggerName,"dd_on");
		
		evaluate(true, true);
		updateDistanceDraggerConstraints();
		updateWidthDraggerConstraints();

		DebugPrinter.println("NSlitDragger.init().end");
	}
	
	void setMode(int mode) {
		m_mode = mode;
		if(mode == SINGLE) {
			m_set_dd_switch.setValue(-1);
			updateWidthDraggerConstraints();
		} else if(mode == NSLIT)
			m_set_dd_switch.setValue(0);
		else
			DebugPrinter.println("Invalid mode");
	}
	
	public void evaluate(boolean updateWidth, boolean updateDistance) {
		//distancePosition = width * (2.0f * n - 3.0f) + distance * (1.0f - n);
		//widthPosition = distancePosition[0] + width;

		if(updateWidth) {
			m_internal_set_wd = true;
			m_set_wd_position.setValue(distance*(n-1)/2+width/2);
		}

		if(updateDistance) {
			m_internal_set_dd = true;
			m_set_dd_position.setValue(distance*(n-1)/2);
		}
	}
	
	public void updateWidthDraggerConstraints() {
		float
			min = distance * (n - 1) / 2.0f,
			max1 = apertureWidth / 2.0f,
			max2 = distance * (n / 2.0f),
			max = 0.0f;
		DebugPrinter.println("min: "+min+" max: "+max1+" or "+max2);

		if(m_mode == SINGLE)
			max = max1;
		else if(m_mode == NSLIT)
			max = (max1 < max2) ? max1 : max2;

		DebugPrinter.println("max: "+max);
		//This code was meant to guarantee that the slit width was never greater
		//than 0.25 mm.	 It doesn't work as of now.	 Associated with it is the
		//width FloatBox having a maximum value of 0.25.	If this is implemented
		//properly, reimplement that.
		/*if((max-min)>250/2) {
			max=(max+min)/2+125;
			min=max-250;import java.awt.event.*;
		}*/

		DebugPrinter.println("After //FIX!: min="+min+", max="+max);

		m_set_min_wd_position.setValue(min);
		m_set_max_wd_position.setValue(max);
	}
	
	public void updateDistanceDraggerConstraints() {
		float
			min = width * (n - 1) / 2.0f,
			max = apertureWidth / 2.0f - width / 2.0f;

		DebugPrinter.println("distance.max = " + max);
		m_set_min_dd_position.setValue(min);
		m_set_max_dd_position.setValue(max);
	}
	
	public void readableFieldChanged(X3DFieldEvent e)
	{
		String arg = (String)e.getData();

		DebugPrinter.println("callback + " + arg);

		if(arg.equals("N") )	//The one integer case
		{
			n = ((SFInt32)e.getSource()).getValue();
			evaluate(true,true);
			return;
		}

		//Otherwise, it's a real, and we branch.
		float val;
		if(e.getSource() instanceof SFVec3f){		//kludge; will fix later with WidgetEvent
			float[] tmp = new float[3];
			((SFVec3f)e.getSource()).getValue(tmp);
			val = tmp[0];
		}
		else
			val=((SFFloat)e.getSource()).getValue();

		if ( arg.equals( "WIDTH" ) ) {
				if(m_internal_set_width) {
				m_internal_set_width = false;
				return;
			}
			width = val;
			DebugPrinter.println("width_changed = " + width);
			updateDistanceDraggerConstraints();
			evaluate(true, true);
		}else if ( arg.equals( "DISTANCE" ) ) {
			if(m_internal_set_distance) {
				m_internal_set_distance = false;
				return;
			}
			distance = val;
			DebugPrinter.println("distance_changed = " + distance);
			updateWidthDraggerConstraints();
			evaluate(true, true);
		}else if( arg.equals( "WIDTH_TRANSLATION" ) ) {
			DebugPrinter.println("width_trans");
			if(m_internal_set_wd) {
				DebugPrinter.println("width_trans.true");
				m_internal_set_wd = false;
				return;
			}
			width = 2 * val - distance * (n - 1);
			m_internal_set_width = true;
			m_set_width.setValue(width);

			evaluate(false, true);
			updateDistanceDraggerConstraints();

			old_width_translation = width;
		} else if ( arg.equals( "DISTANCE_TRANSLATION" ) ) {
			DebugPrinter.println("dist_trans");
			if(m_internal_set_dd) {
				DebugPrinter.println("dist_trans.true");
				m_internal_set_dd = false;
				return;
			}
			distance = 2 * val / (n - 1);
			m_internal_set_distance = true;
			m_set_distance.setValue(distance);

			evaluate(true, false);
			updateWidthDraggerConstraints();

			old_distance_translation = distance;
		}
	}
}
