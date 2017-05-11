package com.sap.sailing.gwt.autoplay.client.nodes;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard.LiveRaceWithRacemapAndLeaderBoardPlace;
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
                cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName(), cf.getAutoPlayCtx().getContextDefinition().getEventId(),
                cf.getAutoPlayCtx().getEvent(), cf.getEventBus(), cf.getDispatch(),cf.getAutoPlayCtx().getLifeRace(), new AsyncCallback<RVWrapper>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        LiveRaceWithRacemapAndLeaderBoardPlace place = new LiveRaceWithRacemapAndLeaderBoardPlace();
                        place.setError(caught);
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                        getBus().fireEvent(new AutoPlayHeaderEvent("", ""));
                    }

                    @Override
                    public void onSuccess(RVWrapper result) {
                        cf.getDispatch().execute(
                                new GetMiniLeaderbordAction(cf.getAutoPlayCtx().getEvent().id,
                                        cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName()),
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
                                        place.setURL(cf.getAutoPlayCtx().getEvent().getOfficialWebsiteURL());
                                        setPlaceToGo(place);
                                        getBus().fireEvent(
                                                new AutoPlayHeaderEvent(cf.getAutoPlayCtx().getLifeRace().getRegattaName(),
                                                        cf.getAutoPlayCtx().getLifeRace().getRaceName()));
                                        firePlaceChangeAndStartTimer();

                                    }
                                });
                    }
                });
    }
}
