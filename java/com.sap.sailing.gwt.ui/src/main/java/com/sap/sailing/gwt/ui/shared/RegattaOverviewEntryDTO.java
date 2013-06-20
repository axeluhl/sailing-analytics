package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RegattaOverviewEntryDTO implements IsSerializable {
    public String courseAreaIdAsString;
    public String courseAreaName;
    public String regattaDisplayName;
    public RaceInfoDTO raceInfo;
    public String regattaName;
    
    // for GWT serialization
    public RegattaOverviewEntryDTO() { }
    
}
