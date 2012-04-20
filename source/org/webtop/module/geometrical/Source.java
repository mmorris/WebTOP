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
import org.sdl.gui.numberbox.*;
import org.webtop.util.*;
import org.web3d.x3d.sai.*;
import org.webtop.x3d.*;
import org.webtop.x3d.output.*;

//import webtop.vrml.*;
//import webtop.wsl.script.*;
//import webtop.wsl.client.*;
//import webtop.wsl.event.*;

class Source implements X3DFieldEventListener {
    private Geometrical wapp;

    private static float[][] makeT(float l, float m) {
        return new float[][] { {0, 0, m}, {0, l, m}, {0, 2 * l, m}, { -l, 2 * l, m}, {l, 2 * l, m}
        };
    }

    private static float[][] makeF(float l, float m) {
        return new float[][] { { -0.5f * l, 0, m}, { -0.5f * l, l, m}, {0, l, m}, {0, 2 * l, m},
                {0.5f * l, 2 * l, m}, { -0.5f * l, 2 * l, m}
        };
    }

    private static float[][] makeSquare(float l) {
        return new float[][] { {0, 0, 0}, {l, 0, 0}, { -l, 0, 0}, {0, l, 0}, {0, -l, 0}, {l, l, 0},
                { -l, l, 0}, {l, -l, 0}, { -l, -l, 0}
        };
    }

    private static float[][] makeMicro(float l) {
        return new float[][] { {0, 0, 0}, {0, l, 0}
        };
    }

    private static float[][] makeDepthPoints(float l, float m) {
        return new float[][] { {l, l, -m}, {0, 0, 0}, { -l, -l, m}
        };
    }

    public static final int ONAXIS_PT = 0, OFFAXIS_PT = 1, SMALL_T = 2, MEDIUM_T = 3, LARGE_T = 4,
    MEDIUM_F = 5, PT_AT_INFY = 6, T_AT_INFY = 7, ROTATING_SRC = 8, MICRO_SRC = 9,
    DEPTH_SRC = 10, CUSTOM = 11;

    private static final int X = 0, Y = 1, Z = 2; //Indices into 3-element point float[]s

    private static final float macropoints[][][] = { { {0, 0, 0}
    }, { {1.5f, 3, 0}
    },
            makeT(.5f, 0),
            makeT(1, 0),
            makeT(1.5f, 0),
            makeF(1, 0), { {0, 0, -1e5f}
    },
            makeT(400, -1e5f),
            makeSquare(1),
            makeMicro(.1f),
            makeDepthPoints(1, 2)};
    public static final float BASE_LUMINOSITY = 300;
    //This array needs a value for the CUSTOM type, unlike the points array above
    private static final float luminosities[] = {1, 1, 1, 1, 1, 1, 11111111, 11111111, 1, .1f, 1};

    private int micropoints = 1000; //for each macropoint, increased from 100 to combat dimming at image point for large magnification
    private static final float micropointRadius[] = {0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f,
            0.1f, 0.01f, 0.1f};
    private int currentSource;
    private float previousRotationAngle = 0;
    private FloatBox fbRotationAngle = new FloatBox( -90, 90, 0, 1);
    private boolean sourceShowing = true;

    private float[][] spine, //updated whenever source changes
    points, //only updated by updateSource()
    colors;

    private float[][] spineColors = { {1f, 1f, 1f}, {1f, 1f, 0f}, {1f, 1f, 1f}, {1f, 1f, 0f}, {1f,
                                    1f, 1f}, {1f, 1f, 0f}, {1f, 1f, 0f}, {1f, 1f, 0f}, {1f, 1f, 1f}
    };
    private float[][] spineColorsDepth = { {1f, 0f, 0f}, {0f, 1f, 0f}, {0f, 0f, 1f}
    };

    //private MFVec3f set_points;
    //private MFColor set_colors;
    private SFFloat wheel_setAngle;
    private SFBool wheel_setEnabled;
    private SFInt32 source_setChoice, wheel_setChoice;

    private PointSet sourcePoints;

    private SFVec2f set_scale;
    private SFFloat set_y_translation;
    private SFFloat set_x_translation;

