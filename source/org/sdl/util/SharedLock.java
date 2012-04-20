/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//SharedLock.java
//Defines a ThreadLock for dual-mode exclusive/non-exclusive access control.
//Davis Herring
//Created March 31 2004
//Updated April 6 2004
//Version 0.1a (cosmetically different from v0.1)

package org.sdl.util;

import java.util.Vector;				// for readOwners; may be temporary
import java.util.Random;				// for testing; is temporary

//AbandonedExceptions not yet used here; abandoning threads will cause
//deadlock.

public class SharedLock extends ThreadLock {
	private class ExclusiveLock extends ThreadLock {
		//An ExclusiveLock shares the _lock of its SharedLock.

		//Note that ExclusiveLock never invokes tryLock() on impl.
		private final ThreadLock impl;
		private int pending;				// number of threads waiting on exclusive lock

		public ExclusiveLock(ThreadLock l) {
			super(l.name==null?null:"ExclusiveLock["+l.name+']');
			if(l==null) throw new NullPointerException("no ThreadLock given");
			//Should we enforce that l be ownable()?
			impl=l;
		}

		//To act on this safely, you'll need synchronization, but synchronization
		//inside it wouldn't help.
		public int pending() {return pending;}

		public void lock() throws InterruptedException {lock0(false);}

		//Note that only SharedLock can call this method, except via reflection,
		//as only it can have references of type ExclusiveLock.  There may,
		//however, be a better way to do this involving ExclusiveLock asking
		//SharedLock about it, rather than SharedLock simply telling (in the form
		//of lock0(true)).
		public void lock0(boolean internal) throws InterruptedException {
			try {
				synchronized(_lock) {
					++pending;
					if(mine()) return;
					if(!internal && isReadOwner(Thread.currentThread()))
						throw new IllegalStateException("threads holding a share-lock cannot call lock() to obtain an exclusive lock; use SharedLock.tryExclusive()");
					while(readOwners()>(internal?1:0)) _lock.wait();
				}
				//This may not be entirely safe; I think that the fact that pending is
				//definitively non-0 at this point guarantees that share-lock-seekers
				//will have to wait, but I'm not sure.  The alternative -- having
				//impl.lock() occur while synchronized on _lock -- would be much
				//worse, as it would block any access to the whole lock (including
				//unlocking it!) until this thread won the exclusive lock.
				impl.lock();
			} finally {
				synchronized(_lock) {--pending;}
			}
		}

		public boolean tryLock() {synchronized(_lock) {return tryLock0();}}

		public void unlock() {
			synchronized(_lock) {
				impl.unlock();
				//If successful, and no other threads waiting for exclusive access,
				//wake threads that may now be able to get shared locks
				if(pending==0) _lock.notifyAll();
			}
		}

		//Should this consider the value of `pending'?
		public boolean isLocked() {return impl.isLocked();}
		public boolean isOwnable() {return impl.isOwnable();}
		public Thread getOwner() {return impl.getOwner();}
		public boolean mine() {return impl.mine();}
		public boolean couldLock() {return impl.couldLock();}
		public boolean couldUnlock() {return impl.couldUnlock();}

		protected String toString0() {return impl+",pending="+pending;}
	}

	//Functions to defer defining a final datatype for the rOwners set.  None of
	//these are synchronized or anything.
	private final Vector readOwners=new Vector();
	private void addReadOwner(Thread t) {readOwners.addElement(t);}
	private void removeReadOwner(Thread t) {readOwners.removeElement(t);}
	private boolean isReadOwner(Thread t) {return readOwners.indexOf(t)!=-1;}
	private int readOwners() {return readOwners.size();}

	private final Object _lock=new Object();
	private final ExclusiveLock eLock;

	public SharedLock(ThreadLock l) {
		eLock=new ExclusiveLock(l);
	}

	public SharedLock(String name,ThreadLock l) {
		super(name);
		eLock=new ExclusiveLock(l);
	}

	//Should eLock just be public (it's final)?
	public ThreadLock getExclusiveLock() {return eLock;}

