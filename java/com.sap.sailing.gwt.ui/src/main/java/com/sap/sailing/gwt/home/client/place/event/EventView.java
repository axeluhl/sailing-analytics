package com.sap.sailing.gwt.home.client.place.event;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public interface EventView {
    Widget asWidget();
    
    void updateEventRaceStates(List<RegattaOverviewEntryDTO> racesStateEntries);
}
