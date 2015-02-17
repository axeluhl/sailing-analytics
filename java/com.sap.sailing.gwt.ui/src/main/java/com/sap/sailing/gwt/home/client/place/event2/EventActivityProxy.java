package com.sap.sailing.gwt.home.client.place.event2;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaActivity;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.MultiregattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaActivity;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventActivityProxy extends AbstractActivityProxy {

    private final AbstractEventPlace place;
    private EventContext ctx;
    private EventClientFactory clientFactory;

    public EventActivityProxy(AbstractEventPlace place, EventClientFactory clientFactory) {
        this.place = place;
        ctx = place.getCtx();
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
                AbstractEventPlace placeToStart = place;
                if(placeToStart instanceof EventPlace) {
                    // TODO decide dependent on event type
                    placeToStart = new MultiregattaOverviewPlace(ctx);
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
}
