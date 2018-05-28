package com.sap.sailing.gwt.autoplay.client.nodes;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.flags.RaceEndWithCompetitorFlagsPlace;
import com.sap.sailing.gwt.home.communication.event.sixtyinch.GetSixtyInchStatisticAction;
import com.sap.sailing.gwt.home.communication.event.sixtyinch.GetSixtyInchStatisticDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RaceEndWithCompetitorsFlagsNode extends FiresPlaceNode {
    protected static final Logger LOGGER = Logger.getLogger(RaceEndWithCompetitorsFlagsNode.class.getName());
    private final AutoPlayClientFactory cf;

    public RaceEndWithCompetitorsFlagsNode(AutoPlayClientFactory cf) {
        super(RaceEndWithCompetitorsFlagsNode.class.getName());
        this.cf = cf;
    }

    public void onStart() {
        RaceEndWithCompetitorFlagsPlace place = new RaceEndWithCompetitorFlagsPlace();

        RegattaAndRaceIdentifier lastRace = cf.getAutoPlayCtxSignalError().getLastRace();
        place.setLifeRace(lastRace);
        setPlaceToGo(place);

        cf.getDispatch().execute(new GetSixtyInchStatisticAction(lastRace.getRaceName(), lastRace.getRegattaName()),
                new AsyncCallback<GetSixtyInchStatisticDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        LOGGER.log(Level.WARNING, "Could not get statistics" , caught);
                    }

                    @Override
                    public void onSuccess(GetSixtyInchStatisticDTO result) {
                        place.setStatistic(result);
                        getBus().fireEvent(new AutoPlayHeaderEvent(lastRace.getRegattaName(),
                                StringMessages.INSTANCE.results() + " " + lastRace.getRaceName()));
                        firePlaceChangeAndStartTimer();
                    }
                });
    };
}
