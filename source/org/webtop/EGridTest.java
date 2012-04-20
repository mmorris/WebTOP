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

/*Author: Paul Cleveland
 *Description: A test of an Xj3D bug where ElevationGrid heights are not dynamically set.
 */
public class EGridTest extends JFrame implements ActionListener {

    private X3DComponent x3dComp;
    private JComponent panel;
    private JPanel buttonPanel = new JPanel();
    private JButton button = new JButton("Toggle heights"); //button to toggle heights
    MFFloat heightField;                            //height field of geometry
    float flatValues[] = new float[] {0,0,0,0,0,0,0,0,0};
    float vValues[]    = new float[] {0,1,0,0,1,0,0,1,0};
    boolean flat = false;


    public EGridTest() {
        //Set up display of world
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        x3dComp = BrowserFactory.createX3DComponent(null);
        panel = (JComponent)x3dComp.getImplementation();

        ExternalBrowser browser = x3dComp.getBrowser();
        X3DScene scene = browser.createX3DFromURL(new String[] {"egridtest.x3dv"});
        browser.replaceWorld(scene);

        //Get the shape's geometry node
        X3DNode geometryNode = scene.getNamedNode("EGrid");

        //Get the node for the heights field
        heightField = (MFFloat)geometryNode.getField("set_height");
        System.out.println("heightField is a " + heightField.getDefinition().getFieldTypeString());

        //Attempt to set the field to create a 9x9 flat plane
        //heightField.setValue(flatValues.length, flatValues);
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
        if(e.getSource()==button) {
            //heightField.setValue((flat ? vValues.length : flatValues.length),(flat ? vValues : flatValues));
            if(flat) {
                System.out.println("flat");
                heightField.setValue(vValues.length, vValues);
                flat = false;
            }
            else {
                System.out.println("not flat");
                heightField.setValue(flatValues.length, flatValues);
                flat = true;
            }

        }
    }

    public static void main(String[] args) {
        EGridTest egridtest = new EGridTest();
        egridtest.setVisible(true);
    }
}
