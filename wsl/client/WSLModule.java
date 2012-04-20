/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package webtop.wsl.client;

import webtop.wsl.script.WSLNode;
//import webtop.vrml.NamedNode;

/**
 * The <code>WSLModule</code> interface is implemented in a single class
 * (usually the main applet class) in a WebTOP module.	This class will pass
 * information to <code>WSLPlayer</code> when needed.	 A <code>WSLPanel</code>
 * requires a <code>WSLModule</code> instance with which to instantiate a
 * <code>WSLPlayer</code>.
 *
 * @author Yong Tze Chi
 */
public interface WSLModule {
	/**
	 * This method is called by <code>WSLPlayer</code> whenever it needs current
	 * state of the WebTOP module. This typically occurs at the beginning of a
	 * recording session.
	 *
	 * <p>This method should return the state of the module as an instance of
	 * <code>WSLNode</code>. The instance will eventually become the
	 * initialization tag in the saved script.</p>
	 *
	 * @return an instance of <code>WSLNode</code> representing the current
	 *				 state of the WebTOP module.
	 */
	public WSLNode toWSLNode();

	/**
	 * Returns the WebTOP module's name.	Each module implementing scripting
	 * capabilities should have a unique name.	The name (a <code>String</code>)
	 * is used to name the initialization tag, and also to check against the
	 * initialization tag of a script being loaded to make sure it is intended
	 * for the running WebTOP module.	 This should return a valid XML tag name
	 * (as specified by the <a
	 * href="http://www.w3.org/TR/2000/REC-xml-20001006#sec-common-syn">XML
	 * Spec</a>) -- basically alphanumerics and underscore.
	 *
	 * @return	the name of the WebTOP module as a <code>String</code>.
	 * @see			WSLParser.InvalidScriptException
	 */
	public String getWSLModuleName();

	/**
	 * Returns a <code>NamedNode</code> associated with the module's
	 * navigation panel.	This lets <code>WSLPlayer</code> access the navigation
	 * panel to record and play back viewpoint changes.
	 *
	 * @return reference to a <code>NamedNode</code> for the NavigationPanel in
	 *         the * module's VRML file.
	 */
	//public NamedNode getNavigationPanelNode();
}
