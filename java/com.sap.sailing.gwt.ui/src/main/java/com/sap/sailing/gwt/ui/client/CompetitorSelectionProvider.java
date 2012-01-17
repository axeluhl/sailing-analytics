package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.CompetitorDAO;

public interface CompetitorSelectionProvider {

    /**
     * @return a non-<code>null</code> sequence of competitors which may be empty; order tries to represent the order in
     *         which elements were selected; all competitors contained in the structure returned are also in the
     *         structure returned by {@link #getAllCompetitors()} at the time of the call.
     */
    Iterable<CompetitorDAO> getSelectedCompetitors();
    
    Iterable<CompetitorDAO> getAllCompetitors();

    /**
     * Updates the selection state of <code>competitor</code> if contained in {@link #getAllCompetitors()}. If this
     * means a change in <code>competitor</code>'s selection state, all listeners except for those in
     * <code>listenersNotToNotify</code> will be informed.
     */
    void setSelected(CompetitorDAO competitor, boolean selected, CompetitorSelectionChangeListener... listenersNotToNotify);

    /**
     * Those competitors in <code>newSelection</code> that are also in {@link #getAllCompetitors()} will be selected,
     * all others from {@link #getAllCompetitors()} will be deselected. Order will only remain stable for new additions
     * to the selection; previously selected elements remain at their position in the {@link #getSelectedCompetitors()
     * selection}.
     * 
     * @param listenersNotToNotify
     *            if provided, these listeners will not be notified about the selection changes caused by this call
     */
    void setSelection(Iterable<CompetitorDAO> newSelection, CompetitorSelectionChangeListener... listenersNotToNotify);
    
    /**
     * Deselects and removes all competitors from {@link #getAllCompetitors()} which are not in <code>newCompetitor</code> and
     * adds all from <code>newCompetitors</code> which as not yet in {@link #getAllCompetitors()}. Afterwards, the contents
     * of {@link #getAllCompetitors()} are equal to <code>newCompetitors</code> except for ordering which is not guaranteed
     * to be stable.
     */
    void setCompetitors(Iterable<CompetitorDAO> newCompetitors);
    
    boolean isSelected(CompetitorDAO competitor);
    
    boolean hasMultiSelection();

    void addCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener);

    void removeCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener);
}
