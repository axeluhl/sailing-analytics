package com.sap.sailing.gwt.home.desktop.app;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystemImpl;
import com.sap.sailing.gwt.home.desktop.places.error.TabletAndDesktopErrorView;
import com.sap.sailing.gwt.home.desktop.places.events.EventsView;
import com.sap.sailing.gwt.home.desktop.places.events.TabletAndDesktopEventsView;
import com.sap.sailing.gwt.home.desktop.places.searchresult.TabletAndDesktopSearchResultView;
import com.sap.sailing.gwt.home.desktop.places.solutions.SolutionsView;
import com.sap.sailing.gwt.home.desktop.places.solutions.TabletAndDesktopSolutionsView;
import com.sap.sailing.gwt.home.desktop.places.sponsoring.SponsoringView;
import com.sap.sailing.gwt.home.desktop.places.sponsoring.TabletAndDesktopSponsoringView;
import com.sap.sailing.gwt.home.desktop.places.start.StartView;
import com.sap.sailing.gwt.home.desktop.places.start.TabletAndDesktopStartView;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.TabletAndDesktopWhatsNewView;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewView;
import com.sap.sailing.gwt.home.shared.partials.busy.BusyViewImpl;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultView;
import com.sap.sailing.gwt.home.shared.places.solutions.SolutionsPlace.SolutionsNavigationTabs;
import com.sap.sailing.gwt.ui.client.refresh.BusyView;


public class TabletAndDesktopApplicationClientFactory extends AbstractApplicationClientFactory<ApplicationTopLevelView<DesktopResettableNavigationPathDisplay>> implements DesktopClientFactory {
    private final SailingDispatchSystem dispatch = new SailingDispatchSystemImpl();
    
    public TabletAndDesktopApplicationClientFactory(boolean isStandaloneServer) {
        this(new SimpleEventBus(), isStandaloneServer);
    }
    
    private TabletAndDesktopApplicationClientFactory(EventBus eventBus, boolean isStandaloneServer) {
        this(eventBus, new PlaceController(eventBus), isStandaloneServer);
    }

    private TabletAndDesktopApplicationClientFactory(EventBus eventBus, PlaceController placeController, boolean isStandaloneServer) {
        this(eventBus, placeController, new DesktopPlacesNavigator(placeController, isStandaloneServer));
    }

    private TabletAndDesktopApplicationClientFactory(EventBus eventBus, PlaceController placeController, DesktopPlacesNavigator placesNavigator) {
        super(new TabletAndDesktopApplicationView(placesNavigator, eventBus), eventBus, placeController, placesNavigator);
    }
    
    @Override
    public DesktopResettableNavigationPathDisplay getNavigationPathDisplay() {
        return getTopLevelView().getNavigationPathDisplay();
    }

    @Override
    public TabletAndDesktopErrorView createErrorView(String errorMessage, Throwable errorReason) {
        return new TabletAndDesktopErrorView(errorMessage, errorReason, null);
    }

    @Override
    public EventsView createEventsView() {
        return new TabletAndDesktopEventsView(getHomePlacesNavigator());
    }

    @Override
    public StartView createStartView() {
        return new TabletAndDesktopStartView(getHomePlacesNavigator());
    }

    @Override
    public SponsoringView createSponsoringView() {
        return new TabletAndDesktopSponsoringView();
    }

    @Override
    public SolutionsView createSolutionsView(SolutionsNavigationTabs navigationTab) {
        return new TabletAndDesktopSolutionsView(navigationTab, getHomePlacesNavigator());
    }

    @Override
    public SearchResultView createSearchResultView() {
        return new TabletAndDesktopSearchResultView(getHomePlacesNavigator());
    }

    @Override
    public WhatsNewView createWhatsNewView(WhatsNewNavigationTabs navigationTab) {
        return new TabletAndDesktopWhatsNewView(navigationTab, getHomePlacesNavigator());
    }

    @Override
    public SailingDispatchSystem getDispatch() {
        return dispatch;
    }

    @Override
    public BusyView createBusyView() {
        return new BusyViewImpl();
    }
}
