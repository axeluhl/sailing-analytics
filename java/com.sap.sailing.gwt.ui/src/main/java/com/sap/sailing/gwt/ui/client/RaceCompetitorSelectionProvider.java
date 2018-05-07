package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.Color;

public interface RaceCompetitorSelectionProvider extends CompetitorSelectionProvider {

    Color getColor(CompetitorDTO competitor, RegattaAndRaceIdentifier raceIdentfier);
    
    void setBoat(CompetitorDTO competitor, BoatDTO boat);

    BoatDTO getBoat(CompetitorDTO competitor);
}
