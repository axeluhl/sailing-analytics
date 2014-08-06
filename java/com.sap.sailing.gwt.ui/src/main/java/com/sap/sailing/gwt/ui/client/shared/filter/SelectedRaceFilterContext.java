package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.RaceIdentifier;

public interface SelectedRaceFilterContext {
    void setSelectedRace(RaceIdentifier selectedRace);
    
    void setQuickRankProvider(QuickRankProvider quickRankProvider);
    
    RaceIdentifier getSelectedRace();
}
