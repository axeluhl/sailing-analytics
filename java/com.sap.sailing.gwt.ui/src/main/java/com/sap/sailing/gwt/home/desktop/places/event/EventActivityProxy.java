package com.sap.sailing.gwt.home.desktop.places.event;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.EventType;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.app.WithHeader;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.EventMultiregattaActivity;
import com.sap.sailing.gwt.home.desktop.places.event.multiregatta.overviewtab.MultiregattaOverviewPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.EventRegattaActivity;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.mobile.places.event.latestnews.LatestNewsPlace;
import com.sap.sailing.gwt.home.mobile.places.event.minileaderboard.MiniLeaderboardPlace;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventActivityProxy;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.gwt.resources.CommonControlsCSS;
import com.sap.sse.gwt.resources.Highcharts;

public class EventActivityProxy extends AbstractEventActivityProxy<EventClientFactory> implements WithHeader {

    private DesktopPlacesNavigator homePlacesNavigator;

    public EventActivityProxy(AbstractEventPlace place, EventClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator) {
        super(clientFactory, place);
        this.homePlacesNavigator = homePlacesNavigator;
    }

    @Override
    protected void afterEventLoad(final EventClientFactory clientFactory, final EventViewDTO event,
            final AbstractEventPlace place) {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                CommonControlsCSS.ensureInjected();
                Highcharts.ensureInjected();
                if (place instanceof AbstractEventRegattaPlace) {
                    withFlagImageResolver(flagImageResolver -> new EventRegattaActivity((AbstractEventRegattaPlace) place, event, clientFactory,
                            homePlacesNavigator, getNavigationPathDisplay(), flagImageResolver));
                }
                if (place instanceof AbstractMultiregattaEventPlace) {
                    onSuccess(new EventMultiregattaActivity((AbstractMultiregattaEventPlace) place, event,
                            clientFactory, homePlacesNavigator, getNavigationPathDisplay()));
                }
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
    
    @Override
    protected AbstractEventPlace verifyAndAdjustPlace(EventViewDTO event) {
        AbstractEventPlace adjustedPlace = super.verifyAndAdjustPlace(event);
        EventContext contextWithoutRegatta = new EventContext(adjustedPlace.getCtx()).withRegattaId(null);
        if(adjustedPlace instanceof LatestNewsPlace) {
            if(event.getType() == EventType.MULTI_REGATTA) {
                return new MultiregattaOverviewPlace(contextWithoutRegatta);
            }
            return new RegattaOverviewPlace(contextWithoutRegatta);
        }
        
        if(adjustedPlace instanceof MiniLeaderboardPlace) {
            return new RegattaLeaderboardPlace(adjustedPlace.getCtx());
        }
        return adjustedPlace;
    }

}
