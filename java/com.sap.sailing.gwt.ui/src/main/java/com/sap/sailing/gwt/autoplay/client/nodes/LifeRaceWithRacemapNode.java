package com.sap.sailing.gwt.autoplay.client.nodes;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.liferaceloop.racemapwithleaderboard.LifeRaceWithRacemapAndLeaderBoardPlace;
import com.sap.sailing.gwt.autoplay.client.utils.RaceMapHelper;
import com.sap.sailing.gwt.autoplay.client.utils.RaceMapHelper.RVWrapper;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

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
                        UUID eventId = cf.getSlideCtx().getSettings().getEventId();
                        String leaderBoardName = cf.getSlideCtx().getSettings().getLeaderBoardName();
                        cf.getDispatch().execute(new GetMiniLeaderbordAction(eventId, leaderBoardName),
                                new AsyncCallback<ResultWithTTL<GetMiniLeaderboardDTO>>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        // fireEvent(new DataLoadFailureEvent(MiniLeaderboardLoader.this, caught));
                                        firePlaceChangeAndStartTimer();
                                    }

                                    @Override
                                    public void onSuccess(ResultWithTTL<GetMiniLeaderboardDTO> resultTTL) {
                                        GetMiniLeaderboardDTO dto = resultTTL.getDto();
                                        LifeRaceWithRacemapAndLeaderBoardPlace place = new LifeRaceWithRacemapAndLeaderBoardPlace();
                                        place.setLeaderBoardDTO(dto);
                                        place.setRaceMap(result.raceboardPerspective, result.csel);
                                        setPlaceToGo(place);
                                        firePlaceChangeAndStartTimer();
                                    }
                                });
                    }
                });
    };
}
