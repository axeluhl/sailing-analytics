package com.sap.sailing.gwt.home.desktop.places.event.regatta;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.communication.event.EventReferenceDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.HasRegattaMetadata;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.communication.eventview.RegattaReferenceDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.home.communication.eventview.HasRegattaMetadata.RegattaState;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.desktop.places.event.EventClientFactory;
import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.home.desktop.places.event.EventView.PlaceCallback;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class EventRegattaActivity extends AbstractEventActivity<AbstractEventRegattaPlace> implements EventRegattaView.Presenter {
    private EventRegattaView currentView = new TabletAndDesktopRegattaEventView();
    private final UserAgentDetails userAgent = new UserAgentDetails(Window.Navigator.getUserAgent());
    private final AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
    private final long delayBetweenAutoAdvancesInMilliseconds = 3000l;

    public EventRegattaActivity(AbstractEventRegattaPlace place, EventViewDTO eventDTO, EventClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator, NavigationPathDisplay navigationPathDisplay) {
        super(place, eventDTO, clientFactory, homePlacesNavigator);
        if (this.ctx.getRegattaAnalyticsManager() == null) {
            ctx.withRegattaAnalyticsManager(new RegattaAnalyticsDataManager(
                    clientFactory.getSailingService(),
                    asyncActionsExecutor,
                    new Timer(PlayModes.Live, PlayStates.Paused, delayBetweenAutoAdvancesInMilliseconds),
                    clientFactory.getErrorReporter(),
                    userAgent));
        }
        
        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        StringMessages i18n = StringMessages.INSTANCE;
        List<NavigationItem> navigationItems = new ArrayList<>();
        navigationItems.add(new NavigationItem(i18n.home(), getHomeNavigation()));
        navigationItems.add(new NavigationItem(i18n.events(), getEventsNavigation()));
        if(getEventDTO().getType() == EventType.SERIES_EVENT) {
            navigationItems.add(new NavigationItem(getEventDTO().getSeriesName(), getCurrentEventSeriesNavigation()));
        }
        navigationItems.add(new NavigationItem(getEventDTO().getLocationOrDisplayName(), getCurrentEventNavigation()));
        
        if(showRegattaMetadata()) {
            navigationItems.add(new NavigationItem(getRegattaMetadata().getDisplayName(), getCurrentRegattaOverviewNavigation()));
        }
        navigationPathDisplay.showNavigationPath(navigationItems.toArray(new NavigationItem[navigationItems.size()]));
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        currentView.registerPresenter(this);
        panel.setWidget(currentView);
        currentView.navigateTabsTo(currentPlace);
    }
    
    @Override
    public boolean needsSelectionInHeader() {
        EventViewDTO event = eventDTO;
        return (event.getType() == EventType.SERIES_EVENT || event.getType() == EventType.MULTI_REGATTA);
    }
    
    @Override
    public void forPlaceSelection(PlaceCallback callback) {
        EventViewDTO event = eventDTO;
        if (event.getType() == EventType.SERIES_EVENT) {
            for(EventReferenceDTO seriesEvent : event.getEventsOfSeries()) {
                AbstractEventRegattaPlace place = currentPlace.newInstanceWithContext(new EventContext().withId(seriesEvent.getId().toString()));
                callback.forPlace(place, seriesEvent.getDisplayName(), (event.getId().equals(seriesEvent.getId())));
            }
        } else {
            for(RegattaReferenceDTO regatta : event.getRegattas()) {
                AbstractEventRegattaPlace place = currentPlace.newInstanceWithContext(contextForRegatta(regatta.getId()));
                callback.forPlace(place, regatta.getDisplayName(), (getRegattaId().equals(regatta.getId())));
            }
        }
    }
    
    @Override
    public boolean showRegattaMetadata() {
        return eventDTO.getType() == EventType.MULTI_REGATTA && getRegatta() != null;
    }
    
    @Override
    public PlaceNavigation<RegattaOverviewPlace> getCurrentRegattaOverviewNavigation() {
        return homePlacesNavigator.getEventNavigation(new RegattaOverviewPlace(ctx), null, false);
    }

    @Override
    public Timer getAutoRefreshTimer() {
        return ctx.getRegattaAnalyticsManager().getTimer();
    }

    @Override
    protected EventView<AbstractEventRegattaPlace, ?> getView() {
        return currentView;
    }
    
    public RegattaMetadataDTO getRegatta() {
        String regattaId = getRegattaId();
        if(regattaId == null) {
            return null;
        }
        for (RegattaMetadataDTO regatta : eventDTO.getRegattas()) {
            if(regattaId.equals(regatta.getId())) {
                return regatta;
            }
        }
        return null;
    }
    
    @Override
    public HasRegattaMetadata getRegattaMetadata() {
        return getRegatta();
    }
    
    @Override
    public boolean isEventOrRegattaLive() {
        if(showRegattaMetadata()) {
            if(getRegatta().getState() == RegattaState.RUNNING) {
                return true;
            }
        }
        return super.isEventOrRegattaLive();
    }
}
