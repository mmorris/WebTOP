/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Updated May 13 2004

package org.webtop.wsl.client;

import java.applet.Applet;
import java.net.*;
import java.io.*;
import java.util.*;
//import org.xml.sax.*;

//import org..external.field.*;

import org.webtop.util.*;
import org.webtop.wsl.script.*;
import org.webtop.wsl.event.*;

/**
 * <code>WSLPlayer</code> is the central class of WSLAPI.	 Most of the features
 * provided by WSLAPI are handled in this class.	In essence, it provides
 * methods to record, load, and playback scripts, plus other methods to set and
 * get certain attributes.	The methods that deal with recording, loading, and
 * playing are listed as follow:
 *
 * <ul>
 *	 <li> <a href="#load"><code>load()</code></a>
 *				-- Loads a script from an InputStream object, a String representing a filename, or from a URL
 *	 <li> <a href="#play"><code>play()</code></a>
 *				-- Start playing the loaded script
 *	 <li> <a href="#pause"><code>pause()</code></a>
 *				-- Pause the playback of a script
 *	 <li> <a href="stop"><code>record()</code></a>
 *				-- Start recording a script
 *	 <li> <a href="stop"><code>stop()</code></a>
 *				-- Stop playing or recording of a script
 *	 <li> <a href="reset"><code>reset()</code></a>
 *				-- Reset the playback to the beginning of a script
 *	 <li> <a href="unload"><code>unload()</code></a>
 *				-- Unloads a script
 * </ul>
 *
 * <p>Not all methods are accessible in a state that <code>WSLPlayer</code>
 * might be in.  For example, when <code>WSLPlayer</code> is playing,
 * <code>record()</code> should not be called; <strong>such calls will raise
 * an <code>IllegalStateException</code></strong>.  However, for convenience,
 * the <code>record</code>* methods return silently when not recording.
 *
 * <p><code>WSLPlayer</code> uses an event model that is similar to the one
 * used in Java's Abstract Windowing Toolkit (AWT) package.	 In WSLAPI,
 * <code>WSLPlayer</code> generates events during state transitions, and
 * during playback of a script.	 Objects interested in responding to these
 * events can register themselves through the <code>addListener()</code>
 * method.	In addition, those objects should implement one or more of these
 * interfaces: <code>WSLScriptListener</code>, <code>WSLPlayerListener</code>,
 * and <code>WSLProgressListener</code>.	These interfaces correspond to the
 * three types of events that <code>WSLPlayer</code> generates:
 * <code>WSLScriptEvent</code>, <code>WSLPlayerEvent</code>, and
 * <code>WSLProgressEvent</code>.	 These classes contain various constants
 * used as event-type identifiers.
 *
 * <p>Note that there is no specialized
 * <code>add</code><i>Event</i><code>Listener()</code> method for each
 * different type of event.	 Instead, the generalized
 * <code>addListener()</code> method is used.	 <code>WSLPlayer</code>
 * determines which types of events to fire to which objects based on the
 * interfaces that the objects implement.</p>
 *
 * <p>There are ten types of WebTOP user interactions that can be recorded
 * using WSLPlayer: <code>OBJECT_ADDED</code>, <code>OBJECT_REMOVED</code>,
 * <code>MOUSE_ENTERED</code>, <code>MOUSE_EXITED</code>,
 * <code>MOUSE_PRESSED</code>, <code>MOUSE_RELEASED</code>,
 * <code>MOUSE_DRAGGED</code>, <code>ACTION_PERFORMED</code>,
 * <code>VIEWPOINT_CHANGED</code>, and <code>VIEWPOINT_SELECTED</code>.	 There
 * is a corresponding <code>record<i>&lt;Interaction&gt;</i>()</code> method
 * for each type of user interaction.</p>
 *
 * @author Yong Tze Chi
 * @author Davis Herring
 */
public class WSLPlayer implements Runnable/*,EventOutObserver*/ {
	public static final int MAJOR_VERSION=3,MINOR_VERSION=3,REVISION=4;
	public static final String VERSION=WTString.versionString(MAJOR_VERSION,MINOR_VERSION,REVISION);

	//The maximum amount of time to wait before posting progress, etc. (ms)
	private static final long MAX_IDLE=20;

	public static final String DEFAULT_APPLET_PARAMETER="wslscript";

	//Action names
	private static final String VIEWPOINT_CHANGED="viewpointChanged",
															VIEWPOINT_SELECTED="viewpointSelected",
															OBJECT_ADDED="objectAdded",
															OBJECT_REMOVED="objectRemoved",
															ACTION_PERFORMED="actionPerformed",
															MOUSE_IN="mouseEntered",
															MOUSE_OUT="mouseExited",
															MOUSE_DOWN="mousePressed",
															MOUSE_UP="mouseReleased",
															MOUSE_DRAG="mouseDragged";

