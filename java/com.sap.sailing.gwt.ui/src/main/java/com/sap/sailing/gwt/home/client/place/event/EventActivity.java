package com.sap.sailing.gwt.home.client.place.event;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sailing.gwt.ui.regattaoverview.RegattaRaceStatesSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.mvp.ErrorView;
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
    private EventDTO event;
    
    public EventActivity(EventPlace place, EventClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        this.eventPlace = place;
        
        this.view = null;
        this.event = null;
        
        timerForClientServerOffset = new Timer(PlayModes.Replay);
        serverUpdateTimer = new Timer(PlayModes.Live, serverUpdateRateInMs);
        
        raceStatesSettings = new RegattaRaceStatesSettings();
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        panel.setWidget(new Placeholder());

        final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
        UUID eventUUID = UUID.fromString(eventPlace.getEventUuidAsString());
        clientFactory.getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(final EventDTO event) {
                loadRegattaStructureAndCreateEventView(panel, clientTimeWhenRequestWasSent, event);
            }

            @Override
            public void onFailure(Throwable caught) {
                createErrorView("Error while loading the event with service getEventById()", caught, panel);
            }
        }); 
    }

    private void loadRegattaStructureAndCreateEventView(final AcceptsOneWidget panel, final long clientTimeWhenRequestWasSent, final EventDTO event) {
        this.event = event;

        if(event.getLeaderboardGroups().size() > 0) {
            clientFactory.getSailingService().getRegattaStructureOfEvent(event.id, new AsyncCallback<List<RaceGroupDTO>>() {
                @Override
                public void onSuccess(List<RaceGroupDTO> raceGroups) {
                    if(raceGroups.size() > 0) {
                        for(LeaderboardGroupDTO leaderboardGroupDTO: event.getLeaderboardGroups()) {
                            final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
                            if (leaderboardGroupDTO.getAverageDelayToLiveInMillis() != null) {
                                timerForClientServerOffset.setLivePlayDelayInMillis(leaderboardGroupDTO.getAverageDelayToLiveInMillis());
                            }
                            timerForClientServerOffset.adjustClientServerOffset(clientTimeWhenRequestWasSent, leaderboardGroupDTO.getCurrentServerTime(), clientTimeWhenResponseWasReceived);
                        }
                        createEventView(event, raceGroups, panel);

                        if(event.isRunning()) {
                            // create update time for race states only for running events
                            serverUpdateTimer.addTimeListener(new TimeListener() {
                                @Override
                                public void timeChanged(Date newTime, Date oldTime) {
                                    loadAndUpdateEventRaceStatesLog();
                                }
                            });
                        }

                    } else {
                        createEventWithoutRegattasView(event, panel);
                    }
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    createErrorView("Error while loading the regatta structure with service getRegattaStructureOfEvent()", caught, panel);
                }
            });
        } else {
            createEventWithoutRegattasView(event, panel);
        }
    }
    
    private void createErrorView(String errorMessage, Throwable errorReason, AcceptsOneWidget panel) {
        ErrorView view = clientFactory.createErrorView(errorMessage, errorReason);
        panel.setWidget(view.asWidget());
    }
    
    private void createEventView(EventDTO event, List<RaceGroupDTO> raceGroups, AcceptsOneWidget panel) {
        view = clientFactory.createEventView(event, eventPlace.getNavigationTab(), raceGroups, eventPlace.getLeaderboardIdAsNameString(), timerForClientServerOffset);
        panel.setWidget(view.asWidget());
        Window.setTitle(eventPlace.getTitle(event.getName()));
    }

    private void createEventWithoutRegattasView(EventDTO event, AcceptsOneWidget panel) {
        // no leaderboard groups defined yet -> show a teaser page
        EventWithoutRegattasView view = clientFactory.createEventWithoutRegattasView(event);
        panel.setWidget(view.asWidget());
        Window.setTitle(eventPlace.getTitle(event.getName()));
    }
    
    protected void loadAndUpdateEventRaceStatesLog() {
        if(view != null && event != null) {
            final long clientTimeWhenRequestWasSent = System.currentTimeMillis();

            clientFactory.getSailingService().getRaceStateEntriesForRaceGroup(event.id, raceStatesSettings.getVisibleCourseAreas(),
                    raceStatesSettings.getVisibleRegattas(), raceStatesSettings.isShowOnlyCurrentlyRunningRaces(), raceStatesSettings.isShowOnlyRacesOfSameDay(),
                    new MarkedAsyncCallback<List<RegattaOverviewEntryDTO>>(
                            new AsyncCallback<List<RegattaOverviewEntryDTO>>() {
                                @Override
                                public void onFailure(Throwable cause) {
                                    Window.setStatus("Error while loading the race states with service getRaceStateEntriesForRaceGroup()");
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

}
