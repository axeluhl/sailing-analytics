package com.sap.sailing.gwt.home.desktop.places.fakeseries;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.client.place.event.legacy.SeriesClientFactory;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.home.communication.fakeseries.GetEventSeriesViewAction;
import com.sap.sailing.gwt.home.desktop.HighChartInjector;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.app.WithHeader;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.eventstab.SeriesEventsPlace;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.overallleaderboardtab.EventSeriesOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.series.minileaderboard.SeriesMiniOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.app.ActivityProxyCallback;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.ProvidesNavigationPath;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SeriesActivityProxy extends AbstractActivityProxy implements ProvidesNavigationPath, WithHeader {

    private AbstractSeriesPlace place;
    private SeriesContext ctx;
    private SeriesClientFactory clientFactory;
    private final DesktopPlacesNavigator homePlacesNavigator;
    private NavigationPathDisplay navigationPathDisplay;

    public SeriesActivityProxy(AbstractSeriesPlace place, SeriesClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator) {
        this.place = place;
        this.ctx = place.getCtx();
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;
    }
    
    @Override
    public void setNavigationPathDisplay(NavigationPathDisplay navigationPathDisplay) {
        this.navigationPathDisplay = navigationPathDisplay;
    }

    @Override
    protected void startAsync() {
        final UUID seriesUUID = UUID.fromString(ctx.getSeriesId());
        clientFactory.getDispatch().execute(new GetEventSeriesViewAction(seriesUUID), 
                new ActivityProxyCallback<EventSeriesViewDTO>(clientFactory, place) {
            @Override
            public void onSuccess(EventSeriesViewDTO series) {
                afterLoad(series);
            }
        });
    }

    private void afterLoad(final EventSeriesViewDTO series) {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if (place instanceof SeriesDefaultPlace) {
                    place = getRealPlace(series);
                }
                place = verifyAndAdjustPlace();
                HighChartInjector.loadHighCharts(new Runnable() {
                    
                    @Override
                    public void run() {
                        onSuccess(new SeriesActivity((AbstractSeriesTabPlace) place, series, clientFactory,
                                homePlacesNavigator, navigationPathDisplay));
                    }
                });
            }
        });
    }

    private AbstractSeriesPlace getRealPlace(EventSeriesViewDTO series) {
        if (series.isHasAnalytics()) {
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
