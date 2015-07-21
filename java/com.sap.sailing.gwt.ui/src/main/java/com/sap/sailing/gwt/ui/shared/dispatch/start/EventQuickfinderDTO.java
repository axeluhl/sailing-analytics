package com.sap.sailing.gwt.ui.shared.dispatch.start;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;
import com.sap.sse.common.TimePoint;

public class EventQuickfinderDTO extends EventReferenceDTO implements DTO, Comparable<EventQuickfinderDTO> {
    private String baseURL;
    private boolean isOnRemoteServer;
    private TimePoint startTimePoint;

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public boolean isOnRemoteServer() {
        return isOnRemoteServer;
    }

    public void setOnRemoteServer(boolean isOnRemoteServer) {
        this.isOnRemoteServer = isOnRemoteServer;
    }

    public TimePoint getStartTimePoint() {
        return startTimePoint;
    }

    public void setStartTimePoint(TimePoint startTimePoint) {
        this.startTimePoint = startTimePoint;
    }
    
    @Override
    public int compareTo(EventQuickfinderDTO o) {
        return -startTimePoint.compareTo(o.startTimePoint);
    }
}
