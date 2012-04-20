/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//ToolBar.java
//Creates a tool bar that allows users to get information about the module
//Appears at the top of modules
//Brian Thomas
//Updated June 18 2007

package org.webtop.component;

import java.awt.event.*;
//import java.awt.event.MouseListener;
import java.awt.*;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import java.util.*;
import org.webtop.component.infobrowser.*;
import org.webtop.component.WApplication;

public class ToolBar extends JPanel implements MouseListener, ActionListener {

	JButton printButton;
	JMenuBar wappMenuBar;
	
	LinkedList<ToolBarButton> buttons = new LinkedList<ToolBarButton>();
	LinkedList<String> urls = new LinkedList<String>();

	//WApplicationMenu wappMenu;
	
	WApplication wapplication;
	
	// we pass a WApplication object so we can pass 
	// the WApplication to the InfoBroswer
	// so that we can access the WSLPlayer
	// from BrowserTab when the user hits the Play button [Shane]	
	public ToolBar(WApplication wapp) {
	
		wapplication = wapp;
		
		//printButton = new ToolBarButton("Print");
		
		wappMenuBar = new JMenuBar();	
		//wappMenu    = new WApplicationMenu("Modules");
		
		
		
		wappMenuBar.setBackground(Color.BLACK);
		wappMenuBar.setBorderPainted(false);
		
		setLayout(new GridBagLayout());
		
		add(wappMenuBar, new GridBagConstraints(5, 0, 1, 1, 0,0
	               , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));
		
		//wappMenuBar.add(printButton);
		//wappMenuBar.add(wappMenu);
	}
	
	public void addBrowserButton(String name, String url) {
		ToolBarButton button = new ToolBarButton(name);
		button.addActionListener(this);
		buttons.add(button);
		urls.add(url);
		//wappMenuBar.remove(wappMenu);
		wappMenuBar.add(button);
		//wappMenuBar.add(wappMenu);
	}
	
	public void actionPerformed(ActionEvent event) {
		ToolBarButton button = (ToolBarButton)event.getSource();
		createBrowser(button.getText());
	}
	
	private void createBrowser(String text) {
		InfoBrowser browser = new InfoBrowser(wapplication);
		
		ListIterator<ToolBarButton> iter;
		for(iter=buttons.listIterator(); iter.hasNext();) {
			ToolBarButton button = iter.next();
			browser.addTab(button.getText(), urls.get(iter.nextIndex()-1));
		}
		
		browser.setActiveTab(text);
	}

	public void mouseClicked(MouseEvent event) {
		
	}

	public void mouseEntered(MouseEvent event) {
		JButton button = (JButton)event.getSource();
		button.setForeground(Color.YELLOW);

	}

	public void mouseExited(MouseEvent event) {
		JButton button = (JButton)event.getSource();
		button.setForeground(Color.WHITE);

	}

	public void mousePressed(MouseEvent event) {
		

	}

	public void mouseReleased(MouseEvent event) {
	

	}
	
	private class ToolBarButton extends JButton implements MouseListener {
		
		public ToolBarButton() {
			this("");
		}
		
		public ToolBarButton(String text) {
			super(text);
			setBackground(Color.BLACK);
			setForeground(Color.WHITE);
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
