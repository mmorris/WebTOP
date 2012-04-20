/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//FolderPanelScripter.java
//Declares a class for automating WSL scripting with FolderPanels.
//Davis Herring
//Created February 4 2004
//Updated February 5 2004
//Version 0.0

package webtop.util.script;

import webtop.component.FolderPanel;
import webtop.wsl.client.WSLPlayer;
import webtop.wsl.event.*;

public class FolderPanelScripter extends Scripter implements FolderPanel.Listener
{
	private final FolderPanel panel;
	public final String defaultPanel;

	public FolderPanelScripter(FolderPanel fp,WSLPlayer player,String id,String param,String defPanel) {
		super(player,id,param);
		if(fp==null) {
			super.destroy();	//broken object should be discarded
			throw new NullPointerException("FolderPanel cannot be null.");
		}
		panel=fp;
		defaultPanel=defPanel;
		panel.addListener(this);
	}

	protected void setValue(String value) {
		panel.showFolder(value==null?defaultPanel:value);
	}

	protected String getValue() {return panel.currentFolder();}

	protected void setEnabled(boolean on) {panel.setEnabled(on);}

	//This is a bit overkill, but to be complete:
	protected void destroy() {panel.removeListener(this); super.destroy();}

	public void folderChanged(FolderPanel fp,int which) {
		if(fp==panel) recordActionPerformed(fp.titleAt(which));
		else System.err.println("FolderPanelScripter: unexpected folderChanged from "+fp);
	}
}
