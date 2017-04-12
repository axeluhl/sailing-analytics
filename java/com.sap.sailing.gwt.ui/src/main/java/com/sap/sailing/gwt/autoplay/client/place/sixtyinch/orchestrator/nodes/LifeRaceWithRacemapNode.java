package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes;

import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.TimedTransitionSimpleNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.RaceMapHelper;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.RaceMapHelper.RVWrapper;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.LifeRaceWithRacemapPlace;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class LifeRaceWithRacemapNode extends TimedTransitionSimpleNode {
    private final AutoPlayClientFactorySixtyInch cf;

    public LifeRaceWithRacemapNode(AutoPlayClientFactorySixtyInch cf) {
        super("slide7", 30000);
        this.cf = cf;

    }

    public void onStart() {
        RaceMapHelper.create(cf.getSailingService(), cf.getErrorReporter(),
                cf.getSlideCtx().getSettings().getLeaderBoardName(), cf.getSlideCtx().getSettings().getEventId(),
                cf.getSlideCtx().getEvent(), cf.getEventBus(), cf.getDispatch(), cf.getSlideCtx().getLifeRace(),
                new AsyncCallback<RVWrapper>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        LifeRaceWithRacemapPlace place = new LifeRaceWithRacemapPlace();
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
                                        LifeRaceWithRacemapPlace place = new LifeRaceWithRacemapPlace();
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
