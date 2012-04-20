/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.wsl.script;

import java.util.*;

/**
 * This class represents information found in a tag in a WSL script -- the
 * tag's name, its attributes, and its children tags.
 *
 * A <code>WSLNode</code> is generally created for three different occasions:
 * when loading a script, while recording user interactions, and when starting
 * a recording.
 *
 * @author Yong Tze Chi
 * @see webtop.wsl.client.WSLModule#toWSLNode()
 */
public class WSLNode {
	/** Tag attribute name. */
	public static final String VALUE="value",
		TARGET="target",
		PARAMETER="param",
		TIMESTAMP="timeStamp";

	private final String name;
	private final Vector<WSLNode> children=new Vector<WSLNode>();
	private final WSLAttributeList attributes;

	/**
	 * Constructs a <code>WSLNode</code> with the name given.
	 *
	 * @param	 tagName	name of the tag.
	 */
	public WSLNode(String tagName) {this(tagName,new WSLAttributeList());}

	/**
	 * Constructs a <code>WSLNode</code> with the name and attributes given.
	 *
	 * <p><b>Warning!</b> The attribute-list argument to this constructor may be
	 * modified by further access to this <code>WSLNode</code>.
	 *
	 * @param	 tagName	name of the tag.
	 * @param	 atts			attributes associated with the tag.
	 */
	public WSLNode(String tagName, WSLAttributeList atts) {
		if(org.webtop.util.WTString.isNull(tagName))
			throw new IllegalArgumentException("non-null tag name required");
		if(atts==null) throw new NullPointerException("attribute list required");
		name = tagName;
		attributes = atts;
	}

	/**
	 * Returns the name of this node.
	 *
	 * @return	name of the node.
	 */
	public String getName() {return name;}

	/**
	 * Returns the time stamp of this node.
	 *
	 * @return	time stamp in terms of milliseconds, or -1 if no time stamp exists.
	 */
	public long getTimeStamp() {return attributes.getLongValue(TIMESTAMP,-1);}

	/**
	 * Sets the time stamp of this node.  This function may only be called once
	 * on a given <code>WSLNode</code>.
	 *
	 * @param t the time stamp to use
	 */
	public void setTimeStamp(long t)
	{attributes.add(TIMESTAMP,String.valueOf(t));}

	/**
	 * Returns the attribute list associated with this node.
	 *
	 * @return	reference to the attribute list for this object.
	 */
	public WSLAttributeList getAttributes() {return attributes;}

	/**
	 * Returns the <code>WSLNode</code> that has the specified tag name. This
	 * method will recursively search for its child nodes until a match is found.
	 *
	 * @param	 node	 name of the node to look for.
	 * @return a <code>WSLNode</code> instance if found; <code>null</code>
	 *				 otherwise.
	 */
	public WSLNode getNode(String node) {
		if(name.equals(node)) return this;

		if(children == null) return null;

		for(int i=0; i<children.size(); i++) {
			WSLNode n = ((WSLNode) children.elementAt(i)).getNode(node);
			if(n!=null) return n;
		}

		return null;
	}

	/**
	 * Returns the number of immediate children this node has.
	 *
	 * @return	number of immediate children in this node.
	 */
	public int getChildCount() {return children.size();}

	/**
	 * Returns the child <code>WSLNode</code> specified by an index.
	 *
	 * @param	 i	index of the child <code>WSLNode</code>.
	 * @return	reference to the child <code>WSLNode</code>.
	 */
	public WSLNode getChild(int i) {return (WSLNode) children.elementAt(i);}

	/**
	 * Adds a child <code>WSLNode</code> into this <code>WSLNode</code>.
	 *
	 * @param	 node	 the child <code>WSLNode</code> to be added.
	 */
	public void addChild(WSLNode node) {children.addElement(node);}

	/**
	 * Inserts a child <code>WSLNode</code> at the given position.
	 *
	 * @param	 node		the <code>WSLNode</code> object to be inserted.
	 * @param	 index	the position to be inserted at.
	 */
	public void insertChildAt(WSLNode node, int index) {
		children.insertElementAt(node, index);
	}

	/**
	 * Removes the child <code>WSLNode</code> that matches the given reference.
	 *
	 * @param	 node	 reference to the <code>WSLNode</code> to be removed.
	 */
	public void removeChild(WSLNode node) {children.removeElement(node);}

	/**
	 * Removes the child <code>WSLNode</code> at the given position.
	 *
	 * @param	 index	position of the <code>WSLNode</code> to be removed.
	 */
	public void removeChildAt(int index) {children.removeElementAt(index);}

	/**
	 * Removes all child <code>WSLNode</code>s that matches the given name.
	 *
	 * @param	 n	name of the children <code>WSLNode</code> to be removed.
	 */
	public void removeChildByName(String n) {
		for(int i=0; i<children.size(); i++)
			if(getChild(i).name.equals(n)) children.removeElementAt(i);
	}

	/**
	 * Returns the XML representation of this <code>WSLNode</code> as a
	 * <code>String</code> object.
	 *
	 * @return	the XML tag representation of this <code>WSLNode</code>.
	 */
	public String toXMLTag() {
		final StringBuffer buffer = new StringBuffer();
		toXMLTag(buffer,0);
		return buffer.toString();
	}

	/**
	 * Appends the XML representation of this <code>WSLNode</code> to the
	 * <code>StringBuffer</code> object given. This method indents the output to
	 * the <code>level</code> given.
	 *
	 * @param	 buffer	 the <code>StringBuffer</code> to which to write.
	 * @param	 level	 the current level the tag should be at.
	 */
	private void toXMLTag(StringBuffer buffer, int level) {
		indent(buffer,level).append('<').append(name);

		if(attributes!=null && attributes.getLength()>0)
			buffer.append(' ').append(attributes.toString());

		if(children.size()>0) {
			buffer.append(">\n");
			for(int i=0; i<children.size(); i++)
				((WSLNode)children.elementAt(i)).toXMLTag(buffer, level+1);
			indent(buffer,level).append("</").append(name).append(">\n");
		} else buffer.append(" />\n");
	}

	private StringBuffer indent(StringBuffer buffer, int level) {
		for(int i=0; i<level; i++) buffer.append("  ");
		return buffer;
	}
}
