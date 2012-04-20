/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//TabPanel.java
//Defines a 'tab-strip' component organizing selectable Tab objects.
//Revised and enhanced by Davis Herring and Yong Tze Chi
//Updated February 11 2004
//Version 3.0.6

package org.webtop.component;

import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class TabPanel extends WComponent implements ActionListener {
	private Vector<Tab> tabs=new Vector<Tab>(4,2);
	private Tab active;

	private ActionListener listeners;

	public TabPanel() {
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		prefSize=new Dimension(0,0);
		minSize=new Dimension(0,0);
	}

	public void addTab(String title) {addTab(new Tab(title),tabs.size());}
	public void addTab(Tab tab) {addTab(tab,tabs.size());}

	public void addTab(Tab tab,int place) {
		if(tab==null) return;
		//Can't add past end of tabs
		if(place>tabs.size()||place<0) place=tabs.size();
		tabs.insertElementAt(tab,place);
		tab.addActionListener(this);
		if(active==null) {
			active = tab;
			active.setActive(true);
		}
		super.add(tab,place);
		Dimension d = tab.getPreferredSize();
		minSize.setSize(minSize.width+d.width, Math.max(minSize.height,d.height));
		if(prefSize.width<minSize.width) prefSize.width = minSize.width;
		if(prefSize.height<minSize.height) prefSize.height = minSize.height;
	}

	public void removeTab(String title)	{removeTab(getTab(title));}
	public void removeTab(int index) {removeTab(getTab(index));}
	public void removeTab(Tab tab) {
		if(tab==null) return;
		tabs.removeElement(tab);
		tab.removeActionListener(this);
		if(active==tab) {
			if(tabs.isEmpty()) active=null;
			else active=(Tab)tabs.firstElement();
		}
		super.remove(tab);
		if(tabs.isEmpty())
			prefSize=minSize=new Dimension(0,0);
		else {
			int maxHeight=0,curHeight;
			Enumeration ts=tabs.elements();
			while(ts.hasMoreElements())
				if(maxHeight<(curHeight=((Tab)ts.nextElement()).getPreferredSize().height))
					maxHeight=curHeight;
			prefSize.setSize(prefSize.width-tab.getPreferredSize().width, maxHeight);
			if(minSize.height>prefSize.height) minSize.height=prefSize.height;
			if(minSize.width>prefSize.width) minSize.width=prefSize.width;
		}
	}

	//We have Container.getComponentCount() for public access...  ...But that's
	//an implementation detail.  Tabs could just be data, and we could be
	//painting the things.
	public int getTabCount() {return tabs.size();}
	public Tab getActiveTab() {return active;}
	public int getActiveIndex() {return tabs.indexOf(active);}
	public int indexOf(Tab tab) {return tabs.indexOf(tab);}
	public int indexOf(String title) {
		int i=0;
		for(;i<tabs.size()&&!((Tab)tabs.elementAt(i)).getLabel().equals(title);i++);
		return i==tabs.size()?-1:i;
	}

	public void setActiveTab(String title) {setActiveTab(getTab(title));}

	public void setActiveTab(int index)
	{setActiveTab((Tab)tabs.elementAt(index));}

	public void setActiveTab(Tab tab) {
		if(!tabs.contains(tab)) return;
		if(active!=null) active.setActive(false);
		active = tab;
		active.setActive(true);
	}

	public Tab getTab(int index) {return (Tab)tabs.elementAt(index);}
	public Tab getTab(String title) {
		Enumeration ts=tabs.elements();
		while(ts.hasMoreElements()) {
			Tab t=(Tab)ts.nextElement();
			if(t.getLabel().equals(title))
				return t;
		}
		return null;
	}

	public void setTabAlignment(int alignment) {
		setLayout(new FlowLayout(alignment, 0, 0));
	}

	public void addActionListener(ActionListener listener) {
		listeners = AWTEventMulticaster.add(listeners, listener);}

	public void removeActionListener(ActionListener listener) {
		listeners = AWTEventMulticaster.remove(listeners, listener);}

	public void actionPerformed(ActionEvent e) {
		Tab t=(Tab)e.getSource();
		if(active!=t && listeners!=null) {
			setActiveTab(t);
			listeners.actionPerformed(e);
		} else setActiveTab(t);
	}

	public String toString() {
		return getClass().getName()+'['+
				tabs.size()+" tab"+(tabs.size()==1?"":"s")+','
				+(active==null?"none":
					"#"+tabs.indexOf(active)+'['+
					active.getLabel()+']')
				+" selected]";
	}
}
