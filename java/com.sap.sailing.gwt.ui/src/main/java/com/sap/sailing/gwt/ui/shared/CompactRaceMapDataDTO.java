package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.common.Util;

/**
 * A compact representation of a {@link RaceMapDataDTO} that represents competitors by their ID only instead of a
 * complete {@link CompetitorDTO} object. This way, a caller of the
 * {@link SailingServiceAsync#getRaceMapData(com.sap.sailing.domain.common.RegattaAndRaceIdentifier, java.util.Date, Map, Map, boolean, com.google.gwt.user.client.rpc.AsyncCallback)}
 * method that has to provide the IDs of all competitors already will already know all those {@link CompetitorDTO}
 * objects already. There is no need to serialize the {@link CompetitorDTO}s either way.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompactRaceMapDataDTO implements IsSerializable {
    private CompactBoatPositionsDTO boatPositionsByCompetitorIdAsString;

    private CoursePositionsDTO coursePositions;
    private List<SidelineDTO> courseSidelines;
    private List<CompactQuickRankDTO> quickRanks;
    private LinkedHashMap<String, Integer> competitorsInOrderOfWindwardDistanceTraveledFarthestFirstIdAsStringAndOneBasedLegNumber;
    private long simulationResultVersion;
    
    /**
     * <code>null</code> if the client's request contained an equal MD5 as the race's competitors {@link RaceDefinition#getCompetitorMD5() produce};
     * otherwise, the set of competitor IDs, {@link Object#toString() converted to strings}, in no particular order.
     */
    private HashSet<String> raceCompetitorIdsAsStrings;
    
    CompactRaceMapDataDTO() {}

    public CompactRaceMapDataDTO(Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> boatPositions, CoursePositionsDTO coursePositions,
           List<SidelineDTO> courseSidelines, QuickRanksDTO quickRanks, long simulationResultVersion, HashSet<String> raceCompetitorIdsAsStrings) {
        this.boatPositionsByCompetitorIdAsString = new CompactBoatPositionsDTO(boatPositions);
        this.raceCompetitorIdsAsStrings = raceCompetitorIdsAsStrings;
        this.quickRanks = new ArrayList<CompactQuickRankDTO>(quickRanks == null ? 0 : Util.size(quickRanks.getQuickRanks()));
        this.competitorsInOrderOfWindwardDistanceTraveledFarthestFirstIdAsStringAndOneBasedLegNumber = new LinkedHashMap<>();
        Map<String, Integer> competitorIdAsStringToOneBasedLegNumber = new HashMap<>();
        if (quickRanks != null) {
            for (QuickRankDTO quickRank : quickRanks.getQuickRanks()) {
                this.quickRanks.add(new CompactQuickRankDTO(quickRank.competitor.getIdAsString(), quickRank.rank,
                        quickRank.legNumberOneBased));
                competitorIdAsStringToOneBasedLegNumber.put(quickRank.competitor.getIdAsString(), quickRank.legNumberOneBased);
            }
            for (CompetitorDTO competitorInOrderOfWindwardDistanceTraveled : quickRanks.getCompetitorsInOrderOfWindwardDistanceTraveledFarthestFirst()) {
                this.competitorsInOrderOfWindwardDistanceTraveledFarthestFirstIdAsStringAndOneBasedLegNumber.put(
                        competitorInOrderOfWindwardDistanceTraveled.getIdAsString(),
                        competitorIdAsStringToOneBasedLegNumber.get(competitorInOrderOfWindwardDistanceTraveled.getIdAsString()));
            }
        }
        this.courseSidelines = courseSidelines;
        this.coursePositions = coursePositions;
        this.simulationResultVersion = simulationResultVersion;
    }
    
    public RaceMapDataDTO getRaceMapDataDTO(Map<String, CompetitorDTO> competitorsByIdAsString) {
        RaceMapDataDTO result = new RaceMapDataDTO();
        result.quickRanks = new LinkedHashMap<CompetitorDTO, QuickRankDTO>(this.quickRanks.size());
        for (CompactQuickRankDTO compactQuickRank : this.quickRanks) {
            final CompetitorDTO competitorDTO = competitorsByIdAsString.get(compactQuickRank.getCompetitorIdAsString());
            if (competitorDTO != null) {
                result.quickRanks.put(competitorDTO, new QuickRankDTO(competitorDTO, compactQuickRank.getRank(), compactQuickRank.getLegNumber()));
            }
        }
        result.courseSidelines = courseSidelines;
        result.coursePositions = coursePositions;
        result.boatPositions = boatPositionsByCompetitorIdAsString.getBoatPositionsForCompetitors(competitorsByIdAsString);
        result.competitorsInOrderOfWindwardDistanceTraveledWithOneBasedLegNumber = new LinkedHashMap<>();
        for (Entry<String, Integer> i : competitorsInOrderOfWindwardDistanceTraveledFarthestFirstIdAsStringAndOneBasedLegNumber.entrySet()) {
            CompetitorDTO c = competitorsByIdAsString.get(i.getKey());
            if (c != null) {
                result.competitorsInOrderOfWindwardDistanceTraveledWithOneBasedLegNumber.put(c, i.getValue());
            }
        }
        result.simulationResultVersion = simulationResultVersion;
        result.raceCompetitorIdsAsStrings = this.raceCompetitorIdsAsStrings;
        return result;
    }
}
