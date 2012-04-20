/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//DebugPrinter.java
//Davis Herring
//Provides a simple global printing service for debugging.
//Updated January 30 2004
//Version 2.02

package org.webtop.util;

import java.io.PrintStream;
import java.util.Vector;

//As of version 2.0, DebugPrinter can optionally not block while printing.
//This can be helpful when large amounts of text are generated, and can
//prevent deadlock in some obscure cases perhaps specific to MSIE.

//A further elaboration: maybe have 'priorities' of messages, and instead of
//off/on have a minimum priority to actually print?	 Would need a real stack.

public final class DebugPrinter {
	public static volatile boolean debug=false;
	public static volatile PrintStream output=System.err;

	private interface Printer {public void enqueue(Object o);}

	private static final Printer bPrinter=new Printer() {
			public void enqueue(Object o) {print0(o);}
		};
	private static class NonBlockingPrinter extends Thread implements Printer {
		public NonBlockingPrinter() {super("DebugPrinter");}
		private final Vector<Object> strs=new Vector<Object>();
		public void run() {
			while(true) {
				Object o;
				synchronized(strs) {
					while(strs.isEmpty()) try {strs.wait();} catch(InterruptedException e) {}
					o=strs.firstElement();
					strs.removeElementAt(0);
				}
				print0(o);
			}
		}
		public void enqueue(Object o) {
			synchronized(strs) {
				try {
					strs.addElement(o);
				} catch(OutOfMemoryError e) {
					strs.removeAllElements();
					strs.addElement("NonBlockingPrinter: out of memory!  Output flushed...");
					strs.addElement(o);
					strs.trimToSize();
				}
				strs.notify();
			}
		}
	}
	private static final NonBlockingPrinter nbPrinter=new NonBlockingPrinter();
	private static volatile Printer printer=bPrinter;		//points to current printer

	private static volatile boolean once=false,stack,blocking=true;
	private static boolean nbstarted;

	//actually does the printing; used by Printers.
	private static void print0(Object o) {
		if(o instanceof Throwable) ((Throwable)o).printStackTrace(output);
		else output.print(o);
	}

	private static boolean active() {return debug||once&&!(once=false);}

	public static void println(String s) {if(active()) printer.enqueue(s+'\n');}
	public static void print(String s) {if(active()) printer.enqueue(s);}
	public static void stackTrace() {if(active()) printer.enqueue(new Exception("tracer"));}
	public static void on() {debug=true;}
	public static void off() {debug=false;}
	public static void once() {once=true;}												//Enables just next printing
	public static void push(boolean nu) {stack=debug; debug=nu;}	//Changes state temporarily
	public static void pop() {debug=stack;}												//Restores state
	//Chooses which Printer to use
	public static synchronized void setBlocking(boolean block) {
		if(blocking && !block && !nbstarted) {
			nbstarted=true;
			nbPrinter.start();
		}
		blocking=block;
		printer=blocking?bPrinter:nbPrinter;
	}

	private DebugPrinter() {}
}
