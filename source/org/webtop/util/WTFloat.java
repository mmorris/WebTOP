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
 * This class makes up for the Float class's infathomable property of immutability.
 * It wraps a float variable and provides mutability.  The default constructor
 * initializes the wrapped float to 0f.
 * @version 1.0
 * @author Paul A. Cleveland
 *
 */

public class WTFloat {
    private float f;
    
    public WTFloat() {
    	f = 0f;
    }
    public WTFloat(float n) {
        f = n;
    }
    
    public void setValue(float n) {
        f = n;
    }
    
    public void increment() {
        f++;
    }
    
    public void decrement() {
        f--;
    }
    
    public void add(float n) {
        f += n;
    }
    
    public void subtract(float n) {
        f -= n;
    }
    
    public float getValue() {
        return f;
    }
    
    public String toString () {
        return new String("" + f + "");
    }
}
