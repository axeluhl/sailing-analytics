package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RegattaOverviewEntryDTO implements IsSerializable {
    public String courseAreaName;
    public String regattaName;
    public RaceInfoDTO raceInfo;

    
    // for GWT serialization
    public RegattaOverviewEntryDTO() { }
    
}
