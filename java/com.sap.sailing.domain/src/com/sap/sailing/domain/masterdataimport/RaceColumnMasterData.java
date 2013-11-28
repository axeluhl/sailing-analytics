package com.sap.sailing.domain.masterdataimport;

import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RaceIdentifier;

public class RaceColumnMasterData {
    private final String name;
    private final boolean medal;
    private final Map<String, RaceIdentifier> raceIdentifiersByFleetName;
    private Double factor;
    private Set<WindTrackMasterData> windTracks;
    
    public RaceColumnMasterData(String name, boolean medal, Map<String, RaceIdentifier> raceIdentifiersByFleetName,
            Double factor, Set<WindTrackMasterData> windTracks) {
        super();
        this.name = name;
        this.medal = medal;
        this.raceIdentifiersByFleetName = raceIdentifiersByFleetName;
        this.factor = factor;
        this.windTracks = windTracks;
    }

    public String getName() {
        return name;
    }

    public boolean isMedal() {
        return medal;
    }

    public Map<String, RaceIdentifier> getRaceIdentifiersByFleetName() {
        return raceIdentifiersByFleetName;
    }

    public Double getFactor() {
        return factor;
    }
    
    public Set<WindTrackMasterData> getWindTrackMasterData() {
        return windTracks;
    }

}
