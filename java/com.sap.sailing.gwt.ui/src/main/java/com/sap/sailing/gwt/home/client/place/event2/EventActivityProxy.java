package com.sap.sailing.gwt.home.client.place.event2;

import java.util.List;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaActivity;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.overview.EventOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaActivity;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.tabs.overview.EventRegattaOverviewPlace;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventActivityProxy extends AbstractActivityProxy {

    private final AbstractEventPlace place;
    private EventContext ctx;
    private EventClientFactory clientFactory;

    public EventActivityProxy(AbstractEventPlace place, EventClientFactory clientFactory) {
        this.place = place;
        ctx = this.place.getCtx();
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        if (ctx.getEventDTO() != null) {
            afterLoad();
        } else {
            final UUID eventUUID = UUID.fromString(ctx.getEventId());
            clientFactory.getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
                @Override
                public void onSuccess(final EventDTO event) {
                    if(event != null) {
                        ctx.updateContext(event);
                        afterLoad();
                    } else {
                        // TODO
//                        createErrorView("No such event with UUID " + eventUUID, null, panel);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    // TODO
//                    createErrorView("Error while loading the event with service getEventById()", caught, panel);
                }
            }); 

        }
        
        
        
    }

    private void afterLoad() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                final AbstractEventPlace placeToStart;
                if(place instanceof EventDefaultPlace) {
                    placeToStart = getRealPlace();
                } else {
                    placeToStart = place;
                }
                
                if(placeToStart instanceof AbstractEventRegattaPlace) {
                    super.onSuccess(new EventRegattaActivity((AbstractEventRegattaPlace) placeToStart, clientFactory));
                }
                if(placeToStart instanceof AbstractMultiregattaEventPlace) {
                    super.onSuccess(new EventMultiregattaActivity((AbstractMultiregattaEventPlace) placeToStart, clientFactory));
                }
            }

        });
    }
    
    private AbstractEventPlace getRealPlace() {
        EventDTO event = ctx.getEventDTO();
        List<LeaderboardGroupDTO> leaderboardGroups = event.getLeaderboardGroups();
        if(leaderboardGroups.size() == 1) {
            LeaderboardGroupDTO leaderboardGroup = leaderboardGroups.get(0);
            if(leaderboardGroup.getLeaderboards().size() == 1) {
                StrippedLeaderboardDTO leaderboard = leaderboardGroup.getLeaderboards().get(0);
                return new EventRegattaOverviewPlace(ctx.withLeaderboardName(leaderboard.name));
            }
        }
        return new EventOverviewPlace(place.getCtx());
    }
}
