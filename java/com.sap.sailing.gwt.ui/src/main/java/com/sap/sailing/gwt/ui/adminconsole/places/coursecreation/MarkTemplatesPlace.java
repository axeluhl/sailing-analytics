package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import com.google.gwt.place.shared.PlaceTokenizer;

public class MarkTemplatesPlace extends AbstractCourseCreationPlace {
    
    public MarkTemplatesPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<MarkTemplatesPlace> {
        @Override
        public String getToken(final MarkTemplatesPlace place) {
            return "";
        }

        @Override
        public MarkTemplatesPlace getPlace(final String token) {
            return new MarkTemplatesPlace();
        }
    }
    
}
