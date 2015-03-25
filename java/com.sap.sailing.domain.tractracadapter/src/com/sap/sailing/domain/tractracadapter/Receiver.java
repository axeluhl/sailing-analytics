package com.sap.sailing.domain.tractracadapter;


public interface Receiver {
    /**
     * Removes all already received events from this receiver's queue and tells the receiver to stop its thread
     * immediately. The currently processing event will finish processing; all other queued events will not be handled
     * anymore. Unsubscribes from receiving further events.
     */
    void stopPreemptively();
    
    /**
     * Processes the events queued so far, then stops. Any other events received after this call will not be handled
     * anymore and therefore get discarded. Unsubscribes from receiving further events.
     */
    void stopAfterProcessingQueuedEvents();
    
    /**
     * Use this for testing only! Waits for <code>timeoutInMilliseconds</code>. If no event is received in that time,
     * the receiver is stopped. Otherwise, the timeout is renewed after the first timeout period has expired (not after
     * the last event was received). Therefore, <code>timeoutInMilliseconds</code> is the <em>minimum</em> timeout
     * period. The maximum timeout period that may occur is <code>2*timeoutInMilliseconds</code>.
     */
    void stopAfterNotReceivingEventsForSomeTime(long timeoutInMilliseconds);

    void subscribe();
    
    /**
     * Waits until this receiver has stopped
     */
    void join() throws InterruptedException;

    /**
     * Waits until this received has stopped, but no longer than <code>timeout</code> milliseconds
     */
    void join(long timeoutInMilliseconds) throws InterruptedException;
    
    void callBackWhenLoadingQueueIsDone(LoadingQueueDoneCallBack callback);
}
