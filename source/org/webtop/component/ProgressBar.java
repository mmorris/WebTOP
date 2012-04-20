/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//ProgressBar.java
//Defines a component to display a fraction (of a task completed or time elapsed).
//Yong Tze Chi (revised by Davis Herring)
//Updated March 10 2004
//Version 2.21

package org.webtop.component;

import java.awt.*;
import java.awt.event.*;

public class ProgressBar extends WComponent {
	public static final int NORMAL = 0;
	public static final int MOUSE_OVER = 1;
	public static final int MOUSE_DRAGGING = 2;
	protected int state = NORMAL;

	protected float progress = 0;
	protected float total = 1;

	private Color normalColor = new Color(32, 32, 196);
	private Color activeColor = new Color(64, 64, 255);
	private Color disabledColor = new Color(64, 64, 64);

	private String title;
	private static Color gColor = Color.white;
	private Color titleColor;
	private static Font gFont = new Font("SansSerif",Font.BOLD,11);
	private Font titleFont;

	public ProgressBar() {}

	public ProgressBar(float progress, float total) {
		setTotal(total);
		setProgress(progress);
	}

	public void setNormalColor(Color c) {if(c!=null) normalColor = c;}
	public void setActiveColor(Color c) {if(c!=null) activeColor = c;}
	public void setDisabledColor(Color c) {if(c!=null) disabledColor = c;}

	public void setProgress(float p) {
		if(p<0) throw new IllegalArgumentException("Cannot have negative progress.");
		if(p>total) throw new IllegalArgumentException("progress "+p+" > total "+total+'.');
		progress = p;
		if(isVisible()) repaint();
	}

	public void setTotal(float t) {
		if(t<=0) throw new IllegalArgumentException("Cannot have non-positive total.");
		total = t;
		if(progress>total) progress=total;
		if(isVisible()) repaint();
	}

	public void setTitle(String t) {
		title = t;
		if(isVisible()) repaint();
	}

	public String getTitle() {return title;}

	//We just call paint() because we always fill our canvas and we don't want
	//flicker when we only need to adjust the progress slightly.
	public void update(Graphics g) {paint(g);}
	public void paint(Graphics g) {
		Dimension d = getSize();
		Image backBuffer = createImage(d.width, d.height);
		Graphics bg = backBuffer.getGraphics();
		int filledWidth = (int) (progress * (d.width-2) / (float)total);

		bg.setColor(getBackground());
		bg.fillRect(filledWidth, 1, d.width-2-filledWidth, d.height-2);

		if(!isEnabled()) {
			bg.setColor(disabledColor);
		} else if(state==NORMAL) {
			bg.setColor(normalColor);
		} else if(state==MOUSE_OVER || state==MOUSE_DRAGGING) {
			bg.setColor(activeColor);
		}

		bg.fillRect(1, 1, filledWidth, d.height-2);

		bg.setColor(bg.getColor().darker());
		bg.drawRect(0, 0, d.width-1, d.height-1);

		if(title!=null) {
			bg.setFont(titleFont);		//titleFont is always null...?
			FontMetrics titleFontMetrics = g.getFontMetrics();
			bg.setColor(titleColor);
			bg.drawString(title, 2, (d.height+titleFontMetrics.getHeight())/2-titleFontMetrics.getDescent());
		}

		bg.dispose();
		g.drawImage(backBuffer, 0, 0, this);
	}

	public String toString() {return getClass().getName()+'['+progress+'/'+total+",title="+title+']';}
}
