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

import java.util.HashMap;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.web3d.x3d.sai.*;
import java.awt.event.ActionEvent;

/*Author: Paul Cleveland
 *Description: A test of an Xj3D bug where IndexedFaceSet colors are not dynamically set.
 *Uses a JButton to cycle the IFS color through red, green, and blue.
 */
public class IFSTest extends JFrame implements ActionListener{

    private X3DComponent x3dComp;
    private JComponent panel;
    private JPanel buttonPanel = new JPanel();
    private JButton button = new JButton("Next Color");  //color toggle button
    MFColor colorField;  //"color" field of geometry's underlying X3DColor node
    float red[][] = new float[][] {{1,0,0},{1,0,0},{1,0,0},{1,0,0}};
    float green[][] = new float[][] {{0,1,0},{0,1,0},{0,1,0},{0,1,0}};
    float blue[][] = new float[][] {{0,0,1},{0,0,1},{0,0,1},{0,0,1}};
    float nextColor[][] = red;
    float currentColor[][] = new float[4][3];

    public IFSTest() {
        //Set up display of world
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        x3dComp = BrowserFactory.createX3DComponent(null);
        panel = (JComponent)x3dComp.getImplementation();

        ExternalBrowser browser = x3dComp.getBrowser();
        X3DScene scene = browser.createX3DFromURL(new String[] {"ifstest.x3dv"});
        browser.replaceWorld(scene);

        /*Now we connect to the IndexedFaceSet node and try to change
          the colors of the underlying X3DColor node.*/

        //Get the tent geometry node
        X3DNode geometryNode = scene.getNamedNode("IFS");

        //Get the node for the "color" field of the geometry
        SFNode colorNode = (SFNode)geometryNode.getField("color");

        //Get the "color" field of the Color node
        colorField = (MFColor)colorNode.getValue().getField("color");

        //Initially the color should be the same as the underlying appearance (white)

        //Set up the GUI
        button.addActionListener(this);
        buttonPanel.add(button);
        getContentPane().setLayout(new GridLayout(2,1));
        getContentPane().add(panel);
        getContentPane().add(buttonPanel);

        this.setSize(500,500);
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==button) {
            //set IFS color to next color
            colorField.setValue(nextColor.length, nextColor);
            //print value of colorField after setting
            colorField.getValue(currentColor);  //temporary reassignment
            //Note that this print always prints the color that was set the last time the button was clicked,
            //not the value of nextColor at this time.
            System.out.println("colorField value is " +
                               "{" + currentColor[0][0] + " " + currentColor[0][1] + " " + currentColor[0][2] + "} " +
                               "{" + currentColor[1][0] + " " + currentColor[1][1] + " " + currentColor[1][2] + "} " +
                               "{" + currentColor[2][0] + " " + currentColor[2][1] + " " + currentColor[2][2] + "} " +
                               "{" + currentColor[3][0] + " " + currentColor[3][1] + " " + currentColor[3][2] + "}");


            //choose next color
            if(nextColor==red) nextColor = green;
            else if(nextColor==green) nextColor = blue;
            else if(nextColor==blue) nextColor = red;
            else throw new IllegalStateException("nextColor not R,G, or B!");

            System.out.println("nextColor is " +
                               "{" + nextColor[0][0] + " " + nextColor[0][1] + " " + nextColor[0][2] + "} " +
                               "{" + nextColor[1][0] + " " + nextColor[1][1] + " " + nextColor[1][2] + "} " +
                               "{" + nextColor[2][0] + " " + nextColor[2][1] + " " + nextColor[2][2] + "} " +
                               "{" + nextColor[3][0] + " " + nextColor[3][1] + " " + nextColor[3][2] + "}\n");

        }
    }

    public static void main(String[] args) {
        IFSTest ifstest = new IFSTest();
        ifstest.setVisible(true);
    }
}
