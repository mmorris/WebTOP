package org.webtop.module.polarization;

import org.webtop.util.*;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;
import org.webtop.x3d.output.*;

public class Engine
	implements AnimationEngine 
{//,WSLScriptListener,WSLPlayerListener {
	public static class EVector 
	{
		public float x,y,xphase,yphase;		// xphase is often 0
		public float intensity() {return x*x+y*y;}
		public void assign(EVector e)
		{x=e.x; y=e.y; xphase=e.xphase; yphase=e.yphase;}
		public String toString() 
		{
			return getClass().getName()+"[<"+x+','+y+">;{"+xphase+','+yphase+"}]";
		}
	}

	// Note that the history modes map to Switch choices in the VRML file.
	public static final int BEGIN_HIST=0,END_HIST=1, // which history?
		HIST_OFF=0,HIST_DOTS=1,HIST_LINES=2, // display what?
		MAX_HIST=2;
	public static final String[] HIST_VALUES={"off","dots","lines"};

	public static final float DEF_XAMP=1,DEF_YAMP=1,MAX_AMP=1,DEF_WAVELENGTH=550,
		DEF_EPSILON=(float)Math.PI/2,DEF_ANGLE=0,
		DEF_THICKNESS=0.5f;

	// The VRML scale is 5 for full-range amplitude range.
	// X_SCALE and Y_SCALE convert from internal electric field to VRML size.
	// Z_SCALE converts from VRML size to nanometers (opposite of others!).
	private static final float X_SCALE=5/DEF_XAMP,Y_SCALE=5/DEF_YAMP,Z_SCALE=100;

	private static final float FILTER_SPACING=5;

	public static final int PERIOD=30,MIN_DELAY=10;
	
	private static final float TWO_PI=2*(float)Math.PI;

	//The speed of the update wave is Z_STEP/T_STEP; the speed of polarized light
	//is LIGHT_SPEED/Z_SCALE (see setWavelength() and render()).  We want these
	//to be equal, so we set LIGHT_SPEED=Z_STEP*Z_SCALE/T_STEP.  Unpolarized
	//light is independent of position (and thus has no speed at all); only the
	//update wave carries it along.
	private static final float Z_STEP = 0.18f,MAX_Z = 20,
		T_STEP=PERIOD/1000f, // this makes t 'real'
		LIGHT_SPEED=Z_STEP*Z_SCALE/T_STEP;
	private static final int VECTORS = (int) (MAX_Z/Z_STEP)+1;

	//This is used to protect data; the Animation protects execute() itself.  No
	//VRML calls should take place while synchronized on this lock!
	private final Object lock=new Object();

	private final Polarization applet;
	private final SAI sai;
	private final SourcePanel panel;
	private final ControlPanel controlPanel;
	private final FilterPanel filterPanel;
	//private final EventManager eManager;

	//private final WSLPlayer wslPlayer;

	//Don't call setCoords() on the h?ILSes!  They share coordinates with
	//unnamed PointSets, so we use h?Coord.
	private final AbstractIS /*xyILS,xILS,yILS,*/h1ILS,h2ILS;
	private final FloatMatrixNode h1Coord,h2Coord;
	private final VectorLine xyVL,xVL,yVL;

	private final SFInt32 set_H1Choice,set_H2Choice,set_XYChoice,set_XChoice,set_YChoice;
	//private final NodeEI addChildren,removeChildren;
	private final SFInt32 set_WidgetsChoice;

	private final SFFloat set_wavelength,set_xAMP,set_yAMP,set_phase;

	private final SFBool set_wavelengthEnabled,set_phaseEnabled,set_exEnabled,set_eyEnabled;

	private final SFBool set_wavelengthHighlighted,set_phaseHighlighted,set_exHighlighted,set_eyHighlighted;

	private final float[][] pxy,px,py;

	private final FilterList filters=new FilterList();

	// Variables used for the simulation
	private float wavelength,			// nm
		K,							// nm^-1
		W;							// omega, Hz

	//The principal electric field data.  E2 is E without the filters applied.
	//However, E is not just the electric field; it also stores phase
	//information for polarized light (see EVector).  In fact, for polarized
	//light an E vector is simply the reference vector in effect when it was
	//written.  Moreover, E is a circular buffer; headE indicates the current
	//location for filling the buffer.  Values get overwritten just as they fall
	//off the other end of the axis anyway, and they are written in reverse
	//order (from the end of the array back to the beginning, repeatedly) so
	//that vectors later in time come earlier in space.
	private final EVector[] E,E2;
	private int headE;

	private volatile float[][] beginHist,endHist;
	private volatile int totalH = 150,showH = 125;
	private volatile int historyMode1 = 0,historyMode2 = 0;
	private volatile boolean startingHistory=true;

	private final EVector E0;
	private volatile float t=-1;

	private Animation anim;

	//These are used as array indices, so modify with care and keep them
	//contiguous.  MAX_MODE is the highest defined value.
	public static final int NONE=0,X_ONLY=1,Y_ONLY=2,X_AND_Y=3,COMPOSITE=4,
		ALL=5,MAX_MODE=5;
	private volatile int fieldMode = COMPOSITE;
	//Lookup tables for what to render based on the fieldMode
	private static final boolean[] useXY={false,false,false,false,true,true},
		useX={false,true,false,true,false,true},
		useY={false,false,true,true,false,true};

	private volatile boolean isPolarized = true,widgetsVisible = true;

	//The number of frequencies used to simulate unpolarized light
	private static final int UNPOL_FREQUENCIES = 100;

	private final float[] phix,phiy,f_unpol;

	private boolean initialized = false,massChange;

	private int polarizerCount = 0,wavePlateCount = 0;

	public Engine(Polarization polarization) 
	{
		massChange = true;

		applet = polarization;
		sai = applet.getSAI();
		//TODO: fix these
		panel = applet.getSourcePanel();
		controlPanel = applet.getControlPanel();
		filterPanel = applet.getFilterPanel();
		//eManager = applet.getEventManager();

		//wslPlayer = applet.getWSLPlayer();
		//wslPlayer.addListener(this);

		//TODO: probably don't need any of this crap
		h1ILS=new IndexedSet(sai,sai.getNode("H1ILS"));
		h2ILS=new IndexedSet(sai,sai.getNode("H2ILS"));
		h1Coord=new FloatMatrixNode(sai,sai.getNode("H1Coord"),FloatMatrixNode.SET_POINT);
		h2Coord=new FloatMatrixNode(sai,sai.getNode("H2Coord"),FloatMatrixNode.SET_POINT);
		xyVL=new VectorLine(new IndexedSet(sai,sai.getNode("XYLineSet")),MAX_Z,VECTORS);
		xVL=new VectorLine(new IndexedSet(sai,sai.getNode("XLineSet")),MAX_Z,VECTORS);
		yVL=new VectorLine(new IndexedSet(sai,sai.getNode("YLineSet")),MAX_Z,VECTORS);

		set_H1Choice = (SFInt32) sai.getField("History1","whichChoice");
		set_H2Choice = (SFInt32) sai.getField("History2","whichChoice");
		set_XYChoice = (SFInt32) sai.getField("XYComposite","set_whichChoice");
		set_XChoice = (SFInt32) sai.getField("XComponent","set_whichChoice");
		set_YChoice = (SFInt32) sai.getField("YComponent","set_whichChoice");
		//addChildren = new NodeEI(sai.getField("Filters","addChildren"));
		//removeChildren = new NodeEI(sai.getField("Filters","removeChildren"));
		set_WidgetsChoice = (SFInt32) sai.getField("PolarizedControls","whichChoice");

		set_wavelength = (SFFloat) sai.getField("WavelengthWidget","set_value");
		set_xAMP = (SFFloat) sai.getField("XAMPDragger","set_position");
		set_yAMP = (SFFloat) sai.getField("YAMPDragger","set_position");
		set_phase = (SFFloat) sai.getField("PhaseDragger","set_phaseDifference");

		set_wavelengthEnabled = (SFBool) sai.getField("WavelengthWidget","set_enabled");
		set_exEnabled = (SFBool) sai.getField("XAMPDragger","set_enabled");
		set_eyEnabled = (SFBool) sai.getField("YAMPDragger","set_enabled");
		set_phaseEnabled = (SFBool) sai.getField("PhaseDragger","set_enabled");

		set_wavelengthHighlighted = (SFBool) sai.getField("WavelengthWidget","set_isActive");
		set_exHighlighted = (SFBool) sai.getField("XAMPDragger","set_isActive");
		set_eyHighlighted = (SFBool) sai.getField("YAMPDragger","set_isActive");
		set_phaseHighlighted = (SFBool) sai.getField("PhaseDragger","set_isActive");

		E0 = new EVector();
		E0.x=DEF_XAMP;
		E0.y=DEF_YAMP;
		E0.yphase=DEF_EPSILON;

		updatePanelIntensity();

		setWavelength(DEF_WAVELENGTH);

		pxy = new float[VECTORS*2][2];
		px = new float[VECTORS*2][2];
		py = new float[VECTORS*2][2];

		E = new EVector[VECTORS];
		E2 = new EVector[VECTORS];
		for(int i=0;i<VECTORS;++i) 
		{
			E[i]=new EVector();
			E2[i]=new EVector();
			// 			//Set z-components of point vectors here, as they're constant
			// 			pxy[2*i][2]=px[2*i][2]=py[2*i][2]=
			// 				pxy[2*i+1][2]=px[2*i+1][2]=py[2*i+1][2]=i*Z_STEP;
		}
		headE = 0;

		setPolarized(true);

		phix = new float[UNPOL_FREQUENCIES];
		phiy = new float[UNPOL_FREQUENCIES];
		f_unpol = new float[UNPOL_FREQUENCIES];
		for(int i=0; i<UNPOL_FREQUENCIES; i++) 
		{
			phix[i] = (float) Math.random() * TWO_PI;
			phiy[i] = (float) Math.random() * TWO_PI;
			// Unpolarized light travels at LIGHT_SPEED, not at LIGHT_SPEED/Z_SCALE,
			// because the update wave (instead of Kx-Wt) is its propogator, and
			// that travels in VRML coordinates which lack the Z_SCALE.
			f_unpol[i] = LIGHT_SPEED/DEF_WAVELENGTH *
				(1 + .14f * (float)WTMath.random.nextGaussian());
		}

		massChange = false;

		render();										// Blaxxun workaround

		initialized = true;
	}

	public void reset(boolean play) 
	{
		massChange = true;

		setWavelength(DEF_WAVELENGTH);
		setEx(DEF_XAMP);
		setEy(DEF_YAMP);
		setEpsilon(DEF_EPSILON);
		showWidgets();
		setHistoryMode(BEGIN_HIST, HIST_DOTS);
		setHistoryMode(END_HIST, HIST_DOTS);
		setFieldMode(COMPOSITE);

		setPolarized(true);					// this also resets time

		//for(Filter p=filters.first(); p!=null; p = p.next)
		//	removeChildren.set(p.getNodes());
		filters.removeAll();
		polarizerCount = wavePlateCount = 0;

		massChange = false;

		if(play) setPlaying(true);
	}

	public void setWavelength(float l) 
	{
		synchronized(lock) 
		{
			wavelength = l;
			K = (float) (TWO_PI / wavelength);
			W = K * LIGHT_SPEED;
			resetHistories();						// everything changes
		}
	}
	public float getWavelength() {return wavelength;}

	public void setEx(float Ex) 
	{
		synchronized(lock) {E0.x = Ex;}
		updatePanelIntensity();
	}
	public float getEx() {return E0.x;}

	public void setEy(float Ey) 
	{
		synchronized(lock) {E0.y = Ey;}
		updatePanelIntensity();
	}
	public float getEy() {return E0.y;}

	public void setEpsilon(float epsilon) 
	{
		synchronized(lock) {E0.yphase = epsilon;}
	}
	public float getEpsilon() {return E0.yphase;}

	private void setHistorySize(int size) 
	{
		totalH = size;

		final int[] indices = new int[totalH];
		for(int i=0; i<totalH-1; ++i) indices[i] = i;
		indices[totalH-1] = -1;
		h1ILS.setCoordIndices(indices);
		h2ILS.setCoordIndices(indices);

		resetHistories();
	}

	private void resetHistories() 
	{
		beginHist = new float[totalH][3];
		endHist = new float[totalH][3];
		startingHistory=true;
	}

	public void setPolarized(boolean p) 
	{
		synchronized(lock) 
		{
			isPolarized = p;
			if(p) 
			{
				setHistorySize(150);
				if(widgetsVisible) set_WidgetsChoice.setValue(0);
				panel.setInitialIntensity(E0.x*E0.x + E0.y*E0.y);
				controlPanel.setPolarized(true);
			} 
			else 
			{
				setHistorySize(600);
				set_WidgetsChoice.setValue(-1);
				panel.setInitialIntensity(1);
				controlPanel.setPolarized(false);
			}
			// Apparently phases don't matter here.
			for(int i=0; i<VECTORS; i++) 
			{
				E[i].x=E[i].y=0;
				E2[i].x=E2[i].y=0;
			}
			t=-1;
		}
	}

	public boolean getPolarized() {return isPolarized;}

	public void setEnabled(boolean enabled) 
	{
		set_wavelengthEnabled.setValue(enabled);
		set_phaseEnabled.setValue(enabled);
		set_exEnabled.setValue(enabled);
		set_eyEnabled.setValue(enabled);

		filters.setEnabled(enabled);
	}


	public Polarizer addPolarizer(String id, float z, float angle) 
	{
		final Polarizer p = new Polarizer(sai, z, angle);
		p.setID(id);
		setupFilter(p);
		++polarizerCount;
		return p;
	}

	public Polarizer addPolarizer() 
	{
		return addPolarizer("polarizer"+polarizerCount,nextFilterPosition(),0);
	}

	public WavePlate addWavePlate(String id, float z, float angle,
		float thickness) 
	{
		final WavePlate wp = new WavePlate(sai, z, angle, thickness);
		wp.setID(id);
		setupFilter(wp);
		++wavePlateCount;
		return wp;
	}

	public WavePlate addWavePlate() 
	{
		return addWavePlate("waveplate"+wavePlateCount,nextFilterPosition(),0,
			DEF_THICKNESS);
	}

	private void setupFilter(Filter f) 
	{
		synchronized(lock) 
		{
			filters.add(f);
			//f.createVRMLNode(sai,eManager);
			//addChildren.set(f.getNodes());
			filters.hideWidgets();
			f.setActive(true);
			filterPanel.showFilter(f);
			//eManager.setActiveFilter(f);

			// With a new filter, we need to update E
			transform();
		}
	}

	private float nextFilterPosition() 
	{
		if(filters.last()!=null)
			return Math.min(filters.last().getZ()+FILTER_SPACING,MAX_Z);
		return FILTER_SPACING;
	}

	//Removes all wave plates from the module
	public void removeWavePlates() 
	{
		for(Filter p=filters.first(),q=p; p!=null;) 
		{
			if(p instanceof WavePlate) 
			{
				removeFilter(p);
				if(p==q) p=filters.first(); else p=filters.next();
			} 
			else 
			{
				q=p;
				p=filters.next();
			}
		}
	}

	public void setActiveFilter(Filter f) 
	{
		if(!filters.contains(f)) return;

		filters.hideWidgets();
		f.setActive(true);
		filterPanel.showFilter(f);
		//eManager.setActiveFilter(f);
	}

	public void moveFilter(Filter p, float z, boolean setVRML) 
	{
		synchronized(lock) 
		{
			filters.move(p, z, setVRML);
			transform();
		}
	}

	public void setFilterAngle(Filter p, float angle, boolean setVRML) 
	{
		synchronized(lock) 
		{
			filters.setAngle(p, angle, setVRML);
			transform();
		}
	}

	public void setWavePlateThickness(WavePlate p, float thickness, boolean setVRML) 
	{
		synchronized(lock) 
		{
			p.setThickness(thickness, setVRML);
			transform();
		}
	}

	public void removeFilter(Filter p) 
	{
		synchronized(lock) 
		{
			if(!filters.contains(p)) return;
			//removeChildren.set(p.getNodes());
			filters.remove(p);
			transform();
		}
	}

	public void hideWidgets() 
	{
		filters.hideWidgets();
		set_WidgetsChoice.setValue(-1);
		widgetsVisible = false;
	}

	public void showWidgets() 
	{
		if(isPolarized)
			set_WidgetsChoice.setValue(0);
		widgetsVisible = true;
	}

	public void setFieldMode(int mode) 
	{
		if(mode<0 || mode>MAX_MODE)
			throw new IllegalArgumentException("bad field mode: "+mode);
		synchronized(lock) {fieldMode = mode;}
		//Don't synchronize around sai calls!
		render();
		SAI.setDraw(set_XYChoice,useXY[mode]);
		SAI.setDraw(set_XChoice,useX[mode]);
		SAI.setDraw(set_YChoice,useY[mode]);
	}

	public void setHistoryMode(int which, int mode) 
	{
		switch(mode) 
		{
			case HIST_OFF:
			case HIST_DOTS:
			case HIST_LINES:
				break;
			default:
				throw new IllegalArgumentException("bad mode value");
		}
		switch(which) 
		{
			case BEGIN_HIST:
				synchronized(lock) {historyMode1 = mode;}
				set_H1Choice.setValue(mode-1);
				break;
			case END_HIST:
				synchronized(lock) {historyMode2 = mode;}
				set_H2Choice.setValue(mode-1);
				break;
			default:
				throw new IllegalArgumentException("bad history selection");
		}
	}

	//Provided here until the net of data flow is reworked
	public void update() {anim.update();}
	public void setPlaying(boolean b) {anim.setPlaying(b);}
	public void setPaused(boolean b) {anim.setPaused(b);}
	public boolean isPlaying() {return anim.isPlaying();}

	public void init(Animation a) {anim=a;}

	private boolean advance;
	public boolean timeElapsed(float periods) 
	{
		advance=periods!=0;
		//For this module, exact T_STEPs are required:
		if(advance) 
		{
			if(t<0) t=0; else t+=T_STEP;
			//Small t values make for better wavelength adjustment.
			if(isPolarized) t%=TWO_PI/W;
		}
		return true;
	}

	//For now, I'm trying to avoid using d at all. [Davis]
	public void execute(Animation.Data d) 
	{
		if(advance) advance();
		render();
	}

	private void render() 
	{
		final boolean usingXY=useXY[fieldMode],
				  usingX=useX[fieldMode],
				  usingY=useY[fieldMode];

		synchronized(lock) 
		{
			for(int i=0;i<VECTORS;++i) 
			{
				final EVector peV=E[(headE+i)%VECTORS];
				float vx,vy;
				if(isPolarized) 
				{
					final float z=i*Z_STEP*Z_SCALE;	// VRML units -> nm
					vx = peV.x * (float)Math.cos(K * z - W * t + peV.xphase);
					vy = peV.y * (float)Math.cos(K * z - W * t + peV.yphase);
				} 
				else 
				{
					vx = peV.x;
					vy = peV.y;
				}
				vx *= X_SCALE;
				vy *= Y_SCALE;

				final int pi=2*i+1;				// index into point-position vector arrays

				// Two values are used for the history and are thus always needed
				if(usingXY || i==0 || i==VECTORS-1) {pxy[pi][0] = vx; pxy[pi][1] = vy;}
				if(usingX) px[pi][0] = vx;
				if(usingY) py[pi][1] = vy;
			}

			//For history values, we want odd-numbered elements of pxy, as those are
			//the off-axis ends of the vectors

			if(startingHistory) 
			{
				for(int i=0; i<totalH-1; i++) 
				{	// the last element will be set later
					beginHist[i][0] = pxy[1][0];
					beginHist[i][1] = pxy[1][1];
				}
				startingHistory=false;
			}
		}

		//sai.world.beginUpdate();

		try 
		{
			if(usingXY) xyVL.setValues(pxy);
			if(usingX) xVL.setValues(px);
			if(usingY) yVL.setValues(py);
			if(historyMode1!=HIST_OFF) h1Coord.set(beginHist);
			if(historyMode2!=HIST_OFF) h2Coord.set(endHist);
		} 
		catch(OutOfMemoryError fake) {}
		catch(ClassCastException fake) {}

		//sai.world.endUpdate();
	}
	// Perhaps inline this into execute()?  It should only be called there...
	private void advance() 
	{
		synchronized(lock) 
		{
			headE = (headE+VECTORS-1) % VECTORS;

			final EVector headV=E[headE];

			if(isPolarized) headV.assign(E0);
			else 
			{
				headV.x=headV.y=0;
				float temp;
				for(int i=0; i<UNPOL_FREQUENCIES; i++) 
				{
					temp = TWO_PI * f_unpol[i] * t;
					headV.x += Math.cos(temp + phix[i]);
					headV.y += Math.cos(temp + phiy[i]);
				}
				headV.x*=X_SCALE / UNPOL_FREQUENCIES;
				headV.y*=Y_SCALE / UNPOL_FREQUENCIES;
				headV.xphase=headV.yphase=0;
			}
			E2[headE].assign(headV);

			final Filter activeFilter = filterPanel.getActiveFilter();

			// for unpolarized light
			float intensity = 1;
			boolean firstPolarizer = true;
			float previousAngle = 0;

			for(Filter filter=filters.first(); filter!=null; filter=filters.next()) 
			{
				float z = filter.getZ();

				int pe = (headE + ((int) (z/Z_STEP)) + 1) % VECTORS;

				filter.transform(E[pe], isPolarized);

				if(filter instanceof Polarizer && !isPolarized) 
				{
					if(firstPolarizer) 
					{
						intensity = 0.5f;
						firstPolarizer = false;
					} 
					else 
					{
						double temp = Math.cos(filter.getAngle() - previousAngle);
						intensity *= temp * temp;
					}
					previousAngle = filter.getAngle();
				}

				// Update intensity value in Java console
				// only if current filter is a Polarizer
				// and it is the active filter
				if(filter instanceof Polarizer && filter==activeFilter) 
				{
					// Check if we are in polarized mode
					if(isPolarized)	
					{
						filterPanel.setIntensity(E[pe].intensity());
					} 
					else 
					{
						// Check if filter is the first Polarizer
						//DebugPrinter.println("Intensity = " + intensity);
						filterPanel.setIntensity(intensity);
					}
				}
			}

			final float[][] hist1=beginHist,hist2=endHist; // for efficiency

			// Rotate history elements
			for(int i=0; i<totalH-1; i++) 
			{
				hist1[i][0] = hist1[i+1][0];
				hist1[i][1] = hist1[i+1][1];
				hist2[i][0] = hist2[i+1][0];
				hist2[i][1] = hist2[i+1][1];
			}
			hist1[totalH-1][0] = pxy[1][0];
			hist1[totalH-1][1] = pxy[1][1];
			hist2[totalH-1][0] = pxy[VECTORS*2-1][0];
			hist2[totalH-1][1] = pxy[VECTORS*2-1][1];
		}
	}

	//All calls to this should be synchronized on lock

	//There's some slight error here (or else in the rendering code) when
	//dragging a filter's position across a Z_STEP boundary, I think. [Davis]
	private void transform() 
	{
		if(!initialized) return;

		for(int i=0; i<VECTORS; i++) E[i].assign(E2[i]);

		for(Filter p=filters.first();p!=null;p=filters.next()) 
		{
			float z = p.getZ();
			int pe = (headE + (int) Math.ceil(z/Z_STEP)) % VECTORS;
			for(; z<=MAX_Z; z+=Z_STEP) 
			{
				p.transform(E[pe], isPolarized);
				pe = (pe+1) % VECTORS;
			}
		}
	}

	private void updatePanelIntensity() 
	{
		panel.setInitialIntensity(E0.intensity());
	}
}