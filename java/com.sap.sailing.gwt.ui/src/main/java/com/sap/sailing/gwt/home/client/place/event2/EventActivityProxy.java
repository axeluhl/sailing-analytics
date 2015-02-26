package com.sap.sailing.gwt.home.client.place.event2;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaActivity;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.MultiregattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaActivity;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaOverviewPlace;
import com.sap.sailing.gwt.ui.shared.eventview.EventMetadataDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventType;
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
            
            clientFactory.getSailingService().getEventMetadataById(eventUUID, new AsyncCallback<EventMetadataDTO>() {
                @Override
                public void onSuccess(final EventMetadataDTO event) {
                    if (event != null) {
                        ctx.updateContext(event);
                        afterLoad();
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
        EventMetadataDTO event = ctx.getEventDTO();
        if(event.getType() == EventType.SERIES_EVENT) {
            return new RegattaOverviewPlace(ctx);
        }
        if(event.getType() == EventType.SINGLE_REGATTA) {
            return new RegattaOverviewPlace(ctx.withRegattaId(event.getRegattas().get(0).getId()));
        }
        return new MultiregattaOverviewPlace(place.getCtx());
    }
}
