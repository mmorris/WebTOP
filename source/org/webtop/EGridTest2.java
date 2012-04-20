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

import java.awt.event.*;
import javax.swing.*;
import org.web3d.x3d.sai.*;


/*Author: Matt Hogan
Test of the ability to dynamically update Elevation Grids at varying speeds
 */
public class EGridTest2 extends JFrame {

    private X3DComponent x3dComp;
    private JComponent panel;
    MFFloat heightField;                            //height field of geometry
    float height[] = new float[50*50];
    //time is the sleep time before each frame of animation in milliseconds
    Long time = new Long(12);


    public EGridTest2() {
        //Set up display of world
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        x3dComp = BrowserFactory.createX3DComponent(null);
        panel = (JComponent)x3dComp.getImplementation();

        ExternalBrowser browser = x3dComp.getBrowser();
        X3DScene scene = browser.createX3DFromURL(new String[] {"EGridTest2.x3dv"});
        browser.replaceWorld(scene);

        //Get the shape's geometry node
        X3DNode geometryNode = scene.getNamedNode("EGrid");

        //Get the node for the heights field
        heightField = (MFFloat)geometryNode.getField("set_height");
        System.out.println("heightField is a " + heightField.getDefinition().getFieldTypeString());
        for(int i = 0; i < height.length; i++)
            height[i] = 0;
        System.out.println("height[10] = " + height[10]);
        //Attempt to set the field to create a 9x9 flat plane
        heightField.setValue(height.length, height);


        //Set up the GUI

        getContentPane().add(panel);


        this.setSize(500,500);

    }



    public void actionPerformed(ActionEvent e) {
        System.out.println("################Action performed#####################");
    }

    public void startAnimation() {
        GridThread thread = new GridThread(heightField);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ex) {
        }
        System.out.println("Thread terminated; this should not happen.");
    }
    public static void main(String[] args) {
        EGridTest2 egridtest = new EGridTest2();
        egridtest.setVisible(true);
        egridtest.startAnimation();
    }

    private class GridThread extends Thread{
        MFFloat heightField;
        float height[] = new float[50*50];

        GridThread(MFFloat field)
        {
            heightField = field;
            System.out.println("Thread Spawned");
        }

        public void run()
        {
            Long oldTime, newTime, dTime;
            double l = 8;
            double coeff = (2 * Math.PI) / l;
            int i, j;
            int t = 0;
            float h;
            double v = 0.6;
            while (true) {
                oldTime = System.currentTimeMillis();
                try{
                    Thread.sleep(time);
                }catch(InterruptedException e)
                {
                System.out.println("Problem with sleep");
                }
                for (i = 0; i < 50; i++) {
                    h = (float) Math.cos((coeff * (i - v * t)));
                    for (j = 0; j < 50; j++) {
                        height[i + 50 * j] = h;
                    }
                }
                try{
                    heightField.setValue(height.length, height);
                }catch(org.web3d.vrml.lang.InvalidFieldException e)
                {
                e.printStackTrace();
            }
                newTime = System.currentTimeMillis();
                dTime = newTime - oldTime;
                /*if(dTime < 1)
                {
                    dTime = new Long(1);
                }
                fps = 1000/dTime;
                System.out.println(fps);*/
                t += dTime*0.1;
            }
        }

    }
}
