package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;

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
    private Map<String, List<GPSFixDTO>> boatPositionsByCompetitorIdAsString;
    private CoursePositionsDTO coursePositions;
    private List<SidelineDTO> courseSidelines;
    private List<CompactQuickRankDTO> quickRanks;
    private int simulationResultVersion;
    
    CompactRaceMapDataDTO() {}

    public CompactRaceMapDataDTO(Map<CompetitorDTO, List<GPSFixDTO>> boatPositions, CoursePositionsDTO coursePositions,
           List<SidelineDTO> courseSidelines, List<QuickRankDTO> quickRanks, int simulationResultVersion) {
        this.boatPositionsByCompetitorIdAsString = new HashMap<String, List<GPSFixDTO>>();
        for (Map.Entry<CompetitorDTO, List<GPSFixDTO>> e : boatPositions.entrySet()) {
            this.boatPositionsByCompetitorIdAsString.put(e.getKey().getIdAsString(), e.getValue());
        }
        this.quickRanks = new ArrayList<CompactQuickRankDTO>(quickRanks == null ? 0 : quickRanks.size());
        if (quickRanks != null) {
            for (QuickRankDTO quickRank : quickRanks) {
                this.quickRanks.add(new CompactQuickRankDTO(quickRank.competitor.getIdAsString(), quickRank.rank,
                        quickRank.legNumberOneBased));
            }
        }
        this.courseSidelines = courseSidelines;
        this.coursePositions = coursePositions;
        this.simulationResultVersion = simulationResultVersion;
    }
    
    public RaceMapDataDTO getRaceMapDataDTO(Iterable<CompetitorDTO> competitors) {
        Map<String, CompetitorDTO> competitorsByIdAsString = new HashMap<String, CompetitorDTO>();
        for (CompetitorDTO competitor : competitors) {
            competitorsByIdAsString.put(competitor.getIdAsString(), competitor);
        }
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
        result.boatPositions = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        for (Map.Entry<String, List<GPSFixDTO>> e : boatPositionsByCompetitorIdAsString.entrySet()) {
            final CompetitorDTO competitor = competitorsByIdAsString.get(e.getKey());
            if (competitor != null) {
                // maybe null in case the competitor was added, e.g., by unsuppressing, while this call was underway
                result.boatPositions.put(competitor, e.getValue());
            }
        }
        result.simulationResultVersion = simulationResultVersion;
        return result;
    }
}
