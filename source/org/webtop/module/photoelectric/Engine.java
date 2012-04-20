/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Engine.java
//Animation engine for the Photolectric Effect module.
//Karolina Sarnowska & Peter Gilbert
//Created September 7 2004
//Updated June 2 2005
//Version 0.1

package org.webtop.module.photoelectric;

import java.util.Vector;
import org.webtop.util.*;
import org.webtop.x3d.*;

//import vrml.external.field.*;

public class Engine implements AnimationEngine {
    private final int IN_RANGE = 0, OUT_OF_RANGE_LEFT = 1, OUT_OF_RANGE_RIGHT = 2;

    private Photoelectric wapp;
    private Animation animation;

    private final SAI sai;

    private final float hc = 1240f;

    private final int CURRENT_RANGE = 10;

    private float t = 0;
    private float periods = 0;
    private int electronsPerPeriod;
    private float difference;
    private float remainder;

    private float wavelength;
    private float intensity;
    private float workFunction;
    private float voltage;
    private int count = 0;

    private int currentCount[] = new int[CURRENT_RANGE];
    private int currentIndex = 0;

    private Vector electronVector, available;

    private Photoelectric.Data curData;

    public Engine(Photoelectric photoelectricEffect, SAI _sai) {
        wapp = photoelectricEffect;
        sai = _sai;
        electronVector = new Vector();
        available = new Vector();
        for (int i = 0; i < CURRENT_RANGE; i++) {
            currentCount[i] = 0;
        }
    }


    public void init(Animation anim) {
        animation = anim;
        //t_step=anim.getPeriod()/1000f;
    }

    public boolean timeElapsed(float periodsElapsed) {
        //t+=periods;
        t += animation.getPeriod() / 1000f;
        //wapp.setElapsedTime(t);
        periods += 1;
        //System.out.println(periodsElapsed);
        return true;
    }

    public synchronized void execute(Animation.Data d) {
        if (curData != d) {
            curData = (Photoelectric.Data) d;
            wavelength = curData.wavelength;
            intensity = curData.intensity;
            workFunction = curData.workFunction;
            voltage = curData.voltage;
        }

        int electrons = electronsPerPeriod;

        if (remainder >= 1) {
            electrons += 1;
            remainder -= 1;
        }

        if (hc / wavelength > workFunction) {
            for (int j = 0; j < electrons; j++) {
                addElectron(wavelength, workFunction, voltage);
                //electronVector.addElement(new Photoelectron(wavelength, workFunction, voltage, wapp,sai));
            }
        }

        /*
           if (periods%40==0) {
         wapp.setCurrent((float)currentCount/2f);
         currentCount=0;
           }
         */

        //currentCount[currentIndex]++;
      //Davis/Dr. Foley hack #3...slow the refresh rate down to get a better average current
        if (periods % 20 == 0) {
            updateCurrent();
            currentIndex++;
            currentIndex %= CURRENT_RANGE;
            currentCount[currentIndex] = 0;
        }

        if (!electronVector.isEmpty()) {
            int k = 0;
            Photoelectron photoelec;

            boolean last;

            do {
                photoelec = (Photoelectron) electronVector.elementAt(k);
                last = photoelec == electronVector.lastElement();
                try {
                    photoelec.passTime(animation.getPeriod() / 1000f);
                } catch (OutOfMemoryError fake) {} catch (ClassCastException fake) {}

                if (photoelec.checkBounds() == OUT_OF_RANGE_LEFT) {
                    removeElectron(photoelec);
                    k--;
                } else if (photoelec.checkBounds() == OUT_OF_RANGE_RIGHT) {
                    //currentCount++;
                    currentCount[currentIndex]++;
                    removeElectron(photoelec);
                    k--;
                }
                k++;
            } while (!last);

        }

        remainder += difference;
        //if (!animation.isPaused())
        //	updateScreen();
    }

    public void setT(float time) {
        t = time;
    }

    public synchronized void addElectron(float wavelength, float workFunction, float voltage) {
        Photoelectron newElectron;
        if (!available.isEmpty()) {
            newElectron = (Photoelectron) (available.firstElement());
            available.removeElementAt(0);
            newElectron.reset(wavelength, workFunction, voltage);
        }
        else
            newElectron = new Photoelectron(wavelength, workFunction, voltage, wapp,sai);
        electronVector.addElement(newElectron);
    }

    public synchronized void removeElectron(Photoelectron _electron) {
        electronVector.removeElement(_electron);
        available.addElement(_electron);
        //_electron.destroy();
    }

    // needs to be fixed
    public synchronized void removeAllElectrons() {
        Photoelectron photoelec;
        if (electronVector.isEmpty()) {
            return;
        }

        int i = 0;
        do {
            photoelec = (Photoelectron) electronVector.elementAt(i);
            photoelec.hide();
            available.addElement(photoelec);
            //removeElectron(photoelec);
            //photoelec.destroy();
            i++;
        } while (photoelec != electronVector.lastElement());
        electronVector.removeAllElements();
    }

    public void reset() {
        t = 0;
    }


    public void setRate(float rate) {
        electronsPerPeriod = (int) (rate / (1000 / animation.getPeriod()));
        difference = (float) (rate / (float) (1000 / animation.getPeriod())) - electronsPerPeriod;
        remainder = 0;
    }

    public void updateCurrent() {
        int total = 0;
        for (int i = 0; i < CURRENT_RANGE; i++) {
            total += currentCount[i];
        }
        //System.out.println("Current: " + (float)total/(float)CURRENT_RANGE*100f);
        //System.out.println("total: " + total);
        wapp.setCurrent((float) total / (float) CURRENT_RANGE * 2f);
        //for (int i=CURRENT_RANGE-1; i>0; i--)
        //	currentCount[i]=currentCount[i-1];
        //currentCount[0]=0;
    }


}