	public boolean tryExclusive() throws InterruptedException {
		synchronized(_lock) {
			checkMine();
			//The whole point of this method: deadlock would be guaranteed if you
			//had two readers wait to upgrade... not quite so sure about the one
			//reader, one non-reader both trying to write-lock, but for now it's
			//treated the same way.  Doing otherwise would probably involve adding a
			//readerPending variable or so to ExclusiveLock.
			if(eLock.pending()>0) return false;
			eLock.lock0(true);
			return true;
		}
	}

	public void lock() throws InterruptedException {
		synchronized(_lock) {
			if(mine()) return;
			while(!couldLock()) _lock.wait();
			addReadOwner(Thread.currentThread());
		}
	}

	public boolean tryLock() {synchronized(_lock) {return tryLock0();}}

	public void unlock() {
		synchronized(_lock) {
			if(!isLocked()) return;
			checkMine();
			removeReadOwner(Thread.currentThread());
			//Wake up threads that may be waiting to lock if there are no more
			//readers in the way; 1 is the case of a thread in tryExclusive().
			if(readOwners()<=1) _lock.notifyAll();
		}
	}

	public boolean isLocked() {
		synchronized(_lock) {return readOwners()>0;}
	}

	public boolean isOwnable() {return true;} // after a fashion

	public Thread getOwner() {return null;}	// no specific owner

	public boolean mine() {
		synchronized(_lock) {return isReadOwner(Thread.currentThread());}
	}

	//Note that this is entirely independent of isLocked().
	public boolean couldLock() {
		synchronized(_lock) {return eLock.couldLock() && eLock.pending()==0;}
	}

	//Oddly enough, the standard couldUnlock() works just fine.

	private void checkMine() {
		if(!mine())
			throw new IllegalStateException("This thread hasn't locked this SharedLock.");
	}

	protected String toString0() {
		final String tls=super.toString0();
		return (tls.length()==0?"":tls+',')+"eLock="+eLock;
	}

	// Testing stuff follows.
	public static void main(String[] args) throws java.io.IOException,InterruptedException {
		Mutex mtx=new Mutex("mutex");
		final SharedLock sl=new SharedLock("shared",mtx);
		final ThreadLock el=sl.getExclusiveLock();
		sl.lock();
		try {
			el.lock();								// throws IllegalStateException
		} catch(IllegalStateException e) {
			System.err.print("main: caught ");
			e.printStackTrace();
		}
		if(sl.tryExclusive()) {			// this works
			System.out.println("exclusivity achieved");
			el.unlock();							// we still unlock using this
		} else System.err.println("exclusivity failed!");
		sl.unlock();
		System.out.println("locks released");

		final Random rnd=new Random();
		final byte[] box=new byte[1];
		final boolean[] seppuku=new boolean[1];

		class BitFlipper extends Thread {
			private final byte xor_mask;

			BitFlipper(String name,byte xor) {
				super(name);
				xor_mask=xor;
			}

			public void run() {
				while(!seppuku[0]) {
					try {
						byte b=(byte)rnd.nextInt();
						print("b=="+b+"; locking...");
						sl.lock();
						print("sl locked.");
						doflip:
						if(b==box[0]) {
							print("b==box --> locking to flip...");
							if(!sl.tryExclusive()) {
								print("can't make lock exclusive; unlocking/relocking");
								sl.unlock();
								el.lock();
								if(b!=box[0]) {
									print("b no longer == box after relocking; aborting change");
									el.unlock();
									break doflip;
								}
							}
							print("exclusive lock acquired; applying mask...");
							box[0]^=xor_mask;
							print("box is now "+box[0]+"; unlocking...");
							el.unlock();
							sl.unlock();
							print("locks released.");
						} else {
							sl.unlock();
							print("b!=box; sl unlocked.");
						}
						//Thread.sleep(Math.abs(rnd.nextInt())%1500+50);
					} catch(InterruptedException e) {}
				}
			}

			private void print(String s) {System.out.println(getName()+": "+s);}
		}

		new BitFlipper("LSB",(byte)1).start();
		new BitFlipper("sign",(byte)128).start();
		new BitFlipper("case",(byte)32).start();
		new BitFlipper("Ctrl",(byte)64).start();

		System.out.println("threads spawned...");
		System.in.read();						// pause...
		seppuku[0]=true;						// threads will then die
	}
}
