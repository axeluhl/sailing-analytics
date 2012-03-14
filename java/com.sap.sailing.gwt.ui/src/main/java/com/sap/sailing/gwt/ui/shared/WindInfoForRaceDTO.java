package com.sap.sailing.gwt.ui.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.WindSource;

public class WindInfoForRaceDTO implements IsSerializable {
    public Map<WindSource, WindTrackInfoDTO> windTrackInfoByWindSource;
    public boolean raceIsKnownToStartUpwind;
    public Iterable<WindSource> windSourcesToExclude;
    
    public WindInfoForRaceDTO() {}
}
