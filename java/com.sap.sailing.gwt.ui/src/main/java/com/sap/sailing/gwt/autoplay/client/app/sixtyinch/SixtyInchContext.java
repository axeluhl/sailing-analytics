package com.sap.sailing.gwt.autoplay.client.app.sixtyinch;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.initial.SixtyInchSetting;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface SixtyInchContext {
    SixtyInchSetting getSettings();

    void updateEvent(EventDTO event);

    EventDTO getEvent();

    public RegattaAndRaceIdentifier getLifeRace();

    public RegattaAndRaceIdentifier getLastRace();

    public void setCurrenLifeRace(RegattaAndRaceIdentifier lifeRace);
}
