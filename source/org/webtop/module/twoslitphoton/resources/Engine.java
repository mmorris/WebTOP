//Engine.java
//Animation engine for the TwoSlit module.
//Peter Gilbert
//Created June 15 2004
//Updated May 31 2005
//Version 0.1

package webtop.twoslit;

import webtop.util.*;
import webtop.vrml.*;
import vrml.external.field.*;
import java.applet.AudioClip;
import webtop.vrml.output.*;

public class Engine implements AnimationEngine
{
	
	private TwoSlit applet;
	private Animation animation;
	
	private static final int  MAX_COUNT=50000;
	private static final float  Z=1000;
	private static final float  X_SCALE=20;
	private static final int UPDATE=0;
	private static final int NO_UPDATE=1;
	
	private final EAI eai;

	private float t=0;
	private float periods=0;
	private int photonsPerPeriod;
	private float difference;
	private float remainder;
	
	private float wavelength;
	private float width;
	private float distance;
	private int count = 0;

	private TwoSlit.Data curData;
	
	private AudioClip click;
	private boolean muted = true;
	
	//old way
	//private float[][] imgpts = new float[MAX_COUNT][];
	//private float[][] imgcolors = new float[MAX_COUNT][3];
	//private float[][] whitePoints = new float[MAX_COUNT][3];
	//private EventInMFVec3f pointCoords;
	//private EventInMFColor pointColors;
	//private EventInMFColor whitePointColors;
	
	public static final float MAX_WIDTH=100000,MAX_HEIGHT=100000,DEF_WIDTH=1000,DEF_HEIGHT=300;

	private EventInMFVec3f pointCoords;
	private EventInSFFloat set_X;
	private EventInSFFloat set_Y;
	private EventInSFFloat set_Z;
	private EventInMFColor pointColors;
	
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
	//final float[][] colors=new float[X_res*Y_res][3];
	//final float[] intensities=new float[X_res*Y_res];
	float[][] colors=new float[X_res*Y_res][3];
	float[] intensities=new float[X_res*Y_res];

	public Engine(TwoSlit twoslit, EAI _eai) {
		applet = twoslit;
		eai=_eai;
		
		click=applet.getAudioClip(applet.getCodeBase(),"click5.au"); 
		
		set_X = (EventInSFFloat) eai.getEI("IFSMover","translation_in_x");
		set_Y = (EventInSFFloat) eai.getEI("IFSMover","translation_in_y");
		set_Z = (EventInSFFloat) eai.getEI("IFSMover","translation_in_z");
		
		ifs=new IFSScreen(new IndexedSet(eai,eai.getNode("IFS")),new int[][] {},X_size,Y_size);
		ifs.setResolution(X_res,Y_res);
		ifs.setup();
	
		set_X.setValue(-X_size/2);
		set_Y.setValue(Y_size/2);
		set_Z.setValue(-Z);

		colors=new float[X_res*Y_res][3];
		intensities=new float[X_res*Y_res];
		
		/*for (int i=0; i<X_res*Y_res; i++)
			intensities[i]=0f;*/
		
		float[][] imgpts = new float[count][];

		XOrigin = X_size/2;
		YOrigin = -Y_size/2;
		
		// old way
		//pointCoords = (EventInMFVec3f) eai.getEI("ImagePoints","point");
		//pointColors = (EventInMFColor) eai.getEI("ImageColors","color");
		//whitePointColors = (EventInMFColor) eai.getEI("WhitePoints","color");
		//for (int p = 0; p<MAX_COUNT; p++)
		//	whitePoints[p]=new float[] {1,1,1};
		//whitePointColors.setValue(whitePoints);
	}

	public void init(Animation anim) {
		animation = anim;
		//t_step=anim.getPeriod()/1000f;
		clearScreen(UPDATE);
	}

	public boolean timeElapsed(float periodsElapsed) {
		//t+=periods;
		t+=animation.getPeriod()/1000f;
		applet.setElapsedTime(t);
		periods+=1;
		//System.out.println(periodsElapsed);
		return true;
	}

	public synchronized void execute(Animation.Data d) {
		if(curData!=d) {	
			curData=(TwoSlit.Data)d;
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
			if (!muted) click.play();
			
			if (t>=applet.getExposureTime()) {
				applet.setStopped();
			}
			
			if (count==MAX_COUNT) {
				applet.setStopped();
				count = 0;
				clearScreen(NO_UPDATE);
			}
		}
		
		remainder+=difference;
		if (!animation.isPaused())
			updateScreen();	
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
			
			/*intensities[index]+=0.5;
			if (intensities[index]>1)
				intensities[index]=1;*/
			
			target = colors[index];
			WTMath.hls2rgb(target,WTMath.hue(wavelength),0.5f,1f);
			count++;
			
			//old way
			/*imgpts[count]=new float[] {x/X_SCALE,y,10};
			float[] target = imgcolors[count];
			WTMath.hls2rgb(target,WTMath.hue(wavelength),.5f,1f);
			//if (count%10==0) {
			//	float[][] imgptsNew = new float[count][];
			//	float[][] imgcolorsNew = new float[count][3];
			//	System.arraycopy(imgpts,0,imgptsNew,0,count);
			//	System.arraycopy(imgcolors,0,imgcolorsNew,0,count);
			//	pointCoords.setValue(imgptsNew);
			//	pointColors.setValue(imgcolorsNew);
			//}
			count++;
			*/
			
			applet.setPhotonCount(count);
			return true;
		}
		else
			return false;
	}
	
	public void reset() {
		t = 0;
		applet.setElapsedTime(0);
		//float[][] imgptsNew = new float[count][];
		//float[][] imgcolorsNew = new float[count][3];
		//pointCoords.setValue(imgptsNew);
		//pointColors.setValue(imgcolorsNew);
		//pointCoords.setValue(imgpts);
		//pointColors.setValue(imgcolors);
	}
	
	public void clearScreen(int update) {
		count = 0;
		for (int i=0; i<X_res*Y_res; i++) {
			colors[i]=new float[] {0,0,0};
		}
		if (update==UPDATE) {
			applet.setPhotonCount(0);
			ifs.setColors(colors);
		}
		
		// old way
		/*for (int i=0; i<MAX_COUNT; i++) {
			imgpts[i]=new float[] {-500,-150,0};
			imgcolors[i]=new float[] {0,0,0};
		}
		if (update==UPDATE) {
			applet.setPhotonCount(0);
			pointCoords.setValue(imgpts);
			pointColors.setValue(imgcolors);
		}*/
	}
	
	public void setAudio(boolean on) {
		muted=!on;
	}
	
	public void setRate(int rate) {
		photonsPerPeriod = (int)(rate/(1000/animation.getPeriod()));
		difference = (float)(rate/(float)(1000/animation.getPeriod()))-photonsPerPeriod;
		remainder = 0;
	}
	
	public void updateScreen() {
		ifs.setColors(colors);	
		//old way
		/*pointCoords.setValue(imgpts);
		pointColors.setValue(imgcolors);*/
	}
	
	
}
