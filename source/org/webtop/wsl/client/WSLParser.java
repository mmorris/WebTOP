/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.wsl.client;

import java.io.*;
import java.util.Stack;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderAdapter;
import org.xml.sax.helpers.XMLReaderFactory;

import org.webtop.wsl.script.*;

/**
 * <code>WSLParser</code> is used by <code>WSLPlayer</code> to parse a WSL
 * script.	It implements the <code>DocumentHandler</code> of JAXP and uses the
 * <code>SAXParser</code> to actually do the parsing. As <code>SAXParser</code>
 * parses the WSL script, methods in <code>WSLParser</code> as parts of the
 * script is recognized. This gives a way for <code>WSLParser</code> to
 * construct a <code>WSLScript</code> object.
 *
 * <p>To parse a WSL script, the <a href="#parse"><code>parse()</code></a> is
 * called.  Upon successful parsing, a <code>WSLScript</code> instance is
 * returned.</p>
 *
 * @author Yong Tze Chi
 * @author Davis Herring
 */
public class WSLParser extends DefaultHandler {
	/**
	 * Thrown to indicate problems encountered by <code>WSLParser</code> in
	 * loading a script.
	 *
	 * @author Davis Herring
	 * @see WSLParser#parse
	 * @since WSL2
	 */
	public static class InvalidScriptException extends Exception {
		/**
		 * Constructs an <code>InvalidScriptException</code> with no detail
		 * message.
		 */
		public InvalidScriptException() {}
		/**
		 * Constructs an <code>InvalidScriptException</code> wrapper around an
		 * exception thrown during script parsing.
		 *
		 * @param xmlException the exception to enclose.	Its detail message (if
		 *										 any) is used as the detail message for this
		 *										 exception.	 If <code>xmlException</code> is
		 *										 <code>null</code>, it is ignored.
		 */
		public InvalidScriptException(Throwable xmlException) {
			super(xmlException==null?null:xmlException.getMessage());
			xml=xmlException;
		}
		/**
		 * Constructs an <code>InvalidScriptException</code> with the specified
		 * detail message.
		 *
		 * @param message the detail message.
		 */
		public InvalidScriptException(String message) {super(message);}
		/**
		 * Constructs an <code>InvalidScriptException</code> with the specified
		 * detail message as a wrapper around an exception thrown during script
		 * parsing.
		 *
		 * @param xmlException the exception to enclose.
		 * @param message the detail message.
		 */
		public InvalidScriptException(Throwable xmlException, String message)
		{super(message); xml=xmlException;}

		/**
		 * Tests whether this exception was created as a wrapper for an exception
		 * thrown during script parsing.
		 *
		 * @return <code>true</code> if this exception was constructed with a
		 * non-<code>null</code> wrapped exception; <code>false</code> otherwise.
		 */
		public boolean isXMLException() {return xml!=null;}
		/**
		 * Gets the exception this <code>InvalidScriptException</code> encloses.
		 *
		 * @return the exception used to create this
		 * <code>InvalidScriptException</code>, or <code>null</code> if none was
		 * given.
		 */
		public Throwable getXMLException() {return xml;}

		/**
		 * Returns a string representation of this exception.	 If this exception
		 * is wrapping another exception, its <code>toString()</code> method's
		 * value is included.
		 *
		 * @return a string representing this exception.
		 */
		public String toString() {
			return getClass().getName()+(xml==null?"":"["+xml+"]")+
				(getMessage()==null?"":": "+getMessage());
		}

		/**
		 * Prints this exception and its backtrace to standard error.	 If this
		 * exception is wrapping another exception, its
		 * <code>printStackTrace()</code> method is also invoked.
		 *
		 * @see Throwable#toString
		 */
		public void printStackTrace() {
			printStackTrace(System.err);
		}
		/**
		 * @see #printStackTrace()
		 */
		public void printStackTrace(PrintWriter out) {
			if(xml==null) super.printStackTrace(out);
			else {
				out.println(super.toString()+"\n[wrapped exception:]");
				xml.printStackTrace(out);
			}
		}
		/**
		 * @see #printStackTrace()
		 */
		public void printStackTrace(PrintStream out) {
			if(xml==null) super.printStackTrace(out);
			else {
				out.println(super.toString()+"\n[wrapped exception:]");
				xml.printStackTrace(out);
			}
		}
		private Throwable xml;
	}

