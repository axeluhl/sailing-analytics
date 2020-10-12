package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import com.google.gwt.place.shared.PlaceTokenizer;

public class CourseLayoutPlace extends AbstractTrackedRacesPlace {
    
    public CourseLayoutPlace() {
    }
    
    public static class Tokenizer implements PlaceTokenizer<CourseLayoutPlace> {
        @Override
        public String getToken(final CourseLayoutPlace place) {
            return "";
        }

        @Override
        public CourseLayoutPlace getPlace(final String token) {
            return new CourseLayoutPlace();
        }
    }
    
}
