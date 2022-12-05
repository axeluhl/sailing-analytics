package com.sap.sailing.gwt.ui.shared;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class CompactBoatPositionsDTO implements IsSerializable {
    private Map<String, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> boatPositions;

    @Deprecated
    CompactBoatPositionsDTO() {} // for GWT serialization only
    
    public CompactBoatPositionsDTO(Map<CompetitorDTO, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> boatPositions) {
        super();
        this.boatPositions = new HashMap<>(boatPositions.size());
        for (final Entry<CompetitorDTO, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> e : boatPositions.entrySet()) {
            this.boatPositions.put(e.getKey().getIdAsString(), e.getValue());
        }
    }

    public static CompactBoatPositionsDTO fromCompetitorIds(Map<String, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> boatPositionsByCompetitorId) {
        final CompactBoatPositionsDTO dto = new CompactBoatPositionsDTO(Collections.emptyMap());
        dto.boatPositions.putAll(boatPositionsByCompetitorId);
        return dto;
    }

    public Map<String, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> getBoatPositions() {
        return Collections.unmodifiableMap(boatPositions);
    }

    public Map<CompetitorDTO, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> getBoatPositionsForCompetitors(Map<String, CompetitorDTO> competitorsByIdsAsStrings) {
        final Map<CompetitorDTO, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> result = new HashMap<>();
        for (final Entry<String, GPSFixDTOWithSpeedWindTackAndLegTypeIterable> e : boatPositions.entrySet()) {
            result.put(competitorsByIdsAsStrings.get(e.getKey()), e.getValue());
        }
        return result;
    }
}
