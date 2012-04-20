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

/**
 * A <code>WSLScript</code> represents an entire WSL script in memory.  It
 * maintains the script as a hierarchy of <code>WSLNode</code>s under itself,
 * and maintains references to the initialization <code>WSLNode</code> and
 * script <code>WSLNode</code>.  <code>WSLScript</code>s also have a title.
 *
 * @see WSLNode
 * @author Yong Tze Chi
 */
public class WSLScript extends WSLNode {
	/** Tag name. */
	public static final String ROOT_TAG="wsl",
														 SCRIPT_TAG="script",
														 SCRIPT_END_TAG="scriptEnded";

	private String title;

	private WSLNode module,script;

	/**
	 * Constructs an empty <code>WSLScript</code>.
	 */
	public WSLScript() {super(ROOT_TAG);}

	/**
	 * Returns the root <code>WSLNode</code>.
	 *
	 * @return	reference to the root <code>WSLNode</code>.
	 * @deprecated this object <em>is</em> the root node
	 */
	public WSLNode getRoot() {return this;}

	/**
	 * Sets the initialization <code>WSLNode</code>.
	 *
	 * @param	 m	the initialization <code>WSLNode</code>.
	 */
	public void addModuleNode(WSLNode m) {insertChildAt(module=m, 0);}

	/**
	 * Sets the script <code>WSLNode</code>.
	 *
	 * @param	 s	the script <code>WSLNode</code>.
	 */
	public void addScriptNode(WSLNode s) {addChild(script=s);}

	/**
	 * Gets the initialization <code>WSLNode</code>.
	 *
	 * @return	reference to the initialization <code>WSLNode</code>.
	 */
	public WSLNode getModuleNode() {return module;}

	/**
	 * Gets the script <code>WSLNode</code>.
	 *
	 * @return	reference to the script <code>WSLNode</code>.
	 */
	public WSLNode getScriptNode() {return script;}

	/**
	 * Sets the title of this WSL script.
	 *
	 * @param	 t	new title of this WSL script.
	 */
	public void setTitle(String t) {title=t;}

	/**
	 * Gets the title of this WSL script.
	 *
	 * @return	title of this WSL script as a <code>String</code>.
	 */
	public String getTitle() {return title;}

	/**
	 * Checks if this <code>WSLScript</code> contains any script action that can
	 * be played back.
	 *
	 * @return	<code>true</code> if the <code>WSLScript</code> has script
	 *					actions recorded; <code>false</code> otherwise.
	 */
	public boolean isPlayable() {
		return (script!=null && script.getChildCount()>0);
	}
}
