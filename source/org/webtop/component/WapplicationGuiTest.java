/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.component;

/**
 * <p>Title: X3DWebTOP</p>
 *
 * <p>Description: The X3D version of The Optics Project for the Web
 * (WebTOP)</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: MSU Department of Physics and Astronomy</p>
 *
 * @author Paul Cleveland, Peter Gilbert
 * @version 0.0
 */

import javax.swing.*;
import java.awt.*;
import java.awt.BorderLayout;

public class WapplicationGuiTest extends JFrame{
    public WapplicationGuiTest() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(gridBagLayout1);
        jPanel1.setBackground(Color.orange);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jPanel2.setBackground(Color.red);
        this.getContentPane().add(jPanel2, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 100, 50));
        this.getContentPane().add(jPanel1, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 480, 435));
    }

    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JPanel jPanel1 = new JPanel();
    private JPanel jPanel2 = new JPanel();

    public static void main(String args[])
    {
        WapplicationGuiTest test = new WapplicationGuiTest();
        test.setSize(640,480);
        test.setVisible(true);
    }
}
