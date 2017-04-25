package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface AutoPlayContext {
    AutoPlaySettings getSettings();

    void updateEvent(EventDTO event);

    EventDTO getEvent();

    public RegattaAndRaceIdentifier getLifeRace();

    public RegattaAndRaceIdentifier getLastRace();

    public void setCurrenLifeRace(RegattaAndRaceIdentifier lifeRace);
}
