package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.CompetitorDAO;

public interface CompetitorSelectionProvider {

    /**
     * @return a non-<code>null</code> sequence of competitors which may be empty; order doesn't have to be stable; all
     *         competitors contained in the structure returned are also in the structure returned by
     *         {@link #getAllCompetitors()} at the time of the call.
     */
    Iterable<CompetitorDAO> getSelectedCompetitors();
    
    Iterable<CompetitorDAO> getAllCompetitors();
    
    boolean isSelected(CompetitorDAO competitor);

    void addCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener);

    void removeCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener);
}
