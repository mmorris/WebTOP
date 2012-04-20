/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Sleep.java
//Defines a class for resumable waits.
//Davis Herring
//Created April 13 2003
//Updated March 12 2004
//Version 0.3

package org.sdl.util;

public class Sleep {
	private long period,slept;
	private final Object lock=new Object();

	public Sleep(long p) {setPeriod(p);}

	public void setPeriod(final long p) {
		//p==0 is more or less a useless case, but it's better to support it than
		//to force conditionals everywhere that an application might want to sleep
		//or not.  p<0, on the other hand...
		if(p<0) throw new IllegalArgumentException("negative period");
		synchronized(lock) {
			period=p;
			reset();
		}
	}

	public void reset() {synchronized(lock) {slept=0;}}

	public void sleep() throws InterruptedException {
		synchronized(lock) {
			if(slept<period) {
				final long pre=System.currentTimeMillis();
				try {
					Thread.sleep(period-slept);
				} catch(InterruptedException e) {
					slept+=System.currentTimeMillis()-pre;
					throw e;
				}
			}
		}
	}

	//For when you just don't care about being interrupted
	public void sleepAll() {
		synchronized(lock) {
			while(true) try{sleep(); return;} catch(InterruptedException e) {}
		}
	}

	public void wait(final Object o) throws InterruptedException {
		synchronized(lock) {
			if(slept<period) {
				final long pre=System.currentTimeMillis();
				try {
					o.wait(period-slept);
				} catch(InterruptedException e) {
					slept+=System.currentTimeMillis()-pre;
					throw e;
				}
			}
		}
	}
}
