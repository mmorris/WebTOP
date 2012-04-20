/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.web3d.x3d.sai.*;

import org.webtop.component.*;
import org.webtop.wsl.script.WSLNode;
import org.webtop.x3d.*;
import java.awt.event.ActionEvent;
import org.sdl.gui.numberbox.FloatBox;
import org.webtop.x3d.widget.*;
import org.sdl.math.*;

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
public class WApplicationTest extends WApplication implements X3DFieldEventListener, ActionListener {
    //Control Variables
    SFFloat floatField1;
    SFFloat floatField2;
    DynamicX3D dynx3d;

    //IFS Test
    NamedNode nn;

    //GUI
    JTextField field;
    JButton button;

    //Widget test
/*    FloatBox xDraggerBox,wheelBox,xDraggerBox2;
    XDragWidget xDragger,xDragger2;
    WheelWidget wheelWidget;
    ScalarCoupler scalarCoupler,wheelCoupler,scalarCoupler2;
*/


    public WApplicationTest() {
        super();
    }

    public WApplicationTest(String title, String world) {
        super(title, world);
    }

    /**
     * Implemented by extending module to give the name of it's author(s).
     *
     * @return String containg the author's name.
     * @todo Implement this x3dwebtop.component.WApplication method
     */
    protected String getAuthor() {
        return "";
    }

    /**
     * Implemented by extending module to give its last modified date.
     *
     * @return String containing the date.
     * @todo Implement this x3dwebtop.component.WApplication method
     */
    protected String getDate() {
        return "";
    }

    /**
     * Implemented by extending class to give the Component that has the initial focus.
     *
     * @return Component with initial focus.
     * @todo Implement this x3dwebtop.component.WApplication method
     */
    protected Component getFirstFocus() {
        return null;
    }

    /**
     * Implemented by extending module to give its major version number.
     *
     * @return int of the Major Version
     * @todo Implement this x3dwebtop.component.WApplication method
     */
    protected int getMajorVersion() {
        return 0;
    }

    /**
     * Implemented by extending module to give its minor version number.
     *
     * @return int of the Minor Version
     * @todo Implement this x3dwebtop.component.WApplication method
     */
    protected int getMinorVersion() {
        return 0;
    }

    /**
     * Implemented by extending module to give its name.
     *
     * @return String holding the module's name
     * @todo Implement this x3dwebtop.component.WApplication method
     */
    protected String getModuleName() {
        return "";
    }

    /**
     * Implemented by extending module to give its revision number.
     *
     * @return int of the Revision Number
     * @todo Implement this x3dwebtop.component.WApplication method
     */
    protected int getRevision() {
        return 0;
    }

    /**
     * invalidEvent
     *
     * @param node String
     * @param event String
     * @todo Implement this x3dwebtop.x3dlib.SAI.InvalidEventListener method
     */
    public void invalidEvent(String node, String event) {
    }

    /**
     * This should reset the module (and will be used to set it up in start()) There may be subtleties here...
     *
     * @todo Implement this x3dwebtop.component.WApplication method
     */

    public void readableFieldChanged(X3DFieldEvent e) {
        //if(e.getSource()==voltageOut)
        SFFloat val = (SFFloat) e.getSource();
            System.out.println("change = " + val.getValue());
    }
    protected void setDefaults() {
    }

    /**
     * Called during construction, after setupX3D(); should establish user interface objects.
     *
     * @todo Implement this x3dwebtop.component.WApplication method
     */
    protected void setupGUI() {
        button = new JButton("Click");
        button.addActionListener(this);
        controlPanel.add(button);

        /*voltageField = new JTextField("0.000", 5);
        voltageField.addActionListener(this);
        controlPanel.add(voltageField);
        */
       /*
       xDraggerBox = new FloatBox(0, 1, 0, 5);
       xDraggerBox2 = new FloatBox(0, 1, 0, 5);
       wheelBox = new FloatBox(400, 700, 400, 5);
       xDraggerBox.setForeground(FOREGROUND);
       xDraggerBox2.setForeground(FOREGROUND);
       wheelBox.setForeground(FOREGROUND);
       controlPanel.add(xDraggerBox);
       controlPanel.add(xDraggerBox2);
       controlPanel.add(wheelBox);
       scalarCoupler = new ScalarCoupler(xDragger, xDraggerBox, 3);
       scalarCoupler = new ScalarCoupler(xDragger2, xDraggerBox2, 3);
       wheelCoupler = new ScalarCoupler(wheelWidget, wheelBox, 3);
        */

       //IFS Testing

    }

