package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.start;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayNode;

public class SlideInitPlace extends Place {

    private FailureEvent failureEvent;
    private AutoPlayNode currentSlideConfig;

    public SlideInitPlace(FailureEvent failureEvent, AutoPlayNode currentSlideConfig) {
        this.failureEvent = failureEvent;
        this.currentSlideConfig = currentSlideConfig;
    }

    public SlideInitPlace() {
    }

    public FailureEvent getFailureEvent() {
        return failureEvent;
    }

    public AutoPlayNode getCurrentSlideConfig() {
        return currentSlideConfig;
    }

    public static class Tokenizer implements PlaceTokenizer<SlideInitPlace> {
        @Override
        public String getToken(SlideInitPlace place) {
            return "";
        }

        @Override
        public SlideInitPlace getPlace(String token) {
            return new SlideInitPlace();
        }
    }
}
