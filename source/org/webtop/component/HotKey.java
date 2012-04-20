/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//HotKey.java
//Davis Herring
//Defines an abstract class to listen for key combinations.
//Created February 17 2003 from version 2.2 of ViewpointReader.java
//Updated April 18 2005
//Version 2.1

package org.webtop.component;

import java.awt.*;
import java.awt.event.*;

public abstract class HotKey extends KeyAdapter implements GUIListener
{
	//trigggerKey matches the key code (not the key character); every bit in
	//modifiersYes is required and every bit in modifiersNo is forbidden.
	private final int triggerKey,modifiersYes,modifiersNo;

	public HotKey(int key) {this(key,0);}
	public HotKey(int key,int myes) {this(key,myes,~myes);}
	public HotKey(int key,int myes,int mno) {
		triggerKey=key;
		modifiersYes=myes;
		modifiersNo=mno;
	}

	public final void register(Component c) {c.addKeyListener(this);}
	public final void deregister(Component c) {c.removeKeyListener(this);}

	protected abstract void hotkey();

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==triggerKey &&
			 (~e.getModifiers()&modifiersYes)==0 &&
			 (e.getModifiers()&modifiersNo)==0) hotkey();
	}

	public String toString() {
		return getClass().getName()+"[key="+triggerKey+
			";modifiers="+modifiersYes+" but not "+modifiersNo+']';
	}
}
