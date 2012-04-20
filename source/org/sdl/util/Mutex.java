/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Mutex.java
//Defines the class Mutex, for flexible thread control.
//Davis Herring
//Created October 9 2002
//Updated March 25 2004
//Version 1.1

package org.sdl.util;

/*/
import java.awt.*;
import java.awt.event.*;
//*/

/**
 * Provides long-term object locking akin to the behavior of 'synchronized'.
 */
public class Mutex extends ThreadLock
{
	/**
	 * Object used for internal synchronization.
	 */
	private final Object _lock=new Object();

	/**
	 * A reference to the Thread object for the thread that owns this Mutex,
	 * or null if none does.
	 */
	private Thread owner;

	/**
	 * A flag indicating that this Mutex has been made private and will not
	 * reveal references to its owner.
	 */
	private boolean priv;

	/**
	 * Any AbandonedException waiting to be thrown in threads waiting on this
	 * Mutex.
	 */
	private AbandonedException ae;

	/**
	 * Constructs a Mutex.
	 */
	public Mutex() {}

	/**
	 * Constructs a named Mutex.
	 */
	public Mutex(String name) {super(name);}

	/**
	 * Locks this Mutex, blocking until the lock is acquired.  Does nothing and
	 * returns silently if this thread has already locked this object.
	 *
	 * @exception InterruptedException the current thread was interrupted while
	 *                                 waiting to acquire the ThreadLock.
	 */
	public void lock() throws InterruptedException {
		synchronized(_lock) {
			if(mine()) return;
			while(isLocked()) _lock.wait();
			owner=Thread.currentThread();
			if(ae!=null) throw ae;
		}
	}

	/**
	 * Attempts to lock this Mutex.  If this Mutex is already locked by another
	 * thread, returns false immediately.
	 *
	 * @return true if the lock was acquired; false if it was already locked
	 */
	public boolean tryLock() {synchronized(_lock) {return tryLock0();}}

	/**
	 * Unlocks this Mutex, which must have been most recently locked by this
	 * thread.  However, if the mutex is unlocked, does nothing and returns
	 * silently.
	 *
	 * @exception IllegalStateException if this Mutex is locked but not by the
	 *                                  current thread.
	 */
	public void unlock() {
		synchronized(_lock) {
			if(!isLocked()) return;
			if(!mine())
				throw new IllegalStateException("This thread doesn't own this Mutex.");
			ae=null;		//This thread didn't abandon it
			unlock0();
		}
	}

	/**
	 * Removes the lock, waking up any waiting threads.
	 */
	private void unlock0() {
		synchronized(_lock) {
			owner=null;
			_lock.notify();
		}
	}

	/**
	 * Checks whether this Mutex is locked.
	 *
	 * @return true if this Mutex has been locked by a thread and not yet
	 *         unlocked; false otherwise.
	 */
	public boolean isLocked() {
		synchronized(_lock) {
			if(owner==null) return false;
			if(owner.isAlive()) return true;
			unlock0();
			//Oops, we've been abandoned.  Make exception to be thrown (somewhere).
			//This can need to know the return value of this very function, so we
			//make this after unlocking (recursive calls will just return false).
			//There is no race condition because we've held the _lock since before
			//the notify(), and don't release it until ae has been assigned.
			ae=new AbandonedException(this,priv?null:owner);
			return false;
		}
	}

	/**
	 * Checks whether this Mutex is ownable.  <code>Mutex</code>es are ownable.
	 *
	 * @return true
	 */
	public boolean isOwnable() {return true;}

	/**
	 * Returns the thread owning this Mutex.
	 *
	 * @see #makePrivate
	 * @return a reference to the Thread that owns this Mutex, or null if it is
	 *         unlocked.  Also null if makePrivate() has been called.
	 */
	public Thread getOwner() {
		synchronized(_lock) {
			if(!isLocked()) return null;
			return priv?null:owner;
		}
	}

	/**
	 * Checks whether the current thread owns this Mutex.
	 *
	 * @return true if the current thread has locked this Mutex most recently;
	 *         false otherwise.
	 */
	public boolean mine() {return owner==Thread.currentThread();}

	/**
	 * Causes this Mutex to be private, preventing it from revealing its owner.
	 * This operation cannot be reversed.
	 *
	 * @see #isPrivate
	 */
	public void makePrivate() {priv=true;}

	/**
	 * Checks whether this Mutex is private.
	 *
	 * @see #makePrivate
	 * @return true if this Mutex will not reveal owners; false otherwise.
	 */
	public boolean isPrivate() {return priv;}

	/*/
	public static void main(String[] args) {
		Label n=new Label("0");
		final Frame f=new Frame("MutexTest");
		f.setBounds(100,100,400,400);
		f.add(n);
		f.show();
		class PokeableThread extends Thread implements MouseListener {
			private final Mutex mutex;
			private final Label label;
			private final int delta;
			private volatile boolean seppuku,letgo;
			public PokeableThread(Mutex m,Label l,int dx) {mutex=m;label=l;delta=dx;}
			public void mouseClicked(MouseEvent e) {
				letgo=true;
				interrupt();
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void run() {
				while(!seppuku) {
					try {
						System.out.println(this+": locking...");
						mutex.lock();
						// It's possible to miss an interrupt() here; a thread that is
						// both notify()ed and interrupt()ed will (at least sometimes)
						// wake up in the manner of the notify, and lose the interrupted
						// status which would cause the Thread.sleep() call below to
						// throw.  This is probably just an indication that calls to
						// Mutex::lock() should, like those to Object.wait(), be put, with
						// their catch(InterruptedException e) blocks, in
						// condition-checking while loops.  But this is fine for here.
						if(seppuku) break;
						System.out.println(this+": locked!  Printing/sleeping...");
						label.setText(""+(Integer.parseInt(label.getText())+delta));
						Thread.sleep(2500);
						System.out.println(this+": done sleeping");
					} catch(InterruptedException e) {
						System.out.println(this+": <interrupted...>");
						//This effectively randomizes the order of interruption
						try {
							Thread.sleep((long)(Math.random()*50));
						} catch(InterruptedException ie) {}	// again?!
						System.out.println(this+": <interrupted>");
						if(letgo) {
							System.out.println(this+": letting go");
							letgo=false;
							if(mutex.mine()) mutex.unlock();
							Thread.yield();
						}
					}
				}
				if(mutex.mine()) mutex.unlock();	//don't be a deadbeat
				System.out.println(this+": done.");
			}
			public void kill() {
				System.out.print(this+"::kill()... ");
				seppuku=true;
				interrupt();
				System.out.println("interrupt sent (to "+this+')');
			}
		}
		final Mutex mtx=new Mutex();
		final PokeableThread t1=new PokeableThread(mtx,n,1),
												 t2=new PokeableThread(mtx,n,-1);
		t1.start();
		t2.start();
		n.addMouseListener(t1);
		n.addMouseListener(t2);
		f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					t1.kill();
					t2.kill();
					f.setVisible(false);
					while(true)
						try {
							System.out.println("Joins: 0");
							t1.join();
							System.out.println("Joins: 1");
							t2.join();
							System.out.println("Joins: 2");
							break;
						} catch(InterruptedException ie) {
							System.out.println("Joins: <interrupted>");
						}
					System.exit(0);
				}
			});
		System.out.println("main(): done");
	}//*/
}
