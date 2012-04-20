/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//FolderPanel.java
//Specifies the FolderPanel class, for building tabbed-dialog constructs
//Revised and updated by Yong Tze Chi and Davis Herring
//Updated February 11 2004
//Version 3.51

package org.webtop.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class FolderPanel extends WComponent implements FocusListener, MouseListener, ActionListener
{
	//folderChanged() is called before the folder actually changes over
	//(This means that which != fp.currentFolderIndex().)
	public interface Listener extends EventListener
	{public void folderChanged(FolderPanel fp, int which);}

	private final TabPanel tabPanel=new TabPanel();
	private final Vector<JPanel> panels=new Vector<JPanel>(4,2);
	private final Vector<Listener> listeners=new Vector<Listener>(2,2);
	private JPanel activePanel;
	private boolean changing;

	private Dimension prevSize;

	//FolderPanel is stubborn.	Change one of its panels, and it resets it for you.
	private ComponentListener resetter=new ComponentListener()
		{public void componentResized(ComponentEvent e) {if(!changing) invalidate();}
		 public void componentMoved(ComponentEvent e) {if(!changing) invalidate();}
		 public void componentShown(ComponentEvent e)
			{if(!changing) e.getComponent().setVisible(e.getComponent()==activePanel);}
		 public void componentHidden(ComponentEvent e)
			{if(!changing) e.getComponent().setVisible(e.getComponent()==activePanel);}};

	public FolderPanel() {
		super.setLayout(null);
		setForeground(Color.white);
		add(tabPanel);
		tabPanel.addFocusListener(this);
		tabPanel.addActionListener(this);
	}

	public void addFolder(String title, JPanel panel)
	{addFolder(title,panel,null);}
	public void addFolder(String title, JPanel panel,String font) {
		//Disallow multiple folders with the same label
		if(whichFolder(title)!=-1) return;

		final Tab tab=new Tab(title,font);
		tabPanel.addTab(tab);
		tab.addMouseListener(this);
		tab.addFocusListener(this);

		panel.setVisible(false);
		panel.addComponentListener(resetter);
		panels.addElement(panel);

		fpLayout();

		add(panel);
		if(activePanel==null)
			showFolder(title);
	}

	public void removeFolder(String title) {removeFolder(whichFolderChecked(title));}
	public void removeFolder(int which) {
		if(which<0 || which>=panels.size()) throw new IndexOutOfBoundsException("No such panel: #"+which);

		final Tab tab=tabPanel.getTab(which);
		tabPanel.removeTab(tab);
		//Just for completeness:
		tab.removeMouseListener(this);
		tab.removeFocusListener(this);

		final JPanel panel=(JPanel)panels.elementAt(which);
		remove(panel);
		panel.removeComponentListener(resetter);
		panels.removeElementAt(which);

		fpLayout();

		if(activePanel==panel) showFolder(Math.min(which,panels.size()-1));
	}

	public void showFolder(String title) {showFolder(whichFolderChecked(title));}
	public void showFolder(int which) {
		if(which<0 || which>=panels.size()) throw new IndexOutOfBoundsException("No such panel: #"+which);

		//Do nothing if that folder is already fully showing
		if(activePanel!=null && which==panels.indexOf(activePanel) && activePanel.isVisible()) return;

		final Enumeration e=listeners.elements();
		while(e.hasMoreElements())
			try {
				((Listener)e.nextElement()).folderChanged(this,which);
			} catch(RuntimeException re) {
				System.err.println("Exception occurred during component event handling:");
				re.printStackTrace();
			}

		tabPanel.setActiveTab(which);

		changing=true;	//to calm the resetter
		if(activePanel!=null) activePanel.setVisible(false);
		activePanel=(JPanel)panels.elementAt(which);
		activePanel.validate();
		activePanel.setVisible(true);
		changing=false;

		if(isShowing()) repaint();
	}

	public void addListener(Listener l)
	{if(l!=null && !listeners.contains(l)) listeners.addElement(l);}
	public void removeListener(Listener l) {listeners.removeElement(l);}

	public int getFolderCount() {return panels.size();}

	public JPanel getPanel(int index) {return (JPanel)panels.elementAt(index);}
	public JPanel getPanel(String title) {
		final int which=whichFolder(title);
		return which==-1?null:getPanel(which);
	}

	public JPanel currentPanel() {return activePanel;}
	public String currentFolder() {return tabPanel.getActiveTab().getLabel();}
	public int currentFolderIndex() {return tabPanel.getActiveIndex();}

	public int whichFolder(String title) {return tabPanel.indexOf(title);}
	public String titleAt(int index) {return tabPanel.getTab(index).getLabel();}

	private int whichFolderChecked(String title) {
		final int which=whichFolder(title);
		if(which==-1) throw new NoSuchElementException("No such panel: "+title);
		return which;
	}

	public void setTabAlignment(int alignment) {tabPanel.setTabAlignment(alignment);}

	public void setLayout(LayoutManager mgr) {return;}

	public void paint(Graphics g) {
		//if(!getSize().equals(prevSize)) invalidate();	 //should be done already [Davis]
		final Dimension tabSize = tabPanel.getPreferredSize();
		g.setColor(getBackground());
		g.draw3DRect(0,tabSize.height,prevSize.width-1,prevSize.height-tabSize.height-1,true);
		if(tabPanel.getActiveTab()!=null) {
			final Rectangle r=tabPanel.getActiveTab().getBounds();
			g.drawLine(r.x+2,tabSize.height,r.x+r.width-2,tabSize.height);
		}
	}

	private void fpLayout() {
		//nbp.println(this+"::fpLayout()... (size="+getSize()+")");
		//nbp.println(new Exception("tracer"));
		final Dimension tpSize=tabPanel.getPreferredSize();

		final Dimension minSize=new Dimension(Integer.MIN_VALUE,Integer.MIN_VALUE);
		final Dimension prefSize=new Dimension(Integer.MIN_VALUE,Integer.MIN_VALUE);
		final Dimension maxSize=new Dimension(Integer.MIN_VALUE,Integer.MIN_VALUE);

		prevSize=getSize();
		tabPanel.setBounds(0,0, prevSize.width, tpSize.height);
		tabPanel.doLayout();

		changing=true;	//to calm the resetter
		for(int i=0;i<panels.size();i++) {
			JPanel curPanel=(JPanel)panels.elementAt(i);
			//nbp.println("FolderPanel::fpLayout: iteration "+i+", panel="+curPanel);
			curPanel.setBounds(4,tpSize.height+5,
												 prevSize.width-8,prevSize.height-tpSize.height-9);
			//nbp.println("--new size="+curPanel.getBounds());
			curPanel.doLayout();

			//Play layout manager bingo: collect the various sizes
			int temp=curPanel.getMinimumSize().width; if(temp>minSize.width) minSize.width=temp;
			temp=curPanel.getMinimumSize().height; if(temp>minSize.height) minSize.height=temp;
			temp=curPanel.getPreferredSize().width; if(temp>prefSize.width) prefSize.width=temp;
			temp=curPanel.getPreferredSize().height; if(temp>prefSize.height) prefSize.height=temp;
			temp=curPanel.getMaximumSize().width; if(temp>maxSize.width) maxSize.width=temp;
			temp=curPanel.getMaximumSize().height; if(temp>maxSize.height) maxSize.height=temp;
			//nbp.println("new minSize: "+minSize);
			//nbp.println("new maxSize: "+minSize);
			//nbp.println("new prefSize: "+minSize);
		}
		changing=false;

		if(minSize.width<0) minSize.width=0;
		if(minSize.height<0) minSize.height=0;
		if(maxSize.width<0) maxSize.width=0;
		if(maxSize.height<0) maxSize.height=0;
		if(prefSize.width<0) prefSize.width=0;
		if(prefSize.height<0) prefSize.height=0;

		//Now correct our bingo card to account for the TabPanel and border.
		minSize.width+=8; minSize.height+=tpSize.height+9;
		prefSize.width+=8; prefSize.height+=tpSize.height+9;
		maxSize.width+=8; maxSize.height+=tpSize.height+9;
		synchronized(this) {
			this.minSize=minSize;
			this.prefSize=minSize;
			this.maxSize=minSize;
		}
	}

	//If a component under us changes its preferred/etc.
	//sizes, this'll (hopefully) get called and count it.
	public void invalidate() {fpLayout(); super.invalidate();}

	//Shows a folder in response to a tab being clicked
	public void actionPerformed(ActionEvent e) {showFolder(tabPanel.indexOf((Tab)e.getSource()));}

	//The code below simply relays events to anyone listening to the FolderPanel.
	//The source of the event will be the TabPanel or else one of the individual tabs.
	public void focusGained(FocusEvent e) {processFocusEvent(e);}
	public void focusLost(FocusEvent e) {processFocusEvent(e);}

	public void mouseClicked(MouseEvent e) {processMouseEvent(e);}
	public void mouseEntered(MouseEvent e) {processMouseEvent(e);}
	public void mouseExited(MouseEvent e) {processMouseEvent(e);}
	public void mousePressed(MouseEvent e) {processMouseEvent(e);}
	public void mouseReleased(MouseEvent e) {processMouseEvent(e);}

	public String toString()
	{return getClass().getName()+'['+panels.size()+" folder"+(panels.size()==1?"":"s")+(activePanel==null?"]":",active="+panels.indexOf(activePanel)+']');}
}
