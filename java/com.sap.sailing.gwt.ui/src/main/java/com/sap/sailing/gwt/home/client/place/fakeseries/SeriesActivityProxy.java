package com.sap.sailing.gwt.home.client.place.fakeseries;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.error.ErrorPlace;
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
                    ctx.updateContext(event);
                    afterLoad();
                }

                @Override
                public void onFailure(Throwable caught) {
                 // TODO @FM: extract text?
                    ErrorPlace errorPlace = new ErrorPlace("Error while loading the series with service getEventSeriesViewById()");
                    // TODO @FM: reload sinnvoll hier?
                    errorPlace.setComingFrom(place);
                    clientFactory.getPlaceController().goTo(errorPlace);
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
                
                super.onSuccess(new SeriesActivity((AbstractSeriesTabPlace) placeToStart, clientFactory, homePlacesNavigator));
            }
        });
    }
    
    private AbstractSeriesPlace getRealPlace() {
        return new SeriesEventsPlace(ctx);
    }
}
