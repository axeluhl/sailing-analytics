package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.dto.CompetitorDTO;


/**
 * Allows UI components to observe a competitor selector, such as a drop-down box showing a list of competitors
 */
public interface CompetitorSelectionChangeListener {
    
    void competitorsListChanged(Iterable<CompetitorDTO> competitors);
    
    void addedToSelection(CompetitorDTO competitor);
    void removedFromSelection(CompetitorDTO competitor);
}
