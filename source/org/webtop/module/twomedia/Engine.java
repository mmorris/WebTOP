/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.twomedia;

import java.util.*;

//import vrml.external.field.*;
//import vrml.external.*;
//import vrml.external.exception.*;

import org.webtop.component.*;
//import org.webtop.wsl.client.*;
//import org.webtop.wsl.script.*;
//import org.webtop.wsl.event.*;

import org.webtop.util.*;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;


public class Engine extends Thread
	implements org.webtop.module.twomedia.PoolController, Runnable {
	private final SAI sai;
	private final TwoMedia applet;
	private final SourcePanel sourcePanel;
	private final Controls controls;
	private final StatusBar statusBar;

	private final SFInt32 WidgetWhichChoiceEI;
	private final SFInt32 arrowsVisible;
	private final MFInt32 coordIndex;
	private final MFVec3f coords;

	private float t = 0;
	private float tStep = 0.5f;
	private boolean done;
	private volatile boolean playing = false;
	private volatile boolean wasPlaying = false;
	private volatile boolean dragging = false;

	// Wave Sources
	private Vector sources;
	private float height[];
	private float sparseHeight[];
	private float dividerCoords[][];
	private int dividerCoordIndex[];
	private int dividerCoordIndexSparse[];
	private Pool pool;
	private int resolution;
	private boolean normalPerVertex;
	private float spacing;
	private int sparseResolution;
	private float sparseSpacing;

	private int linearLayout = 1;
	private int linearCount = 0;
	private int radialCount = 0;


	public static final int INCIDENT_AND_REFLECTED = 0;
	public static final int INCIDENT_ONLY = 1;
	public static final int REFLECTED_ONLY = 2;

	private int visible = INCIDENT_AND_REFLECTED;

	private int staticResolution = 200;
	private int animResolution = 100;

	private boolean staticNormalPerVertex = true;
	private boolean animNormalPerVertex = true;

	private boolean autoUpdate = false;

	private final static int updateInterval = 50;
	private final static int minUpdateInterval = 10;

	public Engine(TwoMedia w) {
		super("TwoMedia Animation Thread");

		//DEBUG:
		//ThreadWatch.add(this);

		applet = w;
		sai=applet.getSAI();
		sourcePanel = w.getSourcePanel();
		sourcePanel.setEngine(this);
		controls = w.getControls();
		controls.setEngine(this);
		statusBar = w.getStatusBar();
		WidgetWhichChoiceEI = (SFInt32) sai.getInputField("Widget-SWITCH","whichChoice");
		arrowsVisible = (SFInt32) sai.getInputField("Arrows-SWITCH","whichChoice");
		coordIndex = (MFInt32) sai.getInputField("Divider","set_coordIndex");
		coords = (MFVec3f) sai.getInputField("Dividing-COORD","set_point");

		sources = new Vector();

		pool = new Pool(sai,100);
		resolution = pool.getResolution();
		spacing = pool.getSpacing();
		sparseResolution = pool.getSparseResolution();
		sparseSpacing = pool.getSparseSpacing();

		height = new float[resolution*resolution];
		sparseHeight = new float[sparseResolution*sparseResolution];

		sourcePanel.setAutoSelect(false);

		addSource(4, 16, 0, (float) (45 * Math.PI / 180), 1, 1.5f);

		sourcePanel.setAutoSelect(true);
		sourcePanel.show(0);

		autoUpdate = true;
		update();
	}

	public void setVectorsVisible(boolean vis) {SAI.setDraw(arrowsVisible,vis);}

	public void setWhichWavesAreVisible(int which) {
		visible = which;
		//this is a hack, but it might work... [Davis]
		if(!playing) update();
	}

	public void update() {
		//DebugPrinter.println(sources.size());
		int i, p;
		int u, v;
		float x, y;
		dividerCoords = new float[resolution][3];
		dividerCoordIndex = new int[resolution];
		LinearSource source;
		if(sources.size() == 0)
			return;
		source = (LinearSource) sources.elementAt(0);

		p = 0;

		//This big decision tree needs to be refactored some [Davis]
		if(pool.getRenderingMode()==Pool.FULL) {
			if(visible == INCIDENT_AND_REFLECTED) {
				//if(!playing) statusBar.setText("Calculating...");
				for(y=50, p=0, v=0; v<resolution; v++, y-=spacing) {
					for(x=-50, u=0; u<resolution; u++, x+=spacing, p++) {
						height[p] = 0;

						if(source.isTIR()) {
							if(x < 0) {
								height[p] = source.getIncidentValue(x, y, t)  + source.getReflectedValueWithTIR(x, y, t);
								//System.out.println(source.getReflectedValueWithTIR(x, y, t));
							} else
								height[p] = source.getTransmittedEvanescentWaves(x, y, t);
						} else {		//Normal refraction case
							if(x < 0)
								height[p] = source.getIncidentValue(x, y, t) + source.getReflectedValue(x, y, t);
							else
								height[p] = source.getTransmittedValue(x, y, t);
						}
					}
				}
			} else if(visible == INCIDENT_ONLY) {
				//if(!playing) statusBar.setText("Calculating...");
				for(y=50, p=0, v=0; v<resolution; v++, y-=spacing) {
					for(x=-50, u=0; u<resolution; u++, x+=spacing, p++) {
						height[p] = 0;
						if(source.isTIR()) {
							if(x < 0)
								height[p] = source.getIncidentValue(x, y, t);

							else
								height[p] = source.getTransmittedEvanescentWaves(x, y, t);
						} else {		//Normal refraction
							if(x < 0)
								height[p] = source.getIncidentValue(x, y, t);
							//+ source.getReflectedValue(x, y, t);
							else
								height[p] = source.getTransmittedValue(x, y, t);
						}
					}
				}
			} else {		//REFLECTED_ONLY
				//if(!playing) statusBar.setText("Calculating...");
				for(y=50, p=0, v=0; v<resolution; v++, y-=spacing) {
					for(x=-50, u=0; u<resolution; u++, x+=spacing, p++) {
						height[p] = 0;
						if(source.isTIR()) {
							if(x < 0) {
								height[p] = source.getReflectedValueWithTIR(x, y, t);
								//System.out.println(height[p]);
							} else
								height[p] = source.getTransmittedEvanescentWaves(x, y, t);
						} else {		//Normal refraction
							if(x < 0)
								height[p] = source.getReflectedValue(x, y, t);
							else
								height[p] = source.getTransmittedValue(x, y, t);
						}
					}
				}
			}

			for(i = 0; i < resolution; i++) {
				dividerCoords[i][0] = spacing;
				//+1 offset on height to fix sync issue between pool and line - MH
				dividerCoords[i][1] = -height[resolution / 2 + i * resolution + 1];
				dividerCoords[i][2] = -50 + i * spacing;
				dividerCoordIndex[i] = i;
			}

			pool.setHeight(height);

			//For white line divider
			coordIndex.setValue(dividerCoordIndex.length, dividerCoordIndex);
			coords.setValue(dividerCoords.length, dividerCoords);

			//if(!playing) statusBar.setText(null);
		} else {	//"sparse" rendering
			if(visible == INCIDENT_AND_REFLECTED) {
				for(y=50, p=0, v=0; v<sparseResolution; v++, y-=sparseSpacing) {
					for(x=-50, u=0; u<sparseResolution; u++, x+=sparseSpacing, p++) {
						sparseHeight[p] = 0;

						if(source.isTIR()) {
							if(x < 0)
								sparseHeight[p] = source.getIncidentValue(x, y, t) + source.getReflectedValueWithTIR(x, y, t);
							else
								sparseHeight[p] = source.getTransmittedEvanescentWaves(x, y, t);
						} else {		//Normal refraction
							if(x < 0)
								sparseHeight[p] = source.getIncidentValue(x, y, t) + source.getReflectedValue(x, y, t);
							else
								sparseHeight[p] = source.getTransmittedValue(x, y, t);
						}
					}
				}
			} else if(visible == INCIDENT_ONLY) {
				for(y=50, p=0, v=0; v<sparseResolution; v++, y-=sparseSpacing) {
					for(x=-50, u=0; u<sparseResolution; u++, x+=sparseSpacing, p++) {
						sparseHeight[p] = 0;
						if(source.isTIR()) {
							if(x < 0)
								sparseHeight[p] = source.getIncidentValue(x, y, t);
							else
								sparseHeight[p] = source.getTransmittedEvanescentWaves(x, y, t);
						} else {		//Normal refraction
							if(x < 0)
								sparseHeight[p] = source.getIncidentValue(x, y, t);
							//+ source.getReflectedValue(x, y, t);
							else
								sparseHeight[p] = source.getTransmittedValue(x, y, t);
						}
					}
				}
			} else {		//REFLECTED_ONLY
				for(y=50, p=0, v=0; v<sparseResolution; v++, y-=sparseSpacing) {
					for(x=-50, u=0; u<sparseResolution; u++, x+=sparseSpacing, p++) {
						sparseHeight[p] = 0;
						if(source.isTIR()) {
							if(x < 0)
								sparseHeight[p] = source.getReflectedValueWithTIR(x, y, t);
							else
								sparseHeight[p] = source.getTransmittedEvanescentWaves(x, y, t);
						} else {
							if(x < 0)
								sparseHeight[p] = source.getReflectedValue(x, y, t);
							else
								sparseHeight[p] = source.getTransmittedValue(x, y, t);
						}
					}
				}
			}

			dividerCoordIndexSparse = new int[sparseResolution];

			for(i = 0; i < sparseResolution; i++) {
				//dividerCoords[i][0] = -sparseSpacing;
				dividerCoords[i][0] = spacing;
				//+1 offset on height to fix sync issue between pool and line - MH
				dividerCoords[i][1] = -sparseHeight[sparseResolution / 2 + i * sparseResolution + 1];
				dividerCoords[i][2] = -50 + i * sparseSpacing;
				dividerCoordIndexSparse[i] = i;
			}

			pool.setHeight(sparseHeight);

			//For white line divider
			coordIndex.setValue(dividerCoordIndexSparse.length, dividerCoordIndexSparse);
			coords.setValue(dividerCoords.length, dividerCoords);
		}

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
			catch(InterruptedException e) {return;}
			t += tStep;
		}
	}

	public void play() {
		//This is something of a kludge; the engine thread is holding the
		//lock on itself indefinitely when running.	 Thus we may safely
		//synchronize on it only if the flag is already set to pause, so
		//that it will stop soon if it's not stopped already.	 This is
		//probably not quite safe anyway, because our access to the flag
		//is not synchronized.	But it should work for now.	 [Davis]
		//Update: now the threads are barely synchronized at all.	 Also bad.	[Davis]
		if(playing) return;
		//DebugPrinter.println(Thread.currentThread()+" : "+new Date()+": notify()ing");
		wasPlaying = playing;
		playing = true;
		//applet.getControls().setPlaying(true);
		synchronized(this) {notify();}
	}

	public void pause() {
		//DebugPrinter.println(Thread.currentThread()+" : "+new Date()+": pause()");
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

	public void exit() {done = true;}

	public boolean isPlaying() {return playing;}

	public void setResolution(int res, boolean normal) {
		if(res<50 || res>400) return;
		if(playing) pause();

		statusBar.setText("Working...");

		height = null;
		height = new float[res*res];
		pool.setOptions(res, normal);
		resolution = pool.getResolution();
		normalPerVertex = pool.getNormalPerVertex();
		spacing = pool.getSpacing();

		statusBar.setText(null);

		controls.setResolution(res, normalPerVertex);

		if(!wasPlaying && autoUpdate) update();
		else play();
	}

	public int getResolution() {return resolution;}

	public void setNormalPerVertex(boolean normal) {
		if(playing) pause();
		statusBar.setText("Working...");
		pool.setNormalPerVertex(normal);
		normalPerVertex = pool.getNormalPerVertex();
		statusBar.setText(null);
		if(!wasPlaying && autoUpdate) update();
		else play();
	}

	public boolean getNormalPerVertex() {return pool.getNormalPerVertex();}

	public void setPoolOptions(int res, boolean normalPerVertex) {
		pause();
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

	public SAI getEAI() {return sai;}


	public LinearSource addSource(float amplitude, float wavelength, float phase, float angle, float ni, float nt) {
		float x;
		float y;
		float widgetSpacing;
		int i;

		linearCount++;
		widgetSpacing = 100f / (linearCount+1);
		switch(linearLayout) {
		case 1:
			x = -50;
			y = -50 + (linearCount) * widgetSpacing;
			break;
		case 2:
			x = -50 + (linearCount) * widgetSpacing;
			y = 50;
			break;
		case 3:
			x = 50;
			y = -50 + (linearCount) * widgetSpacing;
			break;
		case 4:
			x = -50 + (linearCount) * widgetSpacing;
			y = -50;
			break;
		default:
			x = -50;
			y = -50 + (linearCount) * widgetSpacing;
			break;
		}

		LinearSource s = new LinearSource(this, sourcePanel, statusBar, amplitude, wavelength, phase, x, y, angle, ni, nt);
		s.setID("line" + linearCount);
		sources.addElement(s);

		if(linearCount==1) controls.setLayoutButtonsEnabled(true);
		arrangeLinearWidgets();
		sourcePanel.addSource(s);

		if(!playing && autoUpdate) update();
		return s;
	}

	public boolean removeSource(int i) {
		try {
			WaveSource source = (WaveSource) sources.elementAt(i);
			boolean isLinear = source instanceof LinearSource;
			sources.removeElementAt(i);
			source.destroy();
			if(isLinear) {
				linearCount--;
				if(linearCount==0) controls.setLayoutButtonsEnabled(false);
				else arrangeLinearWidgets();
			} else radialCount--;
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
				if(isLinear) {
					linearCount--;
					if(linearCount==0) controls.setLayoutButtonsEnabled(false);
					else arrangeLinearWidgets();
				} else radialCount--;
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
				final LinearSource ls=(LinearSource)sources.elementAt(i);
				switch (linearLayout) {
				case 1:
					ls.setXY(-50, -50+j*widgetSpacing, true);
					break;
				case 2:
					ls.setXY(-50+j*widgetSpacing, 50, true);
					break;
				case 3:
					ls.setXY(50, -50+j*widgetSpacing, true);
					break;
				case 4:
					ls.setXY(-50+j*widgetSpacing, -50, true);
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
			((WaveSource) sources.elementAt(i)).destroy();

		sources = new Vector();
		autoUpdate = false;
		linearLayout = 1;
		linearCount = 0;
		radialCount = 0;
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

		addSource(4, 16, 0, (float) (45 * Math.PI / 180), 1, 1.5f);

		sourcePanel.setAutoSelect(true);
		sourcePanel.show(0);

		visible = INCIDENT_AND_REFLECTED;

		autoUpdate = true;

		update();
	}

}
