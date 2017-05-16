package com.sap.sailing.domain.tractracadapter;

import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tractracadapter.impl.TracTracRaceTrackerImpl;


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
    
    /**
     * Allows to "mark" the currently last event in the queue and provides callback as soon as this event has been
     * handled.
     * <p>
     * 
     * This is used in {@link TracTracRaceTrackerImpl} to ensure that events queued during loading phase will be
     * processed before the new {@link TrackedRaceStatus} is propagated to the {@link TrackedRace}.
     * 
     * @param callback
     *            {@link LoadingQueueDoneCallBack#loadingQueueDone(Receiver)} Will be called as soon as the event that
     *            was last in the queue at the time of calling
     *            {@link #callBackWhenLoadingQueueIsDone(LoadingQueueDoneCallBack)} has been handled.
     */
    void callBackWhenLoadingQueueIsDone(LoadingQueueDoneCallBack callback);
}
