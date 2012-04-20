/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

/**
 * <p>Title: InfoBrowser</p>
 * 
 * <p>Description: Provides additional content for modules.</p>
 * 
 * <p>Company:MSU Department of Physics and Astronomy</p>
 * 
 * @author WebTOP Team
 */

package org.webtop.component.infobrowser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.*;
import javax.swing.event.*;
import java.awt.event.*;

import javax.swing.*;

import org.webtop.component.WApplication;
import org.webtop.util.PrintFile;

public class InfoBrowser extends JFrame implements ChangeListener, HyperlinkListener, ActionListener  {

	Controls controls = new Controls();
	JTabbedPane browserPane = new JTabbedPane();
	List<BrowserTab> browserTabs = new LinkedList<BrowserTab>();;
	BrowserTab currentTab = null;
	
	WApplication wapplication;
	
	// we pass a WApplication object so we can pass 
	// the WApplication to the BrowserTab
	// so that we can access the WSLPlayer
	// when the user hits the Play button [Shane]	
	public InfoBrowser(WApplication wapp) {
	
		wapplication = wapp;
	
		this.setLayout(new GridBagLayout());
		this.add(controls, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
	               , GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0,0));
		this.add(browserPane, new GridBagConstraints(0, 1, 1, 1, 2.0, 1.0
	               , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,0));	
		this.setSize(700,700);
		this.setVisible(true);
		browserPane.addChangeListener(this);
		controls.addActionListener(this);
	}
	
	public void addTab(String name, String URL) {
		BrowserTab newTab = new BrowserTab(name, URL, wapplication);
		newTab.setHyperlinkListener(this);
		browserTabs.add(newTab);
		browserPane.add(name, newTab);
		
		if(currentTab == null) {
			currentTab = newTab;
			updateNavButtons();
		}
	}
	
	public void removeTab(String name) {
	
	}
	
	public void setActiveTab(String name) {
		ListIterator<BrowserTab> iter = browserTabs.listIterator();
		BrowserTab tab;
		do {
			tab = iter.next();
			System.out.println("Name: "+tab.name);
		} while(!tab.name.equals(name));
		
		//TODO: add code to handle if not found
		browserPane.setSelectedIndex(iter.nextIndex()-1);
		
	}
	
	//Listen for tab change
	public void stateChanged(ChangeEvent event) {
		System.out.println(event.getSource());
		currentTab = browserTabs.get(browserPane.getSelectedIndex());
		updateNavButtons();
	}
	
	public void hyperlinkUpdate(HyperlinkEvent event) {
		System.out.println("UPDATING NAV BUTTONS");
		updateNavButtons();		
	}

	private void updateNavButtons() {
		/*if(currentTab.hasBack())
			controls.enableBack();
		else
			controls.disableBack();
		
		if(currentTab.hasForward())
			controls.enableForward();
		else
			controls.disableForward();
			*/
	}

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		/*if(source == controls.backButton) {
			currentTab.back();
		}
		else if(source == controls.forwardButton) {
			currentTab.forward();
		}
		else if(source == controls.homeButton) {
			currentTab.home();
		}
		else*/ if(source == controls.printButton) {
			System.out.println("Attempting to print: " + currentTab.currentPage.toString());
			PrintFile.printComponent(currentTab.editorPane);
		}
		
	}
	
	
}
