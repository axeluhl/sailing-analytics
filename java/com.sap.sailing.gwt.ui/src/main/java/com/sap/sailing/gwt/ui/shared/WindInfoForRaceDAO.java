package com.sap.sailing.gwt.ui.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.WindSource;

public class WindInfoForRaceDAO implements IsSerializable {
    public Map<WindSource, WindTrackInfoDAO> windTrackInfoByWindSource;
    public WindSource selectedWindSource;
    public boolean raceIsKnownToStartUpwind;
    
    public WindInfoForRaceDAO() {}
}
