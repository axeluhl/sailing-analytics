package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RegattaOverviewRaceSelectionModel implements RegattaOverviewRaceSelectionProvider {

    private final List<RegattaOverviewEntryDTO> selection;
    
    private final List<RegattaOverviewEntryDTO> allRegattaOverviewRaces;
    
    private final Set<RegattaOverviewRaceSelectionChangeListener> listeners;
    
    private final boolean hasMultiSelection;
    
    public RegattaOverviewRaceSelectionModel(boolean hasMultiSelection) {
        this.hasMultiSelection = hasMultiSelection;
        this.selection = new ArrayList<RegattaOverviewEntryDTO>();
        this.allRegattaOverviewRaces = new ArrayList<RegattaOverviewEntryDTO>();
        listeners = new HashSet<RegattaOverviewRaceSelectionChangeListener>();
    }
    
    @Override
    public List<RegattaOverviewEntryDTO> getSelectedEntries() {
        return Collections.unmodifiableList(selection);
    }

    @Override
    public void addRegattaOverviewRaceSelectionChangeListener(RegattaOverviewRaceSelectionChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeRegattaOverviewRaceSelectionChangeListener(RegattaOverviewRaceSelectionChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setSelection(List<RegattaOverviewEntryDTO> newSelection, RegattaOverviewRaceSelectionChangeListener... listenersNotToNotify) {
        boolean notify = !selection.equals(newSelection);
        selection.clear();
        selection.addAll(newSelection);
        if (notify) {
            notifyListeners(listenersNotToNotify);
        }
    }
    
    private void notifyListeners(RegattaOverviewRaceSelectionChangeListener[] listenersNotToNotify) {
        List<RegattaOverviewEntryDTO> selectedRegattaOverviewEntries = getSelectedEntries();
        for (RegattaOverviewRaceSelectionChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.onRegattaOverviewEntrySelectionChange(selectedRegattaOverviewEntries);
            }
        }
    }

    @Override
    public boolean hasMultiSelection() {
        return hasMultiSelection;
    }

    @Override
    public List<RegattaOverviewEntryDTO> getAllRegattaOverviewEntries() {
        return Collections.unmodifiableList(allRegattaOverviewRaces);
    }

    @Override
    public void setAllRegattaOverviewEntries(List<RegattaOverviewEntryDTO> newAllRegattaOverviewEntries, 
            RegattaOverviewRaceSelectionChangeListener... listenersNotToNotify) {
        allRegattaOverviewRaces.clear();
        for (RegattaOverviewEntryDTO r : newAllRegattaOverviewEntries) {
            allRegattaOverviewRaces.add(r);
        }
        for (Iterator<RegattaOverviewEntryDTO> i=selection.iterator(); i.hasNext(); ) {
            RegattaOverviewEntryDTO selectedRegattaOverviewEntry = i.next();
            if (!allRegattaOverviewRaces.contains(selectedRegattaOverviewEntry)) {
                i.remove();
            }
        }
        // when setting all regatta overview entries, the actual RegattaOverviewEntryDTOs will usually have changed their identity and maybe also their state;
        // so notifying the selection listeners is necessary anyhow
        notifyListeners(listenersNotToNotify);
    }

}
