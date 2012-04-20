/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Animation.java
//Defines a class for doing thread-safe periodic animation.
//Davis Herring
//Created July 20 2002
//Updated April 7 2004
//Version 1.31

package org.webtop.util;

/**
 * An instance of <code>Animation</code> controls an animation or other
 * periodic process.  The calculation is performed by an instance of
 * <code>AnimationEngine</code>; the relationship is analogous to that of
 * <code>Thread</code> and <code>Runnable</code>.  <code>Animation</code> can
 * be subclassed to provide functionality, or an instance of
 * <code>AnimationEngine</code> can be given it upon construction, and it will
 * be used for the calculations.
 *
 * <p>An object implementing the <code>Animation.Data</code> interface can be
 * used to provide parameters to the engine without risk of interference in
 * its calculations.
 *
 * <p>No <code>Animation</code> method is blocking; it will not wait for some
 * action to be taken by the animation thread (like completing the current
 * execution).  It is thus impossible for a thread's accessing an
 * <code>Animation</code> object to cause deadlock with the animation thread,
 * even if the animation thread must (for other reasons) wait on some action
 * of the client thread.  This is true for any number of client threads:
 * although behavior can become erratic if conflicting requests are made of
 * the <code>Animation</code>, it will always be in a functional state
 * consistent with (at least some of) the calls made to it.
 *
 * <p>The animation thread is created and started before the
 * <code>Animation</code> constructor returns, or is never started if the
 * <code>Animation</code> constructor completes abruptly.  The default state
 * for an <code>Animation</code> is not playing (and not paused), so the
 * thread will not perform any calculations until calls to
 * <code>setPlaying()</code> and/or <code>update()</code> are made.
 */
public class Animation extends Thread implements AnimationEngine
{
	/**
	 * An interface for an arbitrary data object given to the animation engine.
	 * <code>Animation</code> objects must be able to replicate these objects to
	 * guarantee that supplying new data will not affect data in use.
	 */
	public interface Data {
		/**
		 * Returns a replica of this object.  Should generally return an object of
		 * the same type as the original.  Should not return null.
		 */
		public Data copy();
	}

	/**
	 * The number of <code>Animation</code> threads thus far created.
	 */
	private static int animationCounter;
	/**
	 * Returns the next thread ID number.
	 */
	private static synchronized int getNextCtr() {return animationCounter++;}

	private final AnimationEngine engine;
	private Data data,inData;		//data is used; inData is assigned to
	private volatile long period,minDelay;	//all in milliseconds

	private Object lock=new Object();	//for synchronization

	//These flags have a hierarchy of importance reflected by their order here;
	//if a variable is true the variables above it are ignored.

	//pause==true is distinct from setting play=false in that play's prior value
	//is masked instead of destroyed.

	//The manner in which pause and update are handled suggests that a different
	//implementation style (perhaps busy-updating so long as a widget is pausing
	//us) may be better/faster.  If so, mea culpa and good luck.  [Davis]

	//The thread must be notify()-ed when a 'not-playing' condition is to be
	//exited!
	private volatile boolean play,		//If true, animate continually
													 pause,		//If true, don't animate
													 update,	//If true, execute exactly one update without time-stepping
													 seppuku; //If true, kill thread
	private boolean interFrame=true;

	/**
	 * Constructs an <code>Animation</code> running the given
	 * <code>AnimationEngine</code> object.  The engine parameter may not be
	 * null.  See next constructor for other parameter details.
	 */
	public Animation(AnimationEngine e,Data d,long p) {this(e,d,p,false);}
	/**
	 * Constructs a self-running <code>Animation</code> with the given period
	 * and data object.  This constructor is for the use of classes that
	 * subclass <code>Animation</code> to provide functionality.
	 *
	 * @param p the number of milliseconds between animation frames.
	 * @param d the <code>Data</code> object containing whatever parameters are
	 *          needed by the calculations.  May be null without error;
	 *          however, if it is not null, then <code>getData()</code> will
	 *          never return null (which is useful).
	 */
	protected Animation(long p,Data d) {this(null,d,p,true);}
	private Animation(AnimationEngine e,Data d,long p,boolean subclass) {
		super("WebTOP Animation Thread #"+getNextCtr());
		//DEBUG:
		//ThreadWatch.add(this);

		if(!subclass && e==null)
			throw new NullPointerException("No AnimationEngine to use.");
		engine=(subclass?this:e);
		setPeriod(p);
		data=d;
		engine.init(this);
		start();		//Thread automatically runs, pausing immediately
	}

