package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;

public interface RaceSelectionProvider {
    List<Triple<EventDAO, RegattaDAO, RaceDAO>> getSelectedEventAndRace();
    void addRaceSelectionChangeListener(RaceSelectionChangeListener listener);
    void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener);
}
