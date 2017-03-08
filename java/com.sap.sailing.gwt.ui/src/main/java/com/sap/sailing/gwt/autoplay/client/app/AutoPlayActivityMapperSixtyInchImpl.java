package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1PresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1ViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2PresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2ViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.StartActivitySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.StartPlaceSixtyInch;

public class AutoPlayActivityMapperSixtyInchImpl implements ActivityMapper {
    private final AutoPlayClientFactorySixtyInch clientFactory;

    public AutoPlayActivityMapperSixtyInchImpl(AutoPlayClientFactorySixtyInch clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof Slide1Place) {
            return new Slide1PresenterImpl((Slide1Place) place, clientFactory.getSlideCtx(), new Slide1ViewImpl());
        } else if (place instanceof Slide2Place) {
            return new Slide2PresenterImpl((Slide2Place) place, clientFactory.getSlideCtx(), new Slide2ViewImpl());
        } else {
            return new StartActivitySixtyInch((StartPlaceSixtyInch) place, clientFactory);
        }
    }
}
