package com.sap.sailing.gwt.home.mobile.places.series;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.home.communication.fakeseries.GetEventSeriesViewAction;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.app.ActivityProxyCallback;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.ProvidesNavigationPath;
import com.sap.sailing.gwt.home.shared.places.error.ErrorPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class SeriesActivityProxy extends AbstractActivityProxy implements ProvidesNavigationPath {

    private final MobileApplicationClientFactory clientFactory;
    private final AbstractSeriesPlace currentPlace;
    private NavigationPathDisplay navigationPathDisplay;

    public SeriesActivityProxy(AbstractSeriesPlace place, MobileApplicationClientFactory clientFactory) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
    }

    @Override
    public void setNavigationPathDisplay(NavigationPathDisplay navigationPathDisplay) {
        this.navigationPathDisplay = navigationPathDisplay;
    }

    @Override
    protected void startAsync() {
        Runnable doStart = new Runnable() {
            @Override
            public void run() {
                GWT.runAsync(new AbstractRunAsyncCallback() {
                    @Override
                    public void onSuccess() {
                        withFlagImageResolver(flagImageResolver -> new SeriesActivity(currentPlace,
                                navigationPathDisplay, clientFactory, flagImageResolver));
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
        };

        if (currentPlace.getCtx().getLeaderboardGroupId() != null) {
            doStart.run();
        } else {
            if (currentPlace.getCtx().getSeriesId() == null) {
                ErrorPlace errorPlace = new ErrorPlace("series and leaderboardGroup is null");
                errorPlace.setComingFrom(errorPlace);
                clientFactory.getPlaceController().goTo(errorPlace);
            } else {
                // patch old link with seriesId to new leaderboardGroup based one
                clientFactory.getDispatch().execute(new GetEventSeriesViewAction(currentPlace.getCtx()),
                        new ActivityProxyCallback<EventSeriesViewDTO>(clientFactory, currentPlace) {
                            @Override
                            public void onSuccess(EventSeriesViewDTO series) {
                                currentPlace.getCtx().updateLeaderboardGroupId(series.getLeaderboardGroupUUID());
                                doStart.run();
                            }
                        });
            }

        }
    }
}
