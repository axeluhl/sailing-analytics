package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.places.startsixtyinch.SixtyInchSetting;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface SixtyInchContext {
    SixtyInchSetting getSettings();

    void updateEvent(EventDTO event);

    EventDTO getEvent();

    public RegattaAndRaceIdentifier getLifeRace();

    public void setCurrenLifeRace(RegattaAndRaceIdentifier lifeRace);
}
