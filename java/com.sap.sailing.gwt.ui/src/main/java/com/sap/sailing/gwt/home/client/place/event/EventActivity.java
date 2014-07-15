package com.sap.sailing.gwt.home.client.place.event;

import java.util.List;
import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public class EventActivity extends AbstractActivity {
    private final EventClientFactory clientFactory;

    private final EventPlace eventPlace;

    private final Timer timerForClientServerOffset;

    public EventActivity(EventPlace place, EventClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.eventPlace = place;
        
        timerForClientServerOffset = new Timer(PlayModes.Replay);
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
        clientFactory.getSailingService().getEventById(UUID.fromString(eventPlace.getEventUuidAsString()), new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                
                clientFactory.getSailingService().getRegattaStructureForEvent(event.id, new AsyncCallback<List<RaceGroupDTO>>() {
                    @Override
                    public void onSuccess(List<RaceGroupDTO> raceGroups) {
                        for(LeaderboardGroupDTO leaderboardGroupDTO: event.getLeaderboardGroups()) {
                            final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
                            if (leaderboardGroupDTO.getAverageDelayToLiveInMillis() != null) {
                                timerForClientServerOffset.setLivePlayDelayInMillis(leaderboardGroupDTO.getAverageDelayToLiveInMillis());
                            }
                            timerForClientServerOffset.adjustClientServerOffset(clientTimeWhenRequestWasSent, leaderboardGroupDTO.getCurrentServerTime(), clientTimeWhenResponseWasReceived);
                        }
                        
                        final EventView view = clientFactory.createEventView(event, raceGroups, eventPlace.getLeaderboardIdAsNameString(), timerForClientServerOffset);
                        panel.setWidget(view.asWidget());
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Shit happens");
                    }
                });
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Shit happens");
            }
        }); 
    }

}
