package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;


/**
 * Allows UI components to observe a competitor selector, such as a drop-down box showing a list of competitors
 */
public interface CompetitorSelectionChangeListener {
    
    void competitorsListChanged(Iterable<CompetitorWithBoatDTO> competitors);
    
    /**
     * Notifies listeners of a change of the competitor filter set. Note that if the list of filtered competitors has
     * changed, a separate call to filteredCompetitorsListChanged will occur, and that this does not necessarily have to
     * be the case, e.g., if old and new filter match the same set of competitors.
     */
    void filterChanged(FilterSet<CompetitorWithBoatDTO, ? extends Filter<CompetitorWithBoatDTO>> oldFilterSet, FilterSet<CompetitorWithBoatDTO, ? extends Filter<CompetitorWithBoatDTO>> newFilterSet);
    
    /**
     * Notifies listeners that the list of filtered competitors has changed. Possible reasons include a change of the
     * filter condition or a change in the overall list of competitors.
     */
    void filteredCompetitorsListChanged(Iterable<CompetitorWithBoatDTO> filteredCompetitors);
    
    void addedToSelection(CompetitorWithBoatDTO competitor);
    void removedFromSelection(CompetitorWithBoatDTO competitor);
}
