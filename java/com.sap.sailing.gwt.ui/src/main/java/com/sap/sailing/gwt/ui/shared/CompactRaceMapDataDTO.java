package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;

/**
 * A compact representation of a {@link RaceMapDataDTO} that represents competitors by their ID only instead of a
 * complete {@link CompetitorWithBoatDTO} object. This way, a caller of the
 * {@link SailingServiceAsync#getRaceMapData(com.sap.sailing.domain.common.RegattaAndRaceIdentifier, java.util.Date, Map, Map, boolean, com.google.gwt.user.client.rpc.AsyncCallback)}
 * method that has to provide the IDs of all competitors already will already know all those {@link CompetitorWithBoatDTO}
 * objects already. There is no need to serialize the {@link CompetitorWithBoatDTO}s either way.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompactRaceMapDataDTO implements IsSerializable {
    private CompactBoatPositionsDTO boatPositionsByCompetitorIdAsString;

    private CoursePositionsDTO coursePositions;
    private List<SidelineDTO> courseSidelines;
    private List<CompactQuickRankDTO> quickRanks;
    private long simulationResultVersion;
    
    /**
     * <code>null</code> if the client's request contained an equal MD5 as the race's competitors {@link RaceDefinition#getCompetitorMD5() produce};
     * otherwise, the set of competitor IDs, {@link Object#toString() converted to strings}, in no particular order.
     */
    private HashSet<String> raceCompetitorIdsAsStrings;
    /**
     * Contains a rough estimate of the duration the whole race will take, based on the information available at given timepoint
     */
    private Duration estimatedDuration;
    
    CompactRaceMapDataDTO() {}

    public CompactRaceMapDataDTO(Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> boatPositions, CoursePositionsDTO coursePositions,
           List<SidelineDTO> courseSidelines, QuickRanksDTO quickRanks, long simulationResultVersion, HashSet<String> raceCompetitorIdsAsStrings, Duration estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
        this.boatPositionsByCompetitorIdAsString = new CompactBoatPositionsDTO(boatPositions);
        this.raceCompetitorIdsAsStrings = raceCompetitorIdsAsStrings;
        this.quickRanks = new ArrayList<CompactQuickRankDTO>(quickRanks == null ? 0 : Util.size(quickRanks.getQuickRanks()));
        Map<String, Integer> competitorIdAsStringToOneBasedLegNumber = new HashMap<>();
        if (quickRanks != null) {
            for (QuickRankDTO quickRank : quickRanks.getQuickRanks()) {
                this.quickRanks.add(new CompactQuickRankDTO(quickRank.competitor.getIdAsString(), quickRank.oneBasedRank,
                        quickRank.legNumberOneBased));
                competitorIdAsStringToOneBasedLegNumber.put(quickRank.competitor.getIdAsString(), quickRank.legNumberOneBased);
            }
        }
        this.courseSidelines = courseSidelines;
        this.coursePositions = coursePositions;
        this.simulationResultVersion = simulationResultVersion;
    }
    
    public RaceMapDataDTO getRaceMapDataDTO(Map<String, CompetitorDTO> competitorsByIdAsString) {
        RaceMapDataDTO result = new RaceMapDataDTO();
        result.quickRanks = new LinkedHashMap<String, QuickRankDTO>(this.quickRanks.size());
        for (CompactQuickRankDTO compactQuickRank : this.quickRanks) {
            final CompetitorDTO competitorDTO = competitorsByIdAsString.get(compactQuickRank.getCompetitorIdAsString());
            if (competitorDTO != null) {
                result.quickRanks.put(compactQuickRank.getCompetitorIdAsString(), new QuickRankDTO(competitorDTO, compactQuickRank.getOneBasedRank(), compactQuickRank.getLegNumber()));
            }
        }
        result.courseSidelines = courseSidelines;
        result.coursePositions = coursePositions;
        result.boatPositions = boatPositionsByCompetitorIdAsString.getBoatPositionsForCompetitors(competitorsByIdAsString);
        result.simulationResultVersion = simulationResultVersion;
        result.raceCompetitorIdsAsStrings = this.raceCompetitorIdsAsStrings;
        result.estimatedDuration = estimatedDuration;
        return result;
    }
}