	private static final String EXCEPTION_MSG="Exception occurred during WSL event handling:";

	private final WSLModule module;

	private WSLScript script;
	private WSLNode moduleNode,scriptNode;

	private int scriptIndex;

	//State; thread control (these do need to be volatile, yes?)
	private volatile boolean playing,paused,recording;

	//A suffixed T indicates a logical time variable expressed in the ideal
	//world of the script; a suffixed Time represents a physical value gotten
	//(at some point or another) from System.currentTimeMillis().  Physical and
	//logical values cannot be mixed, but the difference between two of one kind
	//can be mixed with the other.
	private long totalScriptT;		// length of script
	private long lastEventTime;		// real time at which last event was sent
	private long scriptT,lastEventT,nextEventT;	// logical times of interest

	private long prevRecordTime;	// when we last recorded something

	private final Vector<WSLPlayerListener> listeners=new Vector<WSLPlayerListener>();

	private Thread thread;
	private final Object lock=new Object();	// for synchronization

//===============
// EAI interface
//===============
// 	private EventInSFBool		navPanel_setEnabled;
// 	private EventInMFFloat	set_view;
// 	private EventInSFInt32	set_activeView;
// 	private EventInSFBool		query_view;
// 	private EventOutMFFloat view_changed;
// 	private EventOutSFInt32 activeView_changed;
// 	private EventOutMFFloat queryView_changed;
//	private final float view[] = new float[6];

// 	private static final int VIEW_CHANGED=1,ACTIVEVIEW_CHANGED=2,
// 													 QUERYVIEW_CHANGED=3;

	private boolean viewpointEventEnabled=true,playbackVPEnabled;

	/**
	 * The constructor of <code>WSLPlayer</code>, requires a reference of
	 * <code>WSLModule</code> it is tied to during runtime.
	 *
	 * @param module reference to a class that implements the
	 *							 <code>WSLModule</code> interface.
	 */
	public WSLPlayer(WSLModule module) {
		if(module==null) throw new NullPointerException("no module given");
		this.module=module;
	}

//===========================
// Principal control methods
//===========================

	//Loads script; does not post state changed event, as more may need
	//to be done before that event can be reasonably generated.	 Nor does
	//it use the title of the loaded script.
	private boolean loadScript(InputStream is) throws WSLParser.InvalidScriptException {
		if(loaded()) throw new IllegalStateException("already loaded a script");
		//state = LOADING;

		script = new WSLParser(module.getWSLModuleName()).parse(is);
		scriptNode = script.getScriptNode();
		moduleNode = script.getModuleNode();

		if(scriptNode!=null) {
			scriptIndex=0;
			calculateScriptLength();
		}

		//initNavigationPanelInfo();

		postInitialize();
		return true;
	}

	/**
	 * Tries to load a script specified by the default applet parameter.
	 *
	 * @see #loadParameter(Applet,String)
	 */
	public boolean loadParameter(Applet app) {
		return loadParameter(app,DEFAULT_APPLET_PARAMETER);
	}

