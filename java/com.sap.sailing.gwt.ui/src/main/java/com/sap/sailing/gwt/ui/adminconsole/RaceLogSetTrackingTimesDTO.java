package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceLogSetTrackingTimesDTO implements IsSerializable {
    public String leaderboardName;
    public String raceColumnName;
    public String fleetName;
    public String authorName;
    public Integer authorPriority;
    public Date logicalTimePoint;
    public Date startOfTracking;
    public Date endOfTracking;
}
