/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//StatusBar.java
//Defines a component to be an applet's status bar with text.
//Revised by Davis Herring and Yong Tze Chi
//Updated October 24 2002
//Revised for WApplication by Brian Thomas
//Updated June 18 2007
//Version 4.0

package org.webtop.component;

import java.awt.*;

import javax.swing.*;

public class StatusBar extends JLabel {
	//private String status;
	private boolean isWarning = false;
	private boolean newWarning = false;

	public static       Color WARNING_COLOR = Color.red.brighter();
	public static final Color TEXT_COLOR = Color.YELLOW;
    public static final Color BACKGROUND_COLOR = Color.DARK_GRAY;
    
	public static void setWarningColor(Color nuColor) {
		if(nuColor!=null) WARNING_COLOR=nuColor;}

	public StatusBar() {
		this("");
	}

	public StatusBar(String s) {
		this.setOpaque(true);
		this.setAlignmentX(LEFT_ALIGNMENT);
		this.setBackground(BACKGROUND_COLOR);
		this.setForeground(TEXT_COLOR);
		this.setText(s);
	}


	public void setWarningText(String s) {
		isWarning = true; 
		newWarning = true;
		this.setForeground(WARNING_COLOR);
		this.setText(s);
	}
	
	public void setText(String s) {
		//Make sure we show some text so that the JLabel will take up space
		if(s==null) s=new String(" ");
		
		if(!newWarning) this.setForeground(TEXT_COLOR);
		newWarning = false;
		
		super.setText(s);
	}

	public String getText() {return super.getText();} //For backwards compatibility
	
	public boolean isWarning() {return isWarning;}

	/**
	 * Clears this StatusBar if it contains a warning; else does nothing.
	 */
	public void clearWarning() {if(isWarning)
		this.setForeground(TEXT_COLOR);
		setText(null);
		isWarning = false;
		newWarning = false;
	}
	
	public void reset() {
		clearWarning();
	}


	//public String toString() {return getClass().getName()+"[\""+
	//															(status==null?"":status)+'"'+
	//															(isWarning?" (warning)":"")+']';}
}
