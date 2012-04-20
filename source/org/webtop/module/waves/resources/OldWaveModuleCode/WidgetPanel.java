//WidgetPanel.java
//Defines a base class for panels controlling PoolWidgets.
//Davis Herring
//Created January 22 2003
//Updated May 2 2003
//Version 1.01

package webtop.wave;

import java.awt.Panel;
import webtop.wsl.client.WSLPlayer;
import sdl.gui.numberbox.FloatBox;

public class WidgetPanel extends Panel
{
	public static final int SIG_DIGITS=3;

	protected Engine engine;
	protected WSLPlayer wslPlayer;

	protected static FloatBox makePositionBox() {
		return new FloatBox(-Engine.POOL_SIZE/2,Engine.POOL_SIZE/2,0,4);
	}

	public WidgetPanel(WSLPlayer player) {
		wslPlayer=player;
	}

	public void setEngine(Engine e) {engine = e;}
}
