/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//ThreadLock.java
//Defines ThreadLock, a superclass for high-level thread control objects.
//Davis Herring
//Created October 9 2002
//Updated April 6 2004
//Version 1.1a (cosmetically different from v1.1)

package org.sdl.util;

/**
 * Superclass for objects that control the execution of threads.  These
 * objects may arbitrate between threads competing for execution time, or they
 * may indicate the availability of and/or restrict (for those clients that
 * honor it) access to other shared resources.
 *
 * <code>ThreadLock</code>s are at least semi-recursive; a thread may lock a
 * <code>ThreadLock</code> multiple times, but none except the first are
 * guaranteed to have an effect.  If they do not, the thread need only unlock
 * it once regardless of the number of locks performed.  Extra unlocks are
 * ignored, but may throw exceptions if another thread has locked the
 * <code>ThreadLock</code> since it became unlocked.
 *
 * <p>The exact meaning of 'lock', 'unlock', 'locked', etc., may vary between
 * subclasses of ThreadLock.  However, their general contract (of controlling
 * multithreaded access to something) is sufficient for most applications.
 */
public abstract class ThreadLock
{
	/**
	 * Thrown into waiting threads to indicate that a lock was abandoned by its
	 * owner.  A lock is abandoned when its owning thread dies without unlocking
	 * it.
	 *
	 * <p>Objects of this class will only be thrown by subclasses of ThreadLock
	 * that can be owned.  When an <code>AbandonedException</code> is thrown,
	 * the lock attempt that threw it has succeeded.
	 *
	 * The rationale for <code>AbandonedException</code> being thrown at all
	 * (and, necessarily, into threads other than that which abandoned the lock)
	 * is that when a <code>ThreadLock</code> has been abandoned it is unwise to
	 * continue with excecution unless the condition is known and handled.
	 * <code>AbandonedException</code>s that kill their target thread will be
	 * rethrown in the next thread receiving the lock (because the killed thread
	 * abandoned the lock as well).  <code>AbandonedException</code> is
	 * unchecked, so only that code which can handle it is likely to do so.
	 */
	public static class AbandonedException extends RuntimeException {
		/**
		 * The abandoned ThreadLock.
		 */
		private final ThreadLock victim;
		/**
		 * The abandoning thread, or null if it is not to be identified.
		 */
		private final Thread deadbeat;

		/**
		 * Creates an AbandonedException for the given abandoning thread and
		 * abandonee ThreadLock.
		 *
		 * @param tl the abandoned ThreadLock.
		 * @param db the abandoning Thread; may be null if thread is not to be
		 *           identified.
		 */
		public AbandonedException(ThreadLock tl,Thread db) {
			super(tl+" abandoned"+(db==null?"":" by "+db)+".");
			if(tl==null) throw new NullPointerException("No ThreadLock specified");
			victim=tl;
			deadbeat=db;
		}

		/**
		 * Returns the abandoned ThreadLock.
		 *
		 * @returns a reference to the ThreadLock raising the exception.
		 */
		public ThreadLock getVictim() {return victim;}
		/**
		 * Returns the abandoning thread.
		 *
		 * @return a reference to the Thread which abandoned the ThreadLock, or
		 *         null if the thread was not identified.
		 */
		public Thread getDeadbeat() {return deadbeat;}
	}

	/**
	 * A descriptive name for this lock.
	 */
	public final String name;

	/**
	 * Constructs a ThreadLock object.
	 */
	public ThreadLock() {this(null);}
	/**
	 * Constructs a named ThreadLock.
	 */
	public ThreadLock(String n) {name=n;}

	/**
	 * Acquires this ThreadLock for the current thread.  This call may block if
	 * this object is currently locked; if it returns normally, the lock has
	 * been acquired.  If the current thread already controls this ThreadLock,
	 * this call will return normally, although it may not have had any effect.
	 *
	 * @see #tryLock
	 * @see #couldLock
	 * @exception InterruptedException the current thread was interrupted while
	 *                                 waiting to acquire the ThreadLock.
	 * @exception AbandonedException this lock (which can be owned) was
	 *                               abandoned by its previous owner.  This
	 *                               thread now owns the lock.
	 */
	public abstract void lock() throws InterruptedException;

	/**
	 * Attempts to acquire this ThreadLock for the current thread.  If this
	 * object is already locked, this call will not block (as would
	 * <code>lock()</code>), but simply return false.
	 *
	 * @see #lock
	 * @see #couldLock
	 * @return true if the lock was acquired; false if it was already locked.
	 * @exception AbandonedException this lock (which can be owned) was
	 *                               abandoned by its previous owner.  This
	 *                               thread now owns the lock.
	 */
	public abstract boolean tryLock();

