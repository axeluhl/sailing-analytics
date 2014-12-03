package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerActivityProxy;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.place.start.StartActivityProxy;
import com.sap.sailing.gwt.autoplay.client.place.start.StartPlace;

public class AutoPlayAppActivityMapper implements ActivityMapper {
    private final AutoPlayAppClientFactory clientFactory;

    public AutoPlayAppActivityMapper(AutoPlayAppClientFactory clientFactory) {
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
