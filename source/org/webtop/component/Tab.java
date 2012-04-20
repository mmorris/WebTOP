/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Tab.java
//Defines a component to represent a selectable heading.
//Revised and enhanced by Davis Herring and Yong Tze Chi
//Updated February 9 2004
//Version 3.1.5

package org.webtop.component;

import java.awt.*;
import java.awt.event.*;

//Tab objects send ActionEvents when selected.

public class Tab extends WComponent {
	private String label;
	private Font font;
	private Font fontB;
	private FontMetrics fontMetrics;
	private boolean isActive;

	private Image buffer;
	private Dimension bufferSize;

	private ActionListener listeners;

	public Tab() {this("",null);}

	public Tab(String l) {this(l,null);}

	public Tab(String l,String f) {
		if(f==null) f="SansSerif";
		font = new Font(f, Font.PLAIN, 11);
		fontB = new Font(f, Font.BOLD, 12);
		label=(l==null?"":l);
		setFont(font);
		setForeground(Color.white);
	}

	public void setActive(boolean ia) {
		if(isActive!=ia) {
			isActive = ia;
			updateBuffer();
			if(isShowing()) repaint();
		}
	}

	public boolean getIsActive() {return isActive;}

	public void setLabel(String l) {
		label=(l==null?"":l);
		updateSizes();
		updateBuffer();
	}

	public String getLabel() {return label;}

	public void setFont(Font f) {
		font = new Font(f.getName(), Font.PLAIN, f.getSize());
		fontB = new Font(f.getName(), Font.BOLD, f.getSize()+1);
		super.setFont(font);
		//We use the metrics for the (presumably) larger bolded font:
		fontMetrics = getFontMetrics(fontB);
		updateSizes();
		updateBuffer();
	}

	public void addActionListener(ActionListener listener) {
		if(listener==null) return;
		if(listeners==null) enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		listeners = AWTEventMulticaster.add(listeners, listener);
	}

	public void removeActionListener(ActionListener listener) {
		if(listener==null) return;
		listeners = AWTEventMulticaster.remove(listeners, listener);
		if(listeners==null) disableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}

	//Calculate component sizes
	private void updateSizes() {
		Dimension d = new Dimension(fontMetrics.stringWidth(label)+13,
			fontMetrics.getMaxAscent()+fontMetrics.getMaxDescent()+7);
		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);
		invalidate();			//New sizes -- new layout
	}

	//Internal function to prepare the Tab's image
	private void updateBuffer() {
		bufferSize = getSize();
		//Making zero-size images is (sometimes) a bad idea.
		if(bufferSize.width*bufferSize.height!=0)
			buffer=createImage(bufferSize.width,bufferSize.height);
		else
			buffer=null;

		if(buffer==null) {bufferSize=null; return;}
		Graphics g2 = buffer.getGraphics();
		g2.setColor(getBackground());
		g2.fillRect(0, 0, bufferSize.width, bufferSize.height);
		if(isActive) {
			g2.setFont(fontB);
			g2.setColor(getForeground());
			g2.drawString(label, 5, fontMetrics.getMaxAscent()+4);
			g2.setColor(getBackground().darker());
			g2.drawLine(0,0, 0,bufferSize.height-1);
			g2.drawLine(0,0, bufferSize.width-5,0);
			g2.setColor(getBackground().brighter());
			g2.drawLine(1,1, 1,bufferSize.height-1);
			g2.drawLine(1,1, bufferSize.width-5,1);
			g2.setColor(getBackground().darker().darker());
			g2.drawLine(bufferSize.width-5,0, bufferSize.width-1,3);
			g2.drawLine(bufferSize.width-1,4, bufferSize.width-1,bufferSize.height-1);
			g2.setColor(getBackground().darker());
			g2.drawLine(bufferSize.width-5,1, bufferSize.width-2,3);
			g2.drawLine(bufferSize.width-2,4, bufferSize.width-2,bufferSize.height-1);
		} else {
			g2.setFont(font);
			g2.setColor(getForeground());
			g2.drawString(label, 5, fontMetrics.getMaxAscent()+4);
			g2.setColor(getBackground().darker());
			g2.drawLine(0,2, 0,bufferSize.height-1);
			g2.drawLine(0,2, bufferSize.width-5,2);
			g2.setColor(getBackground().brighter());
			g2.drawLine(1,3, 1,bufferSize.height-1);
			g2.drawLine(1,3, bufferSize.width-5,3);
			g2.setColor(getBackground().darker().darker());
			g2.drawLine(bufferSize.width-5,2, bufferSize.width-1,5);
			g2.drawLine(bufferSize.width-1,6, bufferSize.width-1,bufferSize.height-1);
			g2.setColor(getBackground().darker());
			g2.drawLine(bufferSize.width-5,3, bufferSize.width-2,5);
			g2.drawLine(bufferSize.width-2,6, bufferSize.width-2,bufferSize.height-1);
		}
		g2.dispose();
	}

	//This just paints the buffer onto the screen; buffer is redrawn only when needed.
	public void paint(Graphics g) {
		super.paint(g);
		if(buffer==null||!getSize().equals(bufferSize))
			updateBuffer();
		//If we have an image to paint now, do so.
		if(buffer!=null) g.drawImage(buffer, 0, 0, this);
	}

	public void processMouseEvent(MouseEvent event) {
		super.processMouseEvent(event);
		if(listeners!=null && event.getComponent()==this && contains(event.getPoint()) && event.getID()==MouseEvent.MOUSE_RELEASED)
			listeners.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,label));
	}

	public String toString() {
		return getClass().getName()+"[\""+label+"\","+(isActive?"":"in")+"active]";}
}
