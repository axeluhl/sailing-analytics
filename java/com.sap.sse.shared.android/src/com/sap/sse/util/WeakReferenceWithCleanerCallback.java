package com.sap.sse.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A generic weak reference that maintains a {@link Runnable callback} which is invoked when the reference
 * is enqueued after it has been cleared by the garbage collector.<p>
 * 
 * Make sure to not create a strong reference from the callback to the referent because this will keep the
 * weak reference from ever being enqueued as its referent will remain strongly referenced.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public class WeakReferenceWithCleanerCallback<T> extends WeakReference<T> {
    private static final Logger logger = Logger.getLogger(WeakReferenceWithCleanerCallback.class.getName());
    private static ReferenceQueue<WeakReferenceWithCleanerCallback<?>> queue = new ReferenceQueue<>();
    
    private final Runnable cleanerCallback;
    
    static {
        Thread t = new Thread(WeakReferenceWithCleanerCallback.class.getSimpleName() + " weak reference cleaner") {
            @Override
            public void run() {
                while (true) {
                    try {
                        final Reference<? extends WeakReferenceWithCleanerCallback<?>> ref = queue.remove();
                        final Runnable callback = ((WeakReferenceWithCleanerCallback<?>) ref).cleanerCallback;
                        if (callback != null) {
                            callback.run();
                        }
                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, "Error trying to clean weak reference", e);
                    } catch (Exception e2) {
                        logger.severe("Shouldn't have gotten here. Continuing...");
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    /**
     * @param cleanerCallback
     *            ensure that no strong reference exists from this parameter object to the {@code referent} or else this
     *            weak reference will never be cleared and hence will never be enqueued.
     */
    public WeakReferenceWithCleanerCallback(T referent, Runnable cleanerCallback) {
        super(referent);
        this.cleanerCallback = cleanerCallback;
    }
}

