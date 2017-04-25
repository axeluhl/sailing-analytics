package com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.initial;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayNode;

public class SixtyInchInitialPlace extends Place {

    private FailureEvent failureEvent;
    private AutoPlayNode currentSlideConfig;

    public SixtyInchInitialPlace(FailureEvent failureEvent, AutoPlayNode currentSlideConfig) {
        this.failureEvent = failureEvent;
        this.currentSlideConfig = currentSlideConfig;
    }

    public SixtyInchInitialPlace() {
    }

    public FailureEvent getFailureEvent() {
        return failureEvent;
    }

    public AutoPlayNode getCurrentSlideConfig() {
        return currentSlideConfig;
    }

    public static class Tokenizer implements PlaceTokenizer<SixtyInchInitialPlace> {
        @Override
        public String getToken(SixtyInchInitialPlace place) {
            return "";
        }

        @Override
        public SixtyInchInitialPlace getPlace(String token) {
            return new SixtyInchInitialPlace();
        }
    }
}
