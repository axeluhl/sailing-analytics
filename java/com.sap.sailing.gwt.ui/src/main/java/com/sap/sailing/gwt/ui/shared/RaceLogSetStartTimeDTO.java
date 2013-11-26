package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceLogSetStartTimeDTO implements IsSerializable {
    public String leaderboardName;
    public String raceColumnName;
    public String fleetName;
    public int passId;
    public String authorName;
    public Integer authorPriority;
    public Date logicalTimePoint;
    public Date startTime;
}
