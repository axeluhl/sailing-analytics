package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.TimePoint;

public class RaceLogSetTrackingTimesDTO implements IsSerializable {
    public String leaderboardName;
    public String raceColumnName;
    public String fleetName;
    public String authorName;
    public Integer authorPriority;
    public TimePoint logicalTimePoint;
    public TimePoint newStartOfTracking;
    public TimePoint newEndOfTracking;
    public TimePoint currentStartOfTracking;
    public TimePoint currentEndOfTracking;
}
