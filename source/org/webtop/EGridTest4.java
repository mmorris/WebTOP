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

/*Author: Matt Hogan
 *Description: A test of an Xj3D bug where ElevationGrid heights are not dynamically set.
 */
public class EGridTest4 extends JFrame implements ActionListener {

    private X3DComponent x3dComp;
    private JComponent panel;
    private JPanel buttonPanel = new JPanel();
    private JButton button = new JButton("Toggle heights"); //button to toggle heights
    MFFloat heightField;                            //height field of geometry
    float heights[];
    int t = 0;
    int i,j;
    double l = 8;
    float h;
    double coeff = (2 * Math.PI) / l;
    double v = 0.6;
    boolean flat = false;
    int resolution = 400;


    public EGridTest4() {
        //Set up display of world
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        x3dComp = BrowserFactory.createX3DComponent(null);
        panel = (JComponent)x3dComp.getImplementation();

        ExternalBrowser browser = x3dComp.getBrowser();
        X3DScene scene = browser.createX3DFromURL(new String[] {"egridtest3.x3dv"});
        browser.replaceWorld(scene);

        //Get the shape's geometry node
        X3DNode geometryNode = scene.getNamedNode("EGrid");

        //Get the node for the heights field
        heightField = (MFFloat)geometryNode.getField("set_height");
        System.out.println("heightField is a " + heightField.getDefinition().getFieldTypeString());

        heights = new float[resolution*resolution];

        //Initialize heights to 0
        for(i = 0; i < heights.length; i++)
        	heights[i] = 0;
        heightField.setValue(heights.length, heights);
        //flat = true;


        //Set up the GUI
        buttonPanel = new JPanel();
        button.addActionListener(this);
        buttonPanel.add(button);
        getContentPane().setLayout(new GridLayout(2,1));
        getContentPane().add(panel);
        getContentPane().add(buttonPanel);


        this.setSize(500,500);
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("################Action performed#####################");
        for (i = 0; i < resolution; i++) {
            h = (float) Math.cos((coeff * (i - v * t)));
            for (j = 0; j < resolution; j++) {
                heights[i + resolution * j] = h;
            }
        }
        heightField.setValue(heights.length, heights);
        t += 1;
        t = t % 1000;
    }

    public static void main(String[] args) {
        EGridTest4 egridtest = new EGridTest4();
        egridtest.setVisible(true);
    }
}
