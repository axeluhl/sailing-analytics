package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.RaceDAO;

public interface RaceSelectionProvider {
    Pair<EventDAO, RaceDAO> getSelectedEventAndRace();
}
