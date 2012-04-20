//WidgetPanel.java
//Defines a base class for panels controlling PoolWidgets.
//Davis Herring
//Updated by: Jeremy Davis
//Created January 22 2003
//Updated May 2 2003
//Updated June 3 2008
//Version 1.02


package org.webtop.module.waves;

import javax.swing.JPanel;
import org.webtop.wsl.client.WSLPlayer;
import org.sdl.gui.numberbox.*;

public class WidgetPanel extends JPanel {
	
	public static final int SIG_DIGITS = 3; 
	protected Engine engine; 
	protected WSLPlayer wslPlayer; 
	
	protected static FloatBox makePositionBox(){
		return new FloatBox(-Engine.POOL_SIZE/2, Engine.POOL_SIZE/2,0,4);
	}
	
	public WidgetPanel(WSLPlayer player){
		wslPlayer = player;
	}
	
	public void setEngine(Engine e){
		engine = e; 
	}
	
}
