/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.threemedia;

import java.util.*;

import org.sdl.math.Complex;
import org.web3d.x3d.sai.MFInt32;
import org.web3d.x3d.sai.MFVec3f;
import org.web3d.x3d.sai.SFBool;
import org.web3d.x3d.sai.SFFloat;
import org.web3d.x3d.sai.SFInt32;
import org.web3d.x3d.sai.X3DFieldEventListener;
import org.web3d.x3d.sai.X3DFieldEvent;
import org.webtop.util.DebugPrinter;
import org.webtop.x3d.SAI;
import org.webtop.x3d.X3DObject;
import org.webtop.component.*;
import org.webtop.module.threemedia.Pool;
import org.web3d.x3d.sai.*;

public class Engine extends Thread 
   implements org.webtop.module.threemedia.PoolController, Runnable /*,X3DFieldEventListener*/{
	
	//Instances of other classes native to ThreeMedia
	private ThreeMedia applet;
	private SAI eai;
	private SourcePanel sourcePanel;
	private Controls controls;
	private StatusBar statusBar;
	
	//X3D Variables
	private SFInt32 WidgetWhichChoiceEI;
	private SFInt32 arrowsVisible;
	private MFInt32 coordIndex;
	private MFVec3f coords;
	private SFFloat amp;
	private MFVec3f movableCoords;
	private MFInt32 movableCoordIndex;
	private SFBool draggerOver;
	
	private float t = 0;
	private float tStep = 0.5f;
	private boolean done;
	private volatile boolean playing = false;
	private volatile boolean wasPlaying = false;
	private volatile boolean dragging = false;
	
	//Thread variables and functions
	private static int threadCount = 0;
	private static synchronized int nextThreadID(){
		return threadCount++;
	}
	
	//Wave Sources
	private Vector sources;
	private float height[];
	private float sparseHeight[];
	private float dividerCoords[][];
	private int dividerCoordIndex[];
	private int dividerCoordIndexSparse[];
	private float movableDividerCoords[][];
	private int movableDividerCoordIndex[];
	private int movableDividerCoordIndexSparse[];
	private Pool pool;
	private int resolution;
	private boolean normalPerVertex;
	private float spacing;
	private int sparseResolution;
	private float sparseSpacing;
	
	private int linearLayout = 1;
	private int linearCount = 0;
	
	public static final int INCIDENT_AND_REFLECTED = 0;
	public static final int INCIDENT_ONLY = 1;
	public static final int REFLECTED_ONLY = 2;
	public static final float S = 15.0f;

	
	private int visible = INCIDENT_AND_REFLECTED;
	
	private int staticResolution = 200;
	private int animResolution = 100;

	private boolean staticNormalPerVertex = true;
	private boolean animNormalPerVertex = true;
	private int times = 0;

	private boolean autoUpdate = false;
	
	private final static int updateInterval = 50;
	private final static int minUpdateInterval = 10;
	
	public Engine(ThreeMedia w){
		super("ThreeMedia Animation Thread #" + nextThreadID());
		
		applet = w;
		eai = w.getSAI();
		sourcePanel = w.getSourcePanel();
		sourcePanel.setEngine(this);
		controls = w.getControls();
		controls.setEngine(this);
		statusBar = w.getStatusBar();
		
		WidgetWhichChoiceEI = (SFInt32) eai.getInputField("Widget-SWITCH","whichChoice");
		
		arrowsVisible = (SFInt32) eai.getInputField("Arrows-SWITCH","whichChoice");
		coordIndex = (MFInt32) eai.getInputField("Divider","set_coordIndex");
		coords = (MFVec3f) eai.getInputField("Dividing-COORD","set_point");
		movableCoords = (MFVec3f) eai.getInputField("Dividing-COORD2","set_point");
		movableCoordIndex = (MFInt32) eai.getInputField("Divider2","set_coordIndex");
		
		sources = new Vector();
		
		pool = new Pool(eai, 100);
		resolution = pool.getResolution();
		spacing = pool.getSpacing();
		sparseResolution = pool.getSparseResolution();
		sparseSpacing = pool.getSparseSpacing();
		
		height = new float[resolution*resolution];
		sparseHeight = new float[sparseResolution*sparseResolution];
		
		sourcePanel.setAutoSelect(false);
		
		addSource(4.0f, 15.0f, 0.0f, (float) (42.0f * Math.PI / 180.0f), 
				1.0f, 1.25f, 1.50f, 25.0f);
		
		sourcePanel.setAutoSelect(true);
		sourcePanel.show(0);
		
		autoUpdate = true;
		update();
		
		setVectorsVisible(true);
		
	}
	
	public void destroy() {
		DebugPrinter.println("Engine: destroyed");
		System.out.println("Engine: destroyed");
		applet = null;
		sources = null;
		pool = null;
		height = null;
		sparseHeight = null;
	}
	
		
	public void setVectorsVisible(boolean vis) {
		SAI.setDraw(arrowsVisible,vis);
	}
	
	public void setWhichWavesAreVisible(int which) {
		visible = which;
		update();
	}
	
	public  synchronized void update(){
		//System.out.println("Update: # of Sources:" + sources.size());
		
		int i, p;
		int u, v, situation;
		float x, y;
		dividerCoords = new float[resolution][3];
		dividerCoordIndex = new int[resolution];
		movableDividerCoords = new float[resolution][3];
		movableDividerCoordIndex = new int[resolution];
		LinearSource source;
		p = 0;
		float d;
		float a;
		
		if(sources.size() == 0)
			return;
		
		source = (LinearSource) sources.elementAt(0);
		d = source.getDistance();
		situation = source.getSituation();
		a = (d - 15.0f) / 20.0f;
			
		DebugPrinter.println(""+a);

		Complex[] G5s=new Complex[resolution];
		Complex[] G6s=new Complex[resolution];
		for(x=-50.0f, u=0; u<resolution; u++, x+=spacing, p++) {
			G5s[u]=source.getG5(x);
			G6s[u]=source.getG6(x);
		}
		
		///Start If Here
		if(pool.getRenderingMode()==Pool.FULL) {
			if(visible == INCIDENT_AND_REFLECTED) {
				//if(!playing) statusBar.setText("Calculating...");

				//Precalculate G5 values
				//Complex[] G5s=new Complex[resolution];
				//for(x=-50.0f, u=0; u<resolution; u++, x+=spacing, p++) G5s[u]=source.getG5(x);
				for(y=50.0f, p=0, v=0; v<resolution; v++, y-=spacing) {
					for(x=-50.0f, u=0; u<resolution; u++, x+=spacing, p++) {
							height[p] = 0;

							source.putG5(G5s[u]);//use precalc array
							source.putG6(G6s[u]);//use precalc array

							if(x < -1.0f * S)
								height[p] = source.getIncidentWaveInIncidentMedium(x, y, t, situation)
											+ source.getReflectedWaveInIncidentMedium(x, y, t, situation);
							else if(x > -1.0f * S + d)
								height[p] = source.getTransmittedWaveInThirdMedium(x,y,t,situation);
							else
								height[p] = source.getWaveInMiddleMedium(x,y,t,situation);
								//height[p] = source.getPlusWaveInMiddleMedium(x,y,t,situation) + source.getMinusWaveInMiddleMedium(x,y,t,situation);
					}
				}
			} else if(visible == INCIDENT_ONLY) {
				//if(!playing) statusBar.setText("Calculating...");
				for(y=50.0f, p=0, v=0; v<resolution; v++, y-=spacing) {
					for(x=-50.0f, u=0; u<resolution; u++, x+=spacing, p++) {
							source.putG5(G5s[u]);//use precalc array
							source.putG6(G6s[u]);//use precalc array

							height[p] = 0;
							if(x < -1.0f * S)
								height[p] = source.getIncidentWaveInIncidentMedium(x, y, t, situation);

							else if(x > -1.0f * S + d)
								height[p] = source.getTransmittedWaveInThirdMedium(x,y,t,situation);
							else
								height[p] = source.getWaveInMiddleMedium(x,y,t,situation);
								//height[p] = source.getPlusWaveInMiddleMedium(x,y,t,situation) + source.getMinusWaveInMiddleMedium(x,y,t,situation);
						}
				}
			} else {
				//if(!playing) statusBar.setText("Calculating...");
				for(y=50.0f, p=0, v=0; v<resolution; v++, y-=spacing) {
					for(x=-50.0f, u=0; u<resolution; u++, x+=spacing, p++) {
							source.putG5(G5s[u]);//use precalc array
							source.putG6(G6s[u]);//use precalc array

							height[p] = 0;
							if(x < -1.0f * S)
								height[p] = source.getReflectedWaveInIncidentMedium(x, y, t, situation);
							else if(x > -1.0f * S + d)
								height[p] = source.getTransmittedWaveInThirdMedium(x,y,t,situation);
							else
								height[p] = source.getWaveInMiddleMedium(x,y,t,situation);
								//height[p] = source.getPlusWaveInMiddleMedium(x,y,t,situation) + source.getMinusWaveInMiddleMedium(x,y,t,situation);
						}
				}
			}

			for(i = 0; i < resolution; i++) {
				dividerCoords[i][0] = -15.0f;
				//+1 offset added to fix sync issue between pool and line [JD]
				dividerCoords[i][1] = -height[resolution * 35 / 100 + i * resolution +1];
				dividerCoords[i][2] = -50.0f + i * spacing;
				dividerCoordIndex[i] = i;
				if(a<2.5f) {	//Leave the divider alone if we've taken the other end of the interface off the sheet (we'll see how much it does for us)
					movableDividerCoords[i][0] = 20 * a;
					movableDividerCoords[i][1] = -height[(int) (resolution * (50 + 20 * a) / 100) + i * resolution];
					movableDividerCoords[i][2] = -50.0f + i * spacing;
					movableDividerCoordIndex[i] = i;
				} else if(i==0) DebugPrinter.println("(d.a) "+d+'.'+a);
			}

			pool.setHeight(height);
			coordIndex.setValue(dividerCoordIndex.length, dividerCoordIndex);
			coords.setValue(dividerCoords.length, dividerCoords);
			movableCoordIndex.setValue(movableDividerCoordIndex.length, movableDividerCoordIndex);
			movableCoords.setValue(movableDividerCoords.length, movableDividerCoords);
			//if(!playing) statusBar.setText(null);
		} else {
			if(visible == INCIDENT_AND_REFLECTED) {
				for(y=50.0f, p=0, v=0; v<sparseResolution; v++, y-=sparseSpacing) {
					for(x=-50.0f, u=0; u<sparseResolution; u++, x+=sparseSpacing, p++) {
						source.putG5(G5s[u]);//use precalc array
						source.putG6(G6s[u]);//use precalc array

						sparseHeight[p] = 0;
						if(x < -1.0f * S)
							sparseHeight[p] = source.getIncidentWaveInIncidentMedium(x, y, t, situation)
												+ source.getReflectedWaveInIncidentMedium(x, y, t, situation);
						else if(x > -1.0f * S + d)
							sparseHeight[p] = source.getTransmittedWaveInThirdMedium(x,y,t,situation);
						else
							sparseHeight[p] = source.getWaveInMiddleMedium(x,y,t,situation);
							//sparseHeight[p] = source.getPlusWaveInMiddleMedium(x,y,t,situation) + source.getMinusWaveInMiddleMedium(x,y,t,situation);
					}
				}
			} else if(visible == INCIDENT_ONLY) {
				for(y=50.0f, p=0, v=0; v<sparseResolution; v++, y-=sparseSpacing) {
					for(x=-50.0f, u=0; u<sparseResolution; u++, x+=sparseSpacing, p++) {
						source.putG5(G5s[u]);//use precalc array
						source.putG6(G6s[u]);//use precalc array

						sparseHeight[p] = 0;
						if(x < -1.0f * S)
							sparseHeight[p] = source.getIncidentWaveInIncidentMedium(x, y, t, situation);

						else if(x > -1.0f * S + d)
							sparseHeight[p] = source.getTransmittedWaveInThirdMedium(x,y,t,situation);
						else
							sparseHeight[p] = source.getWaveInMiddleMedium(x,y,t,situation);
							//sparseHeight[p] = source.getPlusWaveInMiddleMedium(x,y,t,situation) + source.getMinusWaveInMiddleMedium(x,y,t,situation);
					}
				}
			} else {
				for(y=50.0f, p=0, v=0; v<sparseResolution; v++, y-=sparseSpacing) {
					for(x=-50.0f, u=0; u<sparseResolution; u++, x+=sparseSpacing, p++) {
						source.putG5(G5s[u]);//use precalc array
						source.putG6(G6s[u]);//use precalc array

						sparseHeight[p] = 0;
						if(x < -1.0f * S)
							sparseHeight[p] = source.getReflectedWaveInIncidentMedium(x, y, t, situation);
						else if(x > -1.0f * S + d)
							sparseHeight[p] = source.getTransmittedWaveInThirdMedium(x,y,t,situation);
						else
							sparseHeight[p] = source.getWaveInMiddleMedium(x,y,t,situation);
							//sparseHeight[p] = source.getPlusWaveInMiddleMedium(x,y,t,situation) + source.getMinusWaveInMiddleMedium(x,y,t,situation);
					}
				}
			}

			dividerCoordIndexSparse = new int[sparseResolution];
			movableDividerCoordIndexSparse = new int[sparseResolution];

			for(i = 0; i < sparseResolution; i++) {
				dividerCoords[i][0] = -15.0f;
				//+1 offset added to fix sync issue between line and pool [JD]
				dividerCoords[i][1] = -sparseHeight[sparseResolution * 35 / 100 + i * sparseResolution + 1];
				dividerCoords[i][2] = -50.0f + i * sparseSpacing;
				if(a<2.5f) {
				movableDividerCoords[i][0] = 20 * a;
				movableDividerCoords[i][1] = -sparseHeight[(int) (sparseResolution * (50 + 20 * a) / 100) + i * sparseResolution];
				movableDividerCoords[i][2] = -50.0f + i * sparseSpacing;
				}
				dividerCoordIndexSparse[i] = i;
				movableDividerCoordIndexSparse[i] = i;
			}

			pool.setHeight(sparseHeight);
			coordIndex.setValue(dividerCoordIndexSparse.length, dividerCoordIndexSparse);
			coords.setValue(dividerCoords.length, dividerCoords);
			movableCoordIndex.setValue(movableDividerCoordIndexSparse.length, movableDividerCoordIndexSparse);
			movableCoords.setValue(movableDividerCoords.length, movableDividerCoords);
		}
		///End If Here
		
		pool.applyRenderingMode();

		if(playing) t += tStep;
	}
	
	public void run() {
		long time;
		playing = false;
		done = false;

		while(!done) {
			time = System.currentTimeMillis();
			update();
			try {
				time = updateInterval - System.currentTimeMillis() + time;
				if(time<minUpdateInterval) time = minUpdateInterval;
				sleep(time);
				synchronized(this) {while(!playing && !done) wait();}
			}
			catch(InterruptedException e) {
				System.out.println("Error in Engine.run()");
				return;}
			t += tStep;
		}
	}
	
	public void play() {
		//See Two Media module for discussion. [Davis]
		if(playing) return;
		wasPlaying = playing;
		playing = true;
		synchronized(this) {notify();}		
	}
	
	public void pause() {
		wasPlaying = playing;
		playing = false;
	}
	
	public void prevFrame() {
		if(!playing && t-tStep>=0) {
			t -= tStep;
			update();
		}
	}
	
	public void nextFrame() {
		if(!playing) {
			t += tStep;
			update();
		}
	}
	
	public void exit() {
		destroy();
		done = true;
	}
	
	public boolean isPlaying() {return playing;}
	
	public void setResolution(int res, boolean normal) {
		if(res<50 || res>400) return;
		if(playing) pause();

		statusBar.setText("Working...");

		height = new float[res*res];
		pool.setOptions(res, normal);
		resolution = pool.getResolution();
		normalPerVertex = pool.getNormalPerVertex();
		spacing = pool.getSpacing();

		statusBar.reset();

		controls.setResolution(res, normalPerVertex);

		if(wasPlaying && autoUpdate) update();
		else play();
	}
	
	public int getResolution() {
		return resolution;
	}

	public void setNormalPerVertex(boolean normal) {
		if(playing) pause();
		statusBar.setText("Working...");
		pool.setNormalPerVertex(normal);
		normalPerVertex = pool.getNormalPerVertex();
		statusBar.setText(null);
		if(!wasPlaying && autoUpdate) update();
		else play();
	}
	
	public boolean getNormalPerVertex() {
		return pool.getNormalPerVertex();
	}

//	Implement org.webtop.module.threemedia.PoolController
	public synchronized void setPoolOptions(int res, boolean normalPerVertex) {
		if(playing) {
			wasPlaying=true;
			pause();
		} else wasPlaying=false;
		statusBar.setText("Working...");

		pool.setOptions(res, normalPerVertex);
		resolution = pool.getResolution();
		normalPerVertex = pool.getNormalPerVertex();
		spacing = pool.getSpacing();

		height = null;
		height = new float[resolution*resolution];

		statusBar.setText(null);
		if(wasPlaying) play();
		else if(autoUpdate) update();
	}
	
	public LinearSource addSource(float amplitude, float wavelength, float phase, float angle, float na, float nb, float nc, float dist) {
		float x;
		float y;
		float widgetSpacing;
		int i;

		linearCount++;
		widgetSpacing = 100.0f / (linearCount+1);
		switch(linearLayout) {
		case 1:
			x = -50.0f;
			y = -50.0f + (linearCount) * widgetSpacing;
			break;
		case 2:
			x = -50.0f + (linearCount) * widgetSpacing;
			y = 50.0f;
			break;
		case 3:
			x = 50.0f;
			y = -50.0f + (linearCount) * widgetSpacing;
			break;
		case 4:
			x = -50.0f + (linearCount) * widgetSpacing;
			y = -50.0f;
			break;
		default:
			x = -50.0f;
			y = -50.0f + (linearCount) * widgetSpacing;
			break;
		}

		LinearSource s	= new LinearSource(this,	sourcePanel, statusBar, amplitude, wavelength, phase, x, y, angle, na, nb, nc, dist);
		s.setID("line" + linearCount);
		sources.addElement(s);

		arrangeLinearWidgets();
		sourcePanel.addSource(s);

		//To update waves
		s.setDistance(dist);

		if(!playing && autoUpdate) update();
		return s;
	}
	
	public SAI getSAI()
	{
		return eai;
	}
	
	
	public boolean removeSource(int i) {
		try {
			WaveSource source = (WaveSource) sources.elementAt(i);
			boolean isLinear = source instanceof LinearSource;
			sources.removeElementAt(i);
			source.destroy();

			if(sources.size()==0) pause();
			if(!playing && autoUpdate) update();
			return true;
		}
		catch(ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}
	
	public boolean removeSource(WaveSource s) {
		if(sources.contains(s)) {
			boolean isLinear = s instanceof LinearSource;
			if(sources.removeElement(s)) {
				s.destroy();

				if(sources.size()==0) {
					pause();
				}
				if(!playing && autoUpdate) update();
				return true;
			}
		}
		return false;
	}
	
	public int sourceIndex(WaveSource s) {
		return sources.indexOf(s);
	}

	public WaveSource getSource(int index) {
		if(index<sources.size()) return (WaveSource) sources.elementAt(index);
		else return null;
	}
	
	public WaveSource getSource(String id) {
		for(int i=0; i<sources.size(); i++) {
			String ID = ((WaveSource) sources.elementAt(i)).getID();
			if(ID!=null && ID.equals(id))
				return (WaveSource)sources.elementAt(i);
		}
		return null;
	}

	public void hideWidgets() {
		for(int i=0; i<sources.size(); i++) {
			((WaveSource)sources.elementAt(i)).hideWidgets();
		}
	}
	
	public void setWidgetDragging(boolean dragging) {
		pool.setRenderingMode(dragging?Pool.SPARSE:Pool.FULL);
		if(wasPlaying && !dragging) play();
		else {
			if(dragging) pause();
			update();
		}
	}

	public int getLinearCount() {
		return linearCount;
	}
	
	private void arrangeLinearWidgets() {
		float widgetSpacing;
		int i, j;

		if(linearCount<=0) return;
		widgetSpacing = 100 / (linearCount+1);
		for(i=0, j=1; i<sources.size(); i++) {
			if(sources.elementAt(i) instanceof LinearSource) {
				switch (linearLayout) {
				case 1:
					((LinearSource)sources.elementAt(i)).setXY(-50.0f, -50+j*widgetSpacing, true);
					break;
				case 2:
					((LinearSource)sources.elementAt(i)).setXY(-50+j*widgetSpacing, 50.0f, true);
					break;
				case 3:
					((LinearSource)sources.elementAt(i)).setXY(50.0f, -50+j*widgetSpacing, true);
					break;
				case 4:
					((LinearSource)sources.elementAt(i)).setXY(-50+j*widgetSpacing, -50.0f, true);
					break;
				}
				j++;
			}
		}
	}
	
	public void setLinearLayout(int layout) {
		linearLayout = layout;
		arrangeLinearWidgets();
	}

	public void setGridVisible(boolean visible) {
		pool.setGridVisible(visible);
	}

	public void setWidgetsVisible(boolean visible) {
		WidgetWhichChoiceEI.setValue(visible ? 0 : -1);
	}

	public void selectWaveSource(WaveSource selected) {
		sourcePanel.show(selected);
	}

	public void selectWaveSource(int index) {
		sourcePanel.show(getSource(index));
	}
	
	public void reset() {
		if(playing) pause();
		wasPlaying = false;

		for(int i=0; i<sources.size(); i++)
			((X3DObject) sources.elementAt(i)).destroy();
		sources = new Vector();
		autoUpdate = false;
		linearLayout = 1;
		linearCount = 0;
		//mode = MODE_STATIC;

		pool.reset();
		sourcePanel.reset();
		controls.reset();
		statusBar.reset();

		resolution = pool.getResolution();
		spacing = pool.getSpacing();
		sparseResolution = pool.getSparseResolution();
		sparseSpacing = pool.getSparseSpacing();

		height = new float[resolution*resolution];
		sparseHeight = new float[sparseResolution*sparseResolution];

		sourcePanel.setAutoSelect(false);

		addSource(4.0f, 15.0f, 0.0f, (float) (45.0f * Math.PI / 180.0f), 1.0f, 1.25f, 1.50f, 25.0f);

		sourcePanel.setAutoSelect(true);
		sourcePanel.show(0);
		
		visible = INCIDENT_AND_REFLECTED;
		autoUpdate = true;

		update();
	}
	
	
	
	
}
