package com.sap.sailing.gwt.autoplay.client.nodes;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard.LiveRaceWithRacemapAndLeaderBoardPlace;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper.RVWrapper;

public class LiveRaceWithRacemapNode extends FiresPlaceNode {
    private final AutoPlayClientFactory cf;

    public LiveRaceWithRacemapNode(AutoPlayClientFactory cf) {
        super(LiveRaceWithRacemapNode.class.getName());
        this.cf = cf;

    }

    public void onStart() {
        AutoplayHelper.create(cf.getSailingService(), cf.getErrorReporter(),
                cf.getAutoPlayCtx().getContextDefinition().getLeaderboardName(), cf.getAutoPlayCtx().getContextDefinition().getEventId(),
                cf.getAutoPlayCtx().getEvent(), cf.getEventBus(), cf.getDispatch(), cf.getAutoPlayCtx().getLiveRace(),
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
                        LiveRaceWithRacemapAndLeaderBoardPlace place = new LiveRaceWithRacemapAndLeaderBoardPlace();
                        place.setRaceMap(result.raceboardPerspective, result.csel);
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                        getBus().fireEvent(new AutoPlayHeaderEvent(cf.getAutoPlayCtx().getLiveRace().getRegattaName(),
                                cf.getAutoPlayCtx().getLiveRace().getRaceName()));
                    }
                });
    };
}
