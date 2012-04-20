/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.x3d;

import org.web3d.x3d.sai.*;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public abstract class X3DObject {
    private static final String ADDCHILDREN="addChildren",REMOVECHILDREN="removeChildren";

    //It seems that Blank is unnecessary in the X3D version of this class, so it is left out for now.
    public static final class Blank extends X3DObject {
            private String name,names[];

            //public Blank(SAI sai,String targetNode) {super(sai,targetNode);}
            public Blank(SAI sai,NamedNode targetNode) {super(sai,targetNode);}
            //public Blank(SAI sai,String targetNode,String addEvent,String removeEvent) {super(sai,targetNode,addEvent,removeEvent);}
            public Blank(SAI sai,NamedNode targetNode,String addEvent,String removeEvent) {super(sai,targetNode,addEvent,removeEvent);}
            public Blank(SAI sai,NodeInputField addEvent,NodeInputField removeEvent)
            {super(sai,addEvent,removeEvent);}

            public void create(String x3d,String n) {name=n; super.createNode(x3d);}
            public void create(String x3d,String[] ns) {names=ns; super.createNode(x3d);}

            protected NamedNode[] name(X3DNode[] raw)
            {return NamedNode.namedArray(raw,name,names);}

            public NamedNode[] getNodes() {return super.getNodes();}
            public NamedNode getNode() {return super.getNode();}

            //When you want the node, not the X3DObject:
            public NamedNode plant(String x3d,String name) {
                    create(x3d,name);
                    this.place();							// compiler is confused about place()...
                    return getNode();
            }
    }

    protected final SAI sai;
    private final NodeInputField add,remove;

    private NamedNode nodes[],node;
    //This is new as of version 1.0: it seems like a valid state rule to
    //enforce, and may catch wasteful things like repeated removals [Davis]
    private boolean placed;

    //For controlling a normal node field, use null for removeEvent and
    //remove() will blank out the field instead of trying to "remove" anything.

    //public X3DObject(SAI sai,String targetNode) {this(sai,sai.getNode(targetNode));}

    public X3DObject(SAI sai,NamedNode targetNode) {this(sai,targetNode,ADDCHILDREN,REMOVECHILDREN);}
    //public X3DObject(SAI sai,String targetNode,String addEvent,String removeEvent)
    //{this(sai,sai.getNode(targetNode),addEvent,removeEvent);}

    public X3DObject(SAI sai,NamedNode targetNode,String addEvent,String removeEvent) {
            this(sai,new NodeInputField(sai.getField(targetNode,checkAdd(addEvent))),
                             removeEvent==null?null:new NodeInputField(sai.getField(targetNode,removeEvent)));
    }

    public X3DObject(SAI sai,NodeInputField a,NodeInputField r) {
            this.sai=sai;
            add=a;
            remove=r;
    }

    private static String checkAdd(String add) {
            if(org.webtop.util.WTString.isNull(add))
                    throw new IllegalArgumentException("add event required");
            return add;
    }

    /*protected void create(String vrml) {
            release();		//so that variables will be null if there are exceptions
            placed=false;		//new nodes haven't been, obviously
            final X3DNode[] cvfs=sai.world.createVrmlFromString(vrml);
            final int n=cvfs.length;

            if(n>1 && add.single())
                    throw new IllegalArgumentException("More than one node generated for a single-node X3DObject");

            if(n>0) {
                    nodes=name(cvfs);
                    node=nodes[0];
            }
    }*/

    //These replace create() since Xj3D requires different methods for normal nodes and prototype instances
    protected void createNodes(String[] x3d) {}  //unimplemented because it is unlikely a module would need to send multiple nodes at once.
    protected void createNode(String x3d) {
        release();
        placed = false;
        node = new NamedNode(sai.getScene().createNode(x3d));
        nodes = new NamedNode[1];
        nodes[0] = node;
    }
    protected void createProto(String protoName) {
        release();
        placed = false;
        node = new NamedNode(sai.getScene().createProto(protoName));
        nodes = new NamedNode[1];
        nodes[0] = node;
    }

    //Required Xj3D function not actually implemented as of 5/15 -MH
    protected void createExternProto(String protoName)
    {
    	release();
    	placed = false;
    	node = new NamedNode(sai.getScene().getExternProtoDeclaration(protoName).createInstance());
    	nodes = new NamedNode[1];
    	nodes[0] = node;
    }
    protected NamedNode[] name(X3DNode[] raw)
    {return NamedNode.namedArray(raw,getNodeName());}

    //Unless a subclass overrides name(), it'll probably want to override this.
    protected String getNodeName() {
            throw new RuntimeException("No name given by a X3DObject subclass");
            //return "<unnamed X3DObject>";
    }

    protected NamedNode[] getNodes() {return nodes==null?null:(NamedNode[])nodes.clone();}
    public NamedNode getNode() {return node;}

    public void place() {
            if(!exists()) throw new IllegalStateException("No nodes to place");
            if(placed()) throw new IllegalStateException("Already in place");

            if(add.single()) add.set(node);
            else if(add.multi()) add.set(nodes);
            else throw new IllegalStateException("No event available for placement");

            placed=true;
    }

    public void remove() {
            if(!placed()) throw new IllegalStateException("Not in place");

            if(remove.single()) remove.set(node);
            else if(remove.multi()) remove.set(nodes);
            //This isn't symmetric, but it's what you want: to remove when there is no
            //separate remove event, send null (or a zero-length array) to the add
            //event.
            else if(add.single()) add.set((NamedNode)null);
            else add.set(new NamedNode[0]);

            placed=false;
    }

    public void release() {
            nodes=null;
            node=null;
    }

    public void destroy() {
            if(exists()) {
                    if(placed()) remove(); 
                    release();
                    
            }
    }

    public boolean exists() {return node!=null;}
    public boolean placed() {return placed;}

    //This is akin to the getSubNode() operation conspicuously missing from the
    //SAI (as in, the real thing, not webtop.vrml.SAI).  Note, however, that
    //this is making a new subnode, not just grabbing one.  So it's really more
    //of a setSubNode().
    public static NamedNode plant(SAI sai,NamedNode parent,String event,String x3d,String name) {
            return plant(sai,new NodeInputField(sai.getField(parent,event)),x3d,name);
    }
    public static NamedNode plant(SAI sai,NodeInputField event,String x3d,String name) {
            return new Blank(sai,event,null).plant(x3d,name);
        //return null;
        }
}
