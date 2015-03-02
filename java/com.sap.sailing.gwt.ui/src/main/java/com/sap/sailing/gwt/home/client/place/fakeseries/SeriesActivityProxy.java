package com.sap.sailing.gwt.home.client.place.fakeseries;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.SeriesEventsPlace;
import com.sap.sailing.gwt.home.client.place.series.SeriesClientFactory;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SeriesActivityProxy extends AbstractActivityProxy {

    private final AbstractSeriesPlace place;
    private SeriesContext ctx;
    private SeriesClientFactory clientFactory;

    public SeriesActivityProxy(AbstractSeriesPlace place, SeriesClientFactory clientFactory) {
        this.place = place;
        this.ctx = place.getCtx();
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        if (ctx.getEventDTO() != null) {
            afterLoad();
        } else {
            final UUID eventUUID = UUID.fromString(ctx.getEventId());
            
            clientFactory.getSailingService().getEventViewById(eventUUID, new AsyncCallback<EventViewDTO>() {
                @Override
                public void onSuccess(final EventViewDTO event) {
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
                final AbstractSeriesPlace placeToStart;
                if(place instanceof SeriesDefaultPlace) {
                    placeToStart = getRealPlace();
                } else {
                    placeToStart = place;
                }
                
                super.onSuccess(new SeriesTabActivity((AbstractSeriesTabPlace) placeToStart, clientFactory));
            }

        });
    }
    
    private AbstractSeriesPlace getRealPlace() {
        return new SeriesEventsPlace(ctx);
    }
}
