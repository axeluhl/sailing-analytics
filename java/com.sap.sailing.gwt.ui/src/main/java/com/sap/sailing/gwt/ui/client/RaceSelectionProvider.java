package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.sap.sailing.domain.common.EventAndRaceIdentifier;

public interface RaceSelectionProvider {
    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<EventAndRaceIdentifier> getSelectedRaces();

    void addRaceSelectionChangeListener(RaceSelectionChangeListener listener);

    void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener);
    
    void setSelection(List<EventAndRaceIdentifier> newSelection, RaceSelectionChangeListener... listenersNotToNotify);

    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<EventAndRaceIdentifier> getAllRaces();
    
    void setAllRaces(List<EventAndRaceIdentifier> newAllRaces, RaceSelectionChangeListener... listenersNotToNotify);
}
