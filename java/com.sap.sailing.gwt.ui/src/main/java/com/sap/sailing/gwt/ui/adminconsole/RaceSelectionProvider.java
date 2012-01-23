package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.shared.RaceDTO;

public interface RaceSelectionProvider {
    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<RaceDTO> getSelectedRaces();

    void addRaceSelectionChangeListener(RaceSelectionChangeListener listener);

    void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener);
    
    void setSelection(List<RaceDTO> newSelection, RaceSelectionChangeListener... listenersNotToNotify);

    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<RaceDTO> getAllRaces();
    
    void setAllRaces(List<RaceDTO> newAllRaces, RaceSelectionChangeListener... listenersNotToNotify);
}
