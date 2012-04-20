/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Separator.java
//Defines a component to draw a visual separator in a container.
//Revised by Davis Herring and Yong Tze Chi
//Updated June 18 2002
//Version 2.1.1

package org.webtop.component;

import java.awt.*;

public class Separator extends WComponent {
	public Separator(int width, int height) {
		prefSize=new Dimension(width, height);
	}

	public void paint(Graphics g) {
		g.setColor(getBackground());
		g.draw3DRect(0,0,getSize().width-1,getSize().height-1,false);
	}

	public String toString() {return getClass().getName()+'['+getSize().width+'x'+getSize().height+']';}
}
