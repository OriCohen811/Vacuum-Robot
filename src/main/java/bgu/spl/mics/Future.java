package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	private T result;

	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		this.result = null;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	public synchronized T get() {
        while (!isDone()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null; 
            }
        }
        return this.result;
	}
	
	/**
     * Resolves the result of this Future object.
	 * The threads that are currently waiting for a change, waiting for this change.
     */
	public synchronized void resolve (T result) {
        if (!isDone()) {
            this.result = result;
            this.notifyAll();
        }
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	public synchronized boolean isDone() {
		return result != null;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public T get(long timeout, TimeUnit unit) {
        if (!isDone()) {
            try {
                long millsec = TimeUnit.MILLISECONDS.convert(timeout, unit);
                Thread.currentThread();
                Thread.sleep(millsec);

            } catch (InterruptedException e) {
                return this.result;
            }
        }
        return this.result;
	}

}
