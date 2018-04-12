package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sse.common.Color;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public interface CompetitorSelectionProvider {

    /**
     * @return a non-<code>null</code> sequence of competitors which may be empty; order tries to represent the order in
     *         which elements were selected; all competitors contained in the structure returned are also in the
     *         structure returned by {@link #getAllCompetitors()} at the time of the call.
     */
    Iterable<CompetitorWithBoatDTO> getSelectedCompetitors();
    
    /**
     * The intersection of {@link #getSelectedCompetitors()} and {@link #getFilteredCompetitors()}
     */
    Iterable<CompetitorWithBoatDTO> getSelectedFilteredCompetitors();
    
    /**
     * @return a non-<code>null</code> sequence of all competitors which may be empty.
     */
    Iterable<CompetitorWithBoatDTO> getAllCompetitors();

    /**
     * @return a non-<code>null</code> sequence of all competitors filtered by the applied
     * {@link #getCompetitorsFilterSet() filter set} (which may be null).
     */
    Iterable<CompetitorWithBoatDTO> getFilteredCompetitors();

    /**
     * Updates the selection state of <code>competitor</code> if contained in {@link #getAllCompetitors()}. If this
     * means a change in <code>competitor</code>'s selection state, all listeners except for those in
     * <code>listenersNotToNotify</code> will be informed.
     */
    void setSelected(CompetitorWithBoatDTO competitor, boolean selected, CompetitorSelectionChangeListener... listenersNotToNotify);

    /**
     * Those competitors in <code>newSelection</code> that are also in {@link #getAllCompetitors()} will be selected,
     * all others from {@link #getAllCompetitors()} will be deselected. Order will only remain stable for new additions
     * to the selection; previously selected elements remain at their position in the {@link #getSelectedCompetitors()
     * selection}.
     * 
     * @param listenersNotToNotify
     *            if provided, these listeners will not be notified about the selection changes caused by this call
     */
    void setSelection(Iterable<CompetitorWithBoatDTO> newSelection, CompetitorSelectionChangeListener... listenersNotToNotify);
    
    /**
     * Deselects and removes all competitors from {@link #getAllCompetitors()} which are not in <code>newCompetitor</code> and
     * adds all from <code>newCompetitors</code> which as not yet in {@link #getAllCompetitors()}. Afterwards, the contents
     * of {@link #getAllCompetitors()} are equal to <code>newCompetitors</code> except for ordering which is not guaranteed
     * to be stable.
     */
    void setCompetitors(Iterable<CompetitorWithBoatDTO> newCompetitors, CompetitorSelectionChangeListener... listenersNotToNotify);
    
    boolean isSelected(CompetitorWithBoatDTO competitor);
    
    boolean hasMultiSelection();

    Color getColor(CompetitorWithBoatDTO competitor);
    
    void addCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener);

    void removeCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener);
    
    public FilterSet<CompetitorWithBoatDTO, Filter<CompetitorWithBoatDTO>> getCompetitorsFilterSet();

    public void setCompetitorsFilterSet(FilterSet<CompetitorWithBoatDTO, Filter<CompetitorWithBoatDTO>> competitorsFilterSet);

    FilterSet<CompetitorWithBoatDTO, Filter<CompetitorWithBoatDTO>> getOrCreateCompetitorsFilterSet(String nameToAssignToNewFilterSet);
    
    /**
     * Returns <code>true</code> if the provider has any filters that will restrain the selection.
     */
    public boolean hasActiveFilters();
    
    /**
     * Removes all filters and notifies listeners about the change.
     */
    public void clearAllFilters();
    
    /**
     * @return the size of all filtered competitors
     */
    public int getFilteredCompetitorsListSize();
}
