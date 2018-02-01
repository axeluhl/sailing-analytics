package com.sap.sailing.gwt.autoplay.client.nodes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.FiresPlaceNode;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard.LiveRaceWithRacemapAndLeaderBoardPlace;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper;
import com.sap.sailing.gwt.autoplay.client.utils.AutoplayHelper.RVWrapper;
import com.sap.sailing.gwt.home.communication.event.sixtyinch.GetSixtyInchStatisticAction;
import com.sap.sailing.gwt.home.communication.event.sixtyinch.GetSixtyInchStatisticDTO;

public class LiveRaceWithRacemapNode extends FiresPlaceNode {
    private final AutoPlayClientFactory cf;
    protected LiveRaceWithRacemapAndLeaderBoardPlace place;
    private Timer updateTimer;

    public LiveRaceWithRacemapNode(AutoPlayClientFactory cf) {
        super(LiveRaceWithRacemapNode.class.getName());
        this.cf = cf;

    }
    
    @Override
    public void onStop() {
        super.onStop();
        if(updateTimer != null){
            updateTimer.cancel();
        }
    }

    public void onStart() {
        place = new LiveRaceWithRacemapAndLeaderBoardPlace();
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
                        place.setRaceMap(result.raceboardPerspective, result.csel,result.raceboardTimer,result.creationTimeProvider);
                        setPlaceToGo(place);
                        firePlaceChangeAndStartTimer();
                        getBus().fireEvent(new AutoPlayHeaderEvent(cf.getAutoPlayCtx().getLiveRace().getRegattaName(),
                                cf.getAutoPlayCtx().getLiveRace().getRaceName()));
                    }
                });
        
      
        
        updateTimer = new Timer(){

            @Override
            public void run() {
                cf.getDispatch().execute(new GetSixtyInchStatisticAction(cf.getAutoPlayCtx().getLiveRace().getRaceName(), cf.getAutoPlayCtx().getLiveRace().getRegattaName()),
                        new AsyncCallback<GetSixtyInchStatisticDTO>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                GWT.log("error getting data! " + caught.getMessage());
                                caught.printStackTrace();
                            }

                            @Override
                            public void onSuccess(GetSixtyInchStatisticDTO result) {
                                place.setStatistic(result);
                            }
                        });
            }
            
        };
        updateTimer.scheduleRepeating(2000);
    };
}
