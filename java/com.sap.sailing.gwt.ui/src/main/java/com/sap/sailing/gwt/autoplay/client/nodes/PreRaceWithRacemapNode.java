package com.sap.sailing.gwt.autoplay.client.nodes;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.liferaceloop.racemapwithleaderboard.LifeRaceWithRacemapAndLeaderBoardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.PreRaceRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.utils.RaceMapHelper;
import com.sap.sailing.gwt.autoplay.client.utils.RaceMapHelper.RVWrapper;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderbordAction;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

public class PreRaceWithRacemapNode extends FiresPlaceNode {
    private final AutoPlayClientFactory cf;

    public PreRaceWithRacemapNode(AutoPlayClientFactory cf) {
        this.cf = cf;

    }


    public void onStart() {
        RaceMapHelper.create(cf.getSailingService(), cf.getErrorReporter(),
                cf.getSlideCtx().getSettings().getLeaderboardName(), cf.getSlideCtx().getSettings().getEventId(),
                cf.getSlideCtx().getEvent(), cf.getEventBus(), cf.getDispatch(),cf.getSlideCtx().getLifeRace(), new AsyncCallback<RVWrapper>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        LifeRaceWithRacemapAndLeaderBoardPlace place = new LifeRaceWithRacemapAndLeaderBoardPlace();
                        place.setError(caught);
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                    }

                    @Override
                    public void onSuccess(RVWrapper result) {
                        cf.getDispatch().execute(
                                new GetMiniLeaderbordAction(cf.getSlideCtx().getEvent().id,
                                        cf.getSlideCtx().getSettings().getLeaderboardName()),
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
