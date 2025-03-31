package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.abstractlog.TimePointSpecificationFoundInLog;
import com.sap.sse.common.TimePoint;

public class RaceLogSetTrackingTimesDTO implements IsSerializable {
    public String leaderboardName;
    public String raceColumnName;
    public String fleetName;
    public String authorName;
    public Integer authorPriority;
    public TimePoint logicalTimePoint;
    public TimePointSpecificationFoundInLog newStartOfTracking;
    public TimePointSpecificationFoundInLog newEndOfTracking;
    public TimePointSpecificationFoundInLog currentStartOfTracking;
    public TimePointSpecificationFoundInLog currentEndOfTracking;
}
