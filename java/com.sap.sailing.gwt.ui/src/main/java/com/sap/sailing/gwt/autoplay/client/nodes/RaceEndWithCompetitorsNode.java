package com.sap.sailing.gwt.autoplay.client.nodes;

import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats.RaceEndWithCompetitorsTop3Place;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RaceEndWithCompetitorsNode extends FiresPlaceNode {
    private final AutoPlayClientFactory cf;

    public RaceEndWithCompetitorsNode(AutoPlayClientFactory cf) {
        this.cf = cf;
    }

    public void onStart() {
        RaceEndWithCompetitorsTop3Place place = new RaceEndWithCompetitorsTop3Place();

        
        place.setLifeRace(cf.getAutoPlayCtx().getLastRace());
        setPlaceToGo(place);
        firePlaceChangeAndStartTimer();
        getBus().fireEvent(new AutoPlayHeaderEvent(cf.getAutoPlayCtx().getLastRace().getRegattaName(),
                StringMessages.INSTANCE.results() + " " + cf.getAutoPlayCtx().getLastRace().getRaceName()));
    };
}
