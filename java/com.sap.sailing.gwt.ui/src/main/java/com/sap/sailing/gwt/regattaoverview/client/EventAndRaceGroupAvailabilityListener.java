package com.sap.sailing.gwt.regattaoverview.client;

import java.util.List;

import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;

public interface EventAndRaceGroupAvailabilityListener {
    void onEventUpdated(EventDTO event);
    void onRaceGroupsUpdated(List<RaceGroupDTO> raceGroups);
}