    public Source(Geometrical main) {
        wapp = main;

        SAI sai = main.getSAI();

        //set_points = (MFVec3f) sai.getInputField("SourcePoints", "point");
        //set_colors = (MFColor) sai.getInputField("SourceColors", "color");
        sourcePoints = new PointSet(sai, sai.getNode("SourcePoints"));
        source_setChoice = (SFInt32) sai.getInputField("SourceSwitch", "whichChoice");

        wheel_setAngle = (SFFloat) sai.getInputField("SourceWheel", "set_value");
        wheel_setEnabled = (SFBool) sai.getInputField("SourceWheel", "enabled");
        wheel_setChoice = (SFInt32) sai.getInputField("WheelSwitch", "whichChoice");

        set_scale = (SFVec2f) sai.getInputField("BoxScaler", "scale_in");
        set_y_translation = (SFFloat) sai.getInputField("BoxScaler", "translation_in_y");
        set_x_translation = (SFFloat) sai.getInputField("BoxScaler", "translation_in_x");

        //Listen to the wheel!
        sai.getOutputField("SourceWheel", "value_changed", this, null);
        fbRotationAngle.addNumberListener(new RotationAngleListener());
    }

    public void readableFieldChanged(X3DFieldEvent e) {
        float angle = ((SFFloat) e.getSource()).getValue();
        float delta = angle - previousRotationAngle;
        DebugPrinter.println("Callback :: angle = " + angle);
        //ignore lacks of change or irrelevant noise
        if (delta == 0 || currentSource != ROTATING_SRC) {
            return;
        }
        setRotationAngle(angle);

        /*
           DebugPrinter.println("Source: new rotationAngle is: " + angle + ".");

           // Modify the point set here.
           int mpc=getMicropointCount();
           for(int i=0; i<mpc; i++){
         float y = points[i][Y];
         float z = points[i][Z];

         points[i][Y] = (float)(y*Math.cos(delta) - z*Math.sin(delta));
         points[i][Z] = (float)(z*Math.cos(delta) + y*Math.sin(delta));
           }
           rotationAngle = angle;

           applet.update(null,0);
           applet.createRays();
           System.out.println("Rotation angle: " + angle);
         */
    }

    private class RotationAngleListener extends NumberBox.Adapter {
        public void numChanged(NumberBox src, Number newVal) {
            float angle = WTMath.toRads(newVal.floatValue());
            wheel_setAngle.setValue(angle);
        }

        public void invalidEntry(NumberBox src, Number badVal) {}
    };

    public void setRotationAngle(float angle) {
        //System.out.println("setRotationANgle:: angle = " + angle);
        //WSLPlayer wslPlayer = getWSLPlayer();
        //if(wslPlayer!=null)
        //	wslPlayer.recordMouseDragged("RotationSourceAngle",String.valueOf(angle));

        float delta = angle - previousRotationAngle;

        // Modify the point set here.
        int mpc = getMicropointCount();
        for (int i = 0; i < mpc; i++) {
            float y = points[i][Y];
            float z = points[i][Z];

            points[i][Y] = (float) (y * Math.cos(delta) - z * Math.sin(delta));
            points[i][Z] = (float) (z * Math.cos(delta) + y * Math.sin(delta));
        }
        fbRotationAngle.silence();
        fbRotationAngle.setSigValue(WTMath.toDegs(angle), 3);
        previousRotationAngle = angle;
        wapp.update(null, 0);
        wapp.createRays();

        //if(wslPlayer.isPlaying()) {
        //	wheel_setAngle.setValue(angle);
        //}
    }

    public float getRotationAngle() {
        return WTMath.toRads(fbRotationAngle.getValue());
    }

    public FloatBox getfbRotationAngle() {
        return fbRotationAngle;
    }

    public void setWheelEnabled(boolean enabled) {
        wheel_setEnabled.setValue(enabled);
    }

    //When not using rotating source, it ought not to be rotated!	 So call this.
    private void resetWheel() {
        wheel_setAngle.setValue(0);
    }

    public void setVisible(boolean on) {
        SAI.setDraw(source_setChoice, sourceShowing = on);
    }

