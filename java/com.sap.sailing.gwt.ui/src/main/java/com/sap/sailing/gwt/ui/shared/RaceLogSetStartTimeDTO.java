package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;

public class RaceLogSetStartTimeDTO implements IsSerializable {
    public String leaderboardName;
    public String raceColumnName;
    public String fleetName;
    public int passId;
    public String authorName;
    public int authorPriority;
    public Date logicalTimePoint;
    public Date startTime;
}
