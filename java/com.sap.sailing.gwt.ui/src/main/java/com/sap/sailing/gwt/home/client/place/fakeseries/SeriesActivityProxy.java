package com.sap.sailing.gwt.home.client.place.fakeseries;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.fakeseries.tabs.SeriesEventsPlace;
import com.sap.sailing.gwt.home.client.place.series.SeriesClientFactory;
import com.sap.sailing.gwt.ui.shared.fakeseries.EventSeriesViewDTO;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SeriesActivityProxy extends AbstractActivityProxy {

    private final AbstractSeriesPlace place;
    private SeriesContext ctx;
    private SeriesClientFactory clientFactory;
    private final HomePlacesNavigator homePlacesNavigator;

    public SeriesActivityProxy(AbstractSeriesPlace place, SeriesClientFactory clientFactory,
            HomePlacesNavigator homePlacesNavigator) {
        this.place = place;
        this.ctx = place.getCtx();
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;
    }

    @Override
    protected void startAsync() {
        if (ctx.getSeriesDTO() != null) {
            afterLoad();
        } else {
            final UUID seriesUUID = UUID.fromString(ctx.getSeriesId());
            
            clientFactory.getSailingService().getEventSeriesViewById(seriesUUID, new AsyncCallback<EventSeriesViewDTO>() {
                @Override
                public void onSuccess(final EventSeriesViewDTO event) {
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
                
                super.onSuccess(new SeriesTabActivity((AbstractSeriesTabPlace) placeToStart, clientFactory, homePlacesNavigator));
            }

        });
    }
    
    private AbstractSeriesPlace getRealPlace() {
        return new SeriesEventsPlace(ctx);
    }
}
