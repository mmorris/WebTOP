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
 * This class makes up for the Integer class's infathomable property of immutability.
 * It wraps a int variable and provides mutability.  The default constructor
 * initializes the wrapped int to 0.
 * @version 1.0
 * @author Paul A. Cleveland
 *
 */

public class WTInt {
    private int i;
    
    public WTInt() {
    	i = 0;
    }
    
    public WTInt(int integer) {
        i = integer;
    }
    
    public void setValue(int integer) {
        i = integer;
    }
    
    public void increment() {
        i++;
    }
    
    public void decrement() {
        i--;
    }
    
    public void add(int integer) {
        i += integer;
    }
    
    public void subtract(int integer) {
        i -= integer;
    }
    
    public int getValue() {
        return i;
    }
    
    public String toString () {
        return new String("" + i + "");
    }
}
