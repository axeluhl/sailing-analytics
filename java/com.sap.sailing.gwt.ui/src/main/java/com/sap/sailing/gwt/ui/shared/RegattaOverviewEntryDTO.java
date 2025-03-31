package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Distance;

public class RegattaOverviewEntryDTO implements IsSerializable {
    public String courseAreaIdAsString;
    public String courseAreaName;
    public String regattaDisplayName;
    public RaceInfoDTO raceInfo;
    public String leaderboardName;
    public String boatClassName;
    public Date currentServerTime;
    public Distance buyZoneRadius;

    // for GWT serialization
    public RegattaOverviewEntryDTO() { }
    
}
