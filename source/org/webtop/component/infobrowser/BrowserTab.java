/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.component.infobrowser;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.*;
import java.util.jar.*;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.net.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BoxLayout;
import javax.swing.event.HyperlinkEvent.EventType;

import java.net.URI;

import org.webtop.component.WApplication;
import org.webtop.wsl.client.WSLPanel;
import org.webtop.wsl.client.WSLPlayer;

// ExampleFileFilter provided by Sun Microsystems
import org.webtop.util.ExampleFileFilter;

public class BrowserTab extends JScrollPane implements HyperlinkListener {


	LinkedList<URL> history = new LinkedList<URL>();
	ListIterator<URL> currentURL = history.listIterator();
	URL homeURL;
	String name;
	HyperlinkListener hyperlinkListener = null;
	
	WApplication wapplication;
	
	Map<String,HyperlinkListener> urlHandlers;
	
	JEditorPane editorPane = new JEditorPane();
	
	URL currentPage;
	
	//Constructor
	// we pass a WApplication object so we can access 
	// the WSLPlayer in WApplication when the user
	// presses the Play button [Shane]
	public BrowserTab(String name, String url, WApplication wapp) {
	
		wapplication = wapp;
	
		try{
			homeURL = stringToURL(url);
		} catch(Exception e) { System.out.println("Error in creating URL");}

		urlHandlers = new HashMap<String,HyperlinkListener>();

		HyperlinkListener wslFileListener = new HyperlinkListener(){
			public void hyperlinkUpdate(HyperlinkEvent event)
			{
				JEditorPane pane;
				try{
					pane = new JEditorPane(event.getURL());
				}
				catch(Exception e)
				{
					System.out.println("Error finding file");
					return;
				}
				
				JFileChooser jfc = new JFileChooser( System.getProperty("user.home") );
				jfc.setAcceptAllFileFilterUsed(false);
				ExampleFileFilter filter = new ExampleFileFilter();
				filter.addExtension("wsl");
				filter.setDescription("WebTop Script Files");
				jfc.addChoosableFileFilter(filter);
				//jfc.setFileFilter(filter);
				
				
				while(true)
				{
					int status = jfc.showSaveDialog(null);
					
					if( status == JFileChooser.APPROVE_OPTION )
					{
						//String wslFileName = event.getURL().getFile().getName();
						String localFileName = jfc.getSelectedFile().getAbsolutePath();
						
						if(!localFileName.contains(".wsl")) // not a wsl file, so append wsl to the file name
						{
							localFileName += ".wsl";
						}   
						
						File selectedFile = new File(localFileName);
						
						if(selectedFile.exists())
						{
							// prompt the user to overwrite
							int overWrite = JOptionPane.showConfirmDialog(null,"File already exists.  Do you want to overwrite that file?",
																		  "Overwrite File?",JOptionPane.YES_NO_OPTION);
							if(overWrite == JOptionPane.NO_OPTION)
								continue;
							
						}
						
						try{
							PrintWriter outFile = new PrintWriter(selectedFile);
							pane.write(outFile);
							return;
						}
						catch(java.io.IOException e)
						{
							System.out.println("Error saving the file");
						}
					}
					else if(status == JFileChooser.CANCEL_OPTION)
					{
						return;
					}
				}
			}
		};
		
		addUrlHandler("wsl", wslFileListener);
		
		HyperlinkListener imgFileListener = new HyperlinkListener(){
			public void hyperlinkUpdate(HyperlinkEvent event)
			{
				try{
					JEditorPane pane = new JEditorPane();
					pane.setContentType("text/html");
					pane.setEditable(false);
					pane.setText("<html><body><img src= " + event.getURL() + " ></body></html>");
					JFrame frame = new JFrame();
					frame.add(pane);
					frame.setSize(800,600);
					frame.setVisible(true);
				}
				catch(Exception e)
				{
					System.out.println("Error opening image");
					return;
				}
			}
		};
		
		addUrlHandler("jpg", imgFileListener);
		addUrlHandler("gif", imgFileListener);
		addUrlHandler("png", imgFileListener);
		addUrlHandler("bmp", imgFileListener);
		
		HyperlinkListener playScriptListener = new HyperlinkListener(){
			public void hyperlinkUpdate(HyperlinkEvent event)
			{
				try{
					String scriptString = event.getURL().toString();
					scriptString = scriptString.substring(0,scriptString.length()-5);
					
					URL scriptURL = new URL(scriptString);
					System.out.println("Script to play: " + scriptURL);
					WSLPanel wslPanel = wapplication.getWSLPanel();
					
					String jarString = scriptString.substring(9,scriptString.indexOf('!'));
					System.out.println("Jar string: " + jarString);
					scriptString = scriptString.substring(scriptString.lastIndexOf('!')+2,scriptString.length());
					System.out.println("Script file: " + scriptString);
					
					jarString = jarString.replaceAll("%20"," ");
					
					JarFile jarFile = new JarFile(new File(jarString).toURI().getPath());
					System.out.println("Jar file: " + jarFile);
					JarEntry entry = jarFile.getJarEntry(scriptString);
					
					if(entry == null)
					{
						System.out.println("Error getting jarEntry");
						System.exit(0);
					}
					
					InputStream input = jarFile.getInputStream(entry);
					wslPanel.loadScript(input);
					wapplication.requestFocus();
				}
				catch(Exception e)
				{
					System.out.println("Error playing script: ");
					e.printStackTrace();
					return;
				}
			}
		};
		
		addUrlHandler("play", playScriptListener);
		
		
		//Put the editor pane in a scroll pane.
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		setPreferredSize(new Dimension(400,400));
		setMinimumSize(new Dimension(100, 100));
		this.name=name;

		editorPane.setVisible(true);
		editorPane.setEditable(false);
		editorPane.addHyperlinkListener(this);
		browseTo(url);
		this.setViewportView(editorPane);

	}

