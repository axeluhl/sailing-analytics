package com.sap.sailing.gwt.ui.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sailing.gwt.ui.shared.CompetitorDAO;

public class CompetitorSelectionModel implements CompetitorSelectionProvider {
    private final Set<CompetitorDAO> allCompetitors;
    
    private final LinkedHashSet<CompetitorDAO> selectedCompetitors;
    
    private final Set<CompetitorSelectionChangeListener> listeners;
    
    private final boolean hasMultiSelection;
    
    public CompetitorSelectionModel(boolean hasMultiSelection) {
        super();
        this.hasMultiSelection = hasMultiSelection;
        this.allCompetitors = new HashSet<CompetitorDAO>();
        this.selectedCompetitors = new LinkedHashSet<CompetitorDAO>();
        this.listeners = new HashSet<CompetitorSelectionChangeListener>();
    }
    
    /**
     * Adds a competitor to the {@link #getAllCompetitors() set of all competitors}. If the competitor was not yet
     * contained, it will be deselected.
     */
    public void add(CompetitorDAO competitor) {
        allCompetitors.add(competitor);
    }
    
    @Override
    public boolean hasMultiSelection() {
        return hasMultiSelection;
    }
    
    /**
     * Removes all competitors. Afterwards, this model is empty. Deselection events are issued for all competitors that
     * were selected so far.
     */
    public void clear() {
        Iterator<CompetitorDAO> selIter = getSelectedCompetitors().iterator();
        while (selIter.hasNext()) {
            CompetitorDAO selected = selIter.next();
            setSelected(selected, false);
            selIter = getSelectedCompetitors().iterator();
        }
        assert selectedCompetitors.isEmpty();
        allCompetitors.clear();
    }
    
    public void addAll(Iterable<CompetitorDAO> competitors) {
        for (CompetitorDAO competitor : competitors) {
            add(competitor);
        }
    }

    /**
     * Removes the competitor from the {@link #getAllCompetitors() set of all competitor}. If the competitor was previously
     * contained and selected, it is deselected first.
     */
    public void remove(CompetitorDAO competitor) {
        if (isSelected(competitor)) {
            setSelected(competitor, false);
        }
        allCompetitors.remove(competitor);
    }
    
    @Override
    public Iterable<CompetitorDAO> getSelectedCompetitors() {
        return Collections.unmodifiableCollection(selectedCompetitors);
    }

    @Override
    public Iterable<CompetitorDAO> getAllCompetitors() {
        return Collections.unmodifiableCollection(allCompetitors);
    }
    
    public void setSelected(CompetitorDAO competitor, boolean selected, CompetitorSelectionChangeListener... listenersNotToNotify) {
        if (selected) {
            if (allCompetitors.contains(competitor) && !selectedCompetitors.contains(competitor)) {
                selectedCompetitors.add(competitor);
                fireAddedToSelection(competitor, listenersNotToNotify);
            }
        } else {
            if (selectedCompetitors.contains(competitor)) {
                selectedCompetitors.remove(competitor);
                fireRemovedFromSelection(competitor, listenersNotToNotify);
            }
        }
    }
    
    private void fireAddedToSelection(CompetitorDAO competitor, CompetitorSelectionChangeListener... listenersNotToNotify) {
        for (CompetitorSelectionChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.addedToSelection(competitor);
            }
        }
    }
    
    private void fireRemovedFromSelection(CompetitorDAO competitor, CompetitorSelectionChangeListener... listenersNotToNotify) {
        for (CompetitorSelectionChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.removedFromSelection(competitor);
            }
        }
    }

    @Override
    public boolean isSelected(CompetitorDAO competitor) {
        return selectedCompetitors.contains(competitor);
    }

    @Override
    public void addCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setSelection(Iterable<CompetitorDAO> newSelection, CompetitorSelectionChangeListener... listenersNotToNotify) {
        Set<CompetitorDAO> competitorsToRemoveFromSelection = new HashSet<CompetitorDAO>(selectedCompetitors);
        for (CompetitorDAO newSelectedCompetitor : newSelection) {
            setSelected(newSelectedCompetitor, true, listenersNotToNotify);
        }
        for (CompetitorDAO competitorToRemoveFromSelection : competitorsToRemoveFromSelection) {
            setSelected(competitorToRemoveFromSelection, false, listenersNotToNotify);
        }
    }

    @Override
    public void setCompetitors(Iterable<CompetitorDAO> newCompetitors) {
        Set<CompetitorDAO> oldCompetitorsToRemove = new HashSet<CompetitorDAO>(allCompetitors);
        for (CompetitorDAO newCompetitor : newCompetitors) {
            if (allCompetitors.contains(newCompetitor)) {
                oldCompetitorsToRemove.remove(newCompetitor);
            } else {
                add(newCompetitor);
            }
        }
        for (CompetitorDAO oldCompetitorToRemove : oldCompetitorsToRemove) {
            remove(oldCompetitorToRemove);
        }
    }
}
