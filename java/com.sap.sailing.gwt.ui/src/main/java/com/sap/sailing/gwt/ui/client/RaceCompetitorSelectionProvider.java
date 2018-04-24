package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sse.common.Color;

public interface RaceCompetitorSelectionProvider extends CompetitorSelectionProvider {

    Color getColor(CompetitorWithBoatDTO competitor, RegattaAndRaceIdentifier raceIdentfier);
    
    void setBoat(CompetitorWithBoatDTO competitor, BoatDTO boat);

    BoatDTO getBoat(CompetitorWithBoatDTO competitor);
}
