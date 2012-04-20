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
 * This class makes up for the Double class's infathomable property of immutability.
 * It wraps a double variable and provides mutability.  The default constructor
 * initializes the wrapped double to 0.0.
 * @version 1.0
 * @author Paul A. Cleveland
 *
 */

public class WTDouble {
    private double d;
    
    public WTDouble() {
    	d = 0.0;
    }
    
    public WTDouble(double n) {
        d = n;
    }
    
    public void setValue(double n) {
        d = n;
    }
    
    public void increment() {
        d++;
    }
    
    public void decrement() {
        d--;
    }
    
    public void add(double n) {
        d += n;
    }
    
    public void subtract(double n) {
        d -= n;
    }
    
    public double getValue() {
        return d;
    }
    
    public String toString () {
        return new String("" + d + "");
    }
}
