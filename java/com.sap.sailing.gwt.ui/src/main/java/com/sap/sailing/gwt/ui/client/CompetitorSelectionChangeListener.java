package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.CompetitorDAO;


/**
 * Allows UI components to observe a competitor selector, such as a drop-down box showing a list of competitors
 */
public interface CompetitorSelectionChangeListener {
    void addedToSelection(CompetitorDAO competitor);

    void removedFromSelection(CompetitorDAO competitor);

}
