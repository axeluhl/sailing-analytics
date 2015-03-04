package com.sap.sailing.gwt.home.client.place.event2;

import java.util.List;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaActivity;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.MultiregattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.MultiregattaRegattasPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaActivity;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventActivityProxy extends AbstractActivityProxy {

    private AbstractEventPlace place;
    private EventContext ctx;
    private EventClientFactory clientFactory;
    private HomePlacesNavigator homePlacesNavigator;

    public EventActivityProxy(AbstractEventPlace place, EventClientFactory clientFactory,
            HomePlacesNavigator homePlacesNavigator) {
        this.place = place;
        this.homePlacesNavigator = homePlacesNavigator;
        ctx = this.place.getCtx();
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        if (ctx.getEventDTO() != null) {
            afterEventLoad();
        } else {
            final UUID eventUUID = UUID.fromString(ctx.getEventId());
            
            clientFactory.getSailingService().getEventViewById(eventUUID, new AsyncCallback<EventViewDTO>() {
                @Override
                public void onSuccess(final EventViewDTO event) {
                    if (event != null) {
                        ctx.updateContext(event);
                        afterEventLoad();
                    } else {
                        // TODO
                        // createErrorView("No such event with UUID " + eventUUID, null, panel);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    // TODO
                    // createErrorView("Error while loading the event with service getEventById()", caught, panel);
                }
            });

        }
        
        
        
    }

    private void afterEventLoad() {
        if(place instanceof EventDefaultPlace) {
            place = getRealPlace();
        }
        
        loadRegattaStructureIfNeeded();
    }
    
    protected void loadRegattaStructureIfNeeded() {
//        // TODO do this in a nicer way
//        if(place instanceof MultiregattaRegattasPlace || place instanceof RegattaRacesPlace) {
//            final long clientTimeWhenRequestWasSent = System.currentTimeMillis();
//
//            final EventViewDTO eventDTO = ctx.getEventDTO();
//            
//            clientFactory.getSailingService().getRegattaStructureOfEvent(eventDTO.id,
//                    new AsyncCallback<List<RaceGroupDTO>>() {
//                        @Override
//                        public void onSuccess(List<RaceGroupDTO> raceGroups) {
//                            if (raceGroups.size() > 0) {
//                                for (LeaderboardGroupDTO leaderboardGroupDTO : eventDTO.getLeaderboardGroups()) {
//                                    final long clientTimeWhenResponseWasReceived = System.currentTimeMillis();
//                                    if (leaderboardGroupDTO.getAverageDelayToLiveInMillis() != null) {
//                                        currentPresenter.getTimerForClientServerOffset().setLivePlayDelayInMillis(
//                                                leaderboardGroupDTO
//                                                .getAverageDelayToLiveInMillis());
//                                    }
//                                    currentPresenter.getTimerForClientServerOffset().adjustClientServerOffset(
//                                            clientTimeWhenRequestWasSent,
//                                            leaderboardGroupDTO.getCurrentServerTime(), clientTimeWhenResponseWasReceived);
//                                }
//                                afterLoad();
//                            } else {
//                                // createEventWithoutRegattasView(event, panel);
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Throwable caught) {
//                            // createErrorView(
//                            // "Error while loading the regatta structure with service getRegattaStructureOfEvent()",
//                            // caught, panel);
//                        }
//                    });
//        } else {
            afterLoad();
//        }
    }

    private void afterLoad() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if(place instanceof AbstractEventRegattaPlace) {
                    super.onSuccess(new EventRegattaActivity((AbstractEventRegattaPlace) place, clientFactory,
                            homePlacesNavigator));
                }
                if(place instanceof AbstractMultiregattaEventPlace) {
                    super.onSuccess(new EventMultiregattaActivity((AbstractMultiregattaEventPlace) place,
                            clientFactory, homePlacesNavigator));
                }
            }
        });
    }

    private AbstractEventPlace getRealPlace() {
        EventViewDTO event = ctx.getEventDTO();
        if(event.getType() == EventType.SERIES_EVENT) {
            return new RegattaOverviewPlace(ctx);
        }
        if(event.getType() == EventType.SINGLE_REGATTA) {
            return new RegattaOverviewPlace(ctx.withRegattaId(event.getRegattas().get(0).getId()));
        }
        return new MultiregattaOverviewPlace(place.getCtx());
    }
}
