/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//RecursiveListener.java
//Davis Herring (based on MessageCracker by Cay S. Horstmann and Gary Cornell, Core Java)
//Defines an class for establishing component-hierarchy-wide listeners.
//Created August 23 2003 from version 1.0 of HotKey.java
//Updated May 25 2004
//Version 2.0.2

package org.webtop.component;

import java.awt.*;
import java.awt.event.*;

//The old GUIListener was a superclass to the 'real' listener -- the Visitor
//pattern (for establishment/teardown) makes much more sense.  Then you can
//have the same class attached to single components, or to entire hierarchies.

public class RecursiveListener
	implements ContainerListener,org.webtop.util.Helper {

	public final Component target;
	public final GUIListener listener;
	private boolean setup;

	public RecursiveListener(Component c,GUIListener l) {
		if(c==null) throw new NullPointerException("Nothing to which to listen");
		target=c;
		listener=l;
		//Don't call setup() here -- although the listener is set up, and
		//container-listening would make it okay to setup before the component
		//tree is ready, the utility of forceRelease() is not calling setup().
	}

	private void add(Component c) {
		listener.register(c);
		if(c instanceof Container) {
			Container cc=(Container)c;
			cc.addContainerListener(this);
			Component[] a=cc.getComponents();
			for(int i=0;i<a.length;++i) add(a[i]);
		}
	}
	private void remove(Component c) {
		listener.deregister(c);
		if(c instanceof Container) {
			Container cc=(Container)c;
			cc.removeContainerListener(this);
			Component[] a=cc.getComponents();
			for(int i=0;i<a.length;++i) remove(a[i]);
		}
	}

	//Just for the heck of it, we'll make it possible to release()/setup() arbitrarily.
	public void setup() {
		if(setup) throw new IllegalStateException("Already setup");
		else {
			add(target);
			setup=true;
		}
	}
	//This must be idempotent as a Helper method -- besides, it can't hurt anyway
	public void release() {if(setup) forceRelease();}
	public void forceRelease() {remove(target); setup=false;}

	public boolean isSetup() {return setup;}

	public void componentAdded(ContainerEvent e) {add(e.getChild());}
	public void componentRemoved(ContainerEvent e) {remove(e.getChild());}

	public String toString() {return getClass().getName()+'['+target+']';}
}