	/**
	 * Implementation of <code>tryLock()</code>.  Subclasses can call this from
	 * their <code>tryLock()</code> implementation, but should only do so while
	 * properly internally synchronized.
	 *
	 * @see #tryLock
	 */
	protected boolean tryLock0() {
		if(couldLock())
			try {
				lock();
				return true;
			} catch(InterruptedException e) {} // this shouldn't happen; won't wait
		return false;
	}

	/**
	 * Releases this ThreadLock.  Often only the thread which invoked lock()
	 * most recently on this object may invoke unlock(); an
	 * IllegalStateException will be thrown if an unlock() is performed by a
	 * thread which does not have permission to do so.
	 *
	 * <p>If this ThreadLock cannot be owned, or if it is not locked, this call
	 * will silently return.
	 *
	 * @see #couldUnlock
	 * @exception IllegalStateException the particular type of this ThreadLock
	 *                                  does not allow the current thread to
	 *                                  unlock it.
	 */
	public abstract void unlock();

	/**
	 * Checks whether this ThreadLock is locked, by this or any other thread.
	 *
	 * @see #couldLock
	 * @return true if this object is locked; false otherwise.
	 */
	public abstract boolean isLocked();

	/**
	 * Checks whether this ThreadLock can be owned.  If this method returns
	 * false, calls to <code>getOwner()</code> will always return
	 * <code>null</code>, calls to <code>mine()</code> will always return false,
	 * calls to <code>couldUnlock()</code> will always return true, and calls to
	 * <code>unlock()</code> will always return silently.
	 *
	 * @return true if this ThreadLock can be owned; false otherwise.
	 */
	public abstract boolean isOwnable();

	/**
	 * Returns the thread in control of this ThreadLock.
	 *
	 * @return a reference to the Thread which has locked this ThreadLock.  If
	 *         this object is not locked (if <code>isLocked()</code> returns
	 *         false), null will be returned.  If the lock is of a type for
	 *         which this question is meaningless, null will be returned.  If
	 *         (for any reason) access to this information is denied, null will
	 *         be returned.
	 */
	public abstract Thread getOwner();

	/**
	 * Checks that the current thread controls this ThreadLock.  If this object
	 * is of a type for which this question is meaningless, false will be
	 * returned.  Subclasses should override this method if they wish to use a
	 * definition not based on getOwner().
	 *
	 * @see #getOwner
	 * @see #couldUnlock
	 * @return true if the current thread has locked this object; false
	 *         otherwise.  Also false if this object cannot be owned by a
	 *         thread.
	 */
	public boolean mine() {return getOwner()==Thread.currentThread();}

	/**
	 * Checks that the current thread could lock this ThreadLock without
	 * blocking.  Does not actually affect the lock.  Unless some other
	 * synchronization mechanism is used to guard a call to this function and a
	 * call to lock() or tryLock(), the return value may be incorrect by the
	 * time such an attempt to lock is made.
	 *
	 * @see #isLocked
	 * @see #couldUnlock
	 * @return true if the current thread would have succeeded in locking this
	 *         object had it instead called tryLock(); false otherwise.  This is
	 *         typically the case if and only if this object is not locked or it
	 *         has been locked but by the current thread.
	 */
	public boolean couldLock() {return !isLocked()||mine();}

	/**
	 * Checks that the current thread could unlock this ThreadLock.  Does not
	 * actually affect the lock.  Unless some other synchronization mechanism is
	 * used to guard a call to this function and a call to unlock(), the return
	 * value may be incorrect by the time such an attempt to unlock is made.
	 *
	 * @see #mine
	 * @see #couldLock
	 * @return true if the current thread would have succeeded in unlocking this
	 *         object had it instead called unlock(); false if an exception
	 *         would have been raised.  This is typically the case if and only
	 *         if this object has been locked by this thread.
	 */
	public boolean couldUnlock() {return mine()||!isOwnable();}

	/**
	 * Returns a String representation of this ThreadLock.
	 */
	public String toString() {
		return getClass().getName()+'['+toString0()+']';
	}

	/**
	 * Returns a description of the internals of this ThreadLock.
	 */
	protected String toString0() {
		return name!=null?name+(isLocked()?" (locked)":""):isLocked()?"locked":"";
	}
}
