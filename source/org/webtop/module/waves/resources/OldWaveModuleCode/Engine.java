//Updated March 3 2004

package webtop.wave;

import java.util.*;

import vrml.external.field.*;

import webtop.component.*;
import webtop.util.*;
import webtop.vrml.*;
import webtop.wsl.client.*;
import webtop.wsl.script.*;
import webtop.wsl.event.*;

public class Engine extends Thread implements PoolController,WSLScriptListener,WSLPlayerListener {
	public static final int LOW_RESOLUTION=50,
													MEDIUM_RESOLUTION=100,
													HIGH_RESOLUTION=200;
	public static final boolean FASTEST_NPV=false,
															FAST_NPV=true,
															MEDIUM_NPV=true,
															SMOOTH_NPV=false,
															VERYSMOOTH_NPV=true;
	public static final float POOL_SIZE=100;	//on a side

	private final WaveSimulation applet;
	private final WidgetsPanel widgetsPanel;
	private final ControlPanel controlPanel;
	private final StatusBar statusBar;

	private final WSLPlayer wslPlayer;

	private final EventInSFInt32 widgets_setChoice;

	private static int threadCount=0;
	private static synchronized int nextThreadID() {return threadCount++;}

	private float t;
	private float tStep = 1;
	private boolean done;
	private volatile boolean playing = false;
	private volatile boolean wasPlaying = false;
	private volatile boolean dragging = false;

	//Iterator class for the widgets list
	private class WIterator {
		private Enumeration widgetsEnum;
		private boolean moreWidgets;
		public WIterator() {
			widgetsEnum=widgets.elements();
			moreWidgets=widgetsEnum.hasMoreElements();
		}
		public PoolWidget next() {
			PoolWidget ret=(PoolWidget)widgetsEnum.nextElement();
			moreWidgets=widgetsEnum.hasMoreElements();
			return ret;
		}
		public boolean more() {return moreWidgets;}
	}

	// Wave Sources
	private Vector sources,widgets,singleSources;
	private float[] height,sparseHeight/*,height2,sparseHeight2*/;

	private static float[] square(int size) {return new float[size*size];}

	private Pool pool;
	private int resolution;
	private boolean normalPerVertex;
	private float spacing;
	private int sparseResolution;
	private float sparseSpacing;

	private int linearLayout = 1;
	private int linearCount, radialCount, sampleCount, pluckedCount, struckCount;
	public int linearCounter, radialCounter, sampleCounter, pluckedCounter, struckCounter;	//public for WidgetsPanel's sake; blah [Davis]

	private int staticResolution = 200;
	private int animResolution = 100;

	private boolean staticNormalPerVertex = true;
	private boolean animNormalPerVertex = true;

	private boolean autoUpdate = false;

	private final static int updateInterval = 50;
	private final static int minUpdateInterval = 10;

	public Engine(WaveSimulation w) {
		super("Wave Simulation Animation Thread #"+nextThreadID());
		//DEBUG:
		//ThreadWatch.add(this);

		applet = w;
		widgetsPanel = w.getSourcePanel();
		widgetsPanel.setEngine(this);
		controlPanel = w.getControlPanel();
		controlPanel.setEngine(this);
		statusBar = w.getStatusBar();
		widgets_setChoice = (EventInSFInt32) w.getEAI().getEI("Widget-SWITCH","whichChoice");

		sources = new Vector();
		widgets = new Vector();
		singleSources = new Vector();

		pool = new Pool(w.getEAI(),POOL_SIZE);
		resolution = pool.getResolution();
		spacing = pool.getSpacing();
		sparseResolution = pool.getSparseResolution();
		sparseSpacing = pool.getSparseSpacing();

		height = square(resolution);
		sparseHeight = square(sparseResolution);

		widgetsPanel.setAutoSelect(false);

		wslPlayer = w.getWSLPlayer();
		wslPlayer.addListener(this);

		reset();
	}

	public void destroy() {
		//These shouldn't be necessary (as the Engine shouldn't be reachable anyway),
		//and it can cause the animation to get NullPointerExceptions.  [Davis]
		//sources = null;
		//widgets = null;
		//pool = null;
		//height = null;
		//sparseHeight = null;
	}

