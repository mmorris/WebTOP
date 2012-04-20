/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//LinearBarrier.java
//Defines the class LinearBarrier, for (literal) thread synchronization.
//Davis Herring
//Created April 6 2004
//Updated April 6 2004
//Version 0.0

//Presumably this will eventually become Barrier.java, probably with nested
//classes for the two principal implementations.

package org.sdl.util;

public class LinearBarrier extends ThreadLock
{
	/**
	 * Object used for internal synchronization.
	 */
	private final Object _lock=new Object();

	private int count,total;
	private boolean go;						// barrier is being passed if true

	public LinearBarrier() {}
	public LinearBarrier(String name) {super(name);}

	public void lock() throws InterruptedException {
		synchronized(_lock) {
			while(go && count>0) _lock.wait();
			go=false;
			try {
				if(++count>=total) pass();
				else while(!go) _lock.wait();
			} finally {--count;}
			_lock.notifyAll();
		}
	}

	public boolean tryLock() {synchronized(_lock) {return tryLock0();}}

	public void unlock() {}

	public boolean isLocked() {synchronized(_lock) {return count>0;}}
	public boolean isOwnable() {return false;}
	public Thread getOwner() {return null;}
	public boolean couldLock() {synchronized(_lock) {return count>=total-1;}}

	public void add() {synchronized(_lock) {++total;}}
	public void remove() {
		synchronized(_lock) {
			if(total==0) throw new IllegalStateException("no threads to remove");
			--total;
			maybePass();
		}
	}

	public void setThreadCount(int t) {
		if(t<0) throw new IllegalArgumentException("negative thread count");
		total=t;
	}

	//These two methods require synchronization.
	private void pass() {go=true; _lock.notifyAll();}
	private void maybePass() {if(count>=total) pass();}

	protected String toString0() {
		final String s=count+"/"+total;;
		return name==null?s:name+" ("+s+')';
	}

	public static void main(String[] args) {
		class BTest extends Thread {
			private final char c1,c2;
			private final ThreadLock l;
			public BTest(char o,char t,ThreadLock lock) {c1=o; c2=t; l=lock;}
			//Note the completely generic use of the lock; spiffy.  However, this
			//precludes using add()/remove().  So it's not always the answer.
			public void run() {
				while(Math.random()>0.05) System.out.print(c1);
				System.out.println("\n"+c1+": done");
				while(true) try {l.lock(); break;} catch(InterruptedException e) {}
				while(Math.random()>0.1) System.out.print(c2);
				l.unlock();
				System.out.println("\n"+c2+": done");
			}
		}

		final LinearBarrier lb=new LinearBarrier("foo");
		for(int i=0;i<26;++i) {
			lb.add();
			new BTest((char)('A'+i),(char)('a'+i),lb).start();
		}
	}
}
