/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//StringTree.java
//Defines the class StringTree, an AVL tree of properties keyed with strings.
//Davis Herring
//Created August 12 2002
//Updated March 12 2004
//Version 1.02

package org.sdl.util;

/**
 * A StringTree stores a set of values accessible via String keys.
 * Case is preserved but not distinguishing.
 */
public class StringTree
{
	/**
	 * Objects of type Property store one key-value pair in a StringTree.  They
	 * are never invalidated (although they may no longer be part of the tree.)
	 * There are no restrictions on their contents.
	 */
	public static class Property {
		/**
		 * The value associated with this Property.
		 */
		public Object value;
		/**
		 * The key this Property associates with its value.
		 */
		public final String key;
		/**
		 * Creates a new Property with the given key.
		 */
		public Property(String k) {key=k;}
		/**
		 * Returns a String representation of this Property.
		 */
		public String toString() {return getClass().getName()+'['+key+','+value+']';}
	}
	private final static class Node extends Property {
		//Most stuff is public so that StringTree can walk the nodes.
		public final String lkey;
		public Node left,right;

		//Height = max(heightL,heightR)+1; 0 for either of the latter two means 'no tree'
		private int height=1,heightL,heightR;

		/**
		 * Creates a new Node.
		 *
		 * @param k the key to remember (never actually used).
		 * @param kl the key to check for.  Typically this is the lowercase form
		 *           of k.
		 * @param v the value of the node (never used).
		 */
		public Node(String k,String kl,Object v) {super(k);lkey=kl;value=v;}

		/**
		 * Adds or sets the value of a property in this tree (or subtree).  Does rotations
		 * as necessary to preserve balance.
		 *
		 * @param k the 'visible' key to use if property is added.
		 * @param kl the functional key to look for.  Typically the lowercase form
		 *           of k.
		 * @param v the value to set or add.
		 *
		 * @return a reference to the Node that should be considered the 'new
		 *         this'.  References to this Node for structural purposes should
		 *         be changed to the value returned: e.g.,
		 *         'n=n.set(s,s.toLowerCase(),o);'
		 */
		public Node set(String k,String kl,Object v) {
			int c=kl.compareTo(lkey);
			if(c==0) value=v;		//This is it!
			else if(c>0) {
				if(right==null) {
					right=new Node(k,kl,v);
					heightR=1;
					height=2;		//heightL can't be > 1
				}
				else {
					right=right.set(k,kl,v);
					if(heightR<right.height) {		//Right tree has grown
						if(heightR>=heightL) ++height;		//It wasn't shorter, so we've grown too
						if(heightR++>heightL) {	//It was already taller!  Rotate...
							if(right.heightL>right.heightR)	//It's left heavy; double rotation.
								right=right.rotR();
							return rotL();
						}
					}
				}
			}
			else {
				//(Mirror image of above)
				if(left==null) {
					left=new Node(k,kl,v);
					heightL=1;
					height=2;		//heightR can't be > 1
				}
				else {
					left=left.set(k,kl,v);
					if(heightL<left.height) {		//Left tree has grown
						if(heightL>=heightR) ++height;		//It wasn't shorter, so we've grown too
						if(heightL++>heightR) {	//It was already taller!  Rotate...
							if(left.heightR>left.heightL)	//It's right heavy; double rotation.
								left=left.rotL();
							return rotR();
						}
					}
				}
			}
			return this;	//if no rotations got us, we're still ourselves
		}

