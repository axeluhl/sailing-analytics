package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sap.sailing.gwt.ui.adminconsole.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.shared.RaceDTO;

public class RaceSelectionModel implements RaceSelectionProvider {
    private final List<RaceDTO> selection;
    
    private final List<RaceDTO> allRaces;
    
    private final Set<RaceSelectionChangeListener> listeners;
    
    public RaceSelectionModel() {
        this.selection = new ArrayList<RaceDTO>();
        this.allRaces = new ArrayList<RaceDTO>();
        listeners = new HashSet<RaceSelectionChangeListener>();
    }

    @Override
    public List<RaceDTO> getSelectedRaces() {
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
    public void setSelection(List<RaceDTO> newSelection, RaceSelectionChangeListener... listenersNotToNotify) {
        boolean notify = selection.equals(newSelection);
        selection.clear();
        selection.addAll(newSelection);
        if (notify) {
            notifyListeners(listenersNotToNotify);
        }
    }

    private void notifyListeners(RaceSelectionChangeListener[] listenersNotToNotify) {
        List<RaceDTO> selectedRaces = getSelectedRaces();
        for (RaceSelectionChangeListener listener : listeners) {
            listener.onRaceSelectionChange(selectedRaces);
        }
    }
    
    /**
     * Sets the universe of all races from which this selection model may select. An equal list will subsequently be
     * returned by {@link #getAllRaces()}. Races from the {@link #getSelectedRaces() selection} not in
     * <code>newAllRaces</code> are removed from the selection. If this happens, the selection listeners are notified.
     */
    @Override
    public void setAllRaces(List<RaceDTO> newAllRaces, RaceSelectionChangeListener... listenersNotToNotify) {
        allRaces.clear();
        for (RaceDTO r : newAllRaces) {
            allRaces.add(r);
        }
        boolean notify = false;
        for (Iterator<RaceDTO> i=selection.iterator(); i.hasNext(); ) {
            RaceDTO selectedRace = i.next();
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
    public List<RaceDTO> getAllRaces() {
        return Collections.unmodifiableList(allRaces);
    }

}
