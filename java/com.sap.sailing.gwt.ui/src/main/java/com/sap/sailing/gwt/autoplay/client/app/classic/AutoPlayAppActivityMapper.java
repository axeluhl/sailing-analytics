package com.sap.sailing.gwt.autoplay.client.app.classic;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerActivityProxy;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.StartActivityProxy;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.StartPlace;

public class AutoPlayAppActivityMapper implements ActivityMapper {
    private final AutoPlayClientFactory<PlaceNavigator> clientFactory;

    public AutoPlayAppActivityMapper(AutoPlayClientFactory<PlaceNavigator> clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof StartPlace) {
            return new StartActivityProxy((StartPlace) place, clientFactory);
        } else if (place instanceof PlayerPlace) {
            return new PlayerActivityProxy((PlayerPlace) place, clientFactory);
        } else {
            return null;
        }
    }
}
