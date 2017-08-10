package com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.communication.event.sixtyinch.GetSixtyInchStatisticDTO;

public abstract class AbstractRaceEndWithImagesTop3Place extends Place {

    private GetSixtyInchStatisticDTO statistic;
    private RegattaAndRaceIdentifier lifeRace;

    public RegattaAndRaceIdentifier getLastRace() {
        return lifeRace;
    }

    public void setLifeRace(RegattaAndRaceIdentifier lifeRace) {
        this.lifeRace = lifeRace;
    }
    

    public void setStatistic(GetSixtyInchStatisticDTO result) {
        this.statistic = result;
    }
    
    public GetSixtyInchStatisticDTO getStatistic() {
        return statistic;
    }
}
