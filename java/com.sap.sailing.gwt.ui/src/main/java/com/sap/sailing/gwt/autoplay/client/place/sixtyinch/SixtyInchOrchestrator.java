package com.sap.sailing.gwt.autoplay.client.place.sixtyinch;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Timer;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2Place;

public class SixtyInchOrchestrator {
    
    private PlaceController placeController;

    private Place[] simplePath = { new Slide1Place(), new Slide2Place() };

    private int currentSlideNr = -1;

    public SixtyInchOrchestrator(PlaceController placeController) {
        this.placeController = placeController;
    }

    private Timer transitionTrigger = new Timer() {
        @Override
        public void run() {
            triggerNextSlide();
        }
        
    };

    public void start() {
        transitionTrigger.run();
    }
    
    private void triggerNextSlide() {
        currentSlideNr++;
        if (currentSlideNr > simplePath.length - 1) {
            currentSlideNr = 0;
        }
        placeController.goTo(simplePath[currentSlideNr]);
        transitionTrigger.schedule(10000);
    }
}
