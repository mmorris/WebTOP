/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//NavigationPanel.java
//Defines a class to represent the VRML scene's NavigationPanel.
//Davis Herring
//Created March 3 2004
//Updated May 13 2004
//Version 0.2

package org.webtop.x3d.widget;

import java.util.*;

import org.web3d.x3d.sai.*;
import org.webtop.x3d.*;

//It is worth noting that many NavigationPanel-s can be made to refer to the
//same VRML entity; they will not interfere nor arrive at inconsistent states,
//and each one will deliver identical events.  One exception is that creating
//a NavigationPanel object causes the VRML NavigationPanel to reset to the 0th
//viewpoint.

public class NavigationPanel extends Widget {
	public interface Listener {
		public void visibilityChanged(NavigationPanel src,boolean visible);
		//Clients are free to keep the array given them by viewChanged().
		public void viewChanged(NavigationPanel src,float[] view);
		public void activeViewChanged(NavigationPanel src,int view);
	}
	public static abstract class Adapter implements Listener {
		public void visibilityChanged(NavigationPanel src,boolean visible) {}
		public void viewChanged(NavigationPanel src,float[] view) {}
		public void activeViewChanged(NavigationPanel src,int view) {}
	}

	private static final short typeID=getNextInputTypeID(),
														 VISIBILITY=0,
														 VIEW=1,
														 ACTIVE_VIEW=2;
	//Brian - test
	//private static final short VISIBILITY=0, VIEW=1, ACTIVE_VIEW=2;

	/**
	 * Event name used by the VRMLLib <code>NavigationPanel</code>.
	 */
	public static final String VISIBLE_OUT="visible_changed",VISIBLE_IN="set_visible",
														 VIEW_OUT="view_changed",VIEW_IN="set_view",
														 ACTIVE_VIEW_OUT="activeView_changed",ACTIVE_VIEW_IN="set_activeView";

	/**
	 * The <code>Listener</code>s to inform of events.
	 */
	private final Vector listeners=new Vector(1,2);

	private float[] view;
	private int activeView;

	private SFBool set_visible;
	private MFFloat set_view;
	private SFInt32 set_activeView;

	/*public NavigationPanel(EAI eai,String navPanel,short widgetId,String help) {
		super(eai,navPanel,widgetId,help,ISOVER_OUT,ISACTIVE_OUT,null,null,ENABLED_IN);
		getEvents(eai);
	}*/
	public NavigationPanel(SAI eai,NamedNode navPanel,short widgetId,String help) {
		super(eai,navPanel,widgetId,help,ISOVER_OUT,ISACTIVE_OUT,null,null,ENABLED_IN);
		X3DField dummy;								// to give the casts somewhere to go
		dummy=(SFBool)
			eai.getOutputField(getNode(),VISIBLE_OUT,this,new Event(typeID,VISIBILITY));
		dummy=(MFFloat)
			eai.getOutputField(getNode(),VIEW_OUT,this,new Event(typeID,VIEW));
		dummy=(SFInt32)
			eai.getOutputField(getNode(),ACTIVE_VIEW_OUT,this,new Event(typeID,ACTIVE_VIEW));
		set_visible=(SFBool)eai.getInputField(getNode(),VISIBLE_IN);
		set_view=(MFFloat)eai.getInputField(getNode(),VIEW_IN);
		set_activeView=(SFInt32)eai.getInputField(getNode(),ACTIVE_VIEW_IN);
		//This horrid hack fetches a value from the panel; maybe query_view wasn't
		//such a bad thing, but oh was it ugly.  [Davis]
		setActiveView(0);
	}

	public void setVisible(boolean on) {set_visible.setValue(on);}

	public void setView(String view) {
		if(view==null) throw new NullPointerException("null view");

		final float[] array=new float[6];
		for(int i=0,index=0;i<6;i++) {
			try {
				//Our own tokenizer; should probably actually write a silly
				//string-splitter class.	(StreamTokenizer doesn't handle scientific
				//notation at all.) [Davis]
				String next=org.webtop.util.WTString.delimited(view,index,' ');
				index+=next.length()+1;
				array[i]=new Float(next.trim()).floatValue();
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException("bad view: "+view);
			}
		}

		setView(array);
	}

	public void setView(float[] view) {
		if(view==null || view.length!=6)
			throw new IllegalArgumentException("invalid view array");
		set_view.setValue(view.length, view); //TODO: brian - are these args correct?
	}

	public float[] getView() {return (float[])view.clone();}

	public void setActiveView(int view) {set_activeView.setValue(view);}

	public int getActiveView() {return activeView;}

	/**
	 * Registers the given object to be notified of VRML events listened to by
	 * this <code>NavigationPanel</code>.
	 */
	public void addListener(Listener toAdd)
	{if(toAdd!=null && !listeners.contains(toAdd)) listeners.addElement(toAdd);}

	/**
	 * Stops notifying the given object of navigation events.  Does nothing if
	 * the given object is null or not registered to be notified of such events.
	 */
	public void removeListener(Listener toRemove)
	{listeners.removeElement(toRemove);}

	//These (and the corresponding events-out) should be made into widgets
	//creatable with navpanel.makePanWidget(short id) or the like.
// 	public void setRotation(float horiz,float vert) {}
// 	public void setPan(float horiz,float vert) {}
// 	public void setZoom(float zoom) {}

	//If we get an event, pass it along to any registered listeners
	public void readableFieldChanged(X3DFieldEvent x3dFieldEvent) {
		final Event e = (Event) x3dFieldEvent.getData();
		if(e.typeID==typeID) {
			Enumeration en=listeners.elements();
			switch(e.eventID) {
			case VISIBILITY:
				final boolean visible=((SFBool)x3dFieldEvent.getSource()).getValue();
				while(en.hasMoreElements())
					try {
						((Listener)en.nextElement()).visibilityChanged(this,visible);
					} catch(RuntimeException re) {
						System.err.println(EXCEPTION_MSG);
						re.printStackTrace();
					}
				break;
			case VIEW:
				view = new float[6]; //Brian: This must be created unlike with sai
				((MFFloat)x3dFieldEvent.getSource()).getValue(view);
				while(en.hasMoreElements())
					try {
						((Listener)en.nextElement()).viewChanged(this,(float[])view.clone());
					} catch(RuntimeException re) {
						System.err.println(EXCEPTION_MSG);
						re.printStackTrace();
					}
				break;
			case ACTIVE_VIEW:
				activeView=((SFInt32)x3dFieldEvent.getSource()).getValue();
				while(en.hasMoreElements())
					try {
						((Listener)en.nextElement()).activeViewChanged(this,activeView);
					} catch(RuntimeException re) {
						System.err.println(EXCEPTION_MSG);
						re.printStackTrace();
					}
				break;
			default:
				System.err.println("NavigationPanel: unexpected event: "+e);
				break;
			}
		} else super.readableFieldChanged(x3dFieldEvent);
	}

	public static String viewString(float[] view) {
		final StringBuffer sb=new StringBuffer();
		for(int i=0;i<6;i++) {
			if(i>0) sb.append(' ');
			sb.append(org.sdl.math.FPRound.toFixVal(view[i],3));
		}
		return sb.toString();
	}
}
