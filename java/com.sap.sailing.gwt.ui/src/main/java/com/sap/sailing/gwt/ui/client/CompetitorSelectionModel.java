package com.sap.sailing.gwt.ui.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.ColorMap;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.ColorMapImpl;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public class CompetitorSelectionModel implements CompetitorSelectionProvider {
    private final Set<CompetitorDTO> allCompetitors;
    
    private final LinkedHashSet<CompetitorDTO> selectedCompetitors;
    
    private final Set<CompetitorSelectionChangeListener> listeners;
    
    private final boolean hasMultiSelection;
    
    private final ColorMap<CompetitorDTO> competitorsColorMap;
    
    private FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> competitorsFilterSet; 

    public CompetitorSelectionModel(boolean hasMultiSelection) {
        this(hasMultiSelection, null);
    }

    public CompetitorSelectionModel(boolean hasMultiSelection, FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> competitorsFilterSet) {
        super();
        this.hasMultiSelection = hasMultiSelection;
        this.competitorsFilterSet = competitorsFilterSet;
        this.allCompetitors = new HashSet<CompetitorDTO>();
        this.selectedCompetitors = new LinkedHashSet<CompetitorDTO>();
        this.listeners = new HashSet<CompetitorSelectionChangeListener>();
        this.competitorsColorMap = new ColorMapImpl<CompetitorDTO>();
    }
    
    /**
     * Adds a competitor to the {@link #getAllCompetitors() set of all competitors}. If the competitor was not yet
     * contained, it will be deselected.
     */
    public void add(CompetitorDTO competitor) {
        add(competitor, true);
    }
    
    private void add(CompetitorDTO competitor, boolean notifyListeners) {
        if (competitor.getColor() != null) {
            Color color = competitor.getColor();
            competitorsColorMap.addBlockedColor(color);
        }
        boolean changed = allCompetitors.add(competitor);
        if (notifyListeners && changed) {
            fireListChanged(getAllCompetitors());
        }
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
        Iterator<CompetitorDTO> selIter = getSelectedCompetitors().iterator();
        while (selIter.hasNext()) {
            CompetitorDTO selected = selIter.next();
            setSelected(selected, false);
            selIter = getSelectedCompetitors().iterator();
        }
        assert selectedCompetitors.isEmpty();
        allCompetitors.clear();
        fireListChanged(getAllCompetitors());
    }
    
    public void addAll(Iterable<CompetitorDTO> competitors) {
        boolean changed = false;
        for (CompetitorDTO competitor : competitors) {
            add(competitor, false);
            changed = true;
        }
        if (changed) {
            fireListChanged(getAllCompetitors());
        }
    }

    /**
     * Removes the competitor from the {@link #getAllCompetitors() set of all competitor}. If the competitor was previously
     * contained and selected, it is deselected first.
     */
    public void remove(CompetitorDTO competitor) {
        if (isSelected(competitor)) {
            setSelected(competitor, false);
        }
        boolean changed = allCompetitors.remove(competitor);
        if (changed) {
            fireListChanged(getAllCompetitors());
        }
    }
    
    @Override
    public Iterable<CompetitorDTO> getSelectedCompetitors() {
        return Collections.unmodifiableCollection(selectedCompetitors);
    }

    @Override
    public Iterable<CompetitorDTO> getAllCompetitors() {
        return Collections.unmodifiableCollection(allCompetitors);
    }
    
    @Override
    public Collection<CompetitorDTO> getFilteredCompetitors() {
        Set<CompetitorDTO> currentFilteredList = new LinkedHashSet<CompetitorDTO>(allCompetitors);
        if (competitorsFilterSet != null) {
            for (Filter<CompetitorDTO> filter : competitorsFilterSet.getFilters()) {
                for (Iterator<CompetitorDTO> i=currentFilteredList.iterator(); i.hasNext(); ) {
                    CompetitorDTO competitorDTO = i.next();
                    if (!filter.matches(competitorDTO)) {
                        i.remove();
                    }
                }
            }
        }
        return currentFilteredList;
    }
    
    public void setSelected(CompetitorDTO competitor, boolean selected, CompetitorSelectionChangeListener... listenersNotToNotify) {
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
    
    private void fireAddedToSelection(CompetitorDTO competitor, CompetitorSelectionChangeListener... listenersNotToNotify) {
        for (CompetitorSelectionChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.addedToSelection(competitor);
            }
        }
    }
    
    private void fireRemovedFromSelection(CompetitorDTO competitor, CompetitorSelectionChangeListener... listenersNotToNotify) {
        for (CompetitorSelectionChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.removedFromSelection(competitor);
            }
        }
    }
    
    private void fireListChanged(Iterable<CompetitorDTO> competitors, CompetitorSelectionChangeListener... listenersNotToNotify) {
        for (CompetitorSelectionChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.competitorsListChanged(competitors);
            }
        }
    }

    @Override
    public boolean isSelected(CompetitorDTO competitor) {
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
    public void setSelection(Iterable<CompetitorDTO> newSelection, CompetitorSelectionChangeListener... listenersNotToNotify) {
        Set<CompetitorDTO> competitorsToRemoveFromSelection = new HashSet<CompetitorDTO>(selectedCompetitors);
        for (CompetitorDTO newSelectedCompetitor : newSelection) {
            setSelected(newSelectedCompetitor, true, listenersNotToNotify);
            competitorsToRemoveFromSelection.remove(newSelectedCompetitor);
        }
        for (CompetitorDTO competitorToRemoveFromSelection : competitorsToRemoveFromSelection) {
            setSelected(competitorToRemoveFromSelection, false, listenersNotToNotify);
        }
    }

    @Override
    public void setCompetitors(Iterable<CompetitorDTO> newCompetitors, CompetitorSelectionChangeListener... listenersNotToNotify) {
        boolean changed = false;
        
        Set<CompetitorDTO> oldCompetitorsToRemove = new HashSet<CompetitorDTO>(allCompetitors);
        for (CompetitorDTO newCompetitor : newCompetitors) {
            if (allCompetitors.contains(newCompetitor)) {
                oldCompetitorsToRemove.remove(newCompetitor);
            } else {
                add(newCompetitor, false);
                changed = true;
            }
        }
        for (CompetitorDTO oldCompetitorToRemove : oldCompetitorsToRemove) {
            remove(oldCompetitorToRemove);
            changed = true;
        }
        
        if (changed) {
            fireListChanged(getAllCompetitors(), listenersNotToNotify);
        }
    }
    
    @Override
    public Color getColor(CompetitorDTO competitor) {
        Color result = null;
        if (allCompetitors.contains(competitor)) {
            if (competitor.getColor() != null) {
                result = competitor.getColor();
            } else {
                result = competitorsColorMap.getColorByID(competitor); 
            }
        }
        
        return result;
    }

    @Override
    public FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> getCompetitorsFilterSet() {
        return competitorsFilterSet;
    }

    @Override
    public void setCompetitorsFilterSet(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> competitorsFilterSet) {
        this.competitorsFilterSet = competitorsFilterSet;
        for (CompetitorSelectionChangeListener listener : listeners) {
            listener.filteredCompetitorsListChanged(getFilteredCompetitors());
        }
    }
}
