package com.sap.sse.common.util;

/**
 * Instances of this class can be used with a {@link Trigger}. When the {@link Trigger}
 * is accessed, all {@link Triggerable}s {@link Trigger#register registered} with the {@link Trigger}
 * will be executed if they haven't already been executed, and then removed from the {@link Trigger}.
 * A {@link Triggerable} may also be explicitly {@link Triggerable#run executed} without the use of
 * a {@link Trigger} which in particular will turn it into a no-op when triggered through any
 * {@link Trigger} with which it is still registered.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class Triggerable implements Runnable {
    private final Runnable runnable;
    
    private boolean hasRun;
    
    public Triggerable(Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * If not run yet
     */
    public void run() {
        final boolean oldHasRun;
        synchronized (this) {
            oldHasRun = hasRun;
            if (!hasRun) {
                hasRun = true;
            }
        }
        if (!oldHasRun) {
            runnable.run();
        }
    }
}
