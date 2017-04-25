package com.sap.sailing.gwt.autoplay.client.places.startup.classic.initial;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.nodes.base.AutoPlayNode;

public class ClassicInitialPlace extends Place {

    private FailureEvent failureEvent;
    private AutoPlayNode currentSlideConfig;

    public ClassicInitialPlace(FailureEvent failureEvent, AutoPlayNode currentSlideConfig) {
        this.failureEvent = failureEvent;
        this.currentSlideConfig = currentSlideConfig;
    }

    public ClassicInitialPlace() {
    }

    public FailureEvent getFailureEvent() {
        return failureEvent;
    }

    public AutoPlayNode getCurrentSlideConfig() {
        return currentSlideConfig;
    }

    public static class Tokenizer implements PlaceTokenizer<ClassicInitialPlace> {
        @Override
        public String getToken(ClassicInitialPlace place) {
            return "";
        }

        @Override
        public ClassicInitialPlace getPlace(String token) {
            return new ClassicInitialPlace();
        }
    }
}