	/**
	 * Returns the period of this <code>Animation</code>.
	 *
	 * @return the period of this animation (in milliseconds).
	 */
	public long getPeriod() {return period;}
	/**
	 * Sets the period of this <code>Animation</code>.
	 *
	 * @param p the new period for this animation (in milliseconds).
	 * @exception IllegalArgumentException if <code>p</code> is not positive.
	 */
	public void setPeriod(long p) {
		if(p<=0) throw new IllegalArgumentException("Negative period: "+p);
		period=p;
	}
	/**
	 * Returns the minimum amount of time this animation will wait between
	 * frames.
	 *
	 * @return the minimum delay time of this animation (in milliseconds).
	 */
	public long getMinDelay() {return minDelay;}
	/**
	 * Specifies the minimum amount of time this animation must wait between
	 * frames.
	 *
	 * @param d the new minimum delay time for this animation (in milliseconds).
	 * @exception IllegalArgumentException if <code>d</code> is not positive.
	 */
	public void setMinDelay(long d) {
		if(d<0 || d>=period)
			throw new IllegalArgumentException("Delay value "+d+
																				 " not on [0,period ("+period+")].");
		minDelay=d;
	}

	/**
	 * Returns the data object associated with this <code>Animation</code>.
	 * There is no guarantee that objects returned by successive calls to
	 * getData() will be the same or that they will be different. However,
	 * getData() will only return null if no <code>Data</code> object was
	 * specified at construction and setData() has never been called for this
	 * <code>Animation</code>. Once getData() returns non-null for a given
	 * <code>Animation</code>, it will never again return null for that object.
	 *
	 * <p>The object returned by getData() may be modified and used with
	 * setData(); however, if multiple client threads invoke setData() and/or
	 * modify the return value of getData(), they must handle the
	 * synchronization to guarantee data is not lost or overwritten.
	 *
	 * @return a reference to a <code>Data</code> object, either the object most
	 *         recently given to setData(), or a newly allocated object
	 *         equivalent to the object currently in use.
	 */
	public Data getData() {
		synchronized(lock) {return (inData==null&&data!=null)?data.copy():inData;}
	}

	/**
	 * Sets the data object to be used by this <code>Animation</code>.  This may
	 * be invoked at any time without risk to the animation thread.  Until the
	 * next animation frame, the provided data object will simply be held (and
	 * can thus be modified with the expected effects); at the next animation
	 * frame (whether the calculation is performed or not), the most recent
	 * value provided through setData() will be copied and no longer referenced
	 * by this <code>Animation</code>.  Because it can be difficult to determine
	 * the status of the animation thread, calling setData() whenever a change
	 * is to be effected is the most reliable method.
	 *
	 * @param the <code>Data<code> object to use for subsequent iterations of
	 *            the animation.  Use null to discard any pending data and keep
	 *            the object currently in use.
	 */
	public void setData(Data d) {
		synchronized(lock) {inData=d;}
	}

	//There is little need for interrupt()-ion here; notify() if thread may be
	//wait()-ing, but otherwise state change(s) will be properly effected at
	//sleep()'s end.

	/**
	 * Activates or deactivates this <code>Animation</code>.  Note that if the
	 * animation is paused at the time of a <code>setPlaying(true)</code> call,
	 * the pause will remain in effect and no animation will occur until it is
	 * released.
	 *
	 * @param doPlay whether to proceed with the animation.
	 */
	public void setPlaying(boolean doPlay) {
		synchronized(lock) {
			play=doPlay;
			if(isRunning())
				lock.notify();											//Wake up the stoppy thread
		}
	}

	/**
	 * Suspends or resumes the operation of this <code>Animation</code>,
	 * overriding its 'playing' state (as affected by
	 * <code>setPlaying()</code>).  Use to temporarily stop the animation and
	 * then to resume its previous state.
	 *
	 * <p>Note that calling <code>setPaused(false)</code> does not guarantee
	 * animation; it simply allows it (this <code>Animation</code> must be set
	 * to play to animate).  Also, there is no guarantee that the change in the
	 * animation's state will be effected before this method returns.
	 *
	 * @param noPlay true to halt animation; false to allow it to continue.
	 */
	public void setPaused(boolean noPlay) {
		synchronized(lock) {
			pause=noPlay;
			if(isRunning())
				lock.notify();											//Wake up the sleepy thread
		}
	}

	/**
	 * Forces exactly one update as soon as possible, indicating to the engine
	 * for this iteration that no 'time' has passed.
	 */
	public void update() {
		synchronized(lock) {
			update=true;
			//Might as well keep the lock, since isRunning() needs it
			if(!isRunning())
				interrupt();													//Wake up the lazy thread
		}

		//It makes sense to give the animation thread a chance to DO something now.
		Thread.yield();
	}

