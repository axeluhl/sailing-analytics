package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.sap.sailing.domain.common.Util.Triple;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;

public interface RaceSelectionProvider {
    /**
     * @return a non-<code>null</code> list which may be empty
     */
    List<Triple<EventDAO, RegattaDAO, RaceDAO>> getSelectedEventAndRace();

    void addRaceSelectionChangeListener(RaceSelectionChangeListener listener);

    void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener);
}
