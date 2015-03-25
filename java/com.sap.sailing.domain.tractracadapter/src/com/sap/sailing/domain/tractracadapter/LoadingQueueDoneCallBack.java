package com.sap.sailing.domain.tractracadapter;

import com.sap.sailing.domain.tractracadapter.impl.AbstractReceiverWithQueue;
import com.sap.sailing.domain.tractracadapter.impl.TracTracRaceTrackerImpl;

public interface LoadingQueueDoneCallBack {

    /**
     * Callback method
     * See {@link AbstractReceiverWithQueue} and {@link TracTracRaceTrackerImpl} for example usage.
     * 
     * @param receiver Receiver that handled the marked event.
     */
    void loadingQueueDone(Receiver receiver);
    
}
