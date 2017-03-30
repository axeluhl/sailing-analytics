package com.sap.sailing.gwt.autoplay.client.place.sixtyinch;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.Slide0Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.Slide0PresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.Slide0ViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2PresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2ViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4.Slide4Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4.Slide4PresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4.Slide4ViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide5.Slide5Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide5.Slide5PresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide5.Slide5ViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6.Slide6Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6.Slide6PresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6.Slide6ViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.Slide7Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.Slide7PresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.Slide7ViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.RaceEndWithBoatsPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.RaceEndWithBoatsPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.RaceEndWithBoatsViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide9.Slide9Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide9.Slide9PresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide9.Slide9ViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitViewImpl;
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
        if (place instanceof StartPlaceSixtyInch) {
            return new StartActivitySixtyInch((StartPlaceSixtyInch) place, clientFactory);
        }
        if (place instanceof SlideInitPlace) {
            return new SlideInitPresenterImpl((SlideInitPlace) place, clientFactory, new SlideInitViewImpl());
        }

        if (place instanceof Slide0Place) {
            return new Slide0PresenterImpl((Slide0Place) place, clientFactory, new Slide0ViewImpl());
        }
        if (place instanceof Slide2Place) {
            return new Slide2PresenterImpl((Slide2Place) place, clientFactory, new Slide2ViewImpl());
        }
        if (place instanceof Slide4Place) {
            return new Slide4PresenterImpl((Slide4Place) place, clientFactory, new Slide4ViewImpl());
        }
        if (place instanceof Slide5Place) {
            return new Slide5PresenterImpl((Slide5Place) place, clientFactory, new Slide5ViewImpl());
        }
        if (place instanceof Slide6Place) {
            return new Slide6PresenterImpl((Slide6Place) place, clientFactory, new Slide6ViewImpl());
        }
        if (place instanceof Slide7Place) {
            return new Slide7PresenterImpl((Slide7Place) place, clientFactory, new Slide7ViewImpl());
        }
        if (place instanceof RaceEndWithBoatsPlace) {
            return new RaceEndWithBoatsPresenterImpl((RaceEndWithBoatsPlace) place, clientFactory,
                    new RaceEndWithBoatsViewImpl());
        }
        if (place instanceof Slide9Place) {
            return new Slide9PresenterImpl((Slide9Place) place, clientFactory, new Slide9ViewImpl());
        }

        GWT.log("unknown place! " + place);
        return new SlideInitPresenterImpl(new SlideInitPlace(), clientFactory, new SlideInitViewImpl());
    }
}
