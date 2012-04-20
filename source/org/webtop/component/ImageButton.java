/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//ImageButton.java
//Defines a GUI button with images for each state
//Yong Tze Chi (revised by Davis Herring)
//Updated May 14 2004
//Version 2.22

package org.webtop.component;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.util.*;

//import webtop.util.DebugPrinter;

public class ImageButton extends WComponent implements MouseListener,ImageObserver {
	public static final int NORMAL = 0;
	//public static final int SELECTED = 1;
	public static final int DOWN = 2;
	public static final int MOUSE_OVER = 3;
	protected int state = NORMAL;

	private Image normalImage;
	private Image downImage;
	//private Image selectedImage;
	private Image mouseOverImage;
	private Image disabledImage;

	private Dimension mySize;

	private boolean normallyOff=true;
	private boolean on;

	protected String actionCommand;
	protected ActionListener listeners;

	public ImageButton() {
		updateSizes();
		addMouseListener(this);
	}

	public ImageButton(Image normal) {
		normalImage = normal;
		updateSizes();
		addMouseListener(this);
	}

	public ImageButton(Image normal, Image down) {
		normalImage = normal;
		downImage = down;
		updateSizes();
		addMouseListener(this);
	}

	public ImageButton(Image normal, Image down, Image mouseOver) {
		normalImage = normal;
		downImage = down;
		mouseOverImage = mouseOver;
		updateSizes();
		addMouseListener(this);
	}

	/*public ImageButton(Image normal, Image down, Image mouseOver, Image selected) {
		normalImage = normal;
		downImage = down;
		mouseOverImage = mouseOver;
		selectedImage = selected;
		updateSizes();
		addMouseListener(this);
	}*/

	public ImageButton(Image normal, Image down, Image mouseOver, /*Image selected, */Image disabled) {
		normalImage = normal;
		downImage = down;
		mouseOverImage = mouseOver;
		//selectedImage = selected;
		disabledImage = disabled;
		updateSizes();
		addMouseListener(this);
	}

	private void init() {updateSizes(); addMouseListener(this);}

	public void setNormalImage(Image normal) {
		normalImage = normal;
		if(isVisible() && state==NORMAL && isEnabled()) repaint();
	}

	public void setDownImage(Image down) {
		downImage = down;
		if(isVisible() && state==DOWN && isEnabled()) repaint();
	}

	public void setMouseOverImage(Image mouseOver) {
		mouseOverImage = mouseOver;
		if(isVisible() && state==MOUSE_OVER && isEnabled()) repaint();
	}

	/*public void setSelectedImage(Image selected) {
		selectedImage = selected;
		if(isVisible() && state==SELECTED) repaint();
	}*/

	public void setDisabledImage(Image disabled) {
		disabledImage = disabled;
		if(isVisible() && !isEnabled()) repaint();
	}

	public void setNormallyOff(boolean normoff) {normallyOff = normoff;}
	public boolean isNormallyOff() {return normallyOff;}

	public void setOn(boolean notoff) {
		if(normallyOff) return;	//Meaningless to set it down if it won't stay
		on = notoff;
		if(isEnabled()) {
			if(on) state = DOWN;
			else state = NORMAL;
		}
	}
	public boolean isOn() {return on;}

	public void setActionCommand(String command) {actionCommand = command;}
	public String getActionCommand() {return actionCommand;}

	public void addActionListener(ActionListener listener) {
		if(listeners==null) enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		listeners = AWTEventMulticaster.add(listeners, listener);
	}

	public void removeActionListener(ActionListener listener) {
		listeners = AWTEventMulticaster.remove(listeners, listener);
		if(listeners==null) disableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}

	public void paint(Graphics g) {
		super.paint(g);
		Dimension d = getSize();
		Image buffer = createImage(d.width, d.height);
		Graphics bg = buffer.getGraphics();
		bg.setColor(Color.black);		//Should be background color, da?
		bg.fillRect(0,0,d.width,d.height);
		if(!isEnabled() && disabledImage!=null) {
			bg.drawImage(disabledImage, 0, 0, null);
		} else if(state==DOWN && downImage!=null) {
			bg.drawImage(downImage, 0, 0, d.width, d.height, null);
		} else if(state==MOUSE_OVER && mouseOverImage!=null) {
			bg.drawImage(mouseOverImage, 0, 0, d.width, d.height, null);
		}
/*	else if(state==SELECTED && selectedImage!=null) {
			bg.drawImage(selectedImage, 0, 0, d.width, d.height, null);
		}*/
		else if(normalImage!=null){
			bg.drawImage(normalImage, 0, 0, null);
		}

		bg.dispose();
		g.drawImage(buffer, 0, 0, this);
	}

