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

import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;
import org.web3d.x3d.sai.*;
import java.util.HashMap;

/*Author: Peter Gilbert, Paul Cleveland
 *Description: Demonstration of an Xj3D bug causing PointSets to be unresponsive
 *to color changes via external SAI.
 */
public class PointSetTest extends JFrame {

    JPanel buttonPanel = new JPanel();
    JButton button = new JButton("Toggle Points");
    MFColor colorField;
    float white[][] = new float[][] {{1,1,1},{1,1,1},{1,1,1}};
    float black[][] = new float[][] {{0,0,0},{0,0,0},{0,0,0}};
    float nextColor[][] = black;
    float currentColor[][] = new float[3][3];

    public PointSetTest() {
        //Set up display of world
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        X3DComponent x3dComp = BrowserFactory.createX3DComponent(null);
        JComponent panel = (JComponent)x3dComp.getImplementation();

        ExternalBrowser browser = x3dComp.getBrowser();
        X3DScene scene = browser.createX3DFromURL(new String[] {"pointsettest.x3dv"});
        browser.replaceWorld(scene);

        //Get the PointSet geometry node
        X3DNode geometryNode = scene.getNamedNode("pointset");

        //Get the color and coordinate nodes
        X3DNode colorNode = ((SFNode)geometryNode.getField("color")).getValue();
        X3DNode coordNode = ((SFNode)geometryNode.getField("coord")).getValue();

        //Get the color and point fields
        //MFColor colorField = (MFColor)colorNode.getField("color");
        colorField = (MFColor)colorNode.getField("color");
        MFVec3f pointField = (MFVec3f)coordNode.getField("point");

        //Create 3 points in a horizontal line
        float points[][] = new float[][] {{0,1,0},{1,1,0},{-1,1,0}};

        //Color the points white (they should no longer be visible against the white Box)

        //Set the color and point fields
        colorField.setValue(white.length, white);
        pointField.setValue(points.length, points);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                    //set IFS color to next color
                colorField.setValue(nextColor.length, nextColor);
                //print value of colorField after setting
                colorField.getValue(currentColor);  //temporary reassignment
                //Note that this print always prints the color that was set the last time the button was clicked,
                //not the value of nextColor at this time.

                //choose next color
                if(nextColor==white) nextColor = black;
                else if(nextColor==black) nextColor = white;
                else throw new IllegalStateException("nextColor not white or black!");
            }
        });
        buttonPanel.add(button);
        getContentPane().setLayout(new GridLayout(2,1));
        getContentPane().add(panel);
        getContentPane().add(buttonPanel);

        this.setSize(500,500);
    }

    public static void main(String[] args) {
        PointSetTest pointSetTest = new PointSetTest();
        pointSetTest.setVisible(true);
    }
}
