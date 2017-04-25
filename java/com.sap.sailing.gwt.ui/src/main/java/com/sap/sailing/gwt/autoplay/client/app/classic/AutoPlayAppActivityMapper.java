package com.sap.sailing.gwt.autoplay.client.app.classic;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerActivityProxy;
import com.sap.sailing.gwt.autoplay.client.places.startclassic.old.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.config.ClassicConfigPlace;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.config.ClassicConfigPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.config.ClassicConfigViewImpl;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.initial.ClassicInitialImpl;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.initial.ClassicInitialPlace;
import com.sap.sailing.gwt.autoplay.client.places.startup.classic.initial.ClassicInitialPresenterImpl;

public class AutoPlayAppActivityMapper implements ActivityMapper {
    private final AutoPlayClientFactoryClassic clientFactory;

    public AutoPlayAppActivityMapper(AutoPlayClientFactoryClassic clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {

        if (place instanceof ClassicConfigPlace) {
            return new ClassicConfigPresenterImpl((ClassicConfigPlace) place, clientFactory,
                    new ClassicConfigViewImpl(clientFactory));
        } else if (place instanceof ClassicInitialPlace) {
            return new ClassicInitialPresenterImpl((ClassicInitialPlace) place, clientFactory,
                    new ClassicInitialImpl());
        } else if (place instanceof PlayerPlace) {
            return new PlayerActivityProxy((PlayerPlace) place, clientFactory);
        } else {
            return null;
        }
    }
}