	//Function called when a HyperlinkEvent occurs
	public void hyperlinkUpdate(HyperlinkEvent event) {
		System.out.println("hyperlinkUpdate called");
		if(event.getEventType()==EventType.ACTIVATED) {
			
			String file = event.getURL().getFile();
			
			//find the file extension if there is one
			if(file.contains(".")) {
				int lastPeriod = file.lastIndexOf(".");
				if(file.length() > lastPeriod) {
					
					String extension = file.substring(lastPeriod+1);
					
					System.out.println("extension: " + extension);
					//if there is a handler for the extension, call it
					//instead of browsing to the url
					if(urlHandlers.containsKey(extension)) {
						System.out.println("found hyperlinkListener for " + extension);
						urlHandlers.get(extension).hyperlinkUpdate(event);
				
						return;
					}
				}
			}
			browseTo(event.getURL(), true);
		}
	}
	
	
	public void browseTo(String address) {
		
		URL url = getClass().getResource(address);
		if(url == null) {
			System.out.println("Unable to locate: " + address);
			
		}
		browseTo(url, true);
		
	}
	
	
	//Browse to a certain url
	public void browseTo(URL url, boolean clearHistory) {
		
		if(clearHistory) {
			clearForwardHistory();
			currentURL.add(url);
		}
		System.out.println("Size: "+history.size());
		
		try {		
			System.out.println("Trying URL go to:" +url);
			if (url != null) {
				try {
					editorPane.setPage(url);
					currentPage = url;
				} catch (Exception e) {
					System.err.println("Attempted to read a bad URL: " + url);
				}
			}
		} catch (Exception e) { System.out.println("Browser error");}
		
		HyperlinkEvent event = new HyperlinkEvent(this,EventType.ACTIVATED,url);
		if(hyperlinkListener != null)
			hyperlinkListener.hyperlinkUpdate(event);
	}
	
	//Set the home page for the tab
	public void setHome(String url) {
		homeURL = stringToURL(url);
	}

	//Gets the current home for the tab
	public String getHome() {
		return homeURL.toString();
	}
	
	private URL stringToURL(String url) {
		URL absURL = this.getClass().getResource(url);
		if(absURL==null)
			System.out.println("Unable to getResource for URL: "+url);
		return absURL;
	}
	
	//Goes back, if possible
	public void back() {
		if(hasBack()) {
			currentURL.previous();
			URL url = currentURL.previous();
			currentURL.next();
			browseTo(url, false);
		}
	}
	
	//Goes forward, if possible
	public void forward() {
		if(currentURL.hasNext()) {
			browseTo(currentURL.next(), false);
		}
	}
	
	//Goes back to the home url
	public void home() {
		browseTo(homeURL, true);
	}
	
	//Checks to see if back button should be enabled
	public boolean hasBack() {
		//return currentURL.hasPrevious();
		return currentURL.nextIndex()>1;
	}
	
	//Checks to see if forward button should be enabled
	public boolean hasForward() {
		return currentURL.hasNext();
	}

	//When user goes back in history and then clicks a link, we want to
	//erase previous history
	private void clearForwardHistory() {
		while(currentURL.hasNext()) {
			currentURL.next();
			currentURL.remove();
		}
	}
	
	public void setHyperlinkListener(HyperlinkListener _hyperlinkListener) {
		hyperlinkListener = _hyperlinkListener;
	}
	
	public void addUrlHandler(String extension, HyperlinkListener listener) {
		urlHandlers.put(extension,listener);
	}
}