    public synchronized void updateSource() {
        int mpc = getMacropointCount();
        points = new float[mpc * micropoints][];
        colors = new float[mpc * micropoints][];
        int p = 0;
        for (int i = 0; i < mpc; ++i) {
            for (int j = 0; j < micropoints; ++j) {
                //This is for 2D -- do we perhaps want 3 instead? [Davis]
                //This is also uniform over the circle -- is this the distribution of choice? [Davis]
                double r = Math.sqrt(Math.random()) * micropointRadius[currentSource],
                           theta = 2 * Math.PI * Math.random();
                float xVal = spine[i][X] + (float) (r * Math.cos(theta));
                float yVal = spine[i][Y] + (float) (r * Math.sin(theta));
                float zVal = spine[i][Z];
                if (currentSource == ROTATING_SRC) {
                    colors[p] = spineColors[i];
                } else if (currentSource == DEPTH_SRC) {
                    colors[p] = spineColorsDepth[i];
                } else {
                    colors[p] = new float[] {1f, 1f, 1f};
                }
                points[p++] = new float[] {xVal, yVal, zVal};
            }
        }
        sourcePoints.setColors(colors);
        sourcePoints.setCoords(currentSource == PT_AT_INFY ? new float[0][] : points);
        /*set_colors.setValue(mpc * micropoints, colors);
        set_points.setValue(mpc * micropoints,
                            currentSource == PT_AT_INFY ? new float[0][] : points);*/
    }

    /*
      public void setMicropointRadius(float rm) {
     if(rm<0) throw new IllegalArgumentException("rm may not be negative.");
     micropointRadius=rm;
      }
     */

    public void setMicropointRatio(int m) {
        if (m <= 0) {
            throw new IllegalArgumentException("m must be positive.");
        }
        micropoints = m;
    }

    public void setSourceType(int type) {
        //Change the size of screen used to determine mouse coordinates at the source
        fbRotationAngle.setEnabled(false);
        switch (type) {
        case ONAXIS_PT:
            set_scale.setValue(new float[] {0, 0});
            set_y_translation.setValue(0);
            set_x_translation.setValue(0);
            break;
        case OFFAXIS_PT:
            set_scale.setValue(new float[] {1, 1});
            set_y_translation.setValue(3);
            set_x_translation.setValue(1.5f);
            break;
        case SMALL_T:
            set_scale.setValue(new float[] {1.5f, 1.5f});
            set_y_translation.setValue(0.5f);
            set_x_translation.setValue(0);
            break;
        case MEDIUM_T:
            set_scale.setValue(new float[] {2.5f, 2.5f});
            set_y_translation.setValue(1);
            set_x_translation.setValue(0);
            break;
        case LARGE_T:
            set_scale.setValue(new float[] {3.5f, 3.5f});
            set_y_translation.setValue(1.5f);
            set_x_translation.setValue(0);
            break;
        case MEDIUM_F:
            set_scale.setValue(new float[] {2.5f, 2.5f});
            set_y_translation.setValue(1);
            set_x_translation.setValue(0);
            break;
        case PT_AT_INFY:
            set_scale.setValue(new float[] {0, 0});
            set_y_translation.setValue(0);
            set_x_translation.setValue(0);
            break;
        case ROTATING_SRC:
            set_scale.setValue(new float[] {2.5f, 2.5f});
            set_y_translation.setValue(0);
            set_x_translation.setValue(0);
            fbRotationAngle.setEnabled(true);
            break;
        case T_AT_INFY:
            set_scale.setValue(new float[] {0, 0});
            set_y_translation.setValue(0);
            set_x_translation.setValue(0);
            break;
        case MICRO_SRC:
            set_scale.setValue(new float[] {1, 1});
            set_y_translation.setValue(0);
            set_x_translation.setValue(0);
            break;
        case DEPTH_SRC:
            set_scale.setValue(new float[] {1, 1});
            set_y_translation.setValue(0);
            set_x_translation.setValue(0);
            break;
        default:
            throw new IllegalArgumentException("Invalid source number: " + type);
        }
        resetWheel();
        //WSLPlayer wslPlayer = getWSLPlayer();
        //if(wslPlayer!=null)
        //	wslPlayer.recordActionPerformed("source", String.valueOf(type));
        spine = macropoints[currentSource = type];
        SAI.setDraw(wheel_setChoice, type == ROTATING_SRC);
    }

    public void setSource(float s[][]) {
        spine = (float[][]) s.clone();
        currentSource = CUSTOM;
        resetWheel();
    }

    public int getSourceType() {
        return currentSource;
    }

    //Modify the return value of this function at your own risk!
    public synchronized float[][] getPoints() {
        return points;
    }

    public synchronized float[][] getColors() {
        return colors;
    }

    public synchronized float getLuminosity() {
        return BASE_LUMINOSITY * luminosities[currentSource];
    }

    public int getMicropointRatio() {
        return micropoints;
    }

    public int getMacropointCount() {
        return spine.length;
    }

    public int getMicropointCount() {
        return points.length;
    }
    //public WSLPlayer getWSLPlayer(){return applet.getWSLPlayer();}
}
