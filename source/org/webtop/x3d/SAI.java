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
 * @author Paul Cleveland
 * @version 1.0
 */

//class EAI provides four basic services:
// - acquisition of X3D node/event references (optionally 'patient')
//	* getScene()
//	* getNode("nodename")
//	* getInputField("nodename","eventname")
//	* getOutputField("nodename","eventname",listener(=null for none),data)
//	* getInputField(NamedNode,"eventname")
//	* getOutputField(NamedNode,"eventname")
// - automatic error reporting (with optional error messages and hooks)
//	See below for details of implementing handlers.
// - some generally useful X3D routines:
//	* enableDraw(InputFieldSFBool/InputFieldSFInt32 for Switch)
//	* disableDraw(InputFieldSFBool/InputFieldSFInt32)
//	* setDraw(InputFieldSFBool/InputFieldSFInt32,boolean)
//	* set3(InputFieldSFVec3f/SFColor,x/r,y/g,z/b)
// - the Try class (see below)


public class SAI {
    //This interface should be implemented if a client class wishes to be
    //informed of X3D invalid event-get exceptions.  This is supported as an
    //alternative to exception propagation (if no InvalidEventListener is given,
    //exceptions will be propagated to the caller). event will be null if the
    //connection to the node failed.  [PC edit of Davis]
    //Actually, we may not need this at all.  Not really sure since X3D includes the
    //X3DFieldEventListener and it's only method, readableFieldChanged(), doesn't
    //throw exceptions.  We'll keep the whole thing for now.  [PC]
    public static class Try implements X3DFieldEventListener {
        public final X3DFieldEventListener wrapped;
        public Try(X3DFieldEventListener wrap) {
            wrapped = wrap;
        }

        public void readableFieldChanged(X3DFieldEvent evt) {
            try {
                wrapped.readableFieldChanged(evt);
            }
            catch (RuntimeException e) { //readable...() doesn't actually throw exceptions, but we're just sort of leaving this here for now. [PC]
                System.err.println(
                    "Exception occurred during X3D event handling:");
                e.printStackTrace();
                throw e;
            }
            catch (Error e) {
                System.err.println("ERROR occurred during X3D event handling:");
                e.printStackTrace();
                throw e;
            }
        }
    }



    public interface InvalidEventListener extends java.util.EventListener {
        public void invalidEvent(String node, String event);
    }

    public static final int DEF_TRIES=50,DEF_TRYDELAY=20; // milliseconds
    public static int curTry;

    public X3DScene scene;
    public InvalidEventListener listener;

    public SAI() {
        scene = null;
        listener = null;
    }
    public SAI(X3DScene mainScene) {
        this(mainScene, null);
    }
    public SAI(X3DScene mainScene, InvalidEventListener whocares) {
        scene = mainScene;
        listener = whocares;
    }

    public X3DScene getScene() {
        return scene;
    }

    public NamedNode generateNode(String x3d) {
        return new NamedNode(scene.createNode(x3d));
    }

    public NamedNode generateProto(String x3d) {
        return new NamedNode(scene.createProto(x3d));
    }

    public NamedNode getNode(String nodeName) {
        return new NamedNode(scene.getNamedNode(nodeName), nodeName);
    }

    //Utility functions for getInput/OutputField()
    public X3DField getField(String nodeName, String fieldName) {
        NamedNode nn = getNode(nodeName);
        return nn==null ? null :getField(nn,fieldName);
    }
    public X3DField getField(NamedNode nn, String fieldName) {
        return getField(nn,fieldName,DEF_TRIES,DEF_TRYDELAY);
    }
    public X3DField getField(NamedNode nn, String fieldName, int tries, int delay) {
        try {
            for (curTry = 0; curTry < tries; curTry++) {
                if (nn.node.isRealized())
                    return nn.node.getField(fieldName);
                if (curTry < tries - 1)
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {}
            }
            return null;
        }
        finally {
            if(curTry>0) System.out.println("SAI: "+nn+'.'+fieldName+" ("+curTry+')');
        }
    }

    /**
     * Returns an input field <code>fieldName</code> from node <code>nodeName</code>.
     * @param nodeName String containing the name of the node containing the field.
     * @param fieldName String containing the name of the field.
     * @return X3DField corresponding to the field.
     */
    public X3DField getInputField(String nodeName, String fieldName) {
        X3DField field = getField(nodeName, fieldName);
        //Check if field is writable
        //if(field!=null && field.isWritable())
            return field;
        //else
        //    return null;
    }

    /**
     * Returns an input field <code>fieldname</code> from <code>NamedNode nn</code>.
     * @param nn NamedNode referring to the node containing the field.
     * @param fieldName String containing the name of the field.
     * @return X3DField corresponding to the field.
     */
    public X3DField getInputField(NamedNode nn, String fieldName) {
        X3DField field = getField(nn, fieldName);
        //Check if field is writable
        //if(field != null && field.isWritable())
            return field;
        //else
        //    return null;
    }

