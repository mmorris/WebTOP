/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//ThreadWatch.java
//Defines a class to maintain and report on a list of threads.
//Davis Herring
//Created May 14 2004
//Updated May 14 2004
//Version 2.1

package org.webtop.util;

import java.util.*;

public class ThreadWatch extends Thread {
	private static class FlaggedThread {
		public Thread t;
		public boolean l;
		public FlaggedThread(Thread T) {t=T;}
	}

	private static ThreadWatch me;
	private static final Object lock=new Object();

	private ThreadWatch() {start();}

	private static final Vector threads=new Vector(),hasLived=new Vector();

	public static void add(Thread t) {
		synchronized(lock) {
			if(me==null) me=new ThreadWatch();
			threads.addElement(new FlaggedThread(t));
			report(false);										// for convenience
		}
	}

	public static void report(boolean clean) {
		synchronized(lock) {
			System.out.println("ThreadWatch #"+ThreadWatch.class.hashCode());
			for(int i=0;i<threads.size();++i) {
				final FlaggedThread cur=(FlaggedThread)threads.elementAt(i);
				System.out.print(cur.t);
				if(cur.t.isAlive()) {
					cur.l=true;
					System.out.println("\tALIVE");
				} else if(!cur.l) {
					System.out.println("\tPENDING");
				} else {
					System.out.println("\tDEAD");
					if(clean) threads.removeElementAt(i--);
				}
			}
		}
	}

	public void run() {
		boolean seppuku=false;
		try {
			System.out.println("Thread for ThreadWatch #"+ThreadWatch.class.hashCode()+": started.");
			int i=0;
			while(!seppuku) {
				synchronized(lock) {
					if(threads.size()==0) {seppuku=true; break;}
					i=(i+1)%threads.size();
					final FlaggedThread cur=(FlaggedThread)threads.elementAt(i);
					try {
						Thread.sleep(100);
						cur.t.join(10);
						if(cur.t.isAlive()) cur.l=true;
						else if(cur.l) {
							System.out.println("ThreadWatch #"+ThreadWatch.class.hashCode()+": "+cur.t+"\tDIED");
							threads.removeElementAt(i--);
						}
					} catch(InterruptedException e) {}
				}
			}
			System.out.println("Thread for ThreadWatch #"+ThreadWatch.class.hashCode()+": exiting...");
		} finally {
			System.out.println("Thread for ThreadWatch #"+ThreadWatch.class.hashCode()+": finished.");
			me=null;
		}
	}
}
