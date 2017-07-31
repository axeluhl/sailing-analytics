package com.sap.sailing.gwt.ui.shared;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.Duration;

public class RaceMapDataDTO implements IsSerializable {
    public Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> boatPositions;
    public CoursePositionsDTO coursePositions;
    public List<SidelineDTO> courseSidelines;
    public LinkedHashMap<String, QuickRankDTO> quickRanks;
    public long simulationResultVersion;
    
    /**
     * The competitor IDs in their {@link Object#toString()} representation, for all competitors in the race, including those
     * that may be suppressed in a leaderboard.
     */
    public HashSet<String> raceCompetitorIdsAsStrings;
    public Duration estimatedDuration;
    @Override
    public String toString() {
        return "RaceMapDataDTO [boatPositions=" + boatPositions + ", coursePositions=" + coursePositions
                + ", courseSidelines=" + courseSidelines + ", quickRanks=" + quickRanks + ", simulationResultVersion="
                + simulationResultVersion + ", raceCompetitorIdsAsStrings=" + raceCompetitorIdsAsStrings
                + ", estimatedDuration=" + estimatedDuration + "]";
    }
}
