package com.sap.sailing.gwt.autoplay.client.nodes;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.RaceMapHelper;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.RaceMapHelper.RVWrapper;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.liferaceloop.racemap.LifeRaceWithRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.racemap.PreRaceRacemapPlace;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class PreRaceWithRacemapNode extends FiresPlaceNode {
    private final AutoPlayClientFactorySixtyInch cf;

    public PreRaceWithRacemapNode(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;

    }


    public void onStart() {
        RaceMapHelper.create(cf.getSailingService(), cf.getErrorReporter(),
                cf.getSlideCtx().getSettings().getLeaderBoardName(), cf.getSlideCtx().getSettings().getEventId(),
                cf.getSlideCtx().getEvent(), cf.getEventBus(), cf.getDispatch(),cf.getSlideCtx().getLifeRace(), new AsyncCallback<RVWrapper>() {

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
                                        GetMiniLeaderboardDTO dto = resultTTL.getDto();
                                        PreRaceRacemapPlace place = new PreRaceRacemapPlace();
                                        place.setLeaderBoardDTO(dto);
                                        place.setRaceMap(result.raceboardPerspective, result.csel);
                                        // add later with settings here
                                        place.setURL(cf.getSlideCtx().getEvent().getOfficialWebsiteURL());
                                        setPlaceToGo(place);
                                        firePlaceChangeAndStartTimer();

                                    }
                                });
                    }
                });
    }
}
