/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//VerticalLayout.java
//Defines a layout manager for arranging components in a container in a column.
//Yong Tze Chi (revised by Davis Herring)
//Updated February 15 2003
//Version 2.24

package org.webtop.component;

import java.awt.*;

public class VerticalLayout implements LayoutManager {
	private int vgap = 2;

	public final static int TOP = 0;
	public final static int MIDDLE = 1;
	public final static int BOTTOM = 2;
	private int verticalAlignment = MIDDLE;

	public final static int LEFT = 3;
	public final static int CENTER = 4;
	public final static int RIGHT = 5;
	private int horizontalAlignment = CENTER;

	public VerticalLayout() {}

	public VerticalLayout(int gap) {setVerticalGap(gap);}

	public VerticalLayout(int horizAlign, int vertAlign) {this(horizAlign,vertAlign,-1);}

	public VerticalLayout(int horizAlign, int vertAlign, int gap) {
		setHorizontalAlignment(horizAlign);
		setVerticalAlignment(vertAlign);
		setVerticalGap(gap);
	}

	public void setHorizontalAlignment(int align) {
		if(align==LEFT || align==CENTER || align==RIGHT)
			horizontalAlignment = align;
		else throw new IllegalArgumentException("Bad alignment constant.");
	}

	public void setVerticalAlignment(int align) {
		if(align==TOP || align==MIDDLE || align==BOTTOM)
			verticalAlignment = align;
		else throw new IllegalArgumentException("Bad alignment constant.");
	}

	public void setVerticalGap(int gap) {
		if(gap>=0) vgap = gap;
		else throw new IllegalArgumentException("Can't have negative vertical gap.");
	}

	public void addLayoutComponent(String name, java.awt.Component comp) {}
	public void removeLayoutComponent(java.awt.Component comp) {}

	public Dimension preferredLayoutSize(Container parent) {
		int n = parent.getComponentCount();
		int width = 0, height = 0;
		Dimension d;
		for(int i=0; i<n; i++) {
			d = parent.getComponent(i).getPreferredSize();
			if(d.width > width) width = d.width;
			height += d.height;
			if(i<n-1) height+=vgap;
		}

		Insets insets = parent.getInsets();

		return new Dimension(width + insets.left + insets.right,
			height + insets.top + insets.bottom);
	}
	public Dimension minimumLayoutSize(Container parent) {
		int n = parent.getComponentCount();
		int width = 0, height = 0;
		Dimension d;
		for(int i=0; i<n; i++) {
			d = parent.getComponent(i).getMinimumSize();
			if(d.width > width) width = d.width;
			height += d.height;
			if(i<n-1) height+=vgap;
		}

		Insets insets = parent.getInsets();
		return new Dimension(width + insets.left + insets.right,
			height + insets.top + insets.bottom);
	}

	public void layoutContainer(Container parent) {
		Dimension ourSize = preferredLayoutSize(parent),
		//Does the following do something useful?
		//And shouldn't we cater to real instead of maximum sizes?
		//This way, if a VerticallyLaidout component had a particular size forced upon it,
		//wouldn't we neglect to scale down to using minimum sizes so long as its maximum
		//size was bigger than our preferred?	 all: [Davis]
							maxSize = parent.getMaximumSize(),
							size = parent.getSize();
		Insets isets=parent.getInsets();
		Dimension compSize;
		int n = parent.getComponentCount();
		int y;
		boolean usingPreferredSize = true;

		if(ourSize.width>maxSize.width || ourSize.height>maxSize.height) {
			ourSize = minimumLayoutSize(parent);
			usingPreferredSize = false;
		}

		switch (verticalAlignment) {
		case TOP:
			y = isets.top;
			break;
		case BOTTOM:
			y = (size.height-ourSize.height);
			break;
		default:	//assume MIDDLE
			y = (size.height-ourSize.height)/2+isets.top;
			break;
		}
		for(int i=0; i<n; i++) {
			Component c = parent.getComponent(i);
			compSize = usingPreferredSize ? c.getPreferredSize() : c.getMinimumSize();

			switch (horizontalAlignment) {
			case LEFT:
				c.setBounds(isets.left, y, compSize.width, compSize.height);
				break;
			case RIGHT:
				c.setBounds(ourSize.width-isets.right-compSize.width+isets.left, y,
										compSize.width, compSize.height);
				break;
			default:	//assume CENTER
				c.setBounds((ourSize.width-isets.left-isets.right-compSize.width+isets.left)/2, y,
										compSize.width, compSize.height);
			}
			y += compSize.height + vgap;
		}
	}

	public String toString() {
		return getClass().getName() + "[hAlign=" + horizontalAlignment
			+ ",vAlign=" + verticalAlignment + ",vGap=" + vgap + "]";
	}
}