	/**
	 * Tries to load a script specified by the given applet parameter.	Unlike
	 * other <code>load</code>*<code>()</code> methods,
	 * <code>loadParameter()</code> throws no exceptions; if there is no
	 * parameter by the given name, or if no valid script can be read from the
	 * location it specifies, an error message is printed to standard error and
	 * the call returns false.
	 *
	 * @param app the applet whose parameters to examine.
	 * @param param the parameter to read.
	 * @return true if the script was successfully loaded; false if there was no
	 *				 script or if it could not be loaded.
	 *
	 * @see webtop.wsl.event.WSLPlayerEvent#SCRIPT_LOADED
	 */
	public boolean loadParameter(Applet app,String param) {
		final String epfx="WSLPlayer::loadParameter: ";
		String script=app.getParameter(param);
		if(script==null || script.length()==0)
			DebugPrinter.println(epfx+"No '"+param+"' parameter specified.");
		else {
			try {
				load(new URL(app.getCodeBase(),script));
				return true;
			}
			catch(Exception e) {
				System.err.print(epfx+"could not load '"+script+"'; encountered ");
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Loads a script from the given URL.	 If this call returns normally the
	 * script was successfully loaded.
	 *
	 * @param	 url	location of the script file to be loaded.
	 *
	 * @exception IOException if the given URL was invalid or could not be read.
	 * @exception WSLParser.InvalidScriptException if the given URL did not
	 *																						 contain a WSL script valid
	 *																						 for the current module.
	 * @exception SecurityException if access to the given URL was denied by
	 *															Java.
	 *
	 * @see webtop.wsl.event.WSLPlayerEvent#SCRIPT_LOADED
	 */
	public void load(URL url) throws IOException,WSLParser.InvalidScriptException {
		loadScript(url.openStream());
		final String str=url.toString();
		int index = Math.max(str.lastIndexOf('/'), str.lastIndexOf('\\'));
		script.setTitle(index>=0 ? str.substring(index+1) : str);

		postPlayerStateChanged(WSLPlayerEvent.SCRIPT_LOADED);
	}

	/**
	 * Loads a script from the file whose name is given.	If this call returns
	 * normally the script was successfully loaded.
	 *
	 * @param	 filename	 the name of the file from which to read a script.
	 *
	 * @exception IOException if the file could not be read.
	 * @exception WSLParser.InvalidScriptException
	 *	 if the file did not contain a WSL script valid for the current module.
	 * @exception SecurityException if access to the file was denied by Java.
	 *
	 * @see webtop.wsl.event.WSLPlayerEvent#SCRIPT_LOADED
	 */
	public void load(String filename)
		throws IOException,WSLParser.InvalidScriptException {
		FileInputStream fin = new FileInputStream(filename);

		if(!loadScript(fin)) return;

		int index=Math.max(filename.lastIndexOf('/'),filename.lastIndexOf('\\'));
		if(index>=0) filename=filename.substring(index+1);

		script.setTitle(filename);

		postPlayerStateChanged(WSLPlayerEvent.SCRIPT_LOADED);
	}

	/**
	 * Loads a script from the specified <code>InputStream</code>.	If this call
	 * returns normally the script was successfully loaded.
	 *
	 * @param	 in	 the stream from which the script is to be loaded.
	 * @exception WSLParser.InvalidScriptException
	 *	 if problems are encountered reading the stream as a script.
	 *
	 * @see webtop.wsl.event.WSLPlayerEvent#SCRIPT_LOADED
	 */
	public void load(InputStream in) throws WSLParser.InvalidScriptException {
		if(loadScript(in)) postPlayerStateChanged(WSLPlayerEvent.SCRIPT_LOADED);
	}

	/**
	 * Unloads the script that's kept in <code>WSLPlayer</code>.	This method
	 * may only be called while a script is loaded but not playing.
	 *
	 * @see webtop.wsl.event.WSLPlayerEvent#SCRIPT_UNLOADED
	 */
	public void unload() {
		//This first test catches the recording state as well
		if(!loaded()) throw new IllegalStateException("no script to unload");
		if(isPlaying()) throw new IllegalStateException("script busy");

		script = null;
		scriptNode = null;

		postPlayerStateChanged(WSLPlayerEvent.SCRIPT_UNLOADED);
	}

	/**
	 * Starts or resumes playback of a previously loaded script. A thread is
	 * started to play the script. This method is only accessible when a script
	 * is loaded but not playing, or else playing but paused.
	 *
	 * @see webtop.wsl.event.WSLPlayerEvent#PLAYER_STARTED
	 */
	public void play() {
		//This first test catches the recording state as well
		if(!loaded()) throw new IllegalStateException("no script to play");
		if(isPlaying() && !isPaused()) throw new IllegalStateException("script already playing");
		if(!script.isPlayable()) throw new IllegalStateException("script not playable");

		if(isPlaying()) {						// and thence, paused
			synchronized(lock) {
				paused=false;
				lock.notify();
			}
		} else {										// not playing
			playing=true;

			postInitialize();

			thread=new Thread(this,"WSL playback thread");
			thread.start();
		}
		postPlayerStateChanged(WSLPlayerEvent.PLAYER_STARTED);
	}

	/**
	 * Pauses the playback of a script.  This method is only accessible while
	 * playing but not paused.
	 *
	 * @see webtop.wsl.event.WSLPlayerEvent#PLAYER_PAUSED
	 */
	public void pause() {
		if(!isPlaying()) throw new IllegalStateException("script not playing");
		if(isPaused()) throw new IllegalStateException("script already paused");

		paused=true;
		thread.interrupt();					// keep it from going on to the next event

		postPlayerStateChanged(WSLPlayerEvent.PLAYER_PAUSED);
	}

	/**
	 * Prepares <code>WSLPlayer</code> for recording user interactions.  It
	 * first calls <code>WSLModule.toWSLModule()</code> and obtains the current
	 * viewpoint to obtain the current state of the WebTOP module. This method
	 * is only accessible when there is no script loaded and recording is not
	 * already taking place.
	 *
	 * @see webtop.wsl.event.WSLPlayerEvent#RECORDER_STARTED
	 */
	public void record() {
		if(loaded() || isRecording()) throw new IllegalStateException("player busy");

		recording=true;

		script = new WSLScript();
		moduleNode = module.toWSLNode();

		//initNavigationPanelInfo();

		//if(query_view!=null) query_view.setValue(true);

		script.addModuleNode(moduleNode);

		scriptNode = new WSLNode(WSLScript.SCRIPT_TAG);
		script.addScriptNode(scriptNode);

		prevRecordTime = System.currentTimeMillis();

		postPlayerStateChanged(WSLPlayerEvent.RECORDER_STARTED);
	}

	/**
	 * Resets the playback of a script to the beginning of the script.	This
	 * method is only accessible when a script is loaded but not playing.
	 *
	 * @see webtop.wsl.event.WSLPlayerEvent#PLAYER_RESET
	 */
	public void reset() {
		//This first test catches the recording state as well
		if(!loaded()) throw new IllegalStateException("no script loaded");
		if(isPlaying()) throw new IllegalStateException("script busy");

		//Should a progress event of 0 be sent here?
		//No, because progress events are only meaningful during playback.
		postInitialize();
		postPlayerStateChanged(WSLPlayerEvent.PLAYER_RESET);
	}

	/**
	 * Stops the playback or recording of a script.	 This method can only be
	 * called when <code>WSLPlayer</code> is playing or recording a script.
	 *
	 * @see webtop.wsl.event.WSLPlayerEvent#PLAYER_STOPPED
	 * @see webtop.wsl.event.WSLPlayerEvent#RECORDER_STOPPED
	 */
	public void stop() {
		if(isPlaying()) {
			//Assert stoppage on the thread
			playing=paused=false;
			synchronized(lock) {lock.notify();}
			while(true) try{thread.join(); break;} catch(InterruptedException e) {}
		} else if(isRecording()) {
			scriptIndex = 0;
			recordAction(WSLScript.SCRIPT_END_TAG, null, null, null);
			calculateScriptLength();

			recording=false;

			postPlayerStateChanged(WSLPlayerEvent.RECORDER_STOPPED);
		} else if(loaded()) throw new IllegalStateException("script not active");
		else throw new IllegalStateException("no script to stop");
	}

//=========
// Options
//=========

	/**
	 * Returns whether viewpoint events are posted.	 When viewpoint events are
	 * ignored, the user is allowed to manipulate the viewpoint during the
	 * playback of a script.	Otherwise, the navigation panel is locked during
	 * playback.
	 *
	 * @return	<code>true</code> if viewpoint events are posted;
	 *					<code>false</code> otherwise.
	 */
	public boolean isViewpointEventEnabled() {return viewpointEventEnabled;}

	/**
	 * Sets the option that whether viewpoint events are posted during playback.
	 * When viewpoint events are enabled, they are posted during playback, and
	 * the user is not allowed to manipulate the module's viewpoint themselves.
	 * When viewpoint events are disabled, the user can use the navigation panel
	 * to manipulate the module's viewpoint during playback.
	 *
	 * @param enabled <code>true</code> if viewpoint events are to be enabled;
	 *								<code>false</code> if viewpoint events are to be disabled.
	 */
	public void setViewpointEventEnabled(boolean enabled) {
		viewpointEventEnabled=enabled;
	}

//========================
// Informational routines
//========================

	/**
	 * Checks whether a script is loaded.  Note that scripts currently being
	 * recorded are not yet loaded.
	 *
	 * @return true if a script is loaded; false otherwise
	 */
	public boolean loaded() {return script!=null && !isRecording();}

	/**
	 * Checks whether a script is playing.
	 *
	 * @return true if this <code>WSLPlayer</code> is playing back a script;
	 *         false otherwise
	 */
	public boolean isPlaying() {return playing;}

	/**
	 * Checks whether script playback is paused.  This can only be the case if a
	 * script is being played.
	 *
	 * @return true if this <code>WSLPlayer</code> is paused during a script;
	 *         false otherwise
	 */
	public boolean isPaused() {return paused;}

	/**
	 * Checks whether a script is being recorded.  Note that this is not the
	 * case once recording completes.
	 *
	 * @return true if a script is being recorded; false otherwise
	 */
	public boolean isRecording() {return recording;}

	/**
	 * Returns the WSLModule that is tied to WSLPlayer.
	 *
	 * @return	the WSLModule given this <code>WSLPlayer</code> at construction.
	 */
	public WSLModule getWSLModule() {return module;}

	/**
	 * Returns the <code>WSLScript</code> instance that <code>WSLPlayer</code>
	 * keeps in memory.
	 *
	 * @return	reference to this object's <code>WSLScript</code>;
	 *					<code>null</code> if this object does not currently have a
	 *					<code>WSLScript</code> loaded.
	 */
	public WSLScript getScript() {return script;}

	/**
	 * Gets the current time in the script being played back.
	 *
	 * @return		time of the script being played back in milliseconds.
	 */
	public long getCurrentScriptTime() {return Math.min(scriptT,totalScriptT);}

	/**
	 * Gets the total time of the script kept in <code>WSLPlayer</code>
	 *
	 * @return		total time of the script in milliseconds.
	 */
	public long getTotalScriptTime() {return totalScriptT;}

//===================
// Internal routines
//===================

	private void calculateScriptLength() {
		totalScriptT = 0;
		for(int i=0;i<scriptNode.getChildCount();i++)
			totalScriptT += scriptNode.getChild(i).getTimeStamp();
	}

	/*private void initNavigationPanelInfo() {
		NamedNode nn = module.getNavigationPanelNode();
		if(nn!=null && navPanel_setEnabled==null) {
			navPanel_setEnabled =
				(EventInSFBool) nn.node.getEventIn("set_enabled");
			set_view =
				(EventInMFFloat) nn.node.getEventIn("set_view");
			set_activeView =
				(EventInSFInt32) nn.node.getEventIn("set_activeView");
			query_view =
				(EventInSFBool) nn.node.getEventIn("query_view");

			EAI.getEO(nn,"view_changed",this,new Integer(VIEW_CHANGED),null);
			EAI.getEO(nn,"activeView_changed",this,
								new Integer(ACTIVEVIEW_CHANGED),null);
			EAI.getEO(nn,"queryView_changed",this,
								new Integer(QUERYVIEW_CHANGED),null);
		}
		}*/

	/*private void setView(String value) {
		if(value==null) throw new NullPointerException("null view");
		if(set_view==null) return;

		int index=0;
		for(int i=0;i<6;i++) {
			try {
				//Our own tokenizer; should probably actually write a silly
				//string-splitter class.	(StreamTokenizer doesn't handle scientific
				//notation at all.) [Davis]
				String next=WTString.delimited(value,index,' ');
				index+=next.length()+1;
				view[i]=new Float(next.trim()).floatValue();
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException("bad view: "+value);
			}
		}

		set_view.setValue(view);
	}*/

//================
// Thread methods
//================

	//Waits until it is next appropriate to dispatch an event, or until the
	//playback has been stopped.  When this method returns, the playback will
	//not be paused.
	private void delay() {
		if(scriptIndex>=scriptNode.getChildCount()) return; // nothing left!

		final long nextEventT=lastEventT+scriptNode.getChild(scriptIndex).getTimeStamp();
		while(isPlaying()) {
			synchronized(lock) {
				if(isPaused()) {
					//We treat the pausing as if it were a dispatched event:
					lastEventT+=System.currentTimeMillis()-lastEventTime;
					scriptT=lastEventT;
					while(isPaused())
						try{lock.wait();} catch(InterruptedException e) {}
					lastEventTime=System.currentTimeMillis();
				}
			}
			if(!isPaused()) {
				//Note that scriptT may not == nextEventT when this test fails; this
				//will make playback on the whole more accurate.
				if(scriptT<nextEventT) {
					//This is a bit strange; it amounts to 'always do the time-handling
					//stuff, and skip the rest if we were interrupted'.
					try {Thread.sleep(Math.min(nextEventT-scriptT,MAX_IDLE));}
					catch(InterruptedException e) {continue;}
					finally {
						scriptT=System.currentTimeMillis()-lastEventTime+lastEventT;
						postProgress();			// there perhaps should be a condition on this
					}
				} else return;
			}
		}
	}

	//Dispatches the next event (immediately), if there is one
	private void dispatch() {
		lastEventTime=System.currentTimeMillis();

		if(scriptIndex < scriptNode.getChildCount()) {
			lastEventT+=scriptNode.getChild(scriptIndex).getTimeStamp();

			final WSLNode action = scriptNode.getChild(scriptIndex);

// 			if(action.getName().equals(VIEWPOINT_CHANGED)) {
// 				if(playbackVPEnabled)
// 					setView(action.getAttributes().getValue(WSLNode.VALUE));
// 			} else if(action.getName().equals(VIEWPOINT_SELECTED)) {
// 				if(playbackVPEnabled && set_activeView!=null)
// 					set_activeView.setValue(action.getAttributes().getIntValue(WSLNode.VALUE,0));
// 			}

			if(!action.getName().equals(WSLScript.SCRIPT_END_TAG) &&
				 (playbackVPEnabled || (!action.getName().equals(VIEWPOINT_CHANGED) &&
																!action.getName().equals(VIEWPOINT_SELECTED))))
				postScriptAction(new WSLScriptEvent(this, action, module.getWSLModuleName()));

			scriptIndex++;
		}
	}

	/**
	 * This method is the thread started to play the script.	It gradually scans
	 * through the script and posts script actions at timed intervals.	When the
	 * script is finished, a <code>PLAYER_STOPPED</code> event is posted.
	 *
	 * @see webtop.wsl.event.WSLPlayerEvent#PLAYER_STOPPED
	 */
	public void run() {
		//DEBUG:
		//ThreadWatch.add(Thread.currentThread());

		//We need our own copy so that mid-script changes don't confuse things
		playbackVPEnabled=viewpointEventEnabled;

// 		if(navPanel_setEnabled!=null && playbackVPEnabled)
// 			navPanel_setEnabled.setValue(false);

		scriptIndex=0;
		scriptT=0;
		lastEventT=0;
		lastEventTime=System.currentTimeMillis();

		while(scriptIndex<scriptNode.getChildCount()) {
			delay();
			if(!isPlaying()) break;
			dispatch();
		}

// 		if(navPanel_setEnabled!=null && playbackVPEnabled)
// 			navPanel_setEnabled.setValue(true);

		playing=false;							// this can be redundant, but is always ok

		postPlayerStateChanged(WSLPlayerEvent.PLAYER_STOPPED);
	}

	//This method needs to go, eventually; NavigationPanelScripter is the way [Davis]
	/**
	 * This method is called by External Authoring Interface (EAI) whenever the
	 * viewpoint is changed by the user.	It records the viewpoint changes
	 * automatically when a script is being recorded.
	 *
	 * @param event the EventOut instance associated with the VRML event fired.
	 * @param timestamp the time stamp that signifies when the event was fired.
	 * @param param a custom parameter associated with the VRML event.
	 */
// 	public void callback(EventOut event, double timestamp, Object param) {
// 		if(!isRecording()) return;			// for efficiency

// 		int mode = ((Integer)param).intValue();

// 		if(mode == VIEW_CHANGED) {
// 			float view[] = ((EventOutMFFloat) event).getValue();
// 			StringBuffer value = new StringBuffer();
// 			for(int i=0; i<6; i++) {
// 				value.append(view[i]);
// 				if(i<5) value.append(' ');
// 			}

// 			recordViewpointChanged(value.toString());
// 		} else if(mode == ACTIVEVIEW_CHANGED) {
// 			int activeView = ((EventOutSFInt32)event).getValue();
// 			recordViewpointSelected(String.valueOf(activeView));
// 		} else if(mode == QUERYVIEW_CHANGED) {
// 			if(moduleNode!=null) {
// 				float view[] = ((EventOutMFFloat) event).getValue();
// 				StringBuffer value = new StringBuffer();
// 				for(int i=0; i<6; i++) {
// 					value.append(view[i]);
// 					if(i<5) value.append(' ');
// 				}

// 				WSLNode viewNode = new WSLNode("view");
// 				viewNode.getAttributes().add(WSLNode.VALUE, value.toString());
// 				moduleNode.addChild(viewNode);
// 			}
// 		}
// 	}


//========================
// Event Posting Routines
//========================

	/**
	 * Adds an event listener. <code>WSLPlayer</code> only fires events to
	 * registered event listener, and the interfaces
	 * (<code>WSLScriptListener</code>, <code>WSLPlayerListener</code>, and/or
	 * <code>WSLProgressListener</code>) implemented by the class determines
	 * which types of events it receives.
	 *
	 * <p>It is worth noting that it is guaranteed that changes to the listeners
	 * list of a <code>WSLPlayer</code> made during the dispatch of a
	 * <code>WSLScriptEvent</code> will not affect the distribution of that
	 * event.	 This is to allow the creation or destruction of objects (which
	 * may be listening to the playback) during the handling of such an event.
	 */
	public void addListener(WSLPlayerListener listener) {
		if(listener!=null && !listeners.contains(listener))
			listeners.addElement(listener);
	}

	/**
	 * Removes an event listener from <code>WSLPlayer</code>'s listener list.
	 */
	public void removeListener(Object listener) {
		listeners.removeElement(listener);
	}

	private void postProgress() {
		WSLProgressEvent event=new WSLProgressEvent(this,getCurrentScriptTime(),
																								getTotalScriptTime());

		for(int i=0; i<listeners.size(); i++)
			if(listeners.elementAt(i) instanceof WSLProgressListener)
				try {
					((WSLProgressListener)listeners.elementAt(i)).progressChanged(event);
				} catch(RuntimeException e) {
					System.err.println(EXCEPTION_MSG);
					e.printStackTrace();
				}
	}

	private void postScriptAction(WSLScriptEvent event) {
		//It is a reasonable expectation that new listeners may be added during
		//playback (as Java objects are created for module objects represented
		//in the script tags).	Thus, we work with a copy to avoid
		//hard-to-determine behavior with new listeners.
		Vector listeners=(Vector)this.listeners.clone();	//shadows class variable
		for(int i=0; i<listeners.size(); i++)
			if(listeners.elementAt(i) instanceof WSLScriptListener)
				try {
					((WSLScriptListener)listeners.elementAt(i)).scriptActionFired(event);
				} catch(RuntimeException e) {
					System.err.println(EXCEPTION_MSG);
					e.printStackTrace();
				}
	}

	private void postInitialize() {
// 		final WSLNode viewNode=moduleNode.getNode("view");
// 		if(viewNode!=null)
// 			setView(viewNode.getAttributes().getValue(WSLNode.VALUE));

		final WSLScriptEvent event=new WSLScriptEvent(this,script.getModuleNode(),module.getWSLModuleName());
		//It is a reasonable expectation that new listeners may be added during
		//initialization (as Java objects are created for module objects stored
		//in the inititialization tag).	 Thus, we work with a copy to avoid
		//hard-to-determine behavior with new listeners.
		Vector listeners=(Vector)this.listeners.clone();	//shadows class variable
		for(int i=0; i<listeners.size(); i++)
			if(listeners.elementAt(i) instanceof WSLScriptListener)
				try {
					((WSLScriptListener)listeners.elementAt(i)).initialize(event);
				} catch(RuntimeException e) {
					System.err.println(EXCEPTION_MSG);
					e.printStackTrace();
				}
	}

	private void postPlayerStateChanged(int id) {
		WSLPlayerEvent event = new WSLPlayerEvent(this, id);
		for(int i=0; i<listeners.size(); i++)
			if(listeners.elementAt(i) instanceof WSLPlayerListener)
				try {
					((WSLPlayerListener)listeners.elementAt(i)).playerStateChanged(event);
				} catch(RuntimeException e) {
					System.err.println(EXCEPTION_MSG);
					e.printStackTrace();
				}
	}

//====================
// Recording routines
//====================

	/**
	 * Records that the user has added a new object into the WebTOP module (for
	 * example, a new polarizer in the Polarization module).
	 *
	 * @param	 obj	the new object added represented as a <code>WSLNode</code>.
	 */
	public void recordObjectAdded(final WSLNode obj) {
		final WSLNode node=new WSLNode(OBJECT_ADDED);
		node.addChild(obj);
		record(node);
	}

	/**
	 * Records that the user has removed an object from the WebTOP module, for
	 * example, a polarizer in the Polarization module.
	 *
	 * @param	 id	 identifier of the object to be removed.
	 */
	public void recordObjectRemoved(String id) {
		recordAction(OBJECT_REMOVED,id,null,null);
	}

	/**
	 * Records that the user has performed an action with the Java applet.	This
	 * version of <code>recordActionPerformed()</code> only records actions that
	 * affects the module as a whole, rather than just affecting a particular
	 * object, or parameter.	For example, when the user clicks on the Reset
	 * button to reset the WebTOP module to its default state.
	 *
	 * @param	 action	 name of the action performed.
	 */
	public void recordActionPerformed(String action) {
		recordAction(ACTION_PERFORMED, null, action, null);
	}

	/**
	 * Records that the user has entered a value for a particular parameter in
	 * the Java applet.
	 *
	 * @param	 param	the parameter affected.
	 * @param	 value	the new value of the parameter.
	 */
	public void recordActionPerformed(String param, String value) {
		recordAction(ACTION_PERFORMED, null, param, value);
	}

	/**
	 * Records that the user has entered a value for a particular parameter of an
	 * object in the Java applet.	 This might be the value of the angle parameter
	 * of a polarizer in the Polarization module.
	 *
	 * @param	 id			identifier of the object affected.
	 * @param	 param	the parameter affected.
	 * @param	 value	the new value of the parameter.
	 */
	public void recordActionPerformed(String id, String param, String value) {
		recordAction(ACTION_PERFORMED, id, param, value);
	}

	/**
	 * Records that the user has moved the mouse pointer into a VRML widget.
	 *
	 * @param	 id	 identifier of the object the mouse pointer moves into.
	 */
	public void recordMouseEntered(String id) {
		recordAction(MOUSE_IN,id,null,null);
	}

	/**
	 * Records that the user has moved the mouse pointer out of a VRML widget.
	 *
	 * @param	 id	 identifier of the object the mouse pointer moves out of.
	 */
	public void recordMouseExited(String id) {
		recordAction(MOUSE_OUT,id,null,null);
	}

	/**
	 * Records that the user has clicked the mouse button on a VRML widget.
	 *
	 * @param	 param	the parameter that the VRML widget associated with.
	 */
	public void recordMousePressed(String param) {
		recordAction(MOUSE_DOWN, null, param, null);
	}

	/**
	 * Records that the user has clicked the mouse button on a VRML widget
	 * associated with a particular object.
	 *
	 * @param	 id			the identifier of the object.
	 * @param	 param	the parameter associated with the VRML widget.
	 */
	public void recordMousePressed(String id, String param) {
		recordAction(MOUSE_DOWN, id, param, null);
	}

	/**
	 * Records that the user has released the mouse button from a VRML widget.
	 *
	 * @param	 param	the parameter that the VRML widget associated with.
	 */
	public void recordMouseReleased(String param) {
		recordAction(MOUSE_UP, null, param, null);
	}

	/**
	 * Records that the user has released the mouse button from a VRML widget
	 * associated with a certain object.
	 *
	 * @param	 id			the identifier of the object.
	 * @param	 param	the parameter associated with the VRML widget.
	 */
	public void recordMouseReleased(String id, String param) {
		recordAction(MOUSE_UP, id, param, null);
	}

	/**
	 * Records that the user has dragged a VRML widget.
	 *
	 * @param	 param	the parameter associated with the the VRML widget.
	 * @param	 value	the new value of the parameter.
	 */
	public void recordMouseDragged(String param, String value) {
		recordAction(MOUSE_DRAG, null, param, value);
	}

	/**
	 * Records that the user has dragged a VRML widget associated with a
	 * particular object.
	 *
	 * @param	 id			identifier of the object affected.
	 * @param	 param	the parameter associated with the VRML widget.
	 * @param	 value	the new value of the parameter.
	 */
	public void recordMouseDragged(String id, String param, String value) {
		recordAction(MOUSE_DRAG, id, param, value);
	}

	/**
	 * Records that the user has changed the viewpoint for the module.	This
	 * method is typically only called by <code>WSLPlayer</code> since it records
	 * all viewpoint events automatically.
	 *
	 * @param value the new viewpoint specified as a six-value String.	The
	 *							first three floating-point values specify the rotation in
	 *							each of X, Y, and Z-axis, and the last three floating-point
	 *							values specify pan in the X, Y, and Z directions.
	 */
	public void recordViewpointChanged(String value) {
		recordAction(VIEWPOINT_CHANGED, null, null, value);
	}

	/**
	 * Records that the user has selected a preset viewpoint.	 This method is
	 * typically only called by <code>WSLPlayer</code> since it records all
	 * viewpoint events automatically.
	 *
	 * @param	 value	the number of the preset viewpoint selected, starting at 0.
	 */
	public void recordViewpointSelected(String value) {
		recordAction(VIEWPOINT_SELECTED, null, null, value);
	}

	/**
	 * The most general form of recording method.	 All user interactions are
	 * abstracted as action tags with three attributes: <code>id</code>,
	 * <code>param</code>, and <code>value</code>.	The name of the tag
	 * signifies the type of the user interaction, while its attributes specify
	 * the object affected (<code>id</code>), the parameter affected
	 * (<code>param</code>), and the new value of the parameter
	 * (<code>value</code>).	Attributes can be excluded where inapplicable.
	 * Generally, this method is rarely used externally.	Instead, the different
	 * versions of recording methods specific to different user interactions are
	 * used.
	 *
	 * @param	 action	 name of script tag to write.
	 * @param	 id			 target object's identifying string.
	 * @param	 param	 name of changed value.
	 * @param	 value	 new parameter value.
	 */
	//Should this be public?	It allows writing of non-existent action tags... [Davis]
	public void recordAction(String action, String id, String param, String value) {
		WSLAttributeList atts = new WSLAttributeList();
		if(!WTString.isNull(id)) atts.add(WSLNode.TARGET, id);
		if(!WTString.isNull(param)) atts.add(WSLNode.PARAMETER, param);
		if(!WTString.isNull(value)) atts.add(WSLNode.VALUE, value);

		recordAction(action, atts);
	}

	/**
	 * Internal generic recording function.	 Allows the specification of any
	 * attributes.
	 *
	 * @param	 action	 name of script tag to write.
	 * @param	 atts		 attributes for the tag.
	 */
	private void recordAction(String action, WSLAttributeList atts) {
		record(new WSLNode(action,atts));
	}

	/**
	 * Internal generic recording function.  Allows the specification of any node
	 * whatsoever.
	 *
	 * @param node the node to record.
	 */
	//This function is ultimately responsible for the silent returns
	private void record(WSLNode node) {
		if(isRecording()) {
			final long time=System.currentTimeMillis();
			node.setTimeStamp(time-prevRecordTime);
			scriptNode.addChild(node);
			prevRecordTime=time;
		}
	}
}
