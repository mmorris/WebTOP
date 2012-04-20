/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.geometrical;

//import vrml.external.field.*;
import org.web3d.x3d.sai.*;
import org.webtop.util.*;
import org.webtop.x3d.output.PointSet;

class Image {
    /*public static final float[]
     RED = new float[] {1,0,0},
     WHITE = new float[] {1,1,1},
     BLUE = new float[] {0,0,1},
     GREEN = new float[] {0,1,0},
     TEAL = new float[] {0,1,1},
     YELLOW = new float[] {1,1,0};*/

    private PointSet imagePoints;

    private Geometrical wapp;
    public Image(Geometrical main, String pointsEI, String colorsEI) {
        wapp = main;

        imagePoints = new PointSet(main.getSAI(), main.getSAI().getNode("ImagePoints"));
    }

    private int processes; //watcher of wastefulness

    public void clear() {
        float[][] imgpts = new float[0][], colors = new float[0][];
        //pointCoords.setValue(0,imgpts);
        //pointColors.setValue(0,colors);
        imagePoints.setCoords(imgpts);
        imagePoints.setColors(colors);
    }


    public void process(RayList rays, int count) {
        float[][] imgpts = new float[count][], colors = new float[count][];

        //peter
        //float[][] imgpts = new float[count][],colors = new float[count][];

        rays = rays.next; //the first ray is a dummy head for the list
        int i = 0;
        for (i = 0; rays != null; rays = rays.next, i++) {
            imgpts[i] = new float[] {rays.x, rays.y, rays.z / OpticalElement.POSITION_SCALE};
            colors[i] = rays.color;

            /*
                Assign to colors array here...
                if(rays.p<=...) colors[i]=...;
                Et cetera.
             */

            //peter
            //colors[i]=new float[] {1,1,1};
        }

        //pointCoords.setValue(i,imgpts);
        //pointColors.setValue(i,colors);

        imagePoints.setCoords(imgpts);
        imagePoints.setColors(colors);



        //System.arraycopy(colors,0,colors2,0,pointsHaveHit);
        //pointColors.setValue(colors2);

        //pointColors.setValue(colors);

        //DebugPrinter.once();
        DebugPrinter.println("image #" + ++processes + ": " + count + '/' + wapp.getRaysCast());
    }
}
