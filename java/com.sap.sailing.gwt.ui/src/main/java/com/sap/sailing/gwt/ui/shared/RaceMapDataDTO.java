package com.sap.sailing.gwt.ui.shared;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class RaceMapDataDTO implements IsSerializable {
    public Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> boatPositions;
    public CoursePositionsDTO coursePositions;
    public List<SidelineDTO> courseSidelines;
    public LinkedHashMap<CompetitorDTO, QuickRankDTO> quickRanks;
    public LinkedHashMap<CompetitorDTO, Integer> competitorsInOrderOfWindwardDistanceTraveledWithOneBasedLegNumber;
    public long simulationResultVersion;
    
    /**
     * The competitor IDs in their {@link Object#toString()} representation, for all competitors in the race, including those
     * that may be suppressed in a leaderboard.
     */
    public HashSet<String> raceCompetitorIdsAsStrings;
}
