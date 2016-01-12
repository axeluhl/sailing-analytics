package com.sap.sailing.gwt.ui.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class CompactBoatPositionsDTO implements IsSerializable {
    private Map<String, List<GPSFixDTO>> boatPositions;

    CompactBoatPositionsDTO() {} // for GWT serialization only
    
    public CompactBoatPositionsDTO(Map<CompetitorDTO, List<GPSFixDTO>> boatPositions) {
        super();
        this.boatPositions = new HashMap<>();
        for (final Entry<CompetitorDTO, List<GPSFixDTO>> e : boatPositions.entrySet()) {
            this.boatPositions.put(e.getKey().getIdAsString(), e.getValue());
        }
    }

    public Map<CompetitorDTO, List<GPSFixDTO>> getBoatPositionsForCompetitors(Map<String, CompetitorDTO> competitorsByIdsAsStrings) {
        final Map<CompetitorDTO, List<GPSFixDTO>> result = new HashMap<>();
        for (final Entry<String, List<GPSFixDTO>> e : boatPositions.entrySet()) {
            result.put(competitorsByIdsAsStrings.get(e.getKey()), e.getValue());
        }
        return result;
    }
}
