package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sap.sailing.server.api.RaceIdentifier;

public class RaceSelectionModel implements RaceSelectionProvider {
    private final List<RaceIdentifier> selection;
    
    private final List<RaceIdentifier> allRaces;
    
    private final Set<RaceSelectionChangeListener> listeners;
    
    public RaceSelectionModel() {
        this.selection = new ArrayList<RaceIdentifier>();
        this.allRaces = new ArrayList<RaceIdentifier>();
        listeners = new HashSet<RaceSelectionChangeListener>();
    }

    @Override
    public List<RaceIdentifier> getSelectedRaces() {
        return Collections.unmodifiableList(selection);
    }

    @Override
    public void addRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void setSelection(List<RaceIdentifier> newSelection, RaceSelectionChangeListener... listenersNotToNotify) {
        boolean notify = selection.equals(newSelection);
        selection.clear();
        selection.addAll(newSelection);
        if (notify) {
            notifyListeners(listenersNotToNotify);
        }
    }

    private void notifyListeners(RaceSelectionChangeListener[] listenersNotToNotify) {
        List<RaceIdentifier> selectedRaces = getSelectedRaces();
        for (RaceSelectionChangeListener listener : listeners) {
            if (listenersNotToNotify == null || !Arrays.asList(listenersNotToNotify).contains(listener)) {
                listener.onRaceSelectionChange(selectedRaces);
            }
        }
    }
    
    /**
     * Sets the universe of all races from which this selection model may select. An equal list will subsequently be
     * returned by {@link #getAllRaces()}. Races from the {@link #getSelectedRaces() selection} not in
     * <code>newAllRaces</code> are removed from the selection. If this happens, the selection listeners are notified.
     */
    @Override
    public void setAllRaces(List<RaceIdentifier> newAllRaces, RaceSelectionChangeListener... listenersNotToNotify) {
        allRaces.clear();
        for (RaceIdentifier r : newAllRaces) {
            allRaces.add(r);
        }
        boolean notify = false;
        for (Iterator<RaceIdentifier> i=selection.iterator(); i.hasNext(); ) {
            RaceIdentifier selectedRace = i.next();
            if (!allRaces.contains(selectedRace)) {
                notify = true;
                i.remove();
            }
        }
        if (notify) {
            notifyListeners(listenersNotToNotify);
        }
    }

    @Override
    public List<RaceIdentifier> getAllRaces() {
        return Collections.unmodifiableList(allRaces);
    }

}
