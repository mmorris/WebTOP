////////////////////////////////////////////////////////////////////////////
// N-Slit Dragger
//
// Author(s): Kiril Vidimce (vkire@erc.msstate.edu)
//
//													 The Optics Project
//													(top@cs.msstate.edu)
//	 NSF/Mississippi State University Engineering Research Center for CFS
//					 Copyright (C) 1996-2002 Mississippi State University
//
////////////////////////////////////////////////////////////////////////////

package webtop.twoslit;

import vrml.external.field.*;

import webtop.util.DebugPrinter;
import webtop.vrml.EAI;

class NSlitDragger implements EventOutObserver
{
	////////////////////////////////////////////////////////////////////////
	// public input fields/parameters that control the computation engine
	//
	public float
		apertureWidth	 = 1000.0f;						 // width of the aperture (in micrometers?)

	public int n = 2;									// # of slits

	public String nslitDraggerName = "nslitDragger";
	//
	// end of input fields/parameters
	////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////
	// public input/output fields
	//
	public float
		width					 =	40.0f,						// width of the slits
		distance			 =	250.0f;						 // distance between the slits
	//
	// end of input/output fields
	////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////
	// Pointers to the VRML browser, the VRML nodes and events that the
	// engine uses.
	EventInSFFloat	m_set_width,m_set_distance;

	EventInSFFloat	m_set_wd_position,m_set_dd_position;

	EventInSFFloat	m_set_min_dd_position,m_set_max_dd_position,
									m_set_min_wd_position,m_set_max_wd_position;

	EventInSFInt32	m_set_dd_switch;
	////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////
	float
		old_width_translation,
		old_distance_translation;
	////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////
	// some constants for identifying the events in the callback
	private static final int
		N										 = 1,
		WIDTH								 = 2,
		DISTANCE						 = 3,
		WIDTH_TRANSLATION		 = 4,
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

	////////////////////////////////////////////////////////////////////////
	// Initialization of the Fraun Single Silt engine involves
	// getting a pointer to the nodes to which events are sent in with
	// new vertex and color values.
	public NSlitDragger(EAI eai) {
		DebugPrinter.println("NSlitDragger.init().begin");

		eai.getEO(nslitDraggerName,"n",this,new Integer(N));

		m_set_width = (EventInSFFloat) eai.getEI(nslitDraggerName,"width");
		eai.getEO(nslitDraggerName,"width",this,new Integer(WIDTH));

		m_set_distance =
			(EventInSFFloat) eai.getEI(nslitDraggerName,"distance");
		eai.getEO(nslitDraggerName,"distance",this,new Integer(DISTANCE));

		m_set_wd_position = (EventInSFFloat)
			eai.getEI(nslitDraggerName,"set_wd_position");
		eai.getEO(nslitDraggerName,"wd_position_changed",this,new Integer(WIDTH_TRANSLATION));

		m_set_dd_position = (EventInSFFloat)
			eai.getEI(nslitDraggerName,"set_dd_position");
		eai.getEO(nslitDraggerName,"dd_position_changed",this,new Integer(DISTANCE_TRANSLATION));

		m_set_min_dd_position = (EventInSFFloat)
			eai.getEI(nslitDraggerName,"set_min_dd_position");
		m_set_max_dd_position = (EventInSFFloat)
			eai.getEI(nslitDraggerName,"set_max_dd_position");
		m_set_min_wd_position = (EventInSFFloat)
			eai.getEI(nslitDraggerName,"set_min_wd_position");
		m_set_max_wd_position = (EventInSFFloat)
			eai.getEI(nslitDraggerName,"set_max_wd_position");

		m_set_dd_switch =
			(EventInSFInt32) eai.getEI(nslitDraggerName,"dd_on");

		//width = m_width_changed.getValue();
		//distance = m_distance_changed.getValue();
		//width = old_width_translation =
			//m_wd_position_changed.getValue()[0];
		//DebugPrinter.println(width[0]);
		//distance = old_distance_translation =
			//m_dd_position_changed.getValue()[0];

		evaluate(true, true);
		updateDistanceDraggerConstraints();
		updateWidthDraggerConstraints();

		DebugPrinter.println("NSlitDragger.init().end");
	}