    /**
     * Called during construction after initX3D() and initGUI(); should use the SAI object (obtained with getSAI()) to connect to X3D objects.
     *
     * @todo Implement this x3dwebtop.component.WApplication method
     */
    protected void setupX3D() {
        /*voltageIn = (SFFloat) getSAI().getField("Worker", "voltageIn");
        if(voltageIn==null)
            System.out.println("failed to get voltageIn");
        //voltageIn.addX3DEventListener(this);
        voltageOut = (SFFloat) getSAI().getField("Worker", "voltageOut");
        if(voltageOut==null)
            System.out.println("failed to get voltageOut");
        voltageOut.addX3DEventListener(this);
        */
       /*
       xDragger = new XDragWidget(getSAI(), getSAI().getNode("Dragger"), (short) 1, "dragHelp");
       xDragger2 = new XDragWidget(getSAI(), getSAI().getNode("Dragger2"), (short) 2, "dragHelp");
       wheelWidget = new WheelWidget(getSAI(), getSAI().getNode("Wheel"), (short) 3, "dragHelp");
       */


      //Globals for use in color examples
      float red4[][] = new float[][] {{1,0,0},{1,0,0},{1,0,0},{1,0,0}};
      float green4[][] = new float[][] {{0,1,0},{0,1,0},{0,1,0},{0,1,0}};
      float blue4[][] = new float[][] {{0,0,1},{0,0,1},{0,0,1},{0,0,1}};
      /*************Example of setting the color property of an IFS when the X3D has a null value*************/
      /***************************DO NOT DELETE!!!!!!!!!!!!!! [PC]********************************************
        //Set up IFS test
        nn = getSAI().getNode("IFS");
        //Create a dynamic Color node
        X3DNode colorNode = getSAI().getScene().createNode("Color");         //create a dynamic node
        if(colorNode.getField("color") instanceof MFColor)
            System.out.println("colorNode.getField(\"color\") is instanceof MFColor");  //This line does print!  [PC]
        else
            System.out.println(colorNode.getField("color").getDefinition().getFieldTypeString());

        MFColor mfColor = (MFColor) colorNode.getField("color");             //access the MFColor of that node
        mfColor.setValue(green4.length, green4);//set the vale of the MFColor

        //Access IFS's color field and set it to colorNode
        SFNode ifsColor = (SFNode)nn.node.getField("color");
        ifsColor.setValue(colorNode);

      /**********************************End example of setting null color field of IFS************************/
      /*********Attempt to use SAI and WApplication classes to set originally empty color of IFS
       //get the IFS
       NamedNode node = getSAI().getNode("IFS");

       //does IFS's color field return null?  it shouldn't.  it's there, it's just empty
       SFNode ifsColor = (SFNode)node.node.getField("color");
       if(node==null)
           System.out.println("IFS's color field is null");
       else
           System.out.println("IFS's color field returned a " + ifsColor.getDefinition().getFieldTypeString());  //printed "SFNode"

       //we know that the IFS's color field returned an SFNode, so now we have to get a reference to that node
       MFColor colorColor = (MFColor)ifsColor.getValue().getField("color");
       if(colorColor==null)
           System.out.println("color's color field is null");
       else
           System.out.println("color's color field returned a " + colorColor.getDefinition().getFieldTypeString());  //"MFColor"

       //we know that the IFS's color's color field returned a MFColor, so now we can set it.
       colorColor.setValue(green4.length, green4);

       X3DField directColor = getSAI().getGeometricPropery(node, "color");
       System.out.println("directColor's type is " + directColor.getDefinition().getFieldTypeString());
       boolean mf = false;
       if(directColor instanceof MField) {
           mf = true;
       }
       //if not something like MF<FieldName>, it inherits from X3DField directly
       else {
           mf = true;
       }

       if(!mf) {
           System.out.println("directColor not instanceof MField; field type is " + directColor.getDefinition().getFieldTypeString());
       }
       else {
           System.out.println("direcColor is an MField; field type is " + directColor.getDefinition().getFieldTypeString());
           System.out.println("setting color from green to blue");

       }

       /*****End attempt*****/

       NamedNode ifs = getSAI().getNode("IFS");
       X3DField field = ifs.node.getField("color");
       if(field==null)
           System.out.println("field null");
       else
           System.out.println("field not null; field is a " + field.getDefinition().getFieldTypeString());

        /*********Code to get/set color of IFS**********************
        float colors[][] = {{0,1,0},{0,1,0},{0,1,0},{0,1,0}};
        ((MFColor)((SFNode)nn.node.getField("color")).getValue().getField("color")).setValue(colors.length, colors);
        *************************************************************/

    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==field) {
            JTextField source = (JTextField) e.getSource();
            float val = Float.parseFloat(source.getText());
            floatField1.setValue(val);
        }
        else if(e.getSource()==button) {
            System.out.println("button pushed");
            /*dynx3d = new DynamicX3D(getSAI(), getSAI().getNode("VoltageMeter"), "Shape");
            SFNode dynx3dGeo = (SFNode) dynx3d.getNode().node.getField("geometry");
            NamedNode sphereGeo = getSAI().generateNode("Sphere");
            dynx3dGeo.setValue(sphereGeo.node);
            */

            /*IFS Test*/
            //Now test the X3DComposed Geometry method
        }
        else
            System.out.println("unknown action source");
    }


    public static void main(String[] args) {
        WApplicationTest wapplicationtest = new WApplicationTest("WApplication Test", "ifstest_nocolor.x3dv");
    }

    /*protected void toWSLNode(any node) {
    }*/

    protected void setupMenubar() {
    }

    public void mouseEntered(Widget src) {
    }

    public void mouseExited(Widget src) {
    }

    public void mousePressed(Widget src) {
    }

    public void mouseReleased(Widget src) {
    }

    public void toolTip(Tooltip src, String tip) {
    }

	@Override
	protected void toWSLNode(WSLNode node) {
		// TODO Auto-generated method stub
		
	}

	public String getWSLModuleName() {
		// TODO Auto-generated method stub
		return null;
	}
    
    
}
