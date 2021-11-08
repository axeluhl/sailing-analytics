package com.sap.sailing.gwt.home.desktop.places.fakeseries;

import java.util.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.legacy.SeriesClientFactory;
import com.sap.sailing.gwt.home.communication.fakeseries.EventSeriesViewDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.event.EventDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.security.ui.client.UserService;

public class SeriesActivity extends AbstractActivity implements SeriesView.Presenter {

    protected final AbstractSeriesTabPlace currentPlace;
    protected final SeriesContext ctx;
    protected final SeriesClientFactory clientFactory;

    protected final DesktopPlacesNavigator homePlacesNavigator;
    
    private static final ApplicationHistoryMapper historyMapper = GWT.create(ApplicationHistoryMapper.class);
    
    private SeriesView<AbstractSeriesTabPlace, SeriesView.Presenter> currentView = new TabletAndDesktopSeriesView();
    
    private final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
    private final long delayBetweenAutoAdvancesInMilliseconds = LeaderboardEntryPoint.DEFAULT_REFRESH_INTERVAL_MILLIS;
    private final EventSeriesViewDTO series;

    public SeriesActivity(AbstractSeriesTabPlace place, EventSeriesViewDTO series, SeriesClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator, NavigationPathDisplay navigationPathDisplay, FlagImageResolver flagImageResolver) {
        this.currentPlace = place;
        this.series = series;
        this.ctx = new SeriesContext(place.getCtx());
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;

        if (this.ctx.getAnalyticsManager() == null) {
            ctx.withAnalyticsManager(new EventSeriesAnalyticsDataManager( //
                    clientFactory, //
                    asyncActionsExecutor, //
                    new Timer(PlayModes.Live, PlayStates.Paused, delayBetweenAutoAdvancesInMilliseconds), //
                    clientFactory.getErrorReporter(), flagImageResolver));

        }
        
        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        StringMessages i18n = StringMessages.INSTANCE;
        navigationPathDisplay.showNavigationPath(new NavigationItem(i18n.home(), getHomeNavigation()),
                new NavigationItem(i18n.events(), getEventsNavigation()),
                new NavigationItem(getSeriesDTO().getDisplayName(),  getCurrentEventSeriesNavigation()));
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
        return homePlacesNavigator.getEventNavigation(eventId.toString(), null, false);
    }
    
    @Override
    public PlaceNavigation<RegattaOverviewPlace> getRegattaNavigation(UUID eventId, String leaderboardName) {
        return homePlacesNavigator.getRegattaNavigation(eventId.toString(), leaderboardName, null, false);
    }

    @Override
    public PlaceNavigation<SeriesDefaultPlace> getCurrentEventSeriesNavigation() {
        return homePlacesNavigator.getEventSeriesNavigation(ctx, null, false);
    }
    
    @Override
    public Timer getAutoRefreshTimer() {
        return ctx.getAnalyticsManager().getTimer();
    }
    
    @Override
    public EventSeriesViewDTO getSeriesDTO() {
        return series;
    }

    @Override
    public ErrorAndBusyClientFactory getErrorAndBusyClientFactory() {
        return clientFactory;
    }
    
    @Override
    public UserService getUserService() {
        return clientFactory.getUserService();
    }
}