	public StatusBar getStatusBar() {return statusBar;}

	private boolean isSparse() {return pool.getRenderingMode()==Pool.SPARSE;}

	//private float index(float[] array,int r,int c,int C) {return array[r*C+c];}

	public float getHeight(float x,float y) {return getHeight(x,y,t);}

	//This method may NOT be synchronized!
	//If it were, then deadlock would occur when code responding to a VRML event
	//(such as a selection of a SamplingStick) called this function while the
	//engine thread was in the midst of calculating a frame.

	//Actually, it can be synchronized.	 It doesn't make any VRML accesses, so can't block at all.
	//Not that synchronizing it is any use; the only thing that would cause it problems would be
	//changes to the widget list.	 And those wouldn't happen in concurrent calls to this method anyway.
	private float getHeight(float x,float y,float t) {
		float h = 0;

		// For each source
		for(Enumeration waveSources=sources.elements();waveSources.hasMoreElements();)
			h += ((WaveSource) waveSources.nextElement()).getValue(x, y, t);

		for (Enumeration singleWaveSources=singleSources.elements();singleWaveSources.hasMoreElements();)
			h += ((SingleWaveSource) singleWaveSources.nextElement()).getValue(x, y, t);

		return h;
	}

	public void update() {
		int i, p = 0;
		int u, v, uvmax;
		float x, y, space, newHeight[];		//newHeight is pointer
		boolean lores=isSparse();

		if(sampleCount>0) {
			WIterator w=new WIterator();
			i=0;
			do {
				PoolWidget pw=w.next();
				if(pw instanceof SamplingStick) {
					++i;
					((SamplingStick)pw).query();
				}
			} while(i<sampleCount);
		}

		if(lores) {
			uvmax=sparseResolution;
			space=sparseSpacing;
			newHeight=sparseHeight/*2*/;
		} else {
			if(!playing) statusBar.setText("Calculating...");
			uvmax=resolution;
			space=spacing;
			newHeight=height/*2*/;
		}

		for(y=50.0f, p=0, v=0; v<uvmax; v++, y-=space)
			for(x=-50.0f, u=0; u<uvmax; u++, x+=space, p++)
				newHeight[p]=getHeight(x,y,t);

		//height2 and sparseHeight2 DISABLED for now

		//There is unsynchronized access to height/2 and to sparseHeight/2,
		//but this is not a problem as we are only swapping them, not modifying.
		try {
			if(lores) {
				//sparseHeight2=sparseHeight;
				pool.setHeight(sparseHeight/*=newHeight*/);
			} else {
				//height2=height;
				pool.setHeight(height/*=newHeight*/);
				if(!playing) statusBar.setText(null);
			}
			pool.applyRenderingMode();
		} catch(OutOfMemoryError fake) {}
		catch(ClassCastException fake) {}
	}

	//This is something of a kludge, but it seems to work: we can't synchronize
	//update() because that would deadlock if the VRML event-handling thread
	//called it (which it needs to).  So we synchronize outside of update() on a
	//variable that can guarantee that the Engine is not in update().

	//However, note that between frames will only ever be true when the engine
	//is paused.  So you can't wait on it.
	private boolean betweenFrames;
	public synchronized boolean isBetweenFrames() {return betweenFrames;}

	public void run() {
		long time;
		playing = false;
		done = false;

		while(!done) {
			time = System.currentTimeMillis();
			if(!done) update();
			else return;
			try {
				time = updateInterval - System.currentTimeMillis() + time;
				if(time<minUpdateInterval) time = minUpdateInterval;
				sleep(time);
				synchronized (this) {
					betweenFrames=true;
					while(!playing && !done) wait();
					betweenFrames=false;
				}
			}
			catch(InterruptedException e) {return;}
			t+=tStep;
		}
	}

	public void play() {
		if(playing) return;
		wasPlaying = playing;
		playing = true;
		applet.getControlPanel().setPlaying(true);
		synchronized(this) {notify();}
	}

	public void pause() {
		wasPlaying = playing;
		playing = false;
		applet.getControlPanel().setPlaying(false);
	}

