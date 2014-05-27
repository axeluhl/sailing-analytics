package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.PlaceController;
import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.place.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.home.client.shared.mainmenu.MainMenuNavigator;

public class MainMenuNavigatorImpl implements MainMenuNavigator {
    private final PlaceController placeController;
    
    protected MainMenuNavigatorImpl(PlaceController placeController) {
        super();
        this.placeController = placeController;
    }

    @Override
    public void goToHome() {
        placeController.goTo(new StartPlace());
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

}
