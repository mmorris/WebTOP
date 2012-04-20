/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//WTString.java
//Defines string routines of general interest to WebTOP.
//Davis Herring
//Created January 8 2003
//Updated April 18 2005
//Version 0.25

package org.webtop.util;

public final class WTString
{
	public static String versionString(int major,int minor,int revision)
	{return String.valueOf(major)+'.'+minor+(revision==0?"":'.'+String.valueOf(revision));}

	public static boolean isNull(String str) {return str==null || str.length()==0;}
	public static String nonNull(String str) {return str==null?"":str;}
	public static boolean equal(String l,String r) {return l==r||(l!=null && l.equals(r));}

	public static String quote(String s) {return quote(s,'"');}
	public static String quote(String s,char q) {return s==null?s:q+s+q;}

	//Returns the substring of s starting at start and continuing until the next
	//occurrence of delim (or the end of the string if there are no such
	//occurrences).	 The delimiter is not included in the returned string.
	public static String delimited(String s,int start,char delim) {
		int i=s.indexOf(delim,start);
		return s.substring(start,i==-1?s.length():i);
	}

	public static String parseUnicode(String s) {
		final StringBuffer sb=new StringBuffer(s.length());
		boolean backslash=false;		// have we just seen one?
		for(int i=0;i<s.length();++i) {
			final char c=s.charAt(i);
			if(backslash) {
				// Interpret Unicode, or else just pass escaped character
				sb.append(c=='u'?(char)Integer.parseInt(s.substring(i+1,i+5),16):c);
				backslash=false;
			} else if(c=='\\') backslash=true;
			else sb.append(c);
		}
		if(backslash) throw new IllegalArgumentException("trailing backslash");
		return sb.toString();
	}

	/**
	 * Interprets a string as an integer.
	 *
	 * @param	 value the string to parse.
	 * @param	 def	 default value.
	 * @return the value of the given string as an <code>int</code>, or
	 *				 <code>def</code> if the string is null or cannot be parsed
	 *				 as a <code>int</code>.
	 */
	public static int toInt(String value,int def) {
		if(value!=null)
			try {return Integer.parseInt(value);}
			catch(NumberFormatException e) {}	//Oh well
		return def;
	}

	/**
	 * Interprets a string as a long integer.
	 *
	 * @param	 value the string to parse.
	 * @param	 def	 default value.
	 * @return the value of the given string as an <code>long</code>, or
	 *				 <code>def</code> if the string is null or cannot be parsed
	 *				 as a <code>long</code>.
	 */
	public static long toLong(String value,long def) {
		if(value!=null)
			try {return Long.parseLong(value);}
			catch(NumberFormatException e) {}	//Oh well
		return def;
	}

	/**
	 * Interprets a string as a floating-point number.
	 *
	 * @param	 value the string to parse.
	 * @param	 def	 default value.
	 * @return the value of the given string as a <code>float</code>, or
	 *				 <code>def</code> if the string is null or cannot be parsed
	 *				 as a <code>float</code>.
	 */
	public static float toFloat(String value,float def) {
		if(value!=null)
			try {return new Float(value).floatValue();}
			catch(NumberFormatException e) {}	//Oh well
		return def;
	}

	/**
	 * Interprets a string as a double-precision floating-point number.
	 *
	 * @param	 value the string to parse.
	 * @param	 def	 default value.
	 * @return the value of the given string as a <code>double</code>, or
	 *				 <code>def</code> if the string is null or cannot be parsed
	 *				 as a <code>double</code>.
	 */
	public static double toDouble(String value,double def) {
		if(value!=null)
			try {return new Double(value).doubleValue();}
			catch(NumberFormatException e) {}	//Oh well
		return def;
	}

	/**
	 * Interprets a string as a boolean value.
	 *
	 * @param	 value the string to parse.
	 * @param	 def	 default value.
	 * @return the value of the given string as a <code>boolean</code>, or
	 *				 <code>def</code> if the string is null or cannot be parsed
	 *				 as a <code>boolean</code>.
	 */
	public static boolean toBoolean(String value,boolean def) {
		return "true".equals(value)?true:"false".equals(value)?false:def;
		//Should this support 'y'/'n', 'on'/'off', and-or case-insensitivity? [Davis]
	}

	private WTString() {}
}