	public void prevFrame() {
		if(!playing) {
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

	public int getResolution() {return resolution;}

	public void setNormalPerVertex(boolean normal) {
		if(playing) pause();
		statusBar.setText("Working...");
		pool.setNormalPerVertex(normal);
		normalPerVertex = pool.getNormalPerVertex();
		statusBar.reset();
		if(!wasPlaying && autoUpdate) update();
		else play();
	}

	public boolean getNormalPerVertex() {
		return pool.getNormalPerVertex();
	}

	//This updates the GUI and the engine; thus it's used by WSL (which is neither).
	private void wslSetPoolOptions(int res,boolean normalPerVertex) {
		controlPanel.setResolution(res,normalPerVertex);
		setPoolOptions(res,normalPerVertex);
	}

	public void setPoolOptions(int res, boolean normalPerVertex) {
		if(res<50 || res>400) return;

		pause();
		statusBar.setText("Working...");

		pool.setOptions(res, normalPerVertex);
		resolution = pool.getResolution();
		normalPerVertex = pool.getNormalPerVertex();
		spacing = pool.getSpacing();

		height = new float[resolution*resolution];

		statusBar.reset();
		if(wasPlaying) play();
		else if(autoUpdate) update();
	}

	public EAI getEAI() {return applet.getEAI();}

	public WSLPlayer getWSLPlayer() {return wslPlayer;}

	public LinearSource addSource(float amplitude, float wavelength, float phase, float angle) {
		float x;
		float y;
		float widgetSpacing;
		int i;

		linearCount++;
		widgetSpacing = 100.0f / (linearCount+1);
		switch(linearLayout) {
		case 1:
			x = -POOL_SIZE/2;
			y = -POOL_SIZE/2 + (linearCount) * widgetSpacing;
			break;
		case 2:
			x = -POOL_SIZE/2 + (linearCount) * widgetSpacing;
			y = POOL_SIZE/2;
			break;
		case 3:
			x = POOL_SIZE/2;
			y = -POOL_SIZE/2 + (linearCount) * widgetSpacing;
			break;
		case 4:
			x = -POOL_SIZE/2 + (linearCount) * widgetSpacing;
			y = -POOL_SIZE/2;
			break;
		default:
			x = -POOL_SIZE/2;
			y = -POOL_SIZE/2 + (linearCount) * widgetSpacing;
			break;
		}

		LinearSource s = new LinearSource(this, widgetsPanel, amplitude, wavelength, phase, x, y, angle);
		if(wslPlayer.isPlaying()) s.setEnabled(false);
		s.setID("line" + linearCounter++);
		sources.addElement(s);
		widgets.addElement(s);

		if(linearCount==1) controlPanel.setLayoutButtonsEnabled(true);
		arrangeLinearWidgets();
		widgetsPanel.addSource(s);

		if(!playing && autoUpdate) update();
		//This line could conflict with WidgetsPanel's setAutoSelect() method, but it seems to be necessary to make the source's widgets display when the source is added.
		//widgetsPanel.show(s);
		return s;
	}

	public PluckedSource addSource(float amplitude, float width, float xy[]) {
		PluckedSource ps = new PluckedSource(this,widgetsPanel, amplitude, width, xy[0], xy[1]);
		ps.setT(t);
		widgetsPanel.setAutoSelect(true);
		pluckedCount++;
		ps.setID("plucked" + pluckedCounter++);
		if (wslPlayer.isPlaying()) ps.setEnabled(false);
		singleSources.addElement(ps);
		widgets.addElement(ps);
		widgetsPanel.addSource(ps);
		update();
		return ps;
	}

	public StruckSource addStruckSource(float amplitude, float width, float xy[]) {
		StruckSource ss = new StruckSource(this,widgetsPanel, amplitude, width, xy[0], xy[1]);
		ss.setT(t);
		widgetsPanel.setAutoSelect(true);
		struckCount++;
		ss.setID("struck" + struckCounter++);
		if (wslPlayer.isPlaying()) ss.setEnabled(false);
		singleSources.addElement(ss);
		widgets.addElement(ss);
		widgetsPanel.addSource(ss);
		update();
		return ss;
	}

	public RadialSource addSource(float amplitude, float wavelength, float phase, float x, float y) {
		RadialSource s = new RadialSource(this, widgetsPanel, amplitude, wavelength, phase, x, y);
		widgetsPanel.setAutoSelect(true);
		radialCount++;
		s.setID("point" + radialCounter++);
		if(wslPlayer.isPlaying()) s.setEnabled(false);
		sources.addElement(s);
		widgets.addElement(s);
		widgetsPanel.addSource(s);
		//if(!playing && autoUpdate) update();
		//This line could conflict with WidgetsPanel's setAutoSelect() method, but it seems to be necessary to make the source's widgets display when the source is added.
		//widgetsPanel.show(s);
		//if(!playing && autoUpdate) update();
		update();
		return s;
	}

	public SamplingStick addSamplingStick(float x,float y) {
		SamplingStick s = new SamplingStick(this, widgetsPanel, x, y);
		widgetsPanel.setAutoSelect(true);
		sampleCount++;
		s.setID("sample" + sampleCounter++);
		widgets.addElement(s);
		widgetsPanel.addWidget(s);
		if(!playing && autoUpdate) update();
		//This line could conflict with WidgetsPanel's setAutoSelect() method, but it seems to be necessary to make the source's widgets display when the source is added.
		//widgetsPanel.show(s);
		return s;
	}

	public boolean removeWidget(int i) {
		if(i<0 || i>=widgets.size()) return false;
		return removeWidget((PoolWidget)widgets.elementAt(i));
	}

	public boolean removeWidget(PoolWidget s) {
		if(widgets.contains(s)) {
			sources.removeElement(s);		//this may do nothing, but does no harm
			widgets.removeElement(s);
			singleSources.removeElement(s);
			s.destroy();
			if(s instanceof LinearSource) {
				linearCount--;
				if(linearCount==0) controlPanel.setLayoutButtonsEnabled(false);
				else arrangeLinearWidgets();
			} else if(s instanceof RadialSource) radialCount--;
			else if(s instanceof SamplingStick) sampleCount--;
			else System.err.println("Engine::removeWidget: unexpected widget "+s);
			if(sources.size()==0 && singleSources.size()==0)
				pause();
			if(!playing && autoUpdate) update();
			return true;
		}
		return false;
	}

	public int widgetIndex(PoolWidget s) {
		return widgets.indexOf(s);
	}

	public PoolWidget getWidget(int index) {
		if(index>=0 && index<widgets.size()) return (PoolWidget) widgets.elementAt(index);
		else return null;
	}

	//This must not be called mid-animation frame!
	public PoolWidget getWidget(String id) {
		if(id==null) return null;		//in this sense, not even another null matches
		WIterator w=new WIterator();
		while(w.more()) {
			PoolWidget pw=w.next();
			String ID = pw.getID();
			if(id.equals(ID))
				return pw;
		}
		return null;
	}

	public void hideWidgets() {
		WIterator w=new WIterator();
		while(w.more()) w.next().hideWidgets();
	}

	public void setWidgetDragging(boolean dragging) {
		if(dragging) {
			pool.setRenderingMode(Pool.SPARSE);
			pause();
		} else {
			pool.setRenderingMode(Pool.FULL);
			if(wasPlaying) play();
			else if(autoUpdate) update();
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
			PoolWidget pw=(PoolWidget)sources.elementAt(i);
			if(pw instanceof LinearSource) {
				switch (linearLayout) {
				case 1:
					pw.setXY(-POOL_SIZE/2, -POOL_SIZE/2+j*widgetSpacing, true);
					break;
				case 2:
					pw.setXY(-POOL_SIZE/2+j*widgetSpacing, POOL_SIZE/2, true);
					break;
				case 3:
					pw.setXY(POOL_SIZE/2, -POOL_SIZE/2+j*widgetSpacing, true);
					break;
				case 4:
					pw.setXY(-POOL_SIZE/2+j*widgetSpacing, -POOL_SIZE/2, true);
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
		widgets_setChoice.setValue(visible ? 0 : -1);
	}

	public void selectWidget(PoolWidget selected) {
		widgetsPanel.show(selected);
		update();
	}

	public void selectWidget(int index) {
		widgetsPanel.show(getWidget(index));
		update();
	}

	private void clear() {
		if(playing) pause();
		wasPlaying = false;

		WIterator w=new WIterator();
		while(w.more()) w.next().destroy();

		sources = new Vector();
		widgets = new Vector();
		singleSources = new Vector();

		autoUpdate = false;
		linearLayout = 1;
		linearCount = 0;
		radialCount = 0;
		sampleCount = 0;

		pool.reset();
		widgetsPanel.reset();
		controlPanel.reset();
		statusBar.reset();

		resolution = pool.getResolution();
		spacing = pool.getSpacing();
		sparseResolution = pool.getSparseResolution();
		sparseSpacing = pool.getSparseSpacing();

		height = new float[resolution*resolution];
		sparseHeight = new float[sparseResolution*sparseResolution];
	}

	public void reset() {
		clear();

		widgetsPanel.setAutoSelect(false);

		//The default-ly existing point sources:
		addSource(4, 8, 0.0f, 0.0f, 16.0f);
		addSource(4, 8, 0.0f, 0.0f, -16.0f);

		widgetsPanel.setAutoSelect(true);
		widgetsPanel.show(0);

		autoUpdate = true;

		update();
	}

	// ----------------------------------------------------------------------
	// WSL Methods
	// ----------------------------------------------------------------------

	public WSLNode toWSLNode() {
		int i;
		WSLNode node = new WSLNode("wavesimulation");
		final WSLAttributeList atts=node.getAttributes();

		if(!pool.getGridVisible()) atts.add("grid", "hidden");

		atts.add("widgets",widgetsPanel.widgetVisibilityString());

		atts.add("linearLayout", String.valueOf(linearLayout));

		int res = pool.getResolution();
		boolean gouraud = pool.getNormalPerVertex();
		if(res==LOW_RESOLUTION)
			atts.add("resolution",gouraud==FAST_NPV?"2":"1");
		else if(res==MEDIUM_RESOLUTION && gouraud==MEDIUM_NPV)
			atts.add("resolution", "3");
		else if(res==HIGH_RESOLUTION)
			atts.add("resolution",gouraud==VERYSMOOTH_NPV?"5":"4");
		else atts.add("resolution", "" + resolution + ':'
													 + (gouraud ? "true" : "false"));

		atts.add("animation", playing?"play":"stop");

		WSLNode sourcesNode = new WSLNode("sources");
		WIterator w=new WIterator();
		while(w.more()) sourcesNode.addChild(w.next().toWSLNode());
		node.addChild(sourcesNode);

		return node;
	}

	private void setParameter(String target, String param, String value) {
		//Go ahead and get a value and a panel; we might want it
		float f=WTString.toFloat(value,Float.NaN);
		java.awt.Panel p=widgetsPanel.getSwitcher().getActivePanel();

		if(target==null || target.equals("")) {
			if("grid".equalsIgnoreCase(param)) {
				if("visible".equalsIgnoreCase(value)) {
					setGridVisible(true);
					widgetsPanel.setGridVisible(true);
				} else if("hidden".equalsIgnoreCase(value)) {
					setGridVisible(false);
					widgetsPanel.setGridVisible(false);
				}
			} else if("widgets".equalsIgnoreCase(param)) {
				if("visible".equalsIgnoreCase(value)) {
					setWidgetsVisible(true);
					widgetsPanel.setWidgetVisible(widgetsPanel.WIDGET_FULL);
				} else if("minimal".equalsIgnoreCase(value)) {
					setWidgetsVisible(true);
					widgetsPanel.setWidgetVisible(widgetsPanel.WIDGET_ICON);
					WIterator w=new WIterator();
					while(w.more()) w.next().hideWidgets();
				} else if("hidden".equalsIgnoreCase(value)) {
					setWidgetsVisible(false);
					widgetsPanel.setWidgetVisible(widgetsPanel.WIDGET_HIDE);
				}
			} else if("linearLayout".equalsIgnoreCase(param)) {
				int layout = Integer.parseInt(value);
				if(layout>=1 && layout<=4)
					setLinearLayout(layout);
			} else if("resolution".equalsIgnoreCase(param)) {
				if("1".equals(value))
					wslSetPoolOptions(LOW_RESOLUTION, FASTEST_NPV);
				else if("2".equals(value))
					wslSetPoolOptions(LOW_RESOLUTION, FAST_NPV);
				else if("3".equals(value))
					wslSetPoolOptions(MEDIUM_RESOLUTION, MEDIUM_NPV);
				else if("4".equals(value))
					wslSetPoolOptions(HIGH_RESOLUTION, SMOOTH_NPV);
				else if("5".equals(value))
					wslSetPoolOptions(HIGH_RESOLUTION, VERYSMOOTH_NPV);
				else {
					int split = value.indexOf(':');
					int resolution = Integer.parseInt(value.substring(0, split));
					boolean gouraud = new Boolean(value.substring(split+1)).booleanValue();
					wslSetPoolOptions(resolution, gouraud);
				}
			} else if("animation".equalsIgnoreCase(param)) {
				if("play".equalsIgnoreCase(value)) {
					play();
					controlPanel.setPlaying(true);
				} else if("stop".equalsIgnoreCase(value)) {
					pause();
					controlPanel.setPlaying(false);
				}
			} else if("action".equals(param)) {
				if("nextFrame".equals(value))
					nextFrame();
				else if("prevFrame".equals(value))
					prevFrame();
				else if("reset".equals(value))
					reset();
			}
		} else {
			PoolWidget s = getWidget(target);
			if(s==null) return;

			if("wavelength".equalsIgnoreCase(param)) {
				((WaveSource)s).setWavelength(f, true);
				widgetsPanel.getSwitcher().getActiveSourcePanel().setWavelength(f);
			} else if("phase".equalsIgnoreCase(param)) {
				f = WTMath.toRads(f);
				((WaveSource)s).setPhase(f, true);
				widgetsPanel.getSwitcher().getActiveSourcePanel().setPhase(f);
			} else if("amplitude".equalsIgnoreCase(param)) {
				((WaveSource)s).setAmplitude(f, true);
				widgetsPanel.getSwitcher().getActiveSourcePanel().setAmplitude(f);
			} else if("angle".equalsIgnoreCase(param)) {
				if(s instanceof LinearSource) {
					f = WTMath.toRads(f);
					((LinearSource) s).setAngle(f, true);
					((LinearPanel)p).setAngle(f);
				}
			} else if("x".equals(param)) {
				if(s instanceof RadialSource) {
					s.setX(f, true);
					((RadialPanel)p).setX(f);
				} else if(s instanceof SamplingStick) {
					s.setX(f, true);
					((SamplePanel)p).setX(f);
				}
			} else if("y".equals(param)) {
				if(s instanceof RadialSource) {
					s.setY(f, true);
					((RadialPanel)p).setY(f);
				} else if(s instanceof SamplingStick) {
					s.setY(f, true);
					((SamplePanel)p).setY(f);
				}
			} else if("position".equalsIgnoreCase(param)) {
				float[] xy=parsePosition(value);
				boolean abort=false;
				if(s instanceof RadialSource)
					((RadialPanel)p).setXY(xy[0],xy[1]);
				else if(s instanceof SamplingStick)
					((SamplePanel)p).setXY(xy[0],xy[1]);
				else abort=true;
				if(!abort) s.setXY(xy, true);
			}
		}
		if(autoUpdate) update();
	}

	private float[] parsePosition(String wsl) {
		float ret[]=new float[2];
		if(!WTString.isNull(wsl)) {
			int split = wsl.indexOf(',');
			ret[0] = new Float(wsl.substring(0, split)).floatValue();
			ret[1] = new Float(wsl.substring(split+1)).floatValue();
		}
		return ret;
	}

	private void addWidget(WSLNode node) {
		float amplitude;
		float wavelength;
		float phase;
		float angle;
		float width; // for plucked and struck
		float xy[];
		String select;
		boolean selected;
		PoolWidget s;

		final WSLAttributeList atts=node.getAttributes();

		select = atts.getValue("selected");
		selected = "true".equalsIgnoreCase(select);
		if(node.getName().equalsIgnoreCase("pointsource")) {
			phase = (float) WTMath.toRads(atts.getFloatValue("phase", 0));
			amplitude = atts.getFloatValue("amplitude", 2);
			wavelength = atts.getFloatValue("wavelength", 10);
			xy=parsePosition(atts.getValue("position"));
			s = addSource(amplitude, wavelength, phase, xy[0], xy[1]);
			s.setID(atts.getValue("id"));
		} else if(node.getName().equalsIgnoreCase("linesource")) {
			phase = (float) WTMath.toRads(atts.getFloatValue("phase", 0));
			amplitude = atts.getFloatValue("amplitude", 4);
			wavelength = atts.getFloatValue("wavelength", 8);
			angle = (float) WTMath.toRads(atts.getFloatValue("angle", 0));
			DebugPrinter.println("Angle=" + angle);
			s = addSource(amplitude, wavelength, phase, angle);
			s.setID(atts.getValue("id"));
		} else if(node.getName().equals("samplingstick")) {
			xy=parsePosition(atts.getValue("position"));
			s = addSamplingStick(xy[0],xy[1]);
			s.setID(atts.getValue("id"));
		} else if (node.getName().equals("pluckedsource")) {
			amplitude = atts.getFloatValue("amplitude", 2);
			width = atts.getFloatValue("width", 10);
			xy=parsePosition(atts.getValue("position"));
			s = addSource(amplitude, width, xy);
			s.setID(atts.getValue("id"));
		} else if (node.getName().equals("strucksource")) {
			amplitude = atts.getFloatValue("amplitude", 2);
			width = atts.getFloatValue("width", 10);
			xy=parsePosition(atts.getValue("position"));
			s = addStruckSource(amplitude, width, xy);
			s.setID(atts.getValue("id"));
		}	else {
			System.err.println("Engine: bad widget type: "+node.getName());
			return;
		}
		if(selected) {
			selectWidget(s);
		}
	}

	public void initialize(WSLScriptEvent event) {
		clear();

		WSLNode node = event.getNode();

		WSLAttributeList atts = node.getAttributes();
		//Provide backward-ish compatibility with old non-recording of visibility
		if(atts.getValue("widgets")==null) atts.add("widgets","visible");

		int i;
		for(i=0; i<atts.getLength(); i++)
			setParameter(null, atts.getName(i), atts.getValue(i));

		widgetsPanel.setAutoSelect(false);
		WSLNode sourcesNode = node.getNode("sources");
		for(i=0; i<sourcesNode.getChildCount(); i++)	addWidget(sourcesNode.getChild(i));

		widgetsPanel.setAutoSelect(true);
		autoUpdate = true;
		update();
	}

	public void scriptActionFired(WSLScriptEvent event) {
		PoolWidget s;
		switch(event.getID()) {
		case WSLScriptEvent.ACTION_PERFORMED:
		case WSLScriptEvent.MOUSE_DRAGGED:
			setParameter(event.getTarget(), event.getParameter(), event.getValue());
			break;
		case WSLScriptEvent.MOUSE_PRESSED: setWidgetDragging(true); break;
		case WSLScriptEvent.MOUSE_RELEASED: setWidgetDragging(false); break;
		case WSLScriptEvent.MOUSE_ENTERED:
			String target = event.getTarget();
			s = getWidget(target);
			if(s!=null) selectWidget(s);
			break;
		case WSLScriptEvent.OBJECT_ADDED:
			float amplitude;
			float wavelength;
			float phase;
			WSLNode node = event.getNode().getChild(0);

			addWidget(node);
			break;
		case WSLScriptEvent.OBJECT_REMOVED:
			s = getWidget(event.getTarget());
			if(s!=null) {
				widgetsPanel.removeSourceFromList(widgetIndex(s));
				removeWidget(s);
			}
			break;
		}
	}

	public void playerStateChanged(WSLPlayerEvent event) {
		WIterator i;
		switch(event.getID()) {
		case WSLPlayerEvent.PLAYER_STARTED:
			i=new WIterator();
			while(i.more()) i.next().setEnabled(false);
			break;
		case WSLPlayerEvent.PLAYER_STOPPED:
			i=new WIterator();
			while(i.more()) i.next().setEnabled(true);
			break;
		}
	}
}
