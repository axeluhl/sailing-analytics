package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RegattaOverviewEntryDTO implements IsSerializable {
    public String courseAreaIdAsString;
    public String courseAreaName;
    public String regattaDisplayName;
    public RaceInfoDTO raceInfo;
    public String regattaName;
    public String boatClassName;
    public Date currentServerTime;
    
    // for GWT serialization
    public RegattaOverviewEntryDTO() { }
    
}
