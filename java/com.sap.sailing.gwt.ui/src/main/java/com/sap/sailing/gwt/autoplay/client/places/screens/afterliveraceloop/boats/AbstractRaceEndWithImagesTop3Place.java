package com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public abstract class AbstractRaceEndWithImagesTop3Place extends Place {

    private RegattaAndRaceIdentifier lifeRace;

    public RegattaAndRaceIdentifier getLastRace() {
        return lifeRace;
    }

    public void setLifeRace(RegattaAndRaceIdentifier lifeRace) {
        this.lifeRace = lifeRace;
    }
}
