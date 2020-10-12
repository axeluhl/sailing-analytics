package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import com.google.gwt.place.shared.PlaceTokenizer;

public class MarkPropertiesPlace extends AbstractCourseCreationPlace {
    
    public MarkPropertiesPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<MarkPropertiesPlace> {
        @Override
        public String getToken(final MarkPropertiesPlace place) {
            return "";
        }

        @Override
        public MarkPropertiesPlace getPlace(final String token) {
            return new MarkPropertiesPlace();
        }
    }
    
}
