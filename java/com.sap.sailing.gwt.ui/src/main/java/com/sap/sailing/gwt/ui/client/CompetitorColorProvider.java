package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sse.common.Color;

public interface CompetitorColorProvider {
    Color getColor(CompetitorDTO competitor);

    Color getColor(CompetitorDTO competitor, RaceColumnDTO raceColumn);
    
    void addBlockedColor(Color color);
    void removeBlockedColor(Color color);
}