	////////////////////////////////////////////////////////////////////////////
	public void callback(EventOut who, double when, Object which) {
		int mode = ((Integer) which).intValue();
		DebugPrinter.println("callback + " + mode);

		if(mode==N)	//The one integer case
		{
			n = ((EventOutSFInt32)who).getValue();
			evaluate(true,true);
			return;
		}

		//Otherwise, it's a real, and we branch.
		float val;
		if(who instanceof EventOutSFVec3f)		//kludge; will fix later with WidgetEvent
			val=((EventOutSFVec3f)who).getValue()[0];
		else
			val=((EventOutSFFloat)who).getValue();

		switch(mode) {
		case WIDTH:
			if(m_internal_set_width) {
				m_internal_set_width = false;
				return;
			}
			width = val;
			DebugPrinter.println("width_changed = " + width);
			updateDistanceDraggerConstraints();
			evaluate(true, true);
			break;
		case DISTANCE:
			if(m_internal_set_distance) {
				m_internal_set_distance = false;
				return;
			}
			distance = val;
			DebugPrinter.println("distance_changed = " + distance);
			updateWidthDraggerConstraints();
			evaluate(true, true);
		break;
		case WIDTH_TRANSLATION:
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
			break;
		case DISTANCE_TRANSLATION:
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
			break;
		}
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

	////////////////////////////////////////////////////////////////////////////
	//																	y ^
	//																		|
	//																		|
	//
	//												 distance
	//														|
	//										+---------------+
	//										v								v
	//			 +---------+-----+dd-------+-----+-------dd+-----+---------+
	//			 |/////////|		 |/////////|		 |/////////|		 |/////////|
	//			 |/////////|		 |/////////|		 |/////////|		 |/////////|
	//			 |/////////|		 |/////////|		 |/////////|		 |/////////|
	//			 |///////wd|		 |/////////|		 |/////////|		 |wd///////|
	//			 |/////////|		 |/////////|		 |/////////|		 |/////////|
	//			 |/////////|		 |/////////|		 |/////////|		 |/////////|
	//			 |/////////|		 |/////////|		 |/////////|		 |/////////|
	//			 +---------+-----+---------+-----+---------+-----+---------+
	//								 ^		 ^						|
	//								 +-----+						+-------------------------------->
	//										|								O																 x
	//									width
	//
	// Definitions
	// -----------
	//		 N				- number of slits
	//		 width		- width of each slit
	//		 distance - distance between the centers of the slits
	//		 wd				- width dragger
	//		 dd				- distance dragger
	//
	// Odd N
	// -----
	//		 Let N, w, and d be given. Then
	//												 w		N - 1							N - 1
	//			 dd(N, w, d) = +- [- + (----- - 1) * w + (----- (w - d)]
	//												 2			2									2
	//
	//			 wd(N, w, d) = dd(N, w, d) +- w
	//
	//
	// Even N
	// ------
	//		 Let N, w, and d be given. Then
	//												 w - d		N							N
	//			 dd(N, w, d) = +- [----- + (- - 1) * w + (- - 1) * (w - d)
	//													 2			2							2
	//
	//			 wd(N, w, d) = dd(N, w, d) +- w
	//
	// Both of the cases (odd and even) simplify to the same formulas:
	//												 w * (2 * N - 3) + d * (1 - N)
	//			 dd(N, w, d) = +- [-----------------------------]
	//																			 2
	//
	//			 wd(N, w, d) = dd(N, w, d) +- w
	////////////////////////////////////////////////////////////////////////////
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
			min=max-250;
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
}
