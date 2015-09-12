package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.Color;

public interface CompetitorColorProvider {
    Color getColor(CompetitorDTO competitor);
    Color getColor(CompetitorDTO competitor, RegattaAndRaceIdentifier raceIdentfier);
    void setColor(CompetitorDTO competitor, RegattaAndRaceIdentifier raceIdentfier, Color color);
    
    void addBlockedColor(Color color);
    void removeBlockedColor(Color color);
}
