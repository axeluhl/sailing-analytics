package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.CompetitorDAO;

public interface CompetitorSelectionProvider {

    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<CompetitorDAO> getSelectedCompetitors();

    void addCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener);

    void removeCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener);
}