    /**
     * Returns an output field <code>fieldName</code> from node <code>nodeName</code>.
     * @param nodeName String containing the name of the node containing the field.
     * @param fieldName String containing the name of the field.
     * @return X3DField corresponding to the field
     */
    public X3DField getOutputField(String nodeName, String fieldName, X3DFieldEventListener fieldListener, Object data) {
        X3DField field = getField(nodeName, fieldName);
        //if(field!=null && field.isReadable()) {
            field.addX3DEventListener(fieldListener);
            field.setUserData(data);
            return field;
        //}SAI

        //else
        //    return null;
    }

    /**
     * Returns an output field <code>fieldName</code> from <code>NamedNode nn</code>.
     * @param nn NamedNode referring to the node containing the field.
     * @param fieldName String containing the name of the field.
     * @return X3DField corresponding to the field
     */
    public X3DField getOutputField(NamedNode nn, String fieldName, X3DFieldEventListener fieldListener, Object data) {
        X3DField field = getField(nn, fieldName);
        int tries=0;
        while(field == null && tries++<50) {
  
        	//Currently, it appears that there is no way to guarantee that
        	//all extern protos are loaded.  Here, we try for a while before
        	//failing.
        	System.out.println("Unable to get node, trying again...");
        	try{
        		Thread.sleep(100);
        	}
        	catch(Exception e) {}
        	field = getField(nn, fieldName);
        }
        //if(field.isReadable()) {
            field.addX3DEventListener(fieldListener);
            field.setUserData(data);
            return field;
        //}
        //else
        //    return null;
    }


    //Other routines (Provided in webtop.vrml.EAI, so included here.  [PC])


    public static void enableDraw(X3DField target) {
        if (target instanceof SFBool)
            ( (SFBool) target).setValue(true);
        else ( (SFInt32) target).setValue(0);
    }

    public static void disableDraw(X3DField target) {
        if (target instanceof SFBool)
            ( (SFBool) target).setValue(false);
        else ( (SFInt32) target).setValue( -1);
    }

    public static void setDraw(X3DField target, boolean on) {
        if (on) enableDraw(target);
        else disableDraw(target);
    }

    public static void set3(SFVec3f target, float x, float y, float z) {
        target.setValue(new float[] {x, y, z});
    }

    public static void set3(SFColor target, float r, float g, float b) {
        target.setValue(new float[] {r, g, b});
    }


    /**
     * Takes the an X3DField <code>target</code> referencing either a MFVec2f, MFVec3f, or MFColor, casts it appropriately, and sets it.  Throws an IllegalArgument exception if not one of these.
     * @param target X3DField to be set
     * @param vectors float[][] containing the values
     */
    //Note:  Make a setSingleVectors() [PC]
    public static void setMultipleVectors(X3DField target, float[][] vectors) {
        if(target instanceof MFVec2f)
            ((MFVec2f)target).setValue(vectors.length, vectors);
        else if(target instanceof MFVec3f)
            ((MFVec3f)target).setValue(vectors.length, vectors);
        else if(target instanceof MFColor)
            ((MFColor)target).setValue(vectors.length, vectors);
        else throw new IllegalArgumentException("SAI.setVectors()::invalid target type of " + target.getDefinition().getFieldTypeString());
    }

    /**
     * Takes an X3DField <code>target</code> referencing a MFFloat, casts it, and sets it.
     * @param target X3DField to be set
     * @param floats float[] containing the values
     */
    public static void setMultipleFloats(X3DField target, float[] floats) {
        if(target instanceof MFFloat)
            ((MFFloat)target).setValue(floats.length, floats);
    }

    /**
     * Returns the X3DField for the geometric property <code>property</code> of the geometry node contained in <code>geometry</code> cast as it's appropriate type:<br/>
     * <ul><li>MFColor if <code>property</code> = &quot;color&quot;.</li>
     * <li>MFVec3f if <code>property</code> = &quot;coord&quot; or &quot;normal&quot;.</li>
     * <li>MFVec2f if <code>property</code> = &quot;texCoord&quot;.</li></ul>
     * @param geometry NamedNode containing the geometry X3DNode
     * @param property String containing the property name
     * @return X3DField of the property cast as it's appropriate type.
     */
    public static X3DField getGeometricProperty(NamedNode geometry, String property) {
        if(property=="color") {
            /*System.out.println("getGeoProp():  node property " + property + " type is " + (nn.node.getField(property).getDefinition().getFieldTypeString()));
            if(nn.node.getField(property) instanceof X3DField)
                System.out.println("field " + property + " is instanceof X3DField");
            return nn.node.getField(property);
            */
            return ((MFColor)((SFNode)geometry.node.getField(property)).getValue().getField("color"));
        }
        else if(property=="coord") {
            return ((MFVec3f)((SFNode)geometry.node.getField(property)).getValue().getField("point"));
        }
        else if(property=="normal") {
            return ((MFVec3f)((SFNode)geometry.node.getField(property)).getValue().getField("vector"));
        }
        else if(property=="texCoord") {
            return ((MFVec2f)((SFNode)geometry.node.getField(property)).getValue().getField("point"));
        }
        else
            throw new IllegalArgumentException("Invalid Property: " + property);
    }
}
