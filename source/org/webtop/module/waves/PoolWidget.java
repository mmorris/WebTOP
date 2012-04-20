//PoolWidget.java
//Abstract superclass for 'things in the pool': wave sources and sampling sticks.
//Davis Herring
//Created December 21 2002
//Updated May 13 2004
//Updated June 8 2008 by Jeremy Davis
//Version 0.0

package org.webtop.module.waves;

import org.web3d.x3d.sai.X3DFieldEvent;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.webtop.x3d.X3DObject;

import javax.swing.JPanel;

import org.web3d.x3d.sai.*;
//import org.web3d.sai.*;
import org.webtop.wsl.client.*;
import org.webtop.wsl.script.*;
import org.webtop.util.script.*;

public abstract class PoolWidget extends X3DObject implements X3DFieldEventListener {

	protected float X,Y; 
	protected boolean widgetVisible = false; 
	
	protected String id; 
	
	protected final Engine engine; 
	protected final WidgetsPanel widgetsPanel; 
	
	protected SFBool set_enabled; 
	protected SFVec3f set_position; 
	protected SFBool set_widgetVisible; 
	
	protected boolean dragging; 
	
	public PoolWidget(Engine e, WidgetsPanel panel, float x, float y){
		super(e.getSAI(), e.getSAI().getNode("Widget-TRANSFORM"));
		engine = e; 
		widgetsPanel = panel; 
		X = x; 
		Y = y; 
	}
	
	//Going to have to double check these calls.  They are supposed to correlate to the vrml calls used in the old version
	//of the module.  However, some of these more than likely aren't right [JD]
	//May look at two and three media to see the calls [JD]
	protected void create(String x3d){
		super.createNode(x3d);
		
		set_enabled = (SFBool) sai.getInputField(getNode(), "enabled");
		set_position = (SFVec3f) sai.getInputField(getNode(), "set_position");
		set_widgetVisible = (SFBool) sai.getInputField(getNode(), "set_widgetVisible");
		
		sai.getOutputField(getNode(), "mouseClicked", this, "mouse_clicked");
		sai.getOutputField(getNode(), "mouseOver", this, "mouse_over");
		
		place();
	}
	
	public void setEnabled(boolean enabled){
		set_enabled.setValue(enabled);
	}
	
	public void showWidgets(){
		set_widgetVisible.setValue(true);
		widgetVisible = true;
	}
	
	public void hideWidgets(){
		set_widgetVisible.setValue(false);
		widgetVisible = false; 
	}
	
	public boolean getWidgetVisible(){
		return widgetVisible; 
	}
	
	public float getX(){
		return X; 
	}
	
	public float getY(){
		return Y; 
	}

	private void setX3DPosition(){
		engine.getSAI().set3(set_position, X, Y, 0f); //well you can't [JD]
	}
	
	public void setXY(float xy[], boolean setX3D){
		setXY(xy[0], xy[1], setX3D);
	}
	
	public void setXY(float x, float y, boolean setX3D){
		setX(x, false); //so only one set will ever be done
		setY(y, setX3D); 
	}
	
	public void setX(float x, boolean setX3D){
		if(x<-50)
			X = -50;
		else if(x>50)
			X = 50;
		else 
			X = x; 
		
		if(setX3D)
			setX3DPosition();
	}
	
	public void setY(float y, boolean setX3D){
		if(y<-50)
			Y = -50;
		else if(y>50)
			Y = 50;
		else 
			Y = y;
		
		if(setX3D)
			setX3DPosition();
	}
	
	public String getID(){
		return id;
	}
	
	public void setID(String ID){
		id = ID; 
	}
	
	//Subclasses must override to indicate whether they affect the module. 
	//If they do not, updates on user interaction can be optimized away.
	protected abstract boolean passive();
	
	
	
	//Implement X3DFieldEventListener
	//This should be called by subclasses from their readableFieldChanged() if they don't handle events
	public void readableFieldChanged(X3DFieldEvent arg0) {
		//this was previously located in the public void callback(...) method [JD]
		String arg = (String) arg0.getData();
		Object e = arg0.getSource();
		//WSLPlayer = engine.getWSLPlayer(); //commented out for now [JD]
		
		if(arg.equals("mouse_clicked")){
			if(((SFBool)e).getValue()){
				dragging = true; 
				if(!passive()){
					engine.setWidgetDragging(true);
				}
				//if(wslPlayer!=null)
				//wslPlayer.recordMousePressed(getID(), "");
			}
			else {
				dragging = false; 
				if(!passive())
					engine.setWidgetDragging(false);
				engine.statusBar.reset();
				//if(wslPlayer!=null)
				//wslPlayer.recordMouseReleased(getID(), "");
			}
		}
		else if(arg.equals("mouse_over")){
			if(((SFBool) e).getValue()){
				widgetVisible = true; 
				widgetsPanel.show(this);
				//if(wslPlayer!=null)
					//wslPlayer.recordMouseEntered(getID());
			}
			else{
				//if(wslPlayer!=null)
				//wslPlayer.recordMouseExited(getID());
			}
		}
		else if(arg.equals("mouseOverPosition")){
			if(((SFBool)e).getValue()){
				engine.statusBar.setText("Use this widget to change the position");
			}
			else if(!dragging){
				engine.statusBar.reset();
			}
		}
		else if(arg.equals("position_changed")){
			//Positions can be changed for SamplingSticks and RadialSources.  
			//We have to examine the current panel's type.
			float xyz[] = new float[3];
			((SFVec3f)e).getValue(xyz);
			setXY(xyz, false);
			JPanel p = widgetsPanel.getSwitcher().getActivePanel(); 
			
			//Uncomment for working with other widgets
			/*
			if(p instanceof RadialPanel)
				((RadialPanel)p).setXY(xyz[0],xyz[1]);
			else if(p instanceof SamplePanel)
				((SamplePanel)p).setXY(xyz[0], xyz[1]);
			else if(p instanceof PluckedPanel)
				((PluckedPanel)p).setXY(xyz[0], xyz[1]);
			else{
				System.err.println("PoolWidget::callback: unexpected" + arg + " event when panel is of type " +
						p.getClass().getName());
				return; 
			}
			*/
			//if(wslPlayer!=null)
			//wslPlayer.recordMouseDragged(getID(), "position", xyz[0] + "," + xyz[1]);
			if(!passive()){
				if(engine.isBetweenFrames()){
					synchronized(engine){
						engine.update();
					}
				}
			}
		}
		
	}
	
	public boolean equals(Object obj){
		String oid = ((PoolWidget)obj).id;
		return oid != null && oid.equals(id);
	}
	
	//May need to comment this out if scripting blows up [JD]
	public abstract WSLNode toWSLNode();

}
