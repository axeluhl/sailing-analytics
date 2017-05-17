package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.WindSource;

public class WindInfoForRaceDTO implements IsSerializable {
    public Map<WindSource, WindTrackInfoDTO> windTrackInfoByWindSource;
    public boolean raceIsKnownToStartUpwind;
    public Iterable<WindSource> windSourcesToExclude;
    
    private Map<Integer, WindTrackInfoDTO> combinedWindOnLegMiddle = new HashMap<>();
    
    public WindInfoForRaceDTO() {}
    
    public WindTrackInfoDTO getCombinedWindOnLegMiddle(int zeroBasedLegNumber) {
        return combinedWindOnLegMiddle.get(zeroBasedLegNumber);
    }
    
    public void addWindOnLegMiddle(int zeroBasedLegNumber, WindTrackInfoDTO windTrackInfoForLegMiddle) {
        combinedWindOnLegMiddle.put(zeroBasedLegNumber, windTrackInfoForLegMiddle);
    }
}
