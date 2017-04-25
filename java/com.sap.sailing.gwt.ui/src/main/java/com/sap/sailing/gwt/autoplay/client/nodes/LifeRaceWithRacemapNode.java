package com.sap.sailing.gwt.autoplay.client.nodes;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.liferaceloop.racemapwithleaderboard.LifeRaceWithRacemapAndLeaderBoardPlace;
import com.sap.sailing.gwt.autoplay.client.utils.RaceMapHelper;
import com.sap.sailing.gwt.autoplay.client.utils.RaceMapHelper.RVWrapper;

public class LifeRaceWithRacemapNode extends FiresPlaceNode {
    private final AutoPlayClientFactorySixtyInch cf;

    public LifeRaceWithRacemapNode(AutoPlayClientFactorySixtyInch cf) {

        this.cf = cf;

    }

    public void onStart() {
        RaceMapHelper.create(cf.getSailingService(), cf.getErrorReporter(),
                cf.getSlideCtx().getSettings().getLeaderBoardName(), cf.getSlideCtx().getSettings().getEventId(),
                cf.getSlideCtx().getEvent(), cf.getEventBus(), cf.getDispatch(), cf.getSlideCtx().getLifeRace(),
                new AsyncCallback<RVWrapper>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        LifeRaceWithRacemapAndLeaderBoardPlace place = new LifeRaceWithRacemapAndLeaderBoardPlace();
                        place.setError(caught);
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                    }

                    @Override
                    public void onSuccess(RVWrapper result) {
                        LifeRaceWithRacemapAndLeaderBoardPlace place = new LifeRaceWithRacemapAndLeaderBoardPlace();
                        place.setRaceMap(result.raceboardPerspective, result.csel);
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                    }
                });
    };
}
