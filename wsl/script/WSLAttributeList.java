/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package webtop.wsl.script;

import java.util.*;

import webtop.util.WTString;

/**
 * This class is used to represent the list of attributes in an XML
 * tag.	 Attributes are represented as name-value pairs.
 * <code>WSLAttributeList</code> is typically associated with a
 * <code>WSLNode</code> (the counterpart of a tag in a WSL script).
 *
 * @see WSLNode
 * @author	Yong Tze Chi
 */
public class WSLAttributeList {
	//Parallel arrays are ugly [Davis]
	private final Vector names=new Vector(),values=new Vector();

	/**
	 * Adds a new attribute into the list.
	 *
	 * @param	 name		attribute name.
	 * @param	 value	value of the attribute.
	 */
	public void add(String name, String value) {
		if(names.contains(name))
			throw new IllegalArgumentException("attribute "+WTString.quote(name)+
																				 " already exists");
		if(name==null || value==null)
			throw new NullPointerException("name and value required");
		names.addElement(name);
		values.addElement(value);
	}

	/**
	 * Returns the number of attributes in the list.
	 *
	 * @return	number of attributes in the list.
	 */
	public int getLength() {
		return names.size();
	}

	/**
	 * Gets the name of the <code>i</code>-th attribute.
	 *
	 * @param	 i	index to the attribute.
	 * @return	the name of the attribute.
	 * @exception IndexOutOfBoundsException
	 *	 if <code>i</code> is out of range.
	 */
	public String getName(int i) {return (String)names.elementAt(i);}

	/**
	 * Gets the value of the <code>i</code>-th attribute.
	 *
	 * @param	 i	index to the attribute.
	 * @return	the value of the attribute.
	 * @exception IndexOutOfBoundsException
	 *	 if <code>i</code> is out of range.
	 */
	public String getValue(int i) {return (String)values.elementAt(i);}

	/**
	 * Gets the value of an attribute by name.
	 *
	 * @param		name	name of the attribute
	 * @return	value of the named attribute;
	 *					<code>null</code> if no such attribute exists.
	 */
	public String getValue(String name) {
		final int i=names.indexOf(name);
		return (i==-1)?null:(String)values.elementAt(i);
	}

	/**
	 * Interprets the value of an attribute as an integer.
	 *
	 * @param	 name	 name of the attribute.
	 * @param	 def	 default value.
	 * @return the value of the requested attribute as an <code>int</code>,
	 *				 or <code>def</code> if the attribute does not exist or cannot
	 *				 be parsed as an <code>int</code>.
	 */
	public int getIntValue(String name,int def) {
		return WTString.toInt(getValue(name),def);
	}

	/**
	 * Interprets the value of an attribute as a long integer.
	 *
	 * @param	 name	 name of the attribute.
	 * @param	 def	 default value.
	 * @return the value of the requested attribute as an <code>long</code>,
	 *				 or <code>def</code> if the attribute does not exist or cannot
	 *				 be parsed as an <code>long</code>.
	 */
	public long getLongValue(String name,long def) {
		return WTString.toLong(getValue(name),def);
	}

	/**
	 * Interprets the value of an attribute as a floating-point number.
	 *
	 * @param	 name	 name of the attribute.
	 * @param	 def	 default value.
	 * @return the value of the requested attribute as a <code>float</code>,
	 *				 or <code>def</code> if the attribute does not exist or cannot
	 *				 be parsed as a <code>float</code>.
	 */
	public float getFloatValue(String name,float def) {
		return WTString.toFloat(getValue(name),def);
	}

	/**
	 * Interprets the value of an attribute as a double-precision floating-point
	 * number.
	 *
	 * @param	 name	 name of the attribute.
	 * @param	 def	 default value.
	 * @return the value of the requested attribute as a <code>double</code>,
	 *				 or <code>def</code> if the attribute does not exist or cannot
	 *				 be parsed as a <code>double</code>.
	 */
	public double getDoubleValue(String name,double def) {
		return WTString.toDouble(getValue(name),def);
	}

	/**
	 * Interprets the value of an attribute as a boolean value.
	 *
	 * @param	 name	 name of the attribute.
	 * @param	 def	 default value.
	 * @return the value of the requested attribute as a <code>boolean</code>,
	 *				 or <code>def</code> if the attribute does not exist or cannot
	 *				 be parsed as a <code>boolean</code>.
	 */
	public boolean getBooleanValue(String name,boolean def) {
		return WTString.toBoolean(getValue(name),def);
	}

	/**
	 * Returns a <code>String</code> representation of the list of attributes.
	 *
	 * @return	attribute list as a <code>String</code>.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for(int i=0; i<names.size(); i++) {
			buffer.append(getName(i)).append("=\"").append(getValue(i)).append("\"");
			if(i<names.size()-1) buffer.append(' ');
		}
		return buffer.toString();
	}
}
