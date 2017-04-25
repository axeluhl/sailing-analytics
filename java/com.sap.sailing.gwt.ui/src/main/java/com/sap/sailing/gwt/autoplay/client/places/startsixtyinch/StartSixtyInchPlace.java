package com.sap.sailing.gwt.autoplay.client.places.startsixtyinch;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayNode;

public class StartSixtyInchPlace extends Place {

    private FailureEvent failureEvent;
    private AutoPlayNode currentSlideConfig;

    public StartSixtyInchPlace(FailureEvent failureEvent, AutoPlayNode currentSlideConfig) {
        this.failureEvent = failureEvent;
        this.currentSlideConfig = currentSlideConfig;
    }

    public StartSixtyInchPlace() {
    }

    public FailureEvent getFailureEvent() {
        return failureEvent;
    }

    public AutoPlayNode getCurrentSlideConfig() {
        return currentSlideConfig;
    }

    public static class Tokenizer implements PlaceTokenizer<StartSixtyInchPlace> {
        @Override
        public String getToken(StartSixtyInchPlace place) {
            return "";
        }

        @Override
        public StartSixtyInchPlace getPlace(String token) {
            return new StartSixtyInchPlace();
        }
    }
}
