package com.sap.sailing.gwt.ui.client;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sse.common.Color;

public class RaceCompetitorSelectionModel extends CompetitorSelectionModel implements RaceCompetitorSelectionProvider {
    private final Map<CompetitorWithBoatDTO, BoatDTO> boatsOfCompetitors;
    
    public RaceCompetitorSelectionModel(boolean hasMultiSelection) {
        super(hasMultiSelection);
        boatsOfCompetitors = new HashMap<>();
    }

    public RaceCompetitorSelectionModel(boolean hasMultiSelection, CompetitorColorProvider competitorColorProvider, Map<CompetitorWithBoatDTO, BoatDTO> boatsOfCompetitors) {
        super(hasMultiSelection, competitorColorProvider);
        this.boatsOfCompetitors = boatsOfCompetitors;
        addAll(boatsOfCompetitors.keySet());
    }

    @Override
    public Color getColor(CompetitorWithBoatDTO competitor, RegattaAndRaceIdentifier raceIdentfier) {
        return allCompetitors.contains(competitor) ? competitorColorProvider.getColor(competitor, raceIdentfier) : Color.WHITE /* safe default */;
    }

    @Override
    public void setBoat(CompetitorWithBoatDTO competitor, BoatDTO boat) {
        boatsOfCompetitors.put(competitor, boat);        
    }

    @Override
    public BoatDTO getBoat(CompetitorWithBoatDTO competitor) {
        return boatsOfCompetitors.get(competitor);        
    }
}
