package com.sap.sailing.gwt.home.desktop.places.fakeseries;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.client.place.event.legacy.SeriesClientFactory;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.home.communication.fakeseries.GetEventSeriesViewAction;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.app.WithHeader;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.eventstab.SeriesEventsPlace;
import com.sap.sailing.gwt.home.desktop.places.fakeseries.overallleaderboardtab.EventSeriesOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.series.minileaderboard.SeriesMiniOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.app.ActivityProxyCallback;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.ProvidesNavigationPath;
import com.sap.sailing.gwt.home.shared.places.error.ErrorPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;
import com.sap.sse.gwt.resources.CommonControlsCSS;
import com.sap.sse.gwt.resources.Highcharts;

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
        if (ctx.getLeaderboardGroupId() == null && ctx.getSeriesId() == null) {
            ErrorPlace errorPlace = new ErrorPlace("series and leaderboardGroup is null");
            errorPlace.setComingFrom(errorPlace);
            clientFactory.getPlaceController().goTo(errorPlace);
        } else {
            clientFactory.getDispatch().execute(new GetEventSeriesViewAction(ctx),
                    new ActivityProxyCallback<EventSeriesViewDTO>(clientFactory, place) {
                        @Override
                        public void onSuccess(EventSeriesViewDTO series) {
                            ctx.updateLeaderboardGroupId(series.getLeaderboardGroupUUID());
                            afterLoad(series);
                        }
                    });
        }
    }

    private void afterLoad(final EventSeriesViewDTO series) {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if (place instanceof SeriesDefaultPlace) {
                    place = getRealPlace(series);
                }
                place = verifyAndAdjustPlace();

                CommonControlsCSS.ensureInjected();
                Highcharts.ensureInjected();
                withFlagImageResolver(flagImageResolver -> new SeriesActivity((AbstractSeriesTabPlace) place, series, clientFactory,
                        homePlacesNavigator, navigationPathDisplay, flagImageResolver));
            }
            private void withFlagImageResolver(final Function<FlagImageResolver, Activity> activityFactory) {
                final Consumer<Activity> onSuccess = super::onSuccess;
                final Consumer<Throwable> onFailure = super::onFailure;
                FlagImageResolver.get(new AsyncCallback<FlagImageResolver>() {
                    @Override
                    public void onSuccess(FlagImageResolver result) {
                        onSuccess.accept(activityFactory.apply(result));
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        onFailure.accept(caught);
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
