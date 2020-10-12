package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import com.google.gwt.place.shared.PlaceTokenizer;

public class MarkRolesPlace extends AbstractCourseCreationPlace {
    
    public MarkRolesPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<MarkRolesPlace> {
        @Override
        public String getToken(final MarkRolesPlace place) {
            return "";
        }

        @Override
        public MarkRolesPlace getPlace(final String token) {
            return new MarkRolesPlace();
        }
    }
    
}
