/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.component;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JLabel;

public class WApplicationMenu extends JMenu implements MouseListener {
	WApplicationMenuItem wavefront;
	
	WApplicationMenuItem wavesTwoMedia;
	WApplicationMenuItem wavesThreeMedia;
	
	
	public WApplicationMenu() {
		super();
		setLook();
		init();
	}

	public WApplicationMenu(Action arg0) {
		super(arg0);
		setLook();
		init();
	}

	public WApplicationMenu(String arg0, boolean arg1) {
		super(arg0, arg1);
		setLook();
		init();
	}

	public WApplicationMenu(String arg0) {
		super(arg0);
		setLook();
		init();
	}
	
	private void init() {
		wavefront = new WApplicationMenuItem("Wavefront");
		wavesTwoMedia = new WApplicationMenuItem("Waves Two Media");
		wavesThreeMedia = new WApplicationMenuItem("Waves Three Media");
		
		
		this.getPopupMenu().setLightWeightPopupEnabled(false);
		this.add(wavefront);
		add(new JSeparator());
		add(new WApplicationMenuLabel("Reflection Refraction"));
		add(wavesTwoMedia);
		add(wavesThreeMedia);
		
		
		
	}
	
	private void setLook() {
		this.setOpaque(true);
		this.setBackground(Color.BLACK);
		this.setForeground(Color.WHITE);
		this.getPopupMenu().setBackground(Color.BLACK);
		setBorderPainted(false);
		setFocusPainted(false);
		addMouseListener(this);
	}
	
	public void mouseEntered(MouseEvent event) {
		setForeground(Color.YELLOW);
	}

	public void mouseExited(MouseEvent event) {
		setForeground(Color.WHITE);
	}
	
	public void mousePressed(MouseEvent event) {}

	public void mouseReleased(MouseEvent event) {}
	
	public void mouseClicked(MouseEvent event) {}
	
	
	private class WApplicationMenuLabel extends JLabel {

		
		
		public WApplicationMenuLabel() {
			super();
			setLook();
		}

		public WApplicationMenuLabel(Icon arg0, int arg1) {
			super(arg0, arg1);
			setLook();
		}

		public WApplicationMenuLabel(Icon arg0) {
			super(arg0);
			setLook();
		}

		public WApplicationMenuLabel(String arg0, Icon arg1, int arg2) {
			super(arg0, arg1, arg2);
			setLook();
		}

		public WApplicationMenuLabel(String arg0, int arg1) {
			super(arg0, arg1);
			setLook();
		}

		public WApplicationMenuLabel(String arg0) {
			super(arg0);
			setLook();
		}

		private void setLook() {
			this.setForeground(Color.YELLOW);
		}
		
		
	}
	
	private class WApplicationMenuItem extends JMenuItem implements MouseListener {
		
		
		
		public WApplicationMenuItem() {
			super();
			setLook();
		}

		public WApplicationMenuItem(Action arg0) {
			super(arg0);
			setLook();
		}

		public WApplicationMenuItem(Icon arg0) {
			super(arg0);
			setLook();
		}

		public WApplicationMenuItem(String arg0, Icon arg1) {
			super(arg0, arg1);
			setLook();
		}

		public WApplicationMenuItem(String arg0, int arg1) {
			super(arg0, arg1);
			setLook();
		}

		public WApplicationMenuItem(String arg0) {
			super(arg0);
			setLook();
		}
		
		private void setLook() {
			this.setOpaque(true);
			this.setBackground(Color.BLACK);
			this.setForeground(Color.WHITE);
			this.setRolloverEnabled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			addMouseListener(this);
		}

		public void mouseEntered(MouseEvent event) {
			setForeground(Color.YELLOW);
		}

		public void mouseExited(MouseEvent event) {
			setForeground(Color.WHITE);
		}
		
		public void mousePressed(MouseEvent event) {}

		public void mouseReleased(MouseEvent event) {}
		
		public void mouseClicked(MouseEvent event) {}
		
	}
}
