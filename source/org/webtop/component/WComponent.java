/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//WComponent.java
//Defines an abstract base class for WebTOP components.
//Yong Tze Chi (revised and enhanced by Davis Herring)
//Updated February 3 2002
//Version 2.0

package org.webtop.component;

import java.awt.*;
import javax.swing.*;

public abstract class WComponent extends JPanel {
	protected Dimension minSize;
	protected Dimension prefSize;
	protected Dimension maxSize;

	public void setMinimumSize(Dimension size) {
		minSize = size==null ? null : new Dimension(size);}

	public void setPreferredSize(Dimension size) {
		prefSize = size==null ? null : new Dimension(size);}

	public void setMaximumSize(Dimension size) {
		maxSize = size==null ? null : new Dimension(size);}

	public Dimension getMinimumSize() {
		return minSize==null ? super.getMinimumSize() : minSize;
	}

	public Dimension getPreferredSize() {
		return prefSize==null ? super.getPreferredSize() : prefSize;
	}

	public Dimension getMaximumSize() {
		return maxSize==null ? super.getMaximumSize() : maxSize;
	}
}
