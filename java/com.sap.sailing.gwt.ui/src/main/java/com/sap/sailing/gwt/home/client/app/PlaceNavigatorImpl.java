package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;

public class PlaceNavigatorImpl implements PlaceNavigator {
    private final PlaceController placeController;
    
    protected PlaceNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    @Override
    public void goToHome() {
        placeController.goTo(new StartPlace());
    }

    @Override
    public void goToEvent(String eventUuidAsString) {
        placeController.goTo(new EventPlace(eventUuidAsString));
    }

    @Override
    public void goToEvents() {
        placeController.goTo(new EventsPlace());
    }

    @Override
    public void goToAboutUs() {
        placeController.goTo(new AboutUsPlace());
    }

    @Override
    public void goToContact() {
        placeController.goTo(new ContactPlace());
    }

    @Override
    public void goToSolutions() {
        placeController.goTo(new SolutionsPlace());
    }

    @Override
    public void goToSponsoring() {
        placeController.goTo(new SponsoringPlace());
    }

}
