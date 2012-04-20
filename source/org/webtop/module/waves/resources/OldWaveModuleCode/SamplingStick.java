//SamplingStick.java
//Defines the PoolWidget that reads the total height of the waves at that point.
//Davis Herring
//Created December 31 2002
//Updated May 13 2004
//Version 0.0

package webtop.wave;

import java.awt.Panel;
import vrml.external.field.*;
import webtop.wsl.script.*;

public class SamplingStick extends PoolWidget
{
	private EventInSFFloat set_ballHeight;

	public SamplingStick(Engine e,WidgetsPanel panel, float x, float y) {
		super(e,panel,x,y);
		createVRMLNode();
	}

	public void callback(EventOut eo,double when,Object data) {
		query();	//our only trick -- update value on interaction
		super.callback(eo,when,data);
	}

	public void createVRMLNode() {
		create("SamplingStick { x "+X+" y "+Y+" }");

		set_position = (EventInSFVec3f) engine.getEAI().getEI(getNode(),"set_position");
		set_ballHeight = (EventInSFFloat) engine.getEAI().getEI(getNode(),"set_ballHeight");

		engine.getEAI().getEO(getNode(),"position_changed",this, "position_changed");
		engine.getEAI().getEO(getNode(),"mouseOverPosition",this, "mouseOverPosition");
	}

	protected String getNodeName() {return "<SamplingStick>";}

	public void query() {
		float h=engine.getHeight(getX(),getY());
		set_ballHeight.setValue(h);
		if(widgetsPanel.getSelectedWidget()==this) {
			Panel p=widgetsPanel.getSwitcher().getActivePanel();
			if(p instanceof SamplePanel) ((SamplePanel)p).setValue(h);
		}
	}

	protected boolean passive() {return true;}

	public WSLNode toWSLNode() {
		WSLNode ret=new WSLNode("samplingstick");
		final WSLAttributeList atts=ret.getAttributes();
		atts.add("id", id);
		atts.add("position", "" + X + ',' + Y);
		if(widgetVisible) atts.add("selected", "true");
		return ret;
	}
}
