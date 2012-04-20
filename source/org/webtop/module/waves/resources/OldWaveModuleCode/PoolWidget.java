//PoolWidget.java
//Abstract superclass for 'things in the pool': wave sources and sampling sticks.
//Davis Herring
//Created December 21 2002
//Updated May 13 2004
//Version 0.0

package webtop.wave;

import vrml.external.field.*;

import java.awt.Panel;
import webtop.vrml.*;
import webtop.wsl.client.WSLPlayer;
import webtop.wsl.script.WSLNode;

public abstract class PoolWidget extends VRMLObject implements EventOutObserver
{
	protected float X,Y;
	protected boolean widgetVisible = false;

	protected String id;

	protected final Engine engine;
	protected final WidgetsPanel widgetsPanel;

	protected EventInSFBool	 set_enabled;
	protected EventInSFVec3f set_position;
	protected EventInSFBool	 set_widgetVisible;

	protected boolean dragging;

	public PoolWidget(Engine e,WidgetsPanel panel,float x,float y) {
		super(e.getEAI(),e.getEAI().getNode("Widget-TRANSFORM"));
		engine=e;
		widgetsPanel=panel;
		X = x;
		Y = y;
	}

	protected void create(String vrml) {
		super.create(vrml);

		set_enabled = (EventInSFBool) eai.getEI(getNode(),"enabled");
		set_position = (EventInSFVec3f) eai.getEI(getNode(),"set_position");
		set_widgetVisible = (EventInSFBool) eai.getEI(getNode(),"set_widgetVisible");

		eai.getEO(getNode(),"mouseClicked",this, "mouse_clicked");
		eai.getEO(getNode(),"mouseOver",this, "mouse_over");

		place();
	}

	public void setEnabled(boolean enabled) {set_enabled.setValue(enabled);}

	public void showWidgets() {
		//for testing
		//System.out.println(this.getID());
		set_widgetVisible.setValue(true);
		widgetVisible = true;
	}

	public void hideWidgets() {
		set_widgetVisible.setValue(false);
		widgetVisible = false;
	}

	public boolean getWidgetVisible() {
		return widgetVisible;
	}

	public float getX() {
		return X;
	}

	public float getY() {
		return Y;
	}

	private void setVRMLPosition() {EAI.set3(set_position,X,Y,0);}

	public void setXY(float xy[], boolean setVRML) {
		setXY(xy[0],xy[1],setVRML);
	}

	public void setXY(float x, float y, boolean setVRML) {
		setX(x,false);		//so only one set will ever be done
		setY(y,setVRML);
	}

	public void setX(float x, boolean setVRML) {
		if(x<-50) X = -50;
		else if(x>50) X = 50;
		else X = x;
		if(setVRML) setVRMLPosition();
	}

	public void setY(float y, boolean setVRML) {
		if(y<-50) Y = -50;
		else if(y>50) Y = 50;
		else Y = y;
		if(setVRML) setVRMLPosition();
	}

	public String getID() {
		return id;
	}

	public void setID(String ID) {
		id = ID;
	}

	//Subclasses must override to indicate whether they affect the module.
	//If they do not, updates on user interaction can be optimized away.
	protected abstract boolean passive();

	//This should be called by subclasses from their callback() if they do not handle event
	public void callback(EventOut e, double timestamp, Object data) {
		String arg = (String) data;
		WSLPlayer wslPlayer = engine.getWSLPlayer();
		if(arg.equals("mouse_clicked")) {
			if(((EventOutSFBool) e).getValue()) {
				dragging = true;
				if(!passive()) engine.setWidgetDragging(true);
				if(wslPlayer!=null)
					wslPlayer.recordMousePressed(getID(), "");
			} else {
				dragging = false;
				if(!passive()) engine.setWidgetDragging(false);
				engine.getStatusBar().reset();
				if(wslPlayer!=null)
					wslPlayer.recordMouseReleased(getID(), "");
			}
		} else if(arg.equals("mouse_over")) {
			if(((EventOutSFBool) e).getValue()) {
				widgetVisible = true;
				widgetsPanel.show(this);
				if(wslPlayer!=null)
					wslPlayer.recordMouseEntered(getID());
			} else {
				if(wslPlayer!=null)
					wslPlayer.recordMouseExited(getID());
			}
		} else if(arg.equals("mouseOverPosition")) {
			if(((EventOutSFBool) e).getValue())
				engine.getStatusBar().setText("Use this widget to change the position.  Drag the widget to the desired position and release.");
			else if(!dragging)
				engine.getStatusBar().reset();
		} else if(arg.equals("position_changed")) {
			//Positions can be changed for SamplingSticks and for RadialSources.	We have to examine the current panel's type.
			float xyz[] = ((EventOutSFVec3f)e).getValue();
			setXY(xyz, false);
			Panel p=widgetsPanel.getSwitcher().getActivePanel();
			if(p instanceof RadialPanel) ((RadialPanel)p).setXY(xyz[0],xyz[1]);
			else if(p instanceof SamplePanel) ((SamplePanel)p).setXY(xyz[0],xyz[1]);
			else if(p instanceof PluckedPanel) ((PluckedPanel)p).setXY(xyz[0],xyz[1]);
			else {
				System.err.println("PoolWidget::callback: unexpected "+arg+" event when active panel is of type "+p.getClass().getName());
				return;
			}
			//System.out.println("PoolWidget: recording (x,y)=("+xyz[0]+','+xyz[1]+')');
			if(wslPlayer!=null)
				wslPlayer.recordMouseDragged(getID(), "position", xyz[0] + "," + xyz[1]);
			if(!passive()) if(engine.isBetweenFrames()) synchronized(engine) {engine.update();}
		}
	}

	public boolean equals(Object obj) {
		String oid=((PoolWidget)obj).id;
		return oid!=null&&oid.equals(id);
	}

	public abstract WSLNode toWSLNode();
}
