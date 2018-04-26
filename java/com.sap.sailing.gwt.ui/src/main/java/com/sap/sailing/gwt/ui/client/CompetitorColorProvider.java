package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sse.common.Color;

/**
 * 
 * A color provider for a competitor. A color of a competitor can either be the same for all races of a regatta or it
 * can be race specific. Race specific can make sense e.g. when a competitor is changing it's boat during a regatta.
 * 
 * @author Frank Mittag
 */
public interface CompetitorColorProvider {
    Color getColor(CompetitorDTO competitor);

    Color getColor(CompetitorDTO competitor, RegattaAndRaceIdentifier raceIdentfier);

    void addBlockedColor(Color color);

    void removeBlockedColor(Color color);
}