	/**
	 * Attempts to perform an update in the calling thread.  Returns true if
	 * successful; if the animation thread is already performing an update,
	 * immediately returns false.  Using this method can be more efficient than
	 * using (just) <code>update()</code> when many updates are needed.
	 */
	public boolean updateHere() {
		synchronized(lock) {
			if(interFrame) {
				grabData();
				if(engine.timeElapsed(0)) engine.execute(data);
				return true;
			} else return false;
		}
	}

	/**
	 * Stops the <code>Animation</code> permanently and destroys its thread.
	 * Once killed, the <code>Animation</code> cannot be restarted.
	 *
	 * <p>Note that the thread may not actually die before this method returns;
	 * <code>isAlive()</code> can be used to check if needed.
	 */
	public void kill() {
		seppuku=true;
		interrupt();
	}

	/**
	 * Checks whether this <code>Animation</code> is currently set to run.
	 *
	 * @return the most recent value passed to <code>setPlaying()</code>, or
	 *         false if that function has never been called for this object.
	 */
	public boolean isPlaying() {return play;}
	/**
	 * Checks whether this <code>Animation</code> is currently being prevented
	 * from running.
	 *
	 * @return the most recent value passed to <code>setPaused()</code>, or
	 *         false if that function has never been called for this object.
	 */
	public boolean isPaused() {return pause;}
	/**
	 * Checks whether this <code>Animation</code> is currently running.
	 *
	 * @return true if this object is set to play and is not paused.
	 */
	public boolean isRunning() {
		//This uses two variables, and should thus be synchronized.
		synchronized(lock) {return play&&!pause;}
	}

	//Must be synchronized on lock to call this method.
	private void grabData() {
		if(inData!=null) {		//data for me!
			data=inData.copy();	//only we can see the real data
			inData=null;
		}
	}

	/**
	 * Runs the animation.
	 */
	public final void run() {
		long iterTime=-1,_period=period; // iterTime: start of [last] iteration
		while(!seppuku) {
			if(_period!=period) {
				_period=period;
				//Should there be more complicated behavior here?  If so, there likely
				//needs to be synchronization (or at least a local copy of period).
			}

			//DebugPrinter.println("\ni/"+System.currentTimeMillis()%1000);

			boolean immediate;			//will be true if update is
			synchronized(lock) {
				if(!update&&!isRunning()) {
					//Whether we finish or are interrupted, start over to see changes.
					try{lock.wait();} catch(InterruptedException e) {}
					iterTime=-1;
					continue;
				}

				//We're animating, or update is set, so calculate.
				grabData();
				immediate=update;
				update=false;
				interFrame=false;
			}

			//If it's an update(), it's been "0 time"; else, if we've just begun
			//running, it's been the ideal "1 period" by definition.  Otherwise see
			//how far behind we may have gotten:
			final float periods=immediate?0:iterTime==-1?1:
				(System.currentTimeMillis()-iterTime)/(float)_period;

			iterTime=System.currentTimeMillis();

			if(engine.timeElapsed(periods)) engine.execute(data);

			synchronized(lock) {interFrame=true;}

			if(!update) {							// skip attempt at sleep for efficiency
				final long tPrime=System.currentTimeMillis();
				do {
					final long left=Math.max(iterTime+_period-tPrime,minDelay)-
						(tPrime-System.currentTimeMillis()); // already slept this much
					if(left<=0) break;		// no more to sleep!
					try {
						sleep(left);
						break;							// sleep successful
					} catch(InterruptedException e) {}
				} while(!update && !seppuku);	// stop trying to sleep if there's news
			}
		}
	}

	//These functions implement AnimationEngine minimally.
	public void init(Animation a) {}
	//Returns true because returning false would always have to be overridden.
	public boolean timeElapsed(float periods) {return true;}
	public void execute(Data d) {}

	/*public static void main(String[] args) throws java.io.IOException {
		Animation me=new Animation(500,null) {
			long millis;
			public boolean timeElapsed(float periods)
			{System.out.println("te: "+periods); millis+=15*periods; return true;}
			public void execute(Animation.Data d) {
				System.out.print("exe");
				try{sleep(millis);}catch(InterruptedException e){}
				System.out.print('!');
				try{sleep(millis);}catch(InterruptedException e){}
				System.out.print('\n');
			}
		};
		me.setPlaying(true);
		System.in.read();
		me.kill();
	}*/
}
