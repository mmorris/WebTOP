package org.webtop.module.twoslitphoton;



import org.webtop.util.*; 
import org.webtop.util.Animation.*;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;
import org.web3d.*;
import java.applet.AudioClip;
import org.webtop.x3d.output.*;
import sun.audio.*;
import java.io.*;


public class Engine implements AnimationEngine {
	
	private TwoSlitPhoton wapp;
	private Animation animation;
	
	private static final int  MAX_COUNT=50000;
	private static final float  Z=1000;
	private static final float  X_SCALE=20;
	private static final int UPDATE=0;
	private static final int NO_UPDATE=1;
	
	private final SAI sai;

	private float t=0;
	private float periods=0;
	private int photonsPerPeriod;
	private float difference;
	private float remainder;
	
	private float wavelength;
	private float width;
	private float distance;
	private int count = 0;

	private TwoSlitPhoton.Data curData;
	
	private AudioStream click;
	private boolean muted = true;
	

	public static final float MAX_WIDTH=100000,MAX_HEIGHT=100000,DEF_WIDTH=1000,DEF_HEIGHT=300;

	private MFVec3f pointCoords;
	private SFFloat set_X;
	private SFFloat set_Y;
	private SFFloat set_Z;
	private MFColor pointColors;
	
	private static final float RES = 0.25f;
	private float X_size=DEF_WIDTH;
	private float Y_size=DEF_HEIGHT;
	private int X_res=(int)(X_size*RES);
	private int Y_res=(int)(Y_size*RES);
	private static final boolean COLOR_PER_VERTEX=true;
	private IFSScreen ifs;
	private IndexedSet is;
	private float XOrigin;
	private float YOrigin;
	private final float xGridUnit = X_size/X_res;
	private final float yGridUnit = Y_size/Y_res;

	// for coloring
	float[] target;
	
	float[][] colors=new float[X_res*Y_res][3];
	float[] intensities=new float[X_res*Y_res];
	//constructor
	public Engine(TwoSlitPhoton _wapp, SAI _sai){
		sai = _sai;
		wapp = _wapp; 
		
		//getCodeBase() and getAudioClip() are undefined.  will have to look up the way to do this[JD]
		//click=wapp.getAudioClip(wapp.getCodeBase(),"click5.au"); // old way
		try {
			InputStream sndFile = new FileInputStream( "click5.au" );
			click = new AudioStream( sndFile );
		} catch( Exception e ) {
				DebugPrinter.println( "Audio goes kaboom, probably file not found" );
		}
		
		//pretty sure getEI maps to getInputField(), if problems arise, they will be here [JD]
		set_X = (SFFloat) sai.getInputField("IFSMover","translation_in_x");
		set_Y = (SFFloat) sai.getInputField("IFSMover","translation_in_y");
		set_Z = (SFFloat)sai.getInputField("IFSMover","translation_in_z");
		
		ifs=new IFSScreen(new IndexedSet(sai,sai.getNode("IFS")),new int[][] {},X_size,Y_size);
		ifs.setResolution(X_res,Y_res);
		ifs.setup();
		
		set_X.setValue(-X_size/2);
		set_Y.setValue(Y_size/2);
		set_Z.setValue(-Z);

		colors=new float[X_res*Y_res][3];
		intensities=new float[X_res*Y_res];
		
		float[][] imgpts = new float[count][];

		XOrigin = X_size/2;
		YOrigin = -Y_size/2;
		
	}

	public void execute(Data d) {
		if(curData!=d) {	
			curData=(TwoSlitPhoton.Data)d;
			wavelength=curData.wavelength;
			width=curData.width;
			distance=curData.distance;
		}
		
		int photons = photonsPerPeriod;
		
		if (remainder>=1) {
			photons+=1;
			remainder-=1;
		}
		
		for (int j=0; j<photons; j++) {
			while(!tryPhoton((float)((Math.random()*20000)-10000),(float)((Math.random()*300)-150),(float)(Math.random()))) {};
			if (!muted) AudioPlayer.player.start( click );
			
			if (t>=wapp.getExposureTime()) {
				wapp.setStopped();
			}
			
			if (count==MAX_COUNT) {
				wapp.setStopped();
				count = 0;
				clearScreen(NO_UPDATE);
			}
		}
		
		remainder+=difference;
		if (!animation.isPaused())
			updateScreen();	

	}

	public void init(Animation a) {
		animation = a; 
		clearScreen(UPDATE);
	}

	public boolean timeElapsed(float periods) {
		t+=animation.getPeriod()/1000f;
		wapp.setElapsedTime(t);
		periods+=1;
		return true;
	}
	
	public void setT(float time) {
		t = time;
	}
	
//////////////////////////////////////////////////////////////////////////
	// Description:
	//		Computes the intensity for the 2 slit aperture
	//////////////////////////////////////////////////////////////////////////
	//
	// alpha(x, z) = (Pi / lambda) * w * sin(theta)
	//						 ~ (Pi / lambda) * w * (x / z)
	//
	// beta(x, z) = (Pi / lambda) * d * sin(theta)
	//						~ (Pi / lambda) * d * (x / z)
	//
	//					{ [sin(alpha(x, z)) / alpha(x, z)]^2					 if x != 0
	// f(x, z) =		{
	//					{ 1														 if x = 0
	//					
	// g(x, z) =		{ [cos(beta(x, z))]^2	 if beta != m * Pi
	//
	// and m is in Z.
	//
	// I(x, z) = f(x, z) * g(x, z)
	//
	//////////////////////////////////////////////////////////////////////////
	public static float
	compute2SlitIntensity(float lz, // lambda * z
												float w,	// width
												float x,	// x
												float z,	// z
												float d)	// distance
	{
		final float
			xp = x * (float) Math.PI,		// x * Pi
			alpha = xp * w / lz,				// alpha(x, z)
			beta = xp * d / lz;					// beta(x, z)
		float f, g;										// f(x, z), g(x, z)

		if(x == 0.0f)
			f = 1;
		else
			f = (float) Math.pow(Math.sin(alpha) / alpha, 2);

		g = (float) Math.pow(Math.cos(beta), 2);

		return f * g;
	}
	
	public boolean tryPhoton(float x, float y, float ran) {
		float intensity = compute2SlitIntensity(wavelength*Z,width,x*1000,Z,distance);
		if (intensity>ran || intensity==1) {
			
			int xCoord = (int)(((XOrigin+x/X_SCALE)/xGridUnit));
			int yCoord = (int)((YOrigin+y)/yGridUnit);
			
			
			int index = Y_res*xCoord-yCoord;
			
			target = colors[index];
			WTMath.hls2rgb(target,WTMath.hue(wavelength),0.5f,1f);
			count++;
			
			wapp.setPhotonCount(count);
			return true;
		}
		else
			return false;
	}
	
	public void reset() {
		t = 0;
		wapp.setElapsedTime(0);
	}
	
	public void setRate(int rate){
		photonsPerPeriod = (int)(rate/(1000/animation.getPeriod()));
		difference = (float)(rate/(float)(1000/animation.getPeriod()))-photonsPerPeriod;
		remainder = 0;
	}
	
	public void setAudio(boolean on){
		muted = !on;
	}
	public void updateScreen() {
		ifs.setColors(colors);	
	}
	public void clearScreen(int update){
		count = 0;
		for (int i=0; i<X_res*Y_res; i++) {
			colors[i]=new float[] {0,0,0};
		}
		if (update==UPDATE) {
			wapp.setPhotonCount(0);
			ifs.setColors(colors);
		}
	}

}
