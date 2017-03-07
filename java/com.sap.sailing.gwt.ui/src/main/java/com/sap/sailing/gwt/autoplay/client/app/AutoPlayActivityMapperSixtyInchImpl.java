package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideContext;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideContextImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1PresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2PresenterImpl;

public class AutoPlayActivityMapperSixtyInchImpl implements ActivityMapper {
    private final AutoPlayClientFactory clientFactory;

    private SlideContext slideCtx;

    public AutoPlayActivityMapperSixtyInchImpl(AutoPlayClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
        this.slideCtx = new SlideContextImpl();
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof Slide1Place) {
            return new Slide1PresenterImpl((Slide1Place) place, slideCtx);
        } else if (place instanceof Slide2Place) {
            return new Slide2PresenterImpl((Slide2Place) place, slideCtx);
        } else {
            return null;
        }
    }
}
