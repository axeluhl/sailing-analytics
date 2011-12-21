package com.sap.sailing.gwt.ui.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindInfoForRaceDAO implements IsSerializable {
    public Map<String, WindTrackInfoDAO> windTrackInfoByWindSourceName;
    public String selectedWindSourceName;
    public boolean raceIsKnownToStartUpwind;
    
    public WindInfoForRaceDAO() {}
}