		/**
		 * Removes a property from the tree.  If the given property does not exist,
		 * does nothing and returns null.
		 *
		 * @param kl the functional key to look for.
		 *
		 * @return a reference to the Node that should be considered the 'new
		 *         this'.  References to this Node for structural purposes should
		 *         be changed to the value returned: e.g.,
		 *         'n=n.remove(s.toLowerCase());'
		 */
		public Node remove(String kl) {
			int c=kl.compareTo(lkey);
			if(c>0) {
				if(right==null) return this;
				right=right.remove(kl);
				//Check for imbalance (just '&' forces '=' to evaluate)
				if(heightL>heightR & heightR>(heightR=((right==null)?0:right.height))) {
					if(left.heightR>left.heightL)		//need to double-rotate
						left=left.rotL();
					return rotR();	//rotation adjusts heights
				}
				if(heightL==heightR)		//Then tree may have shrunk; recalc height
					height=heightR+1;
				return this;
			}
			else if(c<0) {
				//(Mirror image of above)
				if(left==null) return this;
				left=left.remove(kl);
				//Check for imbalance (just '&' forces '=' to evaluate)
				if(heightR>heightL & heightL>(heightL=((left==null)?0:left.height))) {
					if(right.heightL>right.heightR)		//need to double-rotate
						right=right.rotR();
					return rotL();	//rotation adjusts heights
				}
				if(heightR==heightL)		//Then tree may have shrunk; recalc height
					height=heightL+1;
				return this;
			}
			else {		//We're doomed!
				if(left==null) {		//We're either a leaf, then, or have a right (leaf) child
					return right;		//it replaces us
				}
				else if(left.right==null) {	//Then left is a leaf or has a left (leaf) child
					left.right=right;		//They adopt any right-child we have
					if((left.heightR=heightR)>left.heightL) ++left.height;	//They grow as appropriate
					if(left.height==heightR) {		//If that wasn't enough, they're imbalanced
						left.height++;							//and they're taller anyway
						left=left.rotL();
					}
					return left;
				}
				else {		//Then left isn't the minimum.  Go find it and replace ourselves.
					Node temp=left.remove0(new Node(null,null,null));	//find minimum/etc. (see below)
					Node heir=temp.left;
					heir.left=temp.right;	//They adopt the new child
					heir.right=right;			//And any right-child we have
					heir.heightR=heightR;
					heir.height=height;		//They start with our height (checked below)
					//Assign left height and check for problems (if left tree shrunk)
					if(heightL>(heir.heightL=heir.left.height)) {
						if(heightR>heightL) {		//imbalance (heightR was already bigger)
							if(right.heightL>right.heightR)		//Need to double-rotate
								heir.right=right.rotR();		//I THINK that should act like an insert...
							heir=heir.rotL();
						}
						else if(heightR<heightL)	//shrinkage (heightR was smaller -> they match now)
							--heir.height;
						//Other case is heightR was == heightL; now heightL is smaller, but nothing happens
					}
					//Otherwise, tree didn't shrink.  We're done.
					return heir;
				}
			}
		}

		/**
		 * Finds the next Node after the one with the given functional key.
		 *
		 * @param kl the functional key to look for.
		 * @param box a dummy Node in which to store values: the 'left' value will
		 *            be set to the Node found (or null if the key was not found
		 *            or was associated with the last Node under this Node).  The
		 *            'right' value will also be null if the key was not present.
		 * @return <tt>box</tt>
		 */
		public Node next(String kl,Node box) {
			int c=kl.compareTo(lkey);
			if(c>0) {
				if(right!=null) right.next(kl,box);
				else box.left=box.right=null;		//Failure!
			}
			else if(c<0) {
				if(left==null) box.left=box.right=null;		//Failure!
				else {
					left.next(kl,box);
					if(box.left==null && box.right!=null)
						box.left=box.right=this;	//We're it! (they reported kl was last, we're next)
				}
			}
			else {		//We match kl!  Now to find the next one...
				if(right==null) {		//Ack, we're the last in our tree
					box.left=null;
					box.right=this;
				}
				//Ok, there are nodes to our right.  Now find the first one.
				box.left=right;
				while(box.left.left!=null) box.left=box.left.left;
				//And there we are.  Anyone above us will propogate box.left.
			}
			return box;
		}

		/**
		 * Removes the maximum node in the subtree headed by this node.
		 *
		 * @param box a dummy Node in which to store values: the 'left' value will
		 *            be set to the Node that was removed, and the 'right' value
		 *            will be set to the Node (possibly this one) which should be
		 *            considered the 'new this'.  References to this Node for
		 *            structural purposes should be changed to that value: e.g.,
		 *            'n.remove0(n2=new Node("","",null)); n=n2.right;'
		 * @return <tt>box</tt>
		 */
		private Node remove0(Node box) {
			if(right==null) {		//We're the minimum
				box.left=this;
				box.right=left;		//left (if any) child replaces us
				height=heightL;
			}
			else {
				right.remove0(box);
				right=box.right;
				//Check for imbalance (just '&' forces '=' to evaluate)
				if(heightL>heightR & heightR>(heightR=((right==null)?0:right.height))) {
					if(left.heightR>left.heightL)		//need to double rotate
						left=left.rotL();
					box.right=rotR();	//rotation adjusts heights
				}
				else {
					box.right=this;
					if(heightL==heightR)	//If right might have been taller and is no longer
						height=heightR+1;				//(Maybe) drop down a notch
				}
			}
			return box;
		}

