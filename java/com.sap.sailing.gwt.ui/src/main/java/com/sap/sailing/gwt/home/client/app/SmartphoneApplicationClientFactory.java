package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.events.EventsActivity;
import com.sap.sailing.gwt.home.client.place.events.EventsView;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultView;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsActivity;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsView;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringActivity;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringView;
import com.sap.sailing.gwt.home.client.place.start.SmartphoneStartView;
import com.sap.sailing.gwt.home.client.place.start.StartView;
import com.sap.sailing.gwt.ui.shared.EventDTO;


public class SmartphoneApplicationClientFactory extends AbstractApplicationClientFactory implements ApplicationClientFactory {
    public SmartphoneApplicationClientFactory() {
        this(new SimpleEventBus());
    }
    
    private SmartphoneApplicationClientFactory(EventBus eventBus) {
        this(eventBus, new PlaceController(eventBus));
    }

    private SmartphoneApplicationClientFactory(EventBus eventBus, PlaceController placeController) {
        super(new SmartphoneApplicationView(new PlaceNavigatorImpl(placeController)), eventBus, placeController);
    }

    @Override
    public EventView createEventView(EventDTO event) {
        return null;
    }

    @Override
    public EventsView createEventsView(EventsActivity activity) {
        return null;
    }

    @Override
    public StartView createStartView() {
        return new SmartphoneStartView(getPlaceNavigator());
    }

    @Override
    public SponsoringView createSponsoringView(SponsoringActivity activity) {
        return null;
    }

    @Override
    public SolutionsView createSolutionsView(SolutionsActivity activity) {
        return null;
    }

    @Override
    public SearchResultView createSearchResultView() {
        return null;
    }
}
