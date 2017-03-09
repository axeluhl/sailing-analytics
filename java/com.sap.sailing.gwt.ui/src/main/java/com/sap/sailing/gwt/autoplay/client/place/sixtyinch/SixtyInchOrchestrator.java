package com.sap.sailing.gwt.autoplay.client.place.sixtyinch;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Timer;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.dataloader.AutoPlayDataLoader;
import com.sap.sailing.gwt.autoplay.client.dataloader.EventDTODataLoader;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2Place;

public class SixtyInchOrchestrator {

    private AutoPlayClientFactorySixtyInch cf;

    private Place[] simplePath = { new Slide1Place(), new Slide2Place() };

    private int currentSlideNr = -1;

    private List<AutoPlayDataLoader> loaders = new ArrayList<>();

    public SixtyInchOrchestrator(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;

        loaders.add(new EventDTODataLoader());
    }

    private Timer transitionTrigger = new Timer() {
        @Override
        public void run() {
            triggerNextSlide();
        }
    };

    public void start() {
        transitionTrigger.schedule(0);
        for (AutoPlayDataLoader loader : loaders) {
            loader.startLoading(cf.getEventBus(), cf);
        }
    }

    private void triggerNextSlide() {
        currentSlideNr++;
        if (currentSlideNr > simplePath.length - 1) {
            currentSlideNr = 0;
        }
        Place newPlace = simplePath[currentSlideNr];
        GWT.log("Going to: " + newPlace.getClass().getName());
        cf.getPlaceController().goTo(newPlace);

        transitionTrigger.schedule(10000);
    }
}
