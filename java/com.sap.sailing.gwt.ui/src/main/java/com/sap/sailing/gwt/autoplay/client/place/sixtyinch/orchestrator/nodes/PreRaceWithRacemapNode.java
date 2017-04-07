package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.TimedTransitionSimpleNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.HelperSixty;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.RaceMapHelper;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.RaceMapHelper.RVWrapper;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6.PreRaceRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.LifeRaceWithRacemapPlace;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class PreRaceWithRacemapNode extends TimedTransitionSimpleNode {
    private final AutoPlayClientFactorySixtyInch cf;

    public PreRaceWithRacemapNode(AutoPlayClientFactorySixtyInch cf) {
        super("PreRaceWithRacemapNode", 30000);
        this.cf = cf;

    }

    public void onStart() {
        RaceMapHelper.create(cf.getSailingService(), cf.getErrorReporter(),
                cf.getSlideCtx().getSettings().getLeaderBoardName(), cf.getSlideCtx().getSettings().getEventId(),
                cf.getSlideCtx().getEvent(), cf.getEventBus(), cf.getDispatch(), new AsyncCallback<RVWrapper>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        LifeRaceWithRacemapPlace place = new LifeRaceWithRacemapPlace();
                        place.setError(caught);
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                    }

                    @Override
                    public void onSuccess(RVWrapper result) {
                        cf.getDispatch().execute(
                                new GetMiniLeaderbordAction(cf.getSlideCtx().getEvent().id,
                                        cf.getSlideCtx().getSettings().getLeaderBoardName()),
                                new AsyncCallback<ResultWithTTL<GetMiniLeaderboardDTO>>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        // fireEvent(new DataLoadFailureEvent(MiniLeaderboardLoader.this, caught));
                                        firePlaceChangeAndStartTimer();
                                    }

                                    @Override
                                    public void onSuccess(ResultWithTTL<GetMiniLeaderboardDTO> resultTTL) {
                                        HelperSixty.getLifeRace(cf.getSailingService(), cf.getErrorReporter(),
                                                cf.getSlideCtx().getEvent(),
                                                cf.getSlideCtx().getSettings().getLeaderBoardName(), cf.getDispatch(),
                                                new AsyncCallback<RegattaAndRaceIdentifier>() {

                                                    @Override
                                                    public void onSuccess(RegattaAndRaceIdentifier lifeRace) {
                                                        GetMiniLeaderboardDTO dto = resultTTL.getDto();
                                                        PreRaceRacemapPlace place = new PreRaceRacemapPlace();
                                                        place.setLeaderBoardDTO(dto);
                                                        place.setRaceMap(result.raceboardPerspective, result.csel);
                                                        place.setRace(result.race);
                                                        place.setURL(
                                                                cf.getSlideCtx().getEvent().getOfficialWebsiteURL());
                                                        place.setTime(
                                                                cf.getSlideCtx().getEvent().getCurrentServerTime());
                                                        setPlaceToGo(place);
                                                        firePlaceChangeAndStartTimer();
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable caught) {
                                                        // fireEvent(new
                                                        // DataLoadFailureEvent(MiniLeaderboardLoader.this, caught));
                                                        firePlaceChangeAndStartTimer();
                                                    }
                                                });

                                    }
                                });
                    }
                });
    }
}