	private WSLScript script;
	private Stack<WSLNode> nodeStack;

	private String moduleName;

	/**
	 * Constructs a <code>WSLParser</code>.
	 *
	 * @param name the name of the current WebTOP module against which to check
	 *						 a loaded script.
	 */
	public WSLParser(String name) {moduleName=name;}

	/**
	 * This method is called when <code>SAXParser</code> recognizes a starting
	 * tag.
	 *
	 * @param	 name	 name of the tag recognized.
	 * @param	 attr	 list of attributes associated with the tag.
	 */
	public void startElement(java.lang.String uri,
            java.lang.String name,
            java.lang.String qName,
            Attributes attr) {
		WSLAttributeList atts = new WSLAttributeList();

		for(int i=0; i<attr.getLength(); i++)
			atts.add(attr.getLocalName(i), attr.getValue(i));

		System.out.println("element name: " + name);
		WSLNode n = new WSLNode(name, atts);
		if(name.equals(moduleName)) script.addModuleNode(n);
		else if(name.equals("script")) script.addScriptNode(n);
		else ((WSLNode)nodeStack.peek()).addChild(n);

		nodeStack.push(n);
	}

	/**
	 * This method is called when <code>SAXParser</code> recognizes an ending
	 * tag.
	 *
	 * @param	 name	 name of the tag.
	 */
	public void endElement(java.lang.String uri,
            java.lang.String name,
            java.lang.String qName) {
		if(name.equals(((WSLNode)nodeStack.peek()).getName()))
			nodeStack.pop();
	}

	/**
	 * This method is called when characters in between tags are parsed.
	 *
	 * @param	 ch			 array containing parsed characters.
	 * @param	 start	 offset of characters in <code>ch</code>.
	 * @param	 length	 number of characters parsed.
	 */
	public void characters(char ch[], int start, int length) {
		if(((WSLNode)nodeStack.peek()).getName().equals("title"))
			script.setTitle(script.getTitle() + new String(ch, start, length));
	}

	/**
	 * Parses a WSL script from the given <code>InputStream</code>.
	 *
	 * @param	 stream	 the stream containing the script.
	 * @return	a <code>WSLScript</code> representing the parsed script.
	 * @exception InvalidScriptException if the script could not be parsed.
	 */
	public WSLScript parse(InputStream stream) throws InvalidScriptException {
		XMLReader parser;
		try {parser = XMLReaderFactory.createXMLReader();}
		catch(Exception e) {throw new InvalidScriptException(e,"Unable to acquire XML parser");}

		parser.setContentHandler(this);

		nodeStack = new Stack<WSLNode>();
		script = new WSLScript();
		nodeStack.push(script.getRoot());

		try {parser.parse(new InputSource(stream));}
		catch(SAXException e) {
			//To make ourselves useful to people editing scripts,
			//we try examining the exception:
			if(e instanceof SAXParseException) {
				SAXParseException pe=(SAXParseException) e;
				String errorloc="";
				if(pe.getLineNumber()>=0) {
					errorloc=" at line "+pe.getLineNumber();
					if(pe.getColumnNumber()>=0)
						errorloc+=", column "+pe.getColumnNumber();
				}
				throw new InvalidScriptException(e,"Script seems to be damaged"+
										 errorloc+" (the Java Console may have more information)");
			} else throw new InvalidScriptException(e);
		} catch(IOException e) {
			throw new InvalidScriptException(e,"Unable to read script for parsing");
		}

		nodeStack = null;
		//If the script was not actually for this module,
		//no module node will have been set.
		if(script.getModuleNode()==null)
			throw new InvalidScriptException("Script does not appear to match current module");

		return script;
	}
}
