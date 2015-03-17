package com.sap.sailing.gwt.home.client.place.fakeseries;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.event.legacy.SeriesClientFactory;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.shared.media.MediaDTO;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class SeriesActivity extends AbstractActivity implements SeriesView.Presenter {

    protected final AbstractSeriesTabPlace currentPlace;
    protected final SeriesContext ctx;
    protected final SeriesClientFactory clientFactory;

    protected final HomePlacesNavigator homePlacesNavigator;
    
    private static final ApplicationHistoryMapper historyMapper = GWT.create(ApplicationHistoryMapper.class);
    
    private SeriesView<AbstractSeriesTabPlace, SeriesView.Presenter> currentView = new TabletAndDesktopSeriesView();
    
    private final UserAgentDetails userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());
    private final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
    private final long delayBetweenAutoAdvancesInMilliseconds = 3000l;
    private final Timer autoRefreshTimer = new Timer(PlayModes.Live, PlayStates.Paused,
            delayBetweenAutoAdvancesInMilliseconds);

    public SeriesActivity(AbstractSeriesTabPlace place, SeriesClientFactory clientFactory, HomePlacesNavigator homePlacesNavigator) {
        this.currentPlace = place;
        this.ctx = new SeriesContext(place.getCtx());
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;

        if (this.ctx.getAnalyticsManager() == null) {
            ctx.withAnalyticsManager(new EventSeriesAnalyticsDataManager( //
                    clientFactory.getSailingService(), //
                    asyncActionsExecutor, //
                    autoRefreshTimer, //
                    clientFactory.getErrorReporter(), //
                    userAgent));

        }
    }
    
    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        currentView.registerPresenter(this);
        panel.setWidget(currentView);
        currentView.navigateTabsTo(currentPlace);
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
                callback.onFailure(caught);
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

    @Override
    public Timer getAutoRefreshTimer() {
        return autoRefreshTimer;
    }
}