		//Hopefully the height code here is right, but I'm not sure.  It might
		//even require different behavior for the first of a double rotation.
		/**
		 * Rotates to the left about this node.  There must be a right subtree.
		 *
		 * @param insert whether this rotation is for an insertion.
		 * @return a reference to the Node that should be considered the 'new
		 *         this'.  References to this Node for structural purposes should
		 *         be changed to the value returned: e.g., 'n=n.rotL();' or
		 *         'return rotL();'
		 */
		private Node rotL() {
			Node oldR=right;
			right=right.left;
			oldR.left=this;
			//Reassign height from taller subtree
			height=(heightR=oldR.heightL)>heightL?heightR+1:heightL+1;
			oldR.heightL=height;
			//We won't be shorter than oldR's other subtree
			oldR.height=height+1;
			return oldR;
		}
		/**
		 * Rotates to the right about this node.  There must be a left subtree.
		 *
		 * @param insert whether this rotation is for an insertion.
		 * @return a reference to the Node that should be considered the 'new
		 *         this'.  References to this Node for structural purposes should
		 *         be changed to the value returned: e.g., 'n=n.rotR();' or
		 *         'return rotR();'
		 */
		private Node rotR() {
			//(Mirror image of above)
			Node oldL=left;
			left=left.right;
			oldL.right=this;
			//Reassign height from taller subtree
			height=(heightL=oldL.heightR)>heightR?heightL+1:heightR+1;
			oldL.heightR=height;
			//We won't be shorter than oldL's other subtree
			oldL.height=height+1;
			return oldL;
		}

		/**
		 * Returns a String describing the structure of the tree at this Node.
		 */
		public String debugString() {
			return '['+key+','+value+";(l="+(left==null?null:left.debugString())+':'+heightL+
				",r="+(right==null?null:right.debugString())+':'+heightR+"):h="+height+"]\n";
		}
	}

	private Node root;

	/**
	 * Creates a new, empty StringTree.
	 */
	public StringTree() {}

	/**
	 * Sets the value of the given property to be the given value.  If no such
	 * property previously existed, it is created.
	 *
	 * @param key the string under which to store the value.  May not be null.
	 * @param value the value to store.
	 */
	public void set(String key,Object value) {
		if(key==null) throw new NullPointerException("Keys may not be null.");
		if(root==null) root=new Node(key,key.toLowerCase(),value);
		else root=root.set(key,key.toLowerCase(),value);
	}

	/**
	 * Returns the property with the given key, or null if no such property
	 * exists.  Use '.value' to retrieve the stored object.
	 *
	 * @param key the string for which to retrieve the value.
	 * @return the property associated with the key, or null if there is no such
	 *         property.
	 */
	public Property get(String key) {
		if(key==null) throw new NullPointerException("Keys may not be null.");
		if(root==null) return null;
		key=key.toLowerCase();
		Node n=root;
		while(true) {
			int c=key.compareTo(n.lkey);
			if(c==0) return n;
			else if(c>0) {
				if(n.right==null) return null;
				else n=n.right;
			}
			else {
				//(Mirror image of above)
				if(n.left==null) return null;
				else n=n.left;
			}
		}
	}

	/**
	 * Removes a property from the tree.
	 *
	 * @param key the string naming the property to remove.
	 */
	public void remove(String key) {
		if(key==null) throw new NullPointerException("Keys may not be null.");
		if(root!=null)
			root=root.remove(key.toLowerCase());
	}

	/**
	 * Removes all the properties from this tree.
	 */
	public void removeAll() {root=null;}

	/**
	 * Returns the next property after (lexicographically) the given key.
	 * Returns null if a property with the given key is not in this StringTree,
	 * or if there are no properties after it.
	 *
	 * @param key the key of the property to look 'after'.
	 * @return the property with the next key.  That next key may be retrieved
	 *         as '.key'.
	 */
	public Property next(String key) {
		if(key==null) throw new NullPointerException("Keys may not be null.");
		if(root==null) return null;
		Node temp=new Node(null,null,null);
		root.next(key.toLowerCase(),temp);
		return temp.left;
	}
}
