/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Scripter.java
//Declares an abstract class for automating WSL scripting.
//Davis Herring
//Created November 9 2002
//Updated March 4 2004
//Version 1.1

package org.webtop.util.script;

import org.webtop.util.*;
import org.webtop.wsl.client.WSLPlayer;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;

public abstract class Scripter
	implements WSLScriptListener,WSLPlayerListener,Helper
{
	public boolean enabled=true,					//enables script recording/playback
								 playbackDisable=true,	//if true, disable peer during playback
								 recordTraversal=false;	//enables recording mouse entry/exit
	public final String identifier,parameter;
	//Subclasses should use the record*() methods where applicable.
	protected final WSLPlayer wslPlayer;

	//Note that a Scripter created during script playback (as for an object
	//created during a script) will be unable to immediately disable its peer,
	//so this must be done elsewhere.
	//It might be doable at the leaves of the scripter tree -- would be
	//duplicated, but it'd work.
	public Scripter(WSLPlayer player,String id,String param) {
		if(param==null) throw new NullPointerException("Can't have null parameter.");
		if(player==null) throw new NullPointerException("Null WSLPlayer reference.");
		wslPlayer=player;
		identifier=id;
		parameter=param;
		wslPlayer.addListener(this);
	}

	public void addTo(WSLAttributeList atts) {atts.add(parameter,getValue());}
	//This has to be supported for NavigationPanelScripter if nothing else.
	public void addTo(WSLNode node) {addTo(node.getAttributes());}

	//=====WSL RECORDING METHODS=====\\
	protected void recordActionPerformed(String value) {
		if(enabled)
			if(identifier==null) wslPlayer.recordActionPerformed(parameter,value);
			else wslPlayer.recordActionPerformed(identifier,parameter,value);
	}
	//The recording of mouse traversal is a bit odd -- instead of an optional id
	//and a parameter, WSL wants an id and no parameter.  This is of course
	//meant to tie into such things as the Polarization Module's polarizers or
	//the Wave Module's sources, but serves to confuse the issue otherwise.  It
	//makes sense to record mouse movement even on non-"affiliated" widgets, and
	//while it's not very useful from a practical standpoint, certain cosmetic
	//things (like the RotationWidget's overGeometry, which X[Y]DragWidget is
	//going to have someday) would benefit from it.  However, for now, we'll
	//just let the Scripter library support what is already done with WSL: such
	//things as the Geometrical Module's lenses should have a SimpleTouch
	//created to serve as the overall "selection sensor" for an element with a
	//Scripter attached with recordTraversal set.
	protected void recordMouseEntered() {
		if(enabled && recordTraversal && identifier!=null)
			wslPlayer.recordMouseEntered(identifier);
	}
	protected void recordMouseExited() {
		if(enabled && recordTraversal && identifier!=null)
			wslPlayer.recordMouseExited(identifier);
	}
	protected void recordMousePressed() {
		if(enabled)
			if(identifier==null) wslPlayer.recordMousePressed(parameter);
			else wslPlayer.recordMousePressed(identifier,parameter);
	}
	protected void recordMouseReleased() {
		if(enabled)
			if(identifier==null) wslPlayer.recordMouseReleased(parameter);
			else wslPlayer.recordMouseReleased(identifier,parameter);
	}
	protected void recordMouseDragged(String value) {
		if(enabled)
			if(identifier==null) wslPlayer.recordMouseDragged(parameter,value);
			else wslPlayer.recordMouseDragged(identifier,parameter,value);
	}
	protected void recordViewpointChanged(String value) {
		if(enabled) wslPlayer.recordViewpointChanged(value);
	}
	protected void recordViewpointSelected(String value) {
		if(enabled) wslPlayer.recordViewpointSelected(value);
	}

	//=====SUBCLASSES' METHODS=====\\
	//Subclasses should override this to update their peer with a value from a
	//script.
	//The webtop.util.WTString.to*() methods are suggested for use here.
	//A value of null indicates that a value was expected but not found.
	protected abstract void setValue(String value);
	//Subclasses should override this to (if applicable) supply their peer's
	//current value as a string.
	protected abstract String getValue();
	//Subclasses should override this to enable/disable their peer
	//Has nothing to do with the field 'enabled' above.
	protected abstract void setEnabled(boolean on);
	//Subclasses should override this to (if applicable) deallocate/shut down
	//their peer -- it is called when an object-removed event is received for
	//this object.  Its default action is to remove this object as a WSL
	//listener.
	protected void destroy() {wslPlayer.removeListener(this);}

	//=====WSL EVENT HANDLING=====\\
	//Note that for really bizarre things (like the Reflection/Refraction
	//Module's <source> node), this method won't succeed; nor does it have any
	//way to usefully get attention.	Just code in the special cases.
	public void initialize(WSLScriptEvent e) {
		//If we have a non-null id, this will not be called for us -- we will be
		//created at initialize time, and won't be in the list of listeners at the
		//time of generation of the event.	So we can take no action if we receive
		//this and have an id -- if the event is even relevant to us, it's
		//probably signaling our replacement!
		if(enabled && identifier==null)
			setValue(e.getNode().getAttributes().getValue(parameter));
	}

	//Subclasses generally will not need to override this method.
	public void scriptActionFired(WSLScriptEvent event) {
		if(enabled && filter(event)) process(event);
	}

	//Subclasses should override this and screen events by appropriateness to
	//their type (and typically return the value of super.filter() if an event
	//seems to be appropriate).
	protected boolean filter(WSLScriptEvent event) {
		switch(event.getID()) {
		case WSLScriptEvent.ACTION_PERFORMED:
		case WSLScriptEvent.MOUSE_DRAGGED:
		case WSLScriptEvent.MOUSE_ENTERED:
		case WSLScriptEvent.MOUSE_EXITED:
		case WSLScriptEvent.MOUSE_PRESSED:
		case WSLScriptEvent.MOUSE_RELEASED:
			if(!WTString.equal(identifier,event.getTarget()) ||
				 !parameter.equals(event.getParameter())) return false;
			break;
		case WSLScriptEvent.OBJECT_REMOVED:
			if(!WTString.equal(identifier,event.getTarget())) return false;
			break;
		case WSLScriptEvent.OBJECT_ADDED:
			//Any OBJECT_ADDED event can't be relevant to those of us who already
			//exist:
			return false;
		case WSLScriptEvent.VIEWPOINT_CHANGED:
		case WSLScriptEvent.VIEWPOINT_SELECTED:
			//We know nothing about viewpoint events, so don't filter them out
			break;
		default:										//INITIALIZE_MODULE, or random garbage
			System.err.println("Scripter: unexpected WSLScriptEvent ID "+
												 event.getID());
			return false;							//Insulate process() from garbage
		}
		//This filter(), being the last in the chain, has to return something;
		//subclasses should generally avoid returning 'true' themselves.  Any
		//class which does provide a filter() that returns 'true' for some events
		//should provide a process() mechanism for those events and should
		//generally not call super.process() on such events unless super.filter()
		//returns 'true'.
		return true;
	}

	//Subclasses should override this to provide more and more specific behavior
	//for events (generally, in addition to calling super.process()).
	protected void process(WSLScriptEvent event) {
		switch(event.getID()) {
		case WSLScriptEvent.ACTION_PERFORMED:
		case WSLScriptEvent.MOUSE_DRAGGED:
			setValue(event.getValue());
			break;
		case WSLScriptEvent.OBJECT_REMOVED:
			destroy();
			break;
		}
	}

	//Subclasses should override this if some special action is needed when the
	//script changes state.  However, they should call
	//super.playerStateChanged() somewhere in the function.
	public void playerStateChanged(WSLPlayerEvent event) {
		if(playbackDisable)
			switch(event.getID()) {
			case WSLPlayerEvent.PLAYER_STARTED: setEnabled(false); break;
			case WSLPlayerEvent.PLAYER_STOPPED: setEnabled(true); break;
			}
	}

	//When we are released as a helper:
	public void release() {destroy();}
}
