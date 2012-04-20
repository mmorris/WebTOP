/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.util;
/**
 * This class makes up for the Boolean class's infathomable property of immutability.
 * It wraps a boolean variable and provides mutability.  The default constructor
 * initializes the wrapped boolean to 'false.'
 * @version 1.0
 * @author Paul A. Cleveland
 *
 */

public class WTBoolean {
    private boolean b;
    
    public WTBoolean() {
    	b = false;
    }
    
    public WTBoolean (boolean bool) {
        b = bool;
    }
    
    public void setValue(boolean bool) {
        b = bool;
    }
    
    public void toggle() {
    	b = !b;
    }
    
    public boolean getValue() {
        return b;
    }
    
    public String toString () {
        return new String("" + b + "");
    }
}

