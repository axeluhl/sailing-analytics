package com.sap.sailing.gwt.home.client.place.fakeseries;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.event2.legacy.SeriesClientFactory;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sse.gwt.client.mvp.ErrorView;

public abstract class AbstractSeriesActivity<PLACE extends AbstractSeriesPlace> extends AbstractActivity implements SeriesView.Presenter {

    protected final PLACE currentPlace;
    protected final SeriesContext ctx;
    protected final SeriesClientFactory clientFactory;

    protected final HomePlacesNavigator homePlacesNavigator;
    
    private static final ApplicationHistoryMapper historyMapper = GWT.create(ApplicationHistoryMapper.class);

    public AbstractSeriesActivity(PLACE place, SeriesClientFactory clientFactory, HomePlacesNavigator homePlacesNavigator) {
        this.currentPlace = place;
        this.ctx = new SeriesContext(place.getCtx());
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;
    }

    @Override
    public SeriesContext getCtx() {
        return ctx;
    }

    @Override
    public void handleTabPlaceSelection(TabView<?, ? extends SeriesView.Presenter> selectedActivity) {
        Place tabPlaceToGo = selectedActivity.placeToFire();
        clientFactory.getPlaceController().goTo(tabPlaceToGo);
    }
    
    public void navigateTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }
    
    @Override
    public SafeUri getUrl(AbstractSeriesPlace place) {
        String token = historyMapper.getToken(place);
        return UriUtils.fromString("#" + token);
    }
    
    @Override
    public PlaceNavigation<StartPlace> getHomeNavigation() {
        return homePlacesNavigator.getHomeNavigation();
    }

    @Override
    public PlaceNavigation<EventsPlace> getEventsNavigation() {
        return homePlacesNavigator.getEventsNavigation();
    }
    
    @Override
    public PlaceNavigation<EventDefaultPlace> getEventNavigation(UUID eventId) {
        return homePlacesNavigator.getEventNavigation(eventId.toString(), ctx.getSeriesDTO().getBaseURL(), ctx.getSeriesDTO().isOnRemoteServer());
    }
    
    @Override
    public PlaceNavigation<SeriesDefaultPlace> getCurrentEventSeriesNavigation() {
        return homePlacesNavigator.getEventSeriesNavigation(ctx.getSeriesId(), ctx.getSeriesDTO().getBaseURL(), ctx.getSeriesDTO().isOnRemoteServer());
    }
    
    @Override
    public void ensureMedia(final AsyncCallback<MediaDTO> callback) {
        if(ctx.getMedia() != null) {
            callback.onSuccess(ctx.getMedia());
            return;
        }
        clientFactory.getSailingService().getMediaForEventSeries(ctx.getSeriesDTO().getId(), new AsyncCallback<MediaDTO>() {
            @Override
            public void onFailure(Throwable caught) {
             // TODO @FM: extract error message
                ErrorView errorView = clientFactory.createErrorView("Load media failure for series", caught);
                getView().showErrorInCurrentTab(errorView);
                // TODO: notify callback of failure?
                // callback.onFailure(caught);
            }
            
            @Override
            public void onSuccess(MediaDTO result) {
                ctx.withMedia(result);
                callback.onSuccess(result);
            }
        });
    }
    
    @Override
    public boolean hasMedia() {
        return ctx.getSeriesDTO().isHasMedia();
    }

    protected abstract SeriesView<PLACE, ?> getView();
}
