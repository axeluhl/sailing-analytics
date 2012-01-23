package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.server.api.RaceIdentifier;

public interface RaceSelectionProvider {
    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<RaceIdentifier> getSelectedRaces();

    void addRaceSelectionChangeListener(RaceSelectionChangeListener listener);

    void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener);
    
    void setSelection(List<RaceIdentifier> newSelection, RaceSelectionChangeListener... listenersNotToNotify);

    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<RaceIdentifier> getAllRaces();
    
    void setAllRaces(List<RaceIdentifier> newAllRaces, RaceSelectionChangeListener... listenersNotToNotify);
}
