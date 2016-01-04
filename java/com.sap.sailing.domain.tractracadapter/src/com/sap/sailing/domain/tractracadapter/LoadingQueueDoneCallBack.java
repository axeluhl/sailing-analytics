package com.sap.sailing.domain.tractracadapter;

import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tractracadapter.impl.AbstractReceiverWithQueue;
import com.sap.sailing.domain.tractracadapter.impl.TracTracRaceTrackerImpl;

/**
 * Callback interface which is currently used, so that {@link Receiver}s can be told to callback when current queue
 * content is worked through.
 * 
 * When loading -> tracking or loading -> finished would happen, we want to wait for all the current events in the queue
 * to be handled before notifying the {@link TrackedRace}. This will ensure that caches, polar miner, etc. will not be
 * resumed/started, as long as loading events are handled.
 * 
 * @author Frederik Petersen
 *
 */
public interface LoadingQueueDoneCallBack {

    /**
     * Callback method
     * See {@link AbstractReceiverWithQueue} and {@link TracTracRaceTrackerImpl} for example usage.
     * 
     * @param receiver Receiver that handled the marked event.
     */
    void loadingQueueDone(Receiver receiver);
    
}