	public void mouseEntered(MouseEvent e) {
		if(normallyOff || !on) {
			state = MOUSE_OVER;
			if(mouseOverImage!=null) repaint();
		}
	}

	public void mouseExited(MouseEvent e) {
		if(normallyOff || !on) {
			state = NORMAL;
			if(mouseOverImage!=null) repaint();
		}
	}

	public void mousePressed(MouseEvent e) {
		state = DOWN;
		if(downImage!=null) repaint();
	}

	public void mouseReleased(MouseEvent e) {
		if(!contains(e.getPoint())) return;		//have to release mouse over button to click
		if(on) return;												//Can't push it any father down
		if(normallyOff) {		//pops back up
			state = NORMAL;
		} else {						//stays down
			on = true;
			state = DOWN;
		}
		if(downImage!=null) repaint();
		//If a click begins before the button is disabled, this function will
		//still get called.  As such, we filter on our own.
		if(isEnabled()) listeners.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,actionCommand));
	}

	public void mouseClicked(MouseEvent e) {}

	private void updateSizes() {
		int temp;

		//DebugPrinter.println("updateSizes() {");
		//We'll use mySize to gather data, then assign to all three sizes
		synchronized(this) {		//Firewall between us and image-loading thread (if we're not it)
			//DebugPrinter.println("  us::LOCK");
			mySize=new Dimension(Integer.MIN_VALUE,Integer.MIN_VALUE);

			if(normalImage!=null) {
				if((temp=normalImage.getHeight(this))>mySize.height) mySize.height=temp;
				if((temp=normalImage.getWidth(this))>mySize.width) mySize.width=temp;
			}
			if(downImage!=null) {
				if((temp=downImage.getHeight(this))>mySize.height) mySize.height=temp;
				if((temp=downImage.getWidth(this))>mySize.width) mySize.width=temp;
			}
			if(mouseOverImage!=null) {
				if((temp=mouseOverImage.getHeight(this))>mySize.height) mySize.height=temp;
				if((temp=mouseOverImage.getWidth(this))>mySize.width) mySize.width=temp;
			}
			if(disabledImage!=null) {
				if((temp=disabledImage.getHeight(this))>mySize.height) mySize.height=temp;
				if((temp=disabledImage.getWidth(this))>mySize.width) mySize.width=temp;
			}
			/*if(selectedImage!=null) {
				if((temp=selectedImage.getHeight(this))>mySize.height) mySize.height=temp;
				if((temp=selectedImage.getWidth(this))>mySize.width) mySize.width=temp;
			}*/

			//DebugPrinter.print("  {size,size'}: {"+mySize);

			//If no image's size is completely known yet, abort size determination
			if(mySize.width<0 || mySize.height<0) mySize=null;

			maxSize=prefSize=minSize=mySize;
			//DebugPrinter.println(", "+mySize+"}\n  us::UNLOCK\n}");
		}
		invalidate();
	}

	public boolean imageUpdate(Image img, int info, int x, int y, int width, int height) {
		//DebugPrinter.println("imageUpdate: "+info+": "+width+'x'+height+" {");
		//We're done if we have the height and width to play with.
		if((info&(HEIGHT|WIDTH))!=0) {
			synchronized(this) {
				//DebugPrinter.print("  iu::LOCK\n  data acquired (mySize="+mySize+"; ");
				//If we had no clue before, recalculate
				if(mySize==null)
					updateSizes();
				else {
					//Otherwise, just adjust preexisting figure
					if(height>minSize.height) mySize.height=height;
					if(width>minSize.width) mySize.width=width;
					maxSize=prefSize=minSize=mySize;
					invalidate();
				}
				//DebugPrinter.println("mySize->"+mySize+")\n  iu::UNLOCK\n}");
			}
			return false;
		} else {
			//DebugPrinter.println("  data insufficient\n}");
			return true;
		}
	}

	public String toString() {return getClass().getName()+"[actionCommand="+actionCommand+","+((state==NORMAL?"normal":(state==DOWN?"down":(state==MOUSE_OVER?"mouse_over":"unknown state"))))+(normallyOff?"":",on="+on)+']';}
}
