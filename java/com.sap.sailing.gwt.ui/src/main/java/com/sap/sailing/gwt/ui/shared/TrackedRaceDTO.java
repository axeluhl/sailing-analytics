package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TrackedRaceDTO implements IsSerializable {
    /**
     * Default constructor for GWT-Serialization
     */
    public TrackedRaceDTO() {}
    
    public boolean hasWindData;
    public boolean hasGPSData;

    public Date startOfTracking;
    public Date endOfTracking;
    public Date timePointOfNewestEvent;
    public long delayToLiveInMs;
    
    public boolean isLive() {
        long eventTimeoutTolerance = 60 * 1000; // 60s
        long liveTimePointInMillis = System.currentTimeMillis() - delayToLiveInMs;
        boolean live = timePointOfNewestEvent != null
                && liveTimePointInMillis < timePointOfNewestEvent.getTime() + eventTimeoutTolerance
                && startOfTracking != null && liveTimePointInMillis > startOfTracking.getTime();
        return live;
    }
}
