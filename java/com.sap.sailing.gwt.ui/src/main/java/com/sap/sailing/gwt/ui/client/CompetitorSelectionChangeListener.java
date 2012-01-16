package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.CompetitorDAO;


/**
 * Allows UI components to observe a competitor selector, such as a drop-down box showing a list of competitors
 */
public interface CompetitorSelectionChangeListener {

    /**
     * The first element is the first one selected
     * @param selectedCompetitors a non-<code>null</code> list which is empty if nothing is selected
     */
    void onCompetitorSelectionChange(List<CompetitorDAO> selectedCompetitors);

}
