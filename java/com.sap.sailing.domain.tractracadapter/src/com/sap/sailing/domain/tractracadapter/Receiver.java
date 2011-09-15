package com.sap.sailing.domain.tractracadapter;

import com.maptrack.client.io.TypeController;

public interface Receiver {
    /**
     * Removes all already received events from this receiver's queue and tells the receiver to stop its thread
     * immediately. The currently processing event will finish processing; all other queued events will not be handled
     * anymore.
     */
    void stopPreemptively();
    
    void stopAfterProcessingQueuedEvents();

    Iterable<TypeController> getTypeControllersAndStart();
    
    /**
     * Waits until this receiver has stopped
     */
    void join() throws InterruptedException;

    /**
     * Waits until this received has stopped, but no longer than <code>timeout</code> milliseconds
     */
    void join(long timeoutInMilliseconds) throws InterruptedException;
}
