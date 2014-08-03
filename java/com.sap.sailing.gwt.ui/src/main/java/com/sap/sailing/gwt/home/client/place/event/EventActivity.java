package com.sap.sailing.gwt.home.client.place.event;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.ui.regattaoverview.RegattaRaceStatesSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public class EventActivity extends AbstractActivity {
    private final EventClientFactory clientFactory;

    private final EventPlace eventPlace;
    private final Timer timerForClientServerOffset;

    private final long serverUpdateRateInMs = 10000;
    private final Timer serverUpdateTimer;
    private final RegattaRaceStatesSettings raceStatesSettings; 

    private EventView view;
    
    public EventActivity(EventPlace place, EventClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.eventPlace = place;
        
        timerForClientServerOffset = new Timer(PlayModes.Replay);
        serverUpdateTimer = new Timer(PlayModes.Live, serverUpdateRateInMs);
        
        raceStatesSettings = new RegattaRaceStatesSettings();
        serverUpdateTimer.addTimeListener(new TimeListener() {
            
            @Override
            public void timeChanged(Date newTime, Date oldTime) {
                // loadAndUpdateEventRaceStatesLog();
            }
        });
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
        UUID eventUUID = UUID.fromString(eventPlace.getEventUuidAsString());
        clientFactory.getSailingService().getEventById(eventUUID, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                if(event.getLeaderboardGroups().size() > 0) {
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
                            
                            view = clientFactory.createEventView(event, raceGroups, eventPlace.getLeaderboardIdAsNameString(), timerForClientServerOffset);
                            panel.setWidget(view.asWidget());
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Shit happens at getRegattaStructureForEvent()");
                        }
                    });
                } else {
                    // no leaderboard groups defined yet -> show a teaser page
                    EventWithoutRegattasView view = clientFactory.createEventWithoutRegattasView(event);
                    panel.setWidget(view.asWidget());
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Shit happens at getRegattaStructureForEvent()");
            }
        }); 
    }

    protected void loadAndUpdateEventRaceStatesLog() {
        final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
        UUID eventUUID = UUID.fromString(eventPlace.getEventUuidAsString());

        clientFactory.getSailingService().getRaceStateEntriesForRaceGroup(eventUUID, raceStatesSettings.getVisibleCourseAreas(),
                raceStatesSettings.getVisibleRegattas(), raceStatesSettings.isShowOnlyCurrentlyRunningRaces(), raceStatesSettings.isShowOnlyRacesOfSameDay(),
                new MarkedAsyncCallback<List<RegattaOverviewEntryDTO>>(
                        new AsyncCallback<List<RegattaOverviewEntryDTO>>() {
                            @Override
                            public void onFailure(Throwable cause) {
                                Window.alert("Shit happens at getRaceStateEntriesForRaceGroup()");
                            }
                
                            @Override
                            public void onSuccess(List<RegattaOverviewEntryDTO> result) {
                                final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
                                Date serverTimeDuringRequest = null;
                                for (RegattaOverviewEntryDTO entryDTO : result) {
                                    if (entryDTO.currentServerTime != null) {
                                        serverTimeDuringRequest = entryDTO.currentServerTime;
                                    }
                                }
                                view.updateEventRaceStates(result);
                                timerForClientServerOffset.adjustClientServerOffset(clientTimeWhenRequestWasSent, serverTimeDuringRequest, clientTimeWhenResponseWasReceived);
                            }
                        }));
    }

}
