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

import javax.swing.*;
import org.web3d.x3d.sai.*;

/*Author: Matt Hogan
Test of the ability to dynamically update Elevation Grids at varying speeds
 */
public class EGridTest3 extends JFrame implements X3DFieldEventListener{

	//serialID serves no purpose, just put it in to shut up Eclipse
	private static final long serialVersionUID = 1L;
	private X3DComponent x3dComp;
    private JComponent panel;
    MFFloat heightField;                            //height field of geometry
    float height[] = new float[400*400];
    SFFloat timeFraction;
    //time is the sleep time before each frame of animation in milliseconds
    Long time = new Long(12);
    //time the previous frame executed
    Long oldTime;
    //Various constants used by the wave function
    double l = 8;
    double coeff = (2 * Math.PI) / l;
    int i, j;
    int t = 0;
    float h;
    double v = 0.6;


    public EGridTest3() {
        //Set up display of world
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        x3dComp = BrowserFactory.createX3DComponent(null);
        panel = (JComponent)x3dComp.getImplementation();

        ExternalBrowser browser = x3dComp.getBrowser();
        X3DScene scene = browser.createX3DFromURL(new String[] {"EGridTest3.x3dv"});
        browser.replaceWorld(scene);

        //Get the shape's geometry node
        X3DNode geometryNode = scene.getNamedNode("EGrid");
        //Get a TimeSensor node and its fraction_changed field
        X3DNode timeNode = scene.getNamedNode("sensor");
        timeFraction = (SFFloat)timeNode.getField("fraction_changed");       

        //Get the node for the heights field
        heightField = (MFFloat)geometryNode.getField("set_height");

        //Initialize oldTime and add the fraction_changed event listener
        oldTime = new Long(System.currentTimeMillis());
        timeFraction.addX3DEventListener(this);
        
        for(int k = 0; k < height.length; k++)
            height[k] = 0;
        System.out.println("height[10] = " + height[10]);
        //Attempt to set the field to create a 9x9 flat plane
        heightField.setValue(height.length, height);


        //Set up the GUI

        getContentPane().add(panel);


        this.setSize(500,500);

    }

    public static void main(String[] args) {
        EGridTest3 egridtest = new EGridTest3();
        egridtest.setVisible(true);
    }

	public void readableFieldChanged(X3DFieldEvent arg0) {
        Long newTime, dTime;
        //System.out.println("Event Handler Called.");
        newTime = System.currentTimeMillis();
        dTime = newTime - oldTime;
        t += dTime*0.1;
        oldTime = newTime;
    	for (i = 0; i < 400; i++) {
            h = (float) Math.cos((coeff * (i - v * t)));
            for (j = 0; j < 400; j++) {
                height[i + 400 * j] = h;
            }
        }
        try{
            heightField.setValue(height.length, height);
        }catch(org.web3d.vrml.lang.InvalidFieldException e)
        {
        e.printStackTrace();
        }
		
	}
}
