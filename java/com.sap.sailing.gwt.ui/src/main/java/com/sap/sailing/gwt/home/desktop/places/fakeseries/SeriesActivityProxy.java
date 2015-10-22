package com.sap.sailing.gwt.home.desktop.places.fakeseries;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.client.place.event.legacy.SeriesClientFactory;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.error.ErrorPlace;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.eventstab.SeriesEventsPlace;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.overallleaderboardtab.EventSeriesOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.series.minileaderboard.SeriesMiniOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SeriesActivityProxy extends AbstractActivityProxy {

    private AbstractSeriesPlace place;
    private SeriesContext ctx;
    private SeriesClientFactory clientFactory;
    private final DesktopPlacesNavigator homePlacesNavigator;

    public SeriesActivityProxy(AbstractSeriesPlace place, SeriesClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator) {
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
            clientFactory.getHomeService().getEventSeriesViewById(seriesUUID, new AsyncCallback<EventSeriesViewDTO>() {
                @Override
                public void onSuccess(final EventSeriesViewDTO event) {
                    ctx.updateContext(event);
                    afterLoad();
                }
                @Override
                public void onFailure(Throwable caught) {
                    // TODO @FM: extract text?
                    ErrorPlace errorPlace = new ErrorPlace(
                            "Error while loading the series with service getEventSeriesViewById()");
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
                if (place instanceof SeriesDefaultPlace) {
                    place = getRealPlace();
                }
                place = verifyAndAdjustPlace();
                super.onSuccess(new SeriesActivity((AbstractSeriesTabPlace) place, clientFactory,
                        homePlacesNavigator));
            }
        });
    }

    private AbstractSeriesPlace getRealPlace() {
        if (ctx.getSeriesDTO().isHasAnalytics()) {
            return new EventSeriesOverallLeaderboardPlace(ctx);
        } else {
            return new SeriesEventsPlace(ctx);
        }
    }
    
    /**
     * Checks if the place is valid for the given event.
     * If not, the place is automatically being adjusted.
     */
    private AbstractSeriesPlace verifyAndAdjustPlace() {
        if(place instanceof SeriesMiniOverallLeaderboardPlace) {
            return new EventSeriesOverallLeaderboardPlace(place.getCtx());
        }
        
        // no adjustment necessary
        return place;
    }
}
