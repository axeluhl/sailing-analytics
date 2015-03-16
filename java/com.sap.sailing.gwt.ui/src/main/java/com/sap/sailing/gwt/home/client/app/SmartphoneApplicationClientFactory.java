package com.sap.sailing.gwt.home.client.app;

import java.util.List;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.client.place.error.TabletAndDesktopErrorView;
import com.sap.sailing.gwt.home.client.place.event.EventPlace.EventNavigationTabs;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.EventWithoutRegattasView;
import com.sap.sailing.gwt.home.client.place.event.TabletAndDesktopEventView;
import com.sap.sailing.gwt.home.client.place.event.TabletAndDesktopEventWithoutRegattasView;
import com.sap.sailing.gwt.home.client.place.events.EventsView;
import com.sap.sailing.gwt.home.client.place.events.TabletAndDesktopEventsView;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaAnalyticsView;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaPlace.RegattaNavigationTabs;
import com.sap.sailing.gwt.home.client.place.regatta.TabletAndDesktopRegattaView;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultView;
import com.sap.sailing.gwt.home.client.place.searchresult.TabletAndDesktopSearchResultView;
import com.sap.sailing.gwt.home.client.place.series.SeriesAnalyticsView;
import com.sap.sailing.gwt.home.client.place.series.SeriesPlace.SeriesNavigationTabs;
import com.sap.sailing.gwt.home.client.place.series.TabletAndDesktopSeriesView;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace.SolutionsNavigationTabs;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsView;
import com.sap.sailing.gwt.home.client.place.solutions.TabletAndDesktopSolutionsView;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringView;
import com.sap.sailing.gwt.home.client.place.sponsoring.TabletAndDesktopSponsoringView;
import com.sap.sailing.gwt.home.client.place.start.StartView;
import com.sap.sailing.gwt.home.client.place.start.TabletAndDesktopStartView;
import com.sap.sailing.gwt.home.client.place.whatsnew.TabletAndDesktopWhatsNewView;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.client.place.whatsnew.WhatsNewView;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sse.gwt.client.player.Timer;


public class SmartphoneApplicationClientFactory extends AbstractApplicationClientFactory implements ApplicationClientFactory {
    public SmartphoneApplicationClientFactory() {
        this(new SimpleEventBus());
    }
    
    private SmartphoneApplicationClientFactory(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private SmartphoneApplicationClientFactory(EventBus eventBus, PlaceController placeController) {
        super(new TabletAndDesktopApplicationView(new HomePlacesNavigator(placeController), eventBus), eventBus, placeController);
    }

    @Override
    public EventView createEventView(EventDTO event, EventNavigationTabs navigationTab, List<RaceGroupDTO> raceGroups, String leaderboardName, Timer timerForClientServerOffset) {
        return new TabletAndDesktopEventView(getSailingService(), event, navigationTab, raceGroups, leaderboardName, timerForClientServerOffset, getHomePlacesNavigator());
    }

    @Override
    public EventWithoutRegattasView createEventWithoutRegattasView(EventDTO event) {
        return new TabletAndDesktopEventWithoutRegattasView(getSailingService(), event, getHomePlacesNavigator());
    }

    @Override
    public EventsView createEventsView() {
        return new TabletAndDesktopEventsView(getHomePlacesNavigator());
    }

    @Override
    public TabletAndDesktopErrorView createErrorView(String errorMessage, Throwable errorReason) {
        return new TabletAndDesktopErrorView(errorMessage, errorReason, null);
    }

    @Override
    public StartView createStartView() {
        return new TabletAndDesktopStartView(getHomePlacesNavigator());
    }

    @Override
    public SponsoringView createSponsoringView() {
        return new TabletAndDesktopSponsoringView();
    }

    public SolutionsView createSolutionsView(SolutionsNavigationTabs navigationTab) {
        return new TabletAndDesktopSolutionsView(navigationTab, getHomePlacesNavigator());
    }

    @Override
    public RegattaAnalyticsView createRegattaAnalyticsView(EventDTO event, String leaderboardName, RegattaNavigationTabs navigationTab, Timer timerForClientServerOffset) {
        return new TabletAndDesktopRegattaView(event, leaderboardName, navigationTab, timerForClientServerOffset, getHomePlacesNavigator());
    }

    @Override
    public SeriesAnalyticsView createSeriesAnalyticsView(EventDTO event, String leaderboardName, SeriesNavigationTabs navigationTab, Timer timerForClientServerOffset) {
        return new TabletAndDesktopSeriesView(event, leaderboardName, navigationTab, timerForClientServerOffset, getHomePlacesNavigator());
    }

    @Override
    public SearchResultView createSearchResultView() {
        return new TabletAndDesktopSearchResultView(getHomePlacesNavigator());
    }
    
    @Override
    public WhatsNewView createWhatsNewView(WhatsNewNavigationTabs navigationTab) {
        return new TabletAndDesktopWhatsNewView(navigationTab, getHomePlacesNavigator());
    }
}
