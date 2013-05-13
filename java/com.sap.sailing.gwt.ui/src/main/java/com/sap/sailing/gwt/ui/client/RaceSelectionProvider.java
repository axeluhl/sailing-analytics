package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public interface RaceSelectionProvider {
    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<RegattaAndRaceIdentifier> getSelectedRaces();

    void addRaceSelectionChangeListener(RaceSelectionChangeListener listener);

    void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener);
    
    void setSelection(List<RegattaAndRaceIdentifier> newSelection, RaceSelectionChangeListener... listenersNotToNotify);

    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<RegattaAndRaceIdentifier> getAllRaces();
    
    void setAllRaces(List<RegattaAndRaceIdentifier> newAllRaces, RaceSelectionChangeListener... listenersNotToNotify);
}
