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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

//import vrml.external.field.*;

import org.webtop.component.*;
import org.webtop.util.*;
//import webtop.util.script.NavigationPanelScripter;
//import webtop.util.script.StateButtonScripter;
//import webtop.vrml.*;
//import webtop.vrml.widget.*;
//import webtop.wsl.client.*;
//import webtop.wsl.script.*;
//import webtop.wsl.event.*;
import org.sdl.gui.numberbox.*;
import org.sdl.math.FPRound;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.webtop.wsl.script.WSLNode;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;
import org.webtop.x3d.widget.Widget;

public class Geometrical extends WApplication implements ActionListener,
        NumberBox.Listener, X3DFieldEventListener, StateButton.Listener {
//===========
// CONSTANTS
//===========
    public static final Color FOREGROUND = Color.white, BACKGROUND = Color.darkGray,
    STATUS_FOREGROUND = Color.yellow;

    private static final int X = 0, Y = 1, Z = 2; //Indices into 3-element point float[]s
    private static final float NEW_ELEMENT_OFFSET = 40, MAX_ELEMENT_POSITION = 200;

    private static final int CONVERGING_LENS = 0, DIVERGING_LENS = 1, STOP = 2, DONUT_STOP = 3,
    GROUND_GLASS_SCREEN = 4, MAGIC_SCREEN = 5, UNKNOWN_LENS_A = 6, UNKNOWN_LENS_B = 7,
    UNKNOWN_LENS_C = 8, UNKNOWN_LENS_D = 9, UNKNOWN_LENS_E = 10, UNKNOWN_LENS_F = 11,
    UNKNOWN_LENS_G = 12, UNKNOWN_LENS_H = 13, UNKNOWN_LENS_I = 14, UNKNOWN_LENS_J = 15;

    private static final int SOURCE_SCREEN = 0;

//======
// JAVA
//======
    private Source theSource;
    private Image theImage;
    private RayList rays;
    private RayList virtualRays;

    private OpticalBench bench;
    private ObservationScreen theScreen;
    private GroundGlassScreen theGGScreen;
    private MagicScreen theMagicScreen;

//======
// WSL
//======
    //private WSLPanel wslPanel;
    //private WSLPlayer wslPlayer;
    //Remove once WAppletized:
    //private NavigationPanelScripter nps;

//======
// X3D
//======
    //private EAI eai;
    private MFNode addElements, removeElements;
    //public final SAI.Try saitry = new SAI.Try(this);


//==============
// MODULE STATE
//==============
    //ray density has units of rays/(steradian*macropoint)
    private static final int RAY_DENSITIES[] = {3, 7, 12, 17, 24}, //default densities
    DEF_DENSITY_INDEX = 2,
    CUSTOM_DENSITY = RAY_DENSITIES.length,
    MIN_DENSITY = 1, MAX_DENSITY = 100;
    private int rayDensity;
    private float[][] points;
    private float[][] colors;

    //For hokeyness/incremental hooking/etc.:
    private float lastD, lastP; //distance/position to first element before most recent change
    private OpticalElement activeElement, lastFE; //first element before most recent change

    private int raysCast; //for others' use

    private boolean autoTrace;

    //Used to prevent OpticalElements from being added at the wrong position during reset() and script loading... should be changed.	For some reason, a call to setPosition() for an OpticalElement sometimes does not actually update the position of the element in the VRML after the OpticalElement has been added. [peter]
    protected boolean resetting = false;
    protected boolean scriptAdding = false;

    private boolean addingElement = false;
    private volatile boolean draggingWidget;

    private boolean unknownLens = false, groundGlassScreen = false, magicScreen = false;
    private OpticalElement lastLens;
    private float[] lastLensS;
    private int lastLensIndex = 0;
    private int lastLensOpIndex = 0;

    private boolean widgetsVisible = true;

//=====
// GUI
//=====
    private JButton addElement, removeElement, reset, scrapRays;
    private StateButton sourceToggle, widgetVisibility;
    //private StateButtonScripter sourceToggleScripter;
    //private Choice elementType, elementSelector, sourceType, densitySelector;
    private JComboBox elementType, elementSelector, sourceType, densitySelector;
    private IntBox densityField;
    private CardLayout elementsLayout;
    private JPanel raysPanel, sourcePanel, densityPanel, resetPanel, newRaysPanel, rePanel,
    elementsPanel, addSelectPanel, elementSlotPanel;

    //{DebugPrinter.on();}
//=======
// SETUP
//=======

    public Geometrical(String title, String world) {
        super(title, world, true);
    }

    private class SourceScreenListener implements X3DFieldEventListener {
        public void readableFieldChanged(X3DFieldEvent e) {

        }
    }


    //private X3DFieldEventListener sourceScreenListener = new SourceScreenListener();


    public SAI getSAI() {
        return super.getSAI();
    }

    public StatusBar getStatusBar() {
        return super.getStatusBar();
    }

    public void start() {

        /*NavigationPanel navPanel = new NavigationPanel(eai, eai.getNode("NavPanel"), (short) 0,
                "Changes viewpoints.");
                 new RecursiveListener(this,
                              new ViewpointReader(navPanel, KeyEvent.VK_F12, KeyEvent.SHIFT_MASK)).
                setup();
         new RecursiveListener(this, new ViewpointReset(navPanel, KeyEvent.VK_F12, 0)).setup();
                 nps = new NavigationPanelScripter(navPanel, wslPlayer);*/

        getSAI().getOutputField("ScreenTS", "hitPoint_changed", this, new Integer(SOURCE_SCREEN));
        //eai.getEO("SourceWheel","value_changed",sourceScreenListener,this);
        addElements = (MFNode) getSAI().getInputField("Bench", "addChildren");
        removeElements = (MFNode) getSAI().getInputField("Bench", "removeChildren");

        sourcePanel.add(theSource.getfbRotationAngle());
        sourcePanel.add(sourceToggle);
        sourceToggle.addListener(this);
        //sourceToggleScripter = new StateButtonScripter(sourceToggle, getWSLPlayer(), null,
        //        "hideSource", new String[] {"Off", "On"}, 0);

        theImage = new Image(this, "ImagePoints", "ImageColors");

        //wslPlayer.loadParameter(this);

        reset();

        //We don't seem to need this anymore
        //EAI.setDraw(eai.getEI("ClickBelow","whichChoice"),false);
        //EAI.setDraw(eai.getEI("NavPanel","set_visible"),true);
        DebugPrinter.println("start() done.");
    }

    private void reset() {
        autoTrace = false;
        resetting = true;

        clear();
        OpticalElement.resetIDs();

        theScreen = new ObservationScreen(this, 90);
        //theImage.reset();
        update(theScreen, OpticalElement.CREATED);
        ThinLens firstLens = new ConvergingLens(this, 30);
        update(firstLens, OpticalElement.CREATED);

        elementSelector.invalidate();

        //wslPlayer.recordObjectAdded(firstLens.toWSLNode());
        //wslPlayer.recordObjectAdded(theScreen.toWSLNode());

        resetting = false;

        autoTrace = true;
        setSourceType(Source.SMALL_T); //this will re-calculate/raytrace
        sourceType.setSelectedIndex(Source.SMALL_T);
        sourceToggle.setState(0);
        widgetVisibility.setState(0);

        //setWidgetVisibility(true);
        setActiveElement(firstLens);
    }

    private void clear() {
        while (bench.size() > 0) {
            //wslPlayer.recordObjectRemoved(String.valueOf((bench.firstElement()).getName()));
            unregisterElement(bench.firstElement());
        }
    }

//=====
// EAI
//=====

    public MFNode getAddElements() {
        return addElements;
    }

    public MFNode getRemoveElements() {
        return removeElements;
    }

//============================
// OpticalElement INFORMATION
//============================

    public boolean isAddingElement() {
        return addingElement;
    }

//=========================
// ELEMENT-LIST MANAGEMENT
//=========================
    //Registers the given element with the various things that need to know
    //about it
    //This is now only called from update(), and should perhaps be part of it [Davis]
    public void registerElement(OpticalElement oe) {
        bench.add(oe);
        if (oe instanceof UnknownLens) {
            unknownLens = true;
        } else if (oe instanceof GroundGlassScreen) {
            groundGlassScreen = true;
        } else if (oe instanceof MagicScreen) {
            magicScreen = true;
        }
        elementSlotPanel.add(oe, oe.getName());
        elementSelector.addItem(oe.getName());
        //if(activeElement==null) setActiveElement(oe);
        setActiveElement(oe);
    }

    //Cleans up after an element
    private void unregisterElement(OpticalElement oe) {
        oe.destroyElement();
        bench.remove(oe);
        if (oe instanceof UnknownLens) {
            unknownLens = false;
        } else if (oe instanceof GroundGlassScreen) {
            theGGScreen.clear();
            groundGlassScreen = false;
        } else if (oe instanceof MagicScreen) {
            theMagicScreen.clear();
            magicScreen = false;
        }
        if (!oe.equals(theScreen)) { //Temporary fix 7/22/2004 (see changes log) [Peter]
            elementSlotPanel.remove(oe);
        }
        elementSelector.removeItem(oe.getName());
        if (activeElement == oe) {
            activeElement = null;
        }
        if (activeElement == lastFE) {
            lastFE = null;
        }
    }

    //Returns the proper place to add a new element
    public float getNextPosition() {
        //This is a bit silly; I don't assume there's -anything- on the bench, but
        //then I assume that if there's a screen, there's exactly one and it's
        //'theScreen'.	However, the resulting weirdness ought to be easy to
        //modify in either direction as needed.	 [Davis]
        if (bench.size() == 0) {
            return NEW_ELEMENT_OFFSET;
        }
        int screenIndex = bench.indexOf(theScreen);
        switch (screenIndex) {
        case -1:
            float lastPos = bench.lastElement().getPosition();
            if (lastPos + NEW_ELEMENT_OFFSET > MAX_ELEMENT_POSITION) {
                return (lastPos + MAX_ELEMENT_POSITION) / 2;
            }
            return lastPos + NEW_ELEMENT_OFFSET;
        case 0:
            return theScreen.getPosition() / 3;
        default:

            //Drop the new element between the screen and the last element before it
            return (theScreen.getPosition() + 2 *
                    bench.elementAt(screenIndex - 1).getPosition()) / 3;
        }
    }

    public void setActiveElement(OpticalElement oe) {
        DebugPrinter.println("setActiveElement(" + oe + ')');
        if (activeElement == oe) {
            getStatusBar().setText(oe.getHelp());
            return;
        }
        if (activeElement != null) {
            activeElement.setWidgetVisibility(false);
        }

        activeElement = oe;
        elementsLayout.show(elementSlotPanel, oe.getName());
        statusBar.setText(oe.getHelp());

        if (bench.indexOf(oe) >= 0) {
            elementSelector.setSelectedItem(oe.getName());
        }

        if (widgetsVisible) {
            oe.setWidgetVisibility(true);
        }

        //Can't remove screen
        removeElement.setEnabled(!(oe instanceof ObservationScreen));
    }

    public void setActiveElement(String name) {
        OpticalBench.Enumeration enumeration = bench.elements();
        while (enumeration.hasMoreElements()) {
            OpticalElement oe = enumeration.nextElement();
            if (oe.getName().equals(name)) {
                setActiveElement(oe);
                return;
            }
        }
    }

    public OpticalElement getActiveElement() {
        return activeElement;
    }

//========
// SOURCE
//========
    private void setSourceType(int num) {
        //...?

        theSource.setSourceType(num);
        theSource.updateSource();
        createRays();
    }

    public void setWidgetVisibility(boolean visible) {
        widgetsVisible = visible;
        if (visible) {
            for (int i = 0; i < bench.size(); i++) {
                bench.elementAt(i).setEnabled(true);
            }
            activeElement.setWidgetVisibility(true);
        } else {
            for (int i = 0; i < bench.size(); i++) {
                bench.elementAt(i).setWidgetVisibility(false);
                bench.elementAt(i).setEnabled(false);
            }
        }
    }

    public int getMicropointCount() {
        return theSource.getMacropointCount();
    }

//=========
// QUALITY
//=========
    //Called when a widget starts or stops being dragged
    private void setDragging(boolean d) {
        draggingWidget = d;
        //if(draggingWidget=d) lastFE=bench.firstElement();
    }

    public boolean draggingWidget() {
        return draggingWidget;
    }

    public int getRaysCast() {
        return raysCast;
    }

//=============
// CALCULATION
//=============
    //Sorts the optical elements by position on the bench whenever one moves
    //Also raytraces as need be; OpticalElement argument may be null
    public void update(OpticalElement oe, int event) {
        //boolean traceReal = true, traceVirtual = true;
        System.out.println("Geometrical::update(" + oe + ")");
        if (oe != null) {
            if (event == ThinLens.FOCUS || event == UnknownLens.FOCUS) {
                getStatusBar().setText(oe.getHelp());
            }

            if (event == OpticalElement.POSITION) {
                bench.update(oe);

                /*
                     if (oe instanceof ObservationScreen)
                 traceVirtual=false;
                     else if (oe instanceof MagicScreen)
                 traceReal=false;
                 */

                if (oe instanceof ThinLens || oe instanceof UnknownLens) {
                    getStatusBar().setText(oe.getHelp());
                } else if (oe instanceof GroundGlassScreen || oe instanceof ObservationScreen ||
                           oe instanceof MagicScreen) {
                    if (groundGlassScreen) {
                        if (theGGScreen.getPosition() > theScreen.getPosition()) {
                            theGGScreen.clear();
                        }
                    } else if (magicScreen) {
                        if (theMagicScreen.getPosition() > theScreen.getPosition()) {
                            theMagicScreen.clear();
                        }
                    }
                }
            }

            if (event == OpticalElement.CREATED) {
                registerElement(oe);
                if (oe instanceof UnknownLens || oe instanceof ThinLens) {
                    computeDistances(oe.getID());
                }
            }

            if (unknownLens || event == OpticalElement.REMOVED) {
                checkForImagePlane();
            }

            //If this was a change to the first element, or if the first element has
            //changed, we need to adjust for hokeyness, but not while mid-big-change
            OpticalElement first = bench.firstElement();
            if (autoTrace && (oe == first || first != lastFE)) {
                adjustRays();
            }
            lastD = first.getDiameter();
            lastP = first.getPosition();

            //if(!draggingWidget)
            lastFE = first;
        }
        //maybeRaytrace(traceReal,traceVirtual);
        maybeRaytrace();
    }

    public void maybeRaytrace() {
        System.out.println("autotrace: " + autoTrace);
        if (autoTrace) {
            raytrace();
        }
    }

    //public void maybeRaytrace(boolean traceReal, boolean traceVirtual) { if(autoTrace) raytrace(traceReal,traceVirtual);}

    ////------HELPER FUNCTIONS------\\
    //Returns rays per micropoint per unit area (on axis) for a circle of radius
    //r at position p, from a source point on the axis at z with luminosity L
    //and m micropoints.
    private float getDensity(float r, float p, float z, float L, int m) {
        r *= r; //we need r^2
        p -= z; //we need difference in position
        return rayDensity * L *
                2 * (float) Math.PI * (float) (1 - 1 / Math.sqrt(1 + r / (p * p))) /
                ((float) Math.PI * r) / m;
    }

    private static double theta(double d, double h, double r1, double r2) {
        return Math.atan((h + r2) / d) - Math.atan((h + r1) / d) + Math.atan((h - r1) / d) -
                Math.atan((h - r2) / d);
    }

    //The on-axis case (this is just an optimized version of theta(d,0,r1,r2))
    private static double theta(double d, double r1, double r2) {
        return 2 * (Math.atan(r2 / d) - Math.atan(r1 / d));
    }

    //\\------HELPER FUNCTIONS------//

    //Creates rays with a density of n uniformly distributed over the annulus
    //with inner radius radiusL and outer radius radiusH at position pos, from
    //the point src ({x,y,z}), and with point number p.	 Attaches rays to given
    //head ray.	 Returns reference to last ray added.
    private RayList createRays1(double n, float radiusL, float radiusH, float pos, float[] src,
                                RayList head, int p, float[] colors) {
        float x = src[X], y = src[Y], z = src[Z];
        pos -= z; //difference is all we need

        double ratio = theta(pos, Math.sqrt(x * x + y * y), radiusL, radiusH) /
                       theta(pos, radiusL, radiusH);
        //System.out.println("ratio: "+ratio+" (x="+x+",y="+y+",r1="+radiusL+",r2="+radiusH+",pos="+pos+")");
        //ratio=Math.pow(ratio,10);	//temp; cheat

        //Now we really need the radii squared:
        radiusL *= radiusL;
        radiusH *= radiusH;

        n *= ratio * Math.PI * (radiusH - radiusL); //scale by off-axis ratio and by area of casting
        for (int ri = 0; ri < (int) n || (ri == (int) n && Math.random() < n % 1); ri++) {
            ++raysCast;

            double r = Math.sqrt(radiusL + Math.random() * (radiusH - radiusL)),
                       theta = 2 * Math.PI * Math.random();

            head = head.hook(x, y, z,
                             (float) (r * Math.cos(theta) - x) / pos,
                             (float) (r * Math.sin(theta) - y) / pos, p, colors);
            //if(raysCast%243==0) DebugPrinter.println(previous.toString());
        }

        return head;
    }

    //This is just a loop to call createRays1() for each of a set of points.
    private RayList createRays0(float radiusL, float radiusH, float pos,
                                float[][] points, RayList head, float[][] colors) {
        final int pointCount = points.length; //to only access field once
        final int micropointRatio = theSource.getMicropointRatio();
        final float luminosity = theSource.getLuminosity();
        for (int p = 0; p < pointCount; ++p) {
            head = createRays1(getDensity(radiusH, pos, points[p][Z], luminosity,
                                          micropointRatio),
                               radiusL, radiusH, pos, points[p], head, p, colors[p]);
        }

        return head;
    }

    public void createRays() {
        if (bench.size() == 0) {
            return;
        }

        points = theSource.getPoints();
        colors = theSource.getColors();
        int numPoints = theSource.getMicropointCount();

        //Hokeyness!
        final OpticalElement firstOpElement = bench.firstElement();
        final float radius1 = firstOpElement.getDiameter() / 2;
        final float position1 = firstOpElement.getPosition();

        //We're discarding old rays, so reset counter:
        raysCast = 0;

        createRays0(0, radius1, position1, points, rays, colors);

        maybeRaytrace(); //we made them, so use them!
    }

    //OK, version two: recasts to uniformly hit the first element even if both
    //size and position have changed.	 The trick is to make one change and then
    //another, as this has the same end-result.	 Ideally we'd make the
    //'broadening' change first (that way we keep the most rays); this may not
    //make sense, though, because changes of position always cause casting and
    //reclipping

    //Trick #2: if both have changed, use latter section of code.	 It seems to
    //already be sufficient.
    private void adjustRays() {
        DebugPrinter.print("adjustRays() [rays: " + raysCast + " (" + rays.getLength() + ")");
        if (bench.size() == 0) {
            return;
        }
        final float p = bench.firstElement().getPosition(),
                        d = bench.firstElement().getDiameter();

        //If things haven't moved, we can use the annulus trick:
        if (p == lastP) {
            if (d > lastD) { //It got bigger -- cast more rays
                RayList extant = rays.next,
                                 current = createRays0(lastD / 2, d / 2, lastP, points, rays,
                        colors);
                current.next = extant; //add back the old rays, and we're done
            } else if (d < lastD) { //it got smaller: just clip rays that miss
                raysCast -= rays.prune(lastP, d, true);
            }
            //if neither of these worked, d==lastD and nothing needs to happen
        } else {
            //This section should be general; let's try it.

            //When the first element moves, we have to recast things even if it's
            //farther, because source points whose to-axis distance is greater than
            //the radius of the element will see new 'area' (despite seeing a
            //smaller solid angle overall).

            DebugPrinter.println("d: " + lastD + " -> " + d + "; p: " + lastP + " -> " + p);

            //First, make some new rays:
            RayList nu = new RayList(); //a temporary, dummy head (how fake can you get?)
            createRays0(0, d / 2, p, points, nu, colors);

            //Now remove all the new rays that hit the OLD position -- we want to
            //keep the old rays that did that!
            raysCast -= nu.prune(lastP, lastD, false);

            //Remove all the old rays that miss the new position:
            //Ideally, we'd keep old rays while dragging (particularly for the
            //effect of shuttering and unshuttering the same rays); unfortunately,
            //this creates a series of oblique cones that are nearly impossible to
            //deal with.
            raysCast -= rays.prune(p, d, true);

            //Then add the surviving old rays to the surviving new rays:
            RayList end;
            for (end = nu; end.next != null; end = end.next) {
                ; //Find the end of the new list
            }
            end.next = rays.next;
            rays = nu;
        }
        DebugPrinter.println(" -> " + raysCast + " (" + rays.getLength() + ")]");
    }

    /*
      private void raytrace() {
     raytrace(true,true);
      }
     */

    private void raytrace( /*boolean traceReal, boolean traceVirtual*/) {

        //Shadow master copy of rays with a local copy to play with:
        RayList rays = RayList.clone(this.rays);

        //Find rightmost lens before the observation screen to create virtual rays
        OpticalElement rightmostLens = null;
        if (magicScreen) {
            OpticalBench.Enumeration list = bench.elements();
            while (list.hasMoreElements()) {
                OpticalElement opE = list.nextElement();
                if (opE instanceof ThinLens) {
                    rightmostLens = opE;
                }
                if (opE instanceof ObservationScreen) {
                    break;
                }
            }
        }

        //Raytrace
        OpticalBench.Enumeration enumeration = bench.elements();
        int remaining = raysCast;
        int remainingVirtual = raysCast;
        while (enumeration.hasMoreElements()) {
            OpticalElement oe = enumeration.nextElement();
            //We can keep processing 0 rays, but can no longer propogate them:
            if (rays.next != null) {
                rays.next.propagateAll(oe.getPosition());
            }
            if (!(oe instanceof MagicScreen) && !(oe instanceof GroundGlassScreen)) {
                remaining -= oe.clip(rays);
            }
            oe.process(rays);
            if (oe == rightmostLens && magicScreen) {
                virtualRays = virtualRays.virtualClone(rays);

                //rays.clone(virtualRays);
                remainingVirtual = remaining;
                if (virtualRays.next != null) {
                    virtualRays.next.propagateAll(theMagicScreen.getPosition());
                }
            }
            if (oe instanceof GroundGlassScreen) {
                theGGScreen.raytrace(rays, remaining);
            }
            if (oe instanceof MagicScreen) {
                if (rightmostLens != null && oe.getPosition() >= rightmostLens.getPosition()) {
                    theMagicScreen.raytraceReal(rays, remaining);
                } else if (rightmostLens == null) {
                    theMagicScreen.clear();
                }
            }
            if (oe instanceof ObservationScreen) {
                break;
            }
        }

        //if (traceReal)
        System.out.println("rays length: " + rays.getLength());
        System.out.println("remaining: " + remaining);
        theImage.process(rays, remaining);

        //if (rightmostLens!=null)
        //System.out.println("LastLensOpIndex: " + lastLensOpIndex + ", RighmostLensIndex: " + rightmostLens.getID() + ", distance: " + lastLensS[0]);

        //if (traceVirtual) {
        if (rightmostLens != null && magicScreen &&
            theMagicScreen.getPosition() < rightmostLens.getPosition() &&
            lastLensOpIndex == rightmostLens.getID() && lastLensS[1] < 0) {
            remainingVirtual -= theMagicScreen.clip(virtualRays);
            //System.out.println("REMAINING VIRTUAL: " + remainingVirtual);
            //System.out.println("??: " + virtualRays.getLength());
            theMagicScreen.raytraceVirtual(virtualRays, remainingVirtual);
        } else if (magicScreen && rightmostLens == null) {
            theMagicScreen.clear();
        } else if (rightmostLens != null && lastLensOpIndex == rightmostLens.getID() &&
                   lastLensS[1] >= 0 && theMagicScreen.getPosition() < rightmostLens.getPosition()) { // fix this
            theMagicScreen.clear();
        }
        //}
        //if (theGGScreen!=null)
        //	theGGScreen.raytrace(rays,remaining);

    }

    // for poofing
    //public Image getImage() {
    //	return theImage;
    //}

    public void checkForImagePlane() {
        if (!unknownLens) {
            theScreen.setTextVisible(false);
            return;
        }
        if (lastLens != null &&
            Math.abs((lastLens.getPosition() + lastLensS[1]) - theScreen.getPosition()) <= 0.25f) {
            theScreen.setTextVisible(true);
        } else {
            theScreen.setTextVisible(false);
        }
    }

    public String computeDistances(int id) {
        // To calculate S and S' for lenses
        // Soon to be improved [Peter]
        OpticalElement lenses[] = new OpticalElement[40];
        float distances[][] = new float[4][2];

        int index = 0;
        OpticalBench.Enumeration enumeration = bench.elements();
        while (enumeration.hasMoreElements()) {
            OpticalElement oe = enumeration.nextElement();
            if (oe instanceof ThinLens || oe instanceof UnknownLens) {
                lenses[index] = oe;
                index++;
            }
        }

        OpticalElement hold;

        for (int pass = 0; pass < index - 1; pass++) {
            for (int i = 0; i < index - 1; i++) {
                if (lenses[i].getPosition() > lenses[i + 1].getPosition()) {
                    hold = lenses[i];
                    lenses[i] = lenses[i + 1];
                    lenses[i + 1] = hold;
                }
            }
        }

        if (index < 4) {
            for (int q = 0; q < index; q++) {
                if (lenses[q].getPosition() < theScreen.getPosition()) {
                    lastLens = lenses[q];
                    lastLensIndex = q;
                }
            }
        } else {
            lastLens = null;
        }

        lastLensOpIndex = lenses[lastLensIndex].getID();

        float s1 = lenses[0].getPosition(), f2 = 0, d12 = 0, f3 = 0, d23 = 0, f4 = 0, d34 = 0;
        float f1 = 0;
        if (lenses[0] instanceof ThinLens) {
            f1 = ((ThinLens) lenses[0]).getFocalLength();
        } else if (lenses[0] instanceof UnknownLens) {
            f1 = ((UnknownLens) lenses[0]).getFocalLength();
        }

        if (theSource.getSourceType() == theSource.T_AT_INFY ||
            theSource.getSourceType() == theSource.PT_AT_INFY) {
            s1 = Float.POSITIVE_INFINITY;
        }

        if (index > 1) {
            if (lenses[1] instanceof ThinLens) {
                f2 = ((ThinLens) lenses[1]).getFocalLength();
            } else if (lenses[1] instanceof UnknownLens) {
                f2 = ((UnknownLens) lenses[1]).getFocalLength();
            }
            d12 = lenses[1].getPosition() - lenses[0].getPosition();
        }

        if (index > 2) {
            if (lenses[2] instanceof ThinLens) {
                f3 = ((ThinLens) lenses[2]).getFocalLength();
            } else if (lenses[2] instanceof UnknownLens) {
                f3 = ((UnknownLens) lenses[2]).getFocalLength();
            }
            d23 = lenses[2].getPosition() - lenses[1].getPosition();
        }

        if (index > 3) {
            if (lenses[3] instanceof ThinLens) {
                f4 = ((ThinLens) lenses[3]).getFocalLength();
            } else if (lenses[3] instanceof UnknownLens) {
                f4 = ((UnknownLens) lenses[3]).getFocalLength();
            }
            d34 = lenses[3].getPosition() - lenses[2].getPosition();
        }

        for (int p = 0; p < 4; p++) {
            for (int q = 0; q < 2; q++) {
                distances[p][q] = 0;
            }
        }

        // distances[n][0,1]
        // 0 - image distance
        // 1 - object distance

        if (theSource.getSourceType() == theSource.T_AT_INFY ||
            theSource.getSourceType() == theSource.PT_AT_INFY) {
            distances[0][0] = f1;
        } else {
            distances[0][0] = (s1 * f1) / (s1 - f1);
        }
        distances[0][1] = s1;

        if (index > 1) {
            if (theSource.getSourceType() == theSource.T_AT_INFY ||
                theSource.getSourceType() == theSource.PT_AT_INFY) {
                distances[1][0] = ((f1 - d12) * f2) / (f1 - d12 + f2);
            } else {
                distances[1][0] = ((s1 * f1 - s1 * d12 + d12 * f1) * f2) /
                                  (s1 * f1 - s1 * d12 + s1 * f2 + d12 * f1 - f2 * f1);
            }
            distances[1][1] = 1 / (1 / f2 - 1 / distances[1][0]);
        }
        if (index > 2) {
            if (theSource.getSourceType() == theSource.T_AT_INFY ||
                theSource.getSourceType() == theSource.PT_AT_INFY) {
                distances[2][0] = ((f2 * f1 - d23 * f1 - d12 * f2 + d12 * d23 - d23 * f2) * f3) /
                                  (f2 * f1 - d23 * f1 + f1 * f3 - d12 * f2 + d12 * d23 - d12 * f3 -
                                   d23 * f2 + f3 * f2);
            } else {
                distances[2][0] = (( -s1 * f2 * f1 + s1 * d23 * f1 + s1 * d12 * f2 - s1 * d12 * d23 +
                                    s1 * d23 * f2 - d12 * f1 * f2 + d12 * f1 * d23 - d23 * f2 * f1) *
                                   f3) /
                                  ( -s1 * f2 * f1 + s1 * d23 * f1 - s1 * f1 * f3 + s1 * d12 * f2 -
                                   s1 * d12 * d23 + s1 * d12 * f3 + s1 * d23 * f2 - s1 * f3 * f2 -
                                   d12 * f1 * f2 + d12 * f1 * d23 - d12 * f1 * f3 - d23 * f2 * f1 +
                                   f3 * f2 * f1);
            }
            distances[2][1] = 1 / (1 / f3 - 1 / distances[2][0]);
        }
        if (index > 3) {
            if (theSource.getSourceType() == theSource.T_AT_INFY ||
                theSource.getSourceType() == theSource.PT_AT_INFY) {
                distances[3][0] = ((f3 * f2 * f1 - d34 * f2 * f1 - f1 * d23 * f3 + f1 * d23 * d34 -
                                    f1 * d34 * f3 - d12 * f3 * f2 + d12 * d34 * f2 + d12 * d23 * f3 -
                                    d12 * d23 * d34 + d12 * d34 * f3 - d23 * f2 * f3 +
                                    d23 * f2 * d34 - d34 * f3 * f2) * f4) /
                                  (f3 * f2 * f1 - d34 * f2 * f1 + f2 * f1 * f4 - f1 * d23 * f3 +
                                   f1 * d23 * d34 - f1 * d23 * f4 - f1 * d34 * f3 + f1 * f4 * f3 -
                                   d12 * f3 * f2 + d12 * d34 * f2 - d12 * f2 * f4 + d12 * d23 * f3 -
                                   d12 * d23 * d34 + d12 * d23 * f4 + d12 * d34 * f3 -
                                   d12 * f4 * f3 - d23 * f2 * f3 + d23 * f2 * d34 - d23 * f2 * f4 -
                                   d34 * f3 * f2 + f4 * f3 * f2);
            } else {
                distances[3][0] = (( -s1 * f3 * f2 * f1 + s1 * d34 * f2 * f1 + s1 * f1 * d23 * f3 -
                                    s1 * f1 * d23 * d34 + s1 * f1 * d34 * f3 + s1 * d12 * f3 * f2 -
                                    s1 * d12 * d34 * f2 - s1 * d12 * d23 * f3 +
                                    s1 * d12 * d23 * d34 - s1 * d12 * d34 * f3 + s1 * d23 * f2 * f3 -
                                    s1 * d23 * f2 * d34 + s1 * d34 * f3 * f2 - d12 * f1 * f3 * f2 +
                                    d12 * f1 * d34 * f2 + d12 * f1 * d23 * f3 -
                                    d12 * f1 * d23 * d34 + d12 * f1 * d34 * f3 - d23 * f2 * f1 * f3 +
                                    d23 * f2 * f1 * d34 - d34 * f3 * f2 * f1) * f4) /
                                  ( -d34 * f3 * f2 * f1 - s1 * d23 * f2 * d34 + s1 * d23 * f2 * f3 -
                                   s1 * d12 * d34 * f3 + s1 * d12 * d23 * d34 - s1 * d12 * d23 * f3 -
                                   s1 * d12 * d34 * f2 + s1 * d12 * f3 * f2 + s1 * f1 * d34 * f3 -
                                   s1 * f1 * d23 * d34 + s1 * f1 * d23 * f3 + s1 * d34 * f2 * f1 -
                                   s1 * f3 * f2 * f1 + d23 * f2 * f1 * d34 - d23 * f2 * f1 * f3 +
                                   d12 * f1 * d34 * f3 - d12 * f1 * d23 * d34 + d12 * f1 * d23 * f3 +
                                   d12 * f1 * d34 * f2 - d12 * f1 * f3 * f2 + s1 * d34 * f3 * f2 -
                                   s1 * f2 * f1 * f4 + f4 * f3 * f2 * f1 + s1 * d12 * f4 * f3 -
                                   s1 * d12 * d23 * f4 + s1 * d12 * f2 * f4 - s1 * f1 * f4 * f3 +
                                   s1 * f1 * d23 * f4 - d23 * f2 * f1 * f4 - d12 * f1 * f4 * f3 +
                                   d12 * f1 * d23 * f4 - d12 * f1 * f2 * f4 - s1 * f4 * f3 * f2 +
                                   s1 * d23 * f2 * f4);
            }
            distances[3][1] = 1 / (1 / f4 - 1 / distances[3][0]);
        }

        String tip = "";

        if (index > 4) {
            index = 4;
        }

        for (int n = 0; n < index; n++) {
            if (id == lenses[n].getID()) {
                //tip = "          S" + (n+1) + "=" + FPRound.toFixVal(distances[n][1],3) +"  S" + (n+1) + "'=" + FPRound.toFixVal(distances[n][0],3);
                tip = "         So" + (n + 1) + "=" + FPRound.toFixVal(distances[n][1], 3) + "  Si" +
                      (n + 1) + "=" + FPRound.toFixVal(distances[n][0], 3);
            }
        }
        lastLensS[0] = distances[lastLensIndex][1];
        lastLensS[1] = distances[lastLensIndex][0];
        return tip;
    }


//================
// EVENT-HANDLING
//================

    public void itemStateChanged(ItemEvent e) {
        //if (wslPlayer.isPlaying()) {
        //    return;


    }

    public void actionPerformed(ActionEvent e) {
        //if (wslPlayer.isPlaying())
        //    return;

        if (e.getSource() == elementSelector) {
            setActiveElement((String) elementSelector.getSelectedItem());
            //wslPlayer.recordActionPerformed("select", elementSelector.getSelectedItem());
        } else if (e.getSource() == sourceType) {
            DebugPrinter.println("Changing current source");
            setSourceType(sourceType.getSelectedIndex());
            setActiveElement(getActiveElement());
        } else if (e.getSource() == densitySelector) {
            boolean custom = densitySelector.getSelectedIndex() == CUSTOM_DENSITY;
            densityField.setEnabled(custom);
            if (!custom) {
                densityField.setValue(RAY_DENSITIES[densitySelector.getSelectedIndex()]);
            }
        } else if (e.getSource() == addElement) {
            DebugPrinter.print("ControlPanel: adding new ");
            OpticalElement oe = null;

            try {
                addingElement = true;

                switch (elementType.getSelectedIndex()) {
                case CONVERGING_LENS:
                    DebugPrinter.println("converging lens");
                    oe = new ConvergingLens(this, getNextPosition());
                    break;

                case DIVERGING_LENS:
                    DebugPrinter.println("diverging lens");
                    oe = new DivergingLens(this, getNextPosition());
                    break;

                case UNKNOWN_LENS_A:
                case UNKNOWN_LENS_B:
                case UNKNOWN_LENS_C:
                case UNKNOWN_LENS_D:
                case UNKNOWN_LENS_E:
                    DebugPrinter.println("unknown lens");
                    if (!unknownLens) {
                        oe = new UnknownLens(this, getNextPosition(),
                                             elementType.getSelectedIndex() - 6,
                                             "lensGeometry ConvexLens { resolution 40 depthResolution 10 }");
                    } else {
                        getStatusBar().setWarningText(
                                "Only one Unknown Lens at a time is allowed on the table.");
                    }
                    break;

                case UNKNOWN_LENS_F:
                case UNKNOWN_LENS_G:
                case UNKNOWN_LENS_H:
                case UNKNOWN_LENS_I:
                case UNKNOWN_LENS_J:
                    DebugPrinter.println("unknown lens");
                    if (!unknownLens) {
                        oe = new UnknownLens(this, getNextPosition(),
                                             elementType.getSelectedIndex() - 6,
                                             "lensGeometry ConcaveLens { resolution 40 depthResolution 10 } focusRotation 0 1 0 3.14159265");
                    } else {
                        getStatusBar().setWarningText(
                                "Only one Unknown Lens at a time is allowed on the table.");
                    }
                    break;

                case STOP:
                    DebugPrinter.println("stop aperture");
                    oe = new Stop(this, getNextPosition());
                    break;

                case DONUT_STOP:
                    DebugPrinter.println("donut stop aperture");
                    oe = new DonutStop(this, getNextPosition());
                    break;

                case GROUND_GLASS_SCREEN:
                    if (!groundGlassScreen) {
                        DebugPrinter.println("ground glass screen");
                        theGGScreen = new GroundGlassScreen(this, getNextPosition());
                        oe = theGGScreen;
                    } else {
                        getStatusBar().setWarningText(
                                "Only one Ground Glass Screen at a time is allowed on the table.");
                    }
                    break;

                case MAGIC_SCREEN:
                    if (!magicScreen) {
                        DebugPrinter.println("magic screen");
                        theMagicScreen = new MagicScreen(this, getNextPosition());
                        oe = theMagicScreen;
                    } else {
                        getStatusBar().setWarningText(
                                "Only one Magic Screen at a time is allowed on the table.");
                    }
                    break;

                    //case 3:
                    //DebugPrinter.println("screen");
                    //oe=new ObservationScreen(this,getNextPosition());
                    //break;
                default:
                    System.err.println("Geometrical::actionPerformed(): unexpected element type " +
                                       elementType.getSelectedIndex());
                    return;
                }
            } finally {
                addingElement = false;
            }
            if (oe != null) {
                update(oe, OpticalElement.CREATED);
                //wslPlayer.recordObjectAdded(oe.toWSLNode());
            }
        } else if (e.getSource() == removeElement) {
            OpticalElement oe = activeElement;
            //If you push the remove button fast enough, this can happen despite the fact that the button is disabled
            if (oe == theScreen) {
                return;
            }
            //wslPlayer.recordObjectRemoved(oe.getName());
            int index = bench.indexOf(oe);
            unregisterElement(oe);
            if (bench.size() > 0) {
                setActiveElement(bench.elementAt(Math.min(index, bench.size() - 1)));
            }
            update(oe, OpticalElement.REMOVED);
        } else if (e.getSource() == reset) {
            reset();
        } else if (e.getSource() == widgetVisibility) {
            setWidgetVisibility(!widgetsVisible);
            //wslPlayer.recordActionPerformed("widgetVisibility", String.valueOf(widgetsVisible));
        } else if (e.getSource() == scrapRays) {
            //wslPlayer.recordActionPerformed("new_rays");
            createRays();
        } else {
            System.err.println("Geometrical::actionPerformed(): unexpected " + e);
        }
    }

    public void invalidEvent(String node, String event) {
        getStatusBar().setWarningText("Error loading module; see Java Console for details.");
        setEnabled(false);
    }

    public void stateChanged(StateButton button, int k) {
        if (button == sourceToggle) {
            theSource.setVisible(k == 0);
        } else if (button == widgetVisibility) {
            setWidgetVisibility(k == 0);
        }
    }

    //Only one box, the density field
    public void numChanged(NumberBox source, Number newVal) {
        getStatusBar().clearWarning();
        rayDensity = newVal.intValue();
        //wslPlayer.recordActionPerformed("quality", newVal.toString());
        createRays();
    }

    public void invalidEntry(NumberBox source, Number badVal) {
        getStatusBar().setWarningText("Invalid ray density (use an integer between " + MIN_DENSITY +
                                 " and " + MAX_DENSITY + ").");
    }

    public void boundsForcedChange(NumberBox source, Number oldVal) {} //doesn't happen

    public void readableFieldChanged(X3DFieldEvent e) {
        int mode = ((Integer) e.getData()).intValue();
        if (mode == SOURCE_SCREEN) {
            if (draggingWidget) {
                return;
            }
            float[] pos = null;
            ((SFVec3f) e.getSource()).getValue(pos);
            float x = FPRound.toFixVal(pos[0], 2);
            float y = FPRound.toFixVal(pos[1], 2);
            float z = 0;
            if (theSource.getSourceType() == theSource.ROTATING_SRC) { // Rotation Source
                float rotAngle = theSource.getRotationAngle();
                z = y * (float) Math.sin(rotAngle);
                y = y * (float) Math.cos(rotAngle);
                getStatusBar().setText("{x,y,z}={" + x + "," + FPRound.toFixVal(y, 2) + "," +
                                  FPRound.toDelExpVal(z, 2) + "}");
            } else {
                getStatusBar().setText("{x,y}={" + x + "," + y + "," + z + "}");
            }
            return;
        }
        else {
            setDragging(((SFBool) e.getSource()).getValue());
        }
    }


    private OpticalElement getElement(String target) {
        if (target != null) { // no sense looking otherwise
            OpticalBench.Enumeration enumeration = bench.elements();
            while (enumeration.hasMoreElements()) {
                OpticalElement oe = enumeration.nextElement();
                //if((oe.getNamePrefix()+String.valueOf(oe.getID())).equals(target) )
                if (oe.getName().equals(target)) {
                    return oe;
                }
            }
        }
        return null;
    }

    protected String getModuleName() {
        return "Geometrical Optics";
    }

    protected int getMajorVersion() {
        return 6;
    }

    protected int getMinorVersion() {
        return 1;
    }

    protected int getRevision() {
        return 1;
    }

    protected String getDate() {
        return "Jul 9 2006";
    }

    protected String getAuthor() {
        return "Peter Gilbert";
    }

    protected Component getFirstFocus() {
        return null;
    }

    protected void setupGUI() {
        bench = new OpticalBench();

        autoTrace = false;
        rays = new RayList();
        virtualRays = new RayList();
        lastLensS = new float[2];
        lastLensIndex = 0;
        lastLensOpIndex = 0;

        draggingWidget = false;

        densityField = new IntBox(1, MAX_DENSITY, rayDensity, 1);
        elementsLayout = new CardLayout();
        raysPanel = new JPanel(new VerticalLayout(0));
        sourcePanel = new JPanel(); //source and
        densityPanel = new JPanel(); //quality settings
        resetPanel = new JPanel();
        newRaysPanel = new JPanel();
        rePanel = new JPanel(new VerticalLayout()); //reset/recalculate buttons
        elementsPanel = new JPanel(new VerticalLayout()); //optical element settings:
        addSelectPanel = new JPanel(); //adding and selecting elements
        elementSlotPanel = new JPanel(elementsLayout); //one element's settings

        addElement = new JButton("Add");
        removeElement = new JButton("Remove");
        reset = new JButton("Reset");
        scrapRays = new JButton("New Rays");
        //widgetVisibility = new Button("Widgets");
        widgetVisibility = new StateButton("Widgets", new String[] {"Hide ", "Show "},
                                           new String[] {"", ""});
        sourceToggle = new StateButton("", new String[] {"Off", "On"}, new String[] {"", ""});

        String[] elementTypes = {"Converging Lens ", "Diverging Lens", "Stop",
                                "Donut Stop", "Ground Glass Screen", "Magic Screen",
                                "Unknown Lens A", "Unknown Lens B", "Unknown Lens C",
                                "Unknown Lens D", "Unknown Lens E", "Unknown Lens F",
                                "Unknown Lens G", "Unknown Lens H", "Unknown Lens I",
                                "Unknown Lens J"};
        elementType = new JComboBox(elementTypes);

        elementSelector = new JComboBox();
        elementSelector.addActionListener(this);

        String[] sourceTypes = {"Single On Axis Pt", "Single Off Axis Pt", "Small T",
                               "Medium T", "Large T", "F", "Point at Infinity",
                               "T at Infinity", "Rotation Source", "Micro Source",
                               "Depth Source"};
        sourceType = new JComboBox(sourceTypes);
        sourceType.setSelectedIndex(Source.SMALL_T);
        sourceType.addActionListener(this);

        String[] densities = {"Fastest", "Fast", "Medium", "High", "Very High", "Custom..."};
        densitySelector = new JComboBox(densities);
        densitySelector.addActionListener(this);
        densitySelector.setSelectedIndex(DEF_DENSITY_INDEX);

        //System.out.print(WApplication.INTRODUCTION);

        controlPanel.setFont(new Font("Helvetica", Font.PLAIN, 11));
        //setBackground(BACKGROUND);
        //setForeground(FOREGROUND);
        controlPanel.setLayout(new BorderLayout());

        //wslPlayer = new WSLPlayer(this);
        //wslPlayer.addListener(this);
        //wslPanel = new WSLPanel(wslPlayer);

        densityPanel.add(new JLabel("Image quality:", Label.RIGHT));
        densityPanel.add(densitySelector);
        densityPanel.add(densityField);

        sourcePanel.add(new JLabel("Source:", Label.RIGHT));
        sourcePanel.add(sourceType);
        raysPanel.add(sourcePanel);
        raysPanel.add(densityPanel);

        densityField.addNumberListener(this);
        densityField.setEnabled(false);

        resetPanel.add(reset);
        //newRaysPanel.add(scrapRays);
        //newRaysPanel.add(hideWidgets);
        //rePanel.add(reset);
        //rePanel.add(scrapRays);
        rePanel.add(resetPanel);
        //rePanel.add(newRaysPanel);
        rePanel.add(widgetVisibility);
        reset.addActionListener(this);
        //scrapRays.addActionListener(this);
        widgetVisibility.addActionListener(this);

        //elementType.addItem("Observation Screen");

        addSelectPanel.add(elementType);
        addSelectPanel.add(addElement);
        addSelectPanel.add(new JLabel("Selected:", Label.RIGHT));
        addSelectPanel.add(elementSelector);
        addSelectPanel.add(removeElement);
        elementsPanel.add(addSelectPanel);
        elementsPanel.add(elementSlotPanel);
        addElement.addActionListener(this);
        removeElement.addActionListener(this);

        //statusBar.setForeground(STATUS_FOREGROUND);
        //statusBar.setPreferredSize(new Dimension(getSize().width, 18));

        controlPanel.add(raysPanel, BorderLayout.WEST);
        controlPanel.add(rePanel, BorderLayout.CENTER);
        controlPanel.add(elementsPanel, BorderLayout.EAST);

        JPanel bottom = new JPanel(new VerticalLayout());
        //bottom.add(statusBar);
        //bottom.add(wslPanel);
        controlPanel.add(bottom, BorderLayout.SOUTH);

        DebugPrinter.println("init(): done");

        start();
    }

    protected void setupX3D() {
        rayDensity = RAY_DENSITIES[DEF_DENSITY_INDEX];
        theSource = new Source(this);
    }

    protected void setDefaults() {
    }

    public static void main(String[] args) {
        Geometrical geometrical = new Geometrical("Geometrical Optics",
                                                  "geometrical.x3dv");
    }

    protected void setupMenubar() {
    }

    public void toolTip(Tooltip src, String tip) {
    }

    public void mouseEntered(Widget src) {
    }

    public void mouseExited(Widget src) {
    }

    public void mousePressed(Widget src) {
    }

    public void mouseReleased(Widget src) {
    }

	@Override
	protected void toWSLNode(WSLNode node) {
		// TODO Auto-generated method stub
		
	}

	public String getWSLModuleName() {
		// TODO Auto-generated method stub
		return null;
	}
    
    

    //=============
// WSL Methods
//=============

    /*public String getWSLModuleName() {
        return "geometrical";
             }*/

    /*public WSLPlayer getWSLPlayer() {
        return wslPlayer;
             }*/

    /*public void initialize(WSLScriptEvent event) {
        clear();

        WSLNode node = event.getNode();

        autoTrace = false;

        WSLNode sourcesNode = node.getNode("elements");
        for (int i = 0; i < sourcesNode.getChildCount(); i++) {
            addElement(sourcesNode.getChild(i));
        }

        WSLAttributeList init = event.getNode().getAttributes();
        for (int i = 0; i < init.getLength(); i++) {
            setParameter(null, init.getName(i), init.getValue(i));
        }

        autoTrace = true;
        createRays();
         }*/

    /*private void addElement(WSLNode node) {
        WSLAttributeList atts = node.getAttributes();
        float diameter;
        float focus;
        float position;
        scriptAdding = true;

        position = atts.getFloatValue("position", getNextPosition());

        //OpticalElement oe;

        if (node.getName().equalsIgnoreCase("converginglens")) {
            diameter = atts.getFloatValue("diameter", CircularElement.DEF_DIAMETER);
            focus = atts.getFloatValue("focus", ThinLens.DEF_FOCUS);
            ConvergingLens oe = new ConvergingLens(this, position);
            oe.setID(atts.getValue("id"));
            update(oe, OpticalElement.CREATED);
            oe.setDiameter(diameter);
            oe.setFocus(focus);
            if (wslPlayer.isPlaying()) {
                oe.setEnabled(false);
            }
        } else if (node.getName().equalsIgnoreCase("diverginglens")) {
            diameter = atts.getFloatValue("diameter", CircularElement.DEF_DIAMETER);
            focus = atts.getFloatValue("focus", ThinLens.DEF_FOCUS);
            DivergingLens oe = new DivergingLens(this, position);
            oe.setID(atts.getValue("id"));
            update(oe, OpticalElement.CREATED);
            oe.setDiameter(diameter);
            oe.setFocus( -focus);
            if (wslPlayer.isPlaying()) {
                oe.setEnabled(false);
            }
        } else if (node.getName().equalsIgnoreCase("unknownlens")) {
            diameter = atts.getFloatValue("diameter", CircularElement.DEF_DIAMETER);
            focus = atts.getFloatValue("focus", ThinLens.DEF_FOCUS);
            int index = atts.getIntValue("index", 0);
            UnknownLens oe;
            if (index <= 4) {
                oe = new UnknownLens(this, getNextPosition(), index,
     "lensGeometry ConvexLens { resolution 40 depthResolution 10 }");
            } else {
                oe = new UnknownLens(this, getNextPosition(), elementType.getSelectedIndex() - 4,
     "lensGeometry ConcaveLens { resolution 40 depthResolution 10 } focusRotation 0 1 0 3.14159265");
            }
            oe.setID(atts.getValue("id"));
            update(oe, OpticalElement.CREATED);
            oe.setDiameter(diameter);
            if (wslPlayer.isPlaying()) {
                oe.setEnabled(false);
            }
        } else if (node.getName().equalsIgnoreCase("groundglassscreen")) {
            float width = atts.getFloatValue("width", GroundGlassScreen.DEF_WIDTH),
                          height = atts.getFloatValue("height", GroundGlassScreen.DEF_HEIGHT),
                                   transparency = atts.getFloatValue("transparency",
                    GroundGlassScreen.DEF_TRANS);
            GroundGlassScreen oe = new GroundGlassScreen(this, position);
            oe.setID(atts.getValue("id"));
            update(oe, OpticalElement.CREATED);
            oe.setSigSize(new float[] {width, height});
            oe.setTransparency(transparency);
            if (wslPlayer.isPlaying()) {
                oe.setEnabled(false);
            }
            theGGScreen = oe;
        } else if (node.getName().equalsIgnoreCase("magicscreen")) {
            float width = atts.getFloatValue("width", MagicScreen.DEF_WIDTH),
                          height = atts.getFloatValue("height", MagicScreen.DEF_HEIGHT),
                                   transparency = atts.getFloatValue("transparency",
                    MagicScreen.DEF_TRANS);
            MagicScreen oe = new MagicScreen(this, position);
            oe.setID(atts.getValue("id"));
            update(oe, OpticalElement.CREATED);
            oe.setSigSize(new float[] {width, height});
            oe.setTransparency(transparency);
            if (wslPlayer.isPlaying()) {
                oe.setEnabled(false);
            }
            theMagicScreen = oe;
        } else if (node.getName().equalsIgnoreCase("observationscreen")) {
            float width = atts.getFloatValue("width", ObservationScreen.DEF_WIDTH),
                          height = atts.getFloatValue("height", ObservationScreen.DEF_HEIGHT),
                                   transparency = atts.getFloatValue("transparency",
                    ObservationScreen.DEF_TRANS);
            ObservationScreen oe = new ObservationScreen(this, position);
            oe.setID(atts.getValue("id"));
            update(oe, OpticalElement.CREATED);
            oe.setSigSize(new float[] {width, height});
            oe.setTransparency(transparency);
            if (wslPlayer.isPlaying()) {
                oe.setEnabled(false);
            }
            theScreen = oe;
        } else if (node.getName().equalsIgnoreCase("stop")) {
            diameter = atts.getFloatValue("diameter", CircularElement.DEF_DIAMETER);
            Stop oe = new Stop(this, position);
            oe.setID(atts.getValue("id"));
            update(oe, OpticalElement.CREATED);
            oe.setDiameter(diameter);
            if (wslPlayer.isPlaying()) {
                oe.setEnabled(false);
            }
        } else if (node.getName().equalsIgnoreCase("donutStop")) {
            diameter = atts.getFloatValue("diameter", CircularElement.DEF_DIAMETER);
     float donutDiameter = atts.getFloatValue("donutDiameter", DonutStop.DEF_DONUT_DIAMETER);
            DonutStop oe = new DonutStop(this, position);
            oe.setID(atts.getValue("id"));
            update(oe, OpticalElement.CREATED);
            oe.setDiameter(diameter);
            oe.setDonutDiameter(donutDiameter);
            if (wslPlayer.isPlaying()) {
                oe.setEnabled(false);
            }
        }
        scriptAdding = false;
         }*/

    /*public WSLNode toWSLNode() {
        WSLNode node = new WSLNode("geometrical");
        final WSLAttributeList atts = node.getAttributes();

        atts.add("quality", densityField.getText());
        atts.add("source", String.valueOf(theSource.getSourceType()));
        if (theSource.getSourceType() == theSource.ROTATING_SRC) {
            atts.add("rotationsourceangle", String.valueOf(theSource.getRotationAngle()));
        }
        atts.add("selected", String.valueOf(getActiveElement().getID()));
        atts.add("widgetVisibility", String.valueOf(widgetsVisible));
        WSLNode elementNodes = new WSLNode("elements");
        OpticalBench.Enumeration enum = bench.elements();

        while (enum.hasMoreElements()) {
            OpticalElement oe = enum.nextElement();
            elementNodes.addChild(oe.toWSLNode());
        }
        node.addChild(elementNodes);
        nps.addTo(node);
        sourceToggleScripter.addTo(node);
        return node;
         }*/

    /*public void playerStateChanged(WSLPlayerEvent event) {
        OpticalBench.Enumeration enum = bench.elements();
        final int id = event.getID();
        switch (id) {
        case WSLPlayerEvent.PLAYER_STARTED:
            rePanel.setEnabled(false);
            elementsPanel.setEnabled(false);
            sourcePanel.setEnabled(false);
            theSource.setWheelEnabled(false);
            while (enum.hasMoreElements()) {
                OpticalElement oe = enum.nextElement();
                oe.setEnabled(false);
            }
            break;
        case WSLPlayerEvent.PLAYER_STOPPED:
            rePanel.setEnabled(true);
            elementsPanel.setEnabled(true);
            sourcePanel.setEnabled(true);
            theSource.setWheelEnabled(true);
            while (enum.hasMoreElements()) {
                OpticalElement oe = enum.nextElement();
                oe.setEnabled(true);
            }
        }
         }*/

    /*public void scriptActionFired(WSLScriptEvent event) {
        OpticalElement oe = getElement(event.getTarget());
        final int id = event.getID();
        switch (id) {
        case WSLScriptEvent.ACTION_PERFORMED:
        case WSLScriptEvent.MOUSE_DRAGGED:
            setParameter(event.getTarget(), event.getParameter(), event.getValue());
            break;
        case WSLScriptEvent.MOUSE_PRESSED:
        case WSLScriptEvent.MOUSE_RELEASED:
            break;
        case WSLScriptEvent.MOUSE_ENTERED:
            if (oe != null) {
                setActiveElement(oe);
            }
            break;
        case WSLScriptEvent.OBJECT_ADDED:
            addElement(event.getNode().getChild(0));
            break;
        case WSLScriptEvent.OBJECT_REMOVED:
            if (oe != null) {
                int index = bench.indexOf(oe);
                unregisterElement(oe);
                if (bench.size() > 0) {
                    setActiveElement(bench.elementAt(Math.min(index, bench.size() - 1)));
                    update(oe, OpticalElement.REMOVED);
                }
            }
        }
         }*/

    /*private void setParameter(String target, String param, String value) {
        float fval = WTString.toFloat(value, Float.NaN);
        int ival = WTString.toInt(value, 2);
        boolean bval = WTString.toBoolean(value, true);
        OpticalElement oe = getElement(target); //will be null if no such element

        if ("quality".equalsIgnoreCase(param)) {
            densityField.setEnabled(true);
            densitySelector.select(CUSTOM_DENSITY);
            for (int i = 0; i < RAY_DENSITIES.length; ++i) {
                if (ival == RAY_DENSITIES[i]) {
                    densitySelector.select(i);
                    densityField.setEnabled(false);
                    break;
                }
            }
            densityField.setValue(ival);
        } else if ("source".equalsIgnoreCase(param)) {
            setSourceType(ival);
            sourceType.select(ival);
            setActiveElement(getActiveElement());
        } else if ("selected".equalsIgnoreCase(param)) {
            OpticalBench.Enumeration enum = bench.elements();
            while (enum.hasMoreElements()) {
                oe = enum.nextElement();
                if (oe.getID() == ival) {
                    setActiveElement(oe);
                }
            }
        } else if ("widgetVisibility".equalsIgnoreCase(param)) {
            setWidgetVisibility(bval);
        } else if ("select".equalsIgnoreCase(param)) {
            if (oe != null) {
                setActiveElement(oe);
            }
        } else if ("new_rays".equalsIgnoreCase(param)) {
            createRays();
        } else if ("position".equalsIgnoreCase(param)) {
            if (oe != null) {
                oe.setPosition(fval);
            }
        }
        //The instanceof checks here guarantee that oe!=null if they pass
        else if ("width".equalsIgnoreCase(param)) {
            if (oe instanceof ObservationScreen) {
                ((ObservationScreen) oe).setWidth(fval);
            } else if (oe instanceof GroundGlassScreen) {
                ((GroundGlassScreen) oe).setWidth(fval);
            } else if (oe instanceof MagicScreen) {
                ((MagicScreen) oe).setWidth(fval);
            }
        } else if ("height".equalsIgnoreCase(param)) {
            if (oe instanceof ObservationScreen) {
                ((ObservationScreen) oe).setHeight(fval);
            } else if (oe instanceof GroundGlassScreen) {
                ((GroundGlassScreen) oe).setHeight(fval);
            } else if (oe instanceof MagicScreen) {
                ((MagicScreen) oe).setHeight(fval);
            }
        } else if ("transparency".equalsIgnoreCase(param)) {
            if (oe instanceof ObservationScreen) {
                ((ObservationScreen) oe).setTransparency(fval);
            } else if (oe instanceof GroundGlassScreen) {
                ((GroundGlassScreen) oe).setTransparency(fval);
            } else if (oe instanceof MagicScreen) {
                ((MagicScreen) oe).setTransparency(fval);
            }
        } else if ("diameter".equalsIgnoreCase(param)) {
            if (oe instanceof CircularElement) {
                ((CircularElement) oe).setDiameter(fval);
            }
        } else if ("donutDiameter".equalsIgnoreCase(param)) {
            if (oe instanceof DonutStop) {
                ((DonutStop) oe).setDonutDiameter(fval);
            }
        } else if ("focus".equalsIgnoreCase(param)) {
            if (oe instanceof ThinLens) {
                ((ThinLens) oe).setFocus(fval);
            } else if (oe instanceof UnknownLens) {
                ((UnknownLens) oe).guessFocus(fval);
            }
        } else if ("rotationsourceangle".equalsIgnoreCase(param)) {
            theSource.setRotationAngle(fval);
        }
         }*/
}
