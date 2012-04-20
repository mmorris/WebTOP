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

package org.webtop.util.script;

import org.webtop.component.FolderPanel;
import org.webtop.wsl.client.WSLPlayer;
import org.webtop.wsl.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class FolderPanelScripter extends Scripter implements ChangeListener {
	private final JTabbedPane panel;
	public final String defaultPanel;

	public FolderPanelScripter(JTabbedPane fp,WSLPlayer player,String id,String param,String defPanel) {
		super(player,id,param);
		if(fp==null) {
			super.destroy();	//broken object should be discarded
			throw new NullPointerException("FolderPanel cannot be null.");
		}
		panel=fp;
		defaultPanel=defPanel;
		panel.addChangeListener(this);
	}

	protected void setValue(String value) {
		panel.setSelectedIndex(getPanelIndex(value));
	}

	protected String getValue() {
            System.out.println("VALUE RETURNED: "+panel.getSelectedComponent().getName());
            return panel.getTitleAt(panel.getSelectedIndex());}
        
        protected void setEnabled(boolean on) {panel.setEnabled(on);}

	//This is a bit overkill, but to be complete:
	//protected void destroy() {panel.removeListener(this); super.destroy();}
	
	public void stateChanged(ChangeEvent event) {
		if(event.getSource()==panel) {
                        System.out.println("index: " + ((JTabbedPane)event.getSource()).getSelectedIndex());
			int selected = ((JTabbedPane)event.getSource()).getSelectedIndex();
			
                        if(selected == -1) return;
                        System.out.println("EVENT RECORDED"+panel.getTitleAt(selected));
			
			recordActionPerformed(panel.getTitleAt(selected));
		}
		else System.err.println("FolderPanelScripter: unexpected folderChanged from "+event.getSource());
	}
	
	//Searches for a pane based on title.
	protected int getPanelIndex(String name) {
		int tabs = panel.getTabCount();
		int x=0;
		while(x<tabs && !panel.getTitleAt(x).equals(name)) x++;
		if(x == tabs) {
                  System.out.println("ERROR: Can't find panel: "+name);
                  return -1;
                }
		
		return x;
	}
}
