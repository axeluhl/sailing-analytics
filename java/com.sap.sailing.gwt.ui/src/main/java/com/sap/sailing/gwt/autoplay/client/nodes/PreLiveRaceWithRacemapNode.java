package com.sap.sailing.gwt.autoplay.client.nodes;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard.LiveRaceWithRacemapAndLeaderBoardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.PreRaceRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper.RVWrapper;

public class PreLiveRaceWithRacemapNode extends FiresPlaceNode {
    private final AutoPlayClientFactory cf;

    public PreLiveRaceWithRacemapNode(AutoPlayClientFactory cf) {
        super(PreLiveRaceWithRacemapNode.class.getName());
        this.cf = cf;

    }

    public void onStart() {
        AutoplayHelper.create(cf.getSailingService(), cf.getErrorReporter(),
                cf.getAutoPlayCtxSignalError().getContextDefinition().getLeaderboardName(),
                cf.getAutoPlayCtxSignalError().getContextDefinition().getEventId(), cf.getAutoPlayCtxSignalError().getEvent(),
                cf.getEventBus(), cf.getDispatch(), cf.getAutoPlayCtxSignalError().getPreLiveRace(),
                new AsyncCallback<RVWrapper>() {

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
                        PreRaceRacemapPlace place = new PreRaceRacemapPlace();
                        place.setRaceMap(result.raceboardPerspective, result.csel, result.raceboardTimer,
                                result.creationTimeProvider);
                        // add later with settings here
                        place.setURL(cf.getAutoPlayCtxSignalError().getEvent().getOfficialWebsiteURL());
                        setPlaceToGo(place);
                        getBus().fireEvent(
                                new AutoPlayHeaderEvent(cf.getAutoPlayCtxSignalError().getPreLiveRace().getRegattaName(),
                                        cf.getAutoPlayCtxSignalError().getPreLiveRace().getRaceName()));
                        firePlaceChangeAndStartTimer();
                    }
                });
    }
